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
 * Created with IntelliJ IDEA.
 * User: Brian
 * Date: 04/08/12
 * Time: 18:16
 * To change this template use File | Settings | File Templates.
 */
public class TestPasswordFinder {

    @Mock
    private SecDispatcher securityDispatcher;

    private PasswordFinder passwordFinder;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        passwordFinder = new CRXPasswordFinder("SparkleAndFade");
    }

    @Test
    public void testWithoutSecurityDispatcher() throws Exception {
        assertArrayEquals("SparkleAndFade".toCharArray(), passwordFinder.getPassword());
    }

    @Test
    public void testWithSecurityDispatcher() throws Exception {
        setVariableValueInObject(passwordFinder, "securityDispatcher", securityDispatcher);
        when(securityDispatcher.decrypt("SparkleAndFade")).thenReturn("SoMuchForTheAfterGlow");
        assertArrayEquals("SoMuchForTheAfterGlow".toCharArray(), passwordFinder.getPassword());
    }

    @Test
    public void testWithSecurityDispatcherThrowingException() throws Exception {
        setVariableValueInObject(passwordFinder, "securityDispatcher", securityDispatcher);
        when(securityDispatcher.decrypt("SparkleAndFade")).thenThrow(SecDispatcherException.class);
        assertArrayEquals("SparkleAndFade".toCharArray(), passwordFinder.getPassword());
    }
}

