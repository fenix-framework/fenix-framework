package pt.ist.fenixframework.backend.mem;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.dml.DomainModel;

/**
 * This is the default configuration manager used by the fenix-framework-core.
 * 
 * @see Config
 *
 */
public class DefaultConfig extends Config {
    protected final BackEnd backEnd;

    public DefaultConfig() {
        this.backEnd = new DefaultBackEnd();
    }

    @Override
    protected void init() {
        DomainClassInfo.initializeClassInfos(FenixFramework.getDomainModel(), 0);
    }

    @Override
    public BackEnd getBackEnd() {
        return this.backEnd;
    }
}
