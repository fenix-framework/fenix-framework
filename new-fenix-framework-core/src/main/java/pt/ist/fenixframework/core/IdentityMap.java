package pt.ist.fenixframework.core;

public interface IdentityMap {

    public AbstractDomainObject cache(AbstractDomainObject obj);
    public AbstractDomainObject lookup(Object key);
}
