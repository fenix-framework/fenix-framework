package pt.ist.fenixframework.pstm;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dml.DmlCompiler;

import antlr.ANTLRException;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.pstm.dml.FenixDomainModel;

public class DML {

    public static FenixDomainModel getDomainModel(Class<? extends FenixDomainModel> modelClass, 
                                                  String[] dmlFiles) throws ANTLRException {
        return (FenixDomainModel)DmlCompiler.getDomainModel(modelClass, Arrays.asList(dmlFiles));
    }

    public static FenixDomainModel getDomainModelForURLs(Class<? extends FenixDomainModel> modelClass, 
                                                         List<URL> dmlFileURLs) throws ANTLRException {
        return (FenixDomainModel)DmlCompiler.getDomainModelForURLs(modelClass, dmlFileURLs);
    }
}
