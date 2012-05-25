package dml.maven;

import dml.CodeGenerator;
import dml.CompilerArgs;
import dml.DmlCompiler;
import dml.DomainModel;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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
import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;
import pt.ist.fenixframework.project.DmlFile;
import pt.ist.fenixframework.project.FenixFrameworkProject;
import pt.ist.fenixframework.project.exception.FenixFrameworkProjectException;

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
   * @parameter default-value="${project}"
   */
  private MavenProject mavenProject;

  /**
   * File Source Directory
   * @parameter expression="${generate-domain.src}" default-value="${basedir}/src/main/dml"
   */
  private File srcDirectoryFile;
	
  /**
   * File Destination Directory
   * @parameter expression="${generate-domain.dest}" default-value="${basedir}/src/main/java"
   */
  private File destDirectoryFile;

  /**
   * Base File Destination Directory
   * @parameter expression="${generate-domain.destBase}" default-value="${project.build.directory}/generated-sources/dml-maven-plugin"
   */
  private File destDirectoryBaseFile;

  /**
   * Domain Model Class Name
   * @parameter expression="${generate-domain.domainModelClassName}" default-value="pt.ist.fenixframework.pstm.dml.FenixDomainModel"
   */
  private String domainModelClassName;

  /**
   * Code Generator Class Name
   * @parameter expression="${generate-domain.codeGeneratorClassName}" default-value="pt.ist.fenixframework.pstm.dml.FenixCodeGeneratorOneBoxPerObject"
   */
  private String codeGeneratorClassName;

  /**
   * Package name
   * @parameter expression="${generate-domain.packageName}" default-value=""
   */
  private String packageName;

  /**
   * Generate Finals Flag
   * @parameter expression="${generate-domain.generateFinals}" default-value="false"
   */
  private boolean generateFinals;

  /**
   * Verbose Mode Flag
   * @parameter expression="${generate-domain.verbose}" default-value="false"
   */
  private boolean verbose;
  
  /**
   * Generate Project Properties Flag
   * @parameter expression="${generate-domain.generateProjectProperties}" default-value="false"
   */
  private boolean generateProjectProperties;

  public void execute() throws MojoExecutionException {
		
    CompilerArgs compArgs = null;
    List<String> domainSpecFileNames = new ArrayList<String>();
    long latestBuildTime = destDirectoryBaseFile.lastModified();

    DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(this.srcDirectoryFile);
    
    String[] includes = {"**\\*.dml"};
    scanner.setIncludes(includes);
    scanner.scan();
    
    boolean shouldCompile = false;

    Resource resource = new Resource();
    resource.setDirectory(this.srcDirectoryFile.getAbsolutePath());
    resource.addInclude("**/*.dml");
    mavenProject.addResource(resource);
    
    List<String> externalDmlFileNames = DmlMojoUtils.readDmlFilePathsFromArtifact(getLog(), mavenProject.getArtifacts());
    domainSpecFileNames.addAll(externalDmlFileNames);
    
    List<String> projectDmlFileNames = new ArrayList<String>();
    List<String> classPathDmlFilesNames = new ArrayList<String>();

    String[] includedFiles = scanner.getIncludedFiles();
    for (String includedFile : includedFiles) {
      String filePath = this.srcDirectoryFile.getAbsolutePath() + "/" + includedFile;
      File file = new File(filePath);
      boolean isModified = file.lastModified() > latestBuildTime;
      if(this.verbose==false) {
        getLog().info(includedFile + " : " + (isModified ? "not up to date" : "up to date"));
      }
      classPathDmlFilesNames.add(StringUtils.difference(srcDirectoryFile.getAbsolutePath(), filePath));
      projectDmlFileNames.add(filePath);
      shouldCompile = shouldCompile || isModified;
    }
    
    domainSpecFileNames.addAll(projectDmlFileNames);

    if(generateProjectProperties) {
            try {
                List<String> dependencyArtifacts = DmlMojoUtils.getDependencyArtifacts(mavenProject, getLog());
                Properties properties = FenixFrameworkArtifact.generateProperties(mavenProject.getArtifactId(), classPathDmlFilesNames, dependencyArtifacts);
                ProjectPropertiesGenerator.create(new File(mavenProject.getBuild().getOutputDirectory()+"/"+mavenProject.getArtifactId()+"/project.properties"), properties);
            } catch (IOException ex) {
                Logger.getLogger(DmlMojo.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    shouldCompile=true;
    if(this.packageName==null) {
      packageName="";
    }
    if (shouldCompile) {
      try {
           destDirectoryBaseFile.setLastModified(System.currentTimeMillis());
           if(this.verbose) {
             getLog().info("Using model: " + getDomainModelClass().getName());
             getLog().info("Using generator: " + getCodeGeneratorClass().getName());
           }
           
           compArgs = new CompilerArgs(this.destDirectoryFile, this.destDirectoryBaseFile, this.packageName, this.generateFinals, getCodeGeneratorClass(), getDomainModelClass(), domainSpecFileNames);

           DomainModel model = DmlCompiler.getDomainModel(compArgs);

           CodeGenerator generator = compArgs.getCodeGenerator().getConstructor(CompilerArgs.class, DomainModel.class).newInstance(compArgs, model);
           generator.generateCode();
		   mavenProject.addCompileSourceRoot(destDirectoryBaseFile.getAbsolutePath());
         } catch (Exception e) {
             getLog().error(e);
         }
       } else {
         if(this.verbose) {
           getLog().info("All dml files are up to date. Skipping generation...");
         }
       }
     }

  public Class<? extends CodeGenerator> getCodeGeneratorClass() throws ClassNotFoundException {
    return (Class<? extends CodeGenerator>)Class.forName(this.codeGeneratorClassName);
  }

  public Class<? extends DomainModel> getDomainModelClass() throws ClassNotFoundException {
    return (Class<? extends DomainModel>) Class.forName(this.domainModelClassName);
  }

}
