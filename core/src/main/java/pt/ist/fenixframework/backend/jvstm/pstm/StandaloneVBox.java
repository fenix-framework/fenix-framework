package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.Transaction;
import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.JVSTMBackEnd;
import pt.ist.fenixframework.backend.jvstm.repository.PersistenceException;

public class StandaloneVBox<E> extends VBox<E> {

    private static final Logger logger = LoggerFactory.getLogger(StandaloneVBox.class);

    private final String id;

    protected StandaloneVBox(String id) {
        super();
        this.id = id;
    }

    protected StandaloneVBox(String id, E initial) {
        super(initial);
        this.id = id;
    }

    protected StandaloneVBox(String id, VBoxBody<E> body) {
        super(body);
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    protected void doReload() {
        logger.debug("Reload StandaloneVBox: {}", this.getId());

        try {
            JVSTMBackEnd.getInstance().getRepository().reloadAttribute(this);
        } catch (PersistenceException e) {
            VBoxBody<E> body = getBody(Transaction.current().getNumber());
            if (body != null && body.version == 0) {
                logger.debug("VBox is new. Setting null in body with version 0.");
                body.value = null;
            } else {
                String message = "BUG: After reload, body is " + (body == null ? "null" : "not null");
                if (body != null) {
                    message += ". version=" + body.version;
                }
                logger.error(message);
                throw e;
            }
        }
    }

    public static StandaloneVBox lookupCachedVBox(String vboxId) {
        return VBoxCache.getCache().lookup(vboxId);
    }

    public static <T> StandaloneVBox<T> makeNew(String vboxId, boolean allocateOnly) {
        if (allocateOnly) {
            // when a box is allocated, it is safe to say that the version number is 0
            return new StandaloneVBox<T>(vboxId, VBox.<T> notLoadedBody());
        } else {
            StandaloneVBox<T> vbox = new StandaloneVBox<T>(vboxId);
            vbox.put(null); // this is required to add the vbox to the writeset
            return vbox;
        }
    }

}
