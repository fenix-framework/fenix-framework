package dml.maven;

import dml.CodeGenerator;
import dml.CompilerArgs;
import dml.DmlCompiler;
import dml.DomainModel;
import java.io.File;
import java.io.IOException;
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
import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;

public abstract class AbstractDmlCodeGeneratorMojo extends AbstractMojo {

    protected abstract MavenProject getMavenProject();
    protected abstract String getCodeGeneratorClassName();
    protected abstract String getDomainModelClassName();
    protected abstract File getDmlSourceDirectory();
    protected abstract File getGeneratedSourcesDirectory();
    protected abstract File getSourcesDirectory();

    protected abstract String getPackageName();
    protected abstract boolean verbose();
    protected abstract boolean generateFinals();
    protected abstract boolean generateProjectProperties();

    public void execute() throws MojoExecutionException {

        if (getMavenProject().getArtifact().getType().equals("pom")) {
            getLog().info("Cannot generate domain for pom projects");
            return;
        }

        CompilerArgs compArgs = null;
        List<String> domainSpecFileNames = new ArrayList<String>();
        long latestBuildTime = getGeneratedSourcesDirectory().lastModified();

        List<String> dmlFileList = DmlMojoUtils.readDmlFilePathsFromArtifact(getLog(), getMavenProject().getArtifacts());
        domainSpecFileNames.addAll(dmlFileList);
        boolean shouldCompile = false;

        List<String> classPathDmlFilesNames = new ArrayList<String>();

        if (getDmlSourceDirectory() != null && getDmlSourceDirectory().exists()) {


            DirectoryScanner scanner = new DirectoryScanner();
            scanner.setBasedir(getDmlSourceDirectory());

            String[] includes = {"**\\*.dml"};
            scanner.setIncludes(includes);
            scanner.scan();

            Resource resource = new Resource();
            resource.setDirectory(getDmlSourceDirectory().getAbsolutePath());
            resource.addInclude("*.dml");
            getMavenProject().addResource(resource);

            String[] includedFiles = scanner.getIncludedFiles();
            for (String includedFile : includedFiles) {
                String filePath = getDmlSourceDirectory().getAbsolutePath() + "/" + includedFile;
                File file = new File(filePath);
                boolean isModified = file.lastModified() > latestBuildTime;
                if (verbose() == false) {
                    getLog().info(includedFile + " : " + (isModified ? "not up to date" : "up to date"));
                }
                classPathDmlFilesNames.add(StringUtils.difference(getDmlSourceDirectory().getAbsolutePath(), filePath));
                domainSpecFileNames.add(filePath);
                shouldCompile = shouldCompile || isModified;
            }
        }

        if(generateProjectProperties()) {
            try {
                	List<String> dependencyArtifacts = DmlMojoUtils.getDependencyArtifacts(getMavenProject(), getLog());
                	Properties properties = FenixFrameworkArtifact.generateProperties(getMavenProject().getArtifactId(), classPathDmlFilesNames, dependencyArtifacts);
                	ProjectPropertiesGenerator.create(new File(getMavenProject().getBuild().getOutputDirectory()+"/"+getMavenProject().getArtifactId()+"/project.properties"), properties);
            	} catch (IOException ex) {
                	Logger.getLogger(AbstractDmlCodeGeneratorMojo.class.getName()).log(Level.SEVERE, null, ex);
            	}
    	}

        if(domainSpecFileNames.isEmpty()) {
           getLog().info("No dml files found to generate domain");
           return;
        }
        shouldCompile = true;

        if (shouldCompile) {
            try {
                getGeneratedSourcesDirectory().setLastModified(System.currentTimeMillis());
                if (verbose()) {
                    getLog().info("Dml Source Directory: " + getDmlSourceDirectory().getAbsolutePath());
                    getLog().info("Generated Sources Directory: " + getGeneratedSourcesDirectory().getAbsolutePath());
                    getLog().info("Sources Directory: "+getSourcesDirectory().getAbsolutePath());
                    getLog().info("Package Name: " + getPackageName());
                    getLog().info("Generate Project Properties: "+ generateProjectProperties());
                    getLog().info("Generate Finals: " + generateFinals());
                    getLog().info("Using Model: " + getDomainModelClass().getName());
                    getLog().info("Using Generator: " + getCodeGeneratorClass().getName());
                }

                compArgs = new CompilerArgs(getSourcesDirectory(), getGeneratedSourcesDirectory(), getPackageName(), generateFinals(), getCodeGeneratorClass(), getDomainModelClass(), domainSpecFileNames);

                DomainModel model = DmlCompiler.getDomainModel(compArgs);

                CodeGenerator generator = compArgs.getCodeGenerator().getConstructor(CompilerArgs.class, DomainModel.class).newInstance(compArgs, model);
                generator.generateCode();
            } catch (Exception e) {
                getLog().error(e);
            }
        } else {
            if (verbose()) {
                getLog().info("All dml files are up to date. Skipping generation...");
            }
        }
    }

    public Class<? extends CodeGenerator> getCodeGeneratorClass() throws ClassNotFoundException {
        return (Class<? extends CodeGenerator>) Class.forName(getCodeGeneratorClassName());
    }

    public Class<? extends DomainModel> getDomainModelClass() throws ClassNotFoundException {
        return (Class<? extends DomainModel>) Class.forName(getDomainModelClassName());
    }
}
