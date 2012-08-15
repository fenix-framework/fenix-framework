package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;

import pt.ist.fenixframework.DmlCompiler;
import pt.ist.fenixframework.dml.CodeGenerator;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.core.DmlFile;
import pt.ist.fenixframework.core.Project;

public abstract class AbstractDmlCodeGeneratorMojo extends AbstractMojo {

    protected abstract MavenProject getMavenProject();
    protected abstract String getCodeGeneratorClassName();
    protected abstract File getDmlSourceDirectory();
    protected abstract File getGeneratedSourcesDirectory();
    protected abstract File getSourcesDirectory();
    protected abstract String getOutputDirectoryPath();

    protected abstract String getPackageName();
    protected abstract boolean verbose();
    protected abstract boolean generateFinals();
    protected abstract boolean generateProjectProperties();

    public void execute() throws MojoExecutionException {
        if (getMavenProject().getArtifact().getType().equals("pom")) {
	    getLog().info("Cannot generate domain for pom projects");
	    return;
	}

	DmlMojoUtils.augmentClassLoader(getLog(), getMavenProject());

	CompilerArgs compArgs = null;
	long latestBuildTime = getGeneratedSourcesDirectory().lastModified();

	boolean shouldCompile = false;
	List<URL> dmlFiles = new ArrayList<URL>();
	if (getDmlSourceDirectory().exists()) {
	    DirectoryScanner scanner = new DirectoryScanner();
	    scanner.setBasedir(getDmlSourceDirectory());

	    String[] includes = { "**\\*.dml" };
	    scanner.setIncludes(includes);
	    scanner.scan();

	    Resource resource = new Resource();
	    resource.setDirectory(getDmlSourceDirectory().getAbsolutePath());
	    resource.addInclude("*.dml");
	    getMavenProject().addResource(resource);

	    for (String includedFile : scanner.getIncludedFiles()) {
		String filePath = getDmlSourceDirectory().getAbsolutePath() + "/" + includedFile;
		File file = new File(filePath);
		try {
		    dmlFiles.add(file.toURI().toURL());
		} catch (MalformedURLException e) {
		    getLog().error(e);
		}
		boolean isModified = file.lastModified() > latestBuildTime;
		if (verbose() == false) {
		    getLog().info(includedFile + " : " + (isModified ? "not up to date" : "up to date"));
		}
		shouldCompile = shouldCompile || isModified;
	    }
	}

	try {
	    Project project = DmlMojoUtils.getProject(getMavenProject(), getDmlSourceDirectory(), dmlFiles);

	    List<URL> allDmls = new ArrayList<URL>();
	    for (DmlFile dmlFile : project.getFullDmlSortedList()) {
		allDmls.add(dmlFile.getUrl());
	    }

	    project.generateProjectProperties(getOutputDirectoryPath());

	    if (allDmls.isEmpty()) {
		getLog().info("No dml files found to generate domain");
		return;
	    }

            // Split all DML files in two sets: local and external.
            List<URL> localDmls = new ArrayList<URL>();
            for (DmlFile dmlFile : project.getDmls()) {
                localDmls.add(dmlFile.getUrl());
            }
            List<URL> externalDmls = new ArrayList<URL>(allDmls);
            externalDmls.removeAll(localDmls);
            
	    shouldCompile = true;
	    if (shouldCompile) {
                getSourcesDirectory().mkdirs();
		getSourcesDirectory().setLastModified(System.currentTimeMillis());
		if (verbose()) {
		    getLog().info("Using generator: " + getCodeGeneratorClass().getName());
		}

		compArgs = new CompilerArgs(getSourcesDirectory(), getGeneratedSourcesDirectory(), getPackageName(),
                                            generateFinals(), getCodeGeneratorClass(), localDmls, externalDmls);

                DmlCompiler.compile(compArgs);
	    } else {
		if (verbose()) {
		    getLog().info("All dml files are up to date. Skipping generation...");
		}
	    }
	} catch (Exception e) {
	    getLog().error(e);
	}
    }

    public Class<? extends CodeGenerator> getCodeGeneratorClass() throws ClassNotFoundException {
        return (Class<? extends CodeGenerator>) Class.forName(getCodeGeneratorClassName());
    }
}
