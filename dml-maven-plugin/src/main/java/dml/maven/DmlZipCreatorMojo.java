package dml.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;

import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;
import pt.ist.fenixframework.project.DmlFile;

/**
 * Generate base main classes from the main DML files
 * 
 * @goal package-dmls
 * @phase generate-sources
 * @requiresDependencyResolution runtime
 */
public class DmlZipCreatorMojo extends AbstractMojo {

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
     * @parameter expression="${verbose}" default-value="false"
     */
    private boolean verbose;

    @Override
    public void execute() throws MojoExecutionException {
        if (mavenProject.getArtifact().getType().equals("pom")) {
            getLog().info("Cannot compute dml files for pom projects");
            return;
        }

        DmlMojoUtils.augmentClassLoader(getLog(), mavenProject);

        List<URL> dmlFiles = new ArrayList<URL>();
        if (dmlSourceDirectory.exists()) {
            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(dmlSourceDirectory);

            String[] includes = { "**\\*.dml" };
            scanner.setIncludes(includes);
            scanner.scan();

            Resource resource = new Resource();
            resource.setDirectory(dmlSourceDirectory.getAbsolutePath());
            resource.addInclude("*.dml");
            mavenProject.addResource(resource);

            for (String includedFile : scanner.getIncludedFiles()) {
                String filePath = dmlSourceDirectory.getAbsolutePath() + "/" + includedFile;
                File file = new File(filePath);
                try {
                    dmlFiles.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    getLog().error(e);
                }
            }
            Collections.sort(dmlFiles, new Comparator<URL>() {
                @Override
                public int compare(URL o1, URL o2) {
                    return o1.toExternalForm().compareTo(o2.toExternalForm());
                }
            });
        }

        try {
            FenixFrameworkArtifact artifact =
                    DmlMojoUtils.getArtifact(mavenProject, dmlSourceDirectory, zipDestinationDirectory, dmlFiles, getLog(),
                            verbose);

            List<URL> dmls = new ArrayList<URL>();
            for (DmlFile dmlFile : artifact.getFullDmlSortedList()) {
                dmls.add(dmlFile.getUrl());
            }

            if (dmls.isEmpty()) {
                getLog().info("No dml files found");
                return;
            } else {
                File zipFile = new File(String.format("%s/%s_dmls.zip", zipDestinationDirectory, mavenProject.getArtifactId()));
                zipFile.getParentFile().mkdirs();
                ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
                Integer i = 1;
                for (URL dmlURL : dmls) {
                    String name = String.format("/dmls/domain_model_%02d.dml", i++);
                    ZipEntry e = new ZipEntry(name);
                    out.putNextEntry(e);
                    File f = new File(zipDestinationDirectory + name);
                    FileUtils.copyURLToFile(dmlURL, f);
                    out.write(FileUtils.readFileToByteArray(f));
                    out.closeEntry();
                }
                out.close();
            }

        } catch (Exception e) {
            getLog().error(e);
        }
    }
}
