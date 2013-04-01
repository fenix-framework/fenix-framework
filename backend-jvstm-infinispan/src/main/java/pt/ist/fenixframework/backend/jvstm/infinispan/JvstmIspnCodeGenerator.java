package pt.ist.fenixframework.backend.jvstm.infinispan;

import pt.ist.fenixframework.backend.jvstm.JVSTMCodeGenerator;
import pt.ist.fenixframework.backend.jvstm.JVSTMDomainObject;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainModel;

public class JvstmIspnCodeGenerator extends JVSTMCodeGenerator {

    public JvstmIspnCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getDomainClassRoot() {
        return JVSTMDomainObject.class.getName();
    }

    @Override
    protected String getBackEndName() {
        return JvstmIspnBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return JvstmIspnConfig.class.getName();
    }

}
