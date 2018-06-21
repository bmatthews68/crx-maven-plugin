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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.zip.Deflater;
import org.apache.commons.lang3.SystemUtils;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.*;
import org.bouncycastle.openssl.bc.BcPEMDecryptorProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
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

    private String alias;
    private String storeType;
    private File keyStore;
    private String keyStorePass;
    private String keyPassword;

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public void setKeyStore(File keyStore) {
        this.keyStore = keyStore;
    }

    public void setKeyStorePass(String keyStorePassword) {
        this.keyStorePass = keyStorePassword;
    }

    public void setKeyPassword(String keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Used to inject the location of the .pem file containing the
     * public/private key pair.
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
     * Used to inject the signature helper that is used to to sign the ZIP
     * archive.
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
     * Overriding the implementation in
     * {@link org.codehaus.plexus.archiver.zip.AbstractZipArchiver} to set the
     * packaging type to crx.
     *
     * @return Always returns {@code crx}.
     */
    @Override
    protected String getArchiveType() {
        return "crx";
    }

    /**
     * Generate an in-memory ZIP file containing the resources for the Google
     * Chrome Extension, then sign the ZIP and write out a CRX file containing
     * the header, signature, public key and ZIP data.
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
     * Read the public/private key pair from a PEM, JKS or other keystore file.
     *
     * @return The public/private key pair.
     */
    private KeyPair getKeyPair() {

        if (pemFile.exists()) {
            try {
                final Reader pemFileReader = new FileReader(pemFile);
                try {
                    final PEMParser pemParser = new PEMParser(pemFileReader);
                    try {
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
                    } finally {
                        pemParser.close();
                    }
                } finally {
                    pemFileReader.close();
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

        if (alias != null) {
            try {
                KeyStore ks = KeyStore.getInstance(storeType == null ? "JKS" : storeType);
                if (keyStore != null && keyStore.exists()) {
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(keyStore);
                        ks.load(fis, keyStorePass == null ? null : keyStorePass.toCharArray());
                    } catch (IOException e) {
                        getLogger().warn("error trapped opening keystore, check password " + e.getMessage());
                    } finally {
                        try {
                            fis.close();
                        } catch (IOException ex) {
                        }
                    }
                } else {
                    if (SystemUtils.IS_OS_WINDOWS || SystemUtils.IS_OS_MAC_OSX) {
                        //null for input stream is OK for windows cert store and for macos keychain
                        ks.load(null, keyStorePass == null ? null : keyStorePass.toCharArray());
                    }
                }
                Key key = ks.getKey(alias, keyPassword == null ? null : keyPassword.toCharArray());
                java.security.cert.Certificate cert = ks.getCertificate(alias);
                PublicKey pub = cert.getPublicKey();
                return new KeyPair(pub, (PrivateKey) key);
            } catch (Exception ex) {
                getLogger().warn("Unable to load keystore", ex);
            }
        }
        throw new ArchiverException("Could not load the public/private key from the PEM, or any JKS stores");
    }

    /**
     * Attempt to convert a RSA private key to a public/private key pair.
     *
     * @param pemObject Object loaded from PEM file.
     * @return The public/private key pair.
     * @throws NoSuchAlgorithmException If the RSA algorithm is not supported.
     * @throws NoSuchProviderException If the Bouncy Castle provider is not
     * registered.
     * @throws InvalidKeySpecException If the key specification is not
     * supported.
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
     * org.codehaus.plexus.archiver.zip.ZipOutputStream)} method to store
     * resources in the ZIP file. The ZIP file is then converted to a byte
     * array.
     *
     * @return A byte array containing the ZIP file.
     * @throws java.io.IOException If there was an error reading the contents of
     * the source directory.
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
