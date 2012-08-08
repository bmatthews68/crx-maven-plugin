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

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Implementations sign byte arrays using public/private key pairs.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public interface SignatureHelper {

    /**
     * Generate the signature for a byte array using the private key.
     *
     * @param data The byte array.
     * @param key  private key.
     * @return The signature as a byte array.
     * @throws GeneralSecurityException If there was a error generating the signature.
     */
    byte[] sign(byte[] data, PrivateKey key) throws GeneralSecurityException;

    /**
     * Check that the signature is valid using the public key.
     *
     * @param data      The data for which the signature was generated.
     * @param key       The public key.
     * @param signature The signature.
     * @return {@code true} if the signature was valid. Otherwise, {@code false}.
     * @throws GeneralSecurityException If there was an error validating the signature.
     */
    boolean check(byte[] data, PublicKey key, byte[] signature) throws
            GeneralSecurityException;
}
