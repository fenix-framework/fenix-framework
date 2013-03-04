package org.apache.ojb.broker.core;

import org.apache.ojb.broker.PersistenceBroker;

public class FenixPersistenceBrokerFactory extends PersistenceBrokerFactoryDefaultImpl {

    /**
     * This method overrides OJB's default broker wrapping mechanism. OJB 1.0.0
     * has a bug on the default Broker wrapper, which caused a memory leak, and
     * was not that useful, we simply do not wrap the broker.
     */
    @Override
    protected PersistenceBroker wrapRequestedBrokerInstance(PersistenceBroker broker) {
        return broker;
    }

}
