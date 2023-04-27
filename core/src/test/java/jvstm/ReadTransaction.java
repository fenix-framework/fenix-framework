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
package jvstm;

public class ReadTransaction extends Transaction {
    static final WriteOnReadException WRITE_ON_READ_EXCEPTION = new WriteOnReadException();

    public ReadTransaction(int number) {
        super(number);
    }

    public ReadTransaction(Transaction parent) {
        super(parent);
    }

    public Transaction makeNestedTransaction(boolean readOnly) {
        if (!readOnly) {
            throw WRITE_ON_READ_EXCEPTION;
        }
        return new ReadTransaction(this);
    }

    public <T> T getBoxValue(VBox<T> vbox) {
        return vbox.body.getBody(number).value;
    }

    public <T> void setBoxValue(VBox<T> vbox, T value) {
        throw WRITE_ON_READ_EXCEPTION;
    }

    public <T> T getPerTxValue(PerTxBox<T> box, T initial) {
        return initial;
    }

    public <T> void setPerTxValue(PerTxBox<T> box, T value) {
        throw WRITE_ON_READ_EXCEPTION;
    }

    protected void doCommit() {
        // do nothing
    }
}
