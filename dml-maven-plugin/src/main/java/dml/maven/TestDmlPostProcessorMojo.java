package dml.maven;

import java.io.File;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which injects the constructors into the bytecode of the DML compiled
 * classes
 *
 * @goal test-post-compile
 * @phase process-test-classes
 * @requiresDependencyResolution test
 * @threadSafe
 */
public class TestDmlPostProcessorMojo extends AbstractDmlPostProcessorMojo {

    /**
     * File Source Directory
     *
     * @parameter default-value="${basedir}/src/test/dml"
     * @readonly
     * @required
     */
    private File dmlSourceDirectory;

    /**
     * Classes Directory
     *
     * @parameter default-value="${project.build.testOutputDirectory}"
     * @readonly
     * @required
     */
    private File classesDirectory;

    /**
     * Domain Model Class Name
     * @parameter expression="${generate-domain.domainModelClassName}"
     *            default-value="pt.ist.fenixframework.pstm.dml.FenixDomainModel"
     */
    private String domainModelClassName;

    /**
     * Code Generator Class Name
     * @parameter expression="${generate-domain.codeGeneratorClassName}"
     *            default-value="pt.ist.fenixframework.pstm.dml.FenixCodeGeneratorOneBoxPerObject"
     */
    private String codeGeneratorClassName;

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
    protected String getDomainModelClassName() {
        return domainModelClassName;
    }

    @Override
    protected boolean verbose() {
        return verbose;
    }
}
