package pt.ist.fenixframework.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pt.ist.fenixframework.DomainModelParser;
import pt.ist.fenixframework.core.exception.NoProjectNameSpecifiedException;
import pt.ist.fenixframework.core.exception.ProjectException;
import pt.ist.fenixframework.core.exception.ProjectPropertiesNotFoundException;
import pt.ist.fenixframework.dml.DomainModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectTest {
    private static Project project;
    private static Project projectOptional;
    private static Project projectForOptional;
    private static Project projectWithDependencies;
    private static Project projectWithOptionalDependencies;

    // Note: i know there is a resource to url function but i want to keep package's tests separated
    private URL getResource(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResource(resourceName);
    }

    @BeforeAll
    public static void beforeAll() throws ProjectException, IOException {
        project = Project.fromName("test", Thread.currentThread().getContextClassLoader());
        projectOptional =
                new Project("testOptional", "1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        projectForOptional =
                new Project("testForOptional", "1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        projectWithDependencies = new Project("testWithDependencies", "1", Collections.emptyList(),
                Collections.singletonList(project), Collections.emptyList());
        projectWithOptionalDependencies = new Project("testWithOptionalDependencies", "1", Collections.emptyList(),
                Collections.singletonList(projectForOptional), Collections.singletonList(projectOptional));
    }

    @Test
    public void deprecatedConstructor() throws ProjectException, IOException {
        Project p = new Project("test", Collections.emptyList(), Collections.emptyList(), false);
        assertEquals(p.getName(), "test");
    }

    @Test
    public void fromName() throws ProjectException, IOException {
        assertEquals(Project.fromName("test"), project);
    }

    @Test
    public void fromNameNotFound() throws ProjectException, IOException {
        assertThrows(ProjectPropertiesNotFoundException.class, () -> Project.fromName("test-not-directory-in-resources"));
    }

    @Test
    public void getName() throws ProjectException, IOException {
        assertEquals(project.getName(), "test");
    }

    @Test
    public void shouldCompile() throws ProjectException, IOException {
        assertTrue(project.shouldCompile());
    }

    @Test
    public void getVersion() throws ProjectException, IOException {
        assertEquals("1.0.0-SNAPSHOT", project.getVersion());
    }

    @Test
    public void getDmls() throws ProjectException, IOException {
        assertEquals(1, project.getDmls().size());
        assertEquals(new DmlFile(getResource("TestResource1.dml"), "teste"), project.getDmls().get(0));
    }

    @Test
    public void getDependencyProjects() throws ProjectException, IOException {
        assertEquals(Collections.emptyList(), project.getDependencyProjects());
    }

    @Test
    public void getOptionalDependencies() throws ProjectException, IOException {
        assertEquals(Collections.emptyList(), project.getOptionalDependencies());
    }

    @Test
    public void getFullDmlSortedList() throws ProjectException, IOException {
        assertEquals(1, project.getFullDmlSortedList().size());
        assertEquals(new DmlFile(getResource("TestResource1.dml"), "teste"), project.getFullDmlSortedList().get(0));
    }

    @Test
    public void getProjects() throws ProjectException, IOException {
        assertEquals(1, project.getProjects().size());
        assertEquals(project, project.getProjects().get(0));
    }

    @Test
    public void getDomainModel() throws ProjectException, IOException {
        URL url = getResource("TestResource1.dml");
        DomainModel model = DomainModelParser.getDomainModel(Collections.singletonList(url));
        assertNotNull(project.getDomainModel().findClass("pt.ist.fenixframework.test.TestEmpty"));
        assertNotNull(project.getDomainModel().findClass("pt.ist.fenixframework.test.Test1"));
        assertNotNull(project.getDomainModel().findClass("pt.ist.fenixframework.test.Test2"));
    }

    @Test
    public void equals() throws ProjectException {
        Project p1 = new Project("test1", "1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Project p2 = new Project("test2", "1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Project p3 = new Project("test1", "1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        assertFalse(p1.equals(p2));
        assertFalse(p1.equals(new Object()));
        assertTrue(p1.equals(p3));
    }

    @Test
    public void testToString() {
        assertEquals("test", project.toString());
    }

    @Test
    public void dependency() throws ProjectException, IOException {
        assertTrue(projectWithDependencies.getProjects().contains(project));
        assertTrue(projectWithDependencies.getProjects().contains(projectWithDependencies));
    }

    @Test
    public void generateProjectProperties() throws ProjectException, IOException {
        Path path = Files.createTempDirectory(Paths.get("/tmp"), "propertiesProjectTest");
        path.toFile().deleteOnExit();
        project.generateProjectProperties(path.toFile().getAbsolutePath());
        File generated = new File(path.toFile().getAbsolutePath() + "/test/project.properties");
        Properties prop = new Properties();
        prop.load(new FileInputStream(generated));
        assertEquals(project, Project.fromProperties(prop));
    }

    @Test
    public void generateProjectPropertiesWithDependencies() throws ProjectException, IOException {
        Path path = Files.createTempDirectory(Paths.get("/tmp"), "propertiesProjectWithDependenciesTest");
        path.toFile().deleteOnExit();
        projectWithDependencies.generateProjectProperties(path.toFile().getAbsolutePath());
        File generated = new File(path.toFile().getAbsolutePath() + "/testWithDependencies/project.properties");
        Properties prop = new Properties();
        prop.load(new FileInputStream(generated));
        assertEquals(projectWithDependencies, Project.fromProperties(prop));
    }

    @Test
    public void setSourceDmls() {
        ArrayList<DmlFile> listDmls = new ArrayList<>(project.getDmls());
        projectOptional.setSourceDmls(listDmls);
        assertEquals(1, projectOptional.getFullDmlSortedList().size());
    }

    @Test
    public void invalidNameWhiteSpaces() {
        assertThrows(NoProjectNameSpecifiedException.class,
                () -> new Project("   ", "1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
    }

    @Test
    public void invalidNameNull() {
        assertThrows(NoProjectNameSpecifiedException.class,
                () -> new Project(null, "1", Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
    }
}
