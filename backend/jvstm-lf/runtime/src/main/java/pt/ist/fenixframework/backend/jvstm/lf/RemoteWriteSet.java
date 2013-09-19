/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm.lf;

import jvstm.GarbageCollectable;
import jvstm.VBoxBody;
import jvstm.WriteSet;
import jvstm.util.Cons;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.backend.jvstm.pstm.VBox;

public class RemoteWriteSet extends WriteSet {

    private static final Logger logger = LoggerFactory.getLogger(RemoteWriteSet.class);

    /* This empty WriteSet instance is used to quickly initialize this
    RemoteCommitOnlyTransaction instance.  Remember that such code is running
    in Hazelcast's delivery thread and we want it to be quick.   Later, when we
    decide on the validation status of this request we'll CAS this write set
    for the real one. */
    public static final WriteSet EMPTY = new RemoteWriteSet();

    private RemoteWriteSet() {
        super(new VBox[0], WriteSet.DEFAULT_BLOCK_SIZE);
    }

    public RemoteWriteSet(SimpleWriteSet writeSet) {
        super(makeVBoxesFromIds(writeSet), DEFAULT_BLOCK_SIZE);
    }

    private static VBox[] makeVBoxesFromIds(SimpleWriteSet writeSet) {
        VBox[] vboxes = new VBox[writeSet.getNumElements()];

        int pos = 0;
        for (String id : writeSet.getVboxIds()) {
            vboxes[pos++] = JvstmLockFreeBackEnd.getInstance().vboxFromId(id);
        }
        return vboxes;
    }

    /* The write back loop must remain inverted.  See super.writeBackLoop(...)
    for an explanation. */
    @Override
    protected Cons<GarbageCollectable> writeBackLoop(int newTxNumber, int min, int max, jvstm.VBox[] vboxes, Object[] values) {
        Object newValue = VBox.notLoadedValue();

        Cons<GarbageCollectable> newBodies = Cons.empty();
        for (int i = max - 1; i >= min; i--) {
            jvstm.VBox vbox = vboxes[i];

            logger.debug("Will commit to vbox {} version {} with value {}", ((VBox) vbox).getId(), newTxNumber, newValue);

            VBoxBody newBody = vbox.commit(newValue, newTxNumber);

            newBodies = newBodies.cons(newBody);
        }
        return newBodies;
    }

}
