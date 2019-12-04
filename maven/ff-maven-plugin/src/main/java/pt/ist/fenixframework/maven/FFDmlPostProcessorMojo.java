package pt.ist.fenixframework.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.dml.maven.DmlPostProcessorMojo;

/**
 * This goal is an adapter for dml-maven-plugin:post-compile
 * 
 * @goal ff-post-compile
 * @phase process-classes
 * @requiresDependencyResolution runtime
 * @threadSafe
 */
@Deprecated
public class FFDmlPostProcessorMojo extends DmlPostProcessorMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    protected MavenProject mavenProject;

    /**
     * File Source Directory
     * 
     * @parameter default-value="${basedir}/src/main/dml"
     * @readonly
     * @required
     */
    protected File dmlSourceDirectory;

    /**
     * Classes Directory
     * 
     * @parameter default-value="${project.build.outputDirectory}"
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
