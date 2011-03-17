/*
 * TPCW_Populate.java - database population program
 *------------------------------------------------------------------------
 *
 * This is part of the the Java TPC-W distribution,
 * written by Harold Cain, Tim Heil, Milo Martin, Eric Weglarz, and Todd
 * Bezenek.  University of Wisconsin - Madison, Computer Sciences
 * Dept. and Dept. of Electrical and Computer Engineering, as a part of
 * Prof. Mikko Lipasti's Fall 1999 ECE 902 course.
 *
 * Copyright (C) 1999, 2000 by Harold Cain, Timothy Heil, Milo Martin, 
 *                             Eric Weglarz, Todd Bezenek.
 *
 * This source code is distributed "as is" in the hope that it will be
 * useful.  It comes with no warranty, and no author or distributor
 * accepts any responsibility for the consequences of its use.
 *
 * Everyone is granted permission to copy, modify and redistribute
 * this code under the following conditions:
 *
 * This code is distributed for non-commercial use only.
 * Please contact the maintainer for restrictions applying to 
 * commercial use of these tools.
 *
 * Permission is granted to anyone to make or distribute copies
 * of this code, either as received or modified, in any
 * medium, provided that all copyright notices, permission and
 * nonwarranty notices are preserved, and that the distributor
 * grants the recipient permission for further redistribution as
 * permitted by this document.
 *
 * Permission is granted to distribute this code in compiled
 * or executable form under the same conditions that apply for
 * source code, provided that either:
 *
 * A. it is accompanied by the corresponding machine-readable
 *    source code,
 * B. it is accompanied by a written offer, with no time limit,
 *    to give anyone a machine-readable copy of the corresponding
 *    source code in return for reimbursement of the cost of
 *    distribution.  This written offer must permit verbatim
 *    duplication by anyone, or
 * C. it is distributed by someone who received only the
 *    executable form, and is accompanied by a copy of the
 *    written offer of source code that they received concurrently.
 *
 * In other words, you are welcome to use, share and improve this codes.
 * You are forbidden to forbid anyone else to use, share and improve what
 * you give them.
 *
 ************************************************************************
 *
 * Changed 2003 by Jan Kiefer.
 * 
 * Changed 2008 by Joao Pereira
 * Changed 2010 by Sergio Miguel Fernandes
 ************************************************************************/

package pt.ist.fenixframework.example.tpcw.populate;

//CAVEAT:
//These TPCW DB Population routines stray from the TPCW Spec in the 
//following ways:
//1. The a_lname field in the AUTHOR table is not generated using the DBGEN
//   utility, because of the current unavailability of this utility.
//2. Ditto for the I_TITLE field of the ITEM table.

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;
import java.lang.Math.*;

import jvstm.TransactionalCommand;

import pt.ist.fenixframework.Config;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.AbstractDomainObject;
import pt.ist.fenixframework.pstm.Transaction;
import pt.ist.fenixframework.pstm.VersionNotAvailableException;

import pt.ist.fenixframework.example.tpcw.domain.Author;
import pt.ist.fenixframework.example.tpcw.domain.Book;
import pt.ist.fenixframework.example.tpcw.domain.Country;
import pt.ist.fenixframework.example.tpcw.domain.Address;
import pt.ist.fenixframework.example.tpcw.domain.Customer;
import pt.ist.fenixframework.example.tpcw.domain.Orders;
import pt.ist.fenixframework.example.tpcw.domain.OrderLine;
import pt.ist.fenixframework.example.tpcw.domain.Root;
import pt.ist.fenixframework.example.tpcw.domain.CCXact;

class TPCW_Populate {
    private static Random rand;
  
    //These variables are dependent on the JDBC database driver used.
//     private static final String driverName = "org.gjt.mm.mysql.Driver";
//     private static final String dbName = "jdbc:mysql://localhost/tpcw?user=tpcw&password=tpcw";

    // NUMBER of domain objects created per transaction
    private static final int NUMBER_OBJECTS_TRANSACTION = 1000;
    //   private static final int NUMBER_OBJECTS_TRANSACTION = 1000000;
  
    //ATTENTION: The NUM_EBS and NUM_ITEMS variables are the only variables
    //that should be modified in order to rescale the DB.
    private static /* final */ int NUM_EBS = 10;
    private static /* final */ int NUM_ITEMS = 1000;

    private static /* final */ int NUM_CUSTOMERS = NUM_EBS * 2880;
    private static /* final */ int NUM_ADDRESSES = 2 * NUM_CUSTOMERS;
    private static /* final */ int NUM_AUTHORS = (int) (.25 * NUM_ITEMS);
    private static /* final */ int NUM_ORDERS = (int) (.9 * NUM_CUSTOMERS);
    private static /* final */ int NUM_COUNTRIES = 92; // this is constant. Never changes!


