package com.v8.builtin;

import android.util.Log;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;

public class V8Console extends V8Object {
	public V8Console(V8 runtime) {
		super(runtime);
		
		registerJavaMethod(this, "log", "log", new Class<?>[]{Object.class});
	}
	
	public void log(Object obj){
		Log.i("My","[PJS]:" +obj);
	}
}
