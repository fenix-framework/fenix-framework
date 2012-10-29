package dml;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CompilerArgs {
    boolean generateFinals = false;
    File destDirectoryBase = null;
    File destDirectory = null;
    String packageName = "";
    List<URL> dmls = new ArrayList<URL>();
    Class<? extends CodeGenerator> generatorClass = CodeGenerator.class;
    Class<? extends DomainModel> domainModelClass = DomainModel.class;

    public CompilerArgs(File destDirectory, File destDirectoryBase, String packageName, Boolean generateFinals,
	    Class<? extends CodeGenerator> generatorClass, Class<? extends DomainModel> domainModelClass, List<URL> dmls) {
	this.destDirectory = destDirectory;
	this.destDirectoryBase = destDirectoryBase;
	this.packageName = packageName;
	this.generateFinals = generateFinals;
	this.generatorClass = generatorClass != null ? generatorClass : CodeGenerator.class;
	this.domainModelClass = domainModelClass != null ? domainModelClass : DomainModel.class;
	this.dmls.addAll(dmls);
	checkArguments();
    }

    public CompilerArgs(String[] args) throws Exception {
	processCommandLineArgs(args);
	checkArguments();
    }

    void checkArguments() {
	if (destDirectory == null) {
	    error("destination directory is not specified");
	}
	if (!destDirectory.isDirectory()) {
	    error(destDirectory.toString() + " is not a readable directory");
	}
	if (dmls.isEmpty()) {
	    error("no domainSpec files given");
	}
    }

    void processCommandLineArgs(String[] args) throws Exception {
	boolean processingOptions = true;
	int num = 0;
	while (num < args.length) {
	    if (processingOptions) {
		int newNum = processOption(args, num);
		processingOptions = (newNum != num);
		num = newNum;
	    } else {
		dmls.add(new File(args[num]).toURI().toURL());
		num++;
	    }
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
	    error("option -f no longer exists (use -generator and -domainModelClass instead)");
	} else if (args[pos].equals("-gf")) {
	    generateFinals = true;
	    return pos + 1;
	} else if (args[pos].equals("-generator")) {
	    generatorClass = (Class<? extends CodeGenerator>) Class.forName(getNextArgument(args, pos));
	    return pos + 2;
	} else if (args[pos].equals("-domainModelClass")) {
	    domainModelClass = (Class<? extends DomainModel>) Class.forName(getNextArgument(args, pos));
	    return pos + 2;
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

    void error(String msg) {
	System.err.println("DmlCompiler: " + msg);
	System.exit(1);
    }

    public Class<? extends CodeGenerator> getCodeGenerator() {
	return generatorClass;
    }
}
