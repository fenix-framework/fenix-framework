package pt.ist.fenixframework.project.exception;

public class NoDmlFileSpecifiedException extends FenixFrameworkProjectException {

    public NoDmlFileSpecifiedException(String projectName) {
	super("No DML files specified for project "+projectName);
    }
}
