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
    private static final String URL = "http://www.gpsies.com/api.do?key=%s&username=%s&limit=%s&filetype=gpxtrk&resultPage=%s";
    private static final String API_KEY = "cvghivivbcmwbsgs";
    private static final Integer LIMIT = 100;

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

    protected Integer doInBackground(String... usernames) {
        int trackCount = 0;
        XPathExpression xpathTrack;
        XPathExpression xpathResultSize;

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            xpathTrack = xpath.compile("/gpsies/tracks/track");
            xpathResultSize = xpath.compile("/gpsies/meta/resultSize");
        }
        catch (Exception e) {
            return 0;
        }

        for (String username: usernames) {
            int page = 1;

            // loop until all tracks have been found
            while (true) {
                String url = String.format(URL, API_KEY, username, LIMIT, page);

                try {
                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(new URL(url).openStream());

                    // get tracks
                    NodeList tracks = (NodeList) xpathTrack.evaluate(doc, XPathConstants.NODESET);

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

                    // check for last result page
                    NodeList resultSizeNodes = (NodeList) xpathResultSize.evaluate(doc, XPathConstants.NODESET);
                    if (Integer.parseInt(resultSizeNodes.item(0).getTextContent()) < LIMIT) {
                        break;
                    }

                    page++;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
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
