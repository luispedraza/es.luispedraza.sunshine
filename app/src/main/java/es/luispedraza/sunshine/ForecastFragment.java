package es.luispedraza.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayAdapter<String> mForecastAdapter;


    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Report the Fragment has menu options
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Refresh data every time this fragment is displayed
        refreshData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Sample data
//        String[] sampleData = {"item #1", "item #2", "item #3", "item #4", "item #5", "#item #6"};
//        ArrayList<String> sampleArrayList = new ArrayList<String>(Arrays.asList(sampleData));

        // Create the data adapter: ArrayAdapter
        mForecastAdapter = new ArrayAdapter<String>(//getActivity().getBaseContext(),
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());
        // Now, bind the adapter to the View:
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        // Click listeners
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /* Showing a toast
                // alternative 1:
                // String forecastText = ((TextView) view.findViewById(R.id.list_item_forecast_textview)).getText().toString();
                // alternative 2:
                String forecastText = mForecastAdapter.getItem(position);
                // Now, display information
                Toast.makeText(getActivity(), forecastText, Toast.LENGTH_SHORT).show();
                */

                /* Showing a new activity */
                String forecastText = mForecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, forecastText);
                startActivity(intent);
            }
        });



        return rootView;
    }


    /**
     * Obtain new data from server
     */
    private void refreshData() {
        // Obtain location preference:
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String postalCode = preferences.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default)
        );

        // Obtain new data:
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        fetchWeatherTask.execute(postalCode);
    }

    void updateUI(String[] newData) {
        if (newData != null) {
            mForecastAdapter.clear();
            for (String dayForecast : newData) {
                mForecastAdapter.add(dayForecast);
            }
            // Alternative (more efficient in Honeycomb and above)
            // mForecastAdapter.addAll(newData);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    /* Menu item has been selected */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            Toast.makeText(this.getActivity(), this.getActivity().getString(R.string.refreshing_toast_message), Toast.LENGTH_SHORT).show();
            refreshData();
        }

        return super.onOptionsItemSelected(item);
    }


    // Async activity for downloading new data
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        String getWeatherForecast(String postalCode) {
            // Som constants:
            // API URL for the OpenWeatherMap query. More info: http://openweathermap.org/API#forecast
            final String API_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";
            // final String API_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
            final String API_LOCATION_PARAM = "q";  // values: postal code
            final String API_FORMAT_PARAM = "mode"; // values: json, xml...
            final String API_UNITS_PARAM = "units"; // values: imperial, metric
            final String API_DAYS_PARAM = "cnt";    // number of days for forecast

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;

            try {

                Uri builtUri = Uri.parse(API_BASE_URL).buildUpon()
                        .appendQueryParameter(API_LOCATION_PARAM, postalCode)
                        .appendQueryParameter(API_FORMAT_PARAM, "json")
                        .appendQueryParameter(API_UNITS_PARAM, "metric")
                        .appendQueryParameter(API_DAYS_PARAM, "7")
                        .build();

                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built Uri " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    jsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Adding a newline does make debugging a *lot* easier
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    jsonStr = null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in parsing
                jsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return jsonStr;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // If there is no zip code, nothing to be done:
            if (params.length == 0) {
                return null;
            }
            String forecastJsonStr = getWeatherForecast(params[0]);

            // Get units from settings:
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String prefUnits = preferences.getString(
                    getString(R.string.pref_units_key),
                    getString(R.string.pref_units_default)
            );
            Log.d(LOG_TAG, "Selected units: " + prefUnits);
            OpenWeatherMapParser parser = new OpenWeatherMapParser(forecastJsonStr, prefUnits);
            return parser.getResult();
        }

        @Override
        protected void onPostExecute(String[] forecastArray) {
            // Now, update the interface
            updateUI(forecastArray);
        }
    }
}
