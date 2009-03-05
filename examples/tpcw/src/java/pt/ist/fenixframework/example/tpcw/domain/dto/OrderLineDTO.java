/**
 * This class represents the DTO of the whole content of the OrderLine domain entity.
 **/

package pt.ist.fenixframework.example.tpcw.domain.dto;

import pt.ist.fenixframework.example.tpcw.domain.OrderLine;
import pt.ist.fenixframework.example.tpcw.domain.Book;
import pt.ist.fenixframework.example.tpcw.domain.Country;
import java.util.Date;

public class OrderLineDTO extends tpcw_dto.OrderLine {
  public OrderLineDTO(OrderLine line) {
    ol_qty = line.getQty();
    ol_discount = line.getDiscount();
    ol_comments = line.getComments();

    Book book = line.getBook();
    ol_i_id = book.getI_id();
    i_title = book.getTitle();
    i_publisher = book.getPublisher();
    i_cost = book.getCost();
  }
}