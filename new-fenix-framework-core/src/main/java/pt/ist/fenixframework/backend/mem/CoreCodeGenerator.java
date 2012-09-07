package pt.ist.fenixframework.backend.mem;

import  pt.ist.fenixframework.dml.CompilerArgs;
import  pt.ist.fenixframework.dml.DomainModel;
import  pt.ist.fenixframework.dml.AbstractCodeGenerator;

public class CoreCodeGenerator extends AbstractCodeGenerator {

    public CoreCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getDomainClassRoot() {
        return CoreDomainObject.class.getName();
    }

    @Override
    protected String getBackEndName() {
        return DefaultBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return DefaultConfig.class.getName();
    }
}
