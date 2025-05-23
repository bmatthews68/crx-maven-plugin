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

import java.io.File;
import java.io.IOException;

/**
 * Implementations read and write CRX archives.
 *
 * @author <a href="mailto:bmatthews68@gmail.com">Brian Matthews</a>
 * @since 1.1.0
 */
public interface ArchiveHelper {

    /**
     * Generate the CRX file writing the header, public key, signature and data.
     *
     * @param crxFile    The target CRX file.
     * @param crxArchive The CRX archive.
     * @throws IOException If there was an error writing the CRX file.
     */
    void writeArchive(File crxFile, CRXArchive crxArchive) throws IOException;

    /**
     * Read the CRX archive from a file loading the header, public key, signature and data.
     *
     * @param crxFile The source CRX file.
     * @return The CRX archive.
     * @throws IOException If there was an error reading the CRX file.
     */
    CRXArchive readArchive(File crxFile) throws IOException;
}
