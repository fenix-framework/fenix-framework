package pt.ist.fenixframework.backend.jvstmojb.repository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import pt.ist.fenixframework.DomainModelUtil;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.backend.jvstmojb.ojb.OJBMetadataGenerator;
import pt.ist.fenixframework.dml.DomainClass;

public class DomainModelMetadata {

    private static final ConcurrentMap<Class<? extends DomainObject>, DomainModelMetadata> metadataCache =
            new ConcurrentHashMap<>();

    public static DomainModelMetadata getMetadataForType(Class<? extends DomainObject> type) {
        DomainModelMetadata metadata = metadataCache.get(type);
        if (metadata == null) {
            metadata = new DomainModelMetadata(type);
            metadataCache.putIfAbsent(type, metadata);
        }
        return metadata;
    }

    private final String tableName;
    private final String deleteQuery;

    public DomainModelMetadata(Class<? extends DomainObject> type) {
        DomainClass domClass = DomainModelUtil.getDomainClassFor(type);
        this.tableName = OJBMetadataGenerator.getExpectedTableName(domClass);

        this.deleteQuery = "DELETE FROM `" + tableName + "` WHERE OID = ";
    }

    public String getTableName() {
        return tableName;
    }

    public String getDeleteQuery(long oid) {
        return deleteQuery + oid;
    }

}
