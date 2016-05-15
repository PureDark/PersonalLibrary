package ml.puredark.personallibrary;

/**
 * Created by PureDark on 2015/10/16.
 */

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class PLApplication extends Application {
    public static Context mContext;
    // 全局变量，用于跨Activity传输复杂对象
    public static Object temp;
    public static Bitmap bitmap;

    //服务器地址
    public static String serverHost = "http://115.28.135.76";

    @SuppressWarnings("unused")
    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        initImageLoader(getApplicationContext());
    }

    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
        .cacheInMemory(true)
        .cacheOnDisk(true)
        .build();
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.defaultDisplayImageOptions(defaultOptions);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.memoryCache(new LruMemoryCache(2 * 1024 * 1024));
        config.tasksProcessingOrder(QueueProcessingType.FIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }
    public static String getVersionName() throws Exception
    {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(mContext.getPackageName(),0);
        String version = packInfo.versionName;
        return version;
    }

}
