import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin and Jack on 12/24/14.
 */

public class YahooQuery {
    /**
     * Ok so to begin lets start making the link we'll be placing the request to.
     * Make a base URL instance variable ( dw about formatting intellij will fix all)
     *   - the URL will be BASE_URL + whatever the SQL query is
     * SimpleScrape code at /webStockScrape
     *
     select+Bid+from+yahoo.finance.quotes+where+symbol+in%28"MSFT"%29&env=http%3A%2F%2Fdatatables.org%2Falltables.env&format=json
     */
    public static final String BASE_URL = "http://query.yahooapis.com/v1/public/yql?q=";
    public static final String COLUMNS = "symbol, Ask, Bid, Change_PercentChange, DaysLow, DaysHigh, AverageDailyVolume, Open, PreviousClose, BookValue, DividendShare, EPSEstimateCurrentYear, EPSEstimateNextYear, YearLow, YearHigh";
    public static final String DATA_SOURCE = "&env=http%3A%2F%2Fdatatables.org%2Falltables.env";

    /**
     * Dont worry about URL encoding, use Parens and commas, there are Java objects that will convert that for you.
     * so we need to use simplescrape for the symbols part of the url? correct. dw about it yet though
     * Start writing, lets make a local URL in there, what comes at the beginning of all url queries?
     *  Now, all the symbols need to be in the format "MSFT","GOOG","APPL" like in the query above
     *  SELECT Bid FROM yahoo.finance.quotes WHERE symbol IN ("MSFT","GOOG","APPL"...)
     *  our arg is an array of [(APPL),(MSFT),(GOOG)...] We need a helper function to change:
     *   [(APPL),(MSFT),(GOOG)] => "APPL","MSFT","GOOG"
     *
     */
    public static JSONObject queryYahoo(List<String> stocks) throws IOException, JSONException {
        String sqlQuery = " SELECT " + COLUMNS + " FROM yahoo.finance.quotes WHERE symbol IN(" + queryString(stocks) + ") ";
        String fullUrlStr = BASE_URL + URLEncoder.encode(sqlQuery, "UTF-8") + DATA_SOURCE + "&format=json"; //from API description

        //good? So the below I didnt even know before SO (StackOverflow ill be using the abbrev from now on)
        // Apparently you can make a url object and place the request by saying url.openStream()
        //InputStream is like a scanner almost, but it gives you one character at a time,
        //  luckily JSONTokenizer can take an IS and make it to JSONObject
        // System.out.println("URL: " + fullUrlStr); //Prints the URL

        URL fullUrl = new URL(fullUrlStr);
        InputStream is = fullUrl.openStream();
        JSONTokener tok = new JSONTokener(is);
        JSONObject result = new JSONObject(tok);
        return result; // return the json object.
    }


    /*
     literally, its an ArrayList whose toString function ignores commas. Make sense?
  traverse the list, append one at a time with quotes followed by a comma?  yes
  Now think what we need to do, we have a list input. How to Traverse?: ___ yes for each?
  Ok so we are actually returning the stiring: "(GOOG)","(APPL)" not "GOOG","APPL"
  */
    public static String queryString(List<String> stocks){
        if (stocks.size() < 1)
            return "";
        StringBuilder sb = new StringBuilder();
        for (String stock : stocks) {
            //This checks if the first char is a paren, if it is, like (GOOG) it strips and returns GOOG
            //  If it isnt, it just returns the stock. This allows it to be used when the list format is just [GOOG,APPL]
            //  Potentially useful for scraping other websites in the future.
            stock = stock.charAt(0)=='(' ? stock.substring(1,stock.length()-1) : stock;
            sb.append("\"" + stock  + "\"");
            sb.append(",");
        }
        return sb.substring(0,sb.toString().length()-1);
    }

