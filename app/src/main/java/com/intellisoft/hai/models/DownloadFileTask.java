package com.intellisoft.hai.models;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileTask extends AsyncTask<String, Integer, Boolean> {

    private Context context;
    private ProgressDialog progressDialog;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int notificationId = 1;

    public DownloadFileTask(Context context, ProgressDialog progressDialog) {
        this.context = context;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }
    @Override
    protected Boolean doInBackground(String... params) {

        String fileUrl = params[0];

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
            File file = new File(directory, "file-to-download.pdf");
            OutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int fileSize = urlConnection.getContentLength();
            int downloadedSize = 0;
            int bufferLength;

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bufferLength);
                downloadedSize += bufferLength;
                publishProgress(downloadedSize, fileSize);
            }

            inputStream.close();
            outputStream.close();
            urlConnection.disconnect();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int downloadedSize = values[0];
        int fileSize = values[1];
        int progress = (int) (((float) downloadedSize / fileSize) * 100);
       progressDialog.setProgress(progress);
        progressDialog.setMessage("Downloaded "+progress+" %");

    }
    @Override
    protected void onPostExecute(Boolean result) {
        progressDialog.dismiss();
        if (result) {
            // Show a toast message or perform any other action to indicate success
            Toast.makeText(context, "Download Successful", Toast.LENGTH_SHORT).show();
        } else {
            // Show a toast message or perform any other action to indicate failure
            Toast.makeText(context, "Failed to download", Toast.LENGTH_SHORT).show();
        }
    }
}