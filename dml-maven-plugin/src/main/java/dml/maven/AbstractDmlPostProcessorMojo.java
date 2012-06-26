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


public abstract class AbstractDmlPostProcessorMojo extends AbstractMojo {

    /**
     * Maven Project
     *
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    protected abstract File getDmlSourceDirectory();
    protected abstract File getClassesDirectory();
    protected abstract String getCodeGeneratorClassName();
    protected abstract String getDomainModelClassName();
    protected abstract boolean verbose();

    @Override
    public void execute() throws MojoExecutionException {
        if (mavenProject.getArtifact().getType().equals("pom")) {
            getLog().info("Cannot post process domain for pom projects");
            return;
        }

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
        List<String> dmlFileList = DmlMojoUtils.readDmlFilePathsFromArtifact(getLog(), mavenProject.getArtifacts());
        dmlFiles.addAll(dmlFileList);

            if (getDmlSourceDirectory() != null && getDmlSourceDirectory().exists()) {
                DirectoryScanner scanner = new DirectoryScanner();
                scanner.setBasedir(getDmlSourceDirectory());

                String[] includes = { "**\\*.dml" };
                scanner.setIncludes(includes);
                scanner.scan();

                String[] includedFiles = scanner.getIncludedFiles();
                for (String includedFile : includedFiles) {
                    String filePath = getDmlSourceDirectory().getAbsolutePath() + "/" + includedFile;
                    if(verbose()) {
                        getLog().info("Dml File: " + includedFile);
                        getLog().info("Classes Directory: " + getClassesDirectory().getAbsolutePath());
                        getLog().info("Using Model: " + getDomainModelClassName());
                        getLog().info("Using Generator: " + getCodeGeneratorClassName());

                    }
                    dmlFiles.add(filePath);
                }
            }
            if(dmlFiles.isEmpty()) {
            getLog().info("No dml files found to post process domain");
            return;
            }

        URLClassLoader loader = new URLClassLoader(classesURL, Thread.currentThread().getContextClassLoader());

        Class[] argsConstructor = new Class[] { List.class, String.class, getDomainModelClassName().getClass(),
            ClassLoader.class };
        Object[] args = new Object[] { dmlFiles, getCodeGeneratorClassName(), getDomainModelClassName(), loader };
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
            injector.invoke(null, getClassesDirectory(), loader);
        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
