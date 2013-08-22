package pt.ist.fenixframework.adt.bplustree;

import eu.cloudtm.Constants;
import eu.cloudtm.LocalityHints;

import java.io.Serializable;
import java.util.UUID;

public class ColocatedBPlusTreeGhost<T extends Serializable> extends ColocatedBPlusTreeGhost_Base {

    public ColocatedBPlusTreeGhost() {
        // this is not used by the code generator
    }

    public ColocatedBPlusTreeGhost(LocalityHints localityHints, String relationName) {
        super(localityHints == null ?
                new LocalityHints(new String[]{Constants.GROUP_ID, UUID.randomUUID().toString(), Constants.RELATION_NAME,
                        relationName}) :
                localityHints.clone().addHint(Constants.RELATION_NAME, relationName));
    }

    public ColocatedBPlusTreeGhost(String className, String relationName) {
        super(new LocalityHints(new String[]{Constants.GROUP_ID, className, Constants.RELATION_NAME, relationName}));
    }

    
}
