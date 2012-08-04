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

import static org.apache.maven.plugin.testing.ArtifactStubFactory.setVariableValueToObject;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
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
        setVariableValueToObject(mojo, "outputDirectory", outputDirectory.getRoot());
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "projectHelper", projectHelper);
        setVariableValueToObject(mojo, "crxArchiver", archiver);
    }

    /**
     * Verify that a .crx file can be created without a pemPassword or classifier property.
     *
     * @throws Exception If there was an unexpected error during the test case execution.
     */
    @Test
    public void testMojo() throws Exception {
        setVariableValueToObject(mojo, "finalName", "HelloWorld");
        setVariableValueToObject(mojo, "pemFile", new File("target/test-classes/crxtest.pem"));
        setVariableValueToObject(mojo, "crxSourceDirectory", new File("target/test-classes/HelloWorld"));
        mojo.execute();
        verify(archiver).createArchive();
        verify(artifact).setFile(any(File.class));
    }

    /**
     * Verify that a .crx file can be created with a pemPassword but without a classifier property.
     * <p/>
     * v     * @throws Exception If there was an  unexpected error during the test case execution.
     */
    @Test
    public void testMojoWithPasssword() throws Exception {
        setVariableValueToObject(mojo, "finalName", "HelloWorld");
        setVariableValueToObject(mojo, "pemFile", new File("target/test-classes/crxtest1.pem"));
        setVariableValueToObject(mojo, "pemPassword", "everclear");
        setVariableValueToObject(mojo, "crxSourceDirectory", new File("target/test-classes/HelloWorld"));
        mojo.execute();
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
        setVariableValueToObject(mojo, "finalName", "HelloWorld");
        setVariableValueToObject(mojo, "pemFile", new File("target/test-classes/crxtest.pem"));
        setVariableValueToObject(mojo, "classifier", "debug");
        setVariableValueToObject(mojo, "crxSourceDirectory", new File("target/test-classes/HelloWorld"));
        mojo.execute();
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
        setVariableValueToObject(mojo, "finalName", "HelloWorld");
        setVariableValueToObject(mojo, "pemFile", new File("target/test-classes/crxtest.pem"));
        setVariableValueToObject(mojo, "classifier", "debug");
        setVariableValueToObject(mojo, "crxSourceDirectory", new File("target/test-classes/GoodbyeWorld"));
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
        doThrow(new IOException("Expected exception")).when(archiver).createArchive();
        setVariableValueToObject(mojo, "finalName", "HelloWorld");
        setVariableValueToObject(mojo, "pemFile", new File("target/test-classes/crxtest.pem"));
        setVariableValueToObject(mojo, "crxSourceDirectory", new File("target/test-classes/HelloWorld"));
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
        doThrow(new ArchiverException("Expected exception")).when(archiver).createArchive();
        setVariableValueToObject(mojo, "finalName", "HelloWorld");
        setVariableValueToObject(mojo, "pemFile", new File("target/test-classes/crxtest.pem"));
        setVariableValueToObject(mojo, "crxSourceDirectory", new File("target/test-classes/HelloWorld"));
        mojo.execute();
    }
}
