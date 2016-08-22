package com.ozner.yiquan.Command;

import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by C-sir@hotmail.com  on 2016/1/21.
 */
public class ImageLoaderInit {
    /**
     02. * 初始化ImageLoader
     03. */
public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
    File cacheDir = StorageUtils.getOwnCacheDirectory(context,"/OznerImage/Cache");// 获取到缓存的目录地址
//    ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
//		config.threadPriority(Thread.NORM_PRIORITY);
//    	config.denyCacheImageMultipleSizesInMemory();
//		config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
////		config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
//		config.tasksProcessingOrder(QueueProcessingType.LIFO);
//       // config.diskCache(new UnlimitedDiskCache(cacheDir));
//		config.writeDebugLogs(); // Remove for release app
//		// Initialize ImageLoader with configuration.
	ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
			.memoryCacheExtraOptions(480, 800) // default = device screen dimensions
			.diskCacheExtraOptions(480, 800, null)
			.threadPoolSize(3) // default
			.threadPriority(Thread.NORM_PRIORITY - 2) // default
			.tasksProcessingOrder(QueueProcessingType.FIFO) // default
			.denyCacheImageMultipleSizesInMemory()
			.memoryCache(new LruMemoryCache(2 * 1024 * 1024))
			.memoryCacheSize(2 * 1024 * 1024)
			.memoryCacheSizePercentage(13) // default
			.diskCache(new UnlimitedDiskCache(cacheDir)) // default
			.diskCacheSize(50 * 1024 * 1024)
			.diskCacheFileCount(100)
			.diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
			.imageDownloader(new BaseImageDownloader(context)) // default
			.imageDecoder(new BaseImageDecoder(true)) // default
			.defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
			.writeDebugLogs()
			.build();
		ImageLoader.getInstance().init(config);
	}
}
