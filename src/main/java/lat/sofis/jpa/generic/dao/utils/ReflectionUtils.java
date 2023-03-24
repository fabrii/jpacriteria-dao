package lat.sofis.jpa.generic.dao.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utilities for reflection
 */
public class ReflectionUtils {

    /**
     * It allows to obtain a field according to annotation
     * @param o
     * @param annotationClass
     * @return 
     */
    public static Field obtenerCampoAnotado(Class o, Class annotationClass) {
        Field fieldSeleccionado = null;       
        for (Field field : ReflectionUtils.getAllFields(o)) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(annotationClass)) {
                fieldSeleccionado = field;
                break;
            }
        }
        return fieldSeleccionado;
    }

    /**
     * It allows obtaining the name of a field according to annotation
     * @param o
     * @param annotationClass
     * @return 
     */
    public static String obtenerNombreCampoAnotado(Class o, Class annotationClass) {
        Field f = obtenerCampoAnotado(o, annotationClass);
        if (f != null) {
            return f.getName();
        }
        return null;
    }

    /**
     * Gets all the fields of a class
     * @param type
     * @return 
     */
    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }
        return fields;
    }
    
    /**
     * It allows obtaining a Field of a class according to its name
     * @param type
     * @param name
     * @return 
     */
    public static Field getDeclaredField(Class<?> type, String name){
        List<Field> properties = getAllFields(type);
        for(Field field : properties){
            if(field.getName().equalsIgnoreCase(name)){
                return field;
            }
        }
        return null;
    }

}
