package test.backend.jvstm.domain;

import java.math.BigDecimal;

// This class tests two situations:
// 1) a value type with more than one element
// 2) a value type that is decomposed in another (non-built-in) value type

public class CompositeValueType {

    private BigDecimal value;
    private String unit;

    public static CompositeValueType fromComponents(BigDecimal value, String unit) {
        CompositeValueType cvt = new CompositeValueType();
        cvt.value = value;
        cvt.unit = unit;
        return cvt;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompositeValueType)) {
            return false;
        }
        CompositeValueType other = (CompositeValueType) obj;
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
        return (value == null ? "unknown" : value.toString()) + (unit == null ? " unit" : " " + unit);
    }

}
