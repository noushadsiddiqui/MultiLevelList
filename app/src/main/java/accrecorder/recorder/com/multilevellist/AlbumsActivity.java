package accrecorder.recorder.com.multilevellist;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import accrecorder.recorder.com.helper.AlertDialogManager;
import accrecorder.recorder.com.helper.ConnectionDetector;
import accrecorder.recorder.com.helper.JSONParser;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AlbumsActivity extends ListActivity {
    // Connection detector
    ConnectionDetector cd;

    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();

    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jsonParser = new JSONParser();

    ArrayList<HashMap<String, String>> albumsList;

    // albums JSONArray
    JSONArray albums = null;

    // albums JSON url
    // private static final String URL_ALBUMS = "http://api.androidhive.info/songs/albums.php";
    // private static final String URL_ALBUMS = "http://ec2-52-26-104-86.us-west-2.compute.amazonaws.com:8090/cityStateList";
    private static final String URL_ALBUMS = "http://ec2-52-26-104-86.us-west-2.compute.amazonaws.com:8090/stations/";

    // ALL JSON node names
    private static final String TAG_ID = "id";
    private static final String TAG_NAME = "name";
    private static final String TAG_SONGS_COUNT = "songs_count";

    private static final String CITY_NAME = "city_name";
    private static final String STATE_NAME = "state_name";
    private static final String STATION_COUNT = "station_count";
    private static final String STATE_ID = "state_id";
    private static final String CITY_ID = "city_id";

    private static final String CITY_NAME_RES = "cityName";
    private static final String STATE_NAME_RES = "stateName";
    private static final String STATION_COUNT_RES = "station_count";
    private static final String STATE_ID_RES = "stateId";
    private static final String CITY_ID_RES = "cityId";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.albums_activity);

        cd = new ConnectionDetector(getApplicationContext());

        // Check for internet connection
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(AlbumsActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }

        // Hashmap for ListView
        albumsList = new ArrayList<HashMap<String, String>>();

        // Loading Albums JSON in Background Thread
        new LoadAlbums().execute();

        // get listview
        ListView lv = getListView();

        /**
         * Listview item click listener
         * TrackListActivity will be lauched by passing album id
         * */
        lv.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2,
                                    long arg3) {
                // on selecting a single album
                // TrackListActivity will be launched to show tracks inside the album
                Intent i = new Intent(getApplicationContext(), TrackListActivity.class);

                // send album id to tracklist activity to get list of songs under that album
                String state_id = ((TextView) view.findViewById(R.id.state_id)).getText().toString();
                String city_id = ((TextView) view.findViewById(R.id.city_id)).getText().toString();
                i.putExtra("state_id", state_id);
                i.putExtra("city_id", city_id);

                startActivity(i);
            }
        });
    }

    /**
     * Background Async Task to Load all Albums by making http request
     * */
    class LoadAlbums extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AlbumsActivity.this);
            pDialog.setMessage("Listing Cities ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting Albums JSON
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // getting JSON string from URL
            String json = jsonParser.makeHttpRequest(URL_ALBUMS, "GET",
                    params);

            // Check your log cat for JSON reponse
            Log.d("Albums JSON: ", "> " + json);

            try {
                albums = new JSONArray(json);
                HashMap<String, JSONObject> cityDetailMap = new HashMap<>();
                if (albums != null) {
                    for (int i = 0; i < albums.length(); i++) {
                        JSONObject c = albums.getJSONObject(i);

                        // Storing each json item values in variable
                        // String id = c.getString(TAG_ID);
                        // String name = c.getString(TAG_NAME);
                        // String songs_count = c.getString(TAG_SONGS_COUNT);

                        String state_id = c.getString(STATE_ID_RES);
                        String city_id = c.getString(CITY_ID_RES);
                        String state_name = c.getString(CITY_NAME_RES) +", "+c.getString(STATE_NAME_RES);
                        // String station_count = c.getString(STATION_COUNT);

                        if(cityDetailMap.containsKey(city_id)) {
                            cityDetailMap.put(city_id, cityDetailMap.get(city_id).put(STATION_COUNT, cityDetailMap.get(city_id).getInt(STATION_COUNT) + 1));
                        } else {
                            JSONObject cityInfo = new JSONObject();
                            cityInfo.put(STATE_ID, state_id);
                            cityInfo.put(CITY_ID, city_id);
                            cityInfo.put(STATE_NAME, state_name);
                            cityInfo.put(STATION_COUNT, 1);
                            cityDetailMap.put(city_id, cityInfo);
                        }


                    }
                    for(JSONObject cityInfo : cityDetailMap.values()){
                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(STATE_ID, cityInfo.getString(STATE_ID));
                        map.put(CITY_ID, cityInfo.getString(CITY_ID));
                        map.put(STATE_NAME, cityInfo.getString(STATE_NAME));
                        map.put(STATION_COUNT, cityInfo.getString(STATION_COUNT));

                        // adding HashList to ArrayList
                        albumsList.add(map);
                    }
                }else{
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
            // dismiss the dialog after getting all albums
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    Comparator<Map> ALPHABETICAL_ORDER1 = new Comparator<Map>() {
                        public int compare(Map object1, Map object2) {
                            int res = String.CASE_INSENSITIVE_ORDER.compare(((HashMap)object1).get(STATE_NAME).toString(), ((HashMap)object2).get(STATE_NAME).toString() );
                            return res;
                        }
                    };
                    Collections.sort(albumsList, ALPHABETICAL_ORDER1);

                    ListAdapter adapter = new SimpleAdapter(
                            AlbumsActivity.this, albumsList,
                            R.layout.list_item_albums, new String[] { STATE_ID, CITY_ID, STATE_NAME, STATION_COUNT }, new int[] {
                            R.id.state_id, R.id.city_id, R.id.state_name, R.id.station_count });



                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }
}