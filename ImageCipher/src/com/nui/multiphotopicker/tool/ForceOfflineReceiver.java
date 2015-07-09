package com.nui.multiphotopicker.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.nui.multiphotopicker.view.PublishActivity;

public class ForceOfflineReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		
					ActivityCollector.finishAll();//销毁所有活动
					Intent i = new Intent(context,
							PublishActivity.class	);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);				
					context.startActivity(i);
//					System.exit(0);
	}
}

