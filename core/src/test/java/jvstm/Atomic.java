package jvstm;

import jvstm.atomic.AtomicAdviceFactory;
import pt.ist.esw.advice.AdviceFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface Atomic {
    boolean readOnly() default false;

    boolean canFail() default true;

    boolean speculativeReadOnly() default true;

    Class<? extends AdviceFactory> adviceFactory() default AtomicAdviceFactory.class;
}
