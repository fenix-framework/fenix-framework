package pt.ist.fenixframework.dml;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Super-class for all domain entities that support {@link Modifier}s.
 * 
 * @author João Carvalho (joao.pedro.carvalho@ist.utl.pt)
 * 
 */
public abstract class ModifiableEntity {

    private final Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);

    public ModifiableEntity() {

    }

    public Set<Modifier> getModifiers() {
        return Collections.unmodifiableSet(modifiers);
    }

    public void addModifier(Modifier modifier) {
        modifiers.add(modifier);
    }

    public boolean hasModifier(Modifier modifier) {
        return modifiers.contains(modifier);
    }

}
