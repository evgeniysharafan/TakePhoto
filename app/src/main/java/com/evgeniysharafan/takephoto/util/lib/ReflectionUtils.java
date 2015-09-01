package com.evgeniysharafan.takephoto.util.lib;

import java.lang.reflect.InvocationTargetException;

public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Object tryInvoke(Object target, String methodName, Object... args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }

        return tryInvoke(target, methodName, argTypes, args);
    }

    public static Object tryInvoke(Object target, String methodName, Class<?>[] argTypes, Object... args) {
        //noinspection TryWithIdenticalCatches
        try {
            return target.getClass().getMethod(methodName, argTypes).invoke(target, args);
        } catch (NoSuchMethodException e) {
            L.e(e);
        } catch (IllegalAccessException e) {
            L.e(e);
        } catch (InvocationTargetException e) {
            L.e(e);
        }

        return null;
    }

    public static <E> E callWithDefault(Object target, String methodName, E defaultValue) {
        try {
            //noinspection unchecked
            return (E) target.getClass().getMethod(methodName, (Class[]) null).invoke(target);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            L.e(e);
        }

        return defaultValue;
    }

}
