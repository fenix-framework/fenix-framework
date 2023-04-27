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

/**
 * An UnsafeSingleThreadedTransaction needs to run alone in a single thread. This class is unsafe
 * because, it assumes that no other transactions are running, but doesn't check it. Thus,
 * concurrent transactions can run, but *WILL BREAK* the system. UnsafeSingleThreadedTransactions
 * are useful for setup scenarios, where an application is single-threadedly initialized before
 * being concurrently available.
 */
public class UnsafeSingleThreadedTransaction extends InevitableTransaction {

    public UnsafeSingleThreadedTransaction(ActiveTransactionsRecord activeRecord) {
        super(activeRecord);
    }

    public <T> void setBoxValue(VBox<T> vbox, T value) {
        if ((vbox.body != null) && (vbox.body.version == number)) {
            vbox.body.value = value;
        } else {
            vbox.body = VBox.makeNewBody(value, number, null); // we immediatly clean old unused values
        }
    }
}
