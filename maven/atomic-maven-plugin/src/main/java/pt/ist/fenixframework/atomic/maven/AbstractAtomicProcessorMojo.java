package pt.ist.fenixframework.atomic.maven;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.esw.advice.ProcessAnnotations;
import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.atomic.AtomicContextFactory;

public abstract class AbstractAtomicProcessorMojo extends AbstractMojo {

    protected abstract MavenProject getMavenProject();

    protected abstract File getClassesDirectory();

    @Override
    public void execute() throws MojoExecutionException {
        if (getMavenProject().getArtifact().getType().equals("pom")) {
            getLog().info("Cannot process pom projects");
            return;
        }

        try {
            new ProcessAnnotations(new ProcessAnnotations.ProgramArgs(Atomic.class, AtomicContextFactory.class,
                    getClassesDirectory())).process();

        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Something went wrong with the post processing", e);
        }
    }
}
