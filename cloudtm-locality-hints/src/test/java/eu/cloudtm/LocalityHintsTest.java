package eu.cloudtm;

import org.testng.annotations.Test;

/**
 * // TODO: Document this
 * 
 * @author Pedro Ruivo
 * @since 2.3-cloudtm
 */
@Test
public class LocalityHintsTest {

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testWrongParameters() {
        new LocalityHints(new String[] {"bla"});
    }

    @Test
    public void testEmptyParameters() {
        new LocalityHints().hints2String();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullParameters() {
        new LocalityHints(null).hints2String();
    }

    @Test
    public void testIfInitialized() {
        new LocalityHints(new String[] {Constants.GROUP_ID, "bla"}).hints2String();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNotValidString() {
        LocalityHints.string2Hints("bla");
    }

}
