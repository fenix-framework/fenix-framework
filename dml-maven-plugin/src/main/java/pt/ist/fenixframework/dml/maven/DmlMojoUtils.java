package pt.ist.fenixframework.dml.maven;

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
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import pt.ist.fenixframework.core.DmlFile;
import pt.ist.fenixframework.core.Project;
import pt.ist.fenixframework.core.exception.ProjectException;

public class DmlMojoUtils {

    public static Project getProject(MavenProject project, File srcDirectoryFile,
                                     File generatedSourcesDirectoryFile, List<URL> dmlFiles,
                                     Log log, boolean verbose)
	    throws IOException, ProjectException, MalformedURLException {
	List<Project> dependencies = new ArrayList<Project>();

        boolean shouldCompile = false;

	for (Artifact artifact : project.getDependencyArtifacts()) {
            if (artifact.getFile() == null) continue;
	    String absolutePath = artifact.getFile().getAbsolutePath();
	    JarFile jarFile = new JarFile(absolutePath);


            // check the need to compile
            File file = new File(absolutePath);
            if (file.lastModified() > generatedSourcesDirectoryFile.lastModified()) {
                if (verbose) {
                    log.info("Dependency "
                             + artifact.getArtifactId()
                             + " was last modified after this project generated sources.");
                }
                shouldCompile = true;
            }

	    if (jarFile.getJarEntry(artifact.getArtifactId() + "/project.properties") != null) {
		dependencies.add(Project.fromName(artifact.getArtifactId()));
	    }
            jarFile.close();
	}

		List<DmlFile> dmls = new ArrayList<DmlFile>();
		for (URL url : dmlFiles) {
			URL srcFolder = srcDirectoryFile.toURI().toURL();
			if (url.toExternalForm().contains(srcFolder.toExternalForm())) {
				dmls.add(new DmlFile(url, StringUtils.removeStart(
						url.toExternalForm(), srcFolder.toExternalForm())));
			} else {
				dmls.add(new DmlFile(url, null));
			}
		}
		return new Project(project.getArtifactId(), dmls,
				dependencies, shouldCompile);
	}

	public static URLClassLoader augmentClassLoader(Log log, List<String> classpathElements) {
		URL[] classesURL = new URL[classpathElements.size()];
		int i = 0;

		for (String path : classpathElements) {
			try {
				classesURL[i++] = new File(path).toURI().toURL();
			} catch (MalformedURLException e) {
				log.error(e);
			}
		}

		URLClassLoader loader = new URLClassLoader(classesURL, Thread
				.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(loader);
		return loader;
	}

}
