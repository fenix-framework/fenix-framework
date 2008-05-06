package pt.ist.fenixframework.pstm.ojb;

import org.apache.ojb.broker.accesslayer.RowReaderDefaultImpl;
import org.apache.ojb.broker.metadata.ClassDescriptor;


public class FenixRowReader extends RowReaderDefaultImpl {

    public FenixRowReader(ClassDescriptor cld) {
        super(cld);
        throw new Error("The FenixRowReader is no longer needed");
    }
}
