package pt.ist.fenixframework.backend.jvstmmem;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.Config;

public class JVSTMMemConfig extends Config {
    protected final BackEnd backEnd;

    public JVSTMMemConfig() {
        this.backEnd = new JVSTMMemBackEnd();
    }

    @Override
    protected void init() {
        DomainClassInfo.initializeClassInfos(FenixFramework.getDomainModel(), 0);
    }

    @Override
    public BackEnd getBackEnd() {
        return this.backEnd;
    }

    @Override
    public String getBackEndName() {
        return JVSTMMemBackEnd.BACKEND_NAME;
    }

}