    // need to store all created objects for some persistent classes since
    // they are needed for other objects of other persistent classes
    // we actually only store the OID to allow memory to be collected
    private static int _counter = 0; // maintains the number of previous created objects for each class
    private static Long [] _authors;
    private static Long [] _books;
    private static Long [] _countries;
    private static Long [] _addresses;
    private static Long [] _customers;

    public static void main(String[] args){
 	final String paramDbUrl = "//localhost:3306/";
	final String paramDbName = "tpcwFenix";
	final String paramDbUsername = "tpcw";
	final String paramDbPassword = "tpcw";
	dropCreateDatabase(paramDbUrl, paramDbName, paramDbUsername, paramDbPassword);

	NUM_EBS = Integer.parseInt(args[0]);
	NUM_ITEMS = Integer.parseInt(args[1]);
    
	System.out.println("NUM_EBS = " + NUM_EBS);
	System.out.println("NUM_ITEMS = " + NUM_ITEMS);
    
	NUM_CUSTOMERS = NUM_EBS * 2880;
	NUM_ADDRESSES = 2 * NUM_CUSTOMERS;
	NUM_AUTHORS = (int) (.25 * NUM_ITEMS);
	NUM_ORDERS = (int) (.9 * NUM_CUSTOMERS);

	_authors = new Long[NUM_ITEMS/4];
	_books = new Long[NUM_ITEMS];
	_countries = new Long[NUM_COUNTRIES];
	_addresses = new Long[NUM_ADDRESSES];
	_customers = new Long[NUM_CUSTOMERS];

	System.out.println("Beginning TPCW Database population.");
    
	rand = new Random();
    
	Config config = new Config() {{
	    domainModelPath = "/tpcw.dml";
// 	    dbAlias = "//localhost:3306/tpcwFenix";
// 	    dbUsername = "tpcw";
// 	    dbPassword = "tpcw";
	    dbAlias = paramDbUrl + paramDbName;
	    dbUsername = paramDbUsername;
	    dbPassword = paramDbPassword;
	    updateRepositoryStructureIfNeeded = true;
	    rootClass = Root.class;
	}};
	FenixFramework.initialize(config);
    
	ensureRootObject();
    
	populateAuthorTable();
	populateBookTable();
	populateRelatedBooks();
	populateCountryTable();
	populateAddressTable();
	populateCustomerTable();
    
	//Need to debug
	populateOrdersAndCC_XACTSTable();
    
	// noindexes
	//addIndexes();
	System.out.println("Done");
    }

    private static void dropCreateDatabase(String dbUrl, String dbName, String dbUsername, String dbPassword) {
	try {
	    // initialize driver
	    Class.forName("com.mysql.jdbc.Driver");
	    // get connection
	    Connection conn = DriverManager.getConnection("jdbc:mysql:" + dbUrl, dbUsername, dbPassword);
	    // drop if exists
	    Statement st = conn.createStatement();
	    st.executeUpdate("DROP DATABASE IF EXISTS " + dbName);
	    // create
	    //	st = conn.createStatement();
	    st.executeUpdate("CREATE DATABASE " + dbName);
	    // close connection
	    st.close();
	    conn.close();
	} catch (Exception e) {
	    System.out.println("ERROR: Failed to drop/create database");
	    e.printStackTrace();
	    System.exit(1);
	}
    }


    private static Author getAuthorsFromTable(int idx) {
	return AbstractDomainObject.fromOID(_authors[idx]);
    }
    private static Book getBookFromTable(int idx) {
	return AbstractDomainObject.fromOID(_books[idx]);
    }
    private static Country getCountryFromTable(int idx) {
	return AbstractDomainObject.fromOID(_countries[idx]);
    }
    private static Address getAddressFromTable(int idx) {
	return AbstractDomainObject.fromOID(_addresses[idx]);
    }
    private static Customer getCustomerFromTable(int idx) {
	return AbstractDomainObject.fromOID(_customers[idx]);
    }

    private static void ensureRootObject() {
        Transaction.withTransaction(new TransactionalCommand() {
                public void doIt() {
		    Root root = FenixFramework.getRoot();
		    // force object to load to check if it really exists or is just a stub
		    try {
			root.getLoaded();
		    } catch (VersionNotAvailableException ex) {
			// then create the object.  It is assumed that this object will be created with idInternal 1.
			System.out.println("IT IS NORMAL TO SEE AN OJB EXCEPTION IF THE ROOT OBJECT COULD NOT BE READ.  THIS SHOULD BE OK.");
			root = new Root();
			root.setLoaded(true);
		    }
		    root.setNumCartIds(0);
		}
	    });
    }

