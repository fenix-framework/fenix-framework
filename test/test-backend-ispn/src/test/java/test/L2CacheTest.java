package test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.infinispan.InfinispanConfig;
import pt.ist.fenixframework.backend.infinispan.RelationAwareCacheableSet;
import pt.ist.fenixframework.backend.infinispan.RelationMulValuesIndexedCacheableAwareSet;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class L2CacheTest {
    
    private static final String ISPN_CONFIG_FILE = "ispn.xml";

    @BeforeClass
    public static void init() throws Exception {
        InfinispanConfig infinispanConfig = new InfinispanConfig();
        infinispanConfig.appNameFromString("fenix-framework-test-backend-ispn");
        ConfigurationBuilderHolder holder = new ParserRegistry(Thread.currentThread().getContextClassLoader())
                .parseFile(ISPN_CONFIG_FILE);
        infinispanConfig.setDefaultConfiguration(holder.getDefaultConfigurationBuilder().build());
        infinispanConfig.setGlobalConfiguration(holder.getGlobalConfigurationBuilder().build());
        FenixFramework.initialize(infinispanConfig);
        setupObjects();
    }

    private static final int NUMBER_ELEMENTS = 4;
    private static final int DIVIDE_RATIO = 2;
    
    @Atomic
    private static void setupObjects() {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
        for (int i = 0; i < NUMBER_ELEMENTS; i++) {
            domainRoot.addTheBooks(new Book(i, i));
            domainRoot.addTheAuthors(new Author(i % (NUMBER_ELEMENTS / DIVIDE_RATIO), i));
            domainRoot.addThePublishers(new Publisher(i));
        }
    }
    
    @AfterClass
    public static void shutdown() {
        FenixFramework.shutdown();
    }
    
    @Test
    public void testCachedSlots() {
	getObjects(false);
	getObjects(false);
	getObjects(true);
	getObjects(false);
	
	insertNewObjects();
	getNewObjects(false);
	getNewObjects(true);
    }
    
    @Atomic
    private void getObjects(boolean forceMiss) {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
	for (int i = 0; i < NUMBER_ELEMENTS; i++) {
	    assertTrue(domainRoot.getTheBooksByIdCached(forceMiss, i) == domainRoot.getTheBooksById(i));
	}
	
	for (int i = 0; i < NUMBER_ELEMENTS / DIVIDE_RATIO; i++) {
	    assertTrue(checkSetsEqual(domainRoot.getTheAuthorsByIdCached(forceMiss, i), (domainRoot.getTheAuthorsById(i))));
	}
	
	assertTrue(checkSetsEqual(domainRoot.getTheAuthorsCached(forceMiss), domainRoot.getTheAuthors()));
	assertTrue(checkSetsEqual(domainRoot.getThePublishersCached(forceMiss), domainRoot.getThePublishers()));
	assertTrue(checkSetsEqual(domainRoot.getTheBooksCached(forceMiss), domainRoot.getTheBooks()));
    }
    
    @Atomic
    private void insertNewObjects() {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
	domainRoot.addTheBooks(new Book(NUMBER_ELEMENTS + 1, NUMBER_ELEMENTS + 1));
	domainRoot.addTheAuthors(new Author(NUMBER_ELEMENTS + 1, NUMBER_ELEMENTS + 1));
	domainRoot.addThePublishers(new Publisher(NUMBER_ELEMENTS + 1));
    }
    
    @Atomic
    private void getNewObjects(boolean forceMiss) {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
	if (! forceMiss) {
	    assertTrue(domainRoot.getTheBooksByIdCached(forceMiss, NUMBER_ELEMENTS + 1) == null);
	    assertTrue(domainRoot.getTheBooksById(NUMBER_ELEMENTS + 1) != null);
	} else {
	    assertTrue(domainRoot.getTheBooksByIdCached(forceMiss, NUMBER_ELEMENTS + 1) == domainRoot.getTheBooksById(NUMBER_ELEMENTS + 1));
	}
	
	if (! forceMiss) {
	    assertTrue(domainRoot.getTheAuthorsByIdCached(forceMiss, NUMBER_ELEMENTS + 1).size() == 0);
	} else {
	    assertTrue(checkSetsEqual(domainRoot.getTheAuthorsByIdCached(forceMiss, NUMBER_ELEMENTS + 1), (domainRoot.getTheAuthorsById(NUMBER_ELEMENTS + 1))));
	}
	
	checkSets(domainRoot, forceMiss);
    }
    
    private void checkSets(DomainRoot domainRoot, boolean forceMiss) {
	if (! forceMiss) {
	    assertTrue(! checkSetsEqual(domainRoot.getTheAuthorsCached(forceMiss), domainRoot.getTheAuthors()));
	    assertTrue(! checkSetsEqual(domainRoot.getThePublishersCached(forceMiss), domainRoot.getThePublishers()));
	    assertTrue(! checkSetsEqual(domainRoot.getTheBooksCached(forceMiss), domainRoot.getTheBooks()));
	} else {
	    // FIXME Unfortunately we cannot correctly assert this
	    // The problem is that the underlying method is using the implicit "iterator()" method from Set,
	    // which is actually the intended (and expected) behavior for the programmer.
	    // Therefore the internals of the collection backing the relations will receive calls for cached methods
	    // (because here we invoke Cached methods and thus the wrapper RelationAwareSet is Cacheable), but the 
	    // iterator() method invokes the default behavior of a Cacheable RelationAwareSet, which is to use the 
	    // cache. If the framework preserves the relations as AbstractSets, there is nothing to be done here.
//	    assertTrue(checkSetsEqual(domainRoot.getTheAuthorsCached(forceMiss), domainRoot.getTheAuthors()));
//	    assertTrue(checkSetsEqual(domainRoot.getThePublishersCached(forceMiss), domainRoot.getThePublishers()));
	    
	    // Just to prove the internal implementation is correct, even though it is dubious this will ever 
	    // be exposed in this way, let's force the internals out
	    assertTrue(checkSetsEqualSpecialOne(((RelationMulValuesIndexedCacheableAwareSet<DomainRoot, Author>)domainRoot.getTheAuthorsCached(forceMiss)), domainRoot.getTheAuthors()));
	    assertTrue(checkSetsEqualSpecialTwo(((RelationAwareCacheableSet<DomainRoot, Publisher>)domainRoot.getThePublishersCached(forceMiss)), domainRoot.getThePublishers()));
	    assertTrue(checkSetsEqual(domainRoot.getTheBooksCached(forceMiss), domainRoot.getTheBooks()));
	}
    }
    
    private <T> boolean checkSetsEqual(Set<T> one, Set<T> other) {
	List<T> oneList = new ArrayList<T>();
	List<T> otherList = new ArrayList<T>();
	for (T t : one) {
	    oneList.add(t);
	}
	for (T t : other) {
	    otherList.add(t);
	}
	if (oneList.size() != otherList.size()) {
	    return false;
	}
	for (int i = 0; i < oneList.size(); i++) {
	    if (! oneList.get(i).equals(otherList.get(i))) {
		return false;
	    }
	}
	return true;
    }
    
    private boolean checkSetsEqualSpecialOne(RelationMulValuesIndexedCacheableAwareSet<DomainRoot, Author> one, Set<Author> other) {
	List<Author> oneList = new ArrayList<Author>();
	List<Author> otherList = new ArrayList<Author>();
	Iterator<Author> iter = one.iteratorCached(true);
	while (iter.hasNext()) {
	    oneList.add(iter.next());
	}
	for (Author t : other) {
	    otherList.add(t);
	}
	if (oneList.size() != otherList.size()) {
	    return false;
	}
	for (int i = 0; i < oneList.size(); i++) {
	    if (! oneList.get(i).equals(otherList.get(i))) {
		return false;
	    }
	}
	return true;
    }
    
    private boolean checkSetsEqualSpecialTwo(RelationAwareCacheableSet<DomainRoot, Publisher> one, Set<Publisher> other) {
	List<Publisher> oneList = new ArrayList<Publisher>();
	List<Publisher> otherList = new ArrayList<Publisher>();
	Iterator<Publisher> iter = one.iteratorCached(true);
	while (iter.hasNext()) {
	    oneList.add(iter.next());
	}
	for (Publisher t : other) {
	    otherList.add(t);
	}
	if (oneList.size() != otherList.size()) {
	    return false;
	}
	for (int i = 0; i < oneList.size(); i++) {
	    if (! oneList.get(i).equals(otherList.get(i))) {
		return false;
	    }
	}
	return true;
    }
 
}
