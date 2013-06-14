package pt.ist.fenixframework;

import java.io.Serializable;

import pt.ist.fenixframework.adt.bplustree.DomainBPlusTree;
import pt.ist.fenixframework.core.AbstractDomainObject;

/**
 * A {@link DomainBPlusTree} with a JVSTM-specific optimization to obtain the {@code size()}.
 * 
 * The size is kept in a slot of the {@link DomainBPlusTreeData}, so it's access is O(1).
 * 
 * @author João Neves - JoaoRoxoNeves@ist.utl.pt
 */
@NoDomainMetaObjects
public class DomainBPlusTreeJVSTM<T extends AbstractDomainObject> extends DomainBPlusTreeJVSTM_Base {

    public DomainBPlusTreeJVSTM() {
        super();
        super.setTreeData(new DomainBPlusTreeData());
    }

    @Override
    public int size() {
        return getTreeData().getSize();
    }

    @Override
    public void setTreeData(DomainBPlusTreeData treeData) {
        throw new UnsupportedOperationException("The DomainBPlusTreeData cannot be changed.");
    }

    @Override
    public boolean insert(AbstractDomainObject domainObject) {
        boolean result = super.insert(domainObject);
        if (result) {
            getTreeData().incSize();
        }
        return result;
    }

    @Override
    public boolean insert(Comparable key, Serializable value) {
        boolean result = super.insert(key, value);
        if (result) {
            getTreeData().incSize();
        }
        return result;
    }

    @Override
    public boolean removeKey(Comparable key) {
        boolean result = super.removeKey(key);
        if (result) {
            getTreeData().decSize();
        }
        return result;
    }

    @Override
    public Serializable removeIndex(int index) {
        Serializable result = super.removeIndex(index);
        if (result != null) {
            getTreeData().decSize();
        }
        return result;
    }

    @Override
    public void delete() {
        getTreeData().delete();
        super.delete();
    }
}
