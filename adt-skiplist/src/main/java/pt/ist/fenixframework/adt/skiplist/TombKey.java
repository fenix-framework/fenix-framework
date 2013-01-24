package pt.ist.fenixframework.adt.skiplist;

import java.io.Serializable;

public class TombKey implements Comparable, Serializable {

    private final int constant;

    public TombKey(int constant) {
	this.constant = constant;
    }

    @Override
    public int compareTo(Object arg0) {
	return this.constant;
    }
}