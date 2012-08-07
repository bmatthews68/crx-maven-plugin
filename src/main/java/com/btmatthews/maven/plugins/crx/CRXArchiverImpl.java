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
import java.util.zip.Deflater;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.zip.AbstractZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipOutputStream;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * This archiver packages and signs a Google Chrome Extension.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
@Component(role = CRXArchiver.class, hint = "crx", instantiationStrategy = "per-lookup")
public class CRXArchiverImpl extends AbstractZipArchiver implements CRXArchiver {

    /**
     * The location of the .pem file containing the public/private key pair.
     */
    private File pemFile;

    /**
     * The password used to secure the .pem file.
     */
    private String pemPassword;

    /**
     * The helper that is used to sign the ZIP archive.
     */
    @Requirement(hint = "crx")
    private SignatureHelper signatureHelper;

    /**
     * The helper that is used to output the CRX archive.
     */
    @Requirement(hint = "crx")
    private ArchiveHelper archiveHelper;

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
    public final void setPemPassword(final String password) {
        pemPassword = password;
    }

    /**
     * Used to inject the signature helper that is used to to sign the ZIP archive.
     *
     * @param helper The helper.
     */
    public void setSignatureHelper(final SignatureHelper helper) {
        signatureHelper = helper;
    }

    /**
     * Used to inject the archive helper that is used to output the CRX archive.
     *
     * @param helper The helper.
     */
    public void setArchiveHelper(final ArchiveHelper helper) {
        archiveHelper = helper;
    }

    /**
     * Overriding the implementation in {@link org.codehaus.plexus.archiver.zip.AbstractZipArchiver} to set the
     * packaging type to crx.
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
     */
    @Override
    protected void execute() {

        try {
            Security.addProvider(new BouncyCastleProvider());

            // ZIP the CRX source directory tree

            final byte[] zipData = createZipFile();

            // Get the public/private key and sign the ZIP

            final KeyPair keyPair = getKeyPair();
            byte[] publicKey = keyPair.getPublic().getEncoded();
            byte[] signature = signatureHelper.sign(zipData, keyPair.getPrivate());

            // Write the CRX file

            archiveHelper.writeArchive(getDestFile(), zipData, signature, publicKey);
        } catch (final GeneralSecurityException e) {
            throw new ArchiverException("Could not generate the signature for the CRX file", e);
        } catch (final IOException e) {
            throw new ArchiverException("Could not read resources or output the CRX file", e);
        }
    }

    /**
     * Read the public/private key pair from a PEM file.
     *
     * @return The public/private key pair.
     */
    private KeyPair getKeyPair() {
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
     * Create a ZIP file in memory containing the directory tree leveraging the {@link
     * org.codehaus.plexus.archiver.zip.AbstractZipArchiver#addResources(org.codehaus.plexus.archiver.ResourceIterator,
     * org.codehaus.plexus.archiver.zip.ZipOutputStream)} method to store resources in the ZIP file. The ZIP file is
     * then converted to a byte array.
     *
     * @return A byte array containing the ZIP file.
     * @throws java.io.IOException If there was an error reading the contents of the source directory.
     */
    private byte[] createZipFile() throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final ZipOutputStream out = new ZipOutputStream(buffer);
        try {
            out.setMethod(ZipOutputStream.DEFLATED);
            out.setLevel(Deflater.BEST_COMPRESSION);
            ResourceIterator resourceIterator = getResources();
            addResources(resourceIterator, out);
        } finally {
            out.close();
        }
        return buffer.toByteArray();
    }
}
