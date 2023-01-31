package pt.ist.fenixframework.data;


@SuppressWarnings("all")
public abstract class InstallationData_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    private pt.ist.fenixframework.DomainRoot domainRoot = null;
    private pt.ist.fenixframework.data.ModuleData moduleData = null;

    public static pt.ist.fenixframework.dml.runtime.DirectRelation<pt.ist.fenixframework.data.InstallationData,pt.ist.fenixframework.DomainRoot> getRelationDomainRootHasInstallationData() {
        return new pt.ist.fenixframework.dml.runtime.DirectRelation(null, null);
    }
    
    protected  InstallationData_Base() {
        super();
    }
    
    public pt.ist.fenixframework.data.ModuleData getModuleData() {
        return moduleData;
    }
    
    public void setModuleData(pt.ist.fenixframework.data.ModuleData moduleData) {
        this.moduleData = moduleData;
    }

    public pt.ist.fenixframework.DomainRoot getDomainRoot() {
        return domainRoot;
    }
    
    public void setDomainRoot(pt.ist.fenixframework.DomainRoot domainRoot) {
        this.domainRoot = domainRoot;
    }
}
