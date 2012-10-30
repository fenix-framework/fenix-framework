package pt.ist.fenixframework.dml;

import java.util.*;
import java.net.URL;

public class DomainClass extends DomainEntity {
    private DomainEntity superclass;
    private List interfacesNames;

    private List<Slot> slots = new ArrayList<Slot>();
    private List<Role> roleSlots = new ArrayList<Role>();

    public DomainClass(URL sourceFile, String fullName, DomainEntity superclass, List interfacesNames) {
        super(sourceFile, fullName);

        this.superclass = superclass;

        if (interfacesNames == null) {
            interfacesNames = new ArrayList();
        }
        this.interfacesNames = interfacesNames;
    }

    public boolean hasSuperclass() {
        return getSuperclass() != null;
    }

    public DomainEntity getSuperclass() {
        return superclass;
    }

    public String getSuperclassName() {
        return ((superclass == null) ? null : superclass.getName());
    }

    public Iterator getInterfaceNamesIterator() {
        return interfacesNames.iterator();
    }

    public List getInterfacesNames() {
        return interfacesNames;
    }

    public void addSlot(Slot slot) {
        slots.add(slot);
    }

    public Iterator<Slot> getSlots() {
        return slots.iterator();
    }

    public List<Slot> getSlotsList() {
        return Collections.unmodifiableList(slots);
    }

    public boolean hasSlots() {
        return ((! slots.isEmpty()) || (! roleSlots.isEmpty()));
    }

    public Slot findSlot(String slotName) {
        if (slotName == null) {
            return null;
        }

        for (Slot s : slots) {
            if (s.getName().equals(slotName)) {
                return s;
            }
        }

        return (superclass != null) ? superclass.findSlot(slotName) : null;
    }

    public void addRoleSlot(Role role) {
        roleSlots.add(role);
    }

    public Iterator<Role> getRoleSlots() {
        return roleSlots.iterator();
    }

    public List<Role> getRoleSlotsList() {
        return Collections.unmodifiableList(roleSlots);
    }

    public Role findRoleSlot(String roleName) {
        if (roleName == null) {
            return null;
        }

        for (Role r : roleSlots) {
            if (roleName.equals(r.getName())) {
                return r;
            }
        }

        return (superclass != null) ? superclass.findRoleSlot(roleName) : null;
    }

    public boolean hasSlotWithOption(Slot.Option option) {
        for (Slot slot : slots) {
            if (slot.hasOption(option)) {
                return true;
            }
        }
        return false;
    }
}
