package pt.ist.fenixframework.adt.bplustree;

import java.io.Serializable;
import java.util.TreeMap;

import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.core.AbstractDomainObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * {@link LeafNode} specialized to hold a mapping of [Oid, DomainObject]
 * 
 * The serialization of {@link DomainLeafNode} is done using a JSON array,
 * containing the External Id of the stored objects.
 * 
 * @author João Carvalho (joao.pedro.carvalho@ist.utl.pt)
 * 
 */
@NoDomainMetaObjects
public class DomainLeafNode extends DomainLeafNode_Base {

    /**
     * Initialize a {@link DomainLeafNode} with no entries.
     * 
     * @see LeafNode
     */
    public DomainLeafNode() {
        super();
    }

    /**
     * Initialize a {@link DomainLeafNode} with the given entries.
     * 
     * @see LeafNode
     */
    private DomainLeafNode(TreeMap<Comparable, Serializable> entries) {
        setEntries(entries);
    }

    /*
     * Overriden entries getter and setter.
     * This allows the {@link LeafNode} to use the correct serialized 
     * form without changing the parent's serialization.
     */

    @Override
    public TreeMap<Comparable, ? extends Serializable> getEntries() {
        return getDomainEntries();
    }

    @Override
    public void setEntries(TreeMap<Comparable, ? extends Serializable> entries) {
        setDomainEntries((TreeMap<Comparable, AbstractDomainObject>) entries);
    }

    /*
     * Node instantiators.
     * 
     * This allows for the algorithm to remain in {@link LeafNode} while
     * still creating the correct subclasses.
     */

    @Override
    protected LeafNode createNodeWithEntries(TreeMap<Comparable, Serializable> entries) {
        return new DomainLeafNode(entries);
    }

    @Override
    protected InnerNode createInnerNode(AbstractNode leftNode, AbstractNode rightNode, Comparable splitKey) {
        return new DomainInnerNode(leftNode, rightNode, splitKey);
    }

    /*
     * Serialization code
     */

    /**
     * The {@link JsonParser} to be used. Because its instances are
     * stateless we can reuse the parser.
     */
    private static final JsonParser parser = new JsonParser();

    /**
     * Serializes the given map to a JSON array containing the ExternalId of
     * the values.
     * 
     * @param map
     *            Map to serialize. Must be in the form [Oid, DomainObject]
     * @return
     *         A JSON array containing the External Ids
     */
    public static String externalizeDomainObjectMap(TreeMap map) {
        JsonArray array = new JsonArray();
        for (Object obj : map.values()) {
            DomainObject domainObject = (DomainObject) obj;
            array.add(new JsonPrimitive(domainObject.getExternalId()));
        }
        return array.toString();
    }

    /**
     * Internalizes the given JSON array.
     * 
     * @param externalizedMap
     *            A JSON array returned by {@code externalizeDomainObjectMap}
     * @return
     *         A TreeMap containing pairs [Oid, DomainObject]
     */
    public static TreeMap internalizeDomainObjectMap(String externalizedMap) {
        TreeMap map = new TreeMap();
        JsonArray array = parser.parse(externalizedMap).getAsJsonArray();
        for (JsonElement element : array) {
            AbstractDomainObject ado = FenixFramework.getDomainObject(element.getAsString());
            map.put(ado.getOid(), ado);
        }
        return map;
    }
}
