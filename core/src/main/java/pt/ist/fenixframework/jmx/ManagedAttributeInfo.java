package pt.ist.fenixframework.jmx;

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import java.lang.reflect.Method;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public class ManagedAttributeInfo {

    private final String name;
    private String description;
    private Method setter;
    private Method getter;

    public ManagedAttributeInfo(String name) {
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public final Method getter() {
        return getter;
    }

    public final Method setter() {
        return setter;
    }

    public final void onMethod(Method method, boolean getter) {
        if (getter && this.getter == null) {
            this.getter = method;
        } else if (!getter && this.setter == null) {
            this.setter = method;
        }
    }

    public final void updateDescription(String description) {
        if (this.description == null || JmxUtil.DEFAULT_STRING_VALUE.equals(this.description)) {
            this.description = description;
        }
    }

    public final MBeanAttributeInfo toMBeanInfo() throws IntrospectionException {
        return new MBeanAttributeInfo(name, description, getter, setter);
    }

}
