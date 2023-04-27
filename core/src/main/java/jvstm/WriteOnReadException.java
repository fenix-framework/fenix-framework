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
 * An instance of <code>WriteOnReadException</code> is thrown by a
 * thread whenever a write attempt is made to a VBox within a
 * ReadOnlyTransaction.
 *
 * An application should never catch instances of this class, as the
 * purpose of throwing an instance of this class is to make a
 * non-local exit from the currently running transaction, and restart
 * it with a new type of transaction that is able to deal with writes.
 * This is done by the JVSTM runtime and should not be masked by the
 * application code in anyway.
 *
 * The class <code>WriteOnReadException</code> is specifically a
 * subclass of <code>Error</code> rather than <code>Exception</code>,
 * even though it is a "normal occurrence", because many applications
 * catch all occurrences of <code>Exception</code> and then discard
 * the exception.
 *
 */
public class WriteOnReadException extends Error {
    private static final long serialVersionUID = 1L;
}
