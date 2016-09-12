package net.yourhome.app.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;




public class Util {
	public final static int BITMAP_TIMEOUT = 10000;
    public static final String LOG_PATH = "/yourhome/logs";
    public static String createImageFromBitmap(String fileName, Bitmap bitmap, Context context) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }
	/*public static Bitmap toGrayscale(Bitmap bmpOriginal)
	{        
	    int width, height;
	    Bitmap bitmapGrayscale = Bitmap.createBitmap(bmpOriginal);
	    height = bmpOriginal.getHeight();
	    width = bmpOriginal.getWidth();

	    Canvas c = new Canvas(bitmapGrayscale);
	    Paint paint = new Paint();
	    ColorMatrix cm = new ColorMatrix();
	    cm.setSaturation(0);
	    ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
	    paint.setColorFilter(f);
	    c.drawBitmap(bitmapGrayscale, 0, 0, paint);
	    return bitmapGrayscale;
	}*/
	public static String getStringValue(InputStream input) throws IOException {
		String result = "";
		while(input.available() > 0) {
			result += input.read();
		}
		return result;
	}
	/*public static DateTime convertTimestampToDateTime(Integer timestamp) {
		DateTime time = new DateTime(timestamp*1000L);
		return time;
	}*/
	public static Bitmap getBitmapFromURL(String src) throws IOException {
        URL url = new URL(src);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(BITMAP_TIMEOUT);
        connection.setConnectTimeout(BITMAP_TIMEOUT);
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(input);
        return myBitmap;
	}
	public static Bitmap addShadow(final Bitmap bm, final int dstHeight, final int dstWidth, int color, int size, float dx, float dy) {
	    final Bitmap mask = Bitmap.createBitmap(dstWidth, dstHeight, Config.ALPHA_8);

	    final Matrix scaleToFit = new Matrix();
	    final RectF src = new RectF(0, 0, bm.getWidth(), bm.getHeight());
	    final RectF dst = new RectF(0, 0, dstWidth - dx, dstHeight - dy);
	    scaleToFit.setRectToRect(src, dst, ScaleToFit.CENTER);

	    final Matrix dropShadow = new Matrix(scaleToFit);
	    dropShadow.postTranslate(dx, dy);

	    final Canvas maskCanvas = new Canvas(mask);
	    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    maskCanvas.drawBitmap(bm, scaleToFit, paint);
	    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_OUT));
	    maskCanvas.drawBitmap(bm, dropShadow, paint);

	    final BlurMaskFilter filter = new BlurMaskFilter(size, Blur.NORMAL);
	    paint.reset();
	    paint.setAntiAlias(true);
	    paint.setColor(color);
	    paint.setMaskFilter(filter);
	    paint.setFilterBitmap(true);

	    final Bitmap ret = Bitmap.createBitmap(dstWidth, dstHeight, Config.ARGB_8888);
	    final Canvas retCanvas = new Canvas(ret);
	    retCanvas.drawBitmap(mask, 0,  0, paint);
	    retCanvas.drawBitmap(bm, scaleToFit, null);
	    mask.recycle();
	    return ret;
	}
    /*public static void appendToLogFile(Context context, String text) {
        File externalStorage = context.getExternalFilesDir(null);
        if(externalStorage!=null) {
            // Check or create log dir
            File dir = new File( externalStorage, LOG_PATH);
            if(!dir.exists()){ dir.mkdirs();}
            // Check or create log file
            File logFile = new File(dir,"yourhome.errors.log");
            logFile.setReadable(true,false);
            BufferedWriter writer = null;
            try {
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                // Write to log file
                writer = new BufferedWriter(new FileWriter(logFile, true));
                writer.append(text);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }*/
}
