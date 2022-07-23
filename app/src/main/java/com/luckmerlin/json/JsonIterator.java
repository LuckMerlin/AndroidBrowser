package com.luckmerlin.json;

import com.luckmerlin.debug.Debug;
import com.luckmerlin.object.ObjectCreator;
import org.json.JSONObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import dalvik.system.PathClassLoader;

public class JsonIterator extends ObjectCreator {

    public final Object applySafe(Object object,JSONObject json) {
        try {
            return apply(object,null,json);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Object optValue(final String fieldName,JSONObject jsonObject){
        final int length=null!=fieldName?fieldName.length():-1;
        if (length<1||null==jsonObject){
            return null;
        }
        Object object=null;
        if (null!=(object=jsonObject.opt(fieldName))){
            return object;
        }
        final char firstChar=fieldName.charAt(0);
        if (length<=1){
            return jsonObject.opt((Character.isLowerCase(firstChar)?Character.toUpperCase(firstChar)
                    :Character.toLowerCase(firstChar))+"");
        }else if (firstChar=='m'){
            final String optName=fieldName.substring(1);
            final char optFirstChar=optName.charAt(0);
            if (null!=(object=jsonObject.opt(optName))){
                return object;
            }else if (Character.isUpperCase(optFirstChar)&&null!=(object=jsonObject.
                    opt(optName.replaceFirst(""+optFirstChar,""+Character.toLowerCase(optFirstChar))))){
                return object;
            }
        }
        return null;
    }

    private final Object apply(Object object,Class startLayer,JSONObject json) throws Exception {
        if (null==object||object instanceof String ||object instanceof Number|| object instanceof Boolean){
            Debug.E("Fail apply json object while object invalid."+object);
            return null;
        }else if (object instanceof Class){//Need create object self
            Debug.D("Create json object self while apply json."+object);
            Object instance=createObject((Class) object);
            if (null==instance){
                Debug.D("Fail apply json object while Create json fail."+object);
                return null;
            }
            return applySafe(instance,json);
        }
        Class objectClass=object.getClass();
        startLayer=null!=startLayer?startLayer:objectClass;
        if (!objectClass.isAssignableFrom(startLayer)){
            return null;
        }
        //
        ClassLoader classLoader=null;Field field=null;Object fieldValue=null;
        boolean isAccessible=false;boolean succeed=false;
        while (null!=objectClass&&(classLoader=objectClass.getClassLoader())!=null
                &&classLoader instanceof PathClassLoader){
            final Field[] fields=objectClass.getDeclaredFields();
            int length=null!=fields?fields.length:-1;
            for (int i = 0; i < length; i++) {
                if (null != (field=fields[i])&& !Modifier.isFinal(field.getModifiers())) {
                    if (null!=(fieldValue=createFieldValue(field.getGenericType(),
                            optValue(field.getName(),json)))){
                        if (!(isAccessible=field.isAccessible())){
                            field.setAccessible(true);
                        }
                        field.set(object,fieldValue);
                        if (!isAccessible){
                            field.setAccessible(false);
                        }
                        succeed=true;
                    }
                }
            }
            objectClass=objectClass.getSuperclass();
        }
        Debug.D("EEEEE "+objectClass+" "+classLoader);
        return succeed?object:null;
    }

    public Object createFieldValue(Type type, Object text){
        if (type == null||null==text) {
            return null;
        }else if (type instanceof TypeVariable){
            TypeVariable typeVariable=(TypeVariable)type;
//            Method[] methods=typeVariable.getClass().getDeclaredMethods();
//            for (Method d:methods) {
//                Debug.D("BBBBB "+d.getName()+" "+d.getReturnType());
//            }
            Debug.D("EEEEEesdfas无法撒旦法d "+typeVariable.getName()
                   +" ");
        }else if (type instanceof Class){
            return createFieldValue((Class)type,text);
        }
        return null;
    }

    public Object createFieldValue(Class cls,Object text){
        try{
            if (null==cls||null==text){
                return null;
            }else if (cls.equals(Object.class)){
                return text;
            }else if (cls.equals(String.class)||cls.equals(CharSequence.class)){
                return text instanceof String?(String)text:text.toString();
            }else if (cls.equals(int.class)||cls.equals(Integer.class)){
                return text instanceof Integer?(Integer)text:Integer.parseInt(text instanceof String?(String)text:text.toString());
            }else if (cls.equals(float.class)||cls.equals(Float.class)){
                return text instanceof Float?(Float)text:Float.parseFloat(text instanceof String?(String)text:text.toString());
            }else if (cls.equals(double.class)||cls.equals(Double.class)){
                return text instanceof Double?(Double)text:Double.parseDouble(text instanceof String?(String)text:text.toString());
            }else if (cls.equals(long.class)||cls.equals(Long.class)){
                return text instanceof Long?(Long)text:Long.parseLong(text instanceof String?(String)text:text.toString());
            }else if (cls.equals(byte.class)||cls.equals(Byte.class)){
                return text instanceof Byte?(Byte)text:Byte.parseByte(text instanceof String?(String)text:text.toString());
            }else if (cls.equals(boolean.class)||cls.equals(Boolean.class)){
                return text instanceof Boolean?(Boolean)text:Boolean.parseBoolean(text instanceof String?(String)text:text.toString());
            }else if (cls.equals(char.class)||cls.equals(Character.class)){
                String ch=text instanceof String?(String)text:text.toString();
                return text instanceof Character?(Character)text:null!=ch&&ch.length()>0?ch.charAt(0):'0';
            }
            String ch=text instanceof String?(String)text:text.toString();
            return null!=ch&&ch.length()>0?apply(cls,null,new JSONObject(ch)):null;
        }catch (Exception e){

        }
        return null;
    }
}
