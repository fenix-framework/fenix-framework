package pt.ist.fenixframework.example.suspend;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;

import pt.ist.fenixframework.example.suspend.domain.App;

public class Init {

    public static void init() {
        Config config = new Config() {{ 
            domainModelPath = "/domain.dml";
            dbAlias = "//localhost:3306/test";
            dbUsername = "test";
            dbPassword = "test";
            rootClass = App.class;
        }};
        FenixFramework.initialize(config);
    }
}
