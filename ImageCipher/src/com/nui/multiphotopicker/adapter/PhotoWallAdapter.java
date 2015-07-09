package com.nui.multiphotopicker.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nui.multiphotopicker.R;
import com.nui.multiphotopicker.model.ImageEncryptItem;
import com.nui.multiphotopicker.tool.AESCipher;
import com.nui.multiphotopicker.tool.SaveToSD;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class PhotoWallAdapter extends ArrayAdapter<ImageEncryptItem> implements OnScrollListener{

	/** 
     * ��¼�����������ػ�ȴ����ص����� 
     */  
	 private Set<BitmapWorkerTask> taskCollection;  
	  
	    /** 
	     * ͼƬ���漼���ĺ����࣬���ڻ����������غõ�ͼƬ���ڳ����ڴ�ﵽ�趨ֵʱ�Ὣ�������ʹ�õ�ͼƬ�Ƴ����� 
	     */  
	    private LruCache<ImageEncryptItem, Bitmap> mMemoryCache;  
	  
	    /** 
	     * GridView��ʵ�� 
	     */  
	    private GridView mPhotoWall;  
	  
	    /** 
	     * ��һ�ſɼ�ͼƬ���±� 
	     */  
	    private int mFirstVisibleItem;  
	  
	    /** 
	     * һ���ж�����ͼƬ�ɼ� 
	     */  
	    private int mVisibleItemCount;  
	  
	    /** 
	     * ��¼�Ƿ�մ򿪳������ڽ��������򲻹�����Ļ����������ͼƬ�����⡣ 
	     */  
	    private boolean isFirstEnter = true;  
	    
	    /**
	     * ���ڽ���ArrayList<String> list
	     * */
	   // private ArrayList<String> mList;
	     private ArrayList<ImageEncryptItem> mList;  
	     
	     private Context mContext;
	     
	    public PhotoWallAdapter(Context context, int textViewResourceId, ArrayList<ImageEncryptItem> list,  
	            GridView photoWall) {  
	        super(context, textViewResourceId , list); 
	        mContext = context;
	        mPhotoWall = photoWall;  
	        mList = list;
	        taskCollection = new HashSet<BitmapWorkerTask>();  
	        // ��ȡӦ�ó����������ڴ�  
	        int maxMemory = (int) Runtime.getRuntime().maxMemory();  
	        int cacheSize = maxMemory / 16;  
	        // ����ͼƬ�����СΪ�����������ڴ��1/16  
	        mMemoryCache = new LruCache<ImageEncryptItem, Bitmap>(cacheSize) {  
	            @Override  
	            protected int sizeOf(ImageEncryptItem key, Bitmap bitmap) {  
	                return bitmap.getByteCount();  
	            }  
	        };  
	        mPhotoWall.setOnScrollListener(this);  
	    }  
	    private class ViewHolder{
	    	private ImageView photo;
			private ImageView selectedIv;
			private TextView selectedBgTv;
	    }
	  
	    @Override  
	    public View getView(int position, View convertView, ViewGroup parent) {  
	        final ImageEncryptItem path = getItem(position);
	        final ViewHolder mHolder;
	        View view;  
	        if (convertView == null) {  
	            view = LayoutInflater.from(getContext()).inflate(R.layout.item_publish, null); 
	            mHolder = new ViewHolder();
				mHolder.photo = (ImageView) view.findViewById(R.id.item_grid_image);
				mHolder.selectedIv = (ImageView) view
						.findViewById(R.id.selected_tag);
				mHolder.selectedBgTv = (TextView) view
						.findViewById(R.id.image_selected_bg);
				view.setTag(mHolder);
	        } else {  
	            view = convertView;  
	            mHolder = (ViewHolder)view.getTag();
	        }  
	        // ��ImageView����һ��Tag����֤�첽����ͼƬʱ��������  
	        mHolder.photo.setTag(path);  
	        setImageView(path, mHolder.photo);  
	        
	        if (path.isSelected())
			{
				mHolder.selectedIv.setImageDrawable(mContext.getResources()
						.getDrawable(R.drawable.tag_selected));
				mHolder.selectedIv.setVisibility(View.VISIBLE);
				mHolder.selectedBgTv
						.setBackgroundResource(R.drawable.image_selected);
			}
			else
			{
				mHolder.selectedIv.setImageDrawable(null);
				mHolder.selectedIv.setVisibility(View.GONE);
				mHolder.selectedBgTv.setBackgroundResource(R.color.light_gray);
			}
	        return view;  
	    }  
	  
	    /** 
	     * ��ImageView����ͼƬ�����ȴ�LruCache��ȡ��ͼƬ�Ļ��棬���õ�ImageView�ϡ����LruCache��û�и�ͼƬ�Ļ��棬 
	     * �͸�ImageView����һ��Ĭ��ͼƬ�� 
	     *  
	     * @param imagepath 
	     *            ͼƬ��path��ַ��������ΪLruCache�ļ��� 
	     * @param imageView 
	     *            ������ʾͼƬ�Ŀؼ��� 
	     */  
	    private void setImageView(ImageEncryptItem imagepath, ImageView imageView) { 
	        Bitmap bitmap = getBitmapFromMemoryCache(imagepath);  
	        if (bitmap != null) {  
	            imageView.setImageBitmap(bitmap);  
	        } else {  
	            imageView.setImageResource(R.drawable.bg_img);  
	        }  
	    }  
	  
	    /** 
	     * ��һ��ͼƬ�洢��LruCache�С� 
	     *  
	     * @param key 
	     *            LruCache�ļ������ﴫ��ͼƬ��path��ַ�� 
	     * @param bitmap 
	     *            LruCache�ļ������ﴫ������������ص�Bitmap���� 
	     */  
	    public void addBitmapToMemoryCache(ImageEncryptItem key, Bitmap bitmap) {  
	        if (getBitmapFromMemoryCache(key) == null) {  
	            mMemoryCache.put(key, bitmap);  
	        }  
	    }  
	  
	    /** 
	     * ��LruCache�л�ȡһ��ͼƬ����������ھͷ���null�� 
	     *  
	     * @param key 
	     *            LruCache�ļ������ﴫ��ͼƬ��path��ַ�� 
	     * @return ��Ӧ�������Bitmap���󣬻���null�� 
	     */  
	    public Bitmap getBitmapFromMemoryCache(ImageEncryptItem key) {  
	        return mMemoryCache.get(key);  
	    }  
	  
	    @Override  
	    public void onScrollStateChanged(AbsListView view, int scrollState) {  
	        // ����GridView��ֹʱ��ȥ����ͼƬ��GridView����ʱȡ�������������ص�����  
	        if (scrollState == SCROLL_STATE_IDLE) {  
	            loadBitmaps(mFirstVisibleItem, mVisibleItemCount);  
	        } else {  
	            cancelAllTasks();  
	        }  
	    }  
	  
	    @Override  
	    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,  
	            int totalItemCount) {  
	        mFirstVisibleItem = firstVisibleItem;  
	        mVisibleItemCount = visibleItemCount; 
	        // ���ص�����Ӧ����onScrollStateChanged����ã����״ν������ʱonScrollStateChanged��������ã�  
	        // ���������Ϊ�״ν����������������  
	        if (isFirstEnter && visibleItemCount > 0) {  
	            loadBitmaps(firstVisibleItem, visibleItemCount);  
	            isFirstEnter = false;  
	        }  
	    }  
	  
	    /** 
	     * ����Bitmap���󡣴˷�������LruCache�м��������Ļ�пɼ���ImageView��Bitmap���� 
	     * ��������κ�һ��ImageView��Bitmap�����ڻ����У��ͻῪ���첽�߳�ȥ����ͼƬ�� 
	     *  
	     * @param firstVisibleItem 
	     *            ��һ���ɼ���ImageView���±� 
	     * @param visibleItemCount 
	     *            ��Ļ���ܹ��ɼ���Ԫ���� 
	     */  
	    private void loadBitmaps(int firstVisibleItem, int visibleItemCount) {  
	        try {  
	            for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {  
	            	ImageEncryptItem imagepath = mList.get(i)  ;  
	                Bitmap bitmap = getBitmapFromMemoryCache(imagepath);  
	                if (bitmap == null) {  
	                    BitmapWorkerTask task = new BitmapWorkerTask();  
	                    taskCollection.add(task); 
	                    //Executor exec = new ThreadPoolExecutor(5,128,10,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
	                    //task.execute(imagepath);
	                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, imagepath);
	                } else {  
	                    ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imagepath);  
	                    if (imageView != null && bitmap != null) {  
	                        imageView.setImageBitmap(bitmap);  
	                    }  
	                }  
	            }  
	        } catch (Exception e) {  
	            e.printStackTrace();  
	        }  
	    }  
	  
	    /** 
	     * ȡ�������������ػ�ȴ����ص����� 
	     */  
	    public void cancelAllTasks() {  
	        if (taskCollection != null) {  
	            for (BitmapWorkerTask task : taskCollection) {  
	                task.cancel(true);  
	            }  
	        }  
	    }  
	  
	    /** 
	     * �첽����ͼƬ������ 
	     *  
	     * @author guolin 
	     */  
	    class BitmapWorkerTask extends AsyncTask<ImageEncryptItem, Void, Bitmap> {  
	  
	        /** 
	         * ͼƬ��path��ַ 
	         */  
	        private ImageEncryptItem imagepath;  
	  
	        @Override  
	        protected Bitmap doInBackground(ImageEncryptItem... params) {  
	            imagepath = params[0];  
	            // �ں�̨��ʼ����ͼƬ  
	            Bitmap bitmap = downloadBitmap(params[0]);  
	            if (bitmap != null) {  
	                // ͼƬ������ɺ󻺴浽LrcCache��  
	                addBitmapToMemoryCache(params[0], bitmap);  
	            }  
	            return bitmap;  
	        }  
	        @Override  
	        protected void onPostExecute(Bitmap bitmap) {  
	            super.onPostExecute(bitmap);  
	            // ����Tag�ҵ���Ӧ��ImageView�ؼ��������غõ�ͼƬ��ʾ������  
	            ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imagepath);  
	            if (imageView != null && bitmap != null) {  
	                imageView.setImageBitmap(bitmap);  
	            }  
	            taskCollection.remove(this);  
	        }  
	  
	        /** 
	         *  
	         * @param imagepath 
	         *            ͼƬ��SourcePath
	         * @return �������Bitmap���� 
	         */  
	        private Bitmap downloadBitmap(ImageEncryptItem imagepath) {  
	        	String result = "";
	        	String filename01 = imagepath.getEncryptFilename();
			    String filename02 = imagepath.getUnencryptFilename();
			    String key =imagepath.getKey();
				String str1 = new String(SaveToSD.outFromSD(Environment
						.getExternalStorageDirectory() + "/" + filename01 ));
				String str2 = new String(SaveToSD.outFromSD(Environment.getExternalStorageDirectory()
						+ "/" + filename02));									
				try {
					String result1 = AESCipher.decrypt(key, str1);  //����        
					result1 = result1.trim();
					String result2 = str2.trim();
					 result = result1 + result2;									
				} catch (Exception e) {

					e.printStackTrace();
				}	
	            Bitmap bitmap = null;  																
			   byte[] bitmapArray = Base64.decode(result, Base64.DEFAULT);
			   BitmapFactory.Options options = new BitmapFactory.Options();
			    options.inJustDecodeBounds = true;
			   bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
									bitmapArray.length,options);	
			   float srcWidth = options.outWidth;
			    float srcHeight = options.outHeight;
			    int inSampleSize = 1;
			    if (srcHeight > 100 || srcWidth > 128) {
			        if (srcWidth > srcHeight) {
			            inSampleSize = Math.round((float)srcHeight / (float)100);
			        } else {
			            inSampleSize = Math.round((float)srcWidth / (float)128)  ;
			        }
			    }
			    options = new BitmapFactory.Options();
			    options.inSampleSize = inSampleSize;
			    bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
						bitmapArray.length,options);
			   
	            return bitmap;  
	          
	        }
	    }  
	  
	}  