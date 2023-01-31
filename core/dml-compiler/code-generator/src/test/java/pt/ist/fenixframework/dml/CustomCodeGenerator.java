package pt.ist.fenixframework.dml;

public class CustomCodeGenerator extends CodeGenerator {

    public CustomCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getBackEndName() {
        return "Test";
    }

    @Override
    protected String getDefaultConfigClassName() {
        return "Test";
    }

}
