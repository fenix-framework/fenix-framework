package pt.ist.fenixframework.core.exception;

public class SpecifiedDmlFileNotFoundException extends ProjectException {

    public SpecifiedDmlFileNotFoundException(String dmlFileName) {
	super("The specified DML file was not found: "+dmlFileName);
    }
}
