package pt.ist.fenixframework.atomic.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which calls the Atomic annotation processor on the compiled classes.
 * 
 * @goal process-atomic-annotations
 * @phase process-classes
 * @requiresDependencyResolution runtime
 * @threadSafe
 */
public class AtomicPostProcessorMojo extends AbstractAtomicProcessorMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * Classes Directory
     * 
     * @parameter default-value="${project.build.outputDirectory}"
     * @readonly
     * @required
     */
    private File classesDirectory;

    @Override
    public void execute() throws MojoExecutionException {
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
