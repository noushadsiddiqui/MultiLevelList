package accrecorder.recorder.com.multilevellist;

import android.app.ListActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import accrecorder.recorder.com.helper.AlertDialogManager;
import accrecorder.recorder.com.helper.ConnectionDetector;
import accrecorder.recorder.com.helper.JSONParser;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TrackListActivity extends ListActivity {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> tracksList;

    // tracks JSONArray
    JSONArray albums = null;

    // Album id
    String state_id, city_id, city_name;

    // tracks JSON url
    // id - should be posted as GET params to get track list (ex: id = 5)
    // private static final String URL_ALBUMS = "http://api.androidhive.info/songs/album_tracks.php";
    private String URL_ALBUMS = "http://ec2-52-26-104-86.us-west-2.compute.amazonaws.com:8090/stations";

    // ALL JSON node names
    // private static final String TAG_SONGS = "songs";
    // private static final String TAG_ID = "id";
    // private static final String TAG_NAME = "name";
    // private static final String TAG_ALBUM = "album";
    // private static final String TAG_DURATION = "duration";

    private static final String STATION_NAME = "station_name";
    private static final String STATION_DISPLAY_NAME = "station_display_name";

    private static final String STATION_NAME_RES = "stationName";
    private static final String STATION_DISPLAY_NAME_RES = "fullStationName";
    private static final String CITY_NAME_RES = "cityName";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        cd = new ConnectionDetector(getApplicationContext());

        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(TrackListActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Get album id
        Intent i = getIntent();
        state_id = i.getStringExtra("state_id");
        city_id = i.getStringExtra("city_id");

        // Hashmap for ListView
        tracksList = new ArrayList<HashMap<String, String>>();

        // Loading tracks in Background Thread
        new LoadTracks().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview on item click listener
         * SingleTrackActivity will be lauched by passing album id, song id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // On selecting single track get song information
                Intent i = new Intent(getApplicationContext(), SingleTrackActivity.class);

                // to get song information
                // both album id and song is needed
                // String album_id = ((TextView) view.findViewById(R.id.album_id)).getText().toString();
                // String song_id = ((TextView) view.findViewById(R.id.song_id)).getText().toString();

                String state_id = ((TextView) view.findViewById(R.id.state_id)).getText().toString();
                String city_id = ((TextView) view.findViewById(R.id.city_id)).getText().toString();
                String station_name = ((TextView) view.findViewById(R.id.station_name)).getText().toString();

                //Toast.makeText(getApplicationContext(), "Album Id: " + album_id  + ", Song Id: " + song_id, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "State Id: " + state_id  + ", City Id: " + city_id + ", Station Name: " + station_name, Toast.LENGTH_SHORT).show();

                i.putExtra("state_id", state_id);
                i.putExtra("city_id", city_id);
                i.putExtra("station_name", station_name);

                startActivity(i);
            }
        });

    }

    /**
     * Background Async Task to Load all tracks under one album
     * */
    class LoadTracks extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(TrackListActivity.this);
            pDialog.setMessage("Loading stations ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting tracks json and parsing
         * */
        /* protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post album id as GET parameter
            params.add(new BasicNameValuePair(TAG_ID, album_id));

            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(URL_ALBUMS, "GET",
                    params);

            // Check your log cat for JSON reponse
            Log.d("Track List JSON: ", json);

            try {
                JSONObject jObj = new JSONObject(json);
                if (jObj != null) {
                    String album_id = jObj.getString(TAG_ID);
                    album_name = jObj.getString(TAG_ALBUM);
                    albums = jObj.getJSONArray(TAG_SONGS);

                    if (albums != null) {
                        // looping through All songs
                        for (int i = 0; i < albums.length(); i++) {
                            JSONObject c = albums.getJSONObject(i);

                            // Storing each json item in variable
                            String song_id = c.getString(TAG_ID);
                            // track no - increment i value
                            String track_no = String.valueOf(i + 1);
                            String name = c.getString(TAG_NAME);
                            String duration = c.getString(TAG_DURATION);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put("album_id", album_id);
                            map.put(TAG_ID, song_id);
                            map.put("track_no", track_no + ".");
                            map.put(TAG_NAME, name);
                            map.put(TAG_DURATION, duration);

                            // adding HashList to ArrayList
                            tracksList.add(map);
                        }
                    } else {
                        Log.d("Albums: ", "null");
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        } */

        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // post album id as GET parameter
            // params.add(new BasicNameValuePair(TAG_ID, album_id));

            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(URL_ALBUMS+"/"+state_id+"/"+city_id, "GET",
                    params);

            // Check your log cat for JSON reponse
            Log.d("Track List JSON: ", json);

            try {
                albums = new JSONArray(json);
                     if (albums != null) {
                        // looping through All songs
                        for (int i = 0; i < albums.length(); i++) {
                            JSONObject c = albums.getJSONObject(i);

                            // Storing each json item in variable
                            String station_name = c.getString(STATION_NAME_RES);
                            // track no - increment i value
                            String track_no = String.valueOf(i + 1);
                            String station_display_name = c.getString(STATION_DISPLAY_NAME_RES);
                            city_name = c.getString(CITY_NAME_RES);
                            // String duration = c.getString(TAG_DURATION);

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put("state_id", state_id);
                            map.put("city_id", city_id);
                            map.put(STATION_NAME, station_name);
                            map.put("track_no", track_no + ".");
                            map.put(STATION_DISPLAY_NAME, station_display_name);
                            // map.put(TAG_DURATION, duration);

                            // adding HashList to ArrayList
                            tracksList.add(map);
                        }
                    } else {
                        Log.d("Albums: ", "null");
                    }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all tracks
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            TrackListActivity.this, tracksList,
                            R.layout.list_item_tracks, new String[] { "state_id", "city_id", STATION_NAME, "track_no", STATION_DISPLAY_NAME,}, new int[] {
                            R.id.state_id, R.id.city_id, R.id.station_name, R.id.track_no, R.id.station_display_name });
                    // updating listview
                    setListAdapter(adapter);

                    // Change Activity Title with Album name
                    setTitle(city_name);
                }
            });

        }

    }
}
