package pt.ist.fenixframework.maven;

import java.io.File;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.dml.maven.DmlCodeGeneratorMojo;

/**
 * This goal is an adapter for dml-maven-plugin:generate-domain
 * 
 * @goal ff-generate-domain
 * @phase generate-sources
 * @configurator include-project-dependencies
 * @requiresDependencyResolution compile+runtime
 * @threadSafe
 */
public class FFDmlCodeGeneratorMojo extends DmlCodeGeneratorMojo {

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
     * File Destination Directory
     * 
     * @parameter default-value="${basedir}/src/main/java"
     * @readonly
     * @required
     */
    protected File sourcesDirectory;

    /**
     * Base File Destination Directory
     * 
     * @parameter default-value="${project.build.directory}/generated-sources/dml-maven-plugin"
     * @readonly
     * @required
     */
    protected File generatedSourcesDirectory;

    /**
     * Code Generator Class Name
     * 
     * @parameter expression="${generate-domain.codeGeneratorClassName}"
     *            default-value="pt.ist.fenixframework.dml.DefaultCodeGenerator"
     */
    protected String codeGeneratorClassName;

    /**
     * Package name
     * 
     * @parameter expression="${generate-domain.packageName}"
     */
    protected final String packageName = "";

    /**
     * Generate Finals Flag
     * 
     * @parameter expression="${generate-domain.generateFinals}" default-value="false"
     */
    protected boolean generateFinals;

    /**
     * Verbose Mode Flag
     * 
     * @parameter expression="${verbose}" default-value="false"
     */
    protected boolean verbose;

    /**
     * Generate Project Properties Flag
     * 
     * @parameter expression="${generate-domain.generateProjectProperties}" default-value="false"
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
        super.mavenProject = this.mavenProject;
        super.dmlSourceDirectory = this.dmlSourceDirectory;
        super.sourcesDirectory = this.sourcesDirectory;
        super.generatedSourcesDirectory = this.generatedSourcesDirectory;
        super.codeGeneratorClassName = this.codeGeneratorClassName;
        super.packageName = this.packageName;
        super.generateFinals = this.generateFinals;
        super.verbose = this.verbose;
        super.generateProjectProperties = this.generateProjectProperties;
        super.params = this.params;
        super.execute();
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
        return mavenProject.getBuild().getOutputDirectory();
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

}
