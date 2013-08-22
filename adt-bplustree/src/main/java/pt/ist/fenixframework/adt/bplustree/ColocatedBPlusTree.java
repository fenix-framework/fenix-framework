package pt.ist.fenixframework.adt.bplustree;

import eu.cloudtm.Constants;
import eu.cloudtm.LocalityHints;

import java.io.Serializable;
import java.util.UUID;

public class ColocatedBPlusTree<T extends Serializable> extends ColocatedBPlusTree_Base {

    public ColocatedBPlusTree() {
        // this is not used by the code generator
    }

    public ColocatedBPlusTree(LocalityHints localityHints, String relationName) {
        super(localityHints == null ?
                new LocalityHints(new String[]{Constants.GROUP_ID, UUID.randomUUID().toString(), Constants.RELATION_NAME,
                        relationName}) :
                localityHints.clone().addHint(Constants.RELATION_NAME, relationName));
    }
    
    public ColocatedBPlusTree(String className, String relationName) {
        super(new LocalityHints(new String[]{Constants.GROUP_ID, className, Constants.RELATION_NAME, relationName}));
    }

}