    /**
     * We'll leave this void for now, not sure what we really want it to do. I havent done this part yet.
     * Is this all making sense to you? Kind of cool how web applications work.
     *  Any app I make basically just takes a request then on the web backend it querys the DB, makes a JSONObject, then I just write to the screen (webpage) jsonObj.toString();
     *  That allows it to be digested by the app like we're doing here.
     *  The same way we are sayig jsonObj.get(), we can use jsonObj.put() when making the json, then call toString() if making a webapp backend
     */
    public static void processJSON(JSONObject json) throws JSONException {
        JSONObject query = json.getJSONObject("query"); //Lets ignore it if count is 1.
        JSONObject results = query.getJSONObject("results");
        int count = query.getInt("count"); // That is how we get from JSON back to a Java object. Also getString(String key);
        // if count is 1 or less, just return. Who cares about 1 or 0 result anyway.
        if (count <= 1) {
            return;
        }
        //Count is more than 1 - now what? get the quote
        JSONArray quote = results.getJSONArray("quote");

        //JSONArray is not itearable with forEach.. so for loop, each element is JSONObject in this case.
        //Lets simply print the stock symbol and change_percentchange. Get change as a string since its formatted weird.
        // JSONObject has getInt(key); getDouble(key); getString(key); getBoolean(key) where key is String identifier
        for(int i=0; i<quote.length(); i++){ // JSONArrays have getJSONObject(int index) function
            JSONObject stock = quote.getJSONObject(i); // get index not string since dealing with array now good.
            String symbol = stock.getString("symbol");
            String pChange = stock.getString("Change_PercentChange"); //good, copy paste the key from below to avoid spelling errors
            System.out.println(symbol + ":  \t" + pChange);
        }


    }

    /**
     requires a list to call the static function
     Any luck?
     */
    public static void main(String[] args) throws IOException, JSONException {
        //Make sample data
        List<String> stocks = new ArrayList<String>();
        stocks.add("(GOOG)");
        stocks.add("(MSFT)");
        JSONObject test = YahooQuery.queryYahoo(stocks);//Make sure this function returns the JSONObject ^^  scrolll up to queryYahoo
        processJSON(test);
    } 
   /* 
   So I copied that JSON they gave and ran it through http://jsonformatter.curiousconcept.com (Google search for format JSON)
   This is what the object really is, pretty cool huh?
   So note this: it gives you a count (can be used for for loop through data)
   Note that anything surrounded in {} is a JSONObject
   Note that anything in [] is a JSONArray
     to digest we'll say things like jsonObject.getJSONObject("query"); 
     make sense? since the value associated with key "Query" is a json object (surrounded in {} )
     then when we have that well getJSONObject on that one and get "results"
       Then when we have that we'll do what to get the quotes? getJOSONArray? good. 
       And thats why JSON is such a beautiful thing. It's that easy.
   
  {
     "query":{
        "count":2,
        "created":"2014-12-29T02:18:56Z",
        "lang":"en-us",
        "results":{
           "quote":[
              {
                 "symbol":"GOOG",
                 "Bid":"533.25",
                 "BookValue":"145.685",
                 "Change_PercentChange":"+5.26 - +0.99%",
                 "DividendShare":"0.00",
                 "EPSEstimateCurrentYear":"25.65",
                 "EPSEstimateNextYear":"28.99",
                 "DaysLow":"527.31",
                 "DaysHigh":"534.25",
                 "YearLow":"489.00",
                 "YearHigh":"604.83",
                 "Open":"528.77",
                 "PreviousClose":"528.77",
                 "Ask":null,
                 "AverageDailyVolume":"1977430"
              },
              {
                 "symbol":"MSFT",
                 "Bid":"47.81",
                 "BookValue":"10.923",
                 "Change_PercentChange":"-0.26 - -0.54%",
                 "DividendShare":"1.15",
                 "EPSEstimateCurrentYear":"2.67",
                 "EPSEstimateNextYear":"3.14",
                 "DaysLow":"47.82",
                 "DaysHigh":"48.41",
                 "YearLow":"34.63",
                 "YearHigh":"50.05",
                 "Open":"48.38",
                 "PreviousClose":"48.14",
                 "Ask":null,
                 "AverageDailyVolume":"31899800"
              }
           ]
        }
     }
  }
  
  {
     "query":{
        "count":1,
        "created":"2014-12-29T02:15:14Z",
        "lang":"en-us",
        "results":{
           "quote":{
              "symbol":"GOOG",
              "Bid":"533.25",
              "BookValue":"145.685",
              "Change_PercentChange":"+5.26 - +0.99%",
              "DividendShare":"0.00",
              "EPSEstimateCurrentYear":"25.65",
              "EPSEstimateNextYear":"28.99",
              "DaysLow":"527.31",
              "DaysHigh":"534.25",
              "YearLow":"489.00",
              "YearHigh":"604.83",
              "Open":"528.77",
              "PreviousClose":"528.77",
              "Ask":null,
              "AverageDailyVolume":"1977430"
           }
        }
     } 
  }
something to notice: when there is only one element in results (count == 1) the value of "quote" is actually a JSONObject not array.
that could cause an app to crash if not caught. probably will never happen in reality though (^up to processing now)
*/

}