package test;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
import pt.ist.fenixframework.dml.runtime.RelationAwareSet;

@RunWith(JUnit4.class)
public class BooksTest {

    private static final Logger logger = LoggerFactory.getLogger(BooksTest.class);

    @AfterClass
    public static void shutdown() {
        FenixFramework.shutdown();
    }

    public static final int NUMBER_ELEMENTS = 600;
    public static final int DIVIDE_RATIO = 10;
    public static final int KEY_ONE = (NUMBER_ELEMENTS / 2) % (NUMBER_ELEMENTS / DIVIDE_RATIO);
    public static final int KEY_TWO = ((NUMBER_ELEMENTS / 2) - 1) % (NUMBER_ELEMENTS / DIVIDE_RATIO);
    public static final int KEY_THREE = NUMBER_ELEMENTS * 2;
    public static final int KEY_FOUR = NUMBER_ELEMENTS * 3;
    
    /*
     * The following test fails for B+Trees when the NUMBER_ELEMENTS > (LOWER_BOUND * 2 + 1)
     * This problem was reported on Issue #50 in FF repo.
     */
//    @Test
//    @Atomic
//    public void test00() {
//	DomainRoot domainRoot = FenixFramework.getDomainRoot();
//	
//	assertTrue(domainRoot.getTheBooks().size() == NUMBER_ELEMENTS);
//	assertTrue(domainRoot.getTheAuthors().size() == NUMBER_ELEMENTS);
//	assertTrue(domainRoot.getThePublishers().size() == NUMBER_ELEMENTS);
//	
//        for (Book book : domainRoot.getTheBooks()) {
//            domainRoot.removeTheBooks(book);
//        }
//
//        for (Author author : domainRoot.getTheAuthors()) {
//            domainRoot.removeTheAuthors(author);
//        }
//
//        for (Publisher publisher : domainRoot.getThePublishers()) {
//            domainRoot.removeThePublishers(publisher);
//        }
//        
//        System.out.println("Books: " + domainRoot.getTheBooks().size());
//        System.out.println("Authors: " + domainRoot.getTheAuthors().size());
//        System.out.println("Publishers: " + domainRoot.getThePublishers().size());
//        
//	assertTrue(domainRoot.getTheBooks().size() == 0);
//	assertTrue(domainRoot.getTheAuthors().size() == 0);
//	assertTrue(domainRoot.getThePublishers().size() == 0);
//        
//    }
    
    @Test
    @Atomic
    public void test01() {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
	Set<Book> books = domainRoot.getTheBooks();
	Set<Author> authors = domainRoot.getTheAuthors();
	Set<Publisher> publishers = domainRoot.getThePublishers();
	
	assertTrue(books.size() == NUMBER_ELEMENTS);
	assertTrue(authors.size() == NUMBER_ELEMENTS);
	assertTrue(publishers.size() == NUMBER_ELEMENTS);
	
	int[] countBook = initArrayCount();
	for (Book book : books) {
	    countBook[book.getId()] = countBook[book.getId()] + 1;
	}
	
	int[] countAuthor = initArrayCount();
	int[] countAuthorSecondary = initArrayCount();
	for (Author author : authors) {
	    countAuthor[author.getId()] = countAuthor[author.getId()] + 1;
	    countAuthorSecondary[author.getAge()] = countAuthorSecondary[author.getAge()] + 1;
	}
	
	int[] countPublishers = initArrayCount();
	for (Publisher publisher : publishers) {
	    countPublishers[publisher.getId()] = countPublishers[publisher.getId()] + 1;
	}
	
	checkArrayCount(countBook);
	checkArrayCount(countAuthorSecondary);
	checkArrayCountClashes(countAuthor);
	checkArrayCount(countPublishers);
    }
    
    @Test
    public void test02() {
	int [] ids = test02part1();
	test02part2(ids);
    }
    
    @Atomic
    public int[] test02part1() {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
	Book book = domainRoot.getTheBooksById(NUMBER_ELEMENTS / DIVIDE_RATIO);
	domainRoot.removeTheBooks(book);
	
	Publisher publisher = getPublisherById(0);
	domainRoot.removeThePublishers(publisher);
	
	Set<Author> authors = domainRoot.getTheAuthorsById(KEY_ONE);
	Author author = authors.iterator().next();
	domainRoot.removeTheAuthors(author);
	
	authors = domainRoot.getTheAuthorsById(KEY_TWO);
	
	int[] result = new int[2 + authors.size()];
	result[0] = publisher.getId();
	result[1] = author.getAge();
	
	Set<Author> copyAuthors = new HashSet<Author>();
	for (Author a : authors) {
	    copyAuthors.add(a);
	}
	int upperBound = 2 + copyAuthors.size();
	
	Iterator<Author> iter = copyAuthors.iterator();
	for (int i = 2; i < upperBound; i++) {
	    author = iter.next();
	    result[i] = author.getAge();
	    domainRoot.removeTheAuthors(author);
	}
	
	return result;
    }
    
