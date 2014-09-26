package pt.ist.fenixframework.dml;

import java.util.List;

import pt.ist.fenixframework.DomainObject;

/**
 * A {@link DeletionListener} is invoked when an object is being deleted, before the Framework
 * performing any automated consistency-checks, giving the application a chance to handle the
 * deletion of the provided object.
 * 
 * The main goal of this interface is to allow inter-module deletes to work properly, by giving
 * each module a chance to interfere in the deletion of an object that is declared in another module,
 * but that cannot be deleted on its own.
 * 
 * @author João Carvalho (joao.pedro.carvalho@tecnico.ulisboa.pt)
 *
 */
public interface DeletionListener<T extends DomainObject> {

    /**
     * Notifies this object that the given {@link T} is being deleted, giving a chance to
     * perform additional business logic, prior to the actual deletion of the object.
     * 
     * @param object
     *            The object being deleted.
     */
    public void deleting(T object);

    /**
     * A {@link DeletionAdapter} gives its implementations a chance to aid domain objects
     * in determining whether they can be safely deleted.
     * 
     * @author João Carvalho (joao.pedro.carvalho@tecnico.ulisboa.pt)
     *
     */
    public static interface DeletionAdapter<T extends DomainObject> extends DeletionListener<T> {

        /**
         * Determines whether this object can safely be deleted, from a business-logic point of view.
         * Subclasses may override and use this method to properly determine the object's status.
         * 
         * @param object
         *            The object being checked.
         * @return
         *         Whether the given object can safely be deleted.
         */
        public List<String> getDeletionBlockers(T object);

    }
}
