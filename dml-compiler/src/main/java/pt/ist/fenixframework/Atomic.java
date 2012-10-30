package pt.ist.fenixframework;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
public @interface Atomic {
    boolean readOnly() default false;
    boolean canFail()  default true;
    boolean speculativeReadOnly() default true;
    Class<? extends pt.ist.fenixframework.atomic.ContextFactory> contextFactory() default pt.ist.fenixframework.atomic.DefaultContextFactoryViaReflection.class;
}
