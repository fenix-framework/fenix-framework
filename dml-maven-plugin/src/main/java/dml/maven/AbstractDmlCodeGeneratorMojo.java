package dml.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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

public abstract class AbstractDmlCodeGeneratorMojo extends AbstractMojo {

    protected abstract MavenProject getMavenProject();

    protected abstract String getCodeGeneratorClassName();

    protected abstract String getDomainModelClassName();

    protected abstract File getDmlSourceDirectory();

    protected abstract File getGeneratedSourcesDirectory();

    protected abstract File getSourcesDirectory();

    protected abstract String getOutputDirectoryPath();

    protected abstract String getPackageName();

    protected abstract boolean verbose();

    protected abstract boolean generateFinals();

    protected abstract boolean generateProjectProperties();

    @Override
    public void execute() throws MojoExecutionException {
        if (getMavenProject().getArtifact().getType().equals("pom")) {
            getLog().info("Cannot generate domain for pom projects");
            return;
        }

        URLClassLoader loader = DmlMojoUtils.augmentClassLoader(getLog(), getMavenProject());

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
            FenixFrameworkArtifact artifact =
                    DmlMojoUtils.getArtifact(getMavenProject(), getDmlSourceDirectory(), getGeneratedSourcesDirectory(),
                            dmlFiles, getLog(), verbose());

            List<URL> dmls = new ArrayList<URL>();
            for (DmlFile dmlFile : artifact.getFullDmlSortedList()) {
                dmls.add(dmlFile.getUrl());
            }

            String checksumPath = getGeneratedSourcesDirectory().getAbsolutePath() + ".checksum";
            final File checksumFile = new File(checksumPath);

            String dmlContent = new String();

            boolean checksumShouldCompile = true;

            for (URL dmlUrl : dmls) {
                dmlContent.concat(IOUtils.toString(dmlUrl.openStream()));
            }
            final String dmlMd5 = DigestUtils.md5Hex(dmlContent);

            if (!checksumFile.exists()) {
                FileUtils.writeStringToFile(checksumFile, dmlMd5);
            } else {
                final String prevDmlMd5 = FileUtils.readFileToString(checksumFile);
                checksumShouldCompile = !prevDmlMd5.equals(dmlMd5);
            }

            artifact.generateProjectProperties(getOutputDirectoryPath());

            if (dmls.isEmpty()) {
                getLog().info("No dml files found to generate domain");
                return;
            }

            //if (artifact.shouldCompile() || shouldCompile) {
            if (checksumShouldCompile) {
                getSourcesDirectory().mkdirs();
                getGeneratedSourcesDirectory().setLastModified(System.currentTimeMillis());
                if (verbose()) {
                    getLog().info("Using model: " + getDomainModelClass(loader).getName());
                    getLog().info("Using generator: " + getCodeGeneratorClass(loader).getName());
                }

                compArgs =
                        new CompilerArgs(getSourcesDirectory(), getGeneratedSourcesDirectory(), getPackageName(),
                                generateFinals(), getCodeGeneratorClass(loader), getDomainModelClass(loader), dmls);

                DomainModel model = DmlCompiler.getDomainModel(compArgs);
                CodeGenerator generator =
                        compArgs.getCodeGenerator().getConstructor(CompilerArgs.class, DomainModel.class)
                                .newInstance(compArgs, model);
                generator.generateCode();
            } else {
                getLog().info("All dml files are up to date. Skipping generation...");
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }

    public Class<? extends CodeGenerator> getCodeGeneratorClass(URLClassLoader loader) throws ClassNotFoundException {
        return (Class<? extends CodeGenerator>) Class.forName(getCodeGeneratorClassName(), true, loader);
    }

    public Class<? extends DomainModel> getDomainModelClass(URLClassLoader loader) throws ClassNotFoundException {
        return (Class<? extends DomainModel>) Class.forName(getDomainModelClassName(), true, loader);
    }
}
