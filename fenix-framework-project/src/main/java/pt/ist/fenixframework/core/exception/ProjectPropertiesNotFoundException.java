package pt.ist.fenixframework.core.exception;

public class ProjectPropertiesNotFoundException extends ProjectException {

    public ProjectPropertiesNotFoundException(String projectName) {
	super(projectName + "/project.properties" + " not found.");
    }
}
