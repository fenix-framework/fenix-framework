package pt.ist.fenixframework.messaging;

/**
 * Interface used by Locality Aware Request Dispatcher that will handle the requests made by the application.
 *
 * @author Pedro Ruivo
 * @since 2.8-cloudtm
 */
public interface RequestProcessor {

    /**
     * Process the request sent by a client.
     *
     * @return the reply to send back to the requestor.
     */
    Object onRequest(String data);

}
