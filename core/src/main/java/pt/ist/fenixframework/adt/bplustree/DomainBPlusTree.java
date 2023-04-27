package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;

import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.core.AbstractDomainObject;

/**
 * {@link BPlusTree} specialized for storing {@link AbstractDomainObject}.
 * Uses the object's Oid as the Key, and the object as the Value.
 * 
 * The serialization of a {@link DomainBPlusTree} is done using JSON objects,
 * thus allowing for better performance and human-readable representation.
 * 
 * @author João Carvalho (joao.pedro.carvalho@ist.utl.pt)
 * 
 */
@NoDomainMetaObjects
public class DomainBPlusTree<T extends AbstractDomainObject> extends DomainBPlusTree_Base {

    public DomainBPlusTree() {
        super();
    }

    @Override
    protected void initRoot() {
        this.setRoot(new DomainLeafNode());
    }

    /**
     * Inserts the given {@link AbstractDomainObject} into the tree.
     * 
     * @param domainObject
     *            The object to be inserted
     * @return Whether the object was inserted
     */
    public boolean insert(AbstractDomainObject domainObject) {
        return super.insert(domainObject.getOid(), domainObject);
    }

    /**
     * 
     * Inserting {@link Serializable} into a {@link DomainBPlusTree} is not valid.
     * 
     * Throws {@link UnsupportedOperationException} unless a pair [Oid, AbstractDomainObject]
     * is being inserted.
     * Use {@code insert(AbstractDomainObject)} instead.
     */
    @Override
    public boolean insert(Comparable key, Serializable value) {
        if (value instanceof AbstractDomainObject) {
            if (((AbstractDomainObject) value).getOid().equals(key)) {
                return super.insert(key, value);
            }
        }
        throw new UnsupportedOperationException("DomainBPlusTree can only store AbstractDomainObjects indexed using their OID.");
    }

}
