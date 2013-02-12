package pt.ist.fenixframework.backend.mem;

import pt.ist.fenixframework.atomic.ContextFactory;
import pt.ist.fenixframework.atomic.DefaultContextFactory;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.dml.IndexesCodeGenerator;

public class MemCodeGenerator extends IndexesCodeGenerator {

    public MemCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getDomainClassRoot() {
        return MemDomainObject.class.getName();
    }

    @Override
    protected String getBackEndName() {
        return MemBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return MemConfig.class.getName();
    }

    @Override
    protected Class<? extends ContextFactory> getAtomicContextFactoryClass() {
        return DefaultContextFactory.class;
    }

}
