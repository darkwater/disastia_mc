package red.dark.disastia_mc;

import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    private static String versionString;

    private static String getVersion() {
        if (versionString == null) {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            versionString = name.substring(name.lastIndexOf('.') + 1) + ".";
        }

        return versionString;
    }

    public static Class<?> getNMClass(String nmClassName) {
        String clazzName = "net.minecraft." + nmClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }

        return clazz;
    }


    public static Class<?> getOBCClass(String obcClassName) {
        String clazzName = "org.bukkit.craftbukkit." + getVersion() + obcClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }

        return clazz;
    }

    public static Class<?> getOBCEntityClass(String obcClassName) {
        String clazzName = "org.bukkit.craftbukkit." + getVersion() + "entity." + obcClassName;
        Class<?> clazz;

        try {
            clazz = Class.forName(clazzName);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }

        return clazz;
    }


    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            return clazz.getMethod(methodName, params);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Field getField(Class<?> clazz, String fieldName) {
        try {
            Field f = clazz.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }
}
