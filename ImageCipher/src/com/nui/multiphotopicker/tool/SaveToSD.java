package com.nui.multiphotopicker.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.nui.multiphotopicker.model.ImageEncryptItem;

import android.os.Environment;
import android.util.Log;

public class SaveToSD {
	public static void saveSDCard(String filename, String filecontent)
			throws Exception {
		File file = new File(Environment.getExternalStorageDirectory(),
				filename);
		FileOutputStream outStream = new FileOutputStream(file);
		outStream.write(filecontent.getBytes());
		outStream.flush();
		outStream.close();
	}

	@SuppressWarnings("resource")
	public  static byte[] outFromSD(String filename) {
		byte[] bt = null;
		FileInputStream fis = null;
		int i = 0;
		try {
			fis = new FileInputStream(filename);
			bt = new byte[fis.available()];
			fis.read(bt);
			String result = new String(bt);
			i++;
			Log.i("∂¡»°≥…π¶",i+"" );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bt;

	}
}