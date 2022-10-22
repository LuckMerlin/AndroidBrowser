package com.luckmerlin.binding;

abstract class ObjectBinding implements Binding {
  private Object mObject;

  public final ObjectBinding setObject(Object obj){
      mObject=obj;
      return this;
  }

  public final Object getObject() {
     return mObject;
  }
}