    /**
     * @param n the number of Author objects to create.
     **/
    private static void createAuthorObjects(int n) {
	String A_FNAME, A_MNAME, A_LNAME, A_BIO;
	java.sql.Date A_DOB;
	GregorianCalendar cal;
    
	for (int i = 0; i < n; i++) {
	    A_FNAME = getRandomAString(3,20);
	    A_MNAME = getRandomAString(1,20);
	    A_LNAME = getRandomAString(1,20);
    
	    cal = getRandomDate(1800, 1990);
    
	    A_DOB = new java.sql.Date(cal.getTime().getTime());
	    A_BIO = getRandomAString(125, 500);

	    Author au = new Author(A_FNAME, A_LNAME, A_MNAME, A_DOB, A_BIO, 1 + _counter);
	    au.setRoot((Root)FenixFramework.getRoot());
	    _authors[_counter++] = au.getOid();
	}
    }


    private static void populateAuthorTable(){    
	System.out.println("Populating AUTHOR Table with " + NUM_AUTHORS +
			   " authors");

	_counter = 0;

	for(int i = 1; i <= NUM_AUTHORS/NUMBER_OBJECTS_TRANSACTION; i++){
	    Transaction.withTransaction(new TransactionalCommand() {
		    public void doIt() {
			createAuthorObjects(NUMBER_OBJECTS_TRANSACTION);
		    }
		});
	}

	Transaction.withTransaction(new TransactionalCommand() {
		public void doIt() {
		    createAuthorObjects(NUM_AUTHORS%NUMBER_OBJECTS_TRANSACTION);
		}
	    });
    }

    private static void populateCustomerTable(){    
	System.out.println("Populating CUSTOMER Table with " + 
			   NUM_CUSTOMERS + " customers");
	//	System.out.print("Complete (in 10,000's): ");

	_counter = 0;

	for(int i = 1; i <= NUM_CUSTOMERS/NUMBER_OBJECTS_TRANSACTION; i++){
	    Transaction.withTransaction(new TransactionalCommand() {
		    public void doIt() {
			createCustomerObjects(NUMBER_OBJECTS_TRANSACTION);
		    }
		});

	    //System.out.println(" " + i);
	}

	Transaction.withTransaction(new TransactionalCommand() {
		public void doIt() {
		    createCustomerObjects(NUM_CUSTOMERS%NUMBER_OBJECTS_TRANSACTION);
		    ((Root)FenixFramework.getRoot()).setNumCustomerIds(_counter);
		}
	    });

	System.out.println("Populated CUSTOMER Table with " + 
			   NUM_CUSTOMERS + " customers");
    }

    private static void createCustomerObjects(int n){
	String C_UNAME, C_PASSWD, C_LNAME, C_FNAME;
	int C_PHONE;
	String C_EMAIL;
	java.sql.Date C_SINCE, C_LAST_LOGIN;
	java.sql.Timestamp C_LOGIN, C_EXPIRATION;
	double C_DISCOUNT, C_BALANCE, C_YTD_PMT;
	java.sql.Date C_BIRTHDATE;
	String C_DATA;
	Address address;
    
	for(int i = 0; i < n; i++) {
	    C_UNAME = DigSyl(1 + _counter, 0);
	    C_PASSWD = C_UNAME.toLowerCase();
	    C_LNAME = getRandomAString(8,15);
	    C_FNAME = getRandomAString(8,15);
	    address = getAddressFromTable(getRandomInt(0, NUM_ADDRESSES - 1));
	    C_PHONE = getRandomNString(9,16);
	    C_EMAIL = C_UNAME+"@"+getRandomAString(2,9)+".com";
      
	    GregorianCalendar cal = new GregorianCalendar();
	    cal.add(Calendar.DAY_OF_YEAR,-1*getRandomInt(1,730));
	    C_SINCE = new java.sql.Date(cal.getTime().getTime());
	    cal.add(Calendar.DAY_OF_YEAR,getRandomInt(0,60)); 
	    if(cal.after(new GregorianCalendar()))
		cal = new GregorianCalendar();
      
	    C_LAST_LOGIN = new java.sql.Date(cal.getTime().getTime());
	    C_LOGIN = new java.sql.Timestamp(System.currentTimeMillis());
	    cal = new GregorianCalendar();
	    cal.add(Calendar.HOUR, 2);
	    C_EXPIRATION = new java.sql.Timestamp(cal.getTime().getTime());
      
	    C_DISCOUNT = (double) getRandomInt(0, 50)/100.0;
	    C_BALANCE=0.00;
	    C_YTD_PMT = (double) getRandomInt(0, 99999)/100.0;
      
	    cal = getRandomDate(1880, 2000);
	    C_BIRTHDATE = new java.sql.Date(cal.getTime().getTime());
      
	    C_DATA = getRandomAString(100,500);
      
	    Customer customer =
		new Customer(C_UNAME, C_PASSWD, C_FNAME, C_LNAME, Integer.toString(C_PHONE),
			     C_EMAIL, C_SINCE, C_LAST_LOGIN, C_LOGIN, C_EXPIRATION,
			     C_DISCOUNT, C_BALANCE, C_YTD_PMT, C_BIRTHDATE, C_DATA,
			     address, 1 + _counter);
	    customer.setRoot((Root)FenixFramework.getRoot());
	    _customers[_counter++] = customer.getOid();
	}
    }
 
