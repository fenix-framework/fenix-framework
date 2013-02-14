package pt.ist.fenixframework.pstm;

import org.apache.ojb.broker.metadata.ConnectionPoolDescriptor;
import org.apache.ojb.broker.metadata.JdbcConnectionDescriptor;
import org.apache.ojb.broker.metadata.SequenceDescriptor;
import org.apache.ojb.broker.util.configuration.impl.OjbConfiguration;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.pstm.dml.FenixDomainModel;

public class MetadataManager {

    private static MetadataManager instance;

    private final FenixDomainModel domainModel;

    private org.apache.ojb.broker.metadata.MetadataManager ojbMetadataManager;

    private MetadataManager(final Config config) {
	try {
	    this.domainModel = DML.getDomainModelForURLs(config.getDomainModelClass(), config.getDomainModelURLs(), true);

	    // create the OJB's MetadataManager, but use the correct
	    // OJB.properties file
	    System.setProperty(OjbConfiguration.OJB_PROPERTIES_FILE, "pt/ist/fenixframework/OJB.properties");
	    this.ojbMetadataManager = org.apache.ojb.broker.metadata.MetadataManager.getInstance();

	    // config the OJB's database descriptor
	    JdbcConnectionDescriptor jcd = makeJdbcDescriptor(config);
	    this.ojbMetadataManager.connectionRepository().addDescriptor(jcd);
	    this.ojbMetadataManager.setDefaultPBKey(jcd.getPBKey());

	    // generate the OJB's mappings
	    OJBMetadataGenerator.updateOJBMappingFromDomainModel(domainModel);
	} catch (Exception e) {
	    // transform any exception during the initialization phase into an
	    // Error
	    throw new Error(e);
	}
    }

    public static void init(final Config config) {
	synchronized (MetadataManager.class) {
	    if (instance == null) {
		instance = new MetadataManager(config);
	    }
	}
    }

    public static FenixDomainModel getDomainModel() {
	return instance != null ? instance.domainModel : null;
    }

    public static org.apache.ojb.broker.metadata.MetadataManager getOjbMetadataManager() {
	return instance != null ? instance.ojbMetadataManager : null;
    }

    public static JdbcConnectionDescriptor makeJdbcDescriptor(Config config) {
	JdbcConnectionDescriptor jcd = new JdbcConnectionDescriptor();
	jcd.setJcdAlias("OJB/repository.xml");
	jcd.setDefaultConnection(true);
	jcd.setDbms("MySQL");
	jcd.setJdbcLevel("1.0");
	jcd.setDriver("com.mysql.jdbc.Driver");
	jcd.setProtocol("jdbc");
	jcd.setSubProtocol("mysql");
	jcd.setDbAlias(config.getDbAlias());
	jcd.setUserName(config.getDbUsername());
	jcd.setPassWord(config.getDbPassword());
	jcd.setEagerRelease(false);
	jcd.setBatchMode(false);
	jcd.setUseAutoCommit(2);
	jcd.setIgnoreAutoCommitExceptions(false);

	ConnectionPoolDescriptor cpd = jcd.getConnectionPoolDescriptor();
	cpd.setMaxActive(-1);
	cpd.setMaxIdle(5);
	cpd.setMaxWait(5000);
	cpd.setMinEvictableIdleTimeMillis(600000);
	cpd.setNumTestsPerEvictionRun(10);
	cpd.setTestOnBorrow(true);
	cpd.setTestOnReturn(false);
	cpd.setTestWhileIdle(false);
	cpd.setTimeBetweenEvictionRunsMillis(-1L);
	cpd.setWhenExhaustedAction((byte) 2);
	cpd.setValidationQuery("select 1");
	cpd.setLogAbandoned(false);
	cpd.setRemoveAbandoned(false);
	cpd.setRemoveAbandonedTimeout(300);

	SequenceDescriptor sd = new SequenceDescriptor(jcd);
	jcd.setSequenceDescriptor(sd);
	sd.setSequenceManagerClass(org.apache.ojb.broker.util.sequence.SequenceManagerHighLowImpl.class);
	sd.addAttribute("grabSize", "200");

	return jcd;
    }
}
