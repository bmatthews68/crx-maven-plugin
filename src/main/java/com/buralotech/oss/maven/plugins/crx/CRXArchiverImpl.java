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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.concurrent.ExecutionException;
import java.util.zip.Deflater;

import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.zip.AbstractZipArchiver;
import org.codehaus.plexus.archiver.zip.ConcurrentJarCreator;

import javax.inject.Inject;

/**
 * This archiver packages and signs a Google Chrome Extension.
 *
 * @author <a href="mailto:bmatthews68@gmail.com">Brian Matthews</a>
 * @since 1.1.0
 */
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
    @Inject
    private SignatureHelper signatureHelper;

    /**
     * The helper that is used to output the CRX archive.
     */
    @Inject
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
     * Used to inject the signature helper that is used to sign the ZIP archive.
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

            final CRXArchive archive = new CRXArchive(publicKey, signature, zipData);
            archiveHelper.writeArchive(getDestFile(), archive);
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
            try (Reader pemFileReader = new FileReader(pemFile)) {
                try (PEMParser pemParser = new PEMParser(pemFileReader)) {
                    final Object pemObject = pemParser.readObject();
                    if (pemObject instanceof KeyPair) {
                        return (KeyPair) pemObject;
                    } else if (pemObject instanceof PEMKeyPair) {
                        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                        return converter.getKeyPair((PEMKeyPair) pemObject);
                    } else if (pemObject instanceof PEMEncryptedKeyPair) {
                        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                        final PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) pemObject;
                        final PEMDecryptorProvider decryptorProvider = new BcPEMDecryptorProvider(pemPassword.toCharArray());
                        final PEMKeyPair pemKeyPair = encryptedKeyPair.decryptKeyPair(decryptorProvider);
                        return converter.getKeyPair(pemKeyPair);
                    } else if (pemObject instanceof PrivateKeyInfo) {
                        final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                        final PrivateKey privateKey = converter.getPrivateKey((PrivateKeyInfo) pemObject);
                        return convertRSAPrivateCrtKey(privateKey);
                    } else {
                        return convertRSAPrivateCrtKey(pemObject);
                    }
                }
            }
        } catch (final InvalidKeySpecException e) {
            throw new ArchiverException("Cannot generate RSA public key", e);
        } catch (final NoSuchAlgorithmException e) {
            throw new ArchiverException("RSA Private key algorithm is not supported", e);
        } catch (final NoSuchProviderException e) {
            throw new ArchiverException("Bouncy Castle not registered correctly", e);
        } catch (final IOException e) {
            throw new ArchiverException("Could not load the public/private key from the PEM file", e);
        }
    }

    /**
     * Attempt to convert a RSA private key to a public/private key pair.
     *
     * @param pemObject Object loaded from PEM file.
     * @return The public/private key pair.
     * @throws NoSuchAlgorithmException If the RSA algorithm is not supported.
     * @throws NoSuchProviderException  If the Bouncy Castle provider is not registered.
     * @throws InvalidKeySpecException  If the key specification is not supported.
     */
    private KeyPair convertRSAPrivateCrtKey(final Object pemObject)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        if (pemObject instanceof RSAPrivateCrtKey) {
            final RSAPrivateCrtKey privateCrtKey = (RSAPrivateCrtKey) pemObject;
            final BigInteger exponent = privateCrtKey.getPublicExponent();
            final BigInteger modulus = privateCrtKey.getModulus();
            final RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
            final KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            final PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            return new KeyPair(publicKey, privateCrtKey);
        } else {
            throw new ArchiverException("Could not load the public/private key from invalid PEM file");
        }
    }

    /**
     * Create a ZIP file in memory containing the directory tree leveraging the {@link
     * org.codehaus.plexus.archiver.zip.AbstractZipArchiver#addResources(org.codehaus.plexus.archiver.ResourceIterator,
     * org.codehaus.plexus.archiver.zip.ConcurrentJarCreator)} method to store resources in the ZIP file. The ZIP file is
     * then converted to a byte array.
     *
     * @return A byte array containing the ZIP file.
     * @throws java.io.IOException If there was an error reading the contents of the source directory.
     */
    private byte[] createZipFile() throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream out = new ZipArchiveOutputStream(buffer)) {
            final ConcurrentJarCreator creator = new ConcurrentJarCreator(1);
            out.setMethod(ZipArchiveOutputStream.DEFLATED);
            out.setLevel(Deflater.BEST_COMPRESSION);
            ResourceIterator resourceIterator = getResources();
            addResources(resourceIterator, creator);
            creator.writeTo(out);
        } catch (final ExecutionException | InterruptedException e) {
            throw new IOException("Error generating archive");
        }
        return buffer.toByteArray();
    }
}
