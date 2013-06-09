package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.VBoxBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

}
