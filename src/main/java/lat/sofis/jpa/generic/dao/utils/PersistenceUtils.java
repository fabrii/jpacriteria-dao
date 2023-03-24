package lat.sofis.jpa.generic.dao.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Persistence utilities
 */
public class PersistenceUtils {

    /**
     * Method that returns the property of an object, navigating through it.
     * @param object
     * @param property
     * @return
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    public static Object getPropertyValue(Object object, String property) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        StringTokenizer st = new StringTokenizer(property, ".");
        if (st.countTokens() == 1) {
            property = property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
            Method prpopertyMehtid = object.getClass().getMethod("get" + property);
            return prpopertyMehtid.invoke(object);
        } else {
            Object navigateObj = object;
            String lastToken = "";
            int count = st.countTokens();
            while (st.hasMoreTokens()) {
                String proNav = st.nextToken();
                proNav = proNav.substring(0, 1).toUpperCase() + proNav.substring(1, proNav.length());
                Method propNavM = navigateObj.getClass().getMethod("get" + proNav);
                count = count - 1;
                lastToken = proNav;
                if (count == 0) {
                    //retorna la propiedad navegando
                    Method method = navigateObj.getClass().getMethod("get" + lastToken);
                    Object toR = method.invoke(navigateObj);
                    return toR;
                }
                navigateObj = propNavM.invoke(navigateObj);
                if (navigateObj == null) {
                    throw new IllegalAccessException("Destino inaccesible " + property);
                }

            }

        }
        return null;

    }

    /**
     * Method that allows to set a property in an object, navigating through it
     * TODO: might be implemented with Hibernate Transformer
     * @param object
     * @param property
     * @param value
     * @param value_class
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    public static void setProperty(Object object, String property, Object value, Class value_class) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, IllegalArgumentException, InvocationTargetException {
        StringTokenizer st = new StringTokenizer(property, ".");
        if (st.countTokens() == 1) {
            property = property.substring(0, 1).toUpperCase() + property.substring(1, property.length());
            Method prpopertyMehtid = object.getClass().getMethod("set" + property, value_class);
            prpopertyMehtid.invoke(object, value);
        } else {
            //tenemos que navegar por get antes de llegar al metodo que queremos
            Object navigateObj = object;
            String lastToken = "";
            int count = st.countTokens();
            while (st.hasMoreTokens()) {
                String proNav = st.nextToken();
                proNav = proNav.substring(0, 1).toUpperCase() + proNav.substring(1, proNav.length());
                Method propNavM = navigateObj.getClass().getMethod("get" + proNav);
                count = count - 1;
                lastToken = proNav;
                if (count == 0) {
                    //retorna la propiedad navegando
                    Method method = navigateObj.getClass().getMethod("set" + lastToken, value_class);
                    method.invoke(navigateObj, value);
                    return;
                }
                Object navigateObj1 = propNavM.invoke(navigateObj);
                if (navigateObj1 == null) {
                    try {
                        navigateObj1 = propNavM.getReturnType().getDeclaredConstructor().newInstance();
                        Method method = navigateObj.getClass().getMethod("set" + lastToken, propNavM.getReturnType());
                        method.invoke(navigateObj, navigateObj1);
                        navigateObj = navigateObj1;
                    } catch (Exception w) {
                    }
                } else {
                    navigateObj = navigateObj1;
                }

            }
        }
    }


    /**
     * It allows to identify if a field of an entity is annotated with optional
     * @param field
     * @return true if optional, false otherwise
     */
    public static Boolean fieldIsOptional(Field field) {
        if (field.isAnnotationPresent(OneToOne.class)) {
            OneToOne annotation = field.getAnnotation(OneToOne.class);
            return annotation.optional();
        } else if (field.isAnnotationPresent(ManyToOne.class)) {
            ManyToOne annotation = field.getAnnotation(ManyToOne.class);
            return annotation.optional();
        }
        return Boolean.FALSE;
    }

}
