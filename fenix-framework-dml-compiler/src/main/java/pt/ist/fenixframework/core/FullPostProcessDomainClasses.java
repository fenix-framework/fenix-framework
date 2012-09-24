package pt.ist.fenixframework.core;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import pt.ist.fenixframework.atomic.ProcessAtomicAnnotations;
import pt.ist.fenixframework.core.DmlFile;
import pt.ist.fenixframework.core.PostProcessDomainClasses;
import pt.ist.fenixframework.core.Project;

/**
 * This class aggregates the whole post-processing required on the domain classes.
 */
public class FullPostProcessDomainClasses {
    private static final Logger logger = Logger.getLogger(FullPostProcessDomainClasses.class);

    /**
     * Programmatic API to start the full post-processing.
     *
     * @param projectName the name of the project being post-processed.  This is used to locate the
     * <code>&lt;projectName&gt;/propject.properties</code> file
     * @param codeGeneratorClassName the classname of the code generator to use
     * @param classesDirectory single directory with the compiled classes.  All classes here will be
     * post-processed if required.
     * @param parentClassLoader the class loader on which to delegate the loading of classes
     */
    public static void apply(String projectName, String codeGeneratorClassName, File classesDirectory,
                             ClassLoader parentClassLoader) throws Exception {
        injectCode(projectName, codeGeneratorClassName, parentClassLoader);
        ProcessAtomicAnnotations.processFile(classesDirectory);
    }
    
    /**
     * Programmatic API to start the full post-processing.
     *
     * @param projectName the name of the project being post-processed.  This is used to locate the
     * <code>&lt;projectName&gt;/propject.properties</code> file
     * @param codeGeneratorClassName the classname of the code generator to use
     * @param classDirectories several directories with the compiled classes.  All classes here will
     * be post-processed if required.
     * @param parentClassLoader the class loader on which to delegate the loading of classes
     */
    public static void apply(String projectName, String codeGeneratorClassName,
                             String [] classDirectories) throws Exception {
        
        injectCode(projectName, codeGeneratorClassName,
                   Thread.currentThread().getContextClassLoader());
        ProcessAtomicAnnotations.main(classDirectories);
    }


    /**
     * Invoke the domain class post-processor.
     */
    private static void injectCode(String projectName, String codeGeneratorClassName,
                                   ClassLoader parentClassLoader) throws Exception {
        List<URL> dmlFiles = new ArrayList<URL>();
        for (DmlFile dmlFile : Project.fromName(projectName).getFullDmlSortedList()) {
            dmlFiles.add(dmlFile.getUrl());
        }

        if (dmlFiles.isEmpty()) {
            logger.warn("No dml files found to post process domain");
            return;
        }

        PostProcessDomainClasses postProcessor =
            new PostProcessDomainClasses(dmlFiles, codeGeneratorClassName, parentClassLoader);
        postProcessor.start();
    }

}
