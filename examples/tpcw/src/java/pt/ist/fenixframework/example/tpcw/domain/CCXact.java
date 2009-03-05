package pt.ist.fenixframework.example.tpcw.domain;

public class CCXact extends CCXact_Base {
    
  public  CCXact() {
    super();
  }
  
    public CCXact(String type, long num, String name, java.sql.Date expiry,/* String authId,*/ double total,
		  java.sql.Timestamp shipDate, Orders order, Country country) {
    super();

    setType(type);
    setNum(num);
    setName(name);
    setExpiry(expiry);
//     setAuthId(authId);
    setTotal(total);
    setShipDate(shipDate);
    setOrder(order);
    setCountry(country);
  }
}
