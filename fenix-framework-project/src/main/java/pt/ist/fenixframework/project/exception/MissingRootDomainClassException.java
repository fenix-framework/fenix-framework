package pt.ist.fenixframework.project.exception;

import pt.ist.fenixframework.project.FenixFrameworkProject;

public class MissingRootDomainClassException extends FenixFrameworkProjectException {

    public MissingRootDomainClassException() {
        super("Missing parameter: " + FenixFrameworkProject.ROOT_DOMAIN_CLASS_KEY);
    }

}
