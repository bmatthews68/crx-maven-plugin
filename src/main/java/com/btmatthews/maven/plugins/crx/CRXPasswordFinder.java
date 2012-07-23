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
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 * A {@link org.bouncycastle.openssl.PasswordFinder} object that is used by a
 * {@link org.bouncycastle.openssl.PEMReader} to decrypt a PEM file containing a public/private key pair.
 *
 * @author <a href="mailto:brian@brianmatthews.me">Brian Matthews</a>
 * @since 1.0.0
 */
@Component(role = CRXPasswordFinder.class)
public class CRXPasswordFinder implements PasswordFinder {

    @Requirement(hint = "maven")
    private SecDispatcher securityDispatcher;
    /**
     * The plain text password.
     */
    private String password;

    /**
     * Construct the {@link org.bouncycastle.openssl.PasswordFinder} object initialising it with a plain text
     * password string.
     *
     * @param passwordString The plain text password string.
     */
    public CRXPasswordFinder(final String passwordString) {
        password = passwordString;
    }


    /**
     * Get the plain text password decrypting it if necessary.
     *
     * @return The plain text password.
     * @see org.bouncycastle.openssl.PasswordFinder#getPassword()
     */
    @Override
    public final char[] getPassword() {
        if (securityDispatcher == null) {
            return password.toCharArray();
        } else {
            try {
                return securityDispatcher.decrypt(password)
                        .toCharArray();
            } catch (final SecDispatcherException e) {
                return password.toCharArray();
            }
        }
    }
}
