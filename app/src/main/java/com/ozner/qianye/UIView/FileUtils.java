package com.ozner.qianye.UIView;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Calendar;

public class FileUtils {
	public static String saveBitmap(Bitmap bm) {
		Bitmap bitmap =bm;
		File temp = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/OznerImage/Cache");// 自已缓存文件夹
		if (!temp.exists()) {
			temp.mkdirs();
		}
		File tempFile = new File(temp.getAbsolutePath() + "/"
				+ Calendar.getInstance().getTimeInMillis() + ".jpg"); // 以时间秒为文件名
		// 图像保存到文件中
		FileOutputStream foutput = null;
		try {
			foutput = new FileOutputStream(tempFile);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, foutput)) {
				//  filePath=tempFile.getAbsolutePath();
			return	tempFile.getPath();
				//return Uri.fromFile(tempFile);
			}
			else
				return null;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		finally {
			try{
				if(foutput!=null)
					foutput.close();

			}catch (Exception ex){ex.printStackTrace();}
		}

	}
	public static String saveBitmap(Bitmap bm,String path) {
		Bitmap bitmap =bm;
		File temp = new File(Environment.getExternalStorageDirectory()
				.getPath() + "/OznerImage/Cache");// 自已缓存文件夹
		if (!temp.exists()) {
			temp.mkdirs();
		}
		File tempFile = new File(temp.getAbsolutePath() + "/"
				+ path + ".jpg"); // 以时间秒为文件名
		// 图像保存到文件中
		FileOutputStream foutput = null;
		try {
			foutput = new FileOutputStream(tempFile);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, foutput)) {
				//  filePath=tempFile.getAbsolutePath();
				return	tempFile.getPath();
				//return Uri.fromFile(tempFile);
			}
			else
				return null;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		finally {
			try{
				if(foutput!=null)
					foutput.close();

			}catch (Exception ex){ex.printStackTrace();}
		}

	}

}
