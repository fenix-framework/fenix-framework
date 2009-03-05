package pt.ist.fenixframework.example.tpcw.domain;

import pt.ist.fenixframework.example.tpcw.domain.dto.CartLineDTO;

public class ShoppingCartLine extends ShoppingCartLine_Base {
    
    public  ShoppingCartLine(Book book, int qty) {
        super();
	setBook(book);
	setQty(qty);
    }
    
    public CartLineDTO getDTO() {
	return new CartLineDTO(this);
    }
}
