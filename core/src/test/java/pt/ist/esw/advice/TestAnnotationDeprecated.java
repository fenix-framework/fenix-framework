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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * When running ProcessAnnotations use -f
 * pt.ist.esw.advice.impl.DeprecatedAdviceFactory
 */
public class TestAnnotationDeprecated {

    public int i;

    @BeforeEach
    public void resetCounter() {
        i = 0;
    }

    @Test
    public void testAdviceRunsOnce() {
        assertEquals(0, i);
        // run inc() advised, which should run it twice
        inc();
        assertEquals(2, i);
    }

    @Test
    public void testAdviceRunsTwice() {
        assertEquals(0, i);
        // run inc() advised, which should run it twice
        inc();
        // run inc() advised, which should run it twice
        inc();
        assertEquals(4, i);
    }

    @Deprecated
    private void inc() {
        i++;
    }

}
