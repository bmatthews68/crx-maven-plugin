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

/**
 * Encapsulates the public key, signature and contents for a CRX archive.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.o
 */
public class CRXArchive {

    /**
     * The public key used to verify the signature of the CRX archive.
     */
    private byte[] publicKey;

    /**
     * The signature for the contents of the CRX archive.
     */
    private byte[] signature;

    /**
     * The contents of the CRX archive.
     */
    private byte[] data;

    /**
     * Initialise a CRX archive object.
     *
     * @param key The public key used to verify the signature of the CRX archive.
     * @param sig The signature for the contents of the CRX archive.
     * @param buf The contents of the CRX archive.
     */
    public CRXArchive(final byte[] key, final byte[] sig, final byte[] buf) {
        publicKey = key;
        signature = sig;
        data = buf;
    }

    /**
     * Get the public key used to verify the signature of the CRX archive.
     *
     * @return The public key.
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * Get the signature for the contents of the CRX archive.
     *
     * @return The signature.
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Get the contents of the CRX archive.
     *
     * @return The contents.
     */
    public byte[] getData() {
        return data;
    }
}
