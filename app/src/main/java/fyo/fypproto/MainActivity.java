package fyo.fypproto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView textView;

    private Camera camera;
    private int cameraId = 0;

    private BroadcastReceiver broadcastReceiver;

    public final static String DEBUG_TAG = "MakePhotoActivity";


    static{
        if(OpenCVLoader.initDebug()) {
            Log.d(DEBUG_TAG,"cv loaded");
        }else{
            Log.d(DEBUG_TAG,"cv failed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        textView = findViewById(R.id.textView);

        // do we have a camera?
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findFacingCamera();
            camera = Camera.open(cameraId);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.startPreview();
                camera.takePicture(null,null,new PhotoHandler(getApplicationContext()));
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try{
                    textView.setText(intent.getExtras().get("total").toString());
                }catch (Exception e){
                    Log.d(DEBUG_TAG,e.toString());
                }
            }
        };
    }

    private int findFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d(DEBUG_TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(MainActivity.DEBUG_TAG, "onResume()");
        registerReceiver(broadcastReceiver,new IntentFilter("area"));
        Camera.Parameters p = camera.getParameters();
        try {
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            camera.startPreview();
        } catch (Exception e) {
            Log.d(DEBUG_TAG, e.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(MainActivity.DEBUG_TAG,"onPause()");
        Camera.Parameters p = camera.getParameters();
        try{
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            camera.stopPreview();
        }catch (Exception e){
            Log.d(DEBUG_TAG,e.toString());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(MainActivity.DEBUG_TAG,"onDestroy()");
        if (camera != null) {
            camera.release();
            camera = null;
        }

        if(broadcastReceiver!=null){
            unregisterReceiver(broadcastReceiver);
        }
    }
}
