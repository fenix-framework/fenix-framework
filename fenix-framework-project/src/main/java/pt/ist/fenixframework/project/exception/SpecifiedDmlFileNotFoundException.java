package pt.ist.fenixframework.project.exception;

public class SpecifiedDmlFileNotFoundException extends FenixFrameworkProjectException {

    public SpecifiedDmlFileNotFoundException(String dmlFileName) {
        super("The specified DML file was not found: " + dmlFileName);
    }
}
