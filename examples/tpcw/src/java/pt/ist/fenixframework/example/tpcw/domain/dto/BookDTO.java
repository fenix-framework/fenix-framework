/**
 * This class represents the DTO of the whole content of the Book domain entity.
 **/

package pt.ist.fenixframework.example.tpcw.domain.dto;

import pt.ist.fenixframework.example.tpcw.domain.Book;
import pt.ist.fenixframework.example.tpcw.domain.Author;
import java.util.Date;

public class BookDTO extends tpcw_dto.Book {
  public BookDTO(Book book) {
    i_id = book.getI_id();
    i_title = book.getTitle();
    i_pub_Date = book.getPubDate();
    i_publisher = book.getPublisher();
    i_subject = book.getSubject();
    i_desc = book.getDescription();
    i_related1 = book.getRelatedTo1().getI_id();
    i_related2 = book.getRelatedTo2().getI_id();
    i_related3 = book.getRelatedTo3().getI_id();
    i_related4 = book.getRelatedTo4().getI_id();
    i_related5 = book.getRelatedTo5().getI_id();
    i_thumbnail = book.getThumbnail();
    i_image = book.getImage();
    i_srp = book.getSrp();
    i_cost = book.getCost();
    i_avail = book.getAvail();
    i_isbn = book.getIsbn();
    i_page = book.getPage();
    i_backing = book.getBacking();
    i_dimensions = book.getDimensions();

    Author author = book.getAuthor();
    a_id = author.getA_id();
    a_fname = author.getFname();
    a_lname = author.getLname();
  }
}