    private static void populateBookTable() {
	System.out.println("Populating BOOK table with "  + NUM_ITEMS + " books");
	_counter = 0;

	for(int i = 1; i <= NUM_ITEMS/NUMBER_OBJECTS_TRANSACTION; i++){
	    Transaction.withTransaction(new TransactionalCommand() {
		    public void doIt() {
			createBookObjects(NUMBER_OBJECTS_TRANSACTION);
		    }
		});
	}

	Transaction.withTransaction(new TransactionalCommand() {
		public void doIt() {
		    createBookObjects(NUM_ITEMS%NUMBER_OBJECTS_TRANSACTION);
		}
	    });
    }
  
    private static void createBookObjects(int n) {
	String I_TITLE;
	GregorianCalendar cal;
	Author a_id;
	java.sql.Date I_PUB_DATE;
	String I_PUBLISHER, I_SUBJECT, I_DESC;
	String I_THUMBNAIL, I_IMAGE;
	double I_SRP, I_COST;
	java.sql.Date I_AVAIL;
	int I_STOCK;
	String I_ISBN;
	int I_PAGE;
	String I_BACKING;
	String I_DIMENSIONS;
    
	String[] subjects = { "ARTS", "BIOGRAPHIES", "BUSINESS", "CHILDREN",
			      "COMPUTERS", "COOKING", "HEALTH", "HISTORY",
			      "HOME", "HUMOR", "LITERATURE", "MYSTERY",
			      "NON-FICTION", "PARENTING", "POLITICS",
			      "REFERENCE", "RELIGION", "ROMANCE", 
			      "SELF-HELP", "SCIENCE-NATURE", "SCIENCE-FICTION",
			      "SPORTS", "YOUTH", "TRAVEL"};
    
	String[] backings = { "HARDBACK", "PAPERBACK", "USED", "AUDIO",
			      "LIMITED-EDITION"};

	for (int i = 0; i < n; i++) {
	    I_TITLE= getRandomAString(14,60);
	    if(_counter < (NUM_ITEMS/4))
		a_id = getAuthorsFromTable(_counter);
	    else 
		a_id = getAuthorsFromTable(getRandomInt(0, NUM_ITEMS/4 - 1));

	    cal = getRandomDate(1930, 2000);
      
	    I_PUB_DATE = new java.sql.Date(cal.getTime().getTime());
      
	    I_PUBLISHER = getRandomAString(14,60);
	    I_SUBJECT = subjects[getRandomInt(0, subjects.length - 1)];
	    I_DESC = getRandomAString(100,500);
      
	    // Related books are set after all books are generated
      
	    I_THUMBNAIL = new String("img"+i%100+"/thumb_"+i+".gif");
	    I_IMAGE = new String("img"+i%100+"/image_"+i+".gif");
	    I_SRP = (double) getRandomInt(100, 99999);
	    I_SRP/=100.0;
      
	    I_COST = I_SRP-((((double)getRandomInt(0, 50)/100.0))*I_SRP);
      
	    cal.add(Calendar.DAY_OF_YEAR, getRandomInt(1,30));
	    I_AVAIL = new java.sql.Date(cal.getTime().getTime());
      
	    I_STOCK = getRandomInt(10,30);
	    I_ISBN = getRandomAString(13);
	    I_PAGE = getRandomInt(20,9999);
	    I_BACKING = backings[getRandomInt(0, backings.length - 1)];
	    I_DIMENSIONS= ((double) getRandomInt(1,9999)/100.0) +"x"+
		((double) getRandomInt(1,9999)/100.0) + "x" +
		((double) getRandomInt(1,9999)/100.0);
      
	    // Set parameter
	    Book it = new Book(I_TITLE, I_PUB_DATE, I_PUBLISHER, I_SUBJECT, I_DESC,
			       I_THUMBNAIL, I_IMAGE, I_SRP, I_COST,
			       I_AVAIL, I_STOCK, I_ISBN, I_PAGE, I_BACKING,
			       I_DIMENSIONS, _counter + 1, a_id);	
	    it.setRoot((Root)FenixFramework.getRoot());
	    _books[_counter++] = it.getOid();
	}
      
    }


    private static void populateRelatedBooks() {
	System.out.println("Populating 5 related books for each book");
	_counter = 0;

	for(int i = 1; i <= NUM_ITEMS/NUMBER_OBJECTS_TRANSACTION; i++){
	    Transaction.withTransaction(new TransactionalCommand() {
		    public void doIt() {
			setRelatedBooks(NUMBER_OBJECTS_TRANSACTION);
		    }
		});
	}

	Transaction.withTransaction(new TransactionalCommand() {
		public void doIt() {
		    setRelatedBooks(NUM_ITEMS%NUMBER_OBJECTS_TRANSACTION);
		}
	    });
    }
  
    private static void setRelatedBooks(int n) {
	for (int i = 0; i < n; i++) {
	    Book book = getBookFromTable(_counter++);
	    int I_RELATED1, I_RELATED2, I_RELATED3, I_RELATED4, I_RELATED5;

	    I_RELATED1 = getRandomInt(1, NUM_ITEMS);
	    do {
		I_RELATED2 = getRandomInt(1, NUM_ITEMS);
	    } while(I_RELATED2 == I_RELATED1);
	    do {
		I_RELATED3 = getRandomInt(1, NUM_ITEMS);
	    } while(I_RELATED3 == I_RELATED1 || I_RELATED3 == I_RELATED2);
	    do {
		I_RELATED4 = getRandomInt(1, NUM_ITEMS);
	    } while(I_RELATED4 == I_RELATED1 || I_RELATED4 == I_RELATED2
		    || I_RELATED4 == I_RELATED3);
	    do {
		I_RELATED5 = getRandomInt(1, NUM_ITEMS);
	    } while(I_RELATED5 == I_RELATED1 || I_RELATED5 == I_RELATED2
		    || I_RELATED5 == I_RELATED3 ||
		    I_RELATED5 == I_RELATED4);

	    // remeber that counter started at 0, thus subtract 1 to the index
	    book.setRelatedTo1(getBookFromTable(I_RELATED1-1));
	    book.setRelatedTo2(getBookFromTable(I_RELATED2-1));
	    book.setRelatedTo3(getBookFromTable(I_RELATED3-1));
	    book.setRelatedTo4(getBookFromTable(I_RELATED4-1));
	    book.setRelatedTo5(getBookFromTable(I_RELATED5-1));
	}
    }

    private static void populateAddressTable(){    
	System.out.println("Populating ADDRESS Table with " + NUM_ADDRESSES +
			   " addresses");

	_counter = 0;

	for(int i = 1; i <= NUM_ADDRESSES/NUMBER_OBJECTS_TRANSACTION; i++){
	    Transaction.withTransaction(new TransactionalCommand() {
		    public void doIt() {
			createAddressObjects(NUMBER_OBJECTS_TRANSACTION);
		    }
		});
	}

	Transaction.withTransaction(new TransactionalCommand() {
		public void doIt() {
		    createAddressObjects(NUM_ADDRESSES%NUMBER_OBJECTS_TRANSACTION);
		    ((Root)FenixFramework.getRoot()).setNumAddrIds(_counter);
		}
	    });
    }

    private static void createAddressObjects(int n){

	String ADDR_STREET1, ADDR_STREET2, ADDR_CITY, ADDR_STATE;
	String ADDR_ZIP;
	Country country;

	for (int i = 0; i < n; i++) {
	    ADDR_STREET1 = getRandomAString(15,40);
	    ADDR_STREET2 = getRandomAString(15,40);
	    ADDR_CITY    = getRandomAString(4,30);
	    ADDR_STATE   = getRandomAString(2,20);
	    ADDR_ZIP     = getRandomAString(5,10);
	    country      = getCountryFromTable(getRandomInt(0, NUM_COUNTRIES - 1));
                
	    Address address = new Address(ADDR_STREET1, ADDR_STREET2, ADDR_CITY,
					  ADDR_STATE, ADDR_ZIP, country, 1 + _counter);
	    _addresses[_counter++] = address.getOid();
	}
    }

    private static void populateCountryTable() {
	System.out.println("Populating COUNTRY with " + NUM_COUNTRIES + " countries");

	_counter = 0;

	Transaction.withTransaction(new TransactionalCommand() {
		public void doIt() {
		    createCountryObjects(NUM_COUNTRIES%NUMBER_OBJECTS_TRANSACTION);
		}
	    });
    }
   
    private static void createCountryObjects(int n){
	String[] countries = {
	    "United States","United Kingdom","Canada", "Germany", "France","Japan",
	    "Netherlands","Italy","Switzerland","Australia","Algeria","Argentina",
	    "Armenia","Austria","Azerbaijan","Bahamas","Bahrain","Bangla Desh",
	    "Barbados","Belarus","Belgium","Bermuda", "Bolivia","Botswana","Brazil",
	    "Bulgaria","Cayman Islands","Chad", "Chile", "China","Christmas Island",
	    "Colombia","Croatia","Cuba","Cyprus","Czech Republic","Denmark",
	    "Dominican Republic","Eastern Caribbean","Ecuador","Egypt","El Salvador",
	    "Estonia","Ethiopia","Falkland Island","Faroe Island", "Fiji","Finland",
	    "Gabon","Gibraltar","Greece","Guam","Hong Kong","Hungary","Iceland",
	    "India","Indonesia","Iran","Iraq","Ireland","Israel","Jamaica", "Jordan",
	    "Kazakhstan","Kuwait","Lebanon","Luxembourg","Malaysia","Mexico",
	    "Mauritius", "New Zealand","Norway","Pakistan","Philippines","Poland",
	    "Portugal","Romania","Russia","Saudi Arabia","Singapore","Slovakia",
	    "South Africa","South Korea", "Spain","Sudan","Sweden","Taiwan",
	    "Thailand","Trinidad","Turkey","Venezuela", "Zambia"
	};
      
	double[] exchanges = {
	    1, .625461, 1.46712, 1.86125, 6.24238, 121.907, 2.09715, 1842.64, 1.51645,
	    1.54208, 65.3851, 0.998, 540.92, 13.0949, 3977, 1, .3757, 48.65, 2, 248000,
	    38.3892, 1, 5.74, 4.7304, 1.71, 1846, .8282, 627.1999, 494.2, 8.278,
	    1.5391, 1677, 7.3044, 23, .543, 36.0127, 7.0707, 15.8, 2.7, 9600, 3.33771,
	    8.7, 14.9912, 7.7, .6255, 7.124, 1.9724, 5.65822, 627.1999, .6255, 309.214,
	    1, 7.75473, 237.23, 74.147, 42.75, 8100, 3000, .3083, .749481, 4.12, 37.4,
	    0.708, 150, .3062, 1502, 38.3892, 3.8, 9.6287, 25.245, 1.87539, 7.83101, 52,
	    37.8501, 3.9525, 190.788, 15180.2, 24.43, 3.7501, 1.72929, 43.9642, 6.25845, 
	    1190.15, 158.34, 5.282, 8.54477, 32.77, 37.1414, 6.1764, 401500, 596, 2447.7
	};

	String[] currencies = {
	    "Dollars","Pounds","Dollars","Deutsche Marks","Francs","Yen","Guilders",
	    "Lira","Francs","Dollars","Dinars","Pesos", "Dram","Schillings","Manat",
	    "Dollars","Dinar","Taka","Dollars","Rouble","Francs","Dollars",
	    "Boliviano", "Pula", "Real", "Lev","Dollars","Franc","Pesos","Yuan Renmimbi",
	    "Dollars","Pesos","Kuna","Pesos","Pounds","Koruna","Kroner","Pesos",
	    "Dollars","Sucre","Pounds","Colon","Kroon","Birr","Pound","Krone","Dollars",
	    "Markka","Franc","Pound","Drachmas","Dollars","Dollars","Forint","Krona",
	    "Rupees","Rupiah","Rial","Dinar","Punt","Shekels","Dollars","Dinar","Tenge",
	    "Dinar","Pounds","Francs","Ringgit","Pesos","Rupees","Dollars","Kroner",
	    "Rupees","Pesos","Zloty","Escudo","Leu","Rubles","Riyal","Dollars","Koruna",
	    "Rand","Won","Pesetas","Dinar","Krona","Dollars","Baht","Dollars","Lira",
	    "Bolivar","Kwacha"
	};
      
	for(int i = 0; i < n ; i++){
	    Country country = new Country(countries[i], exchanges[i], currencies[i], i);
	    country.setRoot((Root)FenixFramework.getRoot());
	    _countries[_counter++] = country.getOid();
	}
    }

    private static void populateOrdersAndCC_XACTSTable(){
	System.out.println("Populating BOOK table with "  + NUM_ITEMS + " books");
	_counter = 0;

	System.out.println("Populating ORDERS, ORDER_LINES, CC_XACTS with "
			   + NUM_ORDERS + " orders");
	//	System.out.print("Complete (in 10,000's): ");


	for(int i = 1; i <= NUM_ORDERS/NUMBER_OBJECTS_TRANSACTION; i++){
	    Transaction.withTransaction(new TransactionalCommand() {
		    public void doIt() {
			createOrdersAndCC_XACTSObjects(NUMBER_OBJECTS_TRANSACTION);
		    }
		});
	}

	Transaction.withTransaction(new TransactionalCommand() {
		public void doIt() {
		    createOrdersAndCC_XACTSObjects(NUM_ORDERS%NUMBER_OBJECTS_TRANSACTION);
		    ((Root)FenixFramework.getRoot()).setNumOrderIds(_counter);
		}
	    });
    }

    private static void createOrdersAndCC_XACTSObjects(int n){
	GregorianCalendar cal;
	String[] credit_cards = {"VISA", "MASTERCARD", "DISCOVER", 
				 "AMEX", "DINERS"};
	String[] ship_types = {"AIR", "UPS", "FEDEX", "SHIP", "COURIER", "MAIL"};
	String[] status_types = {"PROCESSING", "SHIPPED", "PENDING", "DENIED"};
    
	//Order variables
	java.sql.Timestamp O_DATE;
	double O_SUB_TOTAL;
	double O_TAX;
	double O_TOTAL;
	String O_SHIP_TYPE;
	java.sql.Timestamp O_SHIP_DATE;
	int O_BILL_ADDR_ID, O_SHIP_ADDR_ID;
	String O_STATUS;
	Customer customer;
	Address shipAddress, billAddress;

	String CX_TYPE;
	int CX_NUM;
	String CX_NAME;
	java.sql.Date CX_EXPIRY;
	String CX_AUTH_ID;
	double CX_XACT_AMT;
	int CX_CO_ID;
    
	for(int i = 0; i < n; i++){
	    int num_items = getRandomInt(1,5);
	    customer = getCustomerFromTable(getRandomInt(0, NUM_CUSTOMERS - 1));
	    cal = new GregorianCalendar();
	    cal.add(Calendar.DAY_OF_YEAR, -1*getRandomInt(1,60));
	    O_DATE = new java.sql.Timestamp(cal.getTime().getTime());
	    O_SUB_TOTAL = (double) getRandomInt(1000, 999999)/100;
	    O_TAX = O_SUB_TOTAL * 0.0825;
	    O_TOTAL = O_SUB_TOTAL + O_TAX + 3.00 + num_items;
	    O_SHIP_TYPE = ship_types[getRandomInt(0, ship_types.length - 1)];
	    cal.add(Calendar.DAY_OF_YEAR, getRandomInt(0,7));
	    O_SHIP_DATE = new java.sql.Timestamp(cal.getTime().getTime());
      
	    billAddress = getAddressFromTable(getRandomInt(0, NUM_ADDRESSES-1));
	    shipAddress = getAddressFromTable(getRandomInt(0, NUM_ADDRESSES-1));
	    O_STATUS = status_types[getRandomInt(0, status_types.length - 1)];
      
	    Orders order = new Orders(O_DATE, O_SUB_TOTAL, O_TAX, O_TOTAL,
				      O_SHIP_TYPE, O_SHIP_DATE, O_STATUS,
				      billAddress, shipAddress, customer, _counter + 1);
	    order.setRoot((Root)FenixFramework.getRoot());

	    for(int j = 1; j <= num_items; j++){
		Book book = getBookFromTable(getRandomInt(0, NUM_ITEMS - 1));
		int OL_QTY = getRandomInt(1, 300);
		double OL_DISCOUNT = (double) getRandomInt(0,30)/100;
		String OL_COMMENTS = getRandomAString(20,100);
		OrderLine orderLine = new OrderLine(OL_QTY, OL_DISCOUNT, OL_COMMENTS,
						    j, order, book);
	    }
      
	    CX_TYPE = credit_cards[getRandomInt(0, credit_cards.length - 1)];
	    CX_NUM = getRandomNString(16);
	    CX_NAME = getRandomAString(14,30);
	    cal = new GregorianCalendar();
	    cal.add(Calendar.DAY_OF_YEAR, getRandomInt(10, 730));
	    CX_EXPIRY = new java.sql.Date(cal.getTime().getTime());
	    //      CX_AUTH_ID = getRandomAString(15);                       // unused
	    Country country = getCountryFromTable(getRandomInt(0,91));
	    CCXact ccXact = new CCXact(CX_TYPE, CX_NUM, CX_NAME, CX_EXPIRY,/* CX_AUTH_ID,*/ O_TOTAL,
				       O_SHIP_DATE, /* 1 + _counter, */order, country);
	    _counter++;
	}
    }

