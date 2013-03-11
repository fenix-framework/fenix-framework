package pt.ist.fenixframework.backend.jvstmojb;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.backend.jvstmojb.ojb.MetadataManager;
import pt.ist.fenixframework.backend.jvstmojb.pstm.DomainClassInfo;
import pt.ist.fenixframework.backend.jvstmojb.pstm.TransactionSupport;
import pt.ist.fenixframework.backend.jvstmojb.repository.RepositoryBootstrap;
import pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicatesConfig;
import pt.ist.fenixframework.pstm.DomainFenixFrameworkRoot;

/**
 * An instance of the <code>Config</code> class bundles together the
 * initialization parameters used by the Fenix Framework. Therefore, before
 * initializing the framework (via the call to the <code>FenixFramework.initialize(Config)</code> method), the programmer should
 * create an instance of <code>Config</code> with the correct values for each of
 * the parameters.
 * 
 * No constructor is provided for this class (other than the default
 * constructor), because the <code>Config</code> class has several parameters,
 * some of which are optional. But, whereas optional parameters do not need to
 * be specified, the parameters that are required must be specified by the
 * programmer before calling the <code>FenixFramework.initialize</code> method.
 * 
 * To create an instance of this class with the proper values for its
 * parameters, programmers should generally use code like this:
 * 
 * <blockquote>
 * 
 * <pre>
 * 
 * Config config = new Config() {
 *     {
 *         this.appName = &quot;MyAppName&quot;;
 *         this.domainModelPath = &quot;path/to/domain.dml&quot;;
 *         this.dbAlias = &quot;//somehost:3306/databaseName&quot;;
 *         this.dbUsername = &quot;dbuser&quot;;
 *         this.dbPassword = &quot;dpPass&quot;;
 *     }
 * };
 * 
 * </pre>
 * 
 * </blockquote>
 * 
 * Note the use of the double
 * 
 * <pre>
 * { {} }
 * </pre>
 * 
 * to delimit an instance initializer block for the anonymous inner class being
 * created.
 * 
 * Each of the parameters of the <code>Config</code> class is represented as a
 * protected class field. Look at the documentation of each field to see what is
 * it for, whether it is optional or required, and in the former case, what is
 * its default value.
 * 
 * The current set of required parameters are the following:
 * <ul>
 * <li>domainModelPath XOR domainModelPaths</li>
 * <li>dbAlias</li>
 * <li>dbUsername</li>
 * <li>dbPassword</li>
 * </ul>
 */
public class JvstmOJBConfig extends ConsistencyPredicatesConfig {

    private static final Logger logger = LoggerFactory.getLogger(JvstmOJBConfig.class);

    protected final BackEnd backEnd;

    /**
     * This <strong>required</strong> parameter specifies the JDBC alias that
     * will be used to access the database where domain entities are stored. The
     * value of this parameter should not contain neither the protocol, nor the
     * sub-protocol, but may contain any parameters that configure the
     * connection to the MySQL database (e.g., a possible value for this
     * parameter is
     * "//localhost:3306/mydb?useUnicode=true&amp;characterEncoding=latin1"). A
     * non-null value must be specified for this parameter.
     */
    protected String dbAlias = null;

    /**
     * This <strong>required</strong> parameter specifies the username that will
     * be used to access the database where domain entities are stored. A
     * non-null value must be specified for this parameter.
     */
    protected String dbUsername = null;

    /**
     * This <strong>required</strong> parameter specifies the password that will
     * be used to access the database where domain entities are stored. A
     * non-null value must be specified for this parameter.
     */
    protected String dbPassword = null;

    /**
     * This <strong>optional</strong> parameter specifies whether an error
     * should be thrown if during a transaction an object that was deleted
     * during the transaction is subsequently changed. The default value of <code>true</code> will cause an <code>Error</code> to
     * be thrown, whereas
     * a value of <code>false</code> will cause only a warning to be issued.
     */
    protected boolean errorIfChangingDeletedObject = true;

    /**
     * This <strong>optional</strong> parameter specifies whether the framework
     * should initialize the persistent store if it detects that the store was
     * not properly initialized (e.g., the database has no tables). The default
     * value for this parameter is <code>true</code>, but a programmer may want
     * to specify a value of <code>false</code>, if she wants to have control
     * over the initialization of the persistent store. In this latter case,
     * however, if the store is not properly initialized before initializing the
     * framework, probably a runtime exception will be thrown during the
     * framework initialization.
     */
    protected boolean createRepositoryStructureIfNotExists = true;

