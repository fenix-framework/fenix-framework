package pt.ist.fenixframework.artifact;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import pt.ist.fenixframework.project.DmlFile;
import pt.ist.fenixframework.project.exception.FenixFrameworkProjectException;
import pt.ist.fenixframework.project.exception.NoArtifactNameSpecifiedException;
import pt.ist.fenixframework.project.exception.NoDmlFileSpecifiedException;

public class FenixFrameworkArtifact {

    private static final String NAME_KEY = "name";
    private static final String DML_FILES_KEY = "dml-files";
    private static final String DEPENDS_KEY = "depends";
    protected static final String SEPARATOR_CHAR = ",";

    public static Properties generateProperties(String artifactName, List<String> projectDmlFileNames, List<String> dependencyArtifacts) {
        Properties properties = new Properties();
        properties.setProperty(NAME_KEY, artifactName);
        properties.setProperty(DML_FILES_KEY, StringUtils.join(projectDmlFileNames, SEPARATOR_CHAR));
        if(dependencyArtifacts.size() > 0) {
            properties.setProperty(DEPENDS_KEY, StringUtils.join(dependencyArtifacts, SEPARATOR_CHAR));
        }
        return properties;
    }

    private String name;
    private LinkedHashSet<DmlFile> dependencyDmlFiles;
    private LinkedHashSet<FenixFrameworkArtifact> dependencyArtifacts;

    public FenixFrameworkArtifact withName(String name) {
	this.name = name;
	return this;
    }

    public FenixFrameworkArtifact withDependencyDmlFiles(LinkedHashSet<DmlFile> dependencyDmlFiles) {
	this.dependencyDmlFiles = dependencyDmlFiles;
	return this;
    }

    public FenixFrameworkArtifact withDependencyArtifacts(LinkedHashSet<FenixFrameworkArtifact> dependencyArtifacts) {
	this.dependencyArtifacts = dependencyArtifacts;
	return this;
    }

    public String getName() {
	return name;
    }

    public LinkedHashSet<DmlFile> getDmlFiles() {
	LinkedHashSet<DmlFile> dmlFiles = new LinkedHashSet<DmlFile>();
	for(FenixFrameworkArtifact dependencyArtifact : dependencyArtifacts) {
	    dmlFiles.addAll(dependencyArtifact.getDmlFiles());
	}
	dmlFiles.addAll(dependencyDmlFiles);
	return dmlFiles;
    }

    public LinkedHashSet<DmlFile> getDependencyDmlFiles() {
	return dependencyDmlFiles;
    }

    public LinkedHashSet<FenixFrameworkArtifact> getArtifacts() {
	LinkedHashSet<FenixFrameworkArtifact> artifacts = new LinkedHashSet<FenixFrameworkArtifact>();
	for(FenixFrameworkArtifact dependencyArtifact : dependencyArtifacts) {
	    artifacts.addAll(dependencyArtifact.getArtifacts());
	    artifacts.add(dependencyArtifact);
	}
	return artifacts;
    }

    public LinkedHashSet<FenixFrameworkArtifact> getDependencyArtifacts() {
	return dependencyArtifacts;
    }

    public static LinkedHashSet<FenixFrameworkArtifact> parseDependencyArtifacts(String dependencyArtifactNamesField) throws IOException, FenixFrameworkProjectException, MalformedURLException {
	LinkedHashSet<FenixFrameworkArtifact> dependencyArtifactList = new LinkedHashSet<FenixFrameworkArtifact>();
	if(!StringUtils.isBlank(dependencyArtifactNamesField)) {
	    if(dependencyArtifactNamesField.contains(SEPARATOR_CHAR)) {
		for(String artifactName : dependencyArtifactNamesField.split(SEPARATOR_CHAR)) {
		    dependencyArtifactList.add(FenixFrameworkArtifact.fromName(artifactName));
		}
	    } else {
		dependencyArtifactList.add(FenixFrameworkArtifact.fromName(dependencyArtifactNamesField));
	    }
	}
	return dependencyArtifactList;
    }

    public static FenixFrameworkArtifact fromName(String artifactName) throws IOException, FenixFrameworkProjectException, MalformedURLException {
	Properties properties = new Properties();
	properties.load(FenixFrameworkArtifact.class.getResourceAsStream("/"+artifactName+"/project.properties"));
	return FenixFrameworkArtifact.fromProperties(properties);
    }

    public static FenixFrameworkArtifact fromProperties(Properties properties) throws FenixFrameworkProjectException, IOException, MalformedURLException {
	LinkedHashSet<DmlFile> dependencyDmlFiles = DmlFile.parseDependencyDmlFiles(properties.getProperty(DML_FILES_KEY));
	LinkedHashSet<FenixFrameworkArtifact> dependencyArtifacts = FenixFrameworkArtifact.parseDependencyArtifacts(properties.getProperty(DEPENDS_KEY));
	FenixFrameworkArtifact artifact = new FenixFrameworkArtifact()
		.withName(properties.getProperty(NAME_KEY))
		.withDependencyDmlFiles(dependencyDmlFiles)
		.withDependencyArtifacts(dependencyArtifacts);
	artifact.validate();
	return artifact;
    }

    public void writeTo(File file) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(NAME_KEY, name);
        properties.setProperty(DML_FILES_KEY, StringUtils.join(dependencyDmlFiles,SEPARATOR_CHAR));
        properties.setProperty(DEPENDS_KEY, StringUtils.join(dependencyArtifacts, SEPARATOR_CHAR));
        properties.store(new FileWriter(file), "project.properties");
    }
    
    public void validate() throws FenixFrameworkProjectException {
	if(StringUtils.isBlank(name))
	    throw new NoArtifactNameSpecifiedException();
	if(dependencyDmlFiles.isEmpty())
	    throw new NoDmlFileSpecifiedException(name);
    }
    
    @Override
    public String toString() {
        return name;
    }
}