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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests to verify the behaviour of the {@link CRXArchive} class.
 *
 * @author <a href="mailto:bmatthews68@gmail.com">Brian Matthews</a>
 * @since 1.1.0
 */
class TestArchive {

    /**
     * Very simple test to verify the constructor of the {@link CRXArchive} class. This test is trivial and the main
     * motivation for creating it is to ensure 100% coverage.
     */
    @Test
    void testArchive() {
        final CRXArchive archive = new CRXArchive(DummyArchive.PUBLIC_KEY, DummyArchive.SIGNATURE, DummyArchive.DATA);
        assertSame(DummyArchive.PUBLIC_KEY, archive.getPublicKey());
        assertSame(DummyArchive.SIGNATURE, archive.getSignature());
        assertSame(DummyArchive.DATA, archive.getData());
    }
}
