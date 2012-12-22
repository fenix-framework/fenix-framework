package pt.ist.fenixframework.dml.maven;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which injects the constructors into the bytecode of the DML compiled
 * classes
 *
 * @goal post-compile
 * @phase process-classes
 * @requiresDependencyResolution runtime
 * @threadSafe
 */
public class DmlPostProcessorMojo extends AbstractDmlPostProcessorMojo {

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
     * Classes Directory
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @readonly
     * @required
     */
    private File classesDirectory;

    /**
     * Code Generator Class Name
     * @parameter expression="${generate-domain.codeGeneratorClassName}"
     *            default-value="pt.ist.fenixframework.dml.DefaultCodeGenerator"
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
     * Verbose Mode Flag
     * @parameter expression="${generate-domain.verbose}"
     *            default-value="false"
     */
    private boolean verbose;

    @Override
    public void execute() throws MojoExecutionException {
        super.execute();
    }

    @Override
    protected File getDmlSourceDirectory() {
        return dmlSourceDirectory;
    }

    @Override
    protected File getClassesDirectory() {
        return classesDirectory;
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
    protected boolean verbose() {
        return verbose;
    }

    @Override
    protected MavenProject getMavenProject() {
        return mavenProject;
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
