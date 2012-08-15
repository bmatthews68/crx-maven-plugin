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

import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.isNull;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

/**
 * Unit test the {@link CRXMojo} class.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.0.0
 */
public class TestMojo {

    /**
     * The name of the artifact final name field in the {@link CRXMojo} class.
     */
    private static final String FINAL_NAME_FIELD = "finalName";

    /**
     * The name of the password for the PEM file field in the {@link CRXMojo} class.
     */
    private static final String PEM_PASSWORD_FIELD = "pemPassword";

    /**
     * The name of the PEM file path field in the {@link CRXMojo} class.
     */
    private static final String PEM_FILE_FIELD = "pemFile";

    /**
     * The name of the source directory path field in the {@link CRXMojo} class.
     */
    private static final String CRX_SOURCE_DIRECTORY_FIELD = "crxSourceDirectory";

    /**
     * The name of the output directory path field in the {@link CRXMojo} class.
     */
    private static final String OUTPUT_DIRECTORY_FIELD = "outputDirectory";

    /**
     * The name of the {@link MavenProject} field in the {@link CRXMojo} class.
     */
    private static final String PROJECT_FIELD = "project";

    /**
     * The name of the {@link MavenProjectHelper} field in the {@link CRXMojo} class.
     */
    private static final String PROJECT_HELPER_FIELD = "projectHelper";

    /**
     * The name of the {@link CRXArchiver} field in the {@link CRXMojo} class.
     */
    private static final String CRX_ARCHIVER_FIELD = "crxArchiver";

    /**
     * The name of the artifact classifier filed in the {@link CRXMojo} class.
     */
    private static final String CLASSIFIER_FIELD = "classifier";

    private static final String PACKAGING_INCLUDES_FIELD = "packagingIncludes";

    private static final String PACKAGING_EXCLUDES_FIELD = "packagingExcludes";

    /**
     * Temporary folder in which the generated .crx file is to be created. JUnit will automatically dispose of this
     * temporary folder and its contents after the unit tests are completed.
     */
    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();

    /**
     * The mojo used for all unit tests.
     */
    private CRXMojo mojo;

    /**
     * The mock Maven project model used to configure the mojo for unit testing.
     */
    @Mock
    private MavenProject project;

    /**
     * The mock Maven artifact descriptor used to configure the mojo for unit testing.
     */
    @Mock
    private Artifact artifact;

    /**
     * The mock Maven project helper used to configure the mojo for unit testing
     */
    @Mock
    private MavenProjectHelper projectHelper;

    /**
     * The mock {@link CRXArchiver} that would be used to output the CRX archiver.
     */
    @Mock
    private CRXArchiver archiver;

