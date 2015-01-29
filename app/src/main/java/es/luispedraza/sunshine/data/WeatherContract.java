package es.luispedraza.sunshine.data;

import android.provider.BaseColumns;

/**
 * Created by luis on 29/1/15.
 *
 * This class defines tables and column names for this App database
 */
public class WeatherContract {

    /** Table contents for the weather table
     *
     */
    public static final class WeatherEntry implements BaseColumns {
        // The table name:
        public static final String TABLE_NAME = "weather";

        // Foreign key (location table)
        public static final String COLUMN_LOC_KEY = "location_id";
        // Date, stored as text YYYY-MM-DD
        public static final String COLUMN_DATETEXT = "date";
        // Weather ID returned by API, defines icon to be used:
        public static final String COLUMN_WEATHER_ID = "weather_id";
        // Short description provided by API:
        public static final String COLUMN_SHORT_DESCRIPTION = "short_description";
        // Min and Max temperatures, stored as float in Metric Units:
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";
        // Humidity tored as percentage in float format:
        public static final String COLUMN_HUMIDITY = "humidity";
        // Pressure stored as float
        public static final String COLUMN_PRESSURE = "pressure";
        // Windspeed stored as float in mph
        public static final String COLUMN_WIND_SPEED = "wind_speed";
        // Wind direction stored as degrees (0 is north, 180 is south) as float
        public static final String COLUMN_WIND_DEG = "wind_deg";
    }
}
