package pt.ist.fenixframework.core.exception;

public class NoProjectNameSpecifiedException extends ProjectException {

    public NoProjectNameSpecifiedException() {
	super("No name was specified in project.properties");
    }
}
