package pt.ist.fenixframework.atomic.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which processes the atomic annotations present in the compiled test classes.
 * 
 * @goal test-process-atomic-annotations
 * @phase process-test-classes
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class TestAtomicPostProcessorMojo extends AbstractAtomicProcessorMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * Setting this to 'true' skips this post-processing
     * 
     * @parameter expression="${maven.test.skip}"
     */
    private boolean skip;

    /**
     * Classes Directory
     * 
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @readonly
     * @required
     */
    private File classesDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("Not post-processing test sources");
        } else {
            super.execute();
        }
    }

    @Override
    protected File getClassesDirectory() {
        return classesDirectory;
    }

    @Override
    protected MavenProject getMavenProject() {
        return mavenProject;
    }

}
