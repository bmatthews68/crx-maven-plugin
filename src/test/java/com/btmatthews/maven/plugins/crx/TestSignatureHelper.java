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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link CRXSignatureHelper}.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public class TestSignatureHelper {
    /**
     * The {@link CRXSignatureHelper} being unit tested.
     */
    private SignatureHelper signatureHelper;

    /**
     * The key factory is used to convert binary data to public and private keys.
     */
    private KeyFactory keyFactory;

    /**
     * Prepare for the unit tests.
     *
     * @throws Exception If there was a problem preparing for the unit tests.
     */
    @Before
    public void setUp() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        signatureHelper = new CRXSignatureHelper();
        keyFactory = KeyFactory.getInstance("RSA", "BC");
    }

    /**
     * Verify the {@link SignatureHelper#sign(byte[], java.security.PrivateKey)} method.
     *
     *                                                                                                               /(
     * @throws Exception If there was an unexpected problem.
     */
    @Test
    public void testSign() throws Exception {
        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(DummyArchive.PRIVATE_KEY);
        final PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        final byte[] signature = signatureHelper.sign(DummyArchive.DATA, privateKey);
        assertArrayEquals(DummyArchive.SIGNATURE, signature);
    }


    /**
     * Verify the {@link SignatureHelper#check(byte[], java.security.PublicKey, byte[])}  method.
     *
     * @throws Exception If there was an unexpected problem.
     */
    @Test
    public void testCheck() throws Exception {
        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DummyArchive.PUBLIC_KEY);
        final PublicKey publicKey = keyFactory.generatePublic(keySpec);
        final boolean result = signatureHelper.check(DummyArchive.DATA, publicKey, DummyArchive.SIGNATURE);
        assertTrue(result);
    }
}
