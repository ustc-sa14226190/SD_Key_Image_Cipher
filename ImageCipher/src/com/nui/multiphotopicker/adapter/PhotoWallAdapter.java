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
     * 记录所有正在下载或等待下载的任务。 
     */  
	 private Set<BitmapWorkerTask> taskCollection;  
	  
	    /** 
	     * 图片缓存技术的核心类，用于缓存所有下载好的图片，在程序内存达到设定值时会将最少最近使用的图片移除掉。 
	     */  
	    private LruCache<ImageEncryptItem, Bitmap> mMemoryCache;  
	  
	    /** 
	     * GridView的实例 
	     */  
	    private GridView mPhotoWall;  
	  
	    /** 
	     * 第一张可见图片的下标 
	     */  
	    private int mFirstVisibleItem;  
	  
	    /** 
	     * 一屏有多少张图片可见 
	     */  
	    private int mVisibleItemCount;  
	  
	    /** 
	     * 记录是否刚打开程序，用于解决进入程序不滚动屏幕，不会下载图片的问题。 
	     */  
	    private boolean isFirstEnter = true;  
	    
	    /**
	     * 用于接受ArrayList<String> list
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
	        // 获取应用程序最大可用内存  
	        int maxMemory = (int) Runtime.getRuntime().maxMemory();  
	        int cacheSize = maxMemory / 16;  
	        // 设置图片缓存大小为程序最大可用内存的1/16  
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
	        // 给ImageView设置一个Tag，保证异步加载图片时不会乱序  
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
	     * 给ImageView设置图片。首先从LruCache中取出图片的缓存，设置到ImageView上。如果LruCache中没有该图片的缓存， 
	     * 就给ImageView设置一张默认图片。 
	     *  
	     * @param imagepath 
	     *            图片的path地址，用于作为LruCache的键。 
	     * @param imageView 
	     *            用于显示图片的控件。 
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
	     * 将一张图片存储到LruCache中。 
	     *  
	     * @param key 
	     *            LruCache的键，这里传入图片的path地址。 
	     * @param bitmap 
	     *            LruCache的键，这里传入从网络上下载的Bitmap对象。 
	     */  
	    public void addBitmapToMemoryCache(ImageEncryptItem key, Bitmap bitmap) {  
	        if (getBitmapFromMemoryCache(key) == null) {  
	            mMemoryCache.put(key, bitmap);  
	        }  
	    }  
	  
	    /** 
	     * 从LruCache中获取一张图片，如果不存在就返回null。 
	     *  
	     * @param key 
	     *            LruCache的键，这里传入图片的path地址。 
	     * @return 对应传入键的Bitmap对象，或者null。 
	     */  
	    public Bitmap getBitmapFromMemoryCache(ImageEncryptItem key) {  
	        return mMemoryCache.get(key);  
	    }  
	  
	    @Override  
	    public void onScrollStateChanged(AbsListView view, int scrollState) {  
	        // 仅当GridView静止时才去下载图片，GridView滑动时取消所有正在下载的任务  
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
	        // 下载的任务应该由onScrollStateChanged里调用，但首次进入程序时onScrollStateChanged并不会调用，  
	        // 因此在这里为首次进入程序开启下载任务。  
	        if (isFirstEnter && visibleItemCount > 0) {  
	            loadBitmaps(firstVisibleItem, visibleItemCount);  
	            isFirstEnter = false;  
	        }  
	    }  
	  
	    /** 
	     * 加载Bitmap对象。此方法会在LruCache中检查所有屏幕中可见的ImageView的Bitmap对象， 
	     * 如果发现任何一个ImageView的Bitmap对象不在缓存中，就会开启异步线程去下载图片。 
	     *  
	     * @param firstVisibleItem 
	     *            第一个可见的ImageView的下标 
	     * @param visibleItemCount 
	     *            屏幕中总共可见的元素数 
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
	     * 取消所有正在下载或等待下载的任务。 
	     */  
	    public void cancelAllTasks() {  
	        if (taskCollection != null) {  
	            for (BitmapWorkerTask task : taskCollection) {  
	                task.cancel(true);  
	            }  
	        }  
	    }  
	  
	    /** 
	     * 异步下载图片的任务。 
	     *  
	     * @author guolin 
	     */  
	    class BitmapWorkerTask extends AsyncTask<ImageEncryptItem, Void, Bitmap> {  
	  
	        /** 
	         * 图片的path地址 
	         */  
	        private ImageEncryptItem imagepath;  
	  
	        @Override  
	        protected Bitmap doInBackground(ImageEncryptItem... params) {  
	            imagepath = params[0];  
	            // 在后台开始下载图片  
	            Bitmap bitmap = downloadBitmap(params[0]);  
	            if (bitmap != null) {  
	                // 图片下载完成后缓存到LrcCache中  
	                addBitmapToMemoryCache(params[0], bitmap);  
	            }  
	            return bitmap;  
	        }  
	        @Override  
	        protected void onPostExecute(Bitmap bitmap) {  
	            super.onPostExecute(bitmap);  
	            // 根据Tag找到相应的ImageView控件，将下载好的图片显示出来。  
	            ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imagepath);  
	            if (imageView != null && bitmap != null) {  
	                imageView.setImageBitmap(bitmap);  
	            }  
	            taskCollection.remove(this);  
	        }  
	  
	        /** 
	         *  
	         * @param imagepath 
	         *            图片的SourcePath
	         * @return 解析后的Bitmap对象 
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
					String result1 = AESCipher.decrypt(key, str1);  //解密        
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