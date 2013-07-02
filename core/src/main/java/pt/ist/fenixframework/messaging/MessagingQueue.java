package pt.ist.fenixframework.messaging;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public interface MessagingQueue {

    void init() throws Exception;

    Object sendRequest(String data, String localityHint, boolean sync) throws Exception;

    void shutdown();
}
