package dml.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;
import pt.ist.fenixframework.project.DmlFile;
import pt.ist.fenixframework.project.exception.FenixFrameworkProjectException;

public class DmlMojoUtils {
    public static FenixFrameworkArtifact getArtifact(MavenProject project, File srcDirectoryFile,
            File generatedSourcesDirectoryFile, List<URL> dmlFiles, Log log, boolean verbose) throws IOException,
            FenixFrameworkProjectException, MalformedURLException {
        List<FenixFrameworkArtifact> dependencies = new ArrayList<FenixFrameworkArtifact>();

        boolean shouldCompile = false;

        for (Artifact artifact : project.getDependencyArtifacts()) {
            String absolutePath = artifact.getFile().getAbsolutePath();
            JarFile jarFile = new JarFile(absolutePath);
            File file = new File(absolutePath);
            if (file.lastModified() > generatedSourcesDirectoryFile.lastModified()) {
                if (verbose) {
                    log.info("Dependency " + artifact.getArtifactId()
                            + " was last modified after this project generated sources.");
                }
                shouldCompile = true;
            }
            if (jarFile.getJarEntry(artifact.getArtifactId() + "/project.properties") != null) {
                dependencies.add(FenixFrameworkArtifact.fromName(artifact.getArtifactId()));
            }
            jarFile.close();
        }

        List<DmlFile> dmls = new ArrayList<DmlFile>();

        for (URL url : dmlFiles) {
            URL srcFolder = srcDirectoryFile.toURI().toURL();
            if (url.toExternalForm().contains(srcFolder.toExternalForm())) {
                dmls.add(new DmlFile(url, StringUtils.removeStart(url.toExternalForm(), srcFolder.toExternalForm())));
            } else {
                dmls.add(new DmlFile(url, null));
            }
        }
        return new FenixFrameworkArtifact(project.getArtifactId(), dmls, dependencies, shouldCompile);
    }

    public static URLClassLoader augmentClassLoader(Log log, MavenProject project) {
        List<String> classpathElements = null;
        try {
            classpathElements = project.getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            log.error(e);
        }

        URL[] classesURL = new URL[classpathElements.size()];
        int i = 0;

        for (String path : classpathElements) {
            try {
                classesURL[i++] = new File(path).toURI().toURL();
            } catch (MalformedURLException e) {
                log.error(e);
            }
        }

        URLClassLoader loader = new URLClassLoader(classesURL, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
        return loader;
    }

}
