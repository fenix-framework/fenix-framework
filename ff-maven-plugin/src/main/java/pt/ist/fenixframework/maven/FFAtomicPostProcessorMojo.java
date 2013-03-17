package pt.ist.fenixframework.atomic.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.atomic.maven.AtomicPostProcessorMojo;

/**
 * This goal is an adapter for atomic-maven-plugin:process-atomic-annotations
 * 
 * @goal ff-process-atomic-annotations
 * @phase process-classes
 * @requiresDependencyResolution runtime
 * @threadSafe
 */
public class FFAtomicPostProcessorMojo extends AtomicPostProcessorMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    protected MavenProject mavenProject;

    /**
     * Classes Directory
     * 
     * @parameter default-value="${project.build.outputDirectory}"
     * @readonly
     * @required
     */
    protected File classesDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        super.mavenProject = this.mavenProject;
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
