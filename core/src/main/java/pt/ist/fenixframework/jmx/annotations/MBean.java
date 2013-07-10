package pt.ist.fenixframework.jmx.annotations;

import pt.ist.fenixframework.jmx.JmxUtil;

import java.lang.annotation.*;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface MBean {
    String objectName() default JmxUtil.DEFAULT_STRING_VALUE;

    String category() default "default";

    String description() default JmxUtil.DEFAULT_STRING_VALUE;
}
