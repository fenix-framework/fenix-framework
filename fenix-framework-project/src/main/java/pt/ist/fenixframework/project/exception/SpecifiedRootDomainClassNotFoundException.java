package pt.ist.fenixframework.project.exception;

public class SpecifiedRootDomainClassNotFoundException extends FenixFrameworkProjectException {

    public SpecifiedRootDomainClassNotFoundException(String className) {
        super("The specified root domain class was not found: " + className);
    }

}
