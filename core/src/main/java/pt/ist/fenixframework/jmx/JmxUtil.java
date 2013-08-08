package pt.ist.fenixframework.jmx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ist.fenixframework.jmx.annotations.MBean;
import pt.ist.fenixframework.jmx.annotations.ManagedAttribute;
import pt.ist.fenixframework.jmx.annotations.ManagedOperation;
import pt.ist.fenixframework.util.Util;

import javax.management.*;
import java.lang.annotation.Annotation;
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
    private static final String MODULE_KEY = "module";
    private static final String COMPONENT_KEY = "component";
    private static final String CATEGORY_KEY = "category";
    private static final MBeanServer mBeanServer;
    public static final String COMMON_MODULE = "common";

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
        if (logger.isInfoEnabled()) {
            logger.info("MBean Server to use is " + mBeanServer);
        }
    }

    public static void processInstance(Object instance, String applicationName, String module, Map<String, String> otherKeys) {
        if (instance == null) {
            if (logger.isErrorEnabled()) {
                logger.error("Trying to analyze object but it is null");
            }
            return;
        }

        Class<?> clazz = instance.getClass();
        MBean mBean = findAnnotation(clazz, MBean.class);
        if (mBean == null) {
            if (logger.isErrorEnabled()) {
                logger.error("Trying to analyze object " + instance + " but it does not have the MBean annotation");
            }
            return;
        }

        ComponentMBean componentMBean = process(instance, clazz, mBean);

        if (componentMBean == null) {
            return;
        }

        String component = mBean.objectName();
        if (component.equals(DEFAULT_STRING_VALUE)) {
            component = clazz.getSimpleName();
        }

        registerMBean(buildObjectName(applicationName, mBean.category(), module, component, otherKeys), componentMBean);
    }

    public static void registerMBean(ObjectName name, Object object) {
        if (name == null || object == null) {
            if (logger.isErrorEnabled()) {
                logger.error("Error registering object " + object + " over the name " + name + ". Null not allowed!");
            }
        }
        if (!mBeanServer.isRegistered(name)) {
            logger.info("Register " + object + " over the name " + name);
            try {
                mBeanServer.registerMBean(object, name);
            } catch (Exception e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error registering object " + name, e);
                }
            }
        } else {
            if (logger.isErrorEnabled()) {
                logger.error(name + " is already registered!");
            }
        }
    }

    public static void unregisterMBean(ObjectName objectName) throws Exception {
        if (mBeanServer.isRegistered(objectName)) {
            if (logger.isInfoEnabled()) {
                logger.info("Unregistering object " + objectName);
            }
            mBeanServer.unregisterMBean(objectName);
        }
    }

    public static void unregisterAllMBeans(String applicationName) {
        if (logger.isInfoEnabled()) {
            logger.info("Unregistering all registered MBeans over the domain " + JMX_DOMAIN + " and application " +
                    applicationName);
        }
        String filter = JMX_DOMAIN + ":" + APPLICATION_KEY + "=" + ObjectName.quote(applicationName) + ",*";
        try {
            ObjectName filterObjName = new ObjectName(filter);
            for (ObjectInstance mbean : mBeanServer.queryMBeans(filterObjName, null)) {
                unregisterMBean(mbean.getObjectName());
            }
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error unregistering all registered MBeans over the domain " + JMX_DOMAIN + " and application " +
                        applicationName, e);
            }
        }
    }

    public static ObjectName buildObjectName(String application, String category, String module, String component,
                                             Map<String, String> otherKeys) {
        StringBuilder builder = new StringBuilder();
        builder.append(JMX_DOMAIN).append(":")
                .append(APPLICATION_KEY).append("=").append(ObjectName.quote(application)).append(",")
                .append(MODULE_KEY).append("=").append(module).append(",")
                .append(CATEGORY_KEY).append("=").append(category).append(",");

        if (otherKeys != null && !otherKeys.isEmpty()) {
            List<String> keys = new ArrayList<String>(otherKeys.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                builder.append(key).append("=").append(otherKeys.get(key)).append(",");
            }
        }

        builder.append(COMPONENT_KEY).append("=").append(component);
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Trying to create ObjectName " + builder);
            }
            return new ObjectName(builder.toString());
        } catch (MalformedObjectNameException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error creating ObjectName.", e);
            }
        }
        return null;
    }

    private static <T extends Annotation> T findAnnotation(Class clazz, Class<T> annotationClass) {
        if (clazz == null) {
            return null;
        }
        T annotation = (T) clazz.getAnnotation(annotationClass);
        if (annotation != null) {
            return annotation;
        }
        if (clazz == Object.class) {
            return null;
        }
        annotation = findAnnotation(clazz.getSuperclass(), annotationClass);
        if (annotation == null) {
            for (Class interfaceClass : clazz.getInterfaces()) {
                annotation = findAnnotation(interfaceClass, annotationClass);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        return annotation;
    }

    private static ComponentMBean process(Object instance, Class<?> clazz, MBean mBean) {
        Map<String, ManagedAttributeInfo> attributeInfoMap = new HashMap<String, ManagedAttributeInfo>();
        Set<MBeanOperationInfo> operationInfoMap = new HashSet<MBeanOperationInfo>();

        internalProcess(clazz, attributeInfoMap, operationInfoMap);

        try {
            ComponentMBean componentMBean = new ComponentMBean(instance, mBean.description(),
                    attributeInfoMap.values(), operationInfoMap);
            if (logger.isInfoEnabled()) {
                logger.info("Created MBean " + componentMBean.getMBeanInfo());
            }
            return componentMBean;
        } catch (IntrospectionException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error creating MBean for object " + instance, e);
            }
        }
        return null;
    }

    private static void internalProcess(Class clazz, Map<String, ManagedAttributeInfo> attributeInfoMap,
                                        Set<MBeanOperationInfo> operationInfoMap) {
        if (clazz == null) {
            return;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(ManagedAttribute.class) != null) {
                processAttribute(method, attributeInfoMap);
            } else if (method.getAnnotation(ManagedOperation.class) != null) {
                processOperation(method, operationInfoMap);
            }
        }
        if (clazz == Object.class) {
            return;
        }
        internalProcess(clazz.getSuperclass(), attributeInfoMap, operationInfoMap);
        for (Class interfaceClass : clazz.getInterfaces()) {
            internalProcess(interfaceClass, attributeInfoMap, operationInfoMap);
        }
    }

    private static void processOperation(Method method, Set<MBeanOperationInfo> operationInfoMap) {
        if (logger.isInfoEnabled()) {
            logger.info("Processing operation " + method);
        }
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
        if (logger.isInfoEnabled()) {
            logger.info("Processing attribute " + attributeName + " with method " + method);
        }
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
