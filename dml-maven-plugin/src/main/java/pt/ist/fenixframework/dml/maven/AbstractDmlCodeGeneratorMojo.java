package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

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

	protected abstract Map<String,String> getParams();

	protected abstract List<String> getClasspathElements();

    @Override
    public void execute() throws MojoExecutionException {
	if (getMavenProject().getArtifact().getType().equals("pom")) {
	    getLog().info("Cannot generate domain for pom projects");
	    return;
	}

	DmlMojoUtils.augmentClassLoader(getLog(), getClasspathElements());

	CompilerArgs compArgs = null;
	long latestBuildTime = getGeneratedSourcesDirectory().lastModified();

	boolean shouldCompile = getMavenProject().getArtifact().getType().trim().equalsIgnoreCase("war");
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
	    getMavenProject().addTestResource(resource);

	    for (String includedFile : scanner.getIncludedFiles()) {
		String filePath = getDmlSourceDirectory().getAbsolutePath() + "/" + includedFile;
		File file = new File(filePath);
		try {
		    dmlFiles.add(file.toURI().toURL());
		} catch (MalformedURLException e) {
		    getLog().error(e);
		}
		boolean isModified = file.lastModified() > latestBuildTime;
		if (verbose()) {
		    getLog().info(includedFile + " : " + (isModified ? "not up to date" : "up to date"));
		}
		shouldCompile = shouldCompile || isModified;
	    }
	    Collections.sort(dmlFiles, new Comparator<URL>() {
                    @Override
                    public int compare(URL o1, URL o2) {
                        return o1.toExternalForm().compareTo(o2.toExternalForm());
                    }
                });
	}

	try {
	    Project project = DmlMojoUtils.getProject(getMavenProject(), getDmlSourceDirectory(),
                                                      getGeneratedSourcesDirectory(), dmlFiles,
                                                      getLog(), verbose());

	    List<URL> allDmls = new ArrayList<URL>();
	    for (DmlFile dmlFile : project.getFullDmlSortedList()) {
		allDmls.add(dmlFile.getUrl());
	    }

	    project.generateProjectProperties(getOutputDirectoryPath());

	    if (allDmls.isEmpty()) {
		getLog().info("No dml files found to generate domain");
		return;
	    }

	    if (project.shouldCompile() || shouldCompile) {
                // Split all DML files in two sets: local and external.
                List<URL> localDmls = new ArrayList<URL>();
                for (DmlFile dmlFile : project.getDmls()) {
                    localDmls.add(dmlFile.getUrl());
                }
                List<URL> externalDmls = new ArrayList<URL>(allDmls);
                externalDmls.removeAll(localDmls);
            
		getSourcesDirectory().mkdirs();
		getSourcesDirectory().setLastModified(System.currentTimeMillis());
		if (verbose()) {
		    getLog().info("Using generator: " + getCodeGeneratorClass().getName());
		}
		Map<String,String> realParams = getParams() == null ? new HashMap<String,String>() : getParams();

		compArgs = new CompilerArgs(getSourcesDirectory(), getGeneratedSourcesDirectory(), getPackageName(),
                                            generateFinals(), getCodeGeneratorClass(), localDmls, externalDmls, realParams);

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

	public Class<? extends CodeGenerator> getCodeGeneratorClass()
			throws ClassNotFoundException {
		return (Class<? extends CodeGenerator>) Class
				.forName(getCodeGeneratorClassName());
	}
}
