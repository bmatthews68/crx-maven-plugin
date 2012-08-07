package com.btmatthews.maven.plugins.crx;

import static org.codehaus.plexus.util.ReflectionUtils.setVariableValueInObject;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.byteThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.PublicKey;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

/**
 * Created with IntelliJ IDEA.
 * User: Brian
 * Date: 06/08/12
 * Time: 22:08
 * To change this template use File | Settings | File Templates.
 */
public class TestVerifyMojo {

    private static final String SIGNATURE_HELPER_FIELD = "signatureHelper";

    private static final String ARCHIVE_HELPER_FIELD = "archiveHelper";

    private static final String OUTPUT_DIRECTORY_FIELD = "outputDirectory";

    private static final String CRX_PATH_FIELD = "crxPath";

    private static final String FINAL_NAME_FIELD = "finalName";

    private static final String CLASSIFIER_FIELD = "classifier";

    private Mojo mojo;

    @Mock
    private ArchiveHelper archiveHelper;

    @Mock
    private SignatureHelper signatureHelper;

    @Rule
    public TemporaryFolder outputDirectory = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mojo = new CRXVerifyMojo();
        setVariableValueInObject(mojo, OUTPUT_DIRECTORY_FIELD, outputDirectory.getRoot());
        setVariableValueInObject(mojo, SIGNATURE_HELPER_FIELD, signatureHelper);
        setVariableValueInObject(mojo, ARCHIVE_HELPER_FIELD, archiveHelper);
        setVariableValueInObject(mojo, FINAL_NAME_FIELD, "HelloWorld");
    }

    @Test
    public void testValidSignature() throws Exception {
        final CRXArchive archive = new CRXArchive(null, null, null);
        when(archiveHelper.readArchive(any(File.class))).thenReturn(archive);
        when(signatureHelper.check(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(true);
        mojo.execute();
    }

    @Test(expected = MojoExecutionException.class)
    public void testInvalidSignature() throws Exception {
        final CRXArchive archive = new CRXArchive(null, null, null);
        when(archiveHelper.readArchive(any(File.class))).thenReturn(archive);
        when(signatureHelper.check(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(false);
        mojo.execute();
    }

    @Test(expected = MojoExecutionException.class)
    public void testInvalidArchive() throws Exception {
        when(archiveHelper.readArchive(any(File.class))).thenThrow(IOException.class);
        mojo.execute();
    }

    @Test(expected = MojoExecutionException.class)
    public void testCannotFindArchive() throws Exception {
        when(archiveHelper.readArchive(any(File.class))).thenThrow(FileNotFoundException.class);
        mojo.execute();
    }
}
