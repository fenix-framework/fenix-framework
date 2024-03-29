/*
 * Advice Library
 * Copyright (C) 2012-2013 INESC-ID Software Engineering Group
 * http://www.esw.inesc-id.pt
 *
 * This file is part of the advice library.
 *
 * advice library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * advice library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with advice library. If not, see <http://www.gnu.org/licenses/>.
 *
 * Author's contact:
 * INESC-ID Software Engineering Group
 * Rua Alves Redol 9
 * 1000 - 029 Lisboa
 * Portugal
 */
package pt.ist.esw.advice.impl;

import pt.ist.esw.advice.Advice;
import pt.ist.esw.advice.AdviceFactory;

import java.util.concurrent.Callable;

public final class ClientAdviceFactory extends AdviceFactory<MyAnnotationWithDefaults> {

    public static class MyAdvice implements Advice {

        @Override
        public <V> V perform(Callable<V> method) throws Exception {
            method.call();
            return method.call();

        }
    }

    private ClientAdviceFactory() {
    }

    private final static ClientAdviceFactory instance = new ClientAdviceFactory();

    public static AdviceFactory<MyAnnotationWithDefaults> getInstance() {
        return instance;
    }

    @Override
    public Advice newAdvice(MyAnnotationWithDefaults annotation) {
        return new MyAdvice();
    }

}
