package pt.ist.fenixframework.backend.jvstm;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstm.repository.NoRepository;

import java.util.HashSet;

/***
 * 
 * This backend is necessary since {@link JVSTMBackEnd} throws {@link UnsupportedOperationException} when invoking
 * {@link FenixFramework#isDomainObjectValid(DomainObject)}
 * 
 * @author Sérgio Silva (sergio.silva@tecnico.ulisboa.pt)
 * @see JVSTMBackEnd#isDomainObjectValid(DomainObject)
 * 
 */
public class InMemDomainObjectValidBackEnd extends JVSTMBackEnd {
    
    public static final String BACKEND_NAME = "inmem-backend";
    
    
    protected final HashSet<Object> deletedObjects;
    
    public InMemDomainObjectValidBackEnd() {
        super(new NoRepository());
        deletedObjects = new HashSet<>();
    }
    
    @Override
    public <T extends DomainObject> T fromOid(Object oid) {
        if (deletedObjects.contains(oid))
            return null;
        else
            return super.fromOid(oid);
    }
    
    protected void deleteObject(Object oid){
        deletedObjects.add(oid);
    }
    
    @Override
    public boolean isDomainObjectValid(DomainObject object) {
        return object != null && !deletedObjects.contains(object.getExternalId());
    }

    
    
}