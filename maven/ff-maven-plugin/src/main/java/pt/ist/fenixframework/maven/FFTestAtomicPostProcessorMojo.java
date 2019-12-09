package pt.ist.fenixframework.atomic.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.atomic.maven.TestAtomicPostProcessorMojo;

/**
 * This goal is an adapter for atomic-maven-plugin:test-process-atomic-annotations
 * 
 * @goal ff-test-process-atomic-annotations
 * @phase process-test-classes
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class FFTestAtomicPostProcessorMojo extends TestAtomicPostProcessorMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    protected MavenProject mavenProject;

    /**
     * Setting this to 'true' skips this post-processing
     * 
     * @parameter property="maven.test.skip"
     */
    protected boolean skip;

    /**
     * Classes Directory
     * 
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @readonly
     * @required
     */
    protected File classesDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        super.mavenProject = this.mavenProject;
        super.skip = this.skip;
        super.classesDirectory = this.classesDirectory;
        super.execute();
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
