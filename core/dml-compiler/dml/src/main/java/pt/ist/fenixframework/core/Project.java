package pt.ist.fenixframework.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;

import pt.ist.fenixframework.DomainModelParser;
import pt.ist.fenixframework.core.exception.NoProjectNameSpecifiedException;
import pt.ist.fenixframework.core.exception.ProjectException;
import pt.ist.fenixframework.core.exception.ProjectPropertiesNotFoundException;
import pt.ist.fenixframework.dml.DomainModel;

public class Project {
    private static final Map<String, Project> projects = new HashMap<String, Project>();

    private static final String NAME_KEY = "name";
    private static final String DML_FILES_KEY = "dml-files";
    private static final String DEPENDS_KEY = "depends";
    private static final String VERSION_KEY = "version";

    public static final String VERSION_UNKNOWN = "_VERSION_UNKNOWN_";
    protected static final char SEPARATOR_CHAR = ',';

    private final String name;
    private final String version;
    private final List<DmlFile> dmls;
    private final List<Project> dependencies;
    private final List<Project> optionalDependencies;
    private final List<Project> depended = new ArrayList<Project>();
    private ArrayList<DmlFile> sourceDmls = new ArrayList<>();;

    /**
     * @deprecated Use constructor with version
     */
    @Deprecated
    public Project(String name, List<DmlFile> dmls, List<Project> dependencies, boolean shouldCompile) throws ProjectException {
        this(name, VERSION_UNKNOWN, dmls, dependencies, shouldCompile);
    }

    /**
     * @deprecated Use constructor with optionalDependencies and without shouldCompile
     */
    @Deprecated
    public Project(String name, String version, List<DmlFile> dmls, List<Project> dependencies, boolean shouldCompile)
            throws ProjectException {
        this(name, version, dmls, dependencies, Collections.<Project> emptyList());
    }

    public Project(String name, String version, List<DmlFile> dmls, List<Project> dependencies, List<Project> optionalDependencies)
            throws ProjectException {
        this.name = name;
        this.version = version;
        this.dmls = dmls;
        this.dependencies = dependencies;
        this.optionalDependencies = optionalDependencies;
        for (Project project : dependencies) {
            project.depended.add(this);
        }
        validate();
        projects.put(name, this);
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public List<DmlFile> getDmls() {
        return dmls;
    }

    /**
     * @deprecated Without replacement. For compatibility reasons, always returns true.
     */
    @Deprecated
    public boolean shouldCompile() {
        return true;
    }

    public List<Project> getDependencyProjects() {
        return dependencies;
    }

    public List<Project> getOptionalDependencies() {
        return optionalDependencies;
    }

    public List<DmlFile> getFullDmlSortedList() {
        List<DmlFile> dmlFiles = new ArrayList<DmlFile>();
        for (Project dependencyProject : getProjects()) {
            dmlFiles.addAll(dependencyProject.getDmls());
        }
        dmlFiles.addAll(sourceDmls);
        return dmlFiles;
    }

    public List<Project> getProjects() {
        Map<Project, List<Project>> incoming = new HashMap<Project, List<Project>>();
        computeIncomingEdges(incoming, this);

        List<Project> projects = new ArrayList<Project>();
        Queue<Project> freeNodes = new LinkedList<Project>();
        freeNodes.add(this);

        while (!freeNodes.isEmpty()) {
            Project project = freeNodes.poll();
            projects.add(project);
            for (Project dependency : project.getDependencyProjects()) {
                incoming.get(dependency).remove(project);
                if (incoming.get(dependency).isEmpty()) {
                    freeNodes.add(dependency);
                }
            }
        }
        Collections.reverse(projects);
        Project.projects.clear();
        return projects;
    }

    private static void computeIncomingEdges(Map<Project, List<Project>> incoming, Project project) {
        incoming.put(project, new ArrayList<Project>(project.depended));
        for (Project dependency : project.getDependencyProjects()) {
            computeIncomingEdges(incoming, dependency);
        }
    }

    public void validate() throws ProjectException {
        if (name == null || name.trim().isEmpty()) {
            throw new NoProjectNameSpecifiedException();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            return name.equals(((Project) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public DomainModel getDomainModel() {
        List<URL> urls = new ArrayList<URL>();
        for (DmlFile file : getFullDmlSortedList()) {
            urls.add(file.getUrl());
        }
        return DomainModelParser.getDomainModel(urls);
    }

    public void generateProjectProperties(String outputDirectory) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(NAME_KEY, getName());
        properties.setProperty(VERSION_KEY, getVersion());
        ArrayList<DmlFile> dmls = new ArrayList<>();
        dmls.addAll(getDmls());
        dmls.addAll(sourceDmls);
        properties.setProperty(DML_FILES_KEY, join(dmls, Collections.emptyList()));
        if (dependencies.size() > 0) {
            properties.setProperty(DEPENDS_KEY, join(getDependencyProjects(), getOptionalDependencies()));
        }
        File output = new File(outputDirectory + "/" + getName() + "/project.properties");
        output.getParentFile().mkdirs();
        properties.store(new FileWriter(output), null);
    }

    private final String join(Collection<?> collection, Collection<?> toExclude) {
        StringBuilder builder = new StringBuilder();
        for (Object item : collection) {
            if (toExclude.contains(item)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(SEPARATOR_CHAR);
            }
            builder.append(item);
        }
        return builder.toString();
    }

    public static Project fromName(String projectName) throws IOException, ProjectException, MalformedURLException {
        return fromName(projectName, Thread.currentThread().getContextClassLoader());
    }

    public static Project fromName(String projectName, ClassLoader loader) throws IOException, ProjectException,
            MalformedURLException {
        Properties properties = new Properties();
        InputStream is = loader.getResourceAsStream(projectName + "/project.properties");
        if (is == null) {
            throw new ProjectPropertiesNotFoundException(projectName);
        }
        properties.load(is);
        try {
            is.close();
        } catch (Throwable ignore) {
        }
        return Project.fromProperties(properties, loader);
    }

    public static Project fromProperties(Properties properties) throws MalformedURLException, IOException, ProjectException {
        return fromProperties(properties, Thread.currentThread().getContextClassLoader());
    }

    public static Project fromProperties(Properties properties, ClassLoader loader) throws MalformedURLException, IOException,
            ProjectException {
        String name = properties.getProperty(NAME_KEY);
        String version = properties.getProperty(VERSION_KEY, VERSION_UNKNOWN);
        if (!projects.containsKey(name)) {
            List<DmlFile> dependencyDmlFiles = DmlFile.parseDependencyDmlFiles(properties.getProperty(DML_FILES_KEY), loader);
            List<Project> dependencies = new ArrayList<Project>();
            for (String projectName : properties.getProperty(DEPENDS_KEY, "").trim().split("\\s*,\\s*")) {
                if (projectName != null && !projectName.isEmpty()) {
                    dependencies.add(Project.fromName(projectName, loader));
                }
            }
            new Project(name, version, dependencyDmlFiles, dependencies, false);
        }
        return projects.get(name);
    }

    public void setSourceDmls(ArrayList<DmlFile> sourceDmls) {
        this.sourceDmls = sourceDmls;
    }
}
