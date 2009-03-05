package pt.ist.fenixframework.example.tpcw.domain;

import pt.ist.fenixframework.example.tpcw.domain.dto.BookDTO;
import pt.ist.fenixframework.example.tpcw.domain.dto.ShortBookDTO;

public class Book extends Book_Base {
    
    public  Book() {
	super();
    }
    
    public Book(String title, java.sql.Date pubDate, String publisher, String subject, String desc,
		String thumbnail, String image, double srp, double cost, java.sql.Date avail,
		int stock, String isbn, int page, String backing, String dimensions, int i_id, Author author)
    {
	super();

	setTitle(title);
	setPubDate(pubDate);
	setPublisher(publisher);
	setSubject(subject);
	setDescription(desc);
	setThumbnail(thumbnail);
	setImage(image);
	setSrp(srp);
	setCost(cost);
	setAvail(avail);
	setStock(stock);
	setIsbn(isbn);
	setPage(page);
	setBacking(backing);
	setDimensions(dimensions);
	setI_id(i_id);
	setAuthor(author);
    }

    public BookDTO getDTO() {
	return new BookDTO(this);
    }

    public ShortBookDTO getShortBookDTO() {
	return new ShortBookDTO(this);
    }
}
