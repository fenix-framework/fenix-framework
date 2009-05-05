
package pt.ist.fenixframework.example.oo7;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.example.oo7.extra.OO7Database;
import pt.ist.fenixframework.example.oo7.domain.OO7Application;

public class InitOO7FFDatabase {

    public static void main(final String[] args) {
        Config config = new Config() {{
            domainModelPath = "/oo7.dml";
            dbAlias = "//localhost:3306/ssilva";
            dbUsername = "ssilva";
            dbPassword = "ssilva";
            rootClass = OO7Application.class;
        }};
        FenixFramework.initialize(config);
        //Transaction.withTransaction(new TransactionalCommand () {
        //	public void doIt() {
        		new OO7FFDatabase(OO7Database.SMALL).createOO7Database();
        //	}
        //});

    }

}