    //UTILITY FUNCTIONS BEGIN HERE

    private static GregorianCalendar getRandomDate(int firstYar, int lastYear) {
	int month, day, year, maxday;
    
	year = getRandomInt(firstYar, lastYear);
	month = getRandomInt(0,11);
    
	maxday = 31;
	if (month == 3 | month ==5 | month == 8 | month == 10)
	    maxday = 30;
	else if (month == 1)
	    maxday = 28;
    
	day = getRandomInt(1, maxday);
	return new GregorianCalendar(year, month, day);
    }
  
    private static String getRandomAString(int min, int max){
	String newstring = new String();
	int i;
	final char[] chars = {'a','b','c','d','e','f','g','h','i','j','k',
			      'l','m','n','o','p','q','r','s','t','u','v',
			      'w','x','y','z','A','B','C','D','E','F','G',
			      'H','I','J','K','L','M','N','O','P','Q','R',
			      'S','T','U','V','W','X','Y','Z','!','@','#',
			      '$','%','^','&','*','(',')','_','-','=','+',
			      '{','}','[',']','|',':',';',',','.','?','/',
			      '~',' '}; //79 characters
	int strlen = (int) Math.floor(rand.nextDouble()*((max-min)+1));
	strlen += min;
	for(i = 0; i < strlen; i++){
	    char c = chars[(int) Math.floor(rand.nextDouble()*79)];
	    newstring = newstring.concat(String.valueOf(c));
	}
	return newstring;
    }
  
    private static String getRandomAString(int length){
	String newstring = new String();
	int i;
	final char[] chars = {'a','b','c','d','e','f','g','h','i','j','k',
			      'l','m','n','o','p','q','r','s','t','u','v',
			      'w','x','y','z','A','B','C','D','E','F','G',
			      'H','I','J','K','L','M','N','O','P','Q','R',
			      'S','T','U','V','W','X','Y','Z','!','@','#',
			      '$','%','^','&','*','(',')','_','-','=','+',
			      '{','}','[',']','|',':',';',',','.','?','/',
			      '~',' '}; //79 characters
	for(i = 0; i < length; i++){
	    char c = chars[(int) Math.floor(rand.nextDouble()*79)];
	    newstring = newstring.concat(String.valueOf(c));
	}
	return newstring;
    }
  
    private static int getRandomNString(int num_digits){
	int return_num = 0;
	for(int i = 0; i < num_digits; i++){
	    return_num += getRandomInt(0, 9) * 
		(int) java.lang.Math.pow(10.0, (double) i);
	}
	return return_num;
    }
  
    private static int getRandomNString(int min, int max){
	int strlen = (int) Math.floor(rand.nextDouble()*((max-min)+1)) + min;
	return getRandomNString(strlen);
    }
  
    private static int getRandomInt(int lower, int upper){
    
	int num = (int) Math.floor(rand.nextDouble()*((upper+1)-lower));
	if(num+lower > upper || num+lower < lower){
	    System.out.println("ERROR: Random returned value of of range!");
	    System.exit(1);
	}
	return num + lower;
    }
    private static String DigSyl(int D, int N){
	int i;
	String resultString = new String();
	String Dstr = Integer.toString(D);
    
	if(N > Dstr.length()){
	    int padding = N - Dstr.length();
	    for(i = 0; i < padding; i++)
		resultString = resultString.concat("BA");
	}
    
	for(i = 0; i < Dstr.length(); i++){
	    if(Dstr.charAt(i) == '0')
		resultString = resultString.concat("BA");
	    else if(Dstr.charAt(i) == '1')
		resultString = resultString.concat("OG");
	    else if(Dstr.charAt(i) == '2')
		resultString = resultString.concat("AL");
	    else if(Dstr.charAt(i) == '3')
		resultString = resultString.concat("RI");
	    else if(Dstr.charAt(i) == '4')
		resultString = resultString.concat("RE");
	    else if(Dstr.charAt(i) == '5')
		resultString = resultString.concat("SE");
	    else if(Dstr.charAt(i) == '6')
		resultString = resultString.concat("AT");
	    else if(Dstr.charAt(i) == '7')
		resultString = resultString.concat("UL");
	    else if(Dstr.charAt(i) == '8')
		resultString = resultString.concat("IN");
	    else if(Dstr.charAt(i) == '9')
		resultString = resultString.concat("NG");
	}
    
	return resultString;
    }
}
