package net.teknoraver.ryanair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;

public final class Ryanair {
	private static final HashMap<FlightRequest, FlightResult[]> results = new HashMap<FlightRequest, FlightResult[]>();
	private static DefaultHttpClient httpclient;
	private static final Pattern flightp = Pattern.compile("value=\\\"([\\w~\\| /:]+)\\\" onclick");

	private static final String agents[] = new String[] {
		// IExplorer 8
		"Mozilla/5.0 (Windows; U; MSIE 9.0; Windows NT 6.0; en-US)",
		"Mozilla/5.0 (Windows; U; MSIE 8.0; Windows NT 5.2; Trident/4.0)",
		"Mozilla/5.0 (Windows; U; MSIE 8.0; Windows NT 5.1; Trident/4.0)",
		"Mozilla/5.0 (Windows; U; MSIE 8.0; Windows NT 5.0; Trident/4.0)",
		// IExplorer 7
		"Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 5.2; en-US)",
		"Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 5.1; en-US)",
		"Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 5.0; en-US)",
		// IExplorer 6
		"Mozilla/4.0 (Windows; MSIE 6.1; Windows XP)",
		"Mozilla/4.0 (Windows; MSIE 6.1; Windows XP)",
		"Mozilla/4.0 (Windows; MSIE 6.1; Windows XP)",
		// Firefox
		"Mozilla/5.0 (Windows; U; Windows NT 5.2; en-GB; rv:1.9.2.9) Gecko/20100824 Firefox/3.6.9",
		"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401 Firefox/3.8",
		"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.4) Gecko/20100611 Firefox/3.6.4",
		// Safari
		"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/534.16+ (KHTML, like Gecko) Version/5.0.3 Safari/533.19.4",
		"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_5; en-gb) AppleWebKit/534.15+ (KHTML, like Gecko) Version/5.0.3 Safari/533.19.4",
		"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_8; en-us) AppleWebKit/533.18.1 (KHTML, like Gecko) Version/5.0.2 Safari/533.18.5",
		// Chrome
		"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.19 Safari/534.13",
		"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.0 Safari/534.13",
		"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) Chrome/9.0.597.15 Safari/534.13",
		"Mozilla/5.0 (X11; U; Windows NT 6; en-US) AppleWebKit/534.12 (KHTML, like Gecko) Chrome/9.0.587.0 Safari/534.12",
		// Chromium
		"Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/534.20 (KHTML, like Gecko) Ubuntu/10.10 Chromium/11.0.669.0 Chrome/11.0.669.0 Safari/534.20",
		// iPhone/iPad
		"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_0 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5 Mobile/8A293 Safari/6531.22.7",
		"Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543 Safari/419.3",
		"Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10",
		// BB
		"BlackBerry9700/5.0.0.862 Profile/MIDP-2.1 Configuration/CLDC-1.1 VendorID/331",
		// Android
		"Mozilla/5.0 (Linux; U; Android 2.2; en-us; SCH-I800 Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
		"Your User Agent: Mozilla/5.0 (Linux; U; Android 2.2.2; it-it; Nexus One Build/FRG83G) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1",
		// Nokia
		"NokiaN97i/SymbianOS/9.1 Series60/3.0"
	};

	// the form URL is http://www.ryanair.com/en/booking/form
	private static final String hostname = "www.bookryanair.com";
	private static final String bookpage = "/SkySales/FRSearch.aspx";
	private static final String selectpage = "/skysales/FRSelect.aspx";
	private static final String farespage = "/SkySales/FRTaxAndFeeInclusiveDisplay-resource.aspx?flightKeys=";

	private static final String bookpageparams =
		"__EVENTTARGET=&" +
		"AvailabilitySearchInputFRSearchView$RadioButtonMarketStructure=OneWay&" +
		"AvailabilitySearchInputFRSearchView$DropDownListPassengerType_ADT=1&" +
		"AvailabilitySearchInputFRSearchView$ButtonSubmit=&";

