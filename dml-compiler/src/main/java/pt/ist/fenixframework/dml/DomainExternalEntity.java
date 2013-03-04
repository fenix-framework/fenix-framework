package pt.ist.fenixframework.dml;

import java.net.URL;

public class DomainExternalEntity extends DomainEntity {

    public DomainExternalEntity(URL sourceFile, String fullName) {
        super(sourceFile, fullName);
    }

    @Override
    public void addRoleSlot(Role role) {
        // do nothing
    }

    @Override
    public String getFullName(String packagePrefix) {
        return super.getFullName(null);
    }
}
