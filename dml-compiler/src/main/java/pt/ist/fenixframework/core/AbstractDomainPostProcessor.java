package pt.ist.fenixframework.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import pt.ist.fenixframework.DmlCompiler;
import pt.ist.fenixframework.dml.DmlCompilerException;
import pt.ist.fenixframework.dml.DomainClass;
import pt.ist.fenixframework.dml.DomainModel;

public abstract class AbstractDomainPostProcessor extends ClassLoader implements Opcodes {
    protected ArrayList<URL> dmlFiles = new ArrayList<URL>();
    private HashSet<String> loadedClasses = new HashSet<String>();

    private DomainModel domainModel;
    // protected String classFullName;

    protected AbstractDomainPostProcessor() {
    }

    protected AbstractDomainPostProcessor(ClassLoader parentClassLoader) {
	super(parentClassLoader);
    }

    // --------------------------------
    // HACK!!!
    //
    // All this command line processing is copied&pasted from
    // src_tools/pt/utl/ist/analysis/ClassFilesVisitor
    //
    // I should refactor this soon!

    public void processArgs(String[] args) throws MalformedURLException {
	int i = 0;
	while (i < args.length) {
	    if ("-d".equals(args[i])) {
		final String arg = getNextArg(args, i);
		final File dir = new File(arg);
		if (dir.isDirectory()) {
		    final List<String> urls = new ArrayList<String>();
		    for (final File file : dir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".dml")) {
			    try {
				urls.add(file.getCanonicalPath());
			    } catch (IOException e) {
				throw new Error(e);
			    }
			}
		    }
		    Collections.sort(urls);

		    for (final String url : urls) {
			dmlFiles.add(new File(url).toURI().toURL());
		    }
		} else {
		    dmlFiles.add(new File(arg).toURI().toURL());
		}
		consumeArg(args, i);
		i += 2;
	    // } else if ("-cfn".equals(args[i])) {
	    //     classFullName = getNextArg(args, i);
	    //     consumeArg(args, i);
	    //     i += 2;
	    } else if (args[i] != null) {
		throw new Error("Unknown argument: '" + args[i] + "'");
	    } else {
		i++;
	    }
	}
    }

    protected void consumeArg(String[] args, int i) {
	args[i] = null;
    }

    protected String getNextArg(String[] args, int i) {
	int next = i + 1;
	if ((next >= args.length) || (args[next] == null)) {
	    throw new Error("Invalid argument following '" + args[i] + "'");
	}
	String result = args[next];
	consumeArg(args, next);
	return result;
    }

    protected DomainModel getModel() {
	if (domainModel == null) {
	    if (dmlFiles.isEmpty()) {
		throw new Error("No DML files specified");
	    } else {
		try {
		    domainModel = DmlCompiler.getDomainModel(dmlFiles);
		} catch (DmlCompilerException e) {
		    System.err.println("Error parsing the DML files, leaving the domain empty");
		}
	    }
	}
	return domainModel;
    }

    public static String descToName(String desc) {
	return desc.replace('/', '.');
    }

    public static String nameToDesc(String name) {
	return name.replace('.', '/');
    }

    public boolean isDomainBaseClass(String name) {
	return (name.endsWith("_Base") && (getModel().findClass(name.substring(0, name.length() - 5)) != null));
    }

    public boolean isDomainNonBaseClass(String name) {
	return (getModel().findClass(name) != null);
    }

    public boolean belongsToDomainModel(String name) {
	return isDomainNonBaseClass(name) || isDomainBaseClass(name);
    }

    protected abstract ClassVisitor makeNewClassVisitor(ClassWriter cw);

    protected void finishedProcessingClass(URL classURL, byte[] classBytecode) {
	// do nothing by default
    }

    @Override
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
	if ((!belongsToDomainModel(name)) || loadedClasses.contains(name)) {
	    return super.loadClass(name, resolve);
	}

	// find the resource for the class file
	URL classURL = getResource(nameToDesc(name) + ".class");
	if (classURL == null) {
	    throw new ClassNotFoundException(name);
	}

	InputStream is = null;
	byte[] bytecode;

	try {
	    // get an input stream to read the bytecode of the class
	    is = classURL.openStream();
	    ClassReader cr = new ClassReader(is);
	    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
	    ClassVisitor cv = makeNewClassVisitor(cw);
	    cr.accept(cv, 0);
	    bytecode = cw.toByteArray();
	    finishedProcessingClass(classURL, bytecode);
	} catch (Exception e) {
	    throw new ClassNotFoundException(name, e);
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (Exception e) {
		    // intentionally empty
		}
	    }
	}

	loadedClasses.add(name);

	return defineClass(name, bytecode, 0, bytecode.length);
    }

    public void start() {
	for (Iterator iter = getModel().getClasses(); iter.hasNext();) {
	    String className = ((DomainClass) iter.next()).getFullName();
	    try {
		loadClass(className);
	    } catch (ClassNotFoundException cnfe) {
		System.err.println("Error: Couldn't load class " + className + ": " + cnfe);
	    }
	}
    }
}
