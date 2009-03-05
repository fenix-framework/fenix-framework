/**
 * This class represents the DTO of the whole content of the Customer domain entity.
 **/

package pt.ist.fenixframework.example.tpcw.domain.dto;

import pt.ist.fenixframework.example.tpcw.domain.Customer;
import pt.ist.fenixframework.example.tpcw.domain.Address;
import pt.ist.fenixframework.example.tpcw.domain.Country;
import java.util.Date;

public class CustomerDTO extends tpcw_dto.Customer {
  public CustomerDTO(Customer customer){
    c_id = customer.getC_id();
    c_uname = customer.getUname();
    c_passwd = customer.getPasswd();
    c_fname = customer.getFname();
    c_lname = customer.getLname();
    
    c_phone = customer.getPhone();
    c_email = customer.getEmail();
    c_since = customer.getSince();
    c_last_visit = customer.getLastLogin();
    c_login = customer.getLogin();
    c_expiration = customer.getExpiration();
    c_discount = customer.getDiscount();
    c_balance = customer.getBalance();
    c_ytd_pmt = customer.getYtd_pmt();
    c_birthdate = customer.getBirthdate();
    c_data = customer.getData();
    
    Address address = customer.getAddress();
    addr_id = address.getAddr_id();
    addr_street1 = address.getStreet1();
    addr_street2 = address.getStreet2();
    addr_city = address.getCity();
    addr_state = address.getState();
    addr_zip = address.getZip();
    addr_co_id = address.getAddr_id();
    
    co_name = address.getCountry().getName();
  }
}