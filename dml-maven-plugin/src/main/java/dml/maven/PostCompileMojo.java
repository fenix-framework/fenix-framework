package dml.maven;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import jvstm.ProcessAtomicAnnotations;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

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

	List<String> classpathElements = null;
	try {
	    classpathElements = this.mavenProject.getCompileClasspathElements();
	} catch (DependencyResolutionRequiredException e) {
	    getLog().error(e);
	}

	URL[] classesURL = new URL[classpathElements.size()];
	int i = 0;

	for (String path : classpathElements) {
	    try {
		classesURL[i++] = new File(path).toURI().toURL();
	    } catch (MalformedURLException e) {
		getLog().error(e);
	    }
	}

	List<String> dmlFiles = new ArrayList<String>();

	DirectoryScanner scanner = new DirectoryScanner();
	scanner.setBasedir(this.dmlDirectoryFile);

	String[] includes = { "**\\*.dml" };
	scanner.setIncludes(includes);
	scanner.scan();

	List<String> dmlFileList = DmlMojoUtils.readDmlFilePathsFromArtifact(getLog(), mavenProject.getArtifacts());
	dmlFiles.addAll(dmlFileList);

	String[] includedFiles = scanner.getIncludedFiles();
	for (String includedFile : includedFiles) {
	    String filePath = this.dmlDirectoryFile.getAbsolutePath() + "/" + includedFile;
	    if (this.verbose) {
		getLog().info(
			"Using: " + includedFile + "\nClass Full Name: " + this.classFullName + "\nDomain Model Class Name: "
				+ this.domainModelClassName + "\nClasses Directory: " + this.classesDirectory);
	    }
	    dmlFiles.add(filePath);
	}

	URLClassLoader loader = new URLClassLoader(classesURL, Thread.currentThread().getContextClassLoader());

	Class[] argsConstructor = new Class[] { List.class, this.classFullName.getClass(), this.domainModelClassName.getClass(),
		ClassLoader.class };
	Object[] args = new Object[] { dmlFiles, this.classFullName, this.domainModelClassName, loader };
	try {
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
