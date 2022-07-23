package com.luckmerlin.object;

import com.luckmerlin.debug.Debug;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class ObjectCreator {

    public final <T> T createObject(Class<T> cls){
        if (null==cls){
            return null;
        }
        int modify=cls.getModifiers();
        if (Modifier.isAbstract(modify)||Modifier.isInterface(modify)){
            return null;
        }
        Constructor<T>[] constructors=(Constructor<T>[])cls.getDeclaredConstructors();
        if (null==constructors){
            return null;
        }
        T instance=null;
        for (Constructor<T> child:constructors) {
            if (null!=child&&null!=(instance=createObjectSafe(child))){
                return instance;
            }
        }
        return null;
    }

    public final <T> T createObjectSafe(Constructor<T> constructor){
        try {
            return createObject(constructor);
        } catch (Exception e) {
            Debug.E("Exception create object.e="+e);
            e.printStackTrace();
            return null;
        }
    }

    public final <T> T createObject(Constructor<T> constructor) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        if (null==constructor){
            return null;
        }
        Class[] classes=constructor.getParameterTypes();
        int count=null!=classes?classes.length:-1;
        if (count<=0){
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
        Class childClass=null;
        Object[] args=new Object[count];
        for (int i = 0; i < count; i++) {
            if (null==(childClass=classes[i])){
                continue;
            }
            args[i]=getTypeDefault(childClass);
        }
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    public final Object getTypeDefault(Class cls){
        if (null==cls){
            return null;
        }else if (int.class.equals(cls)||Integer.class.equals(cls)){
            return 0d;
        }else if (long.class.equals(cls)||Long.class.equals(cls)){
            return 0L;
        }else if (double.class.equals(cls)||Double.class.equals(cls)){
            return 0.0d;
        }else if (float.class.equals(cls)||Float.class.equals(cls)){
            return 0.0f;
        }else if (byte.class.equals(cls)||Byte.class.equals(cls)){
            return (new byte[1])[0];
        }else if (boolean.class.equals(cls)||Boolean.class.equals(cls)){
            return false;
        }else if (char.class.equals(cls)||CharSequence.class.equals(cls)){
            return (new char[1])[0];
        }else if (String.class.equals(cls)){
            return "";
        }
        return null;
    }
}
