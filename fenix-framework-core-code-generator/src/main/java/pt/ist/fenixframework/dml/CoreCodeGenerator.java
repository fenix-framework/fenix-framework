package pt.ist.fenixframework.dml;

public class CoreCodeGenerator extends PojoCodeGenerator {

    public CoreCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getDomainClassRoot() {
        return "pt.ist.fenixframework.core.CoreDomainObject";
    }
}
