package pt.ist.fenixframework.backend.jvstm.lf;

import pt.ist.fenixframework.backend.jvstm.JVSTMCodeGenerator;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainModel;

public class JvstmLockFreeCodeGenerator extends JVSTMCodeGenerator {

    public JvstmLockFreeCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getBackEndName() {
        return JvstmLockFreeBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return JvstmLockFreeConfig.class.getName();
    }

}
