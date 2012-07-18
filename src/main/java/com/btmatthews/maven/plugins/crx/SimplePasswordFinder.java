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

import org.bouncycastle.openssl.PasswordFinder;

/**
 * A {@link org.bouncycastle.openssl.PasswordFinder} object that is used by a {@link org.bouncycastle.openssl
 * .PEMReader} to decrypt a PEM file containing a public/private key pair.
 *
 * @author <a href="mailto:brian@brianmatthews.me">Brian Matthews</a>
 * @since 1.0.0
 */
public class SimplePasswordFinder implements PasswordFinder {

    /**
     * The plain text password.
     */
    private char[] password;

    /**
     * Construct the {@link org.bouncycastle.openssl.PasswordFinder} object initialising it with a plain text
     * password string.
     *
     * @param passwordString The plain text password string.
     */
    public SimplePasswordFinder(final String passwordString) {
        password = passwordString.toCharArray();
    }


    /**
     * Get the plain text password.
     *
     * @return The plain text password.
     * @see {@link org.bouncycastle.openssl.PasswordFinder#getPassword()}
     */
    @Override
    public char[] getPassword() {
        return password;
    }
}
