package pt.ist.fenixframework.backend.infinispan.messaging;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public enum MessageType {
    CH_REQUEST,
    CH_UPDATE,
    WORK_REQUEST,
    ADDRESS_REQUEST;
    public final byte type() {
        return (byte) this.ordinal();
    }

    public static MessageType from(byte type) {
        return values()[type];
    }

}
