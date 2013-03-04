package pt.ist.fenixframework.dml.antTasks;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import pt.ist.fenixframework.DmlCompiler;
import pt.ist.fenixframework.dml.CodeGenerator;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DefaultCodeGenerator;

public class DmlCompileTask extends Task {

    private static final Class<? extends CodeGenerator> defaultCodeGeneratorClass = DefaultCodeGenerator.class;

    private boolean generateFinals = false;
    private String destDirectoryBase = null;
    private String destDirectory = null;
    private String packageName = "";
    private final List<FileSet> filesets = new ArrayList<FileSet>();
    private String generatorClassName;
    private String classPathRef;
    private String hasRun = DmlCompileTask.class.getName() + ".run";

    // this is defined when first requested (see the getter)
    private Class<? extends CodeGenerator> codeGeneratorClass;

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

    public File getDestDirectoryFile() {
        return (this.destDirectory == null) ? null : new File(destDirectory);
    }

    public File getDestDirectoryBaseFile() {
        return (this.destDirectoryBase == null) ? null : new File(destDirectoryBase);
    }

    public Class<? extends CodeGenerator> getCodeGeneratorClass() throws ClassNotFoundException {
        if (codeGeneratorClass == null) {
            String generatorClassName = getGeneratorClassName();
            if (generatorClassName == null) {
                codeGeneratorClass = defaultCodeGeneratorClass;
            } else {
                codeGeneratorClass = (Class<? extends CodeGenerator>) Class.forName(generatorClassName);
            }
        }
        return codeGeneratorClass;
    }

    @Override
    public void execute() throws BuildException {
        super.execute();

        CompilerArgs compArgs = null;

        List<String> localDomainSpecFileNames = new ArrayList<String>();
        File destDirectoryBaseFile = getDestDirectoryBaseFile();
        long latestBuildTime = destDirectoryBaseFile.lastModified();

        boolean shouldCompile = false;

        for (FileSet fileset : filesets) {
            if (fileset.getDir().exists()) {
                DirectoryScanner scanner = fileset.getDirectoryScanner(getProject());
                String[] includedFiles = scanner.getIncludedFiles();
                for (String includedFile : includedFiles) {
                    String filePath = fileset.getDir().getAbsolutePath() + "/" + includedFile;
                    File file = new File(filePath);
                    boolean isModified = file.lastModified() > latestBuildTime;
                    System.out.println(includedFile + " : " + (isModified ? "not up to date" : "up to date"));
                    localDomainSpecFileNames.add(filePath);
                    shouldCompile = shouldCompile || isModified;
                }
            }
        }

        if (shouldCompile) {
            try {
                destDirectoryBaseFile.setLastModified(System.currentTimeMillis());
                System.out.println("Using generator: " + getCodeGeneratorClass().getName());

                compArgs =
                        new CompilerArgs(getDestDirectoryFile(), destDirectoryBaseFile, getPackageName(), isGenerateFinals(),
                                getCodeGeneratorClass(), CompilerArgs.convertFilenamesToURLs(localDomainSpecFileNames),
                                new ArrayList<URL>(), new HashMap<String, String>());

                DmlCompiler.compile(compArgs);

                getProject().setProperty(getHasRun(), Boolean.TRUE.toString());
            } catch (Exception e) {
                throw new BuildException(e);
            }
        } else {
            System.out.println("All dml files are up to date skipping generation");
        }
    }
}
