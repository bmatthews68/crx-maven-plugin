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

import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Unit tests to verify the behaviour of the {@link CRXArchive} class.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public class TestArchive {

    /**
     * Very simple test to verify the constructor of the {@link CRXArchive} class. This test is trivial and the main
     * motivation for creating it is to ensure 100% coverage.
     */
    @Test
    public void testArchive() {
        final byte[] publicKey = new byte[1];
        final byte[] signature = new byte[1];
        final byte[] data = new byte[1];
        final CRXArchive archive = new CRXArchive(publicKey, signature, data);
        assertSame(publicKey, archive.getPublicKey());
        assertSame(signature, archive.getSignature());
        assertSame(data, archive.getData());
    }
}
