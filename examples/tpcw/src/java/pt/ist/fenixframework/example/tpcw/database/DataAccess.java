package pt.ist.fenixframework.example.tpcw.database;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Vector;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.Transaction;

import tpcw_dto.BuyConfirmResult;
import pt.ist.fenixframework.example.tpcw.domain.*;
import pt.ist.fenixframework.example.tpcw.domain.dto.*;

/** This class reimplements the public methods from TPCW_Database.  Generally it delegates the application logic to the domain
 * classes and it just transforms the output into the DTOs that the caller expects. */
public class DataAccess {

    private static Root rootObject = null;

    synchronized private static Root getRoot() {
	if (rootObject == null) {
	    rootObject = FenixFramework.getRoot();
	}
	return rootObject;
    }

    public static String[] getName(int c_id) {
	Customer customer = getRoot().getCustomer(c_id);
	
	String name[] = new String[2];
	name[0] = customer.getFname();
	name[1] = customer.getLname();
	return name;
    }

    public static BookDTO getBook(int i_id) {
	Book book = getRoot().getBook(i_id);
	if (book == null) {
	    return null;
	}
	return book.getDTO();
    }

    private static Vector<BookDTO> convertToBookDTO(List<Book> bookList) {
	Vector<BookDTO> bookVector = new Vector<BookDTO>(bookList.size());

	for (Book book : bookList) {
	    bookVector.add(book.getDTO());
	}
	return bookVector;
    }

    private static Vector<ShortBookDTO> convertToShortBookDTO(List<Book> bookList) {
	Vector<ShortBookDTO> bookVector = new Vector<ShortBookDTO>(bookList.size());

	for (Book book : bookList) {
	    bookVector.add(book.getShortBookDTO());
	}
	return bookVector;
    }

    public static CustomerDTO getCustomer(String UNAME){
	Customer customer = getRoot().getCustomer(UNAME);
	if (customer == null) {
	    return null;
	}
	return customer.getDTO();
    }

    public static Vector doSubjectSearch(String search_key) {
	List<Book> bookList = getRoot().getBooksBySubjectSortedByTitle(search_key, 50 /*limit*/);
	return convertToBookDTO(bookList);
    }

    public static Vector doTitleSearch(String search_key) {
	List<Book> bookList = getRoot().getBooksByTitleSortedByTitle(search_key, 50 /*limit*/);
	return convertToBookDTO(bookList);
    }

    public static Vector doAuthorSearch(String search_key) {
	List<Book> bookList = getRoot().getBooksByAuthorSortedByTitle(search_key, 50 /*limit*/);
	return convertToBookDTO(bookList);
    }

    public static Vector getNewProducts(String subject) {
	List<Book> bookList = getRoot().getBooksBySubjectSortedByPublicationDateAndTitle(subject, 50 /*limit*/);
	return convertToShortBookDTO(bookList);
    }

    public static Vector getBestSellers(String subject) {
	List<Book> bookList = getRoot().getBestSellers(subject,
						       getRoot().getNumOrderIds()-3333 /*recent orders*/,
						       50 /*limit*/);
	return convertToShortBookDTO(bookList);
    }

    public static void getRelated(int i_id, Vector i_id_vec, Vector i_thumbnail_vec) {
	Book book = getRoot().getBook(i_id);
	if (book == null) return;

	// Clear the vectors
	i_id_vec.removeAllElements();
	i_thumbnail_vec.removeAllElements();

	// Results
	i_id_vec.addElement(book.getRelatedTo1().getI_id());
	i_thumbnail_vec.addElement(book.getRelatedTo1().getThumbnail());
	i_id_vec.addElement(book.getRelatedTo2().getI_id());
	i_thumbnail_vec.addElement(book.getRelatedTo2().getThumbnail());
	i_id_vec.addElement(book.getRelatedTo3().getI_id());
	i_thumbnail_vec.addElement(book.getRelatedTo3().getThumbnail());
	i_id_vec.addElement(book.getRelatedTo4().getI_id());
	i_thumbnail_vec.addElement(book.getRelatedTo4().getThumbnail());
	i_id_vec.addElement(book.getRelatedTo5().getI_id());
	i_thumbnail_vec.addElement(book.getRelatedTo5().getThumbnail());
    }

