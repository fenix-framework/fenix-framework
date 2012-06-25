package pt.ist.fenixframework.dml;

import java.util.*;
import java.net.URL;

public class DomainRelation extends DomainClass {
    private List<Role> roles = new ArrayList<Role>();


    public DomainRelation(URL sourceFile, String name, DomainRelation superrelation, List interfaces) {
        super(sourceFile, name, superrelation, interfaces);
    }

    public DomainRelation getSuperrelation() {
        return (DomainRelation)getSuperclass();
    }

    public void addRole(Role role) {
        role.setRelation(this);
        roles.add(role);
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

