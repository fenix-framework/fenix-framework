package pt.ist.fenixframework;

import java.io.Serializable;

/**
 * All domain objects implement this interface.  It provides a way for the programmer to obtain a
 * global external identifier for the given object.  This identifier is an opaque from the
 * programmer's point of view.  Later, the corresponding object can be obtained by invoking {@link
 * pt.ist.fenixframework.FenixFramework#getDomainObject(String)} using the previously obtained
 * external identifier.
 */
public interface DomainObject extends Serializable {
    public String getExternalId();
}
