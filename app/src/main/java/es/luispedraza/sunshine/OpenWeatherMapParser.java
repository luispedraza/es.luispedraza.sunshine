package es.luispedraza.sunshine;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by luis on 27/1/15.
 *
 * This class parses raw JSON data obtained from OpenWeatherMap API
 */
public class OpenWeatherMapParser {

    String LOG_TAG = OpenWeatherMapParser.class.getSimpleName();
    String[] result;
    JSONObject forecastJson;
    private String units;

    public OpenWeatherMapParser(String rawJson, String units) {
        // Units for temperature, etc.
        this.units = units;
        try {
            forecastJson = new JSONObject(rawJson);
            getWeatherDataFromJson();
        } catch (final JSONException e) {
            Log.v(LOG_TAG, e.getMessage(), e);
        };
    }

    /** The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    /** Convert temperature to correct units
     *
     * @param temperature
     * @return
     */
    private long convertTemperature(double temperature) {
        return Math.round(units.equals("metric") ? temperature : (temperature * (9/5) + 32));
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = convertTemperature(high);
        long roundedLow = convertTemperature(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /** Retreive parse result */
    public String[] getResult() {
        return result;
    }


    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getWeatherDataFromJson() throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";

        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        result = new String[weatherArray.length()];

        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime = dayForecast.getLong(OWM_DATETIME);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            result[i] = day + " - " + description + " - " + highAndLow;

            Log.v(LOG_TAG, result[i]);
        }
    }
}
