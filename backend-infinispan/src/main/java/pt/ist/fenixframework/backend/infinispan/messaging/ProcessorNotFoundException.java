package pt.ist.fenixframework.backend.infinispan.messaging;

/**
 * @author Pedro Ruivo
 * @since 2.10
 */
public class ProcessorNotFoundException extends Exception {

    public ProcessorNotFoundException(String message) {
        super(message);
    }

}
