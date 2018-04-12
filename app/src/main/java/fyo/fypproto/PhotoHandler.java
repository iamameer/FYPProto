package fyo.fypproto;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhotoHandler implements Camera.PictureCallback {
    private final Context context;
    public final static String DEBUG_TAG = "MakePhotoActivity";

    public PhotoHandler(Context context) {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        /*File pictureFileDir = getDir();

        if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

            Log.d(MainActivity.DEBUG_TAG, "Can't create directory to save image.");
            Toast.makeText(context, "Can't create directory to save image.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "Picture+"+date+"+.jpg";

        String filename = pictureFileDir.getPath() + File.separator + photoFile;
        File pictureFile = new File(filename);


        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(bytes);
            fos.close();
            Toast.makeText(context, "New Image saved:" + photoFile,
                    Toast.LENGTH_LONG).show();
            Log.d(DEBUG_TAG,"filename: "+filename);
        } catch (Exception error) {
            Log.d(MainActivity.DEBUG_TAG, "File" + filename + "not saved: "
                    + error.getMessage());
            Toast.makeText(context, "Image could not be saved.",
                    Toast.LENGTH_LONG).show();
        }*/

        //PROC PART
        try{
            Log.d(DEBUG_TAG,"decoding byte to bitmap");
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);

            Mat omat = new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC4);
            Log.d(DEBUG_TAG,"util b>m");
            Utils.bitmapToMat(bitmap,omat);

            Log.d(DEBUG_TAG,"threshold");
            Mat tmat = new Mat(bitmap.getWidth(),bitmap.getHeight(), CvType.CV_8UC4);
            Imgproc.threshold(omat,tmat,0,255,Imgproc.THRESH_BINARY);

            Log.d(DEBUG_TAG,"canny");
            Mat cmat = new Mat();
            Imgproc.Canny(tmat,cmat,10,100);

            Log.d(DEBUG_TAG,"findCont");
            Mat fmat = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(cmat,contours,fmat,Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
            Log.d(DEBUG_TAG,"drawCont");
            Imgproc.drawContours(omat,contours,-1,new Scalar(0,0,255),5);

            Imgcodecs.imwrite(Environment.getExternalStorageDirectory()+"/Pictures/CameraAPIDemo/Pic.jpg",cmat);

            Log.d(DEBUG_TAG,"For loop");
            Double total=0.0;
            for(int i = 0 ; i < contours.size() ; i++){
                total = total+Imgproc.contourArea(contours.get(i));
            }
            Log.d(DEBUG_TAG,"ContourArea: "+total);

            Intent intent = new Intent("area");
            intent.putExtra("total",total);
            context.sendBroadcast(intent);
        }catch (Exception e){
            Log.d(MainActivity.DEBUG_TAG,e.toString());
        }
    }

    private File getDir() {
        File sdDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "CameraAPIDemo");
    }
}
