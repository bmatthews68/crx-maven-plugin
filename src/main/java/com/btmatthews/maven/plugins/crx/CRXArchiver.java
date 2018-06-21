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

import org.codehaus.plexus.archiver.Archiver;

/**
 * This archiver packages and signs a Google Chrome Extension.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public interface CRXArchiver extends Archiver {

    /**
     * Used to inject the location of the .pem file containing the public/private key pair.
     *
     * @param file The location of the .pem file.
     */
    void setPemFile(File file);

    /**
     * Used to inject the password that was used to secure the .pem file.
     *
     * @param password The password.
     */
    void setPemPassword(String password);

    /**
     * Used to inject the signature helper that is used to to sign the ZIP archive.
     *
     * @param helper The helper.
     */
    void setSignatureHelper(SignatureHelper helper);

    /**
     * Used to inject the archive helper that is used to output the CRX archive.
     *
     * @param helper The helper.
     */
    void setArchiveHelper(ArchiveHelper helper);

    void setAlias(String alias);

    void setStoreType(String storeType);

    void setKeyStore(File keyStore);

    void setKeyStorePass(String keyStorePassword);

    void setKeyPassword(String keyPassword);
}
