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

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Security;
import java.security.Signature;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;

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
     * The magic number for CRX files.
     */
    private static final byte[] CRX_MAGIC = { 0x43, 0x72, 0x32, 0x34 };

    /**
     * The CRX header version number in little endian format.
     */
    private static final byte[] CRX_VERSION = { 0x02, 0x00, 0x00, 0x00 };

    /**
     * The PEM file containing the public/private key.
     */
    @Parameter(required = true)
    private File pemFile;

    /**
     * The password for the PEM file.
     */
    @Parameter
    private String pemPassword;

    /**
     * The source directory for the Chrome Extension.
     */
    @Parameter(defaultValue = "${basedir}/src/main/chrome", required = true)
    private File crxSourceDirectory;

    /**
     * The final name of the generated artifact.
     */
    @Parameter(defaultValue = "project.build.finalName", required = true)
    private String finalName;

    /**
     * The build target directory.
     */
    @Parameter(defaultValue = "${project.build.directory", required = true)
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
     * The default constructor.
     */
    public CRXMojo() {
    }

    /**
     * Called when the Maven plug-in is executing. It creates an in-memory ZIP file of all the Chrome Extension
     * source files, generates as signature using the private key from the PEM file, outputs a CRX file containing
     * a header, the public key, the signature and the ZIP data.
     *
     * @throws MojoExecutionException If there was an error that should stop the build.
     * @throws MojoFailureException   If there was an error but the build might be allowed to continue.
     */

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        // Make sure we have a manifest file for the CRX

        final File manifestFile = new File(crxSourceDirectory, "manifest.json");
        if (!manifestFile.exists()) {
            throw new MojoExecutionException("Missing manifest.json file");
        }

        // Add the Bouncy Castle security provider

        Security.addProvider(new BouncyCastleProvider());

        // ZIP the CRX source directory tree

        final byte[] zipData = createZipFile();

        // Get the public/private key and sign the ZIP

        final KeyPair keyPair = getKeyPair();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] signature = sign(zipData, keyPair);

        // Generate CRX file name

        final StringBuilder crxFilename = new StringBuilder();
        crxFilename.append(finalName);
        crxFilename.append(".crx");

        // Generate the CRX file

        final File crxFile = new File(outputDirectory, crxFilename.toString());
        outputCRX(crxFile, zipData, signature, publicKey);

        // Attach the artifact to the build life-cycle

        if (classifier != null) {
            projectHelper.attachArtifact(project, "crx", classifier, crxFile);
        } else {
            project.setFile(crxFile);
        }
    }

    private void outputCRX(final File crxFile, final byte[] zipData, final byte[] signature,
                           final byte[] publicKey) throws
            MojoExecutionException {
        try {
            final FileOutputStream crx = new FileOutputStream(crxFile);
            crx.write(CRX_MAGIC);
            crx.write(CRX_VERSION);
            writeLength(crx, publicKey.length);
            writeLength(crx, signature.length);
            crx.write(publicKey);
            crx.write(signature);
            crx.write(zipData);
            crx.close();
        } catch (final IOException e) {
            throw new MojoExecutionException("Could not write CRX file", e);
        }
    }

    /**
     * Read the public/private key pair from a PEM file.
     *
     * @return The public/private key pair.
     * @throws MojoExecutionException If there was an error reading the public/private key pair from the file.
     */
    private KeyPair getKeyPair() throws MojoExecutionException {
        try {
            final Reader pemFileReader = new FileReader(pemFile);
            try {
                final PEMReader pemReader;
                if (pemPassword == null) {
                    pemReader = new PEMReader(pemFileReader);
                } else {
                    final PasswordFinder passwordFinder = new SimplePasswordFinder(pemPassword);
                    pemReader = new PEMReader(pemFileReader, passwordFinder);
                }
                try {
                    return (KeyPair)pemReader.readObject();
                } finally {
                    pemReader.close();
                }
            } finally {
                pemFileReader.close();
            }
        } catch (final IOException e) {
            throw new MojoExecutionException("Could not load the public/private key from the PEM file", e);
        }
    }

    /**
     * Generate the signature for a byte array using the private key.
     *
     * @param data    The byte array.
     * @param keyPair The public/private key pair.
     * @return The signature as a byte array.
     * @throws MojoExecutionException If there was a error generating the signature.
     */
    private byte[] sign(final byte[] data, final KeyPair keyPair) throws MojoExecutionException {
        try {
            final Signature signatureObject = Signature.getInstance("SHA1withRSA");
            signatureObject.initSign(keyPair.getPrivate());
            signatureObject.update(data);
            return signatureObject.sign();
        } catch (final GeneralSecurityException e) {
            throw new MojoExecutionException("Could not generate the signature for the CRX file", e);
        }
    }

    /**
     * Write a 32-bit integer to the output stream in little endian format.
     *
     * @param out The output stream.
     * @param val The 32-bit integer.
     */

    private void writeLength(final OutputStream out, final int val) throws IOException {
        out.write(val & 0xFF);
        out.write((val >> 8) & 0xFF);
        out.write((val >> 16) & 0xFF);
        out.write((val >> 24) & 0xFF);
    }

    /**
     * Create a ZIP file in memory containing the directory tree.
     *
     * @return A byte array containing the ZIP file.
     * @throws MojoExecutionException If there was an error reading the contents of the source directory.
     */
    private byte[] createZipFile() throws MojoExecutionException {
        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ZipOutputStream out = new ZipOutputStream(buffer);
            zipDirectory(crxSourceDirectory, out);
            out.close();
            return buffer.toByteArray();
        } catch (final IOException e) {
            throw new MojoExecutionException("Problem processing the Chrome Extension sources", e);
        }
    }

    /**
     * Recursively compress the contents of a directory tree to a ZIP output stream.
     *
     * @param directory The root of the directory tree.
     * @param out       The ZIP output stream.
     * @throws IOException If there was an error reading the
     *                     directory contents or writing to the ZIP output stream.
     */
    private void zipDirectory(final File directory, final ZipOutputStream out) throws IOException {
        final String[] itemNames = directory.list();
        for (final String itemName : itemNames) {
            final File itemFile = new File(directory, itemName);
            if (itemFile.isDirectory()) {
                zipDirectory(itemFile, out);
            } else {
                final FileInputStream itemInput = new FileInputStream(itemFile);
                try {
                    final String itemPath = crxSourceDirectory.toURI().relativize(itemFile.toURI()).getPath();
                    final ZipEntry entry = new ZipEntry(itemPath);
                    out.putNextEntry(entry);
                    int bytesRead;
                    byte[] byteBuffer = new byte[65536];
                    while ((bytesRead = itemInput.read(byteBuffer)) != -1) {
                        out.write(byteBuffer, 0, bytesRead);
                    }
                } finally {
                    itemInput.close();
                }
            }
        }
    }
}
