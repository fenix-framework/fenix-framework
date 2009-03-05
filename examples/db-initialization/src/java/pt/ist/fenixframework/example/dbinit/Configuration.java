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
	    /* uncomment the next line if you want the repository structure automatically updated when your domain definition
	       changes */
	    // updateRepositoryStructureIfNeeded = true;
        }};
        FenixFramework.initialize(config);
    }

    static Root ensureRootObject() {
	Root root = (Root)Transaction.getDomainObject(Root.class.getName(), 1);
	// force object to load to check if it really exists or is just a stub
	try {
	    root.getDataStore();
	} catch (VersionNotAvailableException ex) {
	    // then create the object.  It is assumed that this object will be created with idInternal 1.
	    System.out.println("IT IS NORMAL TO SEE AN EXCEPTION IF THE ROOT OBJECT COULD NOT BE READ.  THIS SHOULD BE OK.");
	    root = new Root();
	}
	return root;
    }

}
