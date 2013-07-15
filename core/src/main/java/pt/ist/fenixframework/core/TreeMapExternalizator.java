package pt.ist.fenixframework.core;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;

import java.io.*;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Pedro Ruivo
 * @since 2.9
 */
public class TreeMapExternalizator {

    public static <K extends Comparable, V extends Serializable> TreeMap<K, V> internalize(byte[] input) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);
        ObjectInputStream objectInputStream = null;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            if (objectInputStream.readBoolean()) {
                return null;
            }
            TreeMap<K, V> treeMap;
            Comparator<K> comparator = internalizeObject(objectInputStream);
            if (comparator == null) {
                treeMap = new TreeMap<K, V>();
            } else {
                treeMap = new TreeMap<K, V>(comparator);
            }
            int size = objectInputStream.readInt();
            for (int i = 0; i < size; ++i) {
                treeMap.put(TreeMapExternalizator.<K>internalizeObject(objectInputStream),
                        TreeMapExternalizator.<V>internalizeObject(objectInputStream));
            }
            return treeMap;
        } catch (Exception e) {
            throw new ExternalizationException(e);
        } finally {
            safeClose(objectInputStream);
        }
    }

    public static <K extends Comparable, V extends Serializable> byte[] externalize(TreeMap<K, V> treeMap) {
        ObjectOutputStream objectOutputStream = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            if (treeMap == null) {
                objectOutputStream.writeBoolean(true);
            } else {
                objectOutputStream.writeBoolean(false);
                externalizeObject(treeMap.comparator(), objectOutputStream);
                objectOutputStream.writeInt(treeMap.size());
                for (Map.Entry<K, V> entry : treeMap.entrySet()) {
                    externalizeObject(entry.getKey(), objectOutputStream);
                    externalizeSerializable(entry.getValue(), objectOutputStream);
                }
            }
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new ExternalizationException(e);
        } finally {
            safeClose(objectOutputStream);
        }
    }

    private static void externalizeObject(Object object, ObjectOutputStream objectOutputStream) throws IOException {
        if (object instanceof Serializable || object == null) {
            externalizeSerializable((Serializable) object, objectOutputStream);
            return;
        }
        throw new ExternalizationException("Comparator not serializable");
    }

    private static void externalizeSerializable(Serializable object, ObjectOutputStream objectOutputStream) throws IOException {
        Flag flag = Flag.flag(object);
        objectOutputStream.writeByte(flag.mask());
        switch (flag) {
            case DOMAIN_OBJECT:
                objectOutputStream.writeUTF(((DomainObject) object).getExternalId());
                break;
            case OTHER_OBJECT:
                objectOutputStream.writeObject(object);
                break;
        }
    }

    private static <T> T internalizeObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        Flag flag = Flag.mask(objectInputStream.readByte());
        switch (flag) {
            case NULL:
                return null;
            case NULL_OBJECT:
                return (T) Externalization.NULL_OBJECT;
            case DOMAIN_OBJECT:
                return (T) FenixFramework.getDomainObject(objectInputStream.readUTF());
            case OTHER_OBJECT:
                return (T) objectInputStream.readObject();
            default:
                throw new ExternalizationException("Unknown flag " + flag);
        }
    }

    private static void safeClose(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            //no-op
        }
    }

    private enum Flag {
        NULL,
        NULL_OBJECT,
        DOMAIN_OBJECT,
        OTHER_OBJECT;

        public static Flag mask(byte mask) {
            return values()[mask];
        }

        public static Flag flag(Object object) {
            if (object == null) {
                return Flag.NULL;
            } else if (object == Externalization.NULL_OBJECT) {
                return Flag.NULL_OBJECT;
            } else if (object instanceof DomainObject) {
                return Flag.DOMAIN_OBJECT;
            } else {
                return Flag.OTHER_OBJECT;
            }
        }

        public final byte mask() {
            return (byte) ordinal();
        }
    }

}
