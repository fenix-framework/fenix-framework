package pt.ist.fenixframework.backend.jvstm;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.backend.jvstm.JVSTMConfig;

/***
 * 
 * Used to initialize {@link FenixFramework} with {@link InMemDomainObjectValidBackEnd}
 * 
 * Used in fenix-framework.properties for testing.
 * 
 * @author Sérgio Silva (sergio.silva@tecnico.ulisboa.pt)
 *
 */
public class InMemDomainObjectValidConfig extends JVSTMConfig {

    public InMemDomainObjectValidConfig() {
        this.backEnd = new InMemDomainObjectValidBackEnd();
    }
}