package pt.ist.fenixframework.backend.infinispan;

import  pt.ist.fenixframework.dml.CompilerArgs;
import  pt.ist.fenixframework.dml.DomainModel;
import  pt.ist.fenixframework.dml.AbstractCodeGenerator;

public class InfinispanCodeGenerator extends AbstractCodeGenerator {

    public InfinispanCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getDomainClassRoot() {
        return InfinispanDomainObject.class.getName();
    }

    @Override
    protected String getBackEndName() {
        return InfinispanBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return InfinispanConfig.class.getName();
    }
}
