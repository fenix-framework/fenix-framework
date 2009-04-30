package pt.ist.fenixframework.example.dbinit;

import jvstm.TransactionalCommand;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.pstm.VersionNotAvailableException;

import pt.ist.fenixframework.example.dbinit.domain.*;

public class Configuration {

    static void initializeFenixFramework() {
        Config config = new Config() {{
            domainModelPath = "/dbinit.dml";
            dbAlias = "//localhost:3306/test";
            dbUsername = "test";
            dbPassword = "test";
            rootClass = Root.class;
	    /* uncomment the next line if you want the repository structure automatically updated when your domain definition
	       changes */
	    // updateRepositoryStructureIfNeeded = true;
        }};
        FenixFramework.initialize(config);
    }
}
