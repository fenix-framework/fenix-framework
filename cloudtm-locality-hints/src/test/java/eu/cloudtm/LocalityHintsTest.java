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

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIfInitialized() {
        LocalityHints hints = new LocalityHints(new String[] {Constants.GROUP_ID, "bla"});
        hints.hints2String();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testIfInitialized2() {
        LocalityHints.string2Hints("bla");
    }

}
