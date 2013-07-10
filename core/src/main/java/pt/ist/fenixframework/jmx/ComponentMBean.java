package pt.ist.fenixframework.jmx;

import pt.ist.fenixframework.util.Util;

import javax.management.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author Pedro Ruivo
 * @since 2.8
 */
public class ComponentMBean implements DynamicMBean {

    private final HashMap<String, AttributeGetterAndSetter> getterAndSetterHashMap;
    private final Object instance;
    private final MBeanInfo mBeanInfo;

    public ComponentMBean(Object instance, String description, Collection<ManagedAttributeInfo> attributeInfos, Collection<MBeanOperationInfo> operationInfos) throws IntrospectionException {
        this.instance = instance;
        this.getterAndSetterHashMap = new HashMap<String, AttributeGetterAndSetter>();
        String className = instance.getClass().getCanonicalName();
        MBeanAttributeInfo[] attributeInfoArray = new MBeanAttributeInfo[attributeInfos.size()];
        int i = 0;
        for (ManagedAttributeInfo info : attributeInfos) {
            attributeInfoArray[i++] = info.toMBeanInfo();
            getterAndSetterHashMap.put(info.getName(), new AttributeGetterAndSetter(info.getter(), info.setter()));
        }
        MBeanOperationInfo[] operationInfoArray = new MBeanOperationInfo[operationInfos.size()];
        operationInfos.toArray(operationInfoArray);
        this.mBeanInfo = new MBeanInfo(className, description, attributeInfoArray, null, operationInfoArray, null);
    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        try {
            return getAttributeInfo(attribute).getValue(instance);
        } catch (InvocationTargetException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        try {
            getAttributeInfo(attribute.getName()).setValue(instance, attribute.getValue());
        } catch (InvocationTargetException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        } catch (Exception e) {
            throw new MBeanException(e);
        }
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList list = new AttributeList();
        for (String attribute : attributes) {
            try {
                list.add(new Attribute(attribute, getAttribute(attribute)));
            } catch (AttributeNotFoundException e) {
                //ignored
            } catch (MBeanException e) {
                //ignored
            } catch (ReflectionException e) {
                //ignored
            }
        }
        return list;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList list = new AttributeList();
        for (Attribute attribute : attributes.asList()) {
            try {
                setAttribute(attribute);
                list.add(attribute);
            } catch (AttributeNotFoundException e) {
                //ignored
            } catch (ReflectionException e) {
                //ignored
            } catch (MBeanException e) {
                //ignored
            } catch (InvalidAttributeValueException e) {
                //ignored
            }
        }
        return list;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {

        try {
            Class<?>[] classes = new Class[signature.length];
            for (int i = 0; i < classes.length; i++) {
                classes[i] = Util.loadClass(signature[i]);
            }

            Method method = instance.getClass().getMethod(actionName, classes);
            return method.invoke(instance, params);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException(e);
        } catch (InvocationTargetException e) {
            throw new ReflectionException(e);
        } catch (IllegalAccessException e) {
            throw new ReflectionException(e);
        } catch (Exception e) {
            throw new MBeanException(e);
        }

    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mBeanInfo;
    }

    private AttributeGetterAndSetter getAttributeInfo(String attribute) throws AttributeNotFoundException {
        AttributeGetterAndSetter getterAndSetter = getterAndSetterHashMap.get(attribute);
        if (getterAndSetter == null) {
            throw new AttributeNotFoundException("Attribute not found: " + attribute);
        }
        return getterAndSetter;
    }

    private class AttributeGetterAndSetter {
        private final Method getter;
        private final Method setter;

        private AttributeGetterAndSetter(Method getter, Method setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public final void setValue(Object instance, Object value) throws Exception {
            if (setter == null) {
                throw new Exception("Attribute is not writable");
            }
            setter.invoke(instance, value);
        }

        public final Object getValue(Object instance) throws Exception {
            if (getter == null) {
                throw new Exception("Attribute is not readable");
            }
            return getter.invoke(instance);
        }
    }
}
