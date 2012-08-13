package de.jensnistler.routemap.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;
import de.jensnistler.routemap.R;
import de.jensnistler.routemap.activities.Main;

public class TrackDownloader extends AsyncTask<TrackModel, Integer, Integer> {
    private boolean mRunning = true;
    private Context mContext;
    private ProgressDialog mDialog;

    public TrackDownloader(Context context) {
        mContext = context;
        mDialog = new ProgressDialog(mContext);
    }

    protected void onPreExecute() {
        mDialog.setMessage(mContext.getResources().getString(R.string.downloading));
        mDialog.setCancelable(true);
        mDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                onCancelled();
            }
        });
        mDialog.show();
    }

    protected void onCancelled() {
        mRunning = false;
    }

    protected Integer doInBackground(TrackModel... tracks) {
        Integer count = 0;

        for (TrackModel track: tracks) {
            try {
                String url = track.getLink();
                if (0 == url.length()) {
                    continue;
                }

                URL trackUrl = new URL(url);
                HttpURLConnection urlConnection = (HttpURLConnection) trackUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                // disguise as chrome
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/536.6 (KHTML, like Gecko) Chrome/20.0.1092.0 Safari/536.6");
                urlConnection.connect();

                File cacheDir = mContext.getExternalCacheDir();
                if (null == cacheDir || !cacheDir.canWrite()) {
                    continue;
                }
                File cacheFile = new File(cacheDir, track.getKey() + ".gpx");

                FileOutputStream fileOutput = new FileOutputStream(cacheFile);
                InputStream inputStream = urlConnection.getInputStream();

                int totalSize = urlConnection.getContentLength();
                int downloadedSize = 0;

                //create a buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0;

                while (true == mRunning && (bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutput.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;

                    publishProgress(totalSize, downloadedSize);
                }

                //close the output stream when done
                urlConnection.disconnect();
                fileOutput.close();

                // file was not downloaded completely
                if (downloadedSize < totalSize) {
                    cacheFile.delete();
                    continue;
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("routeFile", cacheFile.getAbsolutePath());
                editor.commit();

                count++;
            //catch some possible errors...
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return count; 
    }

    protected void onProgressUpdate(Integer... progress) {
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setMax(progress[0]);
        mDialog.setProgress(progress[1]);
    }

    protected void onPostExecute(Integer count) {
        mDialog.dismiss();

        if (count > 0) {
            Intent mainActivity = new Intent(mContext, Main.class);
            mContext.startActivity(mainActivity);
        }
        else {
            Toast.makeText(mContext, R.string.downloadFailed, Toast.LENGTH_LONG).show();
        }
    }
}
