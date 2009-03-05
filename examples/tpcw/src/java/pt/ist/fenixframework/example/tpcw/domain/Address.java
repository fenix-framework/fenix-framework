package pt.ist.fenixframework.example.tpcw.domain;

public class Address extends Address_Base {
    
    public Address(String street1, String street2, String city, String state,
		   String zip, Country country, int addr_id) {
	super();
    
	setStreet1(street1);
	setStreet2(street2);
	setCity(city);
	setState(state);
	setZip(zip);
	setCountry(country);
	setAddr_id(addr_id);
    }

    public boolean matchesExceptCountry(String street1, String street2, String city, String state, String zip) {
	String fieldAux;
	if ((street1 == getStreet1() || (street1 != null && street1.equals(getStreet1())))
	    && (street2 == getStreet2() || (street2 != null && street2.equals(getStreet2())))
	    && (city == getCity() || (city != null && city.equals(getCity())))
	    && (state == getState() || (state != null && state.equals(getState())))
	    && (zip == getZip() || (zip != null && zip.equals(getZip())))) {
	    return true;
	}
	return false;
    }
}
