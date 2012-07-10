package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

// import jvstm.ProcessAtomicAnnotations;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.core.Project;
import pt.ist.fenixframework.core.DmlFile;

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
	    List<URL> dmlFiles = new ArrayList<URL>();
	    for (DmlFile dmlFile : Project.fromName(getMavenProject().getArtifactId()).getFullDmlSortedList()) {
		dmlFiles.add(dmlFile.getUrl());
	    }

	    if (dmlFiles.isEmpty()) {
		getLog().info("No dml files found to post process domain");
		return;
	    }

	    Class[] argsConstructor = new Class[] { List.class, getCodeGeneratorClassName().getClass(),
                                                    ClassLoader.class };
	    Object[] args = new Object[] { dmlFiles, getCodeGeneratorClassName(), loader };
	    Class postProcessDomainClassesClass = loader.loadClass("pt.ist.fenixframework.core.PostProcessDomainClasses");
	    // Class transactionClass = loader.loadClass("pt.ist.fenixframework.pstm.Transaction");

	    Constructor processDomainClassesConstructor = postProcessDomainClassesClass.getConstructor(argsConstructor);
	    Object postProcessor = processDomainClassesConstructor.newInstance(args);

	    Object[] nullArgs = new Object[] {};
	    Method m = postProcessDomainClassesClass.getMethod("start", new Class[] {});
	    m.invoke(postProcessor, nullArgs);

	    // ProcessAtomicAnnotations atomicAnnotationsProcessor = new ProcessAtomicAnnotations(transactionClass,
	    //         new String[] { "." });
	    // atomicAnnotationsProcessor.start();

            // Removed service annotation for now, postponed for later discussion
	    // Class<?> serviceInjector = loader.loadClass("pt.ist.fenixframework.services.ServiceAnnotationInjector");
	    // Method injector = serviceInjector.getMethod("inject", File.class, ClassLoader.class);
	    // injector.invoke(null, this.getClassesDirectory(), loader);
	} catch (Exception e) {
	    getLog().error(e);
	}
    }
}
