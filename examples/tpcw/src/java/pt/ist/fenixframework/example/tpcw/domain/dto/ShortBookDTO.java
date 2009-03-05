/**
 * This class represents the DTO of the whole content of the ShortBook domain entity.
 **/

package pt.ist.fenixframework.example.tpcw.domain.dto;

import pt.ist.fenixframework.example.tpcw.domain.Book;
import pt.ist.fenixframework.example.tpcw.domain.Author;
import java.util.Date;

public class ShortBookDTO extends tpcw_dto.ShortBook
{
  public ShortBookDTO(Book book) {
    i_id = book.getI_id();
    i_title = book.getTitle();

    Author author = book.getAuthor();
    a_fname = author.getFname();
    a_lname = author.getLname();
  }
}