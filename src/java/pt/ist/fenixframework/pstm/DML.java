package pt.ist.fenixframework.pstm;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dml.DmlCompiler;

import antlr.ANTLRException;

import pt.ist.fenixframework.pstm.dml.FenixDomainModel;

public class DML {

    public static FenixDomainModel getDomainModel(String[] dmlFiles) throws ANTLRException {
        Class<FenixDomainModel> modelClass = FenixDomainModel.class;
        return (FenixDomainModel)DmlCompiler.getDomainModel(modelClass, Arrays.asList(dmlFiles));
    }

    public static FenixDomainModel getDomainModelForURL(URL dmlFileURL) throws ANTLRException {
        Class<FenixDomainModel> modelClass = FenixDomainModel.class;
        List<URL> urls = Collections.singletonList(dmlFileURL);
        return (FenixDomainModel)DmlCompiler.getDomainModelForURLs(modelClass, urls);
    }
}
