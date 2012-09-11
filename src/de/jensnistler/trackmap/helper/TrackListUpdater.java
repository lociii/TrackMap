package de.jensnistler.trackmap.helper;

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

import de.jensnistler.trackmap.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class TrackListUpdater extends AsyncTask<String, Integer, Integer> {
    public static final String URL_TRACKS = "http://www.gpsies.com/api.do?key=%s&username=%s&limit=%s&filetype=gpxtrk&resultPage=%s";
    public static final String URL_NOTEPAD = "http://www.gpsies.com/api.do?key=%s&searchNotepadname=%s&limit=%s&filetype=gpxtrk&resultPage=%s";
    private static final String API_KEY = "cvghivivbcmwbsgs";
    private static final Integer LIMIT = 100;

    private Activity mContext;
    private TrackDataSource mDataSource;
    private TrackAdapter mAdapter;
    private ProgressDialog mDialog;
    private String mUrl;

    public TrackListUpdater(Activity context, TrackDataSource dataSource, TrackAdapter adapter, String url) {
        mContext = context;
        mDataSource = dataSource;
        mAdapter = adapter;
        mDialog = new ProgressDialog(mContext);
        mUrl = url;
    }

    protected void onPreExecute() {
        // keep screen on while downloading
        mContext.getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mDialog.setCancelable(true);
        mDialog.setMessage(mContext.getResources().getString(R.string.loading));
        mDialog.show();
    }

    protected Integer doInBackground(String... usernames) {
        int trackCount = 0;
        XPathExpression xpathTrack;
        XPathExpression xpathResultSize;
        XPathExpression xPathResultType;

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        try {
            xpathTrack = xpath.compile("/gpsies/tracks/track");
            xpathResultSize = xpath.compile("/gpsies/meta/resultSize");
            xPathResultType = xpath.compile("/gpsies/meta/searchNotepadname");
        }
        catch (Exception e) {
            return 0;
        }

        for (String username: usernames) {
            int page = 1;

            // loop until all tracks have been found
            try {
                while (true) {
                    String url = String.format(mUrl, API_KEY, username, LIMIT, page);

                    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(new URL(url).openStream());

                    // get tracks
                    NodeList tracks = (NodeList) xpathTrack.evaluate(doc, XPathConstants.NODESET);

                    // get type
                    Integer nodeType = TrackModel.TYPE_MY_TRACKS;
                    NodeList resultTypeNodes = (NodeList) xPathResultType.evaluate(doc, XPathConstants.NODESET);
                    if (resultTypeNodes.getLength() == 1) {
                        nodeType = TrackModel.TYPE_NOTEPAD;
                    }

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
                        model.setType(nodeType);
                        mDataSource.saveTrack(model);

                        trackCount++;
                    }

                    // check for last result page
                    NodeList resultSizeNodes = (NodeList) xpathResultSize.evaluate(doc, XPathConstants.NODESET);
                    if (resultSizeNodes.getLength() == 0 || Integer.parseInt(resultSizeNodes.item(0).getTextContent()) < LIMIT) {
                        break;
                    }

                    page++;
                }

                long epoch = System.currentTimeMillis() / 1000;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong("gpsiesLastUpdate", epoch);
                editor.commit();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return trackCount;
    }

    protected void onPostExecute(Integer count) {
        mDialog.dismiss();

        // finish keep screen on while downloading
        mContext.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (count == 0) {
            Toast.makeText(mContext, R.string.updateFailed, Toast.LENGTH_LONG).show();
            return;
        }

        if (mAdapter instanceof TrackAdapter) {
            mAdapter.loadData();
        }
    }
}
