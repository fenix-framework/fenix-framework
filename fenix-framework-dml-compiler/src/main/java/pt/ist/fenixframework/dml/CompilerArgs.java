package pt.ist.fenixframework.dml;

import java.io.*;
import java.net.URL;
import java.util.*;

import pt.ist.fenixframework.util.Converter;

/**
 * Information about CompilerArgs:
 * 
 * <table border=1>
 * <tr><th>Type</th><th>Name</th><th>Description</th>
 * <tr>
 *   <td><code>boolean</code></td> 
 *   <td><code>generateFinals</code></td>
 *   <td> Whether to add the <code>final</code> keyword to the accessor methods.  Default: <code>false</code>.</td>
 * </tr>
 *
 * <tr>
 *   <td><code>File</code></td>
 *   <td><code>destDirectoryBase</code></td>
 *   <td>The top level directory where to generate the base classes.</td>
 * </tr>
 *
 * <tr><td><code>File</code></td>
 *   <td><code>destDirectory</code></td>
 *   <td> The top level directory where to generate the non-base classes.</td>
 * </tr>
 *
 * <tr><td><code>String</code></td>
 *   <td><code>packageName</code></td>
 *   <td>The default package name.  Default: <code>""</code>.</td>
 * </tr>
 *
 * <tr><td><code>List&lt;URL&gt;</code></td>
 *   <td><code>localDomainSpecs</code></td>
 *   <td>The list of project-local DML specifications.  The compiler will generate base classes (and non-base classes if they do not exist) for these DML specifications.  Default: empty list.</td>
 * </tr>
 *
 * <tr><td><code>List&lt;URL&gt;</code></td>
 *   <td><code>externalDomainSpecs</code></td>
 *   <td>The list of project-external DML specifications.  The compiler will only generate base classes for these DML specifications.  Default: empty list.</td>
 * </tr>
 *
 * <tr><td><code>Class&lt;? extends CodeGenerator&gt;</code></td>
 *   <td><code>generatorClass</code></td>
 *   <td> The code generator to use.  Default: <code>DefaultCodeGenerator.class</code>.</td>
 * </tr>
 *
 * </ul>
 */
public class CompilerArgs {
    boolean generateFinals = false;
    File destDirectoryBase = null;
    File destDirectory = null;
    String packageName = "";
    List<URL> localDomainSpecs = new ArrayList<URL>();
    List<URL> externalDomainSpecs = new ArrayList<URL>();
    Class<? extends CodeGenerator> generatorClass = DefaultCodeGenerator.class;

    /*
     * This is not part of the API
     * if true => add to local
     * if false => add to external
     * This value is changed by the method processOption
     */
    private boolean addToLocalDomainSpecs = true; 

    public CompilerArgs(File destDirectory, File destDirectoryBase, String packageName, Boolean generateFinals,
	    Class<? extends CodeGenerator> generatorClass, List<URL> localDomainSpecs,
                        List<URL> externalDomainSpecs) {

	this.destDirectory = destDirectory;
	this.destDirectoryBase = destDirectoryBase;
	this.packageName = packageName;
	this.generateFinals = generateFinals;
	this.generatorClass = generatorClass != null ? generatorClass : DefaultCodeGenerator.class;
	if (localDomainSpecs != null) { this.localDomainSpecs.addAll(localDomainSpecs); }
        if (externalDomainSpecs != null) {this.externalDomainSpecs.addAll(externalDomainSpecs); }
	checkArguments();
    }

    public CompilerArgs(String[] args) throws DmlCompilerException {
        try {
            processCommandLineArgs(args);
            checkArguments();
        } catch (Exception e) {
            throw new DmlCompilerException(e);
        }
    }

    void checkArguments() {
	if (destDirectory == null) {
	    error("destination directory is not specified");
	}
	if (!destDirectory.isDirectory()) {
	    error(destDirectory.toString() + " is not a readable directory");
	}
	if (localDomainSpecs.isEmpty() && externalDomainSpecs.isEmpty()) {
	    error("no domainSpec files given (-localDmlSpec and/or -externalDmlSpec)");
	}
    }

    void processCommandLineArgs(String[] args) throws Exception {
	int num = 0;
	while (num < args.length) {
            num = processOption(args, num);
	}
    }

    int processOption(String[] args, int pos) throws Exception {
	if (args[pos].equals("-d")) {
	    destDirectory = new File(getNextArgument(args, pos));
	    return pos + 2;
	} else if (args[pos].equals("-db")) {
	    destDirectoryBase = new File(getNextArgument(args, pos));
	    return pos + 2;
	} else if (args[pos].equals("-p")) {
	    packageName = getNextArgument(args, pos);
	    return pos + 2;
	} else if (args[pos].equals("-f")) {
	    error("option -f no longer exists (consider -generator instead)");
	} else if (args[pos].equals("-gf")) {
	    generateFinals = true;
	    return pos + 1;
	} else if (args[pos].equals("-generator")) {
	    generatorClass = (Class<? extends CodeGenerator>) Class.forName(getNextArgument(args, pos));
	    return pos + 2;
	} else if (args[pos].equals("-domainModelClass")) {
	    error("option -domainModelClass no longer exists");
	} else if (args[pos].equals("-localDmlSpec")) {
            // switch default to local
            addToLocalDomainSpecs = true;

            addToDomainSpecs(getNextArgument(args, pos), addToLocalDomainSpecs);
            return pos + 2;
	} else if (args[pos].equals("-externalDmlSpec")) {
            // switch default to external
            addToLocalDomainSpecs = false;

            addToDomainSpecs(getNextArgument(args, pos), addToLocalDomainSpecs);
            return pos + 2;
        } else {
            // use last set default
            addToDomainSpecs(args[pos], addToLocalDomainSpecs);
            return pos + 1;
        }
	return pos;
    }

    String getNextArgument(String[] args, int pos) {
	int nextPos = pos + 1;
	if (nextPos < args.length) {
	    return args[nextPos];
	} else {
	    error("option " + args[pos] + " requires argument");
	}
	return null;
    }

    void addToDomainSpecs(String domainSpecFilename, boolean addToLocal) {
        URL url = Converter.filenameToURL(domainSpecFilename);
        if (addToLocal) {
            localDomainSpecs.add(url);
        } else {
            externalDomainSpecs.add(url);
        }
    }

    void error(String msg) {
	System.err.println("DmlCompiler: " + msg);
	System.exit(1);
    }

    public Class<? extends CodeGenerator> getCodeGenerator() {
	return generatorClass;
    }

    public List<URL> getLocalDomainSpecs() {
        return Collections.unmodifiableList(localDomainSpecs);
    }

    public List<URL> getExternalDomainSpecs() {
        return Collections.unmodifiableList(externalDomainSpecs);
    }

    public boolean isExternalDefinition(URL domainSpec) {
        return this.externalDomainSpecs.contains(domainSpec);
    }

    // static

    public static List<URL> convertFilenamesToURLs(List<String> filenames) {
        return Arrays.asList(Converter.filenamesToURLArray(filenames.toArray(new String[filenames.size()])));
    }

    public static URL convertFilenameToURL(String filename) {
        return Converter.filenameToURL(filename);
    }
}
