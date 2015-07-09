package com.nui.multiphotopicker.view;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nui.multiphotopicker.R;
import com.nui.multiphotopicker.adapter.PhotoWallAdapter;
import com.nui.multiphotopicker.model.ImageEncryptItem;
import com.nui.multiphotopicker.tool.AESCipher;
import com.nui.multiphotopicker.tool.BaseActivity;
import com.nui.multiphotopicker.tool.SaveToSD;

public class PublishActivity extends BaseActivity implements OnClickListener
{
	/**
	 * 用于展示照片墙的GridView
	 * */
	private GridView mPhotoWall;
	
	/**
	 * GridView的适配器
	 * */
	private PhotoWallAdapter adapter;
	 
	 /**
	  * 用于向PhotoWallAdapter提供的string
	  * */
	private static final String FILENAME = "ImageEncryptItem.txt";
   
    private String password = "123456";//根密钥
	 Gson gson = new Gson();
	 private ArrayList<ImageEncryptItem> mList;
	 private ArrayList<ImageEncryptItem> mSelectedList = new ArrayList<ImageEncryptItem>();
	 
	 private Button mSelectedButton;
	 private Button mDecriptButton;
	 
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_publish);
		mList = getStringList();
		mPhotoWall = (GridView)findViewById(R.id.gridview);
		if (mList != null){			
		adapter = new PhotoWallAdapter(this, 0, mList, mPhotoWall);
		mPhotoWall.setAdapter(adapter);
		initOnItemClickListener();
		}
		mSelectedButton = (Button)findViewById(R.id.button1);
		mSelectedButton.setOnClickListener(this);
		mDecriptButton = (Button)findViewById(R.id.button2);
		mDecriptButton.setOnClickListener(this);
		
	}
	
	private void initOnItemClickListener() {
		mPhotoWall.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				ImageEncryptItem item = mList.get(position);
				if (item.isSelected())
				{
					item.setSelected(false);
					mSelectedList.remove(item);
				}
				else
				{
					item.setSelected(true);
					mSelectedList.add(item);
				}
				adapter.notifyDataSetChanged();
			}
			
		});
		
	}

	private ArrayList<ImageEncryptItem> getStringList() {
		File mFile = new File(Environment
				.getExternalStorageDirectory() + "/" + FILENAME );
		    if(mFile.exists()){
			    String s = new String(SaveToSD.outFromSD(Environment
					.getExternalStorageDirectory() + "/" + FILENAME ));	
			    String ms = "";
			    try {
				    ms = AESCipher.decrypt(password,s);
			    } catch (Exception e1) {
				     e1.printStackTrace();
			    }  
			 ArrayList<ImageEncryptItem> mImage = gson.fromJson(ms, 
					new TypeToken<ArrayList<ImageEncryptItem>>(){}.getType());
			 Log.i("mImage的大小为",""+mImage.size());
		return mImage;
		    }else{
		    	return null;
		    }
	}

    
	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        // 退出程序时结束所有的下载任务  
        adapter.cancelAllTasks();  
	}

	@Override
	public void onClick(View v) {
	   switch(v.getId()){
	   case R.id.button1:
		   Intent i = new Intent(PublishActivity.this,ImageBucketChooseActivity.class);
			startActivity(i);
			break;
	   case R.id.button2:
		   if(mList.removeAll(mSelectedList)){
			   for(ImageEncryptItem item: mSelectedList){
				   String filename01 = item.getEncryptFilename();
				   String filename02 = item.getUnencryptFilename();
				   String key = item.getKey();
				   String imageSource = item.getSourcePath();
				   String str1 = new String(SaveToSD.outFromSD(Environment
							.getExternalStorageDirectory() + "/" + filename01 ));
					String str2 = new String(SaveToSD.outFromSD(Environment.getExternalStorageDirectory()
							+ "/" + filename02));	
					
					try {
						String result1 = AESCipher.decrypt(key, str1);  //解密        
						result1 = result1.trim();
						String result2 = str2.trim();
						String result = result1 + result2;
						byte[] byteArray = Base64.decode(result, Base64.DEFAULT);
						FileOutputStream out=new FileOutputStream(new File(imageSource));       
						out.write(byteArray);       
						out.flush();       
						out.close();
											
					} catch (Exception e) {

						e.printStackTrace();
					}			
				   File delFile1=new File(Environment.getExternalStorageDirectory() 
				    		+ "/" + filename01);
				    if(delFile1.exists())  
				    {   
				      delFile1.delete();  
				    }
				    File delFile2=new File(Environment.getExternalStorageDirectory()
							+ "/" + filename02); 
				    if(delFile2.exists())  
				    {  
				      delFile2.delete();  
				    }
			   }
			   String mString = gson.toJson(mList);
			   try {
					String ms = AESCipher.encrypt(password, mString);
					SaveToSD.saveSDCard(FILENAME, ms);
					} catch (Exception e) {
						e.printStackTrace();
					}
			   Intent mIntent = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");	
			    sendBroadcast(mIntent);
		   }else{
			   Toast.makeText(PublishActivity.this,"请选择要解密的图片", Toast.LENGTH_SHORT).show();
		   }
		   break;
		default:
			break;
	   
	   }
		
	}  
  
     
}