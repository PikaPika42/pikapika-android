package com.pikapika.radar.update;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;

import com.pikapika.radar.R;
import com.pikapika.radar.helpers.ConfigReader;

import java.io.File;

/**
 * Created by flavioreyes on 8/23/16.
 */
public class AppUpdate {
    public static void downloadAndInstallAppUpdate(Context context, ConfigReader configReader) {
        try {
            String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
            String fileName = context.getString(R.string.app_name)+configReader.getRemoteVersion()+".apk";
            destination += fileName;
            final Uri uri = Uri.parse("file://" + destination);

            //Delete update file if exists
            File file = new File(destination);
            if (file.exists()) {
                file.delete();
            }


            //set download manager
            String url = configReader.getAPKUri();
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(context.getString(R.string.updating) + " "+ context.getString(R.string.app_name));

            //set destination
            request.setDestinationUri(uri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    install.setDataAndType(uri, "application/vnd.android.package-archive");
                    ctxt.startActivity(install);
                    ctxt.unregisterReceiver(this);
                }
            };
            //register receiver for when .apk download is complete
            context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            System.out.println("We have an error houston");
        }
    }
}
