package com.luckmerlin.browser.binding;

abstract class ObjectBinding implements Binding {
  private final Object mObject;

  public ObjectBinding(Object object){
    mObject=object;
  }

  public final Object getObject() {
     return mObject;
  }
}
