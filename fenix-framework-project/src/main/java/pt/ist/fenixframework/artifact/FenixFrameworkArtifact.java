package pt.ist.fenixframework.artifact;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import org.apache.commons.lang.StringUtils;

import pt.ist.fenixframework.project.DmlFile;
import pt.ist.fenixframework.project.exception.FenixFrameworkProjectException;
import pt.ist.fenixframework.project.exception.NoArtifactNameSpecifiedException;

public class FenixFrameworkArtifact {
    private static final Map<String, FenixFrameworkArtifact> artifacts = new HashMap<String, FenixFrameworkArtifact>();

    private static final String NAME_KEY = "name";
    private static final String DML_FILES_KEY = "dml-files";
    private static final String DEPENDS_KEY = "depends";
    protected static final String SEPARATOR_CHAR = ",";

    private String name;
    private List<DmlFile> dmls;
    private List<FenixFrameworkArtifact> dependencies;
    private List<FenixFrameworkArtifact> depended = new ArrayList<FenixFrameworkArtifact>();

    public FenixFrameworkArtifact(String name, List<DmlFile> dmls, List<FenixFrameworkArtifact> dependencies)
	    throws FenixFrameworkProjectException {
	this.name = name;
	this.dmls = dmls;
	this.dependencies = dependencies;
	for (FenixFrameworkArtifact artifact : dependencies) {
	    artifact.depended.add(this);
	}
	validate();
	artifacts.put(name, this);
    }

    public String getName() {
	return name;
    }

    public List<DmlFile> getDmls() {
	return dmls;
    }

    public List<FenixFrameworkArtifact> getDependencyArtifacts() {
	return dependencies;
    }

    public List<DmlFile> getFullDmlSortedList() {
	List<DmlFile> dmlFiles = new ArrayList<DmlFile>();
	for (FenixFrameworkArtifact dependencyArtifact : getArtifacts()) {
	    dmlFiles.addAll(dependencyArtifact.getDmls());
	}
	return dmlFiles;
    }

    public List<FenixFrameworkArtifact> getArtifacts() {
	Map<FenixFrameworkArtifact, List<FenixFrameworkArtifact>> incoming = new HashMap<FenixFrameworkArtifact, List<FenixFrameworkArtifact>>();
	computeIncomingEdges(incoming, this);

	List<FenixFrameworkArtifact> artifacts = new ArrayList<FenixFrameworkArtifact>();
	Queue<FenixFrameworkArtifact> freeNodes = new LinkedList<FenixFrameworkArtifact>();
	freeNodes.add(this);

	while (!freeNodes.isEmpty()) {
	    FenixFrameworkArtifact artifact = freeNodes.poll();
	    artifacts.add(artifact);
	    for (FenixFrameworkArtifact dependency : artifact.getDependencyArtifacts()) {
		incoming.get(dependency).remove(artifact);
		if (incoming.get(dependency).isEmpty()) {
		    freeNodes.add(dependency);
		}
	    }
	}
	Collections.reverse(artifacts);
	return artifacts;
    }

    private static void computeIncomingEdges(Map<FenixFrameworkArtifact, List<FenixFrameworkArtifact>> incoming,
	    FenixFrameworkArtifact artifact) {
	incoming.put(artifact, new ArrayList<FenixFrameworkArtifact>(artifact.depended));
	for (FenixFrameworkArtifact dependency : artifact.getDependencyArtifacts()) {
	    computeIncomingEdges(incoming, dependency);
	}
    }

    public void validate() throws FenixFrameworkProjectException {
	if (StringUtils.isBlank(name))
	    throw new NoArtifactNameSpecifiedException();
    }

    @Override
    public String toString() {
	return name;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj instanceof FenixFrameworkArtifact) {
	    return name.equals(((FenixFrameworkArtifact) obj).name);
	}
	return false;
    }

    @Override
    public int hashCode() {
	return name.hashCode();
    }

    public void generateProjectProperties(String outputDirectory) throws IOException {
	Properties properties = new Properties();
	properties.setProperty(NAME_KEY, getName());
	properties.setProperty(DML_FILES_KEY, StringUtils.join(getDmls(), SEPARATOR_CHAR));
	if (dependencies.size() > 0) {
	    properties.setProperty(DEPENDS_KEY, StringUtils.join(getDependencyArtifacts(), SEPARATOR_CHAR));
	}
	File output = new File(outputDirectory + "/" + getName() + "/project.properties");
	output.getParentFile().mkdirs();
	properties.store(new FileWriter(output), null);
    }

    public static FenixFrameworkArtifact fromName(String artifactName) throws IOException, FenixFrameworkProjectException,
	    MalformedURLException {
	Properties properties = new Properties();
	properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(artifactName + "/project.properties"));
	return FenixFrameworkArtifact.fromProperties(properties);
    }

    public static FenixFrameworkArtifact fromProperties(Properties properties) throws MalformedURLException, IOException,
	    FenixFrameworkProjectException {
	String name = properties.getProperty(NAME_KEY);
	if (!artifacts.containsKey(name)) {
	    List<DmlFile> dependencyDmlFiles = DmlFile.parseDependencyDmlFiles(properties.getProperty(DML_FILES_KEY));
	    List<FenixFrameworkArtifact> dependencies = new ArrayList<FenixFrameworkArtifact>();
	    for (String artifactName : properties.getProperty(DEPENDS_KEY, "").trim().split("\\s*,\\s*")) {
		if (StringUtils.isNotEmpty(artifactName)) {
		    dependencies.add(FenixFrameworkArtifact.fromName(artifactName));
		}
	    }
	    new FenixFrameworkArtifact(name, dependencyDmlFiles, dependencies);
	}
	return artifacts.get(name);
    }
}
