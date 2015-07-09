package com.nui.multiphotopicker.tool;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.Display;

/**
 * @author frankiewei
 * 工具类
 * */
public class ImageCacheUtil {
    /**
     * 获取合适的Bitmap平时获取Bitmap就用这个方法吧。
     * 博客地址 http://blog.csdn.net/android_tutor/article/details/8099918
     * @param path 路径
     * @param context 
     * @return
     * */
	@SuppressWarnings("deprecation")
	public static byte[] getResizedBitmap(String path,Activity a){
		Display display = a.getWindowManager().getDefaultDisplay();
	    float destWidth = display.getWidth();
	    float destHeight = display.getHeight();

	    // read in the dimensions of the image on disk
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

	    float srcWidth = options.outWidth;
	    float srcHeight = options.outHeight;
	    

	    int inSampleSize = 1;
	    if (srcHeight > destHeight || srcWidth > destWidth) {
	        if (srcWidth > srcHeight) {
	            inSampleSize = Math.round((float)srcHeight / (float)destHeight) ;
	        } else {
	            inSampleSize = Math.round((float)srcWidth / (float)destWidth) ;
	        }
	    }
        Log.i("inSampleSize", ""+inSampleSize);
	    options = new BitmapFactory.Options();
	    options.inSampleSize = inSampleSize;

	    Bitmap bitmap = BitmapFactory.decodeFile(path, options);
	    ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, 100, bStream); 
		int mOptions = 100;
		//循环判断如果压缩后的图片是否大于275kb，大于则继续压缩
		while(bStream.toByteArray().length/1024 > 235){
			bStream.reset();
			mOptions -= 10;
			bitmap.compress(Bitmap.CompressFormat.JPEG, mOptions, bStream);
		}
		byte[] bytes = bStream.toByteArray();
	    return bytes;
	}
}

