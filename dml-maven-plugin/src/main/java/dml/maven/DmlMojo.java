package dml.maven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import dml.CodeGenerator;
import dml.CompilerArgs;
import dml.DmlCompiler;
import dml.DomainModel;

import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

/**
 * Goal which compiles the DML files
 *
 * @goal generate-domain
 * 
 * @phase process-sources
 *
 * @requiresDependencyResolution runtime
 */
public class DmlMojo extends AbstractMojo {
	
	/**
	 * File Source Directory
	 * @parameter expression="${generate-domain.src}" default-value="src/main/dml"
	 */
	private File srcDirectoryFile;
	
  /**
   * File Destination Directory
   * @parameter expression="${generate-domain.dest}" default-value="src/main/java"
   */
  private File destDirectoryFile;

  /**
   * Base File Destination Directory
   * @parameter expression="${generate-domain.destBase}" default-value="${project.build.directory}/generated-sources"
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
  private Boolean generateFinals;

  /**
   * Verbose Mode Flag
   * @parameter expression="${generate-domain.verbose}" default-value="false"
   */
  private Boolean verbose;

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

    String[] includedFiles = scanner.getIncludedFiles();
    for (String includedFile : includedFiles) {
      String filePath = this.srcDirectoryFile.getAbsolutePath() + "/" + includedFile;
      File file = new File(filePath);
      boolean isModified = file.lastModified() > latestBuildTime;
      if(this.verbose==false) {
        System.out.println(includedFile + " : " + (isModified ? "not up to date" : "up to date"));
      }
      domainSpecFileNames.add(filePath);
      shouldCompile = shouldCompile || isModified;
    }

    shouldCompile=true;
    if(this.packageName==null) {
      packageName="";
    }
    if (shouldCompile) {
      try {
           destDirectoryBaseFile.setLastModified(System.currentTimeMillis());
           if(this.verbose) {
             System.out.println("Using model: " + getDomainModelClass().getName());
             System.out.println("Using generator: " + getCodeGeneratorClass().getName());
           }
           
           compArgs = new CompilerArgs(this.destDirectoryFile, this.destDirectoryBaseFile, this.packageName, this.generateFinals, getCodeGeneratorClass(), getDomainModelClass(), domainSpecFileNames);

           DomainModel model = DmlCompiler.getDomainModel(compArgs);

           CodeGenerator generator = compArgs.getCodeGenerator().getConstructor(CompilerArgs.class, DomainModel.class).newInstance(compArgs, model);
           generator.generateCode();
         } catch (Exception e) {
           e.printStackTrace();
           throw new MojoExecutionException(e,"Erro", "erro");
         }
       } else {
         if(this.verbose) {
           System.out.println("All dml files are up to date. Skipping generation...");
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
