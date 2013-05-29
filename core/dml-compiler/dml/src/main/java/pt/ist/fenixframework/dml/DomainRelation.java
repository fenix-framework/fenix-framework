package pt.ist.fenixframework.dml;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DomainRelation extends DomainClass {
    private final List<Role> roles = new ArrayList<Role>();

    public DomainRelation(URL sourceFile, String name, DomainRelation superrelation, List interfaces) {
        super(sourceFile, name, superrelation, interfaces);
    }

    public DomainRelation getSuperrelation() {
        return (DomainRelation) getSuperclass();
    }

    public void addRole(Role role) {
        role.setRelation(this);
        roles.add(role);
        Collections.sort(roles, Role.COMPARATOR_BY_NAME_OR_TYPE);
    }

    public int countRoles() {
        DomainRelation superRel = getSuperrelation();
        return roles.size() + ((superRel == null) ? 0 : superRel.countRoles());
    }

    public List<Role> getRoles() {
        int numRoles = countRoles();
        List<Role> result = new ArrayList<Role>(numRoles);
        copyRolesInto(this, result);
        return result;
    }

    public Role getFirstRole() {
        return roles.get(0);
    }

    public Role getSecondRole() {
        return roles.get(1);
    }

    private static void copyRolesInto(DomainRelation rel, List<Role> result) {
        DomainRelation superRel = rel.getSuperrelation();
        if (superRel != null) {
            copyRolesInto(superRel, result);
        }
        result.addAll(rel.roles);
    }
}
