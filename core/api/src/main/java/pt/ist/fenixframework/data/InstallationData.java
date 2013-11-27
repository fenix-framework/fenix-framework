package pt.ist.fenixframework.data;

import java.util.Collection;

import pt.ist.fenixframework.DomainRoot;
import pt.ist.fenixframework.core.Project;

public class InstallationData extends InstallationData_Base {

    public InstallationData(DomainRoot domainRoot) {
        super();
        this.setDomainRoot(domainRoot);
    }

    public void updateModuleData(Project project) {
        this.setModuleData(new ModuleData(project.getProjects()));
    }

    public Collection<ModuleInstallation> getInstalledModules() {
        return getModuleData().getInstalledModules();
    }

}
