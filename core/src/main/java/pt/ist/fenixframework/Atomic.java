package pt.ist.fenixframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Atomic {
    boolean readOnly() default false;

    boolean canFail() default true;

    boolean speculativeReadOnly() default true;
}
