package com.mario.recyclearn;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.particle.android.sdk.cloud.ParticleCloud;
import io.particle.android.sdk.cloud.ParticleCloudException;
import io.particle.android.sdk.cloud.ParticleCloudSDK;
import io.particle.android.sdk.cloud.ParticleDevice;

/**
 *
 * Created by williamoliver on 5/21/16.
 *
 */
public class ImageService {

    private static final String TAG = ImageService.class.getSimpleName();

    private URL url;
    public ImageService () {
        try {
            url = new URL("http://ucfpkmn.com:8080/isRecycling");
        } catch (Exception e) {

        }
    }

    public String sendBitmap(Bitmap image) throws IOException {

        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");
//            httpConn.setConnectTimeout(5000);
            httpConn.setDoInput(true);
            httpConn.setDoOutput(true);
            httpConn.setRequestProperty("Content-Type", "application/json");
        } catch (Exception e) {
            return null;
        }

        if(image == null) {
            return "";
        }
        OutputStream output = httpConn.getOutputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        byte[] result = Base64.decode(encodedImage, Base64.NO_WRAP);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imageType", "jpg");
            jsonObject.put("base64Data", encodedImage);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }

        Log.d(TAG, encodedImage);
        output.write(jsonObject.toString().getBytes());
        output.flush();
        output.close();

        InputStream in;

        try {
            in = httpConn.getInputStream();
        } catch (Exception ex) {
            in = httpConn.getErrorStream();
        }

        BufferedReader rd = new BufferedReader(new InputStreamReader(in));
        StringBuffer response = new StringBuffer();
        String line;
        while((line = rd.readLine()) != null) {
            response.append(line);
            response.append("\n");
        }
        httpConn.disconnect();
        return response.toString();
    }

    public void toggleServo(String command)  {
        try {
            ParticleCloudSDK.getCloud().logIn("kingweko@gmail.com", "!qAzXsW2");
            List<ParticleDevice> particleDevices = ParticleCloudSDK.getCloud().getDevices();
            for (ParticleDevice device : particleDevices) {
                if (device.getName().equals("Recyclearn")) {
                    List<String> args = new ArrayList<>();
                    args.add(command);
                    device.callFunction("rotate", args);
                }
            }
        } catch (Exception e) {
            Log.e(MainActivity.TAG, e.getMessage());
        }

    }

}
