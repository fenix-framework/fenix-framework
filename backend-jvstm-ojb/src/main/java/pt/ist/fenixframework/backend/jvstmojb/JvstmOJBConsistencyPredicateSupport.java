package pt.ist.fenixframework.backend.jvstmojb;

import jvstm.cps.ConsistencyCheckTransaction;
import pt.ist.fenixframework.DomainMetaObject;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.backend.jvstmojb.pstm.AbstractDomainObject;
import pt.ist.fenixframework.backend.jvstmojb.pstm.FenixConsistencyCheckTransaction;
import pt.ist.fenixframework.backend.jvstmojb.pstm.TransactionSupport;
import pt.ist.fenixframework.consistencyPredicates.ConsistencyPredicateSupport;

public class JvstmOJBConsistencyPredicateSupport extends ConsistencyPredicateSupport {

    @Override
    public DomainMetaObject getDomainMetaObjectFor(DomainObject obj) {
        AbstractDomainObject domainObject = (AbstractDomainObject) obj;
        return domainObject.getDomainMetaObject();
    }

    @Override
    public void justSetMetaObjectForDomainObject(DomainObject domainObject, DomainMetaObject metaObject) {
        AbstractDomainObject abstractDO = (AbstractDomainObject) domainObject;
        abstractDO.justSetMetaObject(metaObject);
    }

    @Override
    public ConsistencyCheckTransaction<?> createNewConsistencyCheckTransactionForObject(DomainObject obj) {
        return new FenixConsistencyCheckTransaction(TransactionSupport.currentFenixTransaction(), obj);
    }

}
