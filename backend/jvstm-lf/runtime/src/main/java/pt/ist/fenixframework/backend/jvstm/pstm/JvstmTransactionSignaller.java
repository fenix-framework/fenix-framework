package pt.ist.fenixframework.backend.jvstm.pstm;

import jvstm.TransactionSignaller;

// this class exists to enable access to the static SIGNALLER object in jvstm.TransactionSignaller.  It may go away if/when such object becomes public in TransactionSignaller.
public abstract class JvstmTransactionSignaller extends TransactionSignaller {

    static final TransactionSignaller FF_SIGNALLER = TransactionSignaller.SIGNALLER;

}
