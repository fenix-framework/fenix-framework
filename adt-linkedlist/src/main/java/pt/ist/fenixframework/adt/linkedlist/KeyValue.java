package pt.ist.fenixframework.adt.linkedlist;

import java.io.Serializable;

import pt.ist.fenixframework.core.Externalization;

public class KeyValue implements Serializable {

    public Comparable key;
    public Serializable value;
    
    public KeyValue() {
	
    }
    
    public KeyValue(Comparable key, Serializable value) {
	this.key = key;
	this.value = value;
    }
    
    public static Serializable externalizeKeyValue(KeyValue keyValue) {
        return new KeyValueExternalization(keyValue);
    }

    public static KeyValue internalizeKeyValue(Serializable externalizedKeyValue) {
        return ((KeyValueExternalization)externalizedKeyValue).toKeyValue();
    }

    private static class KeyValueExternalization implements Serializable {
        private static final long serialVersionUID = 1L;

        private byte[] serializedKeyValue;

        KeyValueExternalization(KeyValue keyValue) {
            this.serializedKeyValue = Externalization.externalizeSerializable(keyValue);
        }

        KeyValue toKeyValue() {
            return (KeyValue)Externalization.internalizeSerializable(serializedKeyValue);
        }
    }
}
