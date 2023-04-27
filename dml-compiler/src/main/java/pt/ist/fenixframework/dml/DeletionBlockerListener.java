package pt.ist.fenixframework.dml;

import java.util.Collection;

import pt.ist.fenixframework.DomainObject;

/**
 * A {@link DeletionBlockerListener} gives its implementations a chance to aid domain objects
 * in determining whether they can be safely deleted.
 * 
 * @author João Carvalho (joao.pedro.carvalho@tecnico.ulisboa.pt)
 *
 */
public interface DeletionBlockerListener<T extends DomainObject> {

    /**
     * Returns a list of all the blockers that prevent this object from being deleted.
     * 
     * Each blocker must be returned as a human-friendly textual description of the blocker, ideally in the
     * user's preferred language (if applicable).
     * 
     * @param object
     *            The object being checked.
     * @param blockers
     *            A mutable collection where all the blockers that may prevent this object from being deleted should be added.
     */
    public void getDeletionBlockers(T object, Collection<String> blockers);

}
