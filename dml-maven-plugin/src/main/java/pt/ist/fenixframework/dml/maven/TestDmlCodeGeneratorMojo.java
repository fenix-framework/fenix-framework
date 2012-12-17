/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @goal test-generate-domain
 * @phase generate-test-sources
 * @configurator include-project-dependencies
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class TestDmlCodeGeneratorMojo extends AbstractDmlCodeGeneratorMojo {

    /**
     * Maven Project
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * Set this to 'true' to bypass compilation of dml test sources.
     *
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * File Source Directory
     *
     * @parameter default-value="${basedir}/src/test/dml"
     * @readonly
     * @required
     */
    private File dmlSourceDirectory;

    /**
     * File Destination Directory
     *
     * @parameter default-value="${basedir}/src/test/java"
     * @readonly
     * @required
     */
    private File sourcesDirectory;

    /**
     * Base File Destination Directory
     *
     * @parameter default-value="${project.build.directory}/generated-test-sources/dml-maven-plugin"
     * @readonly
     * @required
     */
    private File generatedSourcesDirectory;

    /**
     * Code Generator Class Name
     * @parameter expression="${generate-domain.codeGeneratorClassName}"
     *            default-value="pt.ist.fenixframework.dml.DefaultCodeGenerator"
     */
    private String codeGeneratorClassName;

    /**
     * Package name
     * @parameter expression="${test-generate-domain.packageName}"
     */
    private String packageName = "";

    /**
     * Generate Finals Flag
     * @parameter expression="${test-generate-domain.generateFinals}"
     *            default-value="false"
     */
    private boolean generateFinals;

    /**
     * Verbose Mode Flag
     * @parameter expression="${verbose}"
     *            default-value="false"
     */
    private boolean verbose;

    /**
     * Generate Project Properties Flag
     * @parameter expression="${test-generate-domain.generateProjectProperties}"
     *            default-value="false"
     */
    private boolean generateProjectProperties;

    /**
     * Generic Code Generator Class Parameters
     * @parameter
     */
    private Map<String,String> params;

    @Override
    public void execute() throws MojoExecutionException {
        if(skip) {
            getLog().info("Not compiling test sources");
        } else {
            super.execute();
            getMavenProject().addTestCompileSourceRoot(getGeneratedSourcesDirectory().getAbsolutePath());
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
        return mavenProject.getBuild().getTestOutputDirectory();
    }

    @Override
    protected Map<String,String> getParams() {
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