    /**
     * This <strong>optional</strong> parameter indicates whether the database
     * structure should be automatically updated with missing structure entries
     * when the framework is initialized. Defaults to false. This is only
     * relevant if some database structure already exists.
     */
    protected boolean updateRepositoryStructureIfNeeded = false;

    /*
     * Initialization methods
     */

    public JvstmOJBConfig() {
        this.backEnd = new JvstmOJBBackEnd();
    }

    @Override
    protected void init() {
        print();
        MetadataManager.init(this);
        new RepositoryBootstrap(this).updateDataRepositoryStructureIfNeeded();
        DomainClassInfo.initializeClassInfos(0);
        DomainClassInfo.ensureDomainRoot();
        TransactionSupport.setupJVSTM();
        DomainFenixFrameworkRoot.getLockAndBootstrap(this);
    }

    @Override
    public BackEnd getBackEnd() {
        return this.backEnd;
    }

    @Override
    public void checkConfig() {
        super.checkConfig();
        checkRequired(dbAlias, "dbAlias");
        checkRequired(dbUsername, "dbUsername");
        checkRequired(dbPassword, "dbPassword");
    }

    /*
     * FromString methods.
     * 
     * These are responsible for the correct processing of
     * Config's options.
     */

    protected void dbAliasFromString(String value) {
        this.dbAlias = value;
        StringBuilder encodingParams = new StringBuilder();
        encodingParams.append("useUnicode=true&characterEncoding=UTF-8&clobCharacterEncoding=UTF-8&characterSetResults=UTF-8");

        int questionMarkIndex = this.dbAlias.indexOf('?');

        if (questionMarkIndex == -1) {
            this.dbAlias = this.dbAlias + '?' + encodingParams;
        } else {
            String prefix = this.dbAlias.substring(0, questionMarkIndex + 1);
            String rest = this.dbAlias.substring(questionMarkIndex + 1);

            this.dbAlias = prefix + encodingParams + '&' + rest;
        }
    }

    protected void errorIfChangingDeletedObjectFromString(String value) {
        errorIfChangingDeletedObject = Boolean.parseBoolean(value);
    }

    protected void createRepositoryStructureIfNotExistsFromString(String value) {
        createRepositoryStructureIfNotExists = Boolean.parseBoolean(value);
    }

    protected void updateRepositoryStructureIfNeededFromString(String value) {
        updateRepositoryStructureIfNeeded = Boolean.parseBoolean(value);
    }

    /*
     * Public Getters
     */

    public String getDbAlias() {
        return dbAlias;
    }

    public String getDbName() {
        Pattern pattern = Pattern.compile("//.*/(.*)\\?");
        Matcher matcher = pattern.matcher(getDbAlias());
        if (!matcher.find()) {
            throw new Error("Malformed dbAlias - could not retrieve dbName.");
        }
        return matcher.group(1);
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public boolean isErrorIfChangingDeletedObject() {
        return errorIfChangingDeletedObject;
    }

    public boolean getCreateRepositoryStructureIfNotExists() {
        return createRepositoryStructureIfNotExists;
    }

    public boolean getUpdateRepositoryStructureIfNeeded() {
        return updateRepositoryStructureIfNeeded;
    }

    public void print() {
        logger.trace("dbAlias: " + dbAlias);
        logger.trace("dbName: " + getDbName());
        logger.trace("dbUsername: " + dbUsername);
        // The password should never be shown, or even mentioned
        //logger.trace("dbPassword: *hidden*");

        logger.trace("appName: " + appName);
        logger.trace("errorIfChangingDeletedObject: " + errorIfChangingDeletedObject);

        logger.trace("createRepositoryStructureIfNotExists: " + createRepositoryStructureIfNotExists);
        logger.trace("updateRepositoryStructureIfNeeded: " + updateRepositoryStructureIfNeeded);

        logger.trace("canCreateDomainMetaObjects: " + canCreateDomainMetaObjects);
    }

    public static boolean canCreateDomainMetaObjects() {
        return FenixFramework.<JvstmOJBConfig> getConfig().getCanCreateDomainMetaObjects();
    }

}
