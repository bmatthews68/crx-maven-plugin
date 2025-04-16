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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of {@link ArchiveHelper} that outputs the CRX archive.
 *
 * @author <a href="mailto:bmatthews68@gmail.com">Brian Matthews</a>
 * @since 1.1.0
 */
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
     * @param crxFile    The target CRX file.
     * @param crxArchive The CRX archive.
     * @throws IOException If there was an error writing the CRX file.
     */
    public void writeArchive(final File crxFile, final CRXArchive crxArchive) throws IOException {
        if (crxFile.exists()) {
            crxFile.delete();
        } else {
            crxFile.getParentFile().mkdirs();
        }
        try (FileOutputStream crx = new FileOutputStream(crxFile)) {
            crx.write(CRX_MAGIC);
            crx.write(CRX_VERSION);
            writeLength(crx, crxArchive.getPublicKey().length);
            writeLength(crx, crxArchive.getSignature().length);
            crx.write(crxArchive.getPublicKey());
            crx.write(crxArchive.getSignature());
            crx.write(crxArchive.getData());
        }
    }

    /**
     * Read the CRX archive from a file loading the header, public key, signature and data.
     *
     * @param crxFile The source CRX file.
     * @return The CRX archive.
     * @throws IOException If there was an error reading the CRX file.
     */
    public CRXArchive readArchive(final File crxFile) throws IOException {
        final byte[] buffer = new byte[4];
        try (InputStream crxIn = new FileInputStream(crxFile)) {
            crxIn.read(buffer);
            crxIn.read(buffer);
            final int publicKeyLength = readLength(crxIn);
            final int signatureLength = readLength(crxIn);
            final byte[] publicKey = new byte[publicKeyLength];
            crxIn.read(publicKey);
            final byte[] signature = new byte[signatureLength];
            crxIn.read(signature);
            final int dataLength = (int)(crxFile.length() - 16 - publicKeyLength - signatureLength);
            final byte[] data = new byte[dataLength];
            crxIn.read(data);
            return new CRXArchive(publicKey, signature, data);
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

    /**
     * Read a 32-bit integer from the output stream in little endian format.
     *
     * @param in The input stream.
     * @return The 32-bit integer.
     * @throws IOException If there was a problem reading from the input stream.
     */
    private int readLength(final InputStream in) throws IOException {
        final byte[] buffer = new byte[4];
        in.read(buffer, 0, 4);
        return (buffer[3] << SHIFT_24) | ((buffer[2] & BYTE_MASK) << SHIFT_16) | ((buffer[1] & BYTE_MASK) << SHIFT_8)
                | (buffer[0] & BYTE_MASK);
    }
}
