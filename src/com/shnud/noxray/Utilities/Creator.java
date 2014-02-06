package com.shnud.noxray.Utilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;

/**
 * Created by Andrew on 05/02/2014.
 */
public class Creator<T> {

    private final Constructor<T> _constructor;
    private final Object[] _arguments;

    @SuppressWarnings("unchecked")
    public Creator(Object... arguments) {
        _arguments = arguments;

        Class[] classes = new Class[arguments.length];
        for(int i = 0; i < classes.length; i++) {
            classes[i] = arguments[i].getClass();
        }

        try {

            Class type = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            _constructor = type.getConstructor(classes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No such constructor");
        }
    }

    public T create() {
        try {
            return _constructor.newInstance(_arguments);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
