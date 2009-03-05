/**
 * This class represents the DTO of the whole content of the ShoppingCartLine domain entity.
 **/

package pt.ist.fenixframework.example.tpcw.domain.dto;

import pt.ist.fenixframework.example.tpcw.domain.ShoppingCartLine;

public class CartLineDTO extends tpcw_dto.CartLine {
    public CartLineDTO(ShoppingCartLine scl) {
	super(scl.getBook().getTitle(),
	      scl.getBook().getCost(),
	      scl.getBook().getSrp(),
	      scl.getBook().getBacking(),
	      scl.getQty(),
	      scl.getBook().getI_id());
    }
}
