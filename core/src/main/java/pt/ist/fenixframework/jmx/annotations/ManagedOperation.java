package pt.ist.fenixframework.jmx.annotations;

import pt.ist.fenixframework.jmx.JmxUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ManagedOperation {
    String description() default JmxUtil.DEFAULT_STRING_VALUE;
}
