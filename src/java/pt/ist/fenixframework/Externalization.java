package pt.ist.fenixframework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Externalization {


    public static byte[] externalizeSerializable(Serializable obj) {
        return externalizeSerializable(obj, false);
    }

    public static <T> T internalizeSerializable(byte[] bytes) {
        return (T) internalizeSerializable(bytes, false);
    }

    public static byte[] externalizeSerializableGZiped(Serializable obj) {
        return externalizeSerializable(obj, true);
    }

    public static <T> T internalizeSerializableGZiped(byte[] bytes) {
        return (T) internalizeSerializable(bytes, true);
    }



    private static byte[] externalizeSerializable(Serializable obj, boolean gziped) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(gziped ? new GZIPOutputStream(baos) : baos);
            oos.writeObject(obj);
            oos.close();
            return baos.toByteArray();
        } catch (IOException ioe) {
            throw new ExternalizationException(ioe);
        }
    }

    private static Object internalizeSerializable(byte[] bytes, boolean gziped) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(gziped ? new GZIPInputStream(bais) : bais);
            Object result = ois.readObject();
            ois.close();
            return result;
        } catch (IOException ioe) {
            throw new ExternalizationException(ioe);
        } catch (ClassNotFoundException cnfe) {
            throw new ExternalizationException(cnfe);
        }
    }
}
