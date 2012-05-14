package pt.ist.fenixframework.services;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This will mark methods as services. It will guarantee that it will be
 * executed within a write transaction, and that the whole write transaction is
 * atomic. Also implies inlined nesting transactions.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD })
public @interface Service {
}
