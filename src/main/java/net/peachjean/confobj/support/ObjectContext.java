package net.peachjean.confobj.support;

import net.peachjean.confobj.introspection.GenericType;
import org.apache.commons.beanutils.PropertyUtils;

import javax.inject.Qualifier;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ObjectContext implements InstantiationContext {
    private final Object resolutionContext;
    private final Map<Binding<?>, Method> bindingMethodMap = new HashMap<Binding<?>, Method>();
    private final Map<Binding<?>, List<Method>> duplicatedBindingMethods = new HashMap<Binding<?>, List<Method>>();

    public <C> ObjectContext(C resolutionContext) {

        this.resolutionContext = resolutionContext;
        for(PropertyDescriptor propertyDescriptor: PropertyUtils.getPropertyDescriptors(this.resolutionContext)) {
            if(propertyDescriptor.getReadMethod() != null && !"getClass".equals(propertyDescriptor.getReadMethod().getName())) {
                Binding<?> binding = new Binding<Object>(propertyDescriptor);
                if(bindingMethodMap.containsKey(binding)) {
                    if(!duplicatedBindingMethods.containsKey(binding)) {
                        duplicatedBindingMethods.put(binding, new ArrayList<Method>());
                    }
                    List<Method> duplicatedMethods = duplicatedBindingMethods.get(binding);
                    duplicatedMethods.add(propertyDescriptor.getReadMethod());
                    if(bindingMethodMap.get(binding) != null) {
                        duplicatedMethods.add(bindingMethodMap.put(binding, null));
                    }
                } else {
                    bindingMethodMap.put(binding, propertyDescriptor.getReadMethod());
                }
                bindingMethodMap.put(binding, propertyDescriptor.getReadMethod());
            }
        }
    }

    @Override
    public <T> T lookup(GenericType<T> type) {
        return this.lookup(type, null);
    }

    @Override
    public <T> T lookup(GenericType<T> type, Annotation qualifier) {
        Binding<T> targetBinding = new Binding<T>(type, qualifier);
        if(bindingMethodMap.containsKey(targetBinding)) {
            Method method = bindingMethodMap.get(targetBinding);
            if(method == null) {
                throw new IllegalArgumentException("Cannot lookup binding " + targetBinding + " on " + resolutionContext + " due to multiple potential resolutions:: " + duplicatedBindingMethods.get(bindingMethodMap));
            }
            try {
                return (T) method.invoke(this.resolutionContext);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not invoke getter for binding " + targetBinding, e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Could not invoke getter for binding " + targetBinding, e);
            }
        } else {
            throw new IllegalArgumentException("No available binding " + targetBinding + " on " + resolutionContext);
        }
    }

    private class Binding<T> {
        private final GenericType<T> type;
        private final Annotation qualifier;

        private Binding(GenericType<T> type, Annotation qualifier) {
            this.type = type;
            this.qualifier = qualifier;
        }

        private Binding(PropertyDescriptor propertyDescriptor) {
            Method readMethod = propertyDescriptor.getReadMethod();
            this.type = GenericType.forType(readMethod.getGenericReturnType());
            Annotation qualifier = null;
            for(Annotation a: readMethod.getAnnotations()) {
                if(a.annotationType().isAnnotationPresent(Qualifier.class)) {
                    if(qualifier != null) {
                        throw new RuntimeException("There is more than one qualifier. " + qualifier + " and " + a);
                    }
                    qualifier = a;
                }
            }
            this.qualifier = qualifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Binding binding = (Binding) o;

            if (qualifier != null ? !qualifier.equals(binding.qualifier) : binding.qualifier != null) return false;
            if (!type.equals(binding.type)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Binding{" +
                    "type=" + type +
                    ", qualifier=" + qualifier +
                    '}';
        }
    }
}
