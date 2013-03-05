package pt.ist.fenixframework.dml;

import pt.ist.fenixframework.atomic.AtomicContextFactory;
import pt.ist.fenixframework.dml.runtime.StubAtomicContextFactory;

public class DefaultCodeGenerator extends CodeGenerator {

    public DefaultCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getBackEndName() {
        return "N/A";
    }

    @Override
    protected String getDefaultConfigClassName() {
        return "N/A";
    }

    @Override
    protected Class<? extends AtomicContextFactory> getAtomicContextFactoryClass() {
        return StubAtomicContextFactory.class;
    }

}
