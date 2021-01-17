/*
 * Fenix Framework, a framework to develop Java Enterprise Applications.
 *
 * Copyright (C) 2013 Fenix Framework Team and/or its affiliates and other contributors as indicated by the @author tags.
 *
 * This file is part of the Fenix Framework.  Read the file COPYRIGHT.TXT for more copyright and licensing information.
 */
package pt.ist.fenixframework.backend.jvstm;

import pt.ist.fenixframework.dml.CompilerArgs;
import pt.ist.fenixframework.dml.DomainModel;

public class InMemCodeGenerator extends JVSTMCodeGenerator {

    public InMemCodeGenerator(CompilerArgs compArgs, DomainModel domainModel) {
        super(compArgs, domainModel);
    }

    @Override
    protected String getDomainClassRoot() {
        return InMemDomainObject.class.getName();
    }

}
