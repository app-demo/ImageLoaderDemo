package com.cn.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * // 一级缓存：强引用缓存（内存） 内存溢出，都不会回收   20张
 * // 二级缓存：弱引用缓存（内存） 内存不足的时候回收      超过20张
 * // 三级缓存：本地缓存（硬盘）写入内部存储Sdcard
 * <p/>
 * Author: river
 * Date: 2015/12/28 20:17
 * Description:
 */
public class ImageLoader {
    /**
     * 一级缓存最大数量
     */
    public static final int MAX_CAPACITY = 1;
    /**
     * 一级缓存
     * <p/>
     * key:图片地址
     * value：图片
     * <p/>
     * MAX_CAPACITY：存储最大数量
     * 0.75f经验值
     * accessOrder:true访问排序；false插入排序
     * <p/>
     * LinkedHashMap：内部包含LRU近期最少使用算法
     */
    private final LinkedHashMap<String, Bitmap> firstCacheMap = new LinkedHashMap<String, Bitmap>(MAX_CAPACITY, 0.75f, true) {

        //根据返回值，移除map中最老的值
        @Override
        protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
            //超过MAX_CAPACITY这个数量，则存储到其他地方
            if (this.size() > MAX_CAPACITY) {
                //加入二级缓存
                secondCacheMap.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));

                //加入本地缓存
                addDiskCache(eldest.getKey(), eldest.getValue());

                return true; //移除超出的缓存
            }

            return super.removeEldestEntry(eldest); //false
        }
    };

    /**
     * 二级缓存
     * 强引用：内存爆了，也不会回收
     * SoftReference : 软引用，只有内存不足的时候才会回收
     * WeakReference : 弱引用,GC扫描到就回收
     * <p/>
     * ConcurrentHashMap:线程安全的,为了并发的安全
     */
    private ConcurrentHashMap<String, SoftReference<Bitmap>> secondCacheMap = new ConcurrentHashMap<>();

    private Context context;

    private static ImageLoader mImageLoader;

    private ImageLoader(Context context) {
        this.context = context;
    }


    public static ImageLoader getInstance(Context context) {
        if (mImageLoader == null) {
            synchronized (ImageLoader.class) {
                if (mImageLoader == null) {
                    mImageLoader = new ImageLoader(context);
                }
            }
        }

        return mImageLoader;
    }


    /**
     * 添加到本地缓存
     *
     * @param key   图片的路径(会被当做图片名称保存到硬盘上)
     * @param value 图片
     */
    private void addDiskCache(String key, Bitmap value) {
        //http://7xlovk.com2.z0.glb.qiniucdn.com/upload/com/%E9%A1%B9%E7%9B%AE/%E4%B8%8A%E6%B5%B717%E8%8B%B1%E9%87%8C/Sjt_04.jpg
        //消息摘要算法 MD5算法 抗修改性
        String fileName = MD5Utils.decode(key);

        //存储路径
        String path = context.getCacheDir().getAbsolutePath() + File.separator + fileName;

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(path);
            value.compress(Bitmap.CompressFormat.JPEG, 100, os);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    /**
     * 加载图片
     *
     * @param url
     * @param imageView
     */
    public void loadImage(String url, ImageView imageView) {
        // 读取缓存
        Bitmap bitmap = getFromCache(url);

        if (bitmap != null) {
            // 取消该图片对应的所有异步请求
            cancelDownload(url, imageView);

            // 设置图片
            imageView.setImageBitmap(bitmap);

        } else {
            // 设置加载过程中,空白图片
            imageView.setImageDrawable(defaultImage);

            // 访问网络,请求图片
            AsynImageLoadTask task = new AsynImageLoadTask(imageView);
            task.execute(url);
        }

    }

    /**
     * 标记异步线程下载
     * <p/>
     * 可能有多个异步线程在下载同一张图片
     *
     * @param url
     * @param imageView
     */
    private void cancelDownload(String url, ImageView imageView) {
        AsynImageLoadTask task = new AsynImageLoadTask(imageView);

        String downloadKey = task.key;

        if (downloadKey == null || !downloadKey.equals(url)) {
            //设置标示
            task.cancel(true);
        }

    }

    /**
     * 获取缓存
     *
     * @param key
     * @return
     */
    private Bitmap getFromCache(String key) {
        Bitmap bitmap = null;

        //从一级缓存加载
        synchronized (firstCacheMap) {
            bitmap = firstCacheMap.get(key);

            //保持图片的fresh新鲜(LRU)
            if (bitmap != null) {
                firstCacheMap.remove(key);
                firstCacheMap.put(key, bitmap);
                return bitmap;
            }
        }

        //从二级缓存加载
        SoftReference<Bitmap> softReference = secondCacheMap.get(key);

        //有可能被回收
        if (softReference != null) {
            bitmap = softReference.get();

            if (bitmap != null) {
                // 添加到一级缓存,为了下一次读取更快
                firstCacheMap.put(key, bitmap);

                return bitmap;
            }

        } else {
            //软引用被回收了，清除缓存
            secondCacheMap.remove(key);
        }

        //从三级本地缓存加载
        bitmap = getFromLocal(key);

        if (bitmap != null) {
            // 添加到一级缓存,为了下一次读取更快
            firstCacheMap.put(key, bitmap);

            return bitmap;
        }

        return null;
    }

    /**
     * 读取本地缓存
     *
     * @param key
     * @return
     */
    private Bitmap getFromLocal(String key) {
        String fileName = MD5Utils.decode(key);

//        if (fileName == null) {
//            return null;
//        }

        //存储路径:/data/data/pull.cn.com.htmldemo/cache/9e0a476f7ccbc6afdcf5dbe217cd639b
        String path = context.getCacheDir().getAbsolutePath() + File.separator + fileName;

        FileInputStream is = null;
        try {
            File file = new File(path);

            if (file.exists()) {
                //读取文件
                is = new FileInputStream(file);
                return BitmapFactory.decodeStream(is);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        return null;
    }

    /**
     * 异步线程加载网络图片
     */
    public class AsynImageLoadTask extends AsyncTask<String, Void, Bitmap> {
        /**
         * 图片的地址
         */
        private String key;
        private ImageView imageView;

        public AsynImageLoadTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            key = params[0];

            //下载图片
            return download(key);
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            //取消下载
            if (isCancelled()) {
                bitmap = null;
            }

            if (bitmap != null) {
                //下载完成后，添加到一级缓存中
                addFirstCache(key, bitmap);

                //显示图片
                imageView.setImageBitmap(bitmap);
            }
        }

    }

    /**
     * 默认图片
     */
    private DefaultImage defaultImage = new DefaultImage();

    /**
     * 默认图片
     */
    public class DefaultImage extends ColorDrawable {
        public DefaultImage() {
            super(Color.GRAY);
        }
    }

    /**
     * 添加到一级缓存
     *
     * @param key
     * @param bitmap
     */
    private void addFirstCache(String key, Bitmap bitmap) {
        if (bitmap != null) {
            //firstCacheMap线程不同步，所以添加同步锁
            synchronized (firstCacheMap) {
                firstCacheMap.put(key, bitmap);
            }
        }
    }

    /**
     * 下载网络图片
     *
     * @param key
     * @return
     */
    private Bitmap download(String key) {
        InputStream is = null;

        try {
            is = HttpUtils.download(key);

            return BitmapFactory.decodeStream(is);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
