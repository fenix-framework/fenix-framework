package pt.ist.fenixframework.backend.jvstm.datagrid;

import pt.ist.fenixframework.backend.jvstm.JVSTMCodeGenerator;
import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainModel;

public class JvstmDataGridCodeGenerator extends JVSTMCodeGenerator {

    public JvstmDataGridCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getBackEndName() {
        return JvstmDataGridBackEnd.BACKEND_NAME;
    }

    @Override
    protected String getDefaultConfigClassName() {
        return JvstmDataGridConfig.class.getName();
    }

}
