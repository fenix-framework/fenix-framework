package pt.ist.fenixframework;

import java.util.Set;

import pt.ist.fenixframework.pstm.AbstractDomainObject;

public interface TxIntrospector {
    public Set<AbstractDomainObject> getNewObjects();

    public Set<AbstractDomainObject> getModifiedObjects();

    public Set<Entry> getReadSetLog();

    public Set<Entry> getWriteSetLog();

    public Set<RelationChangelog> getRelationsChangelog();

    public static class Entry {
        public final AbstractDomainObject object;
        public final String attribute;
        public final Object value;

        public Entry(AbstractDomainObject object, String attribute, Object value) {
            this.object = object;
            this.attribute = attribute;
            this.value = value;
        }
    }

    public static class RelationChangelog {
        public final String relation;
        public final AbstractDomainObject first;
        public final AbstractDomainObject second;
        public final boolean remove;

        public RelationChangelog(String relation, AbstractDomainObject first, AbstractDomainObject second, boolean remove) {
            this.relation = relation;
            this.first = first;
            this.second = second;
            this.remove = remove;
        }
    }
}
