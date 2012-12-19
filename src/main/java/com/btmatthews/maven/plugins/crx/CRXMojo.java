/*
 * Copyright 2012 Brian Matthews
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.btmatthews.maven.plugins.crx;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.filtering.MavenFileFilter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Implement the crx goal for the plug-in. The crx goal packages and signs a Chrome Browser Extension producing a file
 * with a .crx extension.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.0.0
 */
@Mojo(name = "crx", defaultPhase = LifecyclePhase.PACKAGE)
public class CRXMojo extends AbstractMojo {

    /**
     * The PEM file containing the public/private key.
     */
    @Parameter(defaultValue = "${crxPEMFile}", required = true)
    private File pemFile;

    /**
     * The password for the PEM file.
     */
    @Parameter(defaultValue = "${crxPEMPassword}")
    private String pemPassword;

    /**
     * The source directory for the Chrome Extension.
     */
    @Parameter(defaultValue = "${basedir}/src/main/chrome", required = true)
    private File crxSourceDirectory;

    /**
     * A comma separated list of inclusion rules.
     */
    @Parameter(required = false)
    private String packagingIncludes;

    /**
     * A comma separated list of exclusion rules.
     */
    @Parameter(required = false)
    private String packagingExcludes;

    /**
     * The final name of the generated artifact.
     */
    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    private String finalName;

    /**
     * The build target directory.
     */
    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * An optional classifier for the artifact.
     */
    @Parameter
    private String classifier;

    /**
     * Specify that the CRX sources should be filtered.
     *
     * @since 1.2.0
     */
    @Parameter(defaultValue = "false")
    private boolean filtering;

    /**
     * Filters (property files) to include during the interpolation of the pom.xml.
     *
     * @since 1.2.0
     */
    @Parameter
    private List filters;

    /**
     * A list of file extensions that should not be filtered if filtering is enabled.
     *
     * @since 1.2.0
     */
    @Parameter
    private List nonFilteredFileExtensions;

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The Maven project helper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The archiver component that is used to package and sign the Google Chrome Extension.
     */
    @Component(role = CRXArchiver.class, hint = "crx")
    private CRXArchiver crxArchiver;

    /**
     * Used to copy the file with resource filtering.
     *
     * @since 1.2.0
     */
    @Component(role = MavenFileFilter.class, hint = "default")
    private MavenFileFilter mavenFileFilter;

    /**
     * Used to perform the file filtering.
     *
     * @since 1.2.0
     */
    @Component(role = MavenResourcesFiltering.class, hint = "default")
    private MavenResourcesFiltering mavenResourcesFiltering;

    /**
     * The current Maven session.
     *
     * @since 1.2.0
     */
    @Component
    private MavenSession session;

    /**
     * File filtering wrappers.
     *
     * @since 1.2.0
     */
    private List filterWrappers;

    /**
     * Called when the Maven plug-in is executing. It creates an in-memory ZIP file of all the Chrome Extension
     * source files, generates as signature using the private key from the PEM file, outputs a CRX file containing
     * a header, the public key, the signature and the ZIP data.
     *
     * @throws MojoExecutionException If there was an error that should stop the build.
     * @throws MojoFailureException   If there was an error but the build might be allowed to continue.
     */
    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {

        // Make sure we have a manifest file for the CRX

        final File manifestFile = new File(crxSourceDirectory, "manifest.json");
        if (!manifestFile.exists()) {
            throw new MojoExecutionException("Missing manifest.json file");
        }

        // Generate CRX file name

        final StringBuilder crxFilename = new StringBuilder();
        crxFilename.append(finalName);
        if (StringUtils.isNotEmpty(classifier)) {
            crxFilename.append('-');
            crxFilename.append(classifier);
        }
        final File crxDirectory = new File(outputDirectory, crxFilename.toString());
        crxFilename.append(".crx");

        copyFiles(crxSourceDirectory, crxDirectory);

        // Generate the CRX file

        final File crxFile = new File(outputDirectory, crxFilename.toString());
        final String[] includes = ParameterUtils.splitParameter(packagingIncludes);
        final String[] excludes = ParameterUtils.splitParameter(packagingExcludes);

        crxArchiver.setPemFile(pemFile);
        crxArchiver.setPemPassword(pemPassword);
        crxArchiver.addDirectory(crxDirectory, includes, excludes);
        crxArchiver.setDestFile(crxFile);

        try {
            crxArchiver.createArchive();
        } catch (final IOException e) {
            throw new MojoExecutionException("Failed to package and sign the Google Chrome Extension", e);
        } catch (final ArchiverException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // Attach the artifact to the build life-cycle

        if (StringUtils.isNotEmpty(classifier)) {
            projectHelper.attachArtifact(project, "crx", classifier, crxFile);
        } else {
            project.getArtifact().setFile(crxFile);
        }
    }

    /**
     * Recursively copy from a source directory to a destination directory applying resource filtering if necessary.
     *
     * @param source      The source directory.
     * @param destination The destination directory.
     * @throws MojoExecutionException If there was an error during the recursive copying or filtering.
     * @since 1.2.0
     */
    private void copyFiles(final File source, final File destination) throws MojoExecutionException {
        try {
            if (!destination.exists() && !destination.mkdirs()) {
                throw new MojoExecutionException("Could not create directory: " + destination.getAbsolutePath());
            }
            for (final File sourceItem : source.listFiles()) {
                final File destinationItem = new File(destination, sourceItem.getName());
                if (sourceItem.isDirectory()) {
                    copyFiles(sourceItem, destinationItem);
                } else {
                    if (filtering && !isNonFilteredExtension(sourceItem.getName())) {
                        mavenFileFilter.copyFile(sourceItem, destinationItem, true, getFilterWrappers(), null);
                    } else {
                        FileUtils.copyFile(sourceItem, destinationItem);
                    }
                }
            }
        } catch (final MavenFilteringException e) {
            throw new MojoExecutionException("Failed to build filtering wrappers", e);
        } catch (final IOException e) {
            throw new MojoExecutionException("Error copying file: " + source.getAbsolutePath(), e);
        }
    }

    /**
     * Determine whether the file name should be filtered or not based on the list of excluded file extensions.
     *
     * @param fileName The file name.
     * @return {@code true} if the file extension is not excluded and the file should be filtered. Otherwise {@code
     *         false}.
     * @throws MojoExecutionException If there was an error determining whether the file name should be filtered.
     * @since 1.2.0
     */
    private boolean isNonFilteredExtension(final String fileName) throws MojoExecutionException {
        return !mavenResourcesFiltering.filteredFileExtension(fileName, nonFilteredFileExtensions);
    }

    /**
     * Build a list of filter wrappers.
     *
     * @return The list of filter wrappers.
     * @throws MojoExecutionException If there was a problem building the list of filter wrappers.
     * @since 1.2.0
     */
    private List getFilterWrappers()
            throws MojoExecutionException {
        if (filterWrappers == null) {
            try {
                final MavenResourcesExecution mavenResourcesExecution = new MavenResourcesExecution();
                mavenResourcesExecution.setEscapeString("\\");
                filterWrappers = mavenFileFilter.getDefaultFilterWrappers(project, filters, true, session,
                        mavenResourcesExecution);
            } catch (final MavenFilteringException e) {
                throw new MojoExecutionException("Failed to build filtering wrappers: " + e.getMessage(), e);
            }
        }
        return filterWrappers;
    }
}
