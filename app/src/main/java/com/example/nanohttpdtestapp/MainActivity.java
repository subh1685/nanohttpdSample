package com.example.nanohttpdtestapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;


public class MainActivity extends AppCompatActivity {
    private WebServer server;
    public Activity currActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currActivity = this;
        server = new WebServer(8080);
        try {
            server.start();
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
    }

    class WebServer extends NanoHTTPD{
        public int requestCount = 0;
        public WebServer(int port){
            super(port);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String uri = session.getUri();
            FileInputStream fis = null;
            Map<String, String> files = new HashMap<>();
            requestCount += 1;
            currActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(currActivity, "Got new request "+ requestCount, Toast.LENGTH_SHORT).show();
                }
            });

            if("/get".equalsIgnoreCase(uri)) {
                String fileName = Environment.getExternalStorageDirectory()
                        + "/nanofile.txt";
                File file = new File(fileName);
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
//                return newFixedLengthResponse(Response.Status.OK, "application/octet-stream", fis, file.length());
                return newFixedLengthResponse(Response.Status.OK, "text/plain", fis, file.length());
            }
            else{
                try {
                    session.parseBody(files);
                    File dst = new File(Environment.getExternalStorageDirectory()+"/nanoupload.txt");
                    File src = new File(files.get("myFile"));
                    try {
                        copy(src, dst);
                    }catch (Exception e){ e.printStackTrace();}
                }
                catch (Exception e){

                }
                return newFixedLengthResponse("{}");
            }
        }

        public void copy(File src, File dst) throws IOException {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
}