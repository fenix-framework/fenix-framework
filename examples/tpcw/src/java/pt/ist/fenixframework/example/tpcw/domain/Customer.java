package pt.ist.fenixframework.example.tpcw.domain;

import java.sql.Timestamp;
import java.util.Map;

import pt.ist.fenixframework.example.tpcw.domain.dto.CustomerDTO;

public class Customer extends Customer_Base {
  
    public Customer() {
	super();
    }

    public  Customer(String uname, String passwd, String fname, String lname,
		     String phone, String email, java.sql.Date since, java.sql.Date lastLogin,
		     java.sql.Timestamp login, java.sql.Timestamp expiration, double discount,
		     double balance, double ytd_pmt, java.sql.Date birthdate, String data,
		     Address address, int c_id) {
	super();

	setUname(uname);
	setPasswd(passwd);
	setFname(fname);
	setLname(lname);
	setPhone(phone);
	setEmail(email);
	setSince(since);
	setLastLogin(lastLogin);
	setLogin(login);
	setExpiration(expiration);
	setDiscount(discount);
	setBalance(balance);
	setYtd_pmt(ytd_pmt);
	setBirthdate(birthdate);
	setData(data);
	setAddress(address);
	setC_id(c_id);
    }
    
    public Orders getMostRecentOrder() {
	Orders mostRecent = null;
	for (Orders order : getOrders()) {
	    if (mostRecent == null) {
		mostRecent = order;
	    } else if (order.isMoreRecentThan(mostRecent)) {
		mostRecent = order;
	    }
	}
	return mostRecent;
    }
    
    public void refreshSession() {
	Timestamp now = new Timestamp(System.currentTimeMillis());
	now.setNanos(0);
	setLogin(now);
	Timestamp expiration = new Timestamp(now.getTime() + 2*3600*1000);
	expiration.setNanos(0);
	setExpiration(expiration);
    }

    // Update the argument Map, adding the amounts for each book that this client ever ordered
    public void updateQuantityOfBooksOrdered(Map<Book,Integer> quantities) {
	for (Orders order : getOrders()) {
	    for (OrderLine orderLine : order.getOrderLines()) {
		Book book = orderLine.getBook();
		Integer quantity = quantities.get(book);
		if (quantity == null) {
		    quantity = orderLine.getQty();
		} else {
		    quantity += orderLine.getQty();
		}
		quantities.put(book, quantity);
	    }
	}
    }

    public final CustomerDTO getDTO() {
	return new CustomerDTO(this);
    }
}
