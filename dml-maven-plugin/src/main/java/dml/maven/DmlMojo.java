package dml.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;
import pt.ist.fenixframework.project.DmlFile;
import dml.CodeGenerator;
import dml.CompilerArgs;
import dml.DmlCompiler;
import dml.DomainModel;

/**
 * Generate base classes from the DML files
 * 
 * @goal generate-domain
 * 
 * @phase generate-sources
 * 
 * @requiresDependencyResolution runtime
 */
public class DmlMojo extends AbstractMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * File Source Directory
     * 
     * @parameter expression="${generate-domain.src}"
     *            default-value="${basedir}/src/main/dml"
     */
    private File srcDirectoryFile;

    /**
     * File Destination Directory
     * 
     * @parameter expression="${generate-domain.dest}"
     *            default-value="${basedir}/src/main/java"
     */
    private File destDirectoryFile;

    /**
     * Base File Destination Directory
     * 
     * @parameter expression="${generate-domain.destBase}" default-value=
     *            "${project.build.directory}/generated-sources/dml-maven-plugin"
     */
    private File destDirectoryBaseFile;

    /**
     * Domain Model Class Name
     * 
     * @parameter expression="${generate-domain.domainModelClassName}"
     *            default-value
     *            ="pt.ist.fenixframework.pstm.dml.FenixDomainModel"
     */
    private String domainModelClassName;

    /**
     * Code Generator Class Name
     * 
     * @parameter expression="${generate-domain.codeGeneratorClassName}"
     *            default-value=
     *            "pt.ist.fenixframework.pstm.dml.FenixCodeGeneratorOneBoxPerObject"
     */
    private String codeGeneratorClassName;

    /**
     * Package name
     * 
     * @parameter expression="${generate-domain.packageName}" default-value=""
     */
    private String packageName;

    /**
     * Generate Finals Flag
     * 
     * @parameter expression="${generate-domain.generateFinals}"
     *            default-value="false"
     */
    private boolean generateFinals;

    /**
     * Verbose Mode Flag
     * 
     * @parameter expression="${generate-domain.verbose}" default-value="false"
     */
    private boolean verbose;

    @Override
    public void execute() throws MojoExecutionException {
	if (mavenProject.getArtifact().getType().equals("pom")) {
	    getLog().info("Cannot generate domain for pom projects");
	    return;
	}

	DmlMojoUtils.augmentClassLoader(getLog(), mavenProject);

	CompilerArgs compArgs = null;
	long latestBuildTime = destDirectoryBaseFile.lastModified();

	boolean shouldCompile = false;
	List<URL> dmlFiles = new ArrayList<URL>();
	if (srcDirectoryFile.exists()) {
	    DirectoryScanner scanner = new DirectoryScanner();
	    scanner.setBasedir(this.srcDirectoryFile);

	    String[] includes = { "**\\*.dml" };
	    scanner.setIncludes(includes);
	    scanner.scan();

	    Resource resource = new Resource();
	    resource.setDirectory(this.srcDirectoryFile.getAbsolutePath());
	    resource.addInclude("*.dml");
	    mavenProject.addResource(resource);

	    for (String includedFile : scanner.getIncludedFiles()) {
		String filePath = this.srcDirectoryFile.getAbsolutePath() + "/" + includedFile;
		File file = new File(filePath);
		try {
		    dmlFiles.add(file.toURI().toURL());
		} catch (MalformedURLException e) {
		    getLog().error(e);
		}
		boolean isModified = file.lastModified() > latestBuildTime;
		if (this.verbose == false) {
		    getLog().info(includedFile + " : " + (isModified ? "not up to date" : "up to date"));
		}
		shouldCompile = shouldCompile || isModified;
	    }
	}

	try {
	    FenixFrameworkArtifact artifact = DmlMojoUtils.getArtifact(mavenProject, srcDirectoryFile, dmlFiles);

	    List<URL> dmls = new ArrayList<URL>();
	    for (DmlFile dmlFile : artifact.getFullDmlSortedList()) {
		dmls.add(dmlFile.getUrl());
	    }

	    artifact.generateProjectProperties(mavenProject.getBuild().getOutputDirectory());

	    if (dmls.isEmpty()) {
		getLog().info("No dml files found to generate domain");
		return;
	    }
	    shouldCompile = true;
	    if (this.packageName == null) {
		packageName = "";
	    }
	    if (shouldCompile) {
		destDirectoryBaseFile.setLastModified(System.currentTimeMillis());
		if (this.verbose) {
		    getLog().info("Using model: " + getDomainModelClass().getName());
		    getLog().info("Using generator: " + getCodeGeneratorClass().getName());
		}

		compArgs = new CompilerArgs(this.destDirectoryFile, this.destDirectoryBaseFile, this.packageName,
			this.generateFinals, getCodeGeneratorClass(), getDomainModelClass(), dmls);

		DomainModel model = DmlCompiler.getDomainModel(compArgs);

		CodeGenerator generator = compArgs.getCodeGenerator().getConstructor(CompilerArgs.class, DomainModel.class)
			.newInstance(compArgs, model);
		generator.generateCode();
		mavenProject.addCompileSourceRoot(destDirectoryBaseFile.getAbsolutePath());
	    } else {
		if (this.verbose) {
		    getLog().info("All dml files are up to date. Skipping generation...");
		}
	    }
	} catch (Exception e) {
	    getLog().error(e);
	}

    }

    public Class<? extends CodeGenerator> getCodeGeneratorClass() throws ClassNotFoundException {
	return (Class<? extends CodeGenerator>) Class.forName(this.codeGeneratorClassName);
    }

    public Class<? extends DomainModel> getDomainModelClass() throws ClassNotFoundException {
	return (Class<? extends DomainModel>) Class.forName(this.domainModelClassName);
    }
}
