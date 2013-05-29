package ${package};

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
            //Insert Test Here
        }
}