package pt.ist.fenixframework.txintrospector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.BackEndId;
import pt.ist.fenixframework.dml.runtime.Relation;
import pt.ist.fenixframework.dml.runtime.RelationListener;

public class TxStats implements TxIntrospector {

    public static final boolean ENABLED = checkEnabled();

    public static boolean checkEnabled() {
	String param = BackEndId.getBackEndId().getParam(TXINTROSPECTOR_ON_CONFIG_KEY);
	return (param != null) && param.trim().equalsIgnoreCase(TXINTROSPECTOR_ON_CONFIG_VALUE);
    }

    private static final boolean FILTER = Boolean.getBoolean("pt.ist.fenixframework.txintrospector.filter");

    private final List<DomainObject> newObjects = new ArrayList<DomainObject>();
    private final Set<DomainObject> modifiedObjects = new HashSet<DomainObject>();
    private final Map<RelationChangelog, RelationChangelog> relationsChangelog = new HashMap<RelationChangelog, RelationChangelog>();

    private TxStats() {
    }

    public static TxStats newInstance() {
	if (!ENABLED)
	    return null;
	return new TxStats();
    }

    public static TxStats getInstance(TxStats txStats) {
	if (txStats == null) {
	    throw new RuntimeException("TxIntrospector is disabled, please enable it and rebuild your application");
	}
	return txStats;
    }

    public void addNewObject(DomainObject object) {
	if (filter(object))
	    return;

	newObjects.add(object);
    }

    public void addModifiedObject(DomainObject object) {
	if (filter(object))
	    return;

	modifiedObjects.add(object);
    }

    @Override
    public void addModifiedRelation(RelationChangelog relation) {
	if (filter(relation.first))
	    return;

	RelationChangelog prev = relationsChangelog.put(relation, relation);
	if (prev != null && (prev.remove != relation.remove)) {
	    // Two changelog entries with opposite remove values cancel out
	    relationsChangelog.remove(relation);
	}
    }

    // Filter out internal Fenix Framework structures
    private boolean filter(DomainObject object) {
	return FILTER && object.getClass().getPackage().getName().startsWith("pt.ist.fenixframework.adt.");
    }

    private static String collectionToStringSafe(Collection<? extends Object> collection) {
	StringBuilder sb = new StringBuilder();
	sb.append('[');
	for (Object o : collection) {
	    sb.append(o.getClass().getName());
	    sb.append('@');
	    sb.append(Integer.toHexString(System.identityHashCode(o)));
	    sb.append(", ");
	}
	if (sb.length() > 1)
	    sb.delete(sb.length() - 2, sb.length());
	sb.append(']');
	return sb.toString();
    }

    @Override
    public String toString() {
	return "TxStats\n\tnewObjects: " + collectionToStringSafe(getNewObjects()) + "\n\tdirectlyModifiedObjects: "
		+ collectionToStringSafe(getDirectlyModifiedObjects()) + "\n\tmodifiedObjects: "
		+ collectionToStringSafe(getModifiedObjects()) + "\n\trelationsChangelog: "
		+ collectionToStringSafe(getRelationsChangelog());
    }

    /**
     * Version of toString() that may fail because it uses the DomainObject's
     * toString() operation to print them, as calling toString() on a
     * DomainObject may cause further changes to objects or relations.
     */
    public String unsafeToString() {
	return "TxStats\n\tnewObjects: " + getNewObjects() + "\n\tdirectlyModifiedObjects: " + getDirectlyModifiedObjects()
		+ "\n\tmodifiedObjects: " + getModifiedObjects() + "\n\trelationsChangelog: " + getRelationsChangelog();
    }

    // TxIntrospector implementation
    @Override
    public Collection<DomainObject> getNewObjects() {
	return Collections.unmodifiableList(newObjects);
    }

    @Override
    public Collection<DomainObject> getDirectlyModifiedObjects() {
	Set<DomainObject> realModifiedObjects = new HashSet<DomainObject>(modifiedObjects);

	// Any slot change triggers an object being added to modifiedObjects
	// However, we want to separate new objects (even with modified slots)
	// from existing objects
	// that were modified, so we duplicate the modifiedObjects set, and
	// remove from it the newObjects
	realModifiedObjects.removeAll(newObjects);

	return Collections.unmodifiableSet(realModifiedObjects);
    }

    @Override
    public Collection<DomainObject> getModifiedObjects() {
	Set<DomainObject> realModifiedObjects = new HashSet<DomainObject>(modifiedObjects);

	// Add objects from relationship changes
	for (RelationChangelog relationChange : relationsChangelog.values()) {
	    /* if (relationChange.first != null) */realModifiedObjects.add(relationChange.first);
	    /* if (relationChange.second != null) */realModifiedObjects.add(relationChange.second);
	}

	// Separate new objects from existing objects that were modified
	realModifiedObjects.removeAll(newObjects);

	return Collections.unmodifiableSet(realModifiedObjects);
    }

    @Override
    public Collection<Entry> getReadSetLog() {
	throw new RuntimeException("Not implemented");
    }

    @Override
    public Collection<Entry> getWriteSetLog() {
	throw new RuntimeException("Not implemented");
    }

    @Override
    public Collection<RelationChangelog> getRelationsChangelog() {
	return Collections.unmodifiableCollection(relationsChangelog.values());
    }

    // RelationListener used to track changes to relations
    @SuppressWarnings("rawtypes")
    public static final RelationListener STATS_LISTENER = new RelationListener<DomainObject, DomainObject>() {
	@Override
	public void beforeAdd(Relation<DomainObject, DomainObject> rel, DomainObject o1, DomainObject o2) {
	    if (o1 == null || o2 == null)
		return; // Ignore relations with null
	    FenixFramework.getTransaction().getTxIntrospector()
		    .addModifiedRelation(new RelationChangelog(rel.getName(), o1, o2, false));
	}

	@Override
	public void afterAdd(Relation<DomainObject, DomainObject> rel, DomainObject o1, DomainObject o2) {
	    // intentionally empty
	}

	@Override
	public void beforeRemove(Relation<DomainObject, DomainObject> rel, DomainObject o1, DomainObject o2) {
	    if (o1 == null || o2 == null)
		return; // Ignore relations with null
	    FenixFramework.getTransaction().getTxIntrospector()
		    .addModifiedRelation(new RelationChangelog(rel.getName(), o1, o2, true));
	}

	@Override
	public void afterRemove(Relation<DomainObject, DomainObject> rel, DomainObject o1, DomainObject o2) {
	    // intentionally empty
	}
    };
}
