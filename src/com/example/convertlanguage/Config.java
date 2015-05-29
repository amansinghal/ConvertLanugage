package com.example.convertlanguage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpParams;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

public class Config 
{

	public static HttpClient CLIENT = getThreadSafeClient();


	public static DefaultHttpClient getThreadSafeClient() 
	{

		DefaultHttpClient client = new DefaultHttpClient();
		ClientConnectionManager mgr = client.getConnectionManager();
		HttpParams params = client.getParams();

		client = new DefaultHttpClient(new ThreadSafeClientConnManager(params,mgr.getSchemeRegistry()), params);

		return client;
	}

	public static String getText(EditText et)
	{
		return et.getText().toString().trim();
	}

	public static void alertDialogBox(Context con,final ActionBarActivity activity,final WhatToClose option,String msg,boolean cancelable)
	{
		AlertDialog.Builder dialog=new AlertDialog.Builder(con);
		dialog.setIcon(android.R.drawable.ic_dialog_alert);
		dialog.setTitle("Alert");
		dialog.setMessage(msg);
		dialog.setCancelable(cancelable);
		dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
		{

			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
				if(option!=null)
					if(option==WhatToClose.Fragment)
					{
						activity.getSupportFragmentManager().popBackStack();
					}
					else
					{
						activity.finish();
					}
			}
		});
		dialog.show();
	}

	public enum WhatToClose
	{
		Fragment,Activity
	};


	public static void hideKeyboard(Context con, Activity activity) 
	{
		InputMethodManager inputManager = (InputMethodManager) con.getSystemService(Context.INPUT_METHOD_SERVICE);

		// check if no view has focus:
		View view = activity.getCurrentFocus();

		if (view != null) 
		{
			inputManager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}


	public static String compressImage(String imagePath)
	{

		String filePath = imagePath;
		Bitmap scaledBitmap = null;

		BitmapFactory.Options options = new BitmapFactory.Options();

		//	      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
		//	      you try the use the bitmap here, you will get null.
		options.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

		int actualHeight = options.outHeight;
		int actualWidth = options.outWidth;

		//	      max Height and width values of the compressed image is taken as 816x612

		float maxHeight = 816.0f;
		float maxWidth = 612.0f;
		float imgRatio = actualWidth / actualHeight;
		float maxRatio = maxWidth / maxHeight;

		//	      width and height values are set maintaining the aspect ratio of the image

		if (actualHeight > maxHeight || actualWidth > maxWidth)
		{
			if (imgRatio < maxRatio) 
			{              
				imgRatio = maxHeight / actualHeight;             
				actualWidth = (int) (imgRatio * actualWidth);             
				actualHeight = (int) maxHeight;            
			}
			else if (imgRatio > maxRatio) 
			{
				imgRatio = maxWidth / actualWidth;
				actualHeight = (int) (imgRatio * actualHeight);
				actualWidth = (int) maxWidth;
			} 
			else 
			{
				actualHeight = (int) maxHeight;
				actualWidth = (int) maxWidth;

			}
		}

		//	      setting inSampleSize value allows to load a scaled down version of the original image

		options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

		//	      inJustDecodeBounds set to false to load the actual bitmap
		options.inJustDecodeBounds = false;

		//	      this options allow android to claim the bitmap memory if it runs low on memory
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inTempStorage = new byte[16 * 1024];

		try {
			//	          load the bitmap from its path
			bmp = BitmapFactory.decodeFile(filePath, options);
		} 
		catch (OutOfMemoryError exception) 
		{
			exception.printStackTrace();

		}
		try
		{
			scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight,Bitmap.Config.ARGB_8888);
		} 
		catch (OutOfMemoryError exception) 
		{
			exception.printStackTrace();
		}

		float ratioX = actualWidth / (float) options.outWidth;
		float ratioY = actualHeight / (float) options.outHeight;
		float middleX = actualWidth / 2.0f;
		float middleY = actualHeight / 2.0f;

		Matrix scaleMatrix = new Matrix();
		scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

		Canvas canvas = new Canvas(scaledBitmap);
		canvas.setMatrix(scaleMatrix);
		canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

		//	      check the rotation of the image and display it properly
		ExifInterface exif;
		try {
			exif = new ExifInterface(filePath);

			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
			Log.d("EXIF", "Exif: " + orientation);
			Matrix matrix = new Matrix();
			if (orientation == 6) 
			{
				matrix.postRotate(90);
				Log.d("EXIF", "Exif: " + orientation);
			}
			else if (orientation == 3) 
			{
				matrix.postRotate(180);
				Log.d("EXIF", "Exif: " + orientation);
			}
			else if (orientation == 8)
			{
				matrix.postRotate(270);
				Log.d("EXIF", "Exif: " + orientation);
			}
			scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,true);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		FileOutputStream out = null;
		String filename = getFilename(imagePath);
		File file=new File(filename);
		if(file.exists())
		{
			return filename;
		}
		try {
			out = new FileOutputStream(filename);

			//	          write the compressed bitmap at the destination specified by filename.
			scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return filename;

	}

	public static String getFilename(String imgPath) 
	{
		File file = new File(Environment.getExternalStorageDirectory().getPath(), "Pinburg/Images");
		if (!file.exists())
		{
			file.mkdirs();
		}
		String uriSting = (file.getAbsolutePath() + "/" + new File(imgPath).getName());
		return uriSting;

	}

	public static int  calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) 
		{
			final int heightRatio = Math.round((float) height/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;      }    
		final float totalPixels = width * height;      
		final float totalReqPixelsCap = reqWidth * reqHeight * 2;      
		while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
			inSampleSize++;
		}

		return inSampleSize;
	}
	

	
	 @TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressWarnings("deprecation")
	public static boolean isLocationServiceEnabled(Context con) 
	 { 
		/* LocationManager lm = (LocationManager)con.getSystemService(Context.LOCATION_SERVICE);
		 String provider = lm.getBestProvider(new Criteria(), true);
		 return (!provider.isEmpty()&&!LocationManager.PASSIVE_PROVIDER.equals(provider));*/
		 
		 int locationMode = 0;
		  
		 String locationProviders;
		 
		    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
		    {
		        try 
		        { 
		            locationMode = Settings.Secure.getInt(con.getContentResolver(), Settings.Secure.LOCATION_MODE);
		 
		        } 
		        catch (SettingNotFoundException e) 
		        {
		            e.printStackTrace();
		        } 
		 
		        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
		 
		    }
		    else
		    { 
		        locationProviders = Settings.Secure.getString(con.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		        return !TextUtils.isEmpty(locationProviders);
		    } 
		 
		 
	 } 	 

		 public static void expand(final View v) 
		 {
	        v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	        final int targetHeight = v.getMeasuredHeight();

	        v.getLayoutParams().height = 0;
	        v.setVisibility(View.VISIBLE);
	        Animation a = new Animation()
	        {
	            @Override
	            protected void applyTransformation(float interpolatedTime, Transformation t) {
	                v.getLayoutParams().height = interpolatedTime == 1
	                        ? LayoutParams.MATCH_PARENT
	                        : (int)(targetHeight * interpolatedTime);
	                v.requestLayout();
	            }

	            @Override
	            public boolean willChangeBounds() {
	                return true;
	            }
	        };

	        // 1dp/ms
	        a.setDuration(200);
	        a.setInterpolator(new LinearInterpolator());
	        v.startAnimation(a);
	    }

	    public static void collapse(final View v) 
		{
	        final int initialHeight = v.getMeasuredHeight();

	        Animation a = new Animation()
	        {
	            @Override
	            protected void applyTransformation(float interpolatedTime, Transformation t) {
	                if(interpolatedTime == 1){
	                    v.setVisibility(View.GONE);
	                }else{
	                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
	                    v.requestLayout();
	                }
	            }

	            @Override
	            public boolean willChangeBounds() {
	                return true;
	            }
	        };

	        // 1dp/ms
	        a.setDuration(200);
	        a.setInterpolator(new LinearInterpolator());
	        v.startAnimation(a);
	    }
}
