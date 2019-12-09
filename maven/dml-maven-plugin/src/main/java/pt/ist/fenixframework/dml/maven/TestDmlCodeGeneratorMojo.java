/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Generate base main classes from the main DML files
 * 
 * @goal test-generate-domain
 * @phase generate-test-sources
 * @configurator include-project-dependencies
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class TestDmlCodeGeneratorMojo extends AbstractDmlCodeGeneratorMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    protected MavenProject mavenProject;

    /**
     * Set this to 'true' to bypass compilation of dml test sources.
     * 
     * @parameter property="maven.test.skip"
     */
    protected boolean skip;

    /**
     * File Source Directory
     * 
     * @parameter default-value="${basedir}/src/test/dml"
     * @readonly
     * @required
     */
    protected File dmlSourceDirectory;

    /**
     * File Destination Directory
     * 
     * @parameter default-value="${basedir}/src/test/java"
     * @readonly
     * @required
     */
    protected File sourcesDirectory;

    /**
     * Base File Destination Directory
     * 
     * @parameter default-value="${project.build.directory}/generated-test-sources/dml-maven-plugin"
     * @readonly
     * @required
     */
    protected File generatedSourcesDirectory;

    /**
     * Code Generator Class Name
     * 
     * @parameter property="generate-domain.codeGeneratorClassName"
     *            default-value="pt.ist.fenixframework.dml.DefaultCodeGenerator"
     */
    protected String codeGeneratorClassName;

    /**
     * Package name
     * 
     * @parameter property="test-generate-domain.packageName"
     */
    protected String packageName = "";

    /**
     * Generate Finals Flag
     * 
     * @parameter property="test-generate-domain.generateFinals"
     *            default-value="false"
     */
    protected boolean generateFinals;

    /**
     * Verbose Mode Flag
     * 
     * @parameter property="verbose"
     *            default-value="false"
     */
    protected boolean verbose;

    /**
     * Generate Project Properties Flag
     * 
     * @parameter property="test-generate-domain.generateProjectProperties"
     *            default-value="false"
     */
    protected boolean generateProjectProperties;

    /**
     * Generic Code Generator Class Parameters
     * 
     * @parameter
     */
    protected Map<String, String> params;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Not compiling test sources");
        } else {
            super.execute();
            getMavenProject().addTestCompileSourceRoot(getGeneratedSourcesDirectory().getAbsolutePath());
        }
    }

    @Override
    protected File getMainDmlDirectory() throws MalformedURLException {
        return new File(mavenProject.getBasedir() + "/src/main/dml");
    }

    @Override
    protected File getDmlSourceDirectory() {
        return dmlSourceDirectory;
    }

    @Override
    protected String getCodeGeneratorClassName() {
        return codeGeneratorClassName;
    }

    @Override
    protected File getGeneratedSourcesDirectory() {
        return generatedSourcesDirectory;
    }

    @Override
    protected File getSourcesDirectory() {
        return sourcesDirectory;
    }

    @Override
    protected String getPackageName() {
        return packageName;
    }

    @Override
    protected boolean verbose() {
        return verbose;
    }

    @Override
    protected boolean generateFinals() {
        return generateFinals;
    }

    @Override
    protected boolean generateProjectProperties() {
        return generateProjectProperties;
    }

    @Override
    protected MavenProject getMavenProject() {
        return mavenProject;
    }

    @Override
    protected String getOutputDirectoryPath() {
        return mavenProject.getBuild().getTestOutputDirectory();
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    @Override
    protected List<String> getClasspathElements() {
        try {
            return getMavenProject().getTestClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            getLog().error(e);
        }
        return null;
    }
}
