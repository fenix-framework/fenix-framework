package pt.ist.fenixframework.pstm;


public interface CommitListener {
    public void beforeCommit(TopLevelTransaction tx);
    public void afterCommit(TopLevelTransaction tx);
}
