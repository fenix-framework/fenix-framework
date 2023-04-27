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
package jvstm.cps;

import jvstm.Transaction;
import jvstm.util.Cons;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConsistencyPredicateSystem {

    // keeps a map of the consistency predicates for each class
    private final static Map<Class, Cons<Method>> PREDICATES_PER_CLASS = new ConcurrentHashMap<Class, Cons<Method>>();

    public static Cons<Method> getPredicatesFor(Object obj) {
        Class objClass = obj.getClass();
        Cons<Method> predicates = PREDICATES_PER_CLASS.get(objClass);
        if (predicates == null) {
            predicates = computePredicatesForClass(objClass);
            PREDICATES_PER_CLASS.put(objClass, predicates);
        }
        return predicates;
    }

    private static Cons<Method> computePredicatesForClass(Class objClass) {
        if (objClass != null) {
            Cons<Method> predicates = computePredicatesForClass(objClass.getSuperclass());
            for (Method m : objClass.getDeclaredMethods()) {
                if (m.isAnnotationPresent(ConsistencyPredicate.class)) {
                    m.setAccessible(true);
                    predicates = predicates.cons(m);
                }
            }
            return predicates;
        } else {
            return Cons.empty();
        }
    }

    public static void initialize() {
        Transaction.setTransactionFactory(new ConsistentTransactionFactory());
    }

    public static void registerNewObject(Object obj) {
        ((ConsistentTransaction) Transaction.current()).registerNewObject(obj);
    }
}
