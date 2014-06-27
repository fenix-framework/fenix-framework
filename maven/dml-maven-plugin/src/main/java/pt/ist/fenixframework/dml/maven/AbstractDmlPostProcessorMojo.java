package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.core.FullPostProcessDomainClasses;

/**
 * @deprecated Dml Post-Processing is no longer required, and as such, usage of this Mojo is no longer needed
 *
 */
@Deprecated
public abstract class AbstractDmlPostProcessorMojo extends AbstractMojo {

    protected abstract MavenProject getMavenProject();

    protected abstract File getDmlSourceDirectory();

    protected abstract File getClassesDirectory();

    protected abstract String getCodeGeneratorClassName();

    protected abstract boolean verbose();

    protected abstract List<String> getClasspathElements();

    @Override
    public void execute() throws MojoExecutionException {
        if (getMavenProject().getArtifact().getType().equals("pom")) {
            getLog().info("Cannot post process domain for pom projects");
            return;
        }

        try {
            URLClassLoader loader = DmlMojoUtils.augmentClassLoader(getLog(), getClasspathElements());
            FullPostProcessDomainClasses.apply(getMavenProject().getArtifactId(), this.getClassesDirectory(), loader);
        } catch (Exception e) {
            getLog().error(e);
            throw new MojoExecutionException("Something went wrong with the Post-Processing", e);
        }
    }
}