	private static final String FROM = "AvailabilitySearchInputFRSearchView$DropDownListMarketOrigin1";
	private static final String TO = "AvailabilitySearchInputFRSearchView$DropDownListMarketDestination1";
	private static final String DAY = "AvailabilitySearchInputFRSearchView$DropDownListMarketDay1";
	private static final String MONTH = "AvailabilitySearchInputFRSearchView$DropDownListMarketMonth1";

	private static void setup() throws IOException {
		System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

		httpclient = new DefaultHttpClient();
		HttpParams params = httpclient.getParams();

		params.setParameter(ClientPNames.DEFAULT_HOST, new HttpHost(hostname));
		params.setParameter(CoreProtocolPNames.USER_AGENT, agents[(int)(Math.random() * agents.length)]);
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
//		HttpConnectionParams.setConnectionTimeout(params, 5000);
//		HttpConnectionParams.setSoTimeout(params, 5000);

		// get Cookie
		HttpResponse response = httpclient.execute(new HttpGet(bookpage));
		if(response.getStatusLine().getStatusCode() != 200)
			throw new IOException("Got bad status: " + response.getStatusLine().getStatusCode());
		response.getEntity().consumeContent();

		// add the cookie
		final BasicClientCookie cookie = new BasicClientCookie("acceptTerms", null);
		cookie.setDomain(hostname);
		httpclient.getCookieStore().addCookie(cookie);
	}

	public static FlightResult[] get(FlightRequest req) throws Exception {
		if(results.containsKey(req)) {
			FlightResult[] oldreq = results.get(req);
			if(oldreq.length > 0 && new Date().getTime() - oldreq[0].timestamp.getTime() < 1000 * 60 * 10)
				return results.get(req);
			else
				results.remove(req);
		}

		if(httpclient == null)
			setup();

		try {
			return get2(req);
		} catch (Exception e) {
			// sometimes the request fails simply because the session expires
			// in this case we just get the cookie again
			httpclient.getConnectionManager().shutdown();
			setup();
			return get2(req);
		}
	}

	public static FlightResult[] get2(FlightRequest req) throws Exception {
		final String dmy[] = req.date.split("/");
		final String day = dmy[0];
		final String month = dmy[2] + "-" + dmy[1];

		// POST search
		HttpPost httppost = new HttpPost(bookpage);

		final String data =	bookpageparams +
					(FROM + '=') + req.from + ('&' +
					TO + '=') + req.to + ('&' +
					DAY + '=') + day + ('&' +
					MONTH + '=') + month;

		final StringEntity reqEntity = new StringEntity(data);
		reqEntity.setContentType("application/x-www-form-urlencoded");
		httppost.setEntity(reqEntity);

		HttpResponse response = httpclient.execute(httppost);

		if(response.getStatusLine().getStatusCode() != 200)
			throw new IOException("Got bad status: " + response.getStatusLine().getStatusCode());
		response.getEntity().consumeContent();

		// get flights list
		httppost = new HttpPost(selectpage);
		httppost.setEntity(reqEntity);		
		response = httpclient.execute(httppost);

		if(response.getStatusLine().getStatusCode() != 200)
			throw new IOException("Got bad status: " + response.getStatusLine().getStatusCode());

		final HttpEntity entity = response.getEntity();
		BufferedReader in = new BufferedReader(new InputStreamReader(entity.getContent()));
		String line;
		ArrayList<String> flights = new ArrayList<String>(); 
		while((line = in.readLine()) != null) {
			Matcher m = flightp.matcher(line);
			while(m.find())
				flights.add(m.group(1));
		}
		entity.consumeContent();

		// get single flights
		FlightResult res[] = new FlightResult[flights.size()];
		for(int i = 0; i < res.length; i++) {
//			System.out.println("Processing: " + flights.get(i));
			response = httpclient.execute(new HttpGet(farespage + URLEncoder.encode(flights.get(i), "UTF-8")));
			if(response.getStatusLine().getStatusCode() != 200)
				throw new IOException("Got bad status: " + response.getStatusLine().getStatusCode());
			res[i] = new FlightResult(response.getEntity().getContent());
//			System.out.println("Found: " + res[i]);
			response.getEntity().consumeContent();
		}
		results.put(req, res);
		return res;
	}
}
