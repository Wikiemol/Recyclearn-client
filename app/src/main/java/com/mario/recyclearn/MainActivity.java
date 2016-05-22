package com.mario.recyclearn;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloudSDK;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String imageDirectory = Environment.getExternalStorageDirectory() + "/Recyclearn";
    private String mCurrentPhotoPath;
    private Bitmap mBitmap;
    private ProgressBar mProgressBar;
    private View rootLayout;
    private Drawable background;
    private FloatingActionButton cameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ParticleCloudSDK.init(this.getApplicationContext());
        if(savedInstanceState == null) {
            Intent intent = getIntent();

        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        rootLayout = findViewById(R.id.backgroundView);
        background = getDrawable(R.drawable.android_leaf_dark);
        setSupportActionBar(toolbar);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        cameraButton = (FloatingActionButton) findViewById(R.id.fab);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;

            try {
                photoFile = createImageFile();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if(photoFile != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void sendImage() {

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading recycling information...");
        progress.show();
        AsyncTask<Void, Void, JSONObject> worker = new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... params) {
                JSONObject json = null;
                String result = null;
                ImageService service = new ImageService();
                try {
                    result = service.sendBitmap(mBitmap);
                    if (!result.isEmpty()) {
                        json = new JSONObject(result);

                        Boolean recyclable = json.getBoolean("recycling");
                        if (recyclable) {
                            service.toggleServo("left");
                        } else {
                            service.toggleServo("right");
                        }
                    }
                    service.toggleServo("middle");

                } catch (Exception ex) {
                    Log.d(TAG, result);
                    ex.printStackTrace();
                }
                return json;
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result == null) {
                    result = new JSONObject();
                    try {
                        result.put("error", true);
                        result.put("recycling", false);
                        result.put("hints", new JSONArray(new Object[]{"There was an error with the server, please try again later."}));
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
                rootLayout.setAlpha(0.3f);
                background.setAlpha(190);
                cameraButton.setAlpha(0.f);
                Log.i(TAG, result.toString());

                CheckMark checkMark = CheckMark.newInstance(result);
                checkMark.show(getFragmentManager(), "dialog");


                progress.dismiss();
            }
        };
        worker.execute();


    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if(data != null) {
                Bundle extras = data.getExtras();
                mBitmap = (Bitmap) extras.get("data");
                sendImage();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("bitmapimage", mBitmap);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle inState) {
        mBitmap = inState.getParcelable("bitmapimage");
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    public void resetAlpha() {
        rootLayout.setAlpha(1.0f);
        background.setAlpha(255);
        cameraButton.setAlpha(1.0f);
    }

}

