package pt.ist.fenixframework.backend.infinispan.messaging;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public class SendBuffer implements ObjectOutput {

    private final ObjectOutputStream objectOutputStream;
    private final ByteArrayOutputStream byteArrayOutputStream;

    public SendBuffer() throws IOException, SecurityException {
        byteArrayOutputStream = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
    }

    @Override
    public void writeObject(Object obj) throws IOException {
        objectOutputStream.writeObject(obj);
    }

    @Override
    public void write(int val) throws IOException {
        objectOutputStream.write(val);
    }

    @Override
    public void write(byte[] buf) throws IOException {
        objectOutputStream.write(buf);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        objectOutputStream.write(buf, off, len);
    }

    @Override
    public void flush() throws IOException {
        objectOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        objectOutputStream.close();
    }

    @Override
    public void writeBoolean(boolean val) throws IOException {
        objectOutputStream.writeBoolean(val);
    }

    @Override
    public void writeByte(int val) throws IOException {
        objectOutputStream.writeByte(val);
    }

    @Override
    public void writeShort(int val) throws IOException {
        objectOutputStream.writeShort(val);
    }

    @Override
    public void writeChar(int val) throws IOException {
        objectOutputStream.writeChar(val);
    }

    @Override
    public void writeInt(int val) throws IOException {
        objectOutputStream.writeInt(val);
    }

    @Override
    public void writeLong(long val) throws IOException {
        objectOutputStream.writeLong(val);
    }

    @Override
    public void writeFloat(float val) throws IOException {
        objectOutputStream.writeFloat(val);
    }

    @Override
    public void writeDouble(double val) throws IOException {
        objectOutputStream.writeDouble(val);
    }

    @Override
    public void writeBytes(String str) throws IOException {
        objectOutputStream.writeBytes(str);
    }

    @Override
    public void writeChars(String str) throws IOException {
        objectOutputStream.writeChars(str);
    }

    @Override
    public void writeUTF(String str) throws IOException {
        objectOutputStream.writeUTF(str);
    }

    public void writeByteArray(byte[] array) throws IOException {
        if (array == null || array.length == 0) {
            objectOutputStream.writeInt(0);
            return;
        }
        objectOutputStream.writeInt(array.length);
        objectOutputStream.write(array);
    }

    public byte[] toByteArray() throws IOException {
        objectOutputStream.flush();
        byte[] array = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.reset();
        return array;
    }

    public final int size() {
        return byteArrayOutputStream.size();
    }
}
