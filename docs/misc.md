# Additional Miscellaneous Documentation

This document aggregates some additional reference documentation regarding the
Fénix Framework.  It is a place to store temporary pieces of documentation
regarding some aspect of the framework that should be better documented, but
is yet to be done.

## Serialization of value-types

Value types used as slots in domain entities should be Serializable.  Their
de-serialization process should not require the invocation of
FenixFramework.getDomainObject(String).  However, it is possible that their
internalization method(s) do so.  The reason for this limitation first
appeared when using the Infinispan-based backends when communicating updates
from one node to another.

### Some further explanation of what caused this

Node A and node B in Infinispan are up.  The first Fénix transaction runs on
node A. At commit it writes some keys.  One object slot is a value type that
externalizes simply as a TreeMap that references some DomainObjects.
Infinispan takes care of sending that key/value to node B.  In doing so, the
following occurs in node B:

  * In one thread the static initializer for the FenixFramework class starts.

  * During that process (note that this thread holds the lock while loading
    class FenixFramework), somehow another thread (a JGoups' thread?) reads
    the key/value from sent from node A and tries to deserialize it.

  * The deserialization entails a call to FenixFramework.getDomainObject().
    But, given that this thread find the FenixFramework **not loaded**, it
    will go through the class loader and it will block until the first thread
    releases the lock.
    
  * The first thread will not release the lock, because it is waiting for the
    state transfer update to finish (work done by the second thread that is
    blocked).
    
  * Deadlock.
  
The problem can be avoided by not causing a call to
FenixFramework.getDomainObject during object deserialization, which implies
the restriction mentioned initially.

## Data type of keys used in collectionsKeys for data types used in 

The data type of keys used in collections that back the implementation of
relations between domain entities, must be Serializable **and** Comparable.

