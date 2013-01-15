package test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.txintrospector.TxIntrospector;

@RunWith(JUnit4.class)
public class BooksTest {

    private static final Logger logger = LoggerFactory.getLogger(BooksTest.class);

    private static final String STEPH = "Steph";
    private static final String MEH = "Meh";
    private static final String BRAM_STOKER = "Bram Stoker";

    private static final String LITTLE = "Little";
    private static final String ARCHIBALD = "Archibald";

    private static final String TWOLIGHTS = "TwoLights";
    private static final String FEW_MOON = "Few Moon";
    private static final String ECLIPSE = "Eclipse";
    private static final String DRACULA = "Dracula";

    @AfterClass
    public static void shutdown() {
        FenixFramework.shutdown();
    }

    // Tests new Objects
    @Test
    @Atomic
    public void test01() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book\n\t"
                + "(New: ['" + ECLIPSE + "']; DM: []; M: []; RCL: [nothing relevant])");

        createEclipse(txIntrospector);

        assertTrue(txIntrospector.getDirectlyModifiedObjects().isEmpty()); // should be empty
        assertTrue(txIntrospector.getModifiedObjects().isEmpty()); // should be empty

        logger.trace(txIntrospector.toString());
    }

    private static VampireBook createEclipse(TxIntrospector txIntrospector) {
        VampireBook eclipse = new VampireBook(ECLIPSE, 0.11, true);

        assertTrue(txIntrospector.getNewObjects().contains(eclipse)); // new objects should contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse

        return eclipse;
    }

    @Test
    @Atomic
    public void test02() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and directly modify it\n\t"
                +"(New: [ '" + ECLIPSE + "']; DM: []; M: []; RCL: [nothing relevant])");

        VampireBook eclipse = createEclipse(txIntrospector);
        eclipse.setBookName(eclipse.getBookName() + " SDK");
        eclipse.setPrice(3.12);
        eclipse.setHasGlowingVampires(false);

        assertTrue(txIntrospector.getDirectlyModifiedObjects().isEmpty()); // should be empty
        assertTrue(txIntrospector.getModifiedObjects().isEmpty()); // should be empty

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test03() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and modify its 1-1 relation with '" + FEW_MOON + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + FEW_MOON + "']; RCL: ['VampireBookToVampireBook'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        VampireBook fewMoon = getVampireBookByName(FEW_MOON);
        eclipse.setPrequel(fewMoon);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(fewMoon)); // modified objects should contain fewMoon
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(fewMoon)); // directly modified objects should NOT contain fewMoon

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test04() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and modify its 1-1 relation with '" + FEW_MOON + "' twice\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + FEW_MOON + "']; RCL: ['VampireBookToVampireBook'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        VampireBook fewMoon = getVampireBookByName(FEW_MOON);
        eclipse.setPrequel(fewMoon);
        fewMoon.setSequel(eclipse);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(fewMoon)); // modified objects should contain fewMoon
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(fewMoon)); // directly modified objects should NOT contain fewMoon

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test05() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and 1) add a relation with '" + FEW_MOON + "', 2) remove it, and 3) add it again\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + FEW_MOON + "']; RCL: ['VampireBookToVampireBook'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        VampireBook fewMoon = getVampireBookByName(FEW_MOON);
        eclipse.setPrequel(fewMoon);
        fewMoon.setSequel(null);
        eclipse.setPrequel(fewMoon);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(fewMoon)); // modified objects should contain fewMoon
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(fewMoon)); // directly modified objects should NOT contain fewMoon

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test06() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book, add a relation between the new book and '" + FEW_MOON + "', and then remove the relation\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: []; RCL: [])");

        VampireBook eclipse = createEclipse(txIntrospector);
        VampireBook fewMoon = getVampireBookByName(FEW_MOON);
        eclipse.setPrequel(fewMoon);
        fewMoon.setSequel(null);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getModifiedObjects().contains(fewMoon)); // modified objects should NOT contain fewMoon
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(fewMoon)); // directly modified objects should NOT contain fewMoon

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test07() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book, add a relation between the new book and '" + FEW_MOON + "', and then change relation to '" + DRACULA + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + DRACULA + "']; RCL: ['VampireBookToVampireBook'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        VampireBook fewMoon = getVampireBookByName(FEW_MOON);
        VampireBook dracula = getVampireBookByName(DRACULA);
        eclipse.setPrequel(fewMoon);
        eclipse.setPrequel(dracula);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getModifiedObjects().contains(fewMoon)); // modified objects should NOT contain fewMoon
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(fewMoon)); // directly modified objects should NOT contain fewMoon
        assertTrue(txIntrospector.getModifiedObjects().contains(dracula)); // modified objects should contain dracula
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(dracula)); // directly modified objects should NOT contain dracula

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test08() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and modify its 1-* relation with '" + LITTLE + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + LITTLE + "']; RCL: ['PublisherWithBooks'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Publisher little = getPublisherByName(LITTLE);
        eclipse.setPublisher(little);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(little)); // modified objects should contain little
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(little)); // directly modified objects should NOT contain little

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test09() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and modify its 1-* relation with '" + LITTLE + "' twice\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + LITTLE + "']; RCL: ['PublisherWithBooks'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Publisher little = getPublisherByName(LITTLE);
        eclipse.setPublisher(little);
        little.addBooksPublished(eclipse);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(little)); // modified objects should contain little
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(little)); // directly modified objects should NOT contain little

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test10() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and 1) add a 1-* relation with '" + LITTLE + "', 2) remove it, and 3) add it again\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + LITTLE + "']; RCL: ['PublisherWithBooks'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Publisher little = getPublisherByName(LITTLE);
        eclipse.setPublisher(little);
        little.removeBooksPublished(eclipse);
        little.addBooksPublished(eclipse);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(little)); // modified objects should contain little
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(little)); // directly modified objects should NOT contain little

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test11() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book, add a 1-* relation between the new book and '" + LITTLE + "', and then remove the relation\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + LITTLE + "']; RCL: [])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Publisher little = getPublisherByName(LITTLE);
        eclipse.setPublisher(little);
        little.removeBooksPublished(eclipse);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getModifiedObjects().contains(little)); // modified objects should NOT contain little
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(little)); // directly modified objects should NOT contain little

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test12() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book, add a 1-* relation between the new book and '" + LITTLE + "', and then change relation to '" + ARCHIBALD + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + ARCHIBALD + "']; RCL: ['PublisherWithBooks'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Publisher little = getPublisherByName(LITTLE);
        Publisher archibald = getPublisherByName(ARCHIBALD);
        eclipse.setPublisher(little);
        eclipse.setPublisher(archibald);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getModifiedObjects().contains(little)); // modified objects should NOT contain little
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(little)); // directly modified objects should NOT contain little
        assertTrue(txIntrospector.getModifiedObjects().contains(archibald)); // modified objects should contain archibald
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(archibald)); // directly modified objects should NOT contain archibald

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test13() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and modify its *-* relation with '" + STEPH + "' and '" + MEH + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + STEPH + "," + MEH + "']; RCL: ['2*AuthorsWithBooks'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Author meh = getAuthorByName(MEH);
        Author steph = getAuthorByName(STEPH);
        eclipse.addAuthors(meh);
        eclipse.addAuthors(steph);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(steph)); // modified objects should contain steph
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(steph)); // directly modified objects should NOT contain steph
        assertTrue(txIntrospector.getModifiedObjects().contains(meh)); // modified objects should contain meh
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(meh)); // directly modified objects should NOT contain meh

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test14() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and modify its *-* relation with '" + STEPH + "' and '" + MEH + "' twice\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + STEPH + "," + MEH + "']; RCL: ['2*AuthorsWithBooks'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Author meh = getAuthorByName(MEH);
        Author steph = getAuthorByName(STEPH);
        eclipse.addAuthors(meh);
        eclipse.addAuthors(steph);
        eclipse.addAuthors(meh);
        eclipse.addAuthors(steph);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(steph)); // modified objects should contain steph
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(steph)); // directly modified objects should NOT contain steph
        assertTrue(txIntrospector.getModifiedObjects().contains(meh)); // modified objects should contain meh
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(meh)); // directly modified objects should NOT contain meh

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test15() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and 1) add a *-* relation with '" + STEPH + "' and '" + MEH + "', 2) remove it, and 3) add it again\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + STEPH + "," + MEH + "']; RCL: ['2*AuthorsWithBooks'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Author meh = getAuthorByName(MEH);
        Author steph = getAuthorByName(STEPH);
        eclipse.addAuthors(meh);
        eclipse.addAuthors(steph);
        eclipse.removeAuthors(meh);
        eclipse.removeAuthors(steph);
        eclipse.addAuthors(meh);
        eclipse.addAuthors(steph);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertTrue(txIntrospector.getModifiedObjects().contains(steph)); // modified objects should contain steph
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(steph)); // directly modified objects should NOT contain steph
        assertTrue(txIntrospector.getModifiedObjects().contains(meh)); // modified objects should contain meh
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(meh)); // directly modified objects should NOT contain meh

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test16() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book and add a *-* relation with '" + STEPH + "' and '" + MEH + "' and then remove the relation\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: []; RCL: [])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Author meh = getAuthorByName(MEH);
        Author steph = getAuthorByName(STEPH);
        eclipse.addAuthors(meh);
        eclipse.addAuthors(steph);
        eclipse.removeAuthors(meh);
        eclipse.removeAuthors(steph);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getModifiedObjects().contains(steph)); // modified objects should contain steph
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(steph)); // directly modified objects should NOT contain steph
        assertFalse(txIntrospector.getModifiedObjects().contains(meh)); // modified objects should contain meh
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(meh)); // directly modified objects should NOT contain meh

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test17() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Create a new book, add a *-* relation with '" + STEPH + "' and '" + MEH + "', and then change relation to '" + BRAM_STOKER + "'\n\t"
                + "(New: [ '" + ECLIPSE + "']; DM: []; M: ['" + BRAM_STOKER + "']; RCL: ['AuthorsWithBooks'])");

        VampireBook eclipse = createEclipse(txIntrospector);
        Author meh = getAuthorByName(MEH);
        Author steph = getAuthorByName(STEPH);
        Author bStoker = getAuthorByName(BRAM_STOKER);
        eclipse.addAuthors(meh);
        eclipse.addAuthors(steph);
        eclipse.removeAuthors(meh);
        eclipse.removeAuthors(steph);
        eclipse.addAuthors(bStoker);

        assertFalse(txIntrospector.getModifiedObjects().contains(eclipse)); // modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(eclipse)); // directly modified objects should NOT contain eclipse
        assertFalse(txIntrospector.getModifiedObjects().contains(steph)); // modified objects should contain steph
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(steph)); // directly modified objects should NOT contain steph
        assertFalse(txIntrospector.getModifiedObjects().contains(meh)); // modified objects should contain meh
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(meh)); // directly modified objects should NOT contain meh
        assertTrue(txIntrospector.getModifiedObjects().contains(bStoker)); // modified objects should contain bStoker
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(bStoker)); // directly modified objects should NOT contain bStoker

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test18() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Change the relation between '" + FEW_MOON + "' and '" + TWOLIGHTS + "' to '" + FEW_MOON + "' and '" + DRACULA + "'\n\t"
                + "(New: []; DM: []; M: ['" + FEW_MOON + "', '" + DRACULA + "', '" + TWOLIGHTS + "']; "
                + "RCL: ['VampireBookToVampireBook' (removed), 'VampireBookToVampireBook' (changed)])");

        VampireBook fewMoon = getVampireBookByName(FEW_MOON);
        VampireBook twoLights = getVampireBookByName(TWOLIGHTS);
        VampireBook dracula = getVampireBookByName(DRACULA);
        fewMoon.setPrequel(dracula);

        assertTrue(txIntrospector.getNewObjects().isEmpty()); // should be empty
        assertTrue(txIntrospector.getModifiedObjects().contains(fewMoon)); // modified objects should contain fewMoon
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(fewMoon)); // directly modified objects should NOT contain fewMoon
        assertTrue(txIntrospector.getModifiedObjects().contains(twoLights)); // modified objects should contain twoLights
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(twoLights)); // directly modified objects should NOT contain twoLights
        assertTrue(txIntrospector.getModifiedObjects().contains(dracula)); // modified objects should contain dracula
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(dracula)); // directly modified objects should NOT contain dracula

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test19() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Change the relation between '" + FEW_MOON + "' and '" + TWOLIGHTS + "' to '" + FEW_MOON + "' and '" + DRACULA + "'. "
                + "Then change the name of '" + TWOLIGHTS + "' to tWoLiGhTs\n\t"
                + "(New: []; DM: []; M: ['" + FEW_MOON + "', '" + DRACULA + "', '" + TWOLIGHTS + "']; "
                + "RCL: ['VampireBookToVampireBook' (removed), 'VampireBookToVampireBook' (changed)])");

        VampireBook fewMoon = getVampireBookByName(FEW_MOON);
        VampireBook twoLights = getVampireBookByName(TWOLIGHTS);
        VampireBook dracula = getVampireBookByName(DRACULA);
        fewMoon.setPrequel(dracula);
        twoLights.setBookName("tWoLiGhTs");

        assertTrue(txIntrospector.getNewObjects().isEmpty()); // should be empty
        assertTrue(txIntrospector.getModifiedObjects().contains(fewMoon)); // modified objects should contain fewMoon
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(fewMoon)); // directly modified objects should NOT contain fewMoon
        assertTrue(txIntrospector.getModifiedObjects().contains(twoLights)); // modified objects should contain twoLights
        assertTrue(txIntrospector.getDirectlyModifiedObjects().contains(twoLights)); // directly modified objects should contain twoLights
        assertTrue(txIntrospector.getModifiedObjects().contains(dracula)); // modified objects should contain dracula
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(dracula)); // directly modified objects should NOT contain dracula

        logger.trace(txIntrospector.toString());
    }

    @Test
    @Atomic
    public void test20() {
	TxIntrospector txIntrospector = FenixFramework.getTransaction().getTxIntrospector();
        printTest("Change the relation '" + FEW_MOON + "' <-> '" + TWOLIGHTS + "' to '" + FEW_MOON + "' <-> '" + DRACULA + "' "
                + "and then back again to '" + FEW_MOON + "' <-> '" + TWOLIGHTS + "'. Finally change the name of '" + TWOLIGHTS + "' to tWoLiGhTs\n\t"
                + "(New: []; DM: ['" + TWOLIGHTS + "']; M: ['" + TWOLIGHTS + "']; RCL: [])");

        VampireBook fewMoon = getVampireBookByName(FEW_MOON);
        VampireBook twoLights = getVampireBookByName(TWOLIGHTS);
        VampireBook dracula = getVampireBookByName(DRACULA);
        fewMoon.setPrequel(dracula);
        fewMoon.setPrequel(twoLights);
        twoLights.setBookName("tWoLiGhTs");

        assertTrue(txIntrospector.getNewObjects().isEmpty()); // should be empty
        assertFalse(txIntrospector.getModifiedObjects().contains(fewMoon)); // modified objects should NOT contain fewMoon
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(fewMoon)); // directly modified objects should NOT contain fewMoon
        assertTrue(txIntrospector.getModifiedObjects().contains(twoLights)); // modified objects should contain twoLights
        assertTrue(txIntrospector.getDirectlyModifiedObjects().contains(twoLights)); // directly modified objects should contain twoLights
        assertFalse(txIntrospector.getModifiedObjects().contains(dracula)); // modified objects should NOT contain dracula
        assertFalse(txIntrospector.getDirectlyModifiedObjects().contains(dracula)); // directly modified objects should NOT contain dracula

        logger.trace(txIntrospector.toString());
    }

    private static void printTest(String desc) {
        String test = Thread.currentThread().getStackTrace()[2].getMethodName().substring(7).toUpperCase();
        logger.trace("############################ " + test + " ############################\n\t" + desc);
    }

    @Before
    @Atomic
    public void init() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        // Authors
        Author steph = new Author(STEPH, 83);
        domainRoot.addTheAuthors(steph);
        Author meh = new Author(MEH, 83);
        domainRoot.addTheAuthors(meh);
        Author bStoker = new Author(BRAM_STOKER, 125);
        domainRoot.addTheAuthors(bStoker);

        // Publishers
        Publisher little = new Publisher(LITTLE);
        domainRoot.addThePublishers(little);
        Publisher archibald = new Publisher(ARCHIBALD);
        domainRoot.addThePublishers(archibald);

        // Books
        VampireBook twolights = new VampireBook(TWOLIGHTS, 0.42, true);
        domainRoot.addTheBooks(twolights);
        VampireBook fewMoons = new VampireBook(FEW_MOON, 0.84, true);
        domainRoot.addTheBooks(fewMoons);
        Book dracula = new VampireBook(DRACULA, 12.42, false);
        domainRoot.addTheBooks(dracula);

        // Publisher with books
        little.addBooksPublished(twolights);
        little.addBooksPublished(fewMoons);
        archibald.addBooksPublished(dracula);

        // Authors with books
        steph.addBooks(twolights);
        steph.addBooks(fewMoons);
        meh.addBooks(twolights);
        meh.addBooks(fewMoons);
        bStoker.addBooks(dracula);

        // VampireBook to VampireBook
        twolights.setSequel(fewMoons);
    }

    @After
    @Atomic
    public void reset() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        for (Book book : domainRoot.getTheBooks()) {
            domainRoot.removeTheBooks(book);
        }

        for (Author author : domainRoot.getTheAuthors()) {
            domainRoot.removeTheAuthors(author);
        }

        for (Publisher publisher : domainRoot.getThePublishers()) {
            domainRoot.removeThePublishers(publisher);
        }
    }

    @Atomic
    @SuppressWarnings("unused")
    private static void printAll() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        for (Book book : domainRoot.getTheBooks()) {
            System.out.println(book);
        }

        for (Author author : domainRoot.getTheAuthors()) {
            System.out.println(author);
        }

        for (Publisher publisher : domainRoot.getThePublishers()) {
            System.out.println(publisher);
        }
    }

    @Atomic
    public static Author getAuthorByName(String authorName) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Author author : domainRoot.getTheAuthors()) {
            if (author.getName().equals(authorName)) {
                return author;
            }
        }
        return null;
    }

    @Atomic
    public static Book getBookByName(String bookName) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Book book : domainRoot.getTheBooks()) {
            if (book.getBookName().equals(bookName)) {
                return book;
            }
        }
        return null;
    }

    public static VampireBook getVampireBookByName(String bookName) {
        return (VampireBook) getBookByName(bookName);
    }

    @Atomic
    public static Publisher getPublisherByName(String publisherName) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Publisher publisher : domainRoot.getThePublishers()) {
            if (publisher.getPublisherName().equals(publisherName)) {
                return publisher;
            }
        }
        return null;
    }
}
