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
import java.util.zip.Deflater;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.zip.AbstractZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipOutputStream;

/**
 * This archiver packages and signs a Google Chrome Extension.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public class CRXArchiver extends AbstractZipArchiver {

    /**
     * Used to as a mask when extracting 8 least significant bits of a integer.
     */
    private static final int BYTE_MASK = 0xFF;

    /**
     * The amounts in order to move bits 15 thru 8 into the 8 least significant bits.
     */
    private static final int SHIFT_8 = 8;

    /**
     * The amounts in order to move bits 23 thru 16 into the 8 least significant bits.
     */
    private static final int SHIFT_16 = 16;

    /**
     * The amounts in order to move bits 31 thru 24 into the 8 least significant bits.
     */
    private static final int SHIFT_24 = 24;

     /**
     * The magic number for CRX files.
     */
    private static final byte[] CRX_MAGIC = { 0x43, 0x72, 0x32, 0x34 };

    /**
     * The CRX header version number in little endian format.
     */
    private static final byte[] CRX_VERSION = { 0x02, 0x00, 0x00, 0x00 };

    /**
     * The location of the .pem file containing the public/private key pair.
     */
    private File pemFile;

    /**
     * The password used to secure the .pem file.
     */
    private String pemPassword;

    /**
     * Used to inject the location of the .pem file containing the public/private key pair.
     *
     * @param file The location of the .pem file.
     */
    public void setPemFile(final File file) {
        pemFile = file;
    }

    /**
     * Used to inject the password that was used to secure the .pem file.
     *
     * @param password The password.
     */
    public void setPemPassword(final String password) {
        pemPassword = password;
    }

    /**
     * Overriding the implementation in {@link AbstractZipArchiver} to set the packaging type to crx.
     *
     * @return Always returns {@code crx}.
     */
    @Override
    protected String getArchiveType() {
        return "crx";
    }

    /**
     * Generate an in-memory ZIP file containing the resources for the Google Chrome Extension, then sign the ZIP
     * and write out a CRX file containing the header, signature, public key and ZIP data.
     *
     * @throws ArchiverException If there was a problem packaging or signing the Google Chrome Extension.
     */
    @Override
    protected void execute() throws ArchiverException {

        Security.addProvider(new BouncyCastleProvider());

        // ZIP the CRX source directory tree

        final byte[] zipData = createZipFile();

        // Get the public/private key and sign the ZIP

        final KeyPair keyPair = getKeyPair();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        byte[] signature = sign(zipData, keyPair);

        // Write the CRX file

        outputCRX(getDestFile(), zipData, signature, publicKey);
    }

    /**
     * Generate the CRX file writing the header, public key, signature and data.
     *
     * @param crxFile   The target CRX file.
     * @param zipData   The zipped CRX contents.
     * @param signature The signature of the zipped CRX contents.
     * @param publicKey The public to be used when verifying signature.
     * @throws ArchiverException If there was an error writing the CRX file.
     */
    private void outputCRX(final File crxFile, final byte[] zipData, final byte[] signature,
                           final byte[] publicKey) throws
            ArchiverException {
        try {
            crxFile.getParentFile().mkdirs();
            final FileOutputStream crx = new FileOutputStream(crxFile);
            try {
                crx.write(CRX_MAGIC);
                crx.write(CRX_VERSION);
                writeLength(crx, publicKey.length);
                writeLength(crx, signature.length);
                crx.write(publicKey);
                crx.write(signature);
                crx.write(zipData);
            } finally {
                crx.close();
            }
        } catch (final IOException e) {
            throw new ArchiverException("Could not write CRX file", e);
        }
    }

    /**
     * Read the public/private key pair from a PEM file.
     *
     * @return The public/private key pair.
     * @throws ArchiverException If there was an error reading the public/private key pair from the file.
     */
    private KeyPair getKeyPair() throws ArchiverException {
        try {
            final Reader pemFileReader = new FileReader(pemFile);
            try {
                final PEMReader pemReader;
                if (pemPassword == null) {
                    pemReader = new PEMReader(pemFileReader);
                } else {
                    final PasswordFinder passwordFinder = new CRXPasswordFinder(pemPassword);
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
            throw new ArchiverException("Could not load the public/private key from the PEM file", e);
        }
    }

    /**
     * Generate the signature for a byte array using the private key.
     *
     * @param data    The byte array.
     * @param keyPair The public/private key pair.
     * @return The signature as a byte array.
     * @throws ArchiverException If there was a error generating the signature.
     */
    private byte[] sign(final byte[] data, final KeyPair keyPair) throws ArchiverException {
        try {
            final Signature signatureObject = Signature.getInstance("SHA1withRSA");
            signatureObject.initSign(keyPair.getPrivate());
            signatureObject.update(data);
            return signatureObject.sign();
        } catch (final GeneralSecurityException e) {
            throw new ArchiverException("Could not generate the signature for the CRX file", e);
        }
    }

    /**
     * Write a 32-bit integer to the output stream in little endian format.
     *
     * @param out The output stream.
     * @param val The 32-bit integer.
     * @throws IOException If there was a problem writing to the output stream.
     */
    private void writeLength(final OutputStream out, final int val) throws IOException {
        out.write(val & BYTE_MASK);
        out.write((val >> SHIFT_8) & BYTE_MASK);
        out.write((val >> SHIFT_16) & BYTE_MASK);
        out.write((val >> SHIFT_24) & BYTE_MASK);
    }

    /**
     * Create a ZIP file in memory containing the directory tree leveraging the {@link
     * AbstractZipArchiver#addResources(org.codehaus.plexus.archiver.ResourceIterator,
     * org.codehaus.plexus.archiver.zip.ZipOutputStream)} method to store resources in the ZIP file. The ZIP file is
     * then converted to a byte array.
     *
     * @return A byte array containing the ZIP file.
     * @throws ArchiverException If there was an error reading the contents of the source directory.
     */
    private byte[] createZipFile() throws ArchiverException {
        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            final ZipOutputStream out = new ZipOutputStream(buffer);
            out.setMethod(ZipOutputStream.DEFLATED);
            out.setLevel(Deflater.BEST_COMPRESSION);
            ResourceIterator resourceIterator = getResources();
            addResources(resourceIterator, out);
            out.close();
            return buffer.toByteArray();
        } catch (final IOException e) {
            throw new ArchiverException("Problem processing the Chrome Extension sources", e);
        }
    }
}
