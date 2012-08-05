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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Implementation of {@link ArchiveHelper} that outputs the CRX archive.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
@Component(role = ArchiveHelper.class, hint = "crx")
public class CRXArchiveHelper implements ArchiveHelper {
    /**
     * Used to as a mask when extracting 8 least significant bits of a integer.
     */
    private static final int BYTE_MASK = 0xFF;

    /**
     * The amounts in order to move bits 15 thru 8 into the 8 least significant bits.
     */
    private static final int SHIFT_8 = 8;

    /**
     * The amounts in order to move bits 23 thru 16 into the 8 least significant bits.
     */
    private static final int SHIFT_16 = 16;

    /**
     * The amounts in order to move bits 31 thru 24 into the 8 least significant bits.
     */
    private static final int SHIFT_24 = 24;

    /**
     * The magic number for CRX files.
     */
    private static final byte[] CRX_MAGIC = { 0x43, 0x72, 0x32, 0x34 };

    /**
     * The CRX header version number in little endian format.
     */
    private static final byte[] CRX_VERSION = { 0x02, 0x00, 0x00, 0x00 };

    /**
     * Generate the CRX file writing the header, public key, signature and data.
     *
     * @param crxFile   The target CRX file.
     * @param zipData   The zipped CRX contents.
     * @param signature The signature of the zipped CRX contents.
     * @param publicKey The public to be used when verifying signature.
     * @throws IOException If there was an error writing the CRX file.
     */
    public void writeArchive(final File crxFile, final byte[] zipData, final byte[] signature,
                             final byte[] publicKey) throws IOException {
        if (crxFile.exists()) {
            crxFile.delete();
        } else {
            crxFile.getParentFile().mkdirs();
        }
        final FileOutputStream crx = new FileOutputStream(crxFile);
        try {
            crx.write(CRX_MAGIC);
            crx.write(CRX_VERSION);
            writeLength(crx, publicKey.length);
            writeLength(crx, signature.length);
            crx.write(publicKey);
            crx.write(signature);
            crx.write(zipData);
        } finally {
            crx.close();
        }
    }

    /**
     * Write a 32-bit integer to the output stream in little endian format.
     *
     * @param out The output stream.
     * @param val The 32-bit integer.
     * @throws IOException If there was a problem writing to the output stream.
     */
    private void writeLength(final OutputStream out, final int val) throws IOException {
        out.write(val & BYTE_MASK);
        out.write((val >> SHIFT_8) & BYTE_MASK);
        out.write((val >> SHIFT_16) & BYTE_MASK);
        out.write((val >> SHIFT_24) & BYTE_MASK);
    }
}
