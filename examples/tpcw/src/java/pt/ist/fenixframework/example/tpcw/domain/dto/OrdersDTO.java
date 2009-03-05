/**
 * This class represents the DTO of the whole content of the Orders domain entity.
 **/

package pt.ist.fenixframework.example.tpcw.domain.dto;

import pt.ist.fenixframework.example.tpcw.domain.Customer;
import pt.ist.fenixframework.example.tpcw.domain.Address;
import pt.ist.fenixframework.example.tpcw.domain.Country;
import pt.ist.fenixframework.example.tpcw.domain.Orders;
import pt.ist.fenixframework.example.tpcw.domain.CCXact;
import java.util.Date;

public class OrdersDTO extends tpcw_dto.Order {
  public OrdersDTO(Orders order) {
    Customer customer = order.getCustomer();

    o_id = order.getO_id();
    c_fname = customer.getFname();
    c_lname = customer.getLname();
    c_passwd = customer.getPasswd();
    c_uname = customer.getUname();
    c_phone = customer.getPhone();
    c_email = customer.getEmail();
    o_date = order.getDate();
    o_subtotal = order.getSubtotal();
    o_tax = order.getTax();
    o_total = order.getTotal();
    o_ship_type = order.getShipType();
    o_ship_date = order.getShipDate();
    o_status = order.getStatus();

    cx_type = order.getCcXact().getType();
    
    Address billAddres = order.getBillAddress();
    Address shipAddres = order.getShipAddress();

    bill_addr_street1 = billAddres.getStreet1();
    bill_addr_street2 = billAddres.getStreet2();
    bill_addr_state = billAddres.getState();
    bill_addr_zip = billAddres.getZip();
    bill_co_name = billAddres.getCountry().getName();
            
    ship_addr_street1 = shipAddres.getStreet1();
    ship_addr_street2 = shipAddres.getStreet2();
    ship_addr_state = shipAddres.getState();
    ship_addr_zip = shipAddres.getZip();
    ship_co_name = shipAddres.getCountry().getName();
  }
}