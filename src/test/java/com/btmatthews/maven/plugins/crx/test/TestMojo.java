package com.btmatthews.maven.plugins.crx.test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import com.btmatthews.maven.plugins.crx.CRXMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.mockito.Mockito;

/**
 * Created with IntelliJ IDEA.
 * User: Brian
 * Date: 18/07/12
 * Time: 08:55
 * To change this template use File | Settings | File Templates.
 */
public class TestMojo extends AbstractMojoTestCase {

    private CRXMojo mojo;

    private MavenProject project;

    private MavenProjectHelper projectHelper;

    protected void setUp() throws Exception {
        mojo = new CRXMojo();
        project = mock(MavenProject.class);
        projectHelper = mock(MavenProjectHelper.class);
        setVariableValueToObject(mojo, "outputDirectory", new File("target"));
        setVariableValueToObject(mojo, "project", project);
        setVariableValueToObject(mojo, "projectHelper", projectHelper);
    }

    public void testMojo() throws Exception {
        setVariableValueToObject(mojo, "finalName", "HelloWorld");
        setVariableValueToObject(mojo, "pemFile", new File("target/test-classes/crxtest.pem"));
        setVariableValueToObject(mojo, "crxSourceDirectory", new File("target/test-classes/HelloWorld"));
        mojo.execute();
        verify(project).setFile(any(File.class));
    }

    public void testMojoWithClassifier() throws Exception {
        setVariableValueToObject(mojo, "finalName", "HelloWorld");
        setVariableValueToObject(mojo, "pemFile", new File("target/test-classes/crxtest.pem"));
        setVariableValueToObject(mojo, "classifier", "debug");
        setVariableValueToObject(mojo, "crxSourceDirectory", new File("target/test-classes/HelloWorld"));
        mojo.execute();
        verify(projectHelper).attachArtifact(project, eq("crx"), eq("debug"), any(File.class));
    }
}
