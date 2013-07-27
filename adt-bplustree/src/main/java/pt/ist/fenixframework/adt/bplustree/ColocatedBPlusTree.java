package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.UUID;

import pt.ist.fenixframework.dml.runtime.DomainBasedMap;

import eu.cloudtm.Constants;
import eu.cloudtm.LocalityHints;

public class ColocatedBPlusTree<T extends Serializable> extends ColocatedBPlusTree_Base {

    public ColocatedBPlusTree() {
	// this is not used by the code generator
    }
    
    public ColocatedBPlusTree(String relationName) {
	super(new LocalityHints(new String[]{Constants.GROUP_ID, UUID.randomUUID().toString() + DomainBasedMap.RELATION_NAME_SEPARATOR + relationName}));
    }

}
