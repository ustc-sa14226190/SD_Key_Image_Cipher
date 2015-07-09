package com.nui.multiphotopicker.tool;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public class BaseActivity extends Activity {
   @Override
   protected void onCreate(Bundle savedInstanceState){
	   super.onCreate(savedInstanceState);
	  ActivityCollector.addActivity(this);
 }
   @Override
   protected void onDestroy(){
	   super.onDestroy();
	   ActivityCollector.removeActivity(this);
	   Log.i("Activity finish",""+this);
   }
}