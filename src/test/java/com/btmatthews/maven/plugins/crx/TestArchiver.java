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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.byteThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import org.codehaus.plexus.archiver.ArchiverException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test the {@link CRXArchiver} in isolation.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public class TestArchiver {

    /**
     * The {@link CRXArchiver} used in unit tests.
     */
    private CRXArchiverImpl archiver;

    /**
     * Temporary folder in which the generated .crx file will be created. JUnit will automatically dispose of this
     * temporary folder and its contents after the unit tests are completed.
     */
    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();

    /**
     * Initialise the unit tests.
     */
    @Before
    public void setUp() throws Exception {
        archiver = new CRXArchiverImpl();
        archiver.setDestFile(new File(outputDirectory.getRoot(), "HelloWord-1.0.0-SNAPSHOT.crx"));
        archiver.setSignatureHelper(new CRXSignatureHelper());
        archiver.setArchiveHelper(new CRXArchiveHelper());
    }

    @Test
    public void testType() {
        assertEquals("crx", archiver.getArchiveType());
    }

    /**
     * Verify that a .crx file can be created without a pemPassword or classifier property.
     *
     * @throws Exception If there was an error executing the unit test.
     */
    @Test
    public void testArchiver() throws Exception {
        archiver.setPemFile(new File("target/test-classes/crxtest.pem"));
        archiver.addDirectory(new File("target/test-classes/HelloWorld"), null, null);
        archiver.createArchive();
    }

    /**
     * Verify that the {@link CRXMojo} will overwrite an existing .crx file
     *
     * @throws Exception If there was an unexpected error during the test case execution.
     */
    @Test
    public void testArchiverWhenFileAlreadyExists() throws Exception {
        outputDirectory.newFile("HelloWord-1.0.0-SNAPSHOT.crx");
        archiver.setPemFile(new File("target/test-classes/crxtest.pem"));
        archiver.addDirectory(new File("target/test-classes/HelloWorld"), null, null);
        archiver.createArchive();
    }

    /**
     * Verify that a .crx file can be created with a pemPassword but without a classifier property.
     *
     * @throws Exception If there was an error executing the unit test.
     */
    @Test
    public void testArchiverWithPasssword() throws Exception {
        archiver.setPemFile(new File("target/test-classes/crxtest1.pem"));
        archiver.setPemPassword("everclear");
        archiver.addDirectory(new File("target/test-classes/HelloWorld"), null, null);
        archiver.createArchive();
    }

    /**
     * Verify that an exception is raised when trying to sign a .crx file with a nonexistent PEM file.
     *
     * @throws Exception If there was an error executing the unit test.
     */
    @Test(expected = ArchiverException.class)
    public void testArchiverWhenPEMFileDoesNotExist() throws Exception {
        archiver.setPemFile(new File("target/test-classes/crxtest2.pem"));
        archiver.setPemPassword("everclear");
        archiver.addDirectory(new File("target/test-classes/HelloWorld"), null, null);
        archiver.createArchive();
    }

    /**
     * Verify that an exception is raised when trying to sign a .crx file with a corrupted PEM file.
     *
     * @throws Exception If there was an error executing the unit test.
     */
    @Test(expected = ArchiverException.class)
    public void testArchiverWhenPEMFileIsCorrupted() throws Exception {
        archiver.setPemFile(new File("target/test-classes/crxtest3.pem"));
        archiver.setPemPassword("everclear");
        archiver.addDirectory(new File("target/test-classes/HelloWorld"), null, null);
        archiver.createArchive();
    }

    /**
     * Verify that an exception is raised when the signing fails.
     *
     * @throws Exception If there was an exepected or unexpected error executing the unit test.
     */
    @Test(expected = ArchiverException.class)
    public void testArchiverWhenSignatureHelperFails() throws Exception {
        final SignatureHelper helper = mock(SignatureHelper.class);
        when(helper.sign(any(byte[].class), any(KeyPair.class))).thenThrow(GeneralSecurityException.class);
        archiver.setPemFile(new File("target/test-classes/crxtest.pem"));
        archiver.addDirectory(new File("target/test-classes/HelloWorld"), null, null);
        archiver.setSignatureHelper(helper);
        archiver.createArchive();
    }

    /**
     * Verify that an exception is raised when writing the archive failes.
     *
     * @throws Exception If there was an exepected or unexpected error executing the unit test.
     */
    @Test(expected = ArchiverException.class)
    public void testArchiverWhenArchiveHelperFails() throws Exception {
        final ArchiveHelper helper = mock(ArchiveHelper.class);
        doThrow(IOException.class).when(helper).writeArchive(any(File.class), any(byte[].class), any(byte[].class),
                any(byte[].class));
        archiver.setPemFile(new File("target/test-classes/crxtest.pem"));
        archiver.addDirectory(new File("target/test-classes/HelloWorld"), null, null);
        archiver.setArchiveHelper(helper);
        archiver.createArchive();
    }
}
