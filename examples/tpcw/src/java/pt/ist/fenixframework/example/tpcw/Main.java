package pt.ist.fenixframework.example.tpcw;

import pt.ist.fenixframework.example.tpcw.domain.*;

import jvstm.Atomic;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;

public class Main {

    static void initializeFenixFramework() {
        Config config = new Config() {{
            domainModelPath = "/tpcw.dml";
   	    dbAlias = "//localhost:3306/tpcwFenix";
            dbUsername = "tpcw";
            dbPassword = "tpcw";
	    // updateRepositoryStructureIfNeeded = true;
            rootClass = Root.class;
        }};
        FenixFramework.initialize(config);
    }

    private static Root rootObject = null;

    synchronized private static Root getRoot() {
	if (rootObject == null) {
	    rootObject = FenixFramework.getRoot();
	}
	return rootObject;
    }

    public static void main(String [] args) {
	initializeFenixFramework();
	testScript();
    }

    @Atomic
    static void testScript() {
// 	System.out.println(Soundex.soundex("-Y"));
	
//  	System.out.println(getRoot().getBooksByAuthorSortedByTitle("erasdj", 50));
	
    }
}