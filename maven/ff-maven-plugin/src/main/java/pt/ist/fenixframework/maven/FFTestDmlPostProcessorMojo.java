package pt.ist.fenixframework.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.dml.maven.TestDmlPostProcessorMojo;

/**
 * This goal is an adapter for dml-maven-plugin:test-post-compile
 * 
 * @goal ff-test-post-compile
 * @phase process-test-classes
 * @requiresDependencyResolution test
 * @threadSafe
 */
@Deprecated
public class FFTestDmlPostProcessorMojo extends TestDmlPostProcessorMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    protected MavenProject mavenProject;

    /**
     * Setting this to 'true' skips post-processing of dml compiled test classes.
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
     * Classes Directory
     * 
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @readonly
     * @required
     */
    protected File classesDirectory;

    /**
     * Code Generator Class Name
     * 
     * @parameter property="generate-domain.codeGeneratorClassName"
     *            default-value="pt.ist.fenixframework.dml.DefaultCodeGenerator"
     */
    protected String codeGeneratorClassName;

    /**
     * Verbose Mode Flag
     * 
     * @parameter property="generate-domain.verbose"
     *            default-value="false"
     */
    protected boolean verbose;

    @Override
    public void execute() throws MojoExecutionException {
        super.mavenProject = this.mavenProject;
        super.skip = this.skip;
        super.dmlSourceDirectory = this.dmlSourceDirectory;
        super.classesDirectory = this.classesDirectory;
        super.codeGeneratorClassName = this.codeGeneratorClassName;
        super.verbose = this.verbose;
        super.execute();
    }

    @Override
    protected File getDmlSourceDirectory() {
        return dmlSourceDirectory;
    }

    @Override
    protected File getClassesDirectory() {
        return classesDirectory;
    }

    @Override
    protected String getCodeGeneratorClassName() {
        return codeGeneratorClassName;
    }

    @Override
    protected boolean verbose() {
        return verbose;
    }

    @Override
    protected MavenProject getMavenProject() {
        return mavenProject;
    }

}
