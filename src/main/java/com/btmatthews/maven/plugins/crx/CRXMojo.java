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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
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
    @Component(role = Archiver.class, hint = "crx")
    private CRXArchiver crxArchiver;

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
        crxFilename.append(".crx");

        // Generate the CRX file

        final File crxFile = new File(outputDirectory, crxFilename.toString());

        crxArchiver.setPemFile(pemFile);
        crxArchiver.setPemPassword(pemPassword);
        crxArchiver.addDirectory(crxSourceDirectory, null, null);
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
}
