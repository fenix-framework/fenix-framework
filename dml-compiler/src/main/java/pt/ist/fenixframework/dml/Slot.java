package pt.ist.fenixframework.dml;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.Set;

public class Slot extends ModifiableEntity implements Serializable {

    public enum Option {
        REQUIRED
    }

    private final String name;
    private final ValueType type;
    private final Set<Option> slotOptions = EnumSet.noneOf(Option.class);

    public Slot(String name, ValueType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    @Deprecated
    public String getType() {
        return getTypeName();
    }

    public ValueType getSlotType() {
        return type;
    }

    public String getTypeName() {
        return type.getFullname();
    }

    public void addOption(Option option) {
        slotOptions.add(option);
    }

    public Set<Option> getOptions() {
        return slotOptions;
    }

    public boolean hasOption(Option option) {
        return slotOptions.contains(option);
    }

}
