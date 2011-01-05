package pt.ist.fenixframework.example.tpcw.domain;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import pt.ist.fenixframework.example.tpcw.Soundex;

import pt.ist.fenixframework.example.tpcw.domain.dto.BookComparatorByDateDescendingAndTitle;
import pt.ist.fenixframework.example.tpcw.domain.dto.BookComparatorByOrderQuantityDesc;
import pt.ist.fenixframework.example.tpcw.domain.dto.BookComparatorByTitle;

public class Root extends Root_Base {
    public static long MILLIS_PER_DAY = 3600*24*1000;
    
    private static BookComparatorByDateDescendingAndTitle BOOK_COMPARATOR_BY_DATE_DESC_AND_TITLE = new BookComparatorByDateDescendingAndTitle();
    private static BookComparatorByOrderQuantityDesc BOOK_COMPARATOR_BY_ORDER_QUANTITY_DESC = new BookComparatorByOrderQuantityDesc();
    private static BookComparatorByTitle BOOK_COMPARATOR_BY_TITLE = new BookComparatorByTitle();

    public  Root() {
        super();
    }

    /*************************************************************************************/
    // Defined in TPC-W Spec Clause 4.6.2.8
    private static final String [] digS = {
	"BA","OG","AL","RI","RE","SE","AT","UL","IN","NG"
    };

    private static int getRandom(int i) {  // Returns integer 1, 2, 3 ... i
	return ((int) (java.lang.Math.random() * i)+1);
    }

    //Not very random function. If called in swift sucession, it will
    //return the same string because the system time used to seed the
    //random number generator won't change. 
    private static String getRandomString(int min, int max){
	String newstring = new String();
	Random rand = new Random();
	int i;
	final char[] chars = {'a','b','c','d','e','f','g','h','i','j','k',
			      'l','m','n','o','p','q','r','s','t','u','v',
			      'w','x','y','z','A','B','C','D','E','F','G',
			      'H','I','J','K','L','M','N','O','P','Q','R',
			      'S','T','U','V','W','X','Y','Z','!','@','#',
			      '$','%','^','&','*','(',')','_','-','=','+',
			      '{','}','[',']','|',':',';',',','.','?','/',
			      '~',' '}; //79 characters
	int strlen = (int) Math.floor(rand.nextDouble()*(max-min+1));
	strlen += min;
	for(i = 0; i < strlen; i++){
	    char c = chars[(int) Math.floor(rand.nextDouble()*79)];
	    newstring = newstring.concat(String.valueOf(c));
	}
	return newstring;
    }

    private static String DigSyl(int d, int n)
    {
	String s = "";
	
	if (n==0) return(DigSyl(d));	
	for (;n>0;n--) {
	    int c = d % 10;
	    s = digS[c]+s;
	    d = d /10;
	}
	
	return(s);
    }
    
    private static String DigSyl(int d)
    {
	String s = "";
	
	for (;d!=0;d=d/10) {
	    int c = d % 10;
	    s = digS[c]+s;      
	}
	
	return(s);
    }

    /*************************************************************************************/

    public Customer getCustomer(String username) {
	for (Customer customer : getCustomers()) {
	    String localUsername = customer.getUname();
	    if ((localUsername == username) || (localUsername != null && localUsername.equals(username))) {
		return customer;
	    }
	}
	return null;
    }

    public Customer getCustomer(int c_id) {
	for (Customer customer : getCustomers()) {
	    if (customer.getC_id() == c_id) {
		return customer;
	    }
	}
	return null;
    }

    private List<Book> getBooksBySubject(String subject) {
	List<Book> books = new ArrayList<Book>();

	// filter subject
	for (Book book : getBooks()) {
	    String localSubject = book.getSubject();
	    if ((localSubject == subject) || (localSubject != null && localSubject.equals(subject))) {
		books.add(book);
	    }
	}
	return books;
    }

    private List<Book> getBooksByTitle(String title) {
	List<Book> books = new ArrayList<Book>();

	// filter title
	for (Book book : getBooks()) {
	    String localTitle = book.getTitle();
	    if ((localTitle == title) || (localTitle != null && localTitle.equals(title))) {
		books.add(book);
	    }
	}
	return books;
    }

    private List<Book> getBooksByAuthorMatches(String lname) {
	List<Book> books = new ArrayList<Book>();

	// filter author by soundex(lastname)
	for (Author author : getAuthors()) {
	    String localLname = author.getLname();
	    if ((localLname == lname)
		|| (localLname != null && lname != null
		    && Soundex.soundex(localLname).equals(Soundex.soundex(lname)))) {
		books.addAll(author.getBooks());
	    }
	}
	return books;
    }

    public List<Book> getBooksBySubjectSortedByTitle(String search_key, int limit) {
	List<Book> books = this.getBooksBySubject(search_key);
	// sort
	Collections.sort(books, BOOK_COMPARATOR_BY_TITLE);
	// limit result
 	return books.subList(0, Math.min(limit, books.size()));
    }

