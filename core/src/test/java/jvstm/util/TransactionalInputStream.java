/*
 * JVSTM: a Java library for Software Transactional Memory
 * Copyright (C) 2005 INESC-ID Software Engineering Group
 * http://www.esw.inesc-id.pt
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Author's contact:
 * INESC-ID Software Engineering Group
 * Rua Alves Redol 9
 * 1000 - 029 Lisboa
 * Portugal
 */
package jvstm.util;

import jvstm.VBox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * The TransactionalInputStream aims to provide a stream that will effectively
 * request input on a decorated regular InputStream only once.
 * This class is to be used inside JVSTM transaction contexts and is the only way of
 * ensuring that input done inside these contexts is effectively executed only once.
 * The reason behind this class comes from the fact that JVSTM transactions that abort
 * only discard the changes done to JVSTM Boxes, so any IO done inside a transaction
 * context occurs regardless of transaction outcome. This usually leads to input being
 * request more than what would be expected.
 * This class provides the transactification of input streams and supports any stream
 * that extends InputStream.
 *
 * NOTE: instances of this class that wrap the same regular InputStream should be shared
 * among the threads that want to read from it.
 *
 * @param buffer ByteArray that will perform the operation in the input
 */
public class TransactionalInputStream extends InputStream {
    private ByteArray buffer;

    public TransactionalInputStream(InputStream i) {
        buffer = new ByteArray(i);
    }

    @Override
    public int read() throws IOException {
        return buffer.readByte();
    }

    /**
     * ByteArray is the class that will speculatively read bytes from the regular
     * InputStream and store them on a byte array. Threads will read from the regular
     * stream byte by byte, store it on the array, increment its speculatively read pointer
     * and try to increment is effective pointer, that it's wrapped in a box.
     * This means that everytime a thread reads a byte, that byte goes to the array, and the number
     * of speculatively read bytes is incremented. But that byte may be consumed or not depending if
     * the transaction context that requested it commits or not. If it aborts, other threads
     * that ask for input should instead read this byte, to avoid asking again the user for input.
     * Everytime the speculative pointer in the array is greater than the effectively read, it means
     * bytes were asked from the input stream but not consumed, so they should be reused.
     * NOTE: no garbagge collection is performed on the bytes stored, so keep that in mind when
     * using this class for long executions of input requesting.
     *
     * @param in InputStream provided to read from the input
     *
     * @param effectivelyRead VBox that holds the index after the last commited/consumed
     *            byte read from the input. Threads will compete to change this VBox in order to
     *            complete the transaction.
     *
     * @param speculativelyRead Index after the last byte written to the
     *            array that represents the real size of the buffer.
     */
    private class ByteArray {
        private InputStream in;
        private VBox<Integer> effectivelyRead = new VBox<Integer>(0);
        private volatile int speculativelyRead = 0;
        private byte[] array = new byte[1];

        public ByteArray(InputStream inputStream) {
            this.in = inputStream;
        }

        public int readByte() throws IOException {
            int byteVal;

            if (effectivelyRead.get() == speculativelyRead) {
                // there are no more bytes at the buffer to reuse
                byteVal = requestByte();
            } else {
                // we have input stored in the buffer to reuse
                byteVal = this.array[this.effectivelyRead.get()];
            }
            this.effectivelyRead.put(this.effectivelyRead.get() + 1);
            return byteVal;
        }

        private synchronized byte requestByte() throws IOException {
            // getting byte from wrapped input stream
            byte byteVal = (byte) in.read();

            // check buffer overflow and reallocate
            if (this.speculativelyRead >= array.length) {
                this.array = Arrays.copyOfRange(this.array, 0, this.array.length * 2);
                this.array[this.speculativelyRead] = byteVal;
            } else {
                this.array[this.speculativelyRead] = byteVal;
            }
            this.speculativelyRead++;
            return byteVal;
        }
    }
}
