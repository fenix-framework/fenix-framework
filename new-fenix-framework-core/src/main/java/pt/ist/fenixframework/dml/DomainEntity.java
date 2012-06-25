package pt.ist.fenixframework.dml;

import java.util.*;
import java.io.Serializable;
import java.net.URL;

public abstract class DomainEntity implements Serializable {
    private URL sourceFile;
    private String fullName;
    private String packageName = "";
    private String name;

    public DomainEntity(URL sourceFile, String fullName) {
        this.sourceFile = sourceFile;
        this.fullName = fullName;
        int pos = fullName.lastIndexOf('.');
        if (pos != -1) {
            this.packageName = fullName.substring(0, pos);
            this.name = fullName.substring(pos + 1);
        } else {
            this.name = fullName;
        }
    }

    public URL getSourceFile() {
        return sourceFile;
    }

    public String getFullName(String packagePrefix) {
        if ((packagePrefix == null) || (packagePrefix.length() == 0)) {
            return fullName;
        } else {
            return packagePrefix + "." + fullName;
        }
    }

    public String getFullName() {
        return getFullName(null);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getBaseName() {
        return name + "_Base";
    }

    public Slot findSlot(String slotName) {
        return null;
    }

    public Role findRoleSlot(String roleName) {
        return null;
    }

    public abstract void addRoleSlot(Role role);
}
