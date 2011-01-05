package pt.ist.fenixframework.example.tpcw.domain;

import pt.ist.fenixframework.example.tpcw.domain.dto.OrdersDTO;

public class Orders extends Orders_Base {
    
    public  Orders() {
        super();
    }
    
    public  Orders(java.sql.Timestamp date, double subtotal, double tax, double total, String shipType,
		   java.sql.Timestamp shipDate, String status, Address billAddress,
		   Address shipAddress, Customer customer, int o_id) {
	super();
    
    
	setDate(date);
	setSubtotal(subtotal);
	setTax(tax);
	setTotal(total);
	setShipType(shipType);
	setShipDate(shipDate);
	setStatus(status);
	setBillAddress(billAddress);
	setShipAddress(shipAddress);
	setCustomer(customer);
	setO_id(o_id);
    }
    
    public void addNewOrderLine(int ol_id, Book book, int ol_qty, double ol_discount, String ol_comment) {
	this.addOrderLines(new OrderLine(ol_qty, ol_discount, ol_comment, ol_id, this, book));
    }

    public boolean isMoreRecentThan(Orders order) {
	int cmp = this.getDate().compareTo(order.getDate());
	if (cmp == 0) {
	    return (this.getO_id() > order.getO_id());
	} else {
	    return (cmp > 0); // cmp>0  => this order is more recent;
	}
    }

    boolean contains(Book book) {
	for (OrderLine orderLine : this.getOrderLines()) {
	    if (orderLine.getBook() == book) {
		return true;
	    }
	}
	return false;
    }

    public final OrdersDTO getDTO() {
	return new OrdersDTO(this);
    }
}
