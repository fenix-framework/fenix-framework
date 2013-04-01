package test.backend.jvstm.domain;

public class Counter extends Counter_Base {

    public Counter() {
        super();
        setValue(0);
    }

    public void inc() {
        setValue(getValue() + 1);
    }
}