    public static void adminUpdate(int i_id, double cost, String image, String thumbnail) {
	getRoot().updateBookInfo(i_id, cost, image, thumbnail);
    }

    public static String GetUserName(int C_ID){
	Customer customer = getRoot().getCustomer(C_ID);
	if (customer == null) {
	    return null;
	} else {
	    return customer.getUname();
	}
    }
	
    public static String GetPassword(String C_UNAME) {
	Customer customer = getRoot().getCustomer(C_UNAME);
	if (customer == null) {
	    return null;
	} else {
	    return customer.getPasswd();
	}
    }

    public static OrdersDTO GetMostRecentOrder(String c_uname, Vector order_lines){
	// find last order for a given customer
	Customer customer = getRoot().getCustomer(c_uname);
	if (customer == null) {
	    return null;
	}
	Orders order = customer.getMostRecentOrder();
	if (order == null) {
	    return null;
	}
	
	// set the order lines in the vector
	order_lines.clear();
	for (OrderLine orderLine : order.getOrderLines()) {
	    order_lines.add(orderLine.getDTO());
	}

	return order.getDTO();
    }

    // ********************** Shopping Cart code below ************************* 

    public static int createEmptyCart() {
	return getRoot().createEmptyCart().getSc_id();
    }

    public static CartDTO doCart(int SHOPPING_ID, Integer I_ID, Vector ids, Vector quantities) {
	CartDTO cartDTO = null;
	ShoppingCart cart = getRoot().getCart(SHOPPING_ID);
	
	if (cart == null) {
	    System.err.println("ERROR: Shopping cart with id = " + SHOPPING_ID + " does not exist.");
	    return null;
	}
	if (I_ID != null) {
	    addItem(cart, I_ID.intValue());
	}
	refreshCart(cart, ids, quantities);
	cart.addRandomItemToCartIfNecessary();
	cart.resetTime();

	return cart.getDTO(0.0);
    }

    public static void addItem(ShoppingCart cart, int I_ID) {
	Book book = getRoot().getBook(I_ID);
	cart.addItem(book);
    }

    public static void refreshCart(ShoppingCart cart, Vector ids, Vector quantities) {
	for(int i = 0; i < ids.size(); i++){
	    String I_IDstr = (String) ids.elementAt(i);
	    String QTYstr = (String) quantities.elementAt(i);
	    int QTY = Integer.parseInt(QTYstr);
	    ShoppingCartLine scl = cart.getShoppingCartLine(Integer.parseInt(I_IDstr));
	    if (scl == null) {
		System.err.println("ERROR: Didn't expect a null shopping cart line!");
		continue;
	    }

	    if(QTY == 0) { // We need to remove the item from the cart
		cart.removeShoppingCartLines(scl);
	    } else { // we update the quantity
		scl.setQty(QTY);
	    }
	}
    }

    public static void addRandomItemToCartIfNecessary(ShoppingCart cart) {
	cart.addRandomItemToCartIfNecessary();
    }

    public static void resetCartTime(ShoppingCart cart) {
	cart.resetTime();
    }

    /* Return a CartDTO given a shopping cart id */
    public static CartDTO getCartDTO(int SHOPPING_ID, double c_discount) {
	ShoppingCart cart = getRoot().getCart(SHOPPING_ID);
	if (cart == null) {
	    return null;
	}
	return getCartDTO(cart, c_discount);
    }

    /* Return a CartDTO given a domain ShoppingCart */
    public static CartDTO getCartDTO(ShoppingCart cart, double c_discount) {
	if (cart == null) {
	    return null;
	}
	return cart.getDTO(c_discount);
    }

    public static BuyConfirmResult doBuyConfirm(int shopping_id, int customer_id, String cc_type, long cc_number,
						String cc_name, Date cc_expiry, String shipping) {
	BuyConfirmResult result = new BuyConfirmResult();
	Customer customer = getRoot().getCustomer(customer_id);
	if (customer == null) {
	    System.err.println("Error: Customer is not supposed to be null (1)");
	    return result;
	}
	//getCDiscount
	double discount = getCDiscount(customer);
	//getCart
	ShoppingCart sc = getRoot().getCart(shopping_id);
	result.cart = getCartDTO(sc, discount);
	//getCAddr
	Address shipAddress = getCAddr(customer);
	//enterOrder
	Orders order = enterOrder(customer, sc, result.cart.SC_SUB_TOTAL, result.cart.SC_TOTAL, shipAddress, shipping, discount);
	result.order_id = order.getO_id();
	//enterCCXact
	enterCCXact(order, cc_type, cc_number, cc_name, cc_expiry, result.cart.SC_TOTAL, shipAddress);
	//clearCart
	clearCart(sc);
	return result;
    }

