package pt.ist.fenixframework.backend.jvstmmem;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.hibernatesearch.HibernateSearchConfig;

public class JVSTMMemConfig extends HibernateSearchConfig {
    protected final BackEnd backEnd;

    public JVSTMMemConfig() {
	this.backEnd = new JVSTMMemBackEnd();
    }

    @Override
    protected void init() {
	DomainClassInfo.initializeClassInfos(FenixFramework.getDomainModel(), 0);
	super.init();
    }

    @Override
    public BackEnd getBackEnd() {
	return this.backEnd;
    }

}
