package pt.ist.fenixframework.backend.mem;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.dml.DomainModel;
import pt.ist.fenixframework.indexes.IndexesConfig;

/**
 * This is the default configuration manager used by the fenix-framework-core.
 * 
 * @see Config
 *
 */
public class MemConfig extends IndexesConfig {
    protected final BackEnd backEnd;

    public MemConfig() {
        this.backEnd = new MemBackEnd();
    }

    @Override
    protected void init() {
        DomainClassInfo.initializeClassInfos(FenixFramework.getDomainModel(), 0);
        updateIndexes();
    }

    @Override
    public BackEnd getBackEnd() {
        return this.backEnd;
    }
}
