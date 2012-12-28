package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which injects the constructors into the bytecode of the DML compiled
 * classes
 *
 * @goal test-post-compile
 * @phase process-test-classes
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class TestDmlPostProcessorMojo extends AbstractDmlPostProcessorMojo {

    /**
     * Maven Project
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * Setting this to 'true' skips post-processing of dml compiled test classes.
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
     * Classes Directory
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @readonly
     * @required
     */
    private File classesDirectory;

    /**
     * Code Generator Class Name
     * @parameter expression="${generate-domain.codeGeneratorClassName}"
     *            default-value="pt.ist.fenixframework.dml.DefaultCodeGenerator"
     */
    private String codeGeneratorClassName;

    /**
     * Verbose Mode Flag
     * @parameter expression="${generate-domain.verbose}"
     *            default-value="false"
     */
    private boolean verbose;

    @Override
    public void execute() throws MojoExecutionException {
        if(skip) {
            getLog().info("Not post-processing test sources");
        } else {
            super.execute();
        }
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