    public static BuyConfirmResult doBuyConfirm(int shopping_id, int customer_id, String cc_type, long cc_number, String cc_name,
						Date cc_expiry, String shipping, String street_1, String street_2, String city,
						String state, String zip, String country) {
	BuyConfirmResult result = new BuyConfirmResult();
	Customer customer = getRoot().getCustomer(customer_id);
	if (customer == null) {
	    System.err.println("Error: Customer is not supposed to be null (2)");
	    return result;
	}
	//getCDiscount
	double discount = getCDiscount(customer);
	//getCart
	ShoppingCart sc = getRoot().getCart(shopping_id);
	result.cart = getCartDTO(sc, discount);
	//enterAddress
	Address shipAddress = enterAddress(street_1, street_2, city, state, zip, country);
	//enterOrder
	Orders order = enterOrder(customer, sc, result.cart.SC_SUB_TOTAL, result.cart.SC_TOTAL, shipAddress, shipping, discount);
	result.order_id = order.getO_id();
	//enterCCXact
	enterCCXact(order, cc_type, cc_number, cc_name, cc_expiry, result.cart.SC_TOTAL, shipAddress);
	//clearCart
	clearCart(sc);
	return result;
    }

    public static double getCDiscount(Customer customer) {
	if (customer == null) {
	    return 0.0;
	}
	return customer.getDiscount();
    }

    public static int getCAddrID(Customer customer) {
	if (customer != null && customer.getAddress() != null) {
	    return customer.getAddress().getAddr_id();
	} else {
	    return 0;
	}
    }

    public static Address getCAddr(Customer customer) {
	if (customer != null) {
	    return customer.getAddress();
	} else {
	    return null;
	}
    }

    public static void addOrderLine(int ol_id, Orders order, Book book,
				     int ol_qty, double ol_discount, String ol_comment) {
	if (order == null || book == null) {
	    return;
	}
	order.addNewOrderLine(ol_id, book, ol_qty, ol_discount, ol_comment);
    }

    public static int getStock(Book book) {
	if (book == null) {
	    return 0;
	}
	return book.getStock();
    }

    public static void enterCCXact(Orders order, String cc_type, long cc_number, String cc_name, Date cc_expiry, double total,
				   Address shipAddress) {
	if(cc_type.length() > 10) {
	    cc_type = cc_type.substring(0,10);
	}
	if(cc_name.length() > 30) {
	    cc_name = cc_name.substring(0,30);
	}

	Timestamp shipDate = new Timestamp(System.currentTimeMillis());
	shipDate.setNanos(0);
	order.setCcXact(new CCXact(cc_type, cc_number, cc_name, cc_expiry, total, shipDate, order, shipAddress.getCountry()));
    }

    public static void clearCart(ShoppingCart cart) {
	cart.getShoppingCartLines().clear();
    }

    public static Address enterAddress(String street1, String street2, String city, String state, String zip, String country) {
	return getRoot().findOrCreateAddress(street1, street2, city, state, zip, country);
    }

    public static Orders enterOrder(Customer customer, ShoppingCart cart, double subTotal, double total, Address shipAddr, String shipping, double c_discount) {
	return getRoot().makeNewOrder(customer, cart, subTotal, total, shipAddr, shipping, c_discount);
    }

    public static void setStock(Book book, int new_stock) {
	if (book == null) {
	    return;
	}
	book.setStock(new_stock);
    }
    
    public static void refreshSession(int C_ID) {
	Customer customer = getRoot().getCustomer(C_ID);
	if (customer == null) {
	    System.err.println("Error: customer should exist in refreshSession");
	    return;
	}
	customer.refreshSession();
    }

    public static CustomerDTO createNewCustomer(tpcw_dto.Customer customer) {
	return getRoot().makeNewCustomer(customer).getDTO();
    }
}
