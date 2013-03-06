package pt.ist.fenixframework.dml;

import java.io.Serializable;
import java.util.Comparator;

public class Role implements Serializable {
    public static final int MULTIPLICITY_MANY = -1;

    public static final Comparator<Role> COMPARATOR_BY_NAME_OR_TYPE = new Comparator<Role>() {
        @Override
        public int compare(Role one, Role other) {
            if (one.getName() == null) {
                return -1;
            }
            if (other.getName() == null) {
                return 1;
            }
            if (!one.getName().equals(other.getName())) {
                return one.getName().compareTo(other.getName());
            } else {
                return one.getType().getFullName().compareTo(other.getType().getFullName());
            }
        }
    };

    private final String name;
    private final DomainEntity type;
    private int multiplicityLower = 0;
    private int multiplicityUpper = 1;
    private DomainRelation relation;
    private String indexProperty;
    private boolean ordered = false;
    private int indexCardinality;

    public Role(String name, DomainEntity type) {
        this.name = name;
        this.type = type;
    }

    public DomainRelation getRelation() {
        return relation;
    }

    public boolean isFirstRole() {
        return relation.getFirstRole() == this;
    }

    public Role getOtherRole() {
        return (isFirstRole() ? relation.getSecondRole() : relation.getFirstRole());
    }

    public void setRelation(DomainRelation relation) {
        this.relation = relation;
    }

    public String getName() {
        return name;
    }

    public DomainEntity getType() {
        return type;
    }

    public void setMultiplicity(int lower, int upper) {
        this.multiplicityLower = lower;
        this.multiplicityUpper = upper;
    }

    public int getMultiplicityLower() {
        return multiplicityLower;
    }

    public int getMultiplicityUpper() {
        return multiplicityUpper;
    }

    public void setIndexProperty(String propName) {
        this.indexProperty = propName;
    }

    public String getIndexProperty() {
        return indexProperty;
    }

    public void setIndexCardinality(int cardinality) {
        this.indexCardinality = cardinality;
    }

    public int getIndexCardinality() {
        return this.indexCardinality;
    }

    public boolean isIndexed() {
        return getIndexProperty() != null;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public boolean needsMultiplicityChecks() {
        return (multiplicityLower > 0) || ((multiplicityUpper > 1) && (multiplicityUpper != MULTIPLICITY_MANY));
    }

    /**
     * Indicates whether the relation is direct on this role
     */
    public boolean isDirect() {
        return this.isFirstRole() || this.getOtherRole().getName() == null;
    }
}
