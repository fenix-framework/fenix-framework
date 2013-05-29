package pt.ist.fenixframework.backend.jvstm.pstm;

public interface StatisticsCapableTransaction extends JvstmInFenixTransaction {

    public int getNumBoxReads();

    public int getNumBoxWrites();

}
