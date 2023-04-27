package pt.ist.fenixframework;


@SuppressWarnings("all")
public abstract class DomainRoot_Base extends pt.ist.fenixframework.core.AbstractDomainObject {
    pt.ist.fenixframework.data.InstallationData installationData = null;
    
    public static pt.ist.fenixframework.dml.runtime.DirectRelation<pt.ist.fenixframework.data.InstallationData,pt.ist.fenixframework.DomainRoot> getRelationDomainRootHasInstallationData() {
        return new pt.ist.fenixframework.dml.runtime.DirectRelation(null, null);
    }
    
    protected  DomainRoot_Base() {
        super();
    }
    
    public pt.ist.fenixframework.data.InstallationData getInstallationData() {
        return installationData;
    }
    
    public void setInstallationData(pt.ist.fenixframework.data.InstallationData installationData) {
        this.installationData  = installationData;
    }
}