    /**
     * Create the test fixtures including the CRX mojo and a mocked the Maven project model and Maven project
     * helper.
     *
     * @throws Exception If there was an error configuring the CRX mojo.
     */
    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mojo = new CRXMojo();
        when(project.getArtifact()).thenReturn(artifact);
        setVariableValueInObject(mojo, OUTPUT_DIRECTORY_FIELD, outputDirectory.getRoot());
        setVariableValueInObject(mojo, PROJECT_FIELD, project);
        setVariableValueInObject(mojo, PROJECT_HELPER_FIELD, projectHelper);
        setVariableValueInObject(mojo, CRX_ARCHIVER_FIELD, archiver);
        setVariableValueInObject(mojo, FINAL_NAME_FIELD, "HelloWorld");
        setVariableValueInObject(mojo, PEM_FILE_FIELD, new File("target/test-classes/crxtest.pem"));
        setVariableValueInObject(mojo, CRX_SOURCE_DIRECTORY_FIELD, new File("target/test-classes/HelloWorld"));
    }

    /**
     * Verify that a .crx file can be created without a pemPassword or classifier property.
     *
     * @throws Exception If there was an unexpected error during the test case execution.
     */
    @Test
    public void testMojo() throws Exception {
        mojo.execute();
        verify(archiver).setPemFile(any(File.class));
        verify(archiver).setPemPassword(isNull(String.class));
        verify(archiver).addDirectory(any(File.class), isNull(String[].class), isNull(String[].class));
        verify(archiver).setDestFile(any(File.class));
        verify(archiver).createArchive();
        verify(artifact).setFile(any(File.class));
    }

    /**
     * Verify that a .crx file can be created with a pemPassword but without a classifier property.
     *
     * @throws Exception If there was an  unexpected error during the test case execution.
     */
    @Test
    public void testMojoWithPasssword() throws Exception {
        setVariableValueInObject(mojo, PEM_FILE_FIELD, new File("target/test-classes/crxtest1.pem"));
        setVariableValueInObject(mojo, PEM_PASSWORD_FIELD, "everclear");
        mojo.execute();
        verify(archiver).setPemFile(any(File.class));
        verify(archiver).setPemPassword(eq("everclear"));
        verify(archiver).addDirectory(any(File.class), isNull(String[].class), isNull(String[].class));
        verify(archiver).setDestFile(any(File.class));
        verify(archiver).createArchive();
        verify(artifact).setFile(any(File.class));
    }

    /**
     * Verify that a .crx file can be created without a classifier property.
     *
     * @throws Exception If there was an  unexpected error during the test case execution.
     */
    @Test
    public void testMojoWithClassifier() throws Exception {
        setVariableValueInObject(mojo, CLASSIFIER_FIELD, "debug");
        mojo.execute();
        verify(archiver).setPemFile(any(File.class));
        verify(archiver).setPemPassword(isNull(String.class));
        verify(archiver).addDirectory(any(File.class), isNull(String[].class), isNull(String[].class));
        verify(archiver).setDestFile(any(File.class));
        verify(archiver).createArchive();
        verify(projectHelper).attachArtifact(same(project), eq("crx"), eq("debug"), any(File.class));
    }

    /**
     * Verify that an exception is thrown if there is no manifest file in the source directory.
     *
     * @throws Exception If there was an expected or unexpected error during the test case execution.
     */
    @Test(expected = MojoExecutionException.class)
    public void testMojoFailsWithoutManifest() throws Exception {
        setVariableValueInObject(mojo, CRX_SOURCE_DIRECTORY_FIELD, new File("target/test-classes/GoodbyeWorld"));
        mojo.execute();
    }

    /**
     * Verify that {@link CRXMojo} will throw an {@link MojoExecutionException} when {@link CRXArchiver#createArchive()}
     * throws an {@link IOException}.
     *
     * @throws Exception If there was an expected or unexpected error during the test case execution.
     */
    @Test(expected = MojoExecutionException.class)
    public void testMojoFailsAfterIOException() throws Exception {
        doThrow(IOException.class).when(archiver).createArchive();
        mojo.execute();
    }

    /**
     * Verify that {@link CRXMojo} will throw an {@link MojoExecutionException} when {@link CRXArchiver#createArchive
     * ()} throws an {@link ArchiverException}.
     *
     * @throws Exception If there was an expected or unexpected error during the test case execution.
     */
    @Test(expected = MojoExecutionException.class)
    public void testMojoFailsAfterArchiverException() throws Exception {
        doThrow(ArchiverException.class).when(archiver).createArchive();
        mojo.execute();
    }

    /**
     * Verify that the {@link CRXMojo} handles inclusion rules.
     *
     * @throws Exception If there was an unexpected exception.
     */
    @Test
    public void testWithIncludes() throws Exception {
        setVariableValueInObject(mojo, PACKAGING_INCLUDES_FIELD, "manifest.json,popup.js,popup.html,icon.png");
        mojo.execute();
        verify(archiver).setPemFile(any(File.class));
        verify(archiver).setPemPassword(isNull(String.class));
        verify(archiver).addDirectory(any(File.class),
                aryEq(new String[]{ "manifest.json", "popup.js", "popup.html", "icon.png" }), isNull(String[].class));
        verify(archiver).setDestFile(any(File.class));
        verify(archiver).createArchive();
        verify(artifact).setFile(any(File.class));
    }

    /**
     * Verify that the {@link CRXMojo} handles exclusion rules.
     *
     * @throws Exception If there was an unexpected exception.
     */
    @Test
    public void testWithExcludes() throws Exception {
        setVariableValueInObject(mojo, PACKAGING_EXCLUDES_FIELD, "WEB-INF/**");
        mojo.execute();
        verify(archiver).setPemFile(any(File.class));
        verify(archiver).setPemPassword(isNull(String.class));
        verify(archiver).addDirectory(any(File.class), isNull(String[].class), aryEq(new String[]{ "WEB-INF/**" }));
        verify(archiver).setDestFile(any(File.class));
        verify(archiver).createArchive();
        verify(artifact).setFile(any(File.class));
    }

    /**
     * Verify that the {@link CRXMojo} handles inclusion and exclusion rules.
     *
     * @throws Exception If there was an unexpected exception.
     */
    @Test
    public void testWithIncludesAndExcludes() throws Exception {
        setVariableValueInObject(mojo, PACKAGING_INCLUDES_FIELD, "manifest.json,popup.js,popup.html,icon.png");
        setVariableValueInObject(mojo, PACKAGING_EXCLUDES_FIELD, "WEB-INF/**");
        mojo.execute();
        verify(archiver).setPemFile(any(File.class));
        verify(archiver).setPemPassword(isNull(String.class));
        verify(archiver).addDirectory(any(File.class),
                aryEq(new String[]{ "manifest.json", "popup.js", "popup.html", "icon.png" }),
                aryEq(new String[]{ "WEB-INF/**" }));
        verify(archiver).setDestFile(any(File.class));
        verify(archiver).createArchive();
        verify(artifact).setFile(any(File.class));
    }
}