    @Atomic
    public void test02part2(int[] ids) {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
	assertTrue(domainRoot.getTheBooksById(NUMBER_ELEMENTS / DIVIDE_RATIO) == null);
	assertTrue(getBookById(NUMBER_ELEMENTS / DIVIDE_RATIO) == null);
	
	Publisher publisher = getPublisherById(ids[0]);
	assertTrue(publisher == null);
	
	Set<Author> authors = domainRoot.getTheAuthorsById(KEY_ONE);
	assertTrue(authors.size() == (DIVIDE_RATIO - 1));
	for (Author author : authors) {
	    assertTrue(author.getId() == KEY_ONE);
	    assertTrue(author.getAge() != ids[1]);
	}
	assertTrue(getAuthorByIdAndAge(KEY_ONE, ids[1]) == null);
	
	authors = domainRoot.getTheAuthorsById(KEY_TWO);
	assertTrue(authors.size() == 0);
	for (int i = 2; i < ids.length; i++) {
	    assertTrue(getAuthorByIdAndAge(KEY_TWO, ids[i]) == null);
	}
    }

    @Test
    public void test03() {
	test03part01();
	test03part02();
    }
    
    @Atomic
    public void test03part01() {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
	domainRoot.addTheBooks(new Book(KEY_THREE, KEY_THREE));
	domainRoot.addTheBooks(domainRoot.getTheBooksById(KEY_TWO + 1));
	
	domainRoot.addTheAuthors(new Author(KEY_THREE, NUMBER_ELEMENTS * 10));
	Iterator<Author> iter = domainRoot.getTheAuthorsById(KEY_TWO - 4).iterator();
	Author duplicate = null;
	for (int i = 0; i < DIVIDE_RATIO / 2; i++) {
	    duplicate = iter.next();
	}
	domainRoot.addTheAuthors(duplicate);
	
	domainRoot.addThePublishers(new Publisher(KEY_THREE));
	domainRoot.addThePublishers(getPublisherById(KEY_TWO + 1));
    }
    
    @Atomic
    public void test03part02() {
	DomainRoot domainRoot = FenixFramework.getDomainRoot();
	
	assertTrue(domainRoot.getTheBooksById(KEY_THREE).getId() == KEY_THREE);
	assertTrue(domainRoot.getTheBooks().size() == (NUMBER_ELEMENTS + 1));
	
	assertTrue(domainRoot.getTheAuthorsById(KEY_THREE).contains(getAuthorByIdAndAge(KEY_THREE, NUMBER_ELEMENTS * 10)));
	assertTrue(domainRoot.getTheAuthors().size() == (NUMBER_ELEMENTS + 1));
	
	assertTrue(domainRoot.getThePublishers().contains(getPublisherById(KEY_THREE)));
	assertTrue(domainRoot.getThePublishers().size() == (NUMBER_ELEMENTS + 1));
    }
    
    private void checkArrayCount(int[] arrayCount) {
	for (int i = 0; i < arrayCount.length; i++) {
	    assertTrue(arrayCount[i] == 1);
	}
    }
    
    private void checkArrayCountClashes(int[] arrayCount) {
	for (int i = 0; i < (NUMBER_ELEMENTS / DIVIDE_RATIO); i++) {
	    assertTrue(arrayCount[i] == DIVIDE_RATIO);
	}
    }    
    
    private int[] initArrayCount() {
	int[] count = new int[NUMBER_ELEMENTS];
	for (int i = 0; i < count.length; i++) {
	    count[i] = 0;
	}
	return count;
    }

    @Before
    @Atomic
    public void init() {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        
        for (int i = 0; i < NUMBER_ELEMENTS; i++) {
            domainRoot.addTheBooks(new Book(i, i));
            domainRoot.addTheAuthors(new Author(i % (NUMBER_ELEMENTS / DIVIDE_RATIO), i));
            domainRoot.addThePublishers(new Publisher(i));
        }
    }

    @After
    @Atomic
    public void reset() {
	
        DomainRoot domainRoot = FenixFramework.getDomainRoot();

        Set<Book> copyBooks = new HashSet<Book>();
        for (Book book : domainRoot.getTheBooks()) { 
            copyBooks.add(book);
        }
        
        Set<Author> copyAuthors = new HashSet<Author>();
        for (Author author : domainRoot.getTheAuthors()) { 
            copyAuthors.add(author);
        }
        
        Set<Publisher> copyPublishers = new HashSet<Publisher>();
        for (Publisher publisher : domainRoot.getThePublishers()) {
            copyPublishers.add(publisher);
        }
        
        for (Book book : copyBooks) {
            domainRoot.removeTheBooks(book);
        }
        
        for (Author author : copyAuthors) {
            domainRoot.removeTheAuthors(author);
        }
        
        for (Publisher publisher : copyPublishers) {
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
    public static Author getAuthorById(int id) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Author author : domainRoot.getTheAuthors()) {
            if (author.getId() == id) {
                return author;
            }
        }
        return null;
    }
    
    @Atomic
    public static Author getAuthorByIdAndAge(int id, int age) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Author author : domainRoot.getTheAuthors()) {
            if (author.getAge() == age && author.getId() == id) {
                return author;
            }
        }
        return null;
    }

    @Atomic
    public static Book getBookById(int id) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Book book : domainRoot.getTheBooks()) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    @Atomic
    public static Publisher getPublisherById(int id) {
        DomainRoot domainRoot = FenixFramework.getDomainRoot();
        for (Publisher publisher : domainRoot.getThePublishers()) {
            if (publisher.getId() == id) {
                return publisher;
            }
        }
        return null;
    }
}
