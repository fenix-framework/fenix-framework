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

import jvstm.PerTxBox;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * The TransactionalOutputStream aims to provide a stream that will effectively
 * print the requested strings on a decorated regular OutputStream just at commit time.
 * This class is to be used inside JVSTM transaction contexts and is the only way of
 * ensuring that output done inside these contexts is effectively executed only once,
 * at transaction commit and in mutual exclusion from other transactions.
 * The reason behind this class comes from the fact that JVSTM transactions that abort
 * only discard the changes done to JVSTM Boxes, so any IO done inside a transaction
 * context occurs regardless of transaction outcome. This usually leads to more/unexpected
 * output being dumped to the stream that what was intended.
 * This class provides the transactification of output streams and supports any stream
 * that extends OutputStream.
 * NOTE: instances of this class can be used both statically or not.
 *
 * @param out OutputStream to print to
 * @param box PerTxBox<ByteArray> to save the output
 */
public class TransactionalOutputStream extends OutputStream {
    private OutputStream out;
    private PerTxBox<ByteArray> box = new PerTxBox<ByteArray>(null) {
        @Override
        public void commit(ByteArray b) {
            try {
                out.write(b.array, 0, b.arrayPointer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public TransactionalOutputStream(OutputStream o) {
        this.out = o;
    }

    public void write(String text) {
        try {
            write(text.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeln(String text) {
        write(text);
        write("\n");
    }

    @Override
    public void write(int arg) {
        ByteArray a = this.box.get();
        if (a == null) {
            box.put(new ByteArray().writeByte((byte) arg));
        } else {
            box.put(a.writeByte((byte) arg));
        }
    }

    /**
     * The ByteArray class is responsible for storing potential output bytes into
     * an array and keeping track of the pointer to the first free byte. This class
     * is stored in a box as an instance variable of class TransactionalOutputStream,
     * to be able to support parallel output requests.
     *
     * The byte array is created with some original size that doubles each time more
     * space is required. No garbage collection is performed, so care must be taken
     * if instances of TransactionOutputStream are to be used for possible long executions.
     *
     * @param array The array where all the output is stored.
     *            It will grow dynamically depending of how much output is stored
     *
     * @param arrayPointer The index for the first free byte on the array.
     */
    private class ByteArray {
        public int arrayPointer = 0;
        public byte[] array = new byte[64];

        public ByteArray writeByte(byte arg) {

            if (this.arrayPointer >= array.length) {
                this.array = Arrays.copyOfRange(this.array, 0, this.arrayPointer * 2);
                this.array[this.arrayPointer] = arg;
            } else {
                this.array[this.arrayPointer] = arg;
            }
            this.arrayPointer++;
            return this;
        }
    }
}
