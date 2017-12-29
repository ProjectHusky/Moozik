package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class GoogleMapsParser {

    // Google Maps API developer key. Leave here for now.
    private static final String KEY = "";
    // The units of measurement. Only support imperial for now.
    private static final String UNIT = "imperial";

    /**
     * Returns the Google Maps API response as a string.
     * @param requestURL The URL of the API request.
     * @return The response of the API.
     * @throws IOException
     */
    private static String getResponse (String requestURL) throws IOException {

        BufferedReader reader = null;
        try {
            // Open a reader using the request URL and attempt to read it line by line to construct
            // the API JSON response.
            URL url = new URL(requestURL);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            StringBuilder responseStringBuilder = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                responseStringBuilder.append(line);
                responseStringBuilder.append('\n');
            }


            return responseStringBuilder.toString();
        } finally {
            // Close the reader at the end of everything.
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * Parses the Google Maps API response and returns them as a 2D array. The first array is the
     * information regarding the distance of the trip and the second array is the information
     * regarding the duration of the trip.
     * @param response The response of the API as a string.
     * @return A 2D containing the parsed information.
     */
    private static String[][] parseResponse (String response) {
        // Split the response into lines.
        String[] responseLines = response.split("\n");

        // Parse the line containing the distance of the trip.
        String[] distanceTokens = responseLines[8].substring(28, responseLines[8].indexOf("\","))
                .split(" ");

        // Parse the line containg the duration of the trip.
        String[] rawDuration = responseLines[12].substring(28, responseLines[12].indexOf("\","))
                .split(" ");

        // Return them both as a 2D array.
        String[][] parsedData = {distanceTokens, rawDuration};

        return parsedData;
    }

    /**
     * Formats the raw distance data, converting it into the standard form of miles. The distance
     * data is defined by 2 values, the unit of measurement and how many of those units there are.
     * For example if the raw of measurement was "52 mi" then the value would be 52 and the unit
     * would be mi(miles). The raw measurement is converted to miles, which will be our standard.
     * @param value The value of the distance measurement.
     * @param unit The unit of the distance measurement.
     * @return The unit measurement converted to miles.
     */
    private static double formatDistance (double value, String unit) {
        return unit.equals("mi") ? value : value / 5280.0;
    }

    /**
     * Formats the raw duration distance, converting it into the standard form of units. The
     * duration data is defined as an array of strings, where odd indices are a unit of time and
     * the index before those are the value of that unit of time. For example an example might be
     * ["1", "day" , "18", "hours"], which corresponds to 1 day and 18 hours. The data is then
     * converted to a standardized form of minutes.
     * @param timeUnits An array containing all the units of time.
     * @return The travel time converted to minutes.
     */
    private static double formatDuration (String[] timeUnits) {
        double duration = 0;

        // Go through all the units, convert and then add them to the counter.
        for (int x = 1; x < timeUnits.length; x += 2) {
            if (timeUnits[x].equals("min")) {
                duration += Double.valueOf(timeUnits[x - 1]);
            } else if (timeUnits[x].equals("hours") || timeUnits[x].equals("hour")) {
                duration += (Double.valueOf(timeUnits[x - 1]) * 60);
            } else if (timeUnits[x].equals("day") || timeUnits[x].equals("days")) {
                duration += (Double.valueOf(timeUnits[x - 1])) * 1440;
            } else if (timeUnits[x].equals("day") || timeUnits[x].equals("days")) {
                duration += (Double.valueOf(timeUnits[x - 1])) * 10080;
            }
        }

        return duration;
    }

    /**
     * Formats the raw parsed data into more simplistic, standardized forms and returns both of the
     * data points as an array.
     * @param rawInput The raw data that is used in the formatting. The first parameter
     *                 corresponds to the travel distance and the second corresponds to the travel
     *                time.
     * @return The formatted data as a double array.
     */
    private static double[] formatData (String[][] rawInput) {
        // Get the tokens.
        String[] distanceTokens = rawInput[0];
        String[] rawDuration = rawInput[1];

        // Use the helper methods to convert the tokens to the formatted doubles.
        double distance = formatDistance(Double.valueOf(distanceTokens[0].
                replace(",", "")), distanceTokens[1]);
        double duration = formatDuration(rawDuration);

        // Return both of them as an array.
        double[] formattedData = {distance, duration};

        return formattedData;
    }

    /**
     * This method is used to calculate the travel information regarding the travel distance and
     * travel time of a roadtrip. The user inputs the origin location and the destination location
     * and is given a double array containing the travel distance and travel time in that order.
     * @param origin The origin for the trip.
     * @param destination The destination of the trip.
     * @return An array containing both the travel distance and travel time.
     * @throws IOException
     */
    public static double[] getTravelInformation (String origin, String destination)
            throws IOException{
        // Replace spaces with '+' in both parameters.
        origin = origin.replace(" ", "+");
        destination = destination.replace(" ", "+");

        // Setup the URL string.
        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?units=";
        // Add the correct distance unit measurement.
        url += UNIT;
        // Add the origin point.
        url += "&origins=" + origin;
        // Add the destination point.
        url += "&destinations=" + destination;
        // Add the Google Maps developer key.
        url += "&key=" + KEY;

        // Get the response from the Google Maps API.
        String response = getResponse(url);
        // Parse the response into the raw String array format.
        String[][] parsedResponse = parseResponse(response);
        // Format and standardize the data into a double array and return that data.
        double[] formattedData = formatData(parsedResponse);

        return formattedData;
    }
}
