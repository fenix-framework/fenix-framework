package pt.ist.fenixframework.dml;

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

}
