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
package pt.ist.esw.advice;

import java.lang.annotation.Annotation;

public abstract class AdviceFactory<T extends Annotation> {
    /**
     * Default AdviceFactory used, when none is specified in the annotation.
     * Clients must either provide this class or define the property
     * <code>Class<? extends AdviceFactory> adviceFactory() </code> in their
     * annotation.
     **/
    public static final String DEFAULT_ADVICE_FACTORY = "pt.ist.esw.advice.impl.ClientAdviceFactory";

    /**
     * AdviceFactories must override this method. Note that replacing
     * AdviceFactory<?> with a covariant return type may not always work, as not
     * all java compilers (some versions of javac, for instance) emit the needed
     * bridge methods for it to work with the advice library.
     **/
    public static AdviceFactory<?> getInstance() {
        throw new UnsupportedOperationException(
                "Clients must provide an AdviceFactory with a 'AdviceFactory getInstance()' method.");
    }

    /** AdviceFactories must override this method **/
    public abstract Advice newAdvice(T annotation);

}
