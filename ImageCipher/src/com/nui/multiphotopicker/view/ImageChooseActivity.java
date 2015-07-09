package com.nui.multiphotopicker.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nui.multiphotopicker.R;
import com.nui.multiphotopicker.adapter.ImageGridAdapter;
import com.nui.multiphotopicker.model.ImageEncryptItem;
import com.nui.multiphotopicker.model.ImageItem;
import com.nui.multiphotopicker.tool.AESCipher;
import com.nui.multiphotopicker.tool.BaseActivity;
import com.nui.multiphotopicker.tool.BitmapChange;
import com.nui.multiphotopicker.tool.ImageCacheUtil;
import com.nui.multiphotopicker.tool.SaveToSD;
import com.nui.multiphotopicker.util.IntentConstants;

public class ImageChooseActivity extends BaseActivity {
	private static final String FILENAME = "ImageEncryptItem.txt";
	private List<ImageItem> mDataList = new ArrayList<ImageItem>();
	private String mBucketName;
	private GridView mGridView;
	private TextView mBucketNameTv;
	private TextView cancelTv;
	private ImageGridAdapter mAdapter;
	private Button mEncryptButton;
	private HashMap<String, ImageItem> selectedImgs = new HashMap<String, ImageItem>();
	private ArrayList<ImageEncryptItem> mImageList = new ArrayList<ImageEncryptItem>();
	private ArrayList<ImageEncryptItem> mImageList1 = new ArrayList<ImageEncryptItem>();
	ArrayList<ImageEncryptItem> mImage = new ArrayList<ImageEncryptItem>();
	BitmapChange bt = new BitmapChange();
	private String password = "123456";// 根密钥
	Gson gson = new Gson();
	private Uri[] uri = null;

