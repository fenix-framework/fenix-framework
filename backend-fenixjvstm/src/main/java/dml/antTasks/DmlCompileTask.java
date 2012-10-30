package dml.antTasks;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
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

    private static final String defaultDomainModelClassName = "pt.ist.fenixframework.pstm.dml.FenixDomainModel";
    private static final String defaultCodeGeneratorClassName = "pt.ist.fenixframework.pstm.dml.FenixCodeGeneratorOneBoxPerObject";

    private boolean generateFinals = false;
    private String destDirectoryBase = null;
    private String destDirectory = null;
    private String packageName = "";
    private final List<FileSet> filesets = new ArrayList<FileSet>();
    private String generatorClassName;
    private String domainModelClassName;
    private String classPathRef;
    private String hasRun = DmlCompileTask.class.getName() + ".run";

    public String getHasRun() {
	return hasRun;
    }

    public void setHasRun(String hasRun) {
	this.hasRun = hasRun;
    }

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
	    generatorClassName = defaultCodeGeneratorClassName;
	}
	return (Class<? extends CodeGenerator>) Class.forName(generatorClassName);
    }

    public Class<? extends DomainModel> getDomainModelClass() throws ClassNotFoundException {
	String domainModelClassName = getDomainModelClassName();
	if (domainModelClassName == null) {
	    domainModelClassName = defaultDomainModelClassName;
	}
	return (Class<? extends DomainModel>) Class.forName(domainModelClassName);
    }

    @Override
    public void execute() throws BuildException {
	super.execute();

	CompilerArgs compArgs = null;

	List<URL> domainSpecFileNames = new ArrayList<URL>();
	File destDirectoryBaseFile = getDestDirectoryBaseFile();
	long latestBuildTime = destDirectoryBaseFile.lastModified();

	boolean shouldCompile = false;

	try {
	    for (FileSet fileset : filesets) {
		if (fileset.getDir().exists()) {
		    DirectoryScanner scanner = fileset.getDirectoryScanner(getProject());
		    String[] includedFiles = scanner.getIncludedFiles();
		    for (String includedFile : includedFiles) {
			String filePath = fileset.getDir().getAbsolutePath() + "/" + includedFile;
			File file = new File(filePath);
			boolean isModified = file.lastModified() > latestBuildTime;
			System.out.println(includedFile + " : " + (isModified ? "not up to date" : "up to date"));
			domainSpecFileNames.add(file.toURI().toURL());
			shouldCompile = shouldCompile || isModified;
		    }
		}
	    }
	} catch (MalformedURLException e) {
	    throw new BuildException(e);
	}

	if (shouldCompile) {
	    try {
		destDirectoryBaseFile.setLastModified(System.currentTimeMillis());
		System.out.println("Using model: " + getDomainModelClass().getName());
		System.out.println("Using generator: " + getCodeGeneratorClass().getName());

		compArgs = new CompilerArgs(getDestDirectoryFile(), destDirectoryBaseFile, getPackageName(), isGenerateFinals(),
			getCodeGeneratorClass(), getDomainModelClass(), domainSpecFileNames);

		DomainModel model = DmlCompiler.getDomainModel(compArgs);

		CodeGenerator generator = compArgs.getCodeGenerator().getConstructor(CompilerArgs.class, DomainModel.class)
			.newInstance(compArgs, model);
		generator.generateCode();
		getProject().setProperty(getHasRun(), Boolean.TRUE.toString());
	    } catch (Exception e) {
		throw new BuildException(e);
	    }
	} else {
	    System.out.println("All dml files are up to date skipping generation");
	}
    }
}
