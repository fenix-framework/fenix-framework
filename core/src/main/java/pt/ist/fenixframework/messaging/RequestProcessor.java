package pt.ist.fenixframework.messaging;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public interface RequestProcessor {

    Object onRequest(String data);

}
