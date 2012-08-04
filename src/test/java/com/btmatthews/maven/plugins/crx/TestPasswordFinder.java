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

import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.bouncycastle.openssl.PasswordFinder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;

/**
 * Unit tests to verify the behaviour of the @{link CRXPasswordFinder} class.
 *
 * @author <a href="mailto:brian@btmatthews.com">Brian Matthews</a>
 * @since 1.1.0
 */
public class TestPasswordFinder {

    /**
     * A password value used for testing.
     */
    private static final String PASSWORD1 = "SparkleAndFade";

    /**
     * A password value used for testing.
     */
    private static final String PASSWORD2 = "SoMuchForTheAfterglow";

    /**
     * The name of the {@link SecDispatcher} field in the {@link CRXPasswordFinder} class.
     */
    private static final String SECURITY_DISPATCHER_FIELD = "securityDispatcher";

    /**
     * Mock security dispatcher that is used to decrypt encrypted passwords.
     */
    @Mock
    private SecDispatcher securityDispatcher;

    /**
     * The password finder being tested.
     */
    private PasswordFinder passwordFinder;

    /**
     * Prepare for test case execution by initialising the mock objects and test fixtures.
     *
     * @throws Exception If there was an error preparing for test case execution.
     */
    @Before
    public void setUp() throws Exception {
        initMocks(this);
        passwordFinder = new CRXPasswordFinder(PASSWORD1);
    }

    /**
     * Verify that the password returns the plain text password when there is no security dispatcher.
     *
     * @throws Exception If there was an unexpected error during test case execution.
     */
    @Test
    public void testWithoutSecurityDispatcher() throws Exception {
        assertArrayEquals(PASSWORD1.toCharArray(), passwordFinder.getPassword());
    }

    /**
     * Verify that the password returns the plain text password when there is a security dispatcher to decrypt the
     * password.
     *
     * @throws Exception If there was an unexpected error during test case execution.
     */
    @Test
    public void testWithSecurityDispatcher() throws Exception {
        setVariableValueInObject(passwordFinder, SECURITY_DISPATCHER_FIELD, securityDispatcher);
        when(securityDispatcher.decrypt(PASSWORD1)).thenReturn(PASSWORD2);
        assertArrayEquals(PASSWORD2.toCharArray(), passwordFinder.getPassword());
    }

    /**
     * Verify that the password returns the plain text password when the security dispatcher fails to decrypt the
     * password.
     *
     * @throws Exception If there was an unexpected error during test case execution.
     */
    @Test
    public void testWithSecurityDispatcherThrowingException() throws Exception {
        setVariableValueInObject(passwordFinder, SECURITY_DISPATCHER_FIELD, securityDispatcher);
        when(securityDispatcher.decrypt(PASSWORD1)).thenThrow(SecDispatcherException.class);
        assertArrayEquals(PASSWORD1.toCharArray(), passwordFinder.getPassword());
    }
}

