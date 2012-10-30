package pt.ist.fenixframework;

import java.util.Set;

public interface TxIntrospector {
    public Set<DomainObject> getNewObjects();
    public Set<DomainObject> getModifiedObjects();
    public Set<Entry> getReadSetLog();
    public Set<Entry> getWriteSetLog();
    public Set<RelationChangelog> getRelationsChangelog();

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
    }
}
