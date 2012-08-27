package pt.ist.fenixframework.backend.infinispan;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.dml.DomainModel;

/**
 * This is the infinispan configuration manager used by the fenix-framework-backend-infinispan
 * project.
 * 
 * @see Config
 *
 */
public class InfinispanConfig extends Config {

    // p.e. uma config util aqui deve ser o shared vs global identity mapper


    protected final BackEnd backEnd;

    public InfinispanConfig() {
        this.backEnd = new InfinispanBackEnd();
    }

    @Override
    protected void init(DomainModel domainModel) {
        // DomainClassInfo.initializeClassInfos(domainModel, 0);
    }

    @Override
    public BackEnd getBackEnd() {
        return this.backEnd;
    }
}
