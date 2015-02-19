import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by GleasonK on 12/24/14.
 */

public class SimpleScrape {
    public static final String USER_AGENT = "Chrome";

    /**
     * Add a header, a header is like meta data to the request. Some sites prevent robots, so setting user-agent
     *  is a workaround. It tricks the browser into thinking the request is being made by someone using Chrome.
     * HttpResponse is a Java data type. There are multiple ways to extract a string from it.
     *  The string will plainly be the HTML from the page returned, this is what we parse.
     *  There are many ways to extract this string, such as input stream, but I chose to use EntityUtils
     *  For other methods use google and stack overflow.
     * Response codes are a HTTP (Web) way to tell you how the request went.
     *  (You may want to catch these with if statements or a switch statement to avoid errors in code)
     *  200 means everything went well. All codes in the 200 range are usually good.
     *  404 is page not found, probably use if statement to ignore requests that return this
     *  500 is Internal Server error. Ignore these too, their fault not yours usually.
     */
    public String makeRequest(String url) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet(url);
        request.addHeader("User-Agent", USER_AGENT);
        HttpResponse response = httpClient.execute(request);
//        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());
        String responseString = EntityUtils.toString(response.getEntity());
        return responseString;
    }

    public String html2text(String html) {
        return Jsoup.parse(html).text();
    }

    /**
     * I made scrapeStocks a function instead of tossing it all in main.
     * ok go into method....
     */
    public static void scrapeStocks(){
        /**
         * A request is "asking the web for something." Almost literally a request. For example,
         *  https://www.google.com/?q=stocks requesting "stocks" from google search.
         *  q in this case stands for query, so google.com/?q=stocks is querying, or requesting, stocks from google.
         */

        String url = "http://blogs.barrons.com/stockstowatchtoday/";
        SimpleScrape simpleScrape = new SimpleScrape();

        /**
         * A response is all the HTML that comes back from placing a request to the web
         */
        String response = "";
        try {
            response = simpleScrape.makeRequest(url);
            //System.out.println(response); //Here's all the text. Now here we work magic.

            /**
             * This regular expression will find all few CAPITAL LETTER ABBREVS surrounded by parentheses.
             *  I use Jsoup to take all HTML tags out of the page, meaning just text
             *  Then I use that regular expression to find all of the occurrences of (ABBREV) regex.
             *  I add those to list then print it out. Those are the popular tags for the day.
             */
            String regex = "\\([A-Z]+\\)";
            String respText = simpleScrape.html2text(response);
            Pattern stockPattern = Pattern.compile(regex);
            Matcher matcher = stockPattern.matcher(respText);
            List<String> result = new ArrayList<String>();
            while (matcher.find()) {
                result.add(matcher.group(0));
            }
//            System.out.println("\nStocks Today: " + result);
            try {
                JSONObject json = YahooQuery.queryYahoo(result);
//                System.out.println(json.toString());
                YahooQuery.processJSON(json);  // simply added this line. Processes the massive array and prints it all.
            } catch (JSONException e){ e.printStackTrace(); }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * DAY 1: [(XLE), (GILD), (AAL), (ALGT), (TSLA), (KKR), (MNST), (STZ), (WWAV), (TXRH), (AMZN), (PLCE), (FL), (FINL),
     *         (BWS), (SKUL), (ARO), (BURL), (ROST), (KMB), (AVP), (PG), (COTY), (CL), (TUP), (SNN), (SYK), (WAG), (ABBV), (ESRX),
     *         (GILD), (HCV), (GILD), (WAG), (CVS), (V), (GS), (CHK), (WAG), (GMCR), (VNDA), (FINL), (FL), (GILD), (TSLA), (SCS),
     *         (CHK), (SWN), (AEIS), (TSLA)]
     * @param args
     */
    public static void main(String[] args) {
        scrapeStocks();
    }

}
