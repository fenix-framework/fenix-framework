package dml.antTasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import dml.CodeGenerator;
import dml.CompilerArgs;
import dml.DmlCompiler;
import dml.DomainModel;

public class DmlCompileTask extends Task {

    private boolean generateFinals = false;
    private String destDirectoryBase = null;
    private String destDirectory = null;
    private String packageName = "";
    private final List<FileSet> filesets = new ArrayList<FileSet>();
    private String generatorClassName;
    private String domainModelClassName;
    private String classPathRef;

    public String getClassPathRef() {
	return classPathRef;
    }

    public void setClassPathRef(String classPathRef) {
	this.classPathRef = classPathRef;
    }

    public boolean isGenerateFinals() {
	return generateFinals;
    }

    public void setGenerateFinals(boolean generateFinals) {
	this.generateFinals = generateFinals;
    }

    public String getDestDirectoryBase() {
	return destDirectoryBase;
    }

    public void setDestDirectoryBase(String destDirectoryBase) {
	this.destDirectoryBase = destDirectoryBase;
    }

    public String getDestDirectory() {
	return destDirectory;
    }

    public void setDestDirectory(String destDirectory) {
	this.destDirectory = destDirectory;
    }

    public String getPackageName() {
	return packageName;
    }

    public void setPackageName(String packageName) {
	this.packageName = packageName;
    }

    public void addFileset(FileSet fileset) {
	filesets.add(fileset);
    }

    public String getGeneratorClassName() {
	return generatorClassName;
    }

    public void setGeneratorClassName(String generatorClassName) {
	this.generatorClassName = generatorClassName;
    }

    public String getDomainModelClassName() {
	return domainModelClassName;
    }

    public void setDomainModelClassName(String domainModelClassName) {
	this.domainModelClassName = domainModelClassName;
    }

    public File getDestDirectoryFile() {
	return (this.destDirectory == null) ? null : new File(destDirectory);
    }

    public File getDestDirectoryBaseFile() {
	return (this.destDirectoryBase == null) ? null : new File(destDirectoryBase);
    }

    public Class<? extends CodeGenerator> getCodeGeneratorClass() throws ClassNotFoundException {
	String generatorClassName = getGeneratorClassName();
	if (generatorClassName == null) {
	    return null;
	}
	return (Class<? extends CodeGenerator>) Class.forName(generatorClassName);
    }

    public Class<? extends DomainModel> getDomainModelClass() throws ClassNotFoundException {
	String domainModelClassName = getDomainModelClassName();
	if (domainModelClassName == null) {
	    return null;
	}
	return (Class<? extends DomainModel>) Class.forName(domainModelClassName);
    }

    @Override
    public void execute() throws BuildException {
	super.execute();

	CompilerArgs compArgs = null;

	List<String> domainSpecFileNames = new ArrayList<String>();

	for (FileSet fileset : filesets) {
	    if (fileset.getDir().exists()) {
		DirectoryScanner scanner = fileset.getDirectoryScanner(getProject());
		String[] includedFiles = scanner.getIncludedFiles();
		for (String includedFile : includedFiles) {
		    System.out.println("Including DML File: " + includedFile);
		    domainSpecFileNames.add(fileset.getDir().getAbsolutePath() + "/" + includedFile);
		}
	    }
	}

	System.out.println("Using model: " + getDomainModelClassName());
	System.out.println("Using generator: " + getGeneratorClassName());
	try {
	    compArgs = new CompilerArgs(getDestDirectoryFile(), getDestDirectoryBaseFile(), getPackageName(), isGenerateFinals(),
		    getCodeGeneratorClass(), getDomainModelClass(), domainSpecFileNames);

	    DomainModel model = DmlCompiler.getDomainModel(compArgs);

	    CodeGenerator generator = compArgs.getCodeGenerator().getConstructor(CompilerArgs.class, DomainModel.class)
		    .newInstance(compArgs, model);
	    generator.generateCode();
	} catch (Exception e) {
	    throw new BuildException(e);
	}

    }
}
