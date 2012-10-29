package pt.ist.fenixframework.pstm;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import pt.ist.fenixframework.pstm.dml.FenixDomainModel;
import antlr.ANTLRException;
import dml.DmlCompiler;

public class DML {

    /**
     * Use {@link #getDomainModelForURLs(Class, List)}
     */
    @Deprecated
    public static FenixDomainModel getDomainModel(Class<? extends FenixDomainModel> modelClass, String[] dmlFiles)
	    throws ANTLRException {
	return (FenixDomainModel) DmlCompiler.getDomainModel(modelClass, Arrays.asList(dmlFiles));
    }

    public static FenixDomainModel getDomainModelForURLs(Class<? extends FenixDomainModel> modelClass, List<URL> dmlFileURLs)
	    throws ANTLRException {
	return (FenixDomainModel) DmlCompiler.getDomainModelForURLs(modelClass, dmlFileURLs, false);
    }

    public static FenixDomainModel getDomainModelForURLs(Class<? extends FenixDomainModel> modelClass, List<URL> dmlFileURLs,
	    boolean checkForMissingExternals) throws ANTLRException {
	return (FenixDomainModel) DmlCompiler.getDomainModelForURLs(modelClass, dmlFileURLs, checkForMissingExternals);
    }
}
