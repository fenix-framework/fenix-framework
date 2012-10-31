package pt.ist.fenixframework.project;

import java.io.IOException;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import pt.ist.fenixframework.artifact.FenixFrameworkArtifact;
import pt.ist.fenixframework.project.exception.FenixFrameworkProjectException;
import pt.ist.fenixframework.project.exception.MissingRootDomainClassException;
import pt.ist.fenixframework.project.persistence.PersistenceInfo;

public class FenixFrameworkProject {

    public static final String ROOT_DOMAIN_CLASS_KEY = "root-class";

    private FenixFrameworkArtifact artifact;
    private PersistenceInfo persistenceInfo;
    private String rootDomainClassFullyQualifiedName;

    public FenixFrameworkProject withArtifact(FenixFrameworkArtifact artifact) {
	this.artifact = artifact;
	return this;
    }

    public FenixFrameworkProject withPersistenceInfo(PersistenceInfo persistenceInfo) {
	this.persistenceInfo = persistenceInfo;
	return this;
    }

    public FenixFrameworkProject withRootDomainClass(String rootDomainClassFullyQualifiedName) {
	this.rootDomainClassFullyQualifiedName = rootDomainClassFullyQualifiedName;
	return this;
    }

    public FenixFrameworkArtifact getArtifact() {
	return artifact;
    }

    public PersistenceInfo getPersistenceInfo() {
	return persistenceInfo;
    }

    public String getRootDomainClassFullyQualifiedName() {
	return rootDomainClassFullyQualifiedName;
    }

    public static FenixFrameworkProject fromProperties(Properties properties) throws FenixFrameworkProjectException, IOException{
	String rootDomainClassFullyQualifiedName = properties.getProperty(ROOT_DOMAIN_CLASS_KEY);
	if(StringUtils.isBlank(rootDomainClassFullyQualifiedName)) {
	    throw new MissingRootDomainClassException();
	}
	FenixFrameworkProject fenixFrameworkProject = new FenixFrameworkProject()
		.withArtifact(FenixFrameworkArtifact.fromProperties(properties))
		.withPersistenceInfo(PersistenceInfo.fromProperties(properties))
		.withRootDomainClass(rootDomainClassFullyQualifiedName);
	fenixFrameworkProject.validate();
	return fenixFrameworkProject;

    }

    protected void validate() throws FenixFrameworkProjectException {
	artifact.validate();
	persistenceInfo.validate();
    }
    
}
