package test.backend.jvstm.domain;

public class ParametricValueType<V, U> {

    private final V value;
    private final U unit;

    public ParametricValueType(V value, U unit) {
        this.value = value;
        this.unit = unit;
    }

    public V getValue() {
        return value;
    }

    public U getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParametricValueType)) {
            return false;
        }
        ParametricValueType other = (ParametricValueType) obj;
        boolean equal = true;
        equal &= (this.value == other.value) || (this.value != null && this.value.equals(other.value));
        equal &= (this.unit == other.unit) || (this.unit != null && this.unit.equals(other.unit));

        return equal;
    }

    @Override
    public int hashCode() {
        return (value == null ? 0 : value.hashCode()) + (unit == null ? 0 : unit.hashCode());
    }

    @Override
    public String toString() {
        return (value == null ? "unknown" : value) + (unit == null ? " unit" : " " + unit);
    }

}
