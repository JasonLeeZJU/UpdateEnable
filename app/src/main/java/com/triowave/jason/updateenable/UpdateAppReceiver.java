package com.triowave.jason.updateenable;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;

public class UpdateAppReceiver extends BroadcastReceiver {
    public UpdateAppReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Cursor cursor = null;

        try {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                if (UpdateAppUtils.downloadUpdateApkId >= 0) {
                    long downloadId = UpdateAppUtils.downloadUpdateApkId;
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadId);
                    DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                    cursor = downloadManager.query(query);
                    if (cursor.moveToFirst()) {
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_FAILED) {
                            downloadManager.remove(downloadId);
                        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            if (UpdateAppUtils.downloadUpdateApkFilePath != null) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                                    i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri uri = FileProvider.getUriForFile(context,context.getPackageName()+"" +
                                            ".fileprovider", new File("file://" + UpdateAppUtils
                                            .downloadUpdateApkFilePath));
                                }else {
                                    i.setDataAndType(Uri.parse("file://" + UpdateAppUtils.downloadUpdateApkFilePath),
                                            "application/vnd.android.package-archive");
                                }
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(i);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if ( cursor != null){
                cursor.close();
            }
        }
    }
}
