package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.UUID;

import eu.cloudtm.Constants;
import eu.cloudtm.LocalityHints;

public class ColocatedBPlusTree<T extends Serializable>  extends BPlusTree<T> {

    public ColocatedBPlusTree() {
	super(new LocalityHints(new String[]{Constants.GROUP_ID, UUID.randomUUID().toString()}));
    }
    
}
