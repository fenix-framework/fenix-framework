package pt.ist.fenixframework.maven;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.dml.maven.DmlZipCreatorMojo;

/**
 * Generate base main classes from the main DML files
 * 
 * @goal package-dmls
 * @phase generate-sources
 * @requiresDependencyResolution runtime
 */
public class FFDmlZipCreatorMojo extends DmlZipCreatorMojo {

    /**
     * Maven Project
     * 
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
     * Zip File Destination Directory
     * 
     * @parameter default-value="${project.build.directory}"
     * @readonly
     * @required
     */
    private File zipDestinationDirectory;

    /**
     * Verbose Mode Flag
     * 
     * @parameter property="verbose" default-value="false"
     */
    private boolean verbose;

    @Override
    public void execute() throws MojoExecutionException {
        super.mavenProject = this.mavenProject;
        super.dmlSourceDirectory = this.dmlSourceDirectory;
        super.zipDestinationDirectory = this.zipDestinationDirectory;
        super.verbose = this.verbose;
        super.execute();
    }

}
