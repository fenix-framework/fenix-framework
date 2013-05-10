package pt.ist.fenixframework.adt.bplustree;

import java.util.Map.Entry;
import java.util.TreeMap;

import pt.ist.fenixframework.FenixFramework;
import pt.ist.fenixframework.NoDomainMetaObjects;
import pt.ist.fenixframework.backend.BackEnd;
import pt.ist.fenixframework.core.AbstractDomainObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * {@link InnerNode} specialized to hold Oids as Keys.
 * 
 * The serialization of {@link DomainInnerNode} is done using a JSON object,
 * containing the External Id of the key objects, and the external id of
 * the {@link AbstractNode}.
 * 
 * @author João Carvalho (joao.pedro.carvalho@ist.utl.pt)
 * 
 */
@NoDomainMetaObjects
public class DomainInnerNode extends DomainInnerNode_Base {

    /*
     * DomainInnerNode constructors. 
     * 
     * Due to the limitations of constructors in _Base classes, these have
     * to be copied from {@link InnerNode} :(
     */

    DomainInnerNode(AbstractNode leftNode, AbstractNode rightNode, Comparable splitKey) {
        TreeMap<Comparable, AbstractNode> newMap =
                new TreeMap<Comparable, AbstractNode>(BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY);
        newMap.put(splitKey, leftNode);
        newMap.put(BPlusTree.LAST_KEY, rightNode);

        setSubNodes(newMap);
        leftNode.setParent(this);
        rightNode.setParent(this);
    }

    private DomainInnerNode(TreeMap<Comparable, AbstractNode> subNodes) {
        setSubNodes(subNodes);
        for (AbstractNode subNode : subNodes.values()) { // smf: either don't do this or don't setParent when making new
            subNode.setParent(this);
        }
    }

    /*
     * Overriden entries getter and setter.
     * This allows the {@link InnerNode} to use the correct serialized 
     * form without changing the parent's serialization.
     */

    @Override
    public TreeMap<Comparable, AbstractNode> getSubNodes() {
        return getIndexedSubNodes();
    }

    @Override
    public void setSubNodes(TreeMap<Comparable, AbstractNode> subNodes) {
        setIndexedSubNodes(subNodes);
    }

    /*
     * Node instantiators.
     * 
     * This allows for the algorithm to remain in {@link InnerNode} while
     * still creating the correct subclasses.
     */

    @Override
    protected InnerNode createNode(AbstractNode leftNode, AbstractNode rightNode, Comparable splitKey) {
        return new DomainInnerNode(leftNode, rightNode, splitKey);
    }

    @Override
    protected InnerNode createNodeWithSubNodes(TreeMap<Comparable, AbstractNode> subNodes) {
        return new DomainInnerNode(subNodes);
    }

    /*
     * Serialization code
     */

    /**
     * The {@link JsonParser} to be used. Because its instances are
     * stateless we can use only one parser.
     */
    private static final JsonParser parser = new JsonParser();

    /**
     * Serializes the given map to a JSON object containing a mapping between the
     * External Ids of the Key and Value objects.
     * 
     * Uses {@link BPlusTree.LAST_KEY_REPRESENTATION} as a well-known value to
     * represent the Last Key.
     * 
     * @param map
     *            Map to serialize. Must be in the form [Oid, AbstractNode]
     * @return
     *         A JSON Object containing the mapping
     */
    public static String externalizeOidIndexedMap(TreeMap map) {
        BackEnd backend = FenixFramework.getConfig().getBackEnd();
        JsonObject jsonObject = new JsonObject();
        for (Object obj : map.entrySet()) {
            Entry<Comparable, AbstractNode> entry = (Entry<Comparable, AbstractNode>) obj;
            String key;
            if (entry.getKey().equals(BPlusTree.LAST_KEY)) {
                key = BPlusTree.LAST_KEY_REPRESENTATION;
            } else {
                key = backend.fromOid(entry.getKey()).getExternalId();
            }
            jsonObject.add(key, new JsonPrimitive(entry.getValue().getExternalId()));
        }
        return jsonObject.toString();
    }

    /**
     * Internalizes the given JSON object.
     * 
     * @param externalizedMap
     *            A JSON array returned by {@code externalizeOidIndexedMap}
     * @return
     *         A TreeMap containing pairs [Oid, AbstractNode]
     */
    public static TreeMap internalizeOidIndexedMap(String externalizedMap) {
        TreeMap map = new TreeMap(BPlusTree.COMPARATOR_SUPPORTING_LAST_KEY);
        JsonObject object = parser.parse(externalizedMap).getAsJsonObject();
        for (Entry<String, JsonElement> entry : object.entrySet()) {
            Comparable key;
            if (entry.getKey().equals(BPlusTree.LAST_KEY_REPRESENTATION)) {
                key = BPlusTree.LAST_KEY;
            } else {
                key = FenixFramework.<AbstractDomainObject> getDomainObject(entry.getKey()).getOid();
            }
            AbstractNode value = FenixFramework.getDomainObject(entry.getValue().getAsString());
            map.put(key, value);
        }
        return map;
    }

}
