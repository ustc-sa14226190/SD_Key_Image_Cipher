package com.nui.multiphotopicker.tool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class BitmapChange {
	SaveToSD save = new SaveToSD();

	// ------------------base64-------------------//
	public String bitmaptoString(byte[] bytes , String filename) throws Exception {

		// 将Bitmap转换成字符串

		String string1 = "";
		byte[] bytes1 = new byte[300];
		int len = bytes.length-300;
		byte[] bytes2 = new byte[len];
		System.arraycopy(bytes, 0, bytes1, 0, 300);
		System.arraycopy(bytes, 300, bytes2, 0, len);
		string1 = Base64.encodeToString(bytes1, Base64.DEFAULT);
		String string2 = Base64.encodeToString(bytes2, Base64.DEFAULT);
		SaveToSD.saveSDCard(filename, string2);//少了300个字节的字节数组转换成字符串
		return string1;

	}

	// --------------base64-----------------//
	public Bitmap stringtoBitmap(String string ,String path) {

		// 将字符串转换成Bitmap类型

		Bitmap bitmap = null;

		try {

			byte[] bitmapArray;

			bitmapArray = Base64.decode(string, Base64.DEFAULT);
			FileOutputStream out=new FileOutputStream(new File(path));       
			out.write(bitmapArray);       
			out.flush();       
			out.close();

			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,

			bitmapArray.length);

		} catch (Exception e) {

			e.printStackTrace();

		}

		return bitmap;

	}
	
	
}
