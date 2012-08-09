package de.jensnistler.routemap.helper;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.jensnistler.routemap.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class TrackListUpdater extends AsyncTask<String, Integer, Integer> {
    private Context mContext;
    private TrackAdapter mAdapter;
    private TrackDataSource mDataSource;
    private ProgressDialog mDialog;

    public TrackListUpdater(Context context, TrackAdapter adapter, TrackDataSource dataSource) {
        mContext = context;
        mAdapter = adapter;
        mDataSource = dataSource;
        mDialog = new ProgressDialog(mContext);
    }

    protected void onPreExecute() {
        mAdapter.setNotifyOnChange(false);
        mAdapter.clear();

        mDialog.setCancelable(false);
        mDialog.setMessage(mContext.getResources().getString(R.string.loading));
        mDialog.show();
    }

    protected Integer doInBackground(String... urls) {
        int trackCount = 0;

        for (String url: urls) {
            int page = 1;

            // loop until all tracks have been found
            while (true) {
                String urlPage = url.replace("<page>", page + "");

                try {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(new URL(urlPage).openStream());

                    // get tracks
                    XPathFactory xpathFactory = XPathFactory.newInstance();
                    XPath xpath = xpathFactory.newXPath();
                    XPathExpression xpathExpression = xpath.compile("/gpsies/tracks/track");
                    NodeList tracks = (NodeList) xpathExpression.evaluate(doc, XPathConstants.NODESET);

                    // loop tracks
                    for (int i = 0; i < tracks.getLength(); i++) {
                        Element track = (Element) tracks.item(i);

                        String key = track.getElementsByTagName("fileId").item(0).getTextContent();
                        String description = track.getElementsByTagName("title").item(0).getTextContent();
                        Float length = Float.parseFloat(track.getElementsByTagName("trackLengthM").item(0).getTextContent());
                        String link = track.getElementsByTagName("downloadLink").item(0).getTextContent();

                        TrackModel model = mDataSource.getTrack(key);
                        if (null == model) {
                            model = new TrackModel(key);
                        }
                        model.setDescription(description);
                        model.setLength(length);
                        model.setLink(link);
                        if (true == mDataSource.saveTrack(model)) {
                            mAdapter.add(model);
                        }
                        trackCount++;
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                page++;
                break;
            }
        }

        long epoch = System.currentTimeMillis() / 1000;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("gpsiesLastUpdate", epoch);
        editor.commit();

        return trackCount;
    }

    protected void onPostExecute(Integer count) {
        mDialog.dismiss();
        mAdapter.setNotifyOnChange(true);

        if (count > 0) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
