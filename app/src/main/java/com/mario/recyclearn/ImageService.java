package com.mario.recyclearn;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by williamoliver on 5/21/16.
 */
public class ImageService {
    private URL url;
    public ImageService () {
        try {
            url = new URL("http://172.20.10.2:8080/isRecycling");
        } catch (Exception e) {

        }
    }

    public String sendBitmap(Bitmap image) throws IOException {

        HttpURLConnection httpConn = null;
        try {
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");
        } catch (Exception e) {
            return null;
        }

        OutputStream output = httpConn.getOutputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("imageType", "jpeg");
            jsonObject.put("base64Data", encodedImage);
        } catch (JSONException e) {
            Log.e("TAG", e.toString());
        }

        output.write(jsonObject.toString().getBytes());
        output.flush();
        output.close();
        httpConn.connect();
        String response = httpConn.getResponseMessage();
        httpConn.disconnect();
        return response;
    }

}
