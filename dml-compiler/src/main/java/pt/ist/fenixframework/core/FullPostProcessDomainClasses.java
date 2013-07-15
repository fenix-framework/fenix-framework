package pt.ist.fenixframework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(FullPostProcessDomainClasses.class);

    private String projectName;
    private File dir;

    private void processArgs(String[] args) {
	int i = 0;
	while (i < args.length) {
	    if ("-cd".equals(args[i])) {
                final String dirName = getNextArg(args, i);
                dir = new File(dirName);
                if (!dir.isDirectory()) {
                    String message = "Parameter -cd value '" + dirName + "' is not a directory.";
                    if (logger.isErrorEnabled()) {
                        logger.error(message);
                    }
                    throw new Error(message);
                }
                consumeArg(args, i);
                i += 2;
	    } else if ("-pn".equals(args[i])) {
		projectName = getNextArg(args, i);
		consumeArg(args, i);
		i += 2;
	    } else if (args[i] != null) {
		throw new Error("Unknown argument: '" + args[i] + "'");
	    } else {
		i++;
	    }
	}
    }

    private void consumeArg(String[] args, int i) {
	args[i] = null;
    }

    private String getNextArg(String[] args, int i) {
	int next = i + 1;
	if ((next >= args.length) || (args[next] == null)) {
            String message = "Invalid argument following '" + args[i] + "'";
        if (logger.isErrorEnabled()) {
            logger.error(message);
        }
	    throw new Error(message);
	}
	String result = args[next];
	consumeArg(args, next);
	return result;
    }


    /**
     * Command-line API to start the full post-processing.
     * @param args Expected values are:
     *
     * <ul>
     *
     * <li><code>-pn &lt;projectName&gt;</code>: the name of the project being post-processed.  This
     * is used to locate the <code>&lt;projectName&gt;/project.properties</code> file;</li>
     * <li><code>-cd &lt;classesDirectory&gt;</code>: single directory with the compiled classes.
     * All classes here will be post-processed if required.</li>
     *
     * </ul>
     */
    public static void main(String args[]) throws Exception {
        FullPostProcessDomainClasses processor = new FullPostProcessDomainClasses();
        processor.processArgs(args);
        apply(processor.projectName, processor.dir, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Programmatic API to start the full post-processing.
     *
     * @param projectName the name of the project being post-processed.  This is used to locate the
     * <code>&lt;projectName&gt;/project.properties</code> file
     * @param classesDirectory single directory with the compiled classes.  All classes here will be
     * post-processed if required.
     * @param parentClassLoader the class loader on which to delegate the loading of classes
     */
    public static void apply(String projectName, File classesDirectory,
                             ClassLoader parentClassLoader) throws Exception {
        injectCode(projectName, parentClassLoader);
        ProcessAtomicAnnotations.processFile(classesDirectory);
    }
    
    /**
     * Invoke the domain class post-processor.
     */
    private static void injectCode(String projectName, ClassLoader parentClassLoader)
        throws Exception {
        List<URL> dmlFiles = new ArrayList<URL>();
        for (DmlFile dmlFile : Project.fromName(projectName).getFullDmlSortedList()) {
            dmlFiles.add(dmlFile.getUrl());
        }

        if (dmlFiles.isEmpty()) {
            if (logger.isWarnEnabled()) {
                logger.warn("No dml files found to post process domain");
            }
            return;
        }

        PostProcessDomainClasses postProcessor =
            new PostProcessDomainClasses(dmlFiles, parentClassLoader);
        postProcessor.start();
    }

}
