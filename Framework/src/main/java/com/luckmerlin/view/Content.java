package com.luckmerlin.view;

import android.content.Context;
import android.view.View;

public interface Content {
   View onCreateContentView(Context context,ViewIterator iterator);
}
