package ${package};

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;
import ${package}.${rootClassname};
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SimpleTestCase extends TestCase {

	public SimpleTestCase(String testName) {
		super(testName);
	}
	
	public static Test suite() {
		return new TestSuite(SimpleTestCase.class);
	}
	
	public void testBootstrapAndSimpleWrite() {
    Bootstrap.init();
    Transaction.begin();
    ${rootClassname} app = (${rootClassname})FenixFramework.getRoot();
    app.setName("It Works");
    Transaction.commit();
  }
}