package pt.ist.fenixframework.consistencyPredicates;

import pt.ist.fenixframework.Config;

public abstract class ConsistencyPredicatesConfig extends Config {

    /**
     * This <strong>optional</strong> parameter indicates whether the framework
     * should automatically create {@link DomainMetaObject}s and {@link DomainMetaClass}es. Only if the value is true will a
     * consistency
     * predicate of a domain object be allowed to read values from other
     * objects. The default value is false.<br>
     * 
     * If this parameter is set to true, then, {@link Config#errorfIfDeletingObjectNotDisconnected} must also be set to
     * true.<br>
     * <br>
     * <strong>Note: Setting this parameter to true causes the fenix-framework
     * to create a {@link DomainMetaObject} for each existing domain object
     * during the initialization. Depending on the amount of already existing
     * objects, this may cause the next application startup to be very
     * slow.</strong><br>
     * <br>
     * If set to true, the fenix-framework no longer supports overriding a
     * consistency predicate with a non-predicate method.<br>
     * <br>
     * <strong>If a programmer changes the implementation of an existing
     * consistency predicate, he/she should always change the method's signature
     * to force the fenix-framework to re-execute the predicate and re-calculate
     * its dependencies.</strong>
     * 
     * @see DomainMetaObject#delete()
     */
    protected boolean canCreateDomainMetaObjects = false;

    protected void canCreateDomainMetaObjectsFromString(String value) {
        canCreateDomainMetaObjects = Boolean.valueOf(value);
    }

    public boolean getCanCreateDomainMetaObjects() {
        return canCreateDomainMetaObjects;
    }

}
