package pt.ist.fenixframework.dml;

import java.util.*;
import java.net.URL;

public class DomainExternalEntity extends DomainEntity {

    public DomainExternalEntity(URL sourceFile, String fullName) {
        super(sourceFile, fullName);
    }

    public void addRoleSlot(Role role) {
        // do nothing
    }

    public String getFullName(String packagePrefix) {
        return super.getFullName(null);
    }
}
