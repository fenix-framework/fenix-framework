package pt.ist.fenixframework.adt.skiplist;

import java.io.Serializable;

import pt.ist.fenixframework.core.Externalization;

public class Externalizer {
    public static Serializable externalizeNodeArray(ForwardArray nodeArray) {
        return new NodeArrayExternalization(nodeArray);
    }

    public static ForwardArray internalizeNodeArray(Serializable externalizedNodeArray) {
        return ((NodeArrayExternalization)externalizedNodeArray).toNodeArray();
    }

    private static class NodeArrayExternalization implements Serializable {
        private static final long serialVersionUID = 1L;

        private byte[] serializedNodeArray;

        NodeArrayExternalization(ForwardArray nodeArray) {
            this.serializedNodeArray = Externalization.externalizeSerializable(nodeArray);
        }

        ForwardArray toNodeArray() {
            return (ForwardArray)Externalization.internalizeSerializable(serializedNodeArray);
        }
    }
    
}
