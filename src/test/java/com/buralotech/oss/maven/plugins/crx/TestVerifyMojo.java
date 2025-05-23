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

package com.buralotech.oss.maven.plugins.crx;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test the {@link CRXVerifyMojo} which implements the crx-verify goal.
 *
 * @author <a href="mailto:bmatthews68@gmail.com">Brian Matthews</a>
 * @since 1.1.0
 */
@ExtendWith(MockitoExtension.class)
class TestVerifyMojo {

    /**
     * The name of the {@link SignatureHelper} field in {@link CRXVerifyMojo}.
     */
    private static final String SIGNATURE_HELPER_FIELD = "signatureHelper";

    /**
     * The name of the {@link ArchiveHelper} field in {@link CRXVerifyMojo}.
     */
    private static final String ARCHIVE_HELPER_FIELD = "archiveHelper";

    /**
     * The name of the output directory field in {@link CRXVerifyMojo}.
     */
    private static final String OUTPUT_DIRECTORY_FIELD = "outputDirectory";

    /**
     * The name of the crx path field in {@link CRXVerifyMojo}.
     */
    private static final String CRX_PATH_FIELD = "crxPath";

    /**
     * The name of the final name field in {@link CRXVerifyMojo}.
     */
    private static final String FINAL_NAME_FIELD = "finalName";

    /**
     * The name of the classifier field in {@link CRXVerifyMojo}.
     */
    private static final String CLASSIFIER_FIELD = "classifier";

    /**
     * The {@link CRXVerifyMojo} being tested.
     */
    private Mojo mojo;

    /**
     * Used to mock the {@link ArchiveHelper} for the unit test.
     */
    @Mock
    private ArchiveHelper archiveHelper;

    /**
     * Used to mock the {@link SignatureHelper} for the unit test.
     */
    @Mock
    private SignatureHelper signatureHelper;

    /**
     * Temporary directory used for output.
     */
    @TempDir
    private File outputDirectory;

    /**
     * Prepare for the unit test execution creating mock objects and configuring the {@link CRXVerifyMojo}.
     *
     * @throws Exception If there was an unexpected exception executing the test case.
     */
    @BeforeEach
    void setUp() throws Exception {
        mojo = new CRXVerifyMojo();
        setVariableValueInObject(mojo, OUTPUT_DIRECTORY_FIELD, outputDirectory);
        setVariableValueInObject(mojo, SIGNATURE_HELPER_FIELD, signatureHelper);
        setVariableValueInObject(mojo, ARCHIVE_HELPER_FIELD, archiveHelper);
        setVariableValueInObject(mojo, FINAL_NAME_FIELD, "HelloWorld");
    }

    /**
     * Verify that the {@link CRXVerifyMojo} when the signature is valid.
     *
     * @throws Exception If there was an unexpected error.
     */
    @Test
    void testValidSignature() throws Exception {
        final CRXArchive archive = new DummyArchive();
        when(archiveHelper.readArchive(any(File.class))).thenReturn(archive);
        when(signatureHelper.check(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(true);
        mojo.execute();
        verify(archiveHelper).readArchive(any(File.class));
        verify(signatureHelper).check(same(DummyArchive.DATA), any(PublicKey.class), same(DummyArchive.SIGNATURE));
    }

    /**
     * Verify that the {@link CRXVerifyMojo} when the signature is valid and a classifier has been specified.
     *
     * @throws Exception If there was an unexpected error.
     */
    @Test
    void testValidSignatureWithClassifier() throws Exception {
        final CRXArchive archive = new DummyArchive();
        when(archiveHelper.readArchive(any(File.class))).thenReturn(archive);
        when(signatureHelper.check(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(true);
        setVariableValueInObject(mojo, CLASSIFIER_FIELD, "debug");
        mojo.execute();
        verify(archiveHelper).readArchive(any(File.class));
        verify(signatureHelper).check(any(byte[].class), any(PublicKey.class), any(byte[].class));
    }

    /**
     * Verify that the {@link CRXVerifyMojo} when the signature is valid and the crxPath parameter has been set.
     *
     * @throws Exception If there was an unexpected error.
     */
    @Test
    void testValidSignatureWithCRXPath() throws Exception {
        final CRXArchive archive = new DummyArchive();
        when(archiveHelper.readArchive(any(File.class))).thenReturn(archive);
        when(signatureHelper.check(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(true);
        setVariableValueInObject(mojo, CRX_PATH_FIELD, new File(outputDirectory,
                "HelloWorld-1.0.0-SNAPSHOT.crx"));
        mojo.execute();
        verify(archiveHelper).readArchive(any(File.class));
        verify(signatureHelper).check(any(byte[].class), any(PublicKey.class), any(byte[].class));
    }

    /**
     * Verify that the {@link CRXVerifyMojo} throws an {@link MojoFailureException} when the signature check fails.
     *
     * @throws Exception If there was an expected or unexpected error.
     */
    @Test
    void testSignatureCheckFailure() throws Exception {
        final CRXArchive archive = new DummyArchive();
        when(archiveHelper.readArchive(any(File.class))).thenReturn(archive);
        when(signatureHelper.check(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(false);
        assertThrows(MojoFailureException.class, () -> mojo.execute());
    }

    /**
     * Verify that the {@link CRXVerifyMojo} throws an {@link MojoFailureException} when the signature is invalid.
     *
     * @throws Exception If there was an expected or unexpected error.
     */
    @Test
    void testInvalidSignature() throws Exception {
        final CRXArchive archive = new DummyArchive();
        when(archiveHelper.readArchive(any(File.class))).thenReturn(archive);
        when(signatureHelper.check(any(byte[].class), any(PublicKey.class),
                any(byte[].class))).thenThrow(GeneralSecurityException.class);
        assertThrows(MojoExecutionException.class, () -> mojo.execute());
    }

    /**
     * Verify that the {@link CRXVerifyMojo} throws an {@link MojoExecutionException} when the archive is invalid.
     *
     * @throws Exception If there was an expected or unexpected error.
     */
    @Test
    void testInvalidArchive() throws Exception {
        when(archiveHelper.readArchive(any(File.class))).thenThrow(IOException.class);
        assertThrows(MojoExecutionException.class, () -> mojo.execute());
    }

    /**
     * Verify that the {@link CRXVerifyMojo} throws an {@link MojoFailureException} when the archive cannot be found.
     *
     * @throws Exception If there was an expected or unexpected error.
     */
    @Test
    void testCannotFindArchive() throws Exception {
        when(archiveHelper.readArchive(any(File.class))).thenThrow(FileNotFoundException.class);
        assertThrows(MojoExecutionException.class, () -> mojo.execute());
    }
}
