package pt.ist.fenixframework.project.exception;

public class NoArtifactNameSpecifiedException extends FenixFrameworkProjectException {

    public NoArtifactNameSpecifiedException() {
	super("No name was specified in project.properties");
    }
}
