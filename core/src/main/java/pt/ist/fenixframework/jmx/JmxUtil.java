package pt.ist.fenixframework.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.jmx.annotations.MBean;
import pt.ist.fenixframework.jmx.annotations.ManagedAttribute;
import pt.ist.fenixframework.jmx.annotations.ManagedOperation;
import pt.ist.fenixframework.jmx.ManagedAttributeInfo;
import pt.ist.fenixframework.jmx.ComponentMBean;
import pt.ist.fenixframework.util.Util;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public class JmxUtil {

    public static final String DEFAULT_STRING_VALUE = "";
    public static final String JMX_DOMAIN = "pt.ist.fenixframework";
    private static final Logger logger = LoggerFactory.getLogger(JmxUtil.class);
    private static final String APPLICATION_KEY = "application";
    private static final String COMPONENT_KEY = "component";
    private static final String CATEGORY_KEY = "category";
    private static final MBeanServer mBeanServer;

    static {
        MBeanServer tmpMbeanServer;
        //copied from infinispan...
        try {
            Class<?> mbsLocator = Util.loadClass("org.jboss.mx.util.MBeanServerLocator");
            tmpMbeanServer = (MBeanServer) mbsLocator.getMethod("locateJBoss").invoke(null);
        } catch (Exception e) {
            //ignored
            tmpMbeanServer = ManagementFactory.getPlatformMBeanServer();
        }
        mBeanServer = tmpMbeanServer;
        logger.info("MBean Server to use is " + mBeanServer);
    }

    public static void processInstance(Object instance, String applicationName) {
        ComponentMBean componentMBean = process(instance);

        if (componentMBean == null) {
            return;
        }

        registerMBean(buildObjectName(instance.getClass(), applicationName), componentMBean);
    }

    public static void registerMBean(ObjectName name, Object object) {
        if (name == null || object == null) {
            logger.error("Error registering object " + object + " over the name " + name + ". Null not allowed!");
        }
        if (!mBeanServer.isRegistered(name)) {
            logger.info("Register " + object + " over the name " + name);
            try {
                mBeanServer.registerMBean(object, name);
            } catch (InstanceAlreadyExistsException e) {
                logger.error("Error registering object " + name, e);
            } catch (NotCompliantMBeanException e) {
                logger.error("Error registering object " + name, e);
            } catch (MBeanRegistrationException e) {
                logger.error("Error registering object " + name, e);
            }
        } else {
            logger.error(name + " is already registered!");
        }
    }

    public static void unregisterMBean(ObjectName objectName) throws Exception {
        if (mBeanServer.isRegistered(objectName)) {
            logger.info("Unregistering object " + objectName);
            mBeanServer.unregisterMBean(objectName);
        }
    }

    public static void unregisterAllMBeans(String applicationName) {
        logger.info("Unregistering all registered MBeans over the domain " + JMX_DOMAIN + " and application " +
                applicationName);
        String filter = JMX_DOMAIN + ":" + APPLICATION_KEY + "=" + applicationName + ",*";
        try {
            ObjectName filterObjName = new ObjectName(filter);
            for (ObjectInstance mbean : mBeanServer.queryMBeans(filterObjName, null)) {
                unregisterMBean(mbean.getObjectName());
            }
        } catch (Exception e) {
            logger.error("Error unregistering all registered MBeans over the domain " + JMX_DOMAIN + " and application " +
                    applicationName, e);
        }
    }

    public static ObjectName buildObjectName(Class<?> clazz, String applicationName) {
        if (clazz == null) {
            return null;
        }
        MBean mBean = clazz.getAnnotation(MBean.class);
        if (mBean == null) {
            return null;
        }
        String component = mBean.objectName();
        if (component.equals(DEFAULT_STRING_VALUE)) {
            component = clazz.getSimpleName();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(JMX_DOMAIN).append(":")
                .append(APPLICATION_KEY).append("=").append(ObjectName.quote(applicationName)).append(",")
                .append(CATEGORY_KEY).append("=").append(mBean.category()).append(",")
                .append(COMPONENT_KEY).append("=").append(component);
        try {
            return new ObjectName(builder.toString());
        } catch (MalformedObjectNameException e) {
            logger.error("Error creating ObjectName for class " + clazz + " and application " + applicationName);
        }
        return null;
    }

    private static ComponentMBean process(Object instance) {
        if (instance == null) {
            logger.error("Trying to analyze object but it is null");
            return null;
        }

        Class<?> clazz = instance.getClass();
        MBean mBean = clazz.getAnnotation(MBean.class);
        if (mBean == null) {
            logger.error("Trying to analyze object " + instance + " but it does not have the MBean annotation");
            return null;
        }

        Map<String, ManagedAttributeInfo> attributeInfoMap = new HashMap<String, ManagedAttributeInfo>();
        Set<MBeanOperationInfo> operationInfoMap = new HashSet<MBeanOperationInfo>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(ManagedAttribute.class) != null) {
                processAttribute(method, attributeInfoMap);
            } else if (method.getAnnotation(ManagedOperation.class) != null) {
                processOperation(method, operationInfoMap);
            }
        }

        try {
            ComponentMBean componentMBean =  new ComponentMBean(instance, mBean.description(),
                    attributeInfoMap.values(), operationInfoMap);
            logger.info("Created MBean " + componentMBean.getMBeanInfo());
            return componentMBean;
        } catch (IntrospectionException e) {
            logger.error("Error creating MBean for object " + instance, e);
        }
        return null;
    }

    private static void processOperation(Method method, Set<MBeanOperationInfo> operationInfoMap) {
        logger.info("Processing operation " + method);
        ManagedOperation managedAttribute = method.getAnnotation(ManagedOperation.class);
        String description = managedAttribute.description();
        MBeanOperationInfo info = new MBeanOperationInfo(description, method);
        operationInfoMap.add(info);
    }

    private static void processAttribute(Method method, Map<String, ManagedAttributeInfo> attributeInfoMap) {
        ManagedAttribute managedAttribute = method.getAnnotation(ManagedAttribute.class);
        String attributeName = managedAttribute.name();
        if (DEFAULT_STRING_VALUE.equals(attributeName)) {
            attributeName = extractName(method.getName());
        }
        logger.info("Processing attribute " + attributeName + " with method " + method);
        boolean getter = managedAttribute.method() == MethodType.GETTER;
        String description = managedAttribute.description();
        ManagedAttributeInfo info;
        if (attributeInfoMap.containsKey(attributeName)) {
            info = attributeInfoMap.get(attributeName);
        } else {
            info = new ManagedAttributeInfo(attributeName);
            attributeInfoMap.put(attributeName, info);
        }
        info.onMethod(method, getter);
        info.updateDescription(description);
    }

    private static String extractName(String methodName) {
        String name;
        if (methodName.startsWith("set") || methodName.startsWith("get")) {
            name = methodName.substring(3);
        } else if (methodName.startsWith("is")) {
            name = methodName.substring(2);
        } else {
            name = methodName;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(Character.toLowerCase(name.charAt(0)));
        if (name.length() > 2) sb.append(name.substring(1));
        return sb.toString();
    }

}