	// ----------------------
	private Set<BitmapWorkerTask> taskCollection = new HashSet<BitmapWorkerTask>();;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_image_choose);
		mDataList = (List<ImageItem>) getIntent().getSerializableExtra(
				IntentConstants.EXTRA_IMAGE_LIST);
		if (mDataList == null) {
			mDataList = new ArrayList<ImageItem>();
		}
		mBucketName = getIntent().getStringExtra(
				IntentConstants.EXTRA_BUCKET_NAME);

		if (TextUtils.isEmpty(mBucketName)) {
			mBucketName = "空相册";
		}
		initView();
		initListener();

	}

	private void initView() {
		mBucketNameTv = (TextView) findViewById(R.id.title);
		mBucketNameTv.setText(mBucketName);

		mGridView = (GridView) findViewById(R.id.gridview);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mAdapter = new ImageGridAdapter(ImageChooseActivity.this, mDataList);
		mGridView.setAdapter(mAdapter);
		mEncryptButton = (Button) findViewById(R.id.encrypt_btn);
		cancelTv = (TextView) findViewById(R.id.action);
		mEncryptButton.setText("已选择" + "(" + selectedImgs.size() + ")");
		mAdapter.notifyDataSetChanged();
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 1) {
				if (taskCollection.isEmpty()) {
					doSomeThing();
					msg.recycle();
				} else {
					Message message = new Message();
					message.what = 1;
					handler.sendMessageDelayed(message, 10);
				}
			}

		}
	};

	private void initListener() {
		mEncryptButton.setOnClickListener(new OnClickListener() {

			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			public void onClick(View v) {
				ArrayList<ImageItem> imageSelected = new ArrayList<ImageItem>(
						selectedImgs.values());// 获得被选中的图片
				int a = imageSelected.size();
				String[] sourcePath = new String[a];
				uri = new Uri[a];
				Uri mUri = Uri.parse("content://media/external/images/media");
				for (int i = 0; i < a; i++) {
					sourcePath[i] = imageSelected.get(i).sourcePath;
					uri[i] = Uri.withAppendedPath(mUri,
							"" + imageSelected.get(i).imageId);
				}
				// 如果选择0张图片
				if (imageSelected.size() == 0) {
					Toast.makeText(ImageChooseActivity.this, "请选择要加密的图片",
							Toast.LENGTH_SHORT).show();
				} else {
					// --------------------------------------------------------------------------------------------------------------
					for (String b : sourcePath) {
						BitmapWorkerTask task = new BitmapWorkerTask();
						taskCollection.add(task);
						task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
								b);
					}
					if (taskCollection.isEmpty()) {
						doSomeThing();
					} else {
						Message message = new Message();
						message.what = 1;
						handler.sendMessageDelayed(message, 10);
					}
				}
			}
		});
		mGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ImageItem item = mDataList.get(position);
				if (item.isSelected) {
					item.isSelected = false;
					selectedImgs.remove(item.imageId);
				} else {
					item.isSelected = true;
					selectedImgs.put(item.imageId, item);
				}

				mEncryptButton.setText("已选择" + "(" + selectedImgs.size() + ")");
				mAdapter.notifyDataSetChanged();
			}

		});

		cancelTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ImageChooseActivity.this,
						PublishActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * 异步处理图片
	 * 
	 * @author guolin
	 */
	class BitmapWorkerTask extends AsyncTask<String, Void, ImageEncryptItem> {

		/**
		 * 图片的path地址
		 */
		private String imagepath;

		@Override
		protected ImageEncryptItem doInBackground(String... params) {
			imagepath = params[0];
			// 在后台开始下载图片
			ImageEncryptItem mImage = downloadBitmap(imagepath);
			return mImage;
		}

		@Override
		protected void onPostExecute(ImageEncryptItem mImage) {
			super.onPostExecute(mImage);
			mImageList.add(mImage);
			taskCollection.remove(this);
		}

		/**
		 * 
		 * @param imagepath
		 *            图片的SourcePath
		 * @return 解析后的Bitmap对象
		 */
		private ImageEncryptItem downloadBitmap(String imagepath) {
			StringBuffer mStringBuffer = new StringBuffer(String.valueOf(Math
					.random() * 100000 + 97));
			String filename01 = mStringBuffer.toString();// 产生随机字符串
			String filename02 = mStringBuffer.append("wj").toString();
			String mkey = mStringBuffer.deleteCharAt(1).toString();
			try {
				// 防止加载一张大图片就OOM了，对图片大小和质量进行压缩,将其转换为byte[]数组
				byte[] bytes = ImageCacheUtil.getResizedBitmap(imagepath,
						ImageChooseActivity.this);
				String s1 = bt.bitmaptoString(bytes, filename02);
				String strEncrypt = AESCipher.encrypt(mkey, s1);// 加密
				SaveToSD.saveSDCard(filename01, strEncrypt);
			} catch (Exception e) {
				e.printStackTrace();
			}
			ImageEncryptItem mImage = new ImageEncryptItem(filename01,
					filename02, imagepath, mkey, false);
			return mImage;

		}
	}

	public void doSomeThing() {
		for (Uri i : uri) {
			getContentResolver().delete(i, null, null);// 删除相册的照片
		}
		File mFile = new File(Environment.getExternalStorageDirectory() + "/"
				+ FILENAME);
		if (mFile.exists()) {
			String s = new String(SaveToSD.outFromSD(Environment
					.getExternalStorageDirectory() + "/" + FILENAME));
			String ms = "";
			try {
				ms = AESCipher.decrypt(password, s);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			mImageList1 = gson.fromJson(ms,
					new TypeToken<ArrayList<ImageEncryptItem>>() {
					}.getType());
			for (int i = 0; i < mImageList1.size(); i++) {
				mImageList.add(mImageList1.get(i));
			}
		}
		String str = gson.toJson(mImageList);
		try {
			String ms = AESCipher.encrypt(password, str);
			SaveToSD.saveSDCard(FILENAME, ms);
		} catch (Exception e) {
			e.printStackTrace();
		}
		ArrayList<String> mImageSourcePathList = new ArrayList<String>();
		for (ImageEncryptItem c : mImageList) {
			String path = c.getSourcePath();
			mImageSourcePathList.add(path);
		}
		Intent i = new Intent("com.example.broadcasttest.LOCAL_BROADCAST");
		sendBroadcast(i);
	}
}
