package pt.ist.fenixframework.pstm.antTasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;

import pt.ist.fenixframework.pstm.dml.FenixDomainModel;

public class PostCompileTask extends Task {

    private final List<FileSet> filesets = new ArrayList<FileSet>();
    private String classFullName;
    private String domainModelClassName = FenixDomainModel.class.getName();
    private CommandlineJava commandline;
    private File dir = null;
    private Environment env = new Environment();
    private static final String POST_PROCESSOR_CLASS = "pt.ist.fenixframework.pstm.PostProcessDomainClasses";

    public String getClassFullName() {
	return classFullName;
    }

    public void setClassFullName(String classFullName) {
	this.classFullName = classFullName;
    }

    public String getDomainModelClassName() {
	return domainModelClassName;
    }

    public void setDomainModelClassName(String domainModelClassName) {
	this.domainModelClassName = domainModelClassName;
    }

    public List<FileSet> getFilesets() {
	return filesets;
    }

    public void addFileset(FileSet fileset) {
	filesets.add(fileset);
    }

    public void setDir(File dir) {
	this.dir = dir;
    }

    protected CommandlineJava getCommandline() {
	if (commandline == null) {
	    commandline = new CommandlineJava();
	}
	return commandline;
    }

    protected String getPostProcessorClass() {
	return POST_PROCESSOR_CLASS;
    }

    public void setClasspath(Path s) {
	createClasspath().append(s);
    }

    public Path createClasspath() {
	return getCommandline().createClasspath(getProject()).createPath();
    }

    @Override
    public void execute() throws BuildException {
	super.execute();

	List<String> domainSpecFileNames = new ArrayList<String>();

	for (FileSet fileset : filesets) {
	    if (fileset.getDir().exists()) {
		DirectoryScanner scanner = fileset.getDirectoryScanner(getProject());
		String[] includedFiles = scanner.getIncludedFiles();
		for (String includedFile : includedFiles) {
		    domainSpecFileNames.add(fileset.getDir().getAbsolutePath() + "/" + includedFile);
		}
	    }
	}

	try {
	    executeAsForked(domainSpecFileNames, getClassFullName(), getDomainModelClassName());
	} catch (Exception e) {
	    throw new BuildException(e);
	}
    }

    private void executeAsForked(List<String> dmlFiles, String classFullName, String domainModelClassName) throws BuildException {

	CommandlineJava cmd;
	try {
	    cmd = (CommandlineJava) (getCommandline().clone());
	} catch (CloneNotSupportedException e) {
	    throw new BuildException("This shouldn't happen", e, getLocation());
	}
	cmd.setClassname(POST_PROCESSOR_CLASS);
	if (classFullName != null) {
	    cmd.createArgument().setValue("-cfn");
	    cmd.createArgument().setValue(classFullName);
	}
	if (domainModelClassName != null) {
	    cmd.createArgument().setValue("-domainModelClass");
	    cmd.createArgument().setValue(domainModelClassName);

	}
	for (String file : dmlFiles) {
	    cmd.createArgument().setValue("-d");
	    cmd.createArgument().setValue(file);
	}

	Execute execute = new Execute();
	execute.setCommandline(cmd.getCommandline());
	execute.setAntRun(getProject());
	if (dir != null) {
	    execute.setWorkingDirectory(dir);
	}

	String[] environment = env.getVariables();
	execute.setNewenvironment(true);
	execute.setEnvironment(environment);
	try {
	    execute.execute();
	} catch (IOException e) {
	    throw new BuildException("Process fork failed.", e, getLocation());
	}
    }

}
