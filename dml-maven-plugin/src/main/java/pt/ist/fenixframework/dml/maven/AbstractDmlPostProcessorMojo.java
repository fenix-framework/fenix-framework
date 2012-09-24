package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.core.FullPostProcessDomainClasses;

public abstract class AbstractDmlPostProcessorMojo extends AbstractMojo {

    protected abstract MavenProject getMavenProject();
    protected abstract File getDmlSourceDirectory();
    protected abstract File getClassesDirectory();
    protected abstract String getCodeGeneratorClassName();
    protected abstract boolean verbose();

    @Override
    public void execute() throws MojoExecutionException {
        if (getMavenProject().getArtifact().getType().equals("pom")) {
	    getLog().info("Cannot post process domain for pom projects");
	    return;
	}

	try {
	    URLClassLoader loader = DmlMojoUtils.augmentClassLoader(getLog(), getMavenProject());
            FullPostProcessDomainClasses.apply(getMavenProject().getArtifactId(),
                                               this.getClassesDirectory(), loader);
	} catch (Exception e) {
	    getLog().error(e);
	}
    }
}
