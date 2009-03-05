package pt.ist.fenixframework.example.tpcw.domain;

public class Country extends Country_Base {
    
    public Country(String name, double currency, String exchange, int co_id) {
	super();
    
	setName(name);
	setCurrency(currency);
	setExchange(exchange);
	setCo_id(co_id);
    }

    public Address findOrCreateAddress(String street1, String street2, String city, String state, String zip) {
	Address foundAddress = null;
	for (Address address : getAddresses()) {
	    if (address.matchesExceptCountry(street1, street2, city, state, zip)) {
		return address;
	    }
	}
	int addr_id = getRoot().getNumAddrIds() + 1;
	getRoot().setNumAddrIds(addr_id);
	return new Address(street1, street2, city, state, zip, this, addr_id);
    }
}
