package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Generate base main classes from the main DML files
 *
 * @goal generate-domain
 * @phase generate-sources
 * @configurator include-project-dependencies
 * @requiresDependencyResolution compile+runtime
 * @threadSafe
 */
public class DmlCodeGeneratorMojo extends AbstractDmlCodeGeneratorMojo {

    /**
     * Maven Project
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * File Source Directory
     *
     * @parameter default-value="${basedir}/src/main/dml"
     * @readonly
     * @required
     */
    private File dmlSourceDirectory;

    /**
     * File Destination Directory
     *
     * @parameter default-value="${basedir}/src/main/java"
     * @readonly
     * @required
     */
    private File sourcesDirectory;

    /**
     * Base File Destination Directory
     *
     * @parameter default-value="${project.build.directory}/generated-sources/dml-maven-plugin"
     * @readonly
     * @required
     */
    private File generatedSourcesDirectory;

    /**
     * Code Generator Class Name
     * @parameter expression="${generate-domain.codeGeneratorClassName}" default-value="pt.ist.fenixframework.dml.DefaultCodeGenerator"
     */
    private String codeGeneratorClassName;

    /**
     * Collection Class Name built on top of DML to be used in the code generation  
     * @parameter expression="${generate-domain.collectionClassName}"
     */
    private String collectionClassName = "";
    
    /**
     * Generate unsafe methods to access data in a transactional and consistent way, but 
     * not by taking those accesses into account when validating the transaction. It is 
     * left to the backend to implement this accordingly, which might not even make sense 
     * in a pessimistic concurrency control backend. 
     * This only guarantees that the methods are generated, but does not guarantee their 
     * semantics.
     * @parameter expression="${generate-domain.generateUnsafeAccesses}" default-value="false"
     */
    private boolean generateUnsafeAccesses;
    
    /**
     * Package name
     * @parameter expression="${generate-domain.packageName}"
     */
    private String packageName = "";

    /**
     * Generate Finals Flag
     * @parameter expression="${generate-domain.generateFinals}" default-value="false"
     */
    private boolean generateFinals;

    /**
     * Verbose Mode Flag
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    /**
     * Generate Project Properties Flag
     * @parameter expression="${generate-domain.generateProjectProperties}" default-value="false"
     */
    private boolean generateProjectProperties;

    /**
     * Generic Code Generator Class Parameters
     * @parameter
     */
    private Map<String,String> params;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();
        getMavenProject().addCompileSourceRoot(getGeneratedSourcesDirectory().getAbsolutePath());
    }

    @Override
    protected File getDmlSourceDirectory() {
        return dmlSourceDirectory;
    }

    @Override
    protected String getCodeGeneratorClassName() {
        return codeGeneratorClassName;
    }
    
    @Override
    protected String getCollectionClassName() {
	return collectionClassName;
    }

    @Override
    protected boolean generateUnsafeAccesses() {
	return generateUnsafeAccesses;
    }
    
    @Override
    protected File getGeneratedSourcesDirectory() {
        return generatedSourcesDirectory;
    }

    @Override
    protected File getSourcesDirectory() {
        return sourcesDirectory;
    }

    @Override
    protected String getPackageName() {
        return packageName;
    }

    @Override
    protected boolean verbose() {
        return verbose;
    }

    @Override
    protected boolean generateFinals() {
        return generateFinals;
    }

    @Override
    protected boolean generateProjectProperties() {
        return generateProjectProperties;
    }

    @Override
    protected MavenProject getMavenProject() {
        return mavenProject;
    }

    @Override
    protected String getOutputDirectoryPath() {
        return mavenProject.getBuild().getOutputDirectory();
    }
     
    @Override
    protected Map<String,String> getParams() {
	return params;
    }

    @Override
    protected List<String> getClasspathElements() {
        try {
            return getMavenProject().getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            getLog().error(e);
        }
        return null;
    }
}
