package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Generate base main classes from the main DML files
 * 
 * @goal generate-domain
 * @phase generate-sources
 * @configurator include-project-dependencies
 * @requiresDependencyResolution compile+runtime
 * @threadSafe
 */
public class DmlCodeGeneratorMojo extends AbstractDmlCodeGeneratorMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    protected MavenProject mavenProject;

    /**
     * Skip class generation
     *
     * @parameter default-value="false"
     */
    protected boolean skip;

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
     * @parameter property="generate-domain.codeGeneratorClassName"
     *            default-value="pt.ist.fenixframework.dml.DefaultCodeGenerator"
     */
    protected String codeGeneratorClassName;

    /**
     * Package name
     * 
     * @parameter property="generate-domain.packageName"
     */
    protected String packageName = "";

    /**
     * Generate Finals Flag
     * 
     * @parameter property="generate-domain.generateFinals" default-value="false"
     */
    protected boolean generateFinals;

    /**
     * Verbose Mode Flag
     * 
     * @parameter property="verbose" default-value="false"
     */
    protected boolean verbose;

    /**
     * Generate Project Properties Flag
     * 
     * @parameter property="generate-domain.generateProjectProperties" default-value="false"
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
        if (!skip) {
            System.out.println("Generating DML Sources...");
            super.execute();
            getMavenProject().addCompileSourceRoot(getGeneratedSourcesDirectory().getAbsolutePath());
        } else {
            System.out.println("Skip flag is set. Ignoring");
        }
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

    @Override
    protected List<String> getClasspathElements() {
        try {
            return getMavenProject().getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            getLog().error(e);
        }
        return null;
    }
}
