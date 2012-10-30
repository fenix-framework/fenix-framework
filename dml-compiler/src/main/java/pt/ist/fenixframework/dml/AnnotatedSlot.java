package pt.ist.fenixframework.dml;

import java.io.Serializable;

public class AnnotatedSlot implements Serializable {

    private DomainClass domainClass;
    private Slot slot;
    
    public AnnotatedSlot(DomainClass domainClass, Slot slot) {
	this.domainClass = domainClass;
	this.slot = slot;
    }

    public DomainClass getDomainClass() {
        return domainClass;
    }

    public void setDomainClass(DomainClass domainClass) {
        this.domainClass = domainClass;
    }

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }
    
}
