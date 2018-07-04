package com.triowave.jason.updateenable;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static android.content.ContentValues.TAG;
import static android.content.Context.DOWNLOAD_SERVICE;

public class UpdateAppUtils{

    private static JSONObject jsonObject;

    private static String Server_ver_name;
    private static String Server_update_content;
    private static String Server_update_url;
    private static int Server_ver_code;
    private static boolean Server_update_ignoreable;
    private static String Server_update_md5;
    private static Handler handler;

    public static String downloadUpdateApkFilePath;
    public static long downloadUpdateApkId = -1;


    public static void checkAndUpdate(final Context ctx, String jsonAddress) {
        getServerJson(jsonAddress);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //Log.i(TAG, "checkAndUpdate: "+msg.obj);

                try {
                    jsonObject = new JSONObject((String) msg.obj);
                    Server_ver_name = jsonObject.getString("update_ver_name");
                    Server_update_content = jsonObject.getString("update_content");
                    Server_update_url = jsonObject.getString("update_url");
                    Server_ver_code = jsonObject.getInt("update_ver_code");
                    Server_update_ignoreable = jsonObject.getBoolean("ignore_able");
                    Server_update_md5 = jsonObject.getString("md5");
                    //Log.i("JSONObject fields",
                            //Server_ver_name +"\n"+Server_ver_code+"\n"+Server_update_content
                                    //+"\n"+Server_update_url+"\n"+Server_update_ignoreable+"\n" +
                           // ""+Server_update_md5);

                    if (Server_ver_code > getAPPLocalVersion(ctx)){
                        if (ContextCompat.checkSelfPermission(ctx,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                                .PERMISSION_GRANTED){
                            update(ctx);
                            Log.i(TAG,"有新版本，开始下载");
                        }
                        else{
                            ActivityCompat.requestPermissions((Activity) ctx,new String[]{Manifest
                                    .permission.WRITE_EXTERNAL_STORAGE},1);
                            Log.i(TAG,"有新版本，申请权限");
                            update(ctx);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        };


    }

    private static void getServerJson(final String jsonAddress) {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    HttpURLConnection urlConnection = (HttpURLConnection) new URL(jsonAddress).openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(3000);

                    int responseCode = urlConnection.getResponseCode();

                    Log.i(TAG, "checkAndUpdate: "+responseCode);

                    if (responseCode == 200){
                        InputStream inputStream = urlConnection.getInputStream();
                        String stream = toStream(inputStream);

                        /*

                        //Log.i("JSON",stream);

                        jsonObject = new JSONObject(stream);
                        //Log.i("JSON",jsonObject.toString());

                        Server_ver_name = jsonObject.getString("update_ver_name");
                        Server_update_content = jsonObject.getString("update_content");
                        Server_update_url = jsonObject.getString("update_url");
                        Server_ver_code = jsonObject.getInt("update_ver_code");
                        Server_update_ignoreable = jsonObject.getBoolean("ignore_able");
                        Server_update_md5 = jsonObject.getString("md5");

                        Log.i("JSON",
                                Server_ver_name+Server_ver_code+Server_update_content
                               +Server_update_url+Server_update_ignoreable+Server_update_md5);
                               */

                         Message message = Message.obtain();
                         message.obj = stream;
                         handler.sendMessage(message);

                    }
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } .start();
    }

    private static int getAPPLocalVersion(Context ctx) {
        String localVersionName;
        int localVersionCode = 0;

        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
            localVersionName = info.versionName; // 版本名
            localVersionCode = info.versionCode; // 版本号
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersionCode;
    }

    private static String toStream(InputStream inputStream) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        int length = 0;
        byte[] buffer = new byte[1024];

        while ((length = inputStream.read(buffer)) != -1){
            outputStream.write(buffer,0,length);
        }

        inputStream.close();
        outputStream.close();

        return outputStream.toString();

    }

    private static void update(final Context ctx) {
        final ConfirmDialog confirmDialog = new ConfirmDialog(ctx, new Callback() {
            @Override
            public void callback(int position) {
                switch (position){
                    case 0:  //cancel
                        if (!Server_update_ignoreable){
                            //Toast.makeText(ctx,"必须更新！",Toast.LENGTH_SHORT);
                            System.exit(0);
                        }
                        break;
                    case 1:
                        if (isWifiConnect(ctx)) {
                            DownloadApp(ctx, Server_update_url, ctx.getString(R.string.AppName));
                        }else {
                                new ConfirmDialog(ctx, new Callback() {
                                    @Override
                                    public void callback(int position) {
                                        if (position == 1){
                                            DownloadApp(ctx, Server_update_url, ctx.getString(R.string.AppName));
                                        }else {
                                            if (!Server_update_ignoreable){
                                            }
                                        }
                                    }
                                }).setContent("目前没有WIFI连接\n是否继续下载更新？").show();
                        }
                        break;
                }
            }
        });
        String content = "发现新版本:"+Server_ver_name+"，是否更新？ \n"+Server_update_content;
        if (TextUtils.isEmpty(Server_update_content)){
            content = "是否更新到新版本:"+Server_ver_name+"?";
        }
        confirmDialog.setContent(content);
        confirmDialog.setCancelable(false);
        confirmDialog.show();
    }

    private static void DownloadApp(Context ctx, String url, String titile) {
        if (TextUtils.isEmpty(url)) {
            Log.i(TAG, "DownloadApp: 下载地址为空");
            return;
        }
        try {
            Log.i(TAG, "DownloadApp: 下载地址不为空");

        DownloadManager downloadManager = (DownloadManager) ctx.getSystemService(DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedOverRoaming(false);

        String rootPath = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                //Log.i(TAG, "DownloadApp: 找到储存位置，准备下载更新" + rootPath);
                //Toast.makeText(ctx, "找到储存位置，准备下载更新", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ctx, "没有储存空间，无法下载更新", Toast.LENGTH_SHORT).show();
                return;
            }

        downloadUpdateApkFilePath = rootPath + File.separator + titile +"_"+Server_ver_name +".apk";
        Uri fileUri = Uri.parse("file://" + downloadUpdateApkFilePath);

        deleteFile(downloadUpdateApkFilePath);

        request.setDestinationUri(fileUri);
        downloadUpdateApkId = downloadManager.enqueue(request);

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setMimeType("application/vnd.android.package-archive");

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(titile);
        request.setDescription("正在下载中...");
        request.setVisibleInDownloadsUi(true);


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void deleteFile(String downloadUpdateApkFilePath) {
        File file = new File(downloadUpdateApkFilePath);
        if (file.exists())
        {
            file.delete();
        }
    }

    private static boolean isWifiConnect(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        if (connectivityManager != null){
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }
        return false;
    }


}