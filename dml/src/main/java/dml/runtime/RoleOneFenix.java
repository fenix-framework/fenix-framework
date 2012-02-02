package dml.runtime;

/**
 * This class is similar to RoleOne except that it deals with VBoxes
 * from Fenix which have special behavior because of persistence.
 * Maybe later, this class will be removed.
 */

public abstract class RoleOneFenix<C1,C2 extends FenixDomainObject> implements Role<C1,C2> {

    private String attrName;

    public RoleOneFenix(String attrName) {
        this.attrName = attrName;
    }

    public void add(C1 o1, C2 o2, Relation<C1,C2> relation) {
        if (o1 != null) {
            FenixVBox<C2> o1Box = getBox(o1);
            C2 old2 = o1Box.get(o1, attrName);
            if (o2 != old2) {
                relation.remove(o1, old2);
                o1Box.put(o1, attrName, o2);
            }
        }
    }

    public void remove(C1 o1, C2 o2) {
        if (o1 != null) {
            getBox(o1).put(o1, attrName, null);
        }
    }

    public abstract FenixVBox<C2> getBox(C1 o1);
}