    public List<Book> getBooksByTitleSortedByTitle(String search_key, int limit) {
	List<Book> books = this.getBooksByTitle(search_key);
	// sort
	Collections.sort(books, BOOK_COMPARATOR_BY_TITLE);
	// limit result
 	return books.subList(0, Math.min(limit, books.size()));
    }

    public List<Book> getBooksByAuthorSortedByTitle(String search_key, int limit) {
	List<Book> books = this.getBooksByAuthorMatches(search_key);
	// sort
	Collections.sort(books, BOOK_COMPARATOR_BY_TITLE);
	// limit result
 	return books.subList(0, Math.min(limit, books.size()));
    }

    public List<Book> getBooksBySubjectSortedByPublicationDateAndTitle(String subject, int limit) {
	List<Book> books = this.getBooksBySubject(subject);
	// sort
	Collections.sort(books, BOOK_COMPARATOR_BY_DATE_DESC_AND_TITLE);
	// limit result
	return books.subList(0, Math.min(limit, books.size()));
    }

    public Book getBook(int i_id) {
	for (Book book : getBooks()) {
	    if (book.getI_id() == i_id) {
		return book;
	    }
	}
	return null;
    }

    public Book getRandomBook() {
	List<Book> books = getBooks();
	Random rand = new Random();
	Double temp = new Double(Math.floor(rand.nextFloat() * books.size()));
	return books.get(temp.intValue());
    }

    /** Return a limited list of most ordered books for a given subject in recent orders */
    public List<Book> getBestSellers(String subject, int limitOrderId, int limit) {
	Set<Map.Entry<Book,Integer>> entries = calculateRecentOrdersPerBook(subject, limitOrderId).entrySet();
	TreeSet<Map.Entry<Book,Integer>> sortedEntries = new TreeSet<Map.Entry<Book,Integer>>(BOOK_COMPARATOR_BY_ORDER_QUANTITY_DESC);
	sortedEntries.addAll(entries);

	List<Book> bestSellers = new ArrayList<Book>(limit);
	Iterator<Map.Entry<Book,Integer>> it = sortedEntries.iterator();
	for (int counter = 0; counter < limit && it.hasNext(); counter++) {
	    Map.Entry<Book,Integer> entry = it.next();
	    bestSellers.add(entry.getKey());
	}
	return bestSellers;
    }

    /** Return the total amount of order for books of a given subject, for orders more recent that a given limitOrderId */
    private Map<Book,Integer> calculateRecentOrdersPerBook(String subject, int limitOrderId) {
	Map<Book,Integer> ordersPerBook = new HashMap<Book,Integer>();

	// go through all books
	for (Book book : getBooks()) {
	    // filter subject
	    String localSubject = book.getSubject();
	    if ((localSubject == subject) || (localSubject != null && localSubject.equals(subject))) {
		for (OrderLine orderLine : book.getOrderLines()) {
		    // filter order
		    if (orderLine.getOrder().getO_id() > limitOrderId) {
			addBookQuantitytoMap(ordersPerBook, book, orderLine.getQty());
		    }
		}
	    }
	}
    	return ordersPerBook;
    }

    private void addBookQuantitytoMap(Map<Book,Integer> booksMap, Book book, Integer quantity) {
	Integer currentQuantity = booksMap.get(book);
	if (currentQuantity == null) {
	    currentQuantity = quantity;
	} else {
	    currentQuantity += quantity;
	}
	booksMap.put(book, currentQuantity);
    }

    public ShoppingCart createEmptyCart() {
	int id = this.getNumCartIds();
	this.setNumCartIds(id + 1);

	ShoppingCart sc = new ShoppingCart(id);
 	this.addShoppingCarts(sc);
	return sc;
    }

    public ShoppingCart getCart(int sc_id) {
	for (ShoppingCart shoppingCart : getShoppingCarts()) {
	    if (shoppingCart.getSc_id() == sc_id) {
		return shoppingCart;
	    }
	}
	return null;
    }

    public Orders getOrder(int o_id) {
	for (Orders order : getOrderss()) {
	    if (order.getO_id() == o_id) {
		return order;
	    }
	}
	return null;
    }

    public Orders getOrder(String c_uname) {
	for (Orders order : getOrderss()) {
	    Customer customer = order.getCustomer();
	    if (customer != null && c_uname.equals(customer.getUname())) {
		return order;
	    }
	}
	return null;
    }

    private Country getCountry(String countryName) {
	for (Country country : getCountries()) {
	    String localName = country.getName();
	    if (localName == countryName || (localName != null && localName.equals(countryName))) {
		return country;
	    }
	}
	return null;
    }

    public Address findOrCreateAddress(String street1, String street2, String city, String state, String zip, String countryName) {
 	Country country = getCountry(countryName);
	if (country == null) {
	    System.err.println("ERROR: according to the original implementation the country should always exist!");
	    return null;
	}

	return country.findOrCreateAddress(street1, street2, city, state, zip);
    }

    public Orders makeNewOrder(Customer customer, ShoppingCart cart, double subTotal, double total, Address shipAddr, String shipping, double c_discount) {
	long now = System.currentTimeMillis();
	Timestamp orderTime = new Timestamp(now);
	orderTime.setNanos(0);
	Timestamp shipTime = new Timestamp(orderTime.getTime() + getRandom(7)*MILLIS_PER_DAY);
	shipTime.setNanos(0);

	int o_id = this.getNumOrderIds() + 1;
	this.setNumOrderIds(o_id);

	Orders order = new Orders(orderTime, subTotal, 8.25, total, shipping, shipTime, "Pending", customer.getAddress(), shipAddr, customer, o_id);
	this.addOrderss(order);

	
	for (ShoppingCartLine scl : cart.getShoppingCartLines()) {
	    Book book = scl.getBook();
	    // create order line
	    order.addNewOrderLine(0, book, scl.getQty(), c_discount, getRandomString(20, 100));

	    // update stock
	    int newStock = book.getStock() - scl.getQty();
	    if ((newStock) < 10) {
		newStock += 21;
	    }
	    book.setStock(newStock);
	}
	return order;
    }

    public Customer makeNewCustomer(tpcw_dto.Customer cust) {
	// get new customer id
	int c_id = this.getNumCustomerIds() + 1;
	this.setNumCustomerIds(c_id);
	// get the username
	String uname = this.DigSyl(c_id, 0);
	// get the current timestamp
	Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
	currentTimestamp.setNanos(0);
	// get the current date
	java.sql.Date currentDate = new Date(0);
	currentDate.setTime(currentTimestamp.getTime());
	// get the address
	Address address = findOrCreateAddress(cust.addr_street1, cust.addr_street2, cust.addr_city,
			    cust.addr_state, cust.addr_zip, cust.co_name);

	Customer customer = new Customer(uname, uname.toLowerCase(), cust.c_fname, cust.c_lname, cust.c_phone,
					 cust.c_email, currentDate, currentDate, currentTimestamp, currentTimestamp,
					 (int) (java.lang.Math.random() * 51), 0.0, 0.0,
					 new Date(cust.c_birthdate.getTime()), cust.c_data, address, c_id);
	this.addCustomers(customer);
	return customer;
    }

    public void updateBookInfo(int i_id, double cost, String image, String thumbnail) {
	Book book = getBook(i_id);
	if (book == null) {
	    return;
	}
	book.setCost(cost);
	book.setImage(image);
	book.setThumbnail(thumbnail);
	// Now, for the more complex part of the logic: update the related books

	// first, get the customers of the 10000 most recent orders
	Set<Customer> recentCustomers = getRecentCustomersForBook(10000, book);
	// second, get the total amount ordered for each book that these clients ever ordered
	Map<Book,Integer> quantities = new HashMap<Book,Integer>();
	for (Customer customer : recentCustomers) {
	    customer.updateQuantityOfBooksOrdered(quantities);
	}
	// now order the books by amount ordered
	TreeSet<Map.Entry<Book,Integer>> sortedEntries = new TreeSet<Map.Entry<Book,Integer>>(BOOK_COMPARATOR_BY_ORDER_QUANTITY_DESC);
	sortedEntries.addAll(quantities.entrySet());
	// and pick the top 5, ignoring the current book to which we want to relate to
	List<Book> relatedBooks = new ArrayList<Book>(5);
	int counter = 0;
	Iterator<Map.Entry<Book,Integer>> it = sortedEntries.iterator();
	while (counter < 5 && it.hasNext()) {
	    Book relatedBook = it.next().getKey();
	    if (book != relatedBook) {
		relatedBooks.add(relatedBook);
		counter++;
	    }
	}
	// if the top 5 list is not full, fill it with random books
	while (counter < 5) {
	    Book randomBook = getRandomBook();
	    if (book != randomBook) {
		relatedBooks.add(randomBook);
		counter++;
	    }
	}
	// finally, set the five books in the related books of this book
	book.setRelatedTo1(relatedBooks.get(0));
	book.setRelatedTo2(relatedBooks.get(1));
	book.setRelatedTo3(relatedBooks.get(2));
	book.setRelatedTo4(relatedBooks.get(3));
	book.setRelatedTo5(relatedBooks.get(4));
    }

    private Set<Customer> getRecentCustomersForBook(int lastNOrders, Book book) {
	Set<Customer> result = new HashSet<Customer>();

	int pastOrderId = getNumOrderIds()-lastNOrders;
	for (Orders order : getOrderss()) {
	    if (order.getO_id() > pastOrderId
		&& order.contains(book)) {
		result.add(order.getCustomer());
	    }
	}
	return result;
    }
}
