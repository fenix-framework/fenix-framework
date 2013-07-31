package pt.ist.fenixframework.messaging;

import java.util.Collection;
import java.util.Map;

/**
 * Locality Aware Request Dispatcher interface. This interface is {@link pt.ist.fenixframework.backend.BackEnd}
 * dependent and each backend should provide an implementation.
 *
 * @author Pedro Ruivo
 * @since 2.8-cloudtm
 */
public interface MessagingQueue {

    /**
     * Initializes this queue.
     *
     * @throws Exception
     */
    void init() throws Exception;

    /**
     * Sends a request to the node that contains the {@param localityHint}.
     *
     * @param data         The request to be sent.
     * @param localityHint The locality hint to find the node who has it. If it is {@code null}, then the underline
     *                     implementation can dispatch the request to any node.
     * @param sync         If {@code true} the invocation will block until the reply is received. Otherwise, the reply
     *                     is ignored and the invocation returns immediately.
     * @param write        {@code true} if the request can write, {@code false} otherwise.
     * @return The reply if the request is Synchronous, {@code null} otherwise.
     * @throws Exception If some exception has occurred during the request dispatcher.
     */
    Object sendRequest(String data, String localityHint, boolean sync, boolean write) throws Exception;

    /**
     * Shutdowns this queue.
     */
    void shutdown();

    /**
     * @param localityHintsList the locality hint list
     * @return a map with the owner and the locality hints;
     */
    Map<String, String> printLocationInfo(Collection<String> localityHintsList);
}
