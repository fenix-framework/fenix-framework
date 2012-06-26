package dml.maven;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import jvstm.ProcessAtomicAnnotations;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;
import pt.ist.fenixframework.project.DmlFile;

/**
 * Goal which injects the constructors into the bytecode of the DML compiled
 * classes
 * 
 * @goal post-compile
 * 
 * @requiresDependencyResolution runtime
 * 
 * @phase process-classes
 */
public class PostCompileMojo extends AbstractMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * DML Source Directory
     * 
     * @parameter expression="${post-compile.dmlSourceDirectory}"
     *            default-value="src/main/dml/"
     */
    private File dmlDirectoryFile;
    /**
     * Classes Directory
     * 
     * @parameter expression="${post-compile.classesDirectory}"
     *            default-value="${project.build.outputDirectory}"
     */
    private File classesDirectory;
    /**
     * Post Processor Class Name
     * 
     * @parameter expression="${post-compile.classFullName}" default-value=
     *            "pt.ist.fenixframework.pstm.dml.FenixCodeGeneratorOneBoxPerObject"
     */
    private String classFullName;
    /**
     * Class Full Name
     * 
     * @parameter expression="${post-compile.domainModelClassName}"
     *            default-value
     *            ="pt.ist.fenixframework.pstm.dml.FenixDomainModel"
     */
    private String domainModelClassName;
    /**
     * Verbose Mode Flag
     * 
     * @parameter expression="${post-compile.verbose}" default-value="false"
     */
    private Boolean verbose;

    @Override
    public void execute() throws MojoExecutionException {
	if (mavenProject.getArtifact().getType().equals("pom")) {
	    getLog().info("Cannot post process domain for pom projects");
	    return;
	}

	try {
	    URLClassLoader loader = DmlMojoUtils.augmentClassLoader(getLog(), mavenProject);
	    List<URL> dmlFiles = new ArrayList<URL>();
	    for (DmlFile dmlFile : FenixFrameworkArtifact.fromName(mavenProject.getArtifactId()).getFullDmlSortedList()) {
		dmlFiles.add(dmlFile.getUrl());
	    }

	    if (dmlFiles.isEmpty()) {
		getLog().info("No dml files found to post process domain");
		return;
	    }

	    Class[] argsConstructor = new Class[] { List.class, this.classFullName.getClass(),
		    this.domainModelClassName.getClass(), ClassLoader.class };
	    Object[] args = new Object[] { dmlFiles, this.classFullName, this.domainModelClassName, loader };
	    Class postProcessDomainClassesClass = loader.loadClass("pt.ist.fenixframework.pstm.PostProcessDomainClasses");
	    Class transactionClass = loader.loadClass("pt.ist.fenixframework.pstm.Transaction");

	    Constructor processDomainClassesConstructor = postProcessDomainClassesClass.getConstructor(argsConstructor);
	    Object postProcessor = processDomainClassesConstructor.newInstance(args);

	    Object[] nullArgs = new Object[] {};
	    Method m = postProcessDomainClassesClass.getMethod("start", new Class[] {});
	    m.invoke(postProcessor, nullArgs);

	    ProcessAtomicAnnotations atomicAnnotationsProcessor = new ProcessAtomicAnnotations(transactionClass,
		    new String[] { "." });
	    atomicAnnotationsProcessor.start();

	    Class<?> serviceInjector = loader.loadClass("pt.ist.fenixframework.services.ServiceAnnotationInjector");
	    Method injector = serviceInjector.getMethod("inject", File.class, ClassLoader.class);
	    injector.invoke(null, classesDirectory, loader);
	} catch (Exception e) {
	    getLog().error(e);
	}
    }
}
