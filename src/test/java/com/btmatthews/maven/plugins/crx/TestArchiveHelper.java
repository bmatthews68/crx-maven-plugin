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
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test {@link CRXArchiveHelper}.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public class TestArchiveHelper {

    private ArchiveHelper archiveHelper;

    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();

    @Before
    public void setUp() {
        archiveHelper = new CRXArchiveHelper();
    }

    @Test
    public void testReadWrite() throws IOException {
        final File crxFile = new File(outputDirectory.getRoot(), "test.crx");
        final CRXArchive crxOutArchive = new DummyArchive();
        archiveHelper.writeArchive(crxFile, crxOutArchive);
        final CRXArchive crxInArchive = archiveHelper.readArchive(crxFile);
        assertNotNull(crxInArchive);
        assertArrayEquals(DummyArchive.PUBLIC_KEY, crxInArchive.getPublicKey());
        assertArrayEquals(DummyArchive.SIGNATURE, crxInArchive.getSignature());
        assertArrayEquals(DummyArchive.DATA, crxInArchive.getData());
    }
}
