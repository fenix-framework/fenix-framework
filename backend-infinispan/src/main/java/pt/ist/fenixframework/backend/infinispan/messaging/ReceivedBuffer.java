package pt.ist.fenixframework.backend.infinispan.messaging;

import java.io.*;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public class ReceivedBuffer implements ObjectInput {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private final ObjectInputStream objectInputStream;

    public ReceivedBuffer(byte[] buffer) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        this.objectInputStream = new ObjectInputStream(byteArrayInputStream);
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        return objectInputStream.readObject();
    }

    @Override
    public int read() throws IOException {
        return objectInputStream.read();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return objectInputStream.read(buf, off, len);
    }

    @Override
    public int available() throws IOException {
        return objectInputStream.available();
    }

    @Override
    public void close() throws IOException {
        objectInputStream.close();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return objectInputStream.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return objectInputStream.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return objectInputStream.readUnsignedByte();
    }

    @Override
    public char readChar() throws IOException {
        return objectInputStream.readChar();
    }

    @Override
    public short readShort() throws IOException {
        return objectInputStream.readShort();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return objectInputStream.readUnsignedShort();
    }

    @Override
    public int readInt() throws IOException {
        return objectInputStream.readInt();
    }

    @Override
    public long readLong() throws IOException {
        return objectInputStream.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return objectInputStream.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return objectInputStream.readDouble();
    }

    @Override
    public void readFully(byte[] buf) throws IOException {
        objectInputStream.readFully(buf);
    }

    @Override
    public void readFully(byte[] buf, int off, int len) throws IOException {
        objectInputStream.readFully(buf, off, len);
    }

    @Override
    public int skipBytes(int len) throws IOException {
        return objectInputStream.skipBytes(len);
    }

    @Override
    @Deprecated
    public String readLine() throws IOException {
        return objectInputStream.readLine();
    }

    @Override
    public String readUTF() throws IOException {
        return objectInputStream.readUTF();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return objectInputStream.read(b);
    }

    @Override
    public long skip(long n) throws IOException {
        return objectInputStream.skip(n);
    }

    public byte[] readByteArray() throws IOException {
        int size = objectInputStream.readInt();
        if (size == 0) {
            return EMPTY_BYTE_ARRAY;
        }
        byte[] array = new byte[size];
        objectInputStream.readFully(array);
        return array;
    }

}
