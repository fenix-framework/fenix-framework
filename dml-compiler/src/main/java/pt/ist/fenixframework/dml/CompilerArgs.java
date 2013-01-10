package pt.ist.fenixframework.dml;

import java.io.*;
import java.net.URL;
import java.util.*;

import pt.ist.fenixframework.core.DmlFile;
import pt.ist.fenixframework.core.Project;
import pt.ist.fenixframework.util.Converter;

/**
 * Information about CompilerArgs:
 * 
 * <table border=1>
 * <tr><th>Type</th><th>Name</th><th>Command-line flag</th><th>Description</th><th>Required</th>
 * <tr>
 *   <td><code>boolean</code></td> 
 *   <td><code>generateFinals</code></td>
 *   <td><code>gf</code></td>
 *   <td> Whether to add the <code>final</code> keyword to the accessor methods.  Default: <code>false</code>.</td>
 *   <td>No</td>
 * </tr>
 *
 * <tr>
 *   <td><code>File</code></td>
 *   <td><code>destDirectoryBase</code></td>
 *   <td><code>db</code></td>
 *   <td>The top level directory where to generate the base classes.</td>
 *   <td>No. Defaults to <code>destDirectory</code></td>
 * </tr>
 *
 * <tr><td><code>File</code></td>
 *   <td><code>destDirectory</code></td>
 *   <td><code>d</code></td>
 *   <td> The top level directory where to generate the non-base classes.</td>
 *   <td>Yes</td>
 * </tr>
 *
 * <tr><td><code>String</code></td>
 *   <td><code>packageName</code></td>
 *   <td><code>p</code></td>
 *   <td>The default package name.  Default: <code>""</code>.</td>
 *   <td>No</td>
 * </tr>
 *
 * <tr><td><code>List&lt;URL&gt;</code></td>
 *   <td><code>localDomainSpecs</code></td>
 *   <td><code>localDmlSpec</code></td>
 *   <td>The list of project-local DML specifications.  For these DML specifications, the compiler will generate base classes (and non-base classes if they do not exist).  Default: empty list.</td>
 *   <td rowspan="3">Must provide DML specifications either using <code>localDmlSpecs/externalDmlSpecs</code> or <code>projectName</code> </td>
 * </tr>
 *
 * <tr><td><code>List&lt;URL&gt;</code></td>
 *   <td><code>externalDomainSpecs</code></td>
 *   <td><code>externalDmlSpec</code></td>
 *   <td>The list of project-external DML specifications.  For these DML specifications, the compiler will generate base classes only.  Default: empty list.</td>
 * </tr>
 *
 * <tr><td><code>String</code></td>
 *   <td><code>projectName</code></td>
 *   <td><code>pn</code></td>
 *   <td>The name of the project.  The compiler will search for a &lt;projectName&gt;/project.properties file with the local DML files and other project dependencies to set the local/external specs.</td>
 * </tr>
 *
 * <tr><td><code>Class&lt;? extends CodeGenerator&gt;</code></td>
 *   <td><code>generatorClass</code></td>
 *   <td><code>generator</code></td>
 *   <td> The code generator to use.  Default: <code>DefaultCodeGenerator.class</code>.</td>
 *   <td>No</td>
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
    Map<String,String> params = new HashMap<String,String>();

    /*
     * This is not part of the API
     * if true => add to local
     * if false => add to external
     * This value is changed by the method processOption
     */
    private boolean addToLocalDomainSpecs = true; 

    private CompilerArgs(File destDirectory, File destDirectoryBase, String packageName, Boolean generateFinals,
	    Class<? extends CodeGenerator> generatorClass, Map<String,String> params) {

	this.destDirectory = destDirectory;
	this.destDirectoryBase = destDirectoryBase;
	this.packageName = packageName;
	this.generateFinals = generateFinals;
	this.generatorClass = generatorClass != null ? generatorClass : DefaultCodeGenerator.class;
	this.params = new HashMap<String,String>(params);
    }

    /**
     *  Create the CompilerArgs using a local and external DML files specification
     */
    public CompilerArgs(File destDirectory, File destDirectoryBase, String packageName,
                        Boolean generateFinals, Class<? extends CodeGenerator> generatorClass,
                        List<URL> localDomainSpecs, List<URL> externalDomainSpecs, Map<String,String> params) {

        this(destDirectory, destDirectoryBase, packageName, generateFinals, generatorClass, params);
	if (localDomainSpecs != null) { this.localDomainSpecs.addAll(localDomainSpecs); }
        if (externalDomainSpecs != null) {this.externalDomainSpecs.addAll(externalDomainSpecs); }
	checkArguments();
    }

    /**
     *  Create the CompilerArgs using the project name (requires a
     *  <code>&lt;projName&gt;</code>/project.properties file in the classpath).
     */
    public CompilerArgs(File destDirectory, File destDirectoryBase, String packageName,
                        Boolean generateFinals, Class<? extends CodeGenerator> generatorClass,
                        String projectName, Map<String,String> params) throws DmlCompilerException {

        this(destDirectory, destDirectoryBase, packageName, generateFinals, generatorClass, params);
        try {
            setDmlSpecsFromProjectName(projectName);
        } catch (Exception e) {
            throw new DmlCompilerException(e);
        }
        checkArguments();
    }

    /**
     *  Create the CompilerArgs using command-line args.
     */
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
	    error("no domainSpec files given (-localDmlSpec and/or -externalDmlSpec) and no project name give (-pn).  Must provide one of the alternatives");
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
	} else if (args[pos].equals("-pn")) {
            String projectName = getNextArgument(args, pos);
            setDmlSpecsFromProjectName(projectName);
            return pos + 2;
        } else if (args[pos].equals("-param")) {
	    String paramValue = getNextArgument(args, pos);
	    String[] vals = paramValue.split("=");
            params.put(vals[0],vals[1]);
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
    
    private void setDmlSpecsFromProjectName(String projectName) throws Exception {
        Project project = Project.fromName(projectName);
        List<URL> allDmls = new ArrayList<URL>();
        for (DmlFile dmlFile : project.getFullDmlSortedList()) {
            allDmls.add(dmlFile.getUrl());
        }
        
        List<URL> localDmls = new ArrayList<URL>();
        for (DmlFile dmlFile : project.getDmls()) {
            localDmls.add(dmlFile.getUrl());
        }
        List<URL> externalDmls = new ArrayList<URL>(allDmls);
        externalDmls.removeAll(localDmls);

        this.localDomainSpecs = localDmls;
        this.externalDomainSpecs = externalDmls;
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
    
    public Map<String,String> getParams() {
        return Collections.unmodifiableMap(params);
    }

    // static

    public static List<URL> convertFilenamesToURLs(List<String> filenames) {
        return Arrays.asList(Converter.filenamesToURLArray(filenames.toArray(new String[filenames.size()])));
    }

    public static URL convertFilenameToURL(String filename) {
        return Converter.filenameToURL(filename);
    }
}
