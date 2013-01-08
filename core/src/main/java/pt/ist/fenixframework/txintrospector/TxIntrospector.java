package pt.ist.fenixframework.txintrospector;

import java.util.Collection;

import pt.ist.fenixframework.DomainObject;

/**
 * The TxIntrospector class allows access to internal details from transactions
 * executed in the system.
 */
public interface TxIntrospector {

    public static final String TXINTROSPECTOR_ON_CONFIG_KEY = "ptIstTxIntrospectorEnable";
    public static final String TXINTROSPECTOR_ON_CONFIG_VALUE = "true";

    /**
     * Returns a Collection containing new objects created during this
     * transaction.
     * 
     * @return Collection of new objects
     */
    public Collection<DomainObject> getNewObjects();

    /**
     * Returns a Collection containing objects which had any of their slots
     * modified, excluding those that only had relationship changes.
     * 
     * @return Collection of directly modified objects
     */
    public Collection<DomainObject> getDirectlyModifiedObjects();

    /**
     * Returns a Collection containing both objects which had any of their slots
     * modified and those that had relationship changes.
     * 
     * @return Collection of modified objects
     */
    public Collection<DomainObject> getModifiedObjects();

    /**
     * Returns the read-set of this transaction.
     * 
     * @return Read-set of the this transaction
     */
    public Collection<Entry> getReadSetLog();

    /**
     * Returns the write-set of this transaction.
     * 
     * @return Write-set of the this transaction
     */
    public Collection<Entry> getWriteSetLog();

    /**
     * Returns a Collection containing the relations changed during this
     * transaction.
     * 
     * @return Collection of relation changes
     */
    public Collection<RelationChangelog> getRelationsChangelog();

    /**
     * Adds the {@link RelationChangeLog} to the list of relations changed
     * during this transaction.
     * 
     * @param relationChangelog
     *            The change log to be added.
     */
    void addModifiedRelation(RelationChangelog relationChangelog);

    /**
     * Used to represent a read or write-set entry.
     */
    public static class Entry {
	public final DomainObject object;
	public final String attribute;
	public final Object value;

	public Entry(DomainObject object, String attribute, Object value) {
	    this.object = object;
	    this.attribute = attribute;
	    this.value = value;
	}
    }

    /**
     * Used to represent a change to a relationship between two objects.
     */
    public static class RelationChangelog {
	public final String relation;
	public final DomainObject first;
	public final DomainObject second;
	public final boolean remove;

	public RelationChangelog(String relation, DomainObject first, DomainObject second, boolean remove) {
	    this.relation = relation;
	    this.first = first;
	    this.second = second;
	    this.remove = remove;
	}

	@Override
	public int hashCode() {
	    return relation.hashCode() + (first != null ? first.hashCode() : 0) + (second != null ? second.hashCode() : 0);
	}

	@Override
	public boolean equals(Object obj) {
	    if ((obj != null) && (obj instanceof RelationChangelog)) {
		RelationChangelog other = (RelationChangelog) obj;
		return this.relation.equals(other.relation)
			&& (this.first == other.first || (this.first != null && this.first.equals(other.first)))
			&& (this.second == other.second || (this.second != null && this.second.equals(other.second)));
	    }
	    return false;
	}

	@Override
	public String toString() {
	    return "Relation '" + relation + "' " + first + "---" + second + " (" + (remove ? "removed)" : "changed)");
	}
    }

}
