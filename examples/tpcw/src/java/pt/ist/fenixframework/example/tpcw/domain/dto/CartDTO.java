/**
 * This class represents the DTO of the whole content of the ShoppingCartLine domain entity.
 **/

package pt.ist.fenixframework.example.tpcw.domain.dto;

import java.util.Vector;

import tpcw_dto.CartLine;
import pt.ist.fenixframework.example.tpcw.domain.ShoppingCart;
import pt.ist.fenixframework.example.tpcw.domain.ShoppingCartLine;

public class CartDTO extends tpcw_dto.Cart {
    public CartDTO(ShoppingCart sc, double C_DISCOUNT) {
	int i;
	int total_items;
	lines = new Vector();
	for (ShoppingCartLine scl : sc.getShoppingCartLines()) { //While there are lines remaining
	    CartLine line = scl.getDTO();
	    lines.addElement(line);
	}

	SC_SUB_TOTAL = 0;
	total_items = 0;
	for(i = 0; i < lines.size(); i++){
	    CartLine thisline = (CartLine) lines.elementAt(i);
	    SC_SUB_TOTAL += thisline.scl_cost * thisline.scl_qty;
	    total_items += thisline.scl_qty;
	}
	
	//Need to multiply the sub_total by the discount.
	SC_SUB_TOTAL = SC_SUB_TOTAL * ((100 - C_DISCOUNT)*.01);
	SC_TAX = SC_SUB_TOTAL * .0825;
	SC_SHIP_COST = 3.00 + (1.00 * total_items);
	SC_TOTAL = SC_SUB_TOTAL + SC_SHIP_COST + SC_TAX;
    }
}
