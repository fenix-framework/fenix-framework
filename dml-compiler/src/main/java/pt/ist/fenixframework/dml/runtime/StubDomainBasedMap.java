/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.dml.runtime;

import java.io.Serializable;
import java.util.Iterator;

/**
 * This class is a stub used by the DML compiler's default code generator. Whenever a module that contains a DML file is compiled,
 * we need to generate the _Base classes, even though later they are not packaged in the JAR file. This class is used whenever a
 * {@link DomainBasedMap} is required for the _Base code to compile.
 */
public class StubDomainBasedMap<T extends Serializable> implements DomainBasedMap<T> {

    private static final long serialVersionUID = -4431304752408833476L;

    private static final UnsupportedOperationException STUB_EXCEPTION = new UnsupportedOperationException(
            "This is a stub and should not be used.  A real DomainBasedMap should be provided by the concrete BackEnd.");

    @Override
    public String getExternalId() {
        throw STUB_EXCEPTION;
    }

    @Override
    public T get(Comparable key) {
        throw STUB_EXCEPTION;
    }

    @Override
    public boolean putIfMissing(Comparable key, T value) {
        throw STUB_EXCEPTION;
    }

    @Override
    public void put(Comparable key, T value) {
        throw STUB_EXCEPTION;
    }

    @Override
    public boolean remove(Comparable key) {
        throw STUB_EXCEPTION;
    }

    @Override
    public boolean contains(Comparable key) {
        throw STUB_EXCEPTION;
    }

    @Override
    public int size() {
        throw STUB_EXCEPTION;
    }

    @Override
    public Iterator<T> iterator() {
        throw STUB_EXCEPTION;
    }

}
