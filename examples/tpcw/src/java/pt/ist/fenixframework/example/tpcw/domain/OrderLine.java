package pt.ist.fenixframework.example.tpcw.domain;

import pt.ist.fenixframework.example.tpcw.domain.dto.OrderLineDTO;

public class OrderLine extends OrderLine_Base {
    
  public  OrderLine() {
    super();
  }
  
  public OrderLine(int qty, double discount, String comments, int ol_id, Orders order, Book book) {
    super();
    setQty(qty);
    setDiscount(discount);
    setComments(comments);
    setOl_id(ol_id);
    setBook(book);
    setOrder(order);
  }

  public final OrderLineDTO getDTO() {
    return new OrderLineDTO(this);
  }
}
