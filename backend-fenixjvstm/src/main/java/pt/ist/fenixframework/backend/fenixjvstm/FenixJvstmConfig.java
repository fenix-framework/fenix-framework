package pt.ist.fenixframework.backend.fenixjvstm;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.pstm.DataAccessPatterns;
import pt.ist.fenixframework.pstm.MetadataManager;
import pt.ist.fenixframework.pstm.repository.RepositoryBootstrap;

public class FenixJvstmConfig extends Config {
    protected final BackEnd backEnd;

    protected String dbAlias;

    protected String dbUsername;

    protected String dbPassword;

    protected boolean createRepositoryStructureIfNotExists;

    protected boolean updateRepositoryStructureIfNeeded;

    protected boolean collectDataAccessPatterns;

    protected String collectDataAccessPatternsPath;

    public FenixJvstmConfig() {
	this.backEnd = new FenixJvstmBackEnd();
    }

    @Override
    protected void init() {
	MetadataManager.init(this);
	new RepositoryBootstrap(this).updateDataRepositoryStructureIfNeeded();
	DataAccessPatterns.init(this);
    }

    @Override
    public BackEnd getBackEnd() {
	return this.backEnd;
    }

    public String getDbAlias() {
	return dbAlias;
    }

    public String getDbUsername() {
	return dbUsername;
    }

    public String getDbPassword() {
	return dbPassword;
    }

    public boolean getCreateRepositoryStructureIfNotExists() {
	return createRepositoryStructureIfNotExists;
    }

    public boolean getUpdateRepositoryStructureIfNeeded() {
	return updateRepositoryStructureIfNeeded;
    }

    public boolean getCollectDataAccessPatterns() {
	return collectDataAccessPatterns;
    }

    public String getCollectDataAccessPatternsPath() {
	return collectDataAccessPatternsPath;
    }

}
