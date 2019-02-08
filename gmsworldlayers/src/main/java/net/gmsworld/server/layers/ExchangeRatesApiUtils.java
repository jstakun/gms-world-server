package net.gmsworld.server.layers;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import net.gmsworld.server.utils.HttpUtils;
import net.gmsworld.server.utils.memcache.CacheProvider;

public class ExchangeRatesApiUtils {

	private static final Logger logger = Logger.getLogger(ExchangeRatesApiUtils.class.getName());
	
	private static final String[] currencies = {"EUR","USD","GBP","PLN", "HUF","MXN","SEK","CHF","ILS","ZAR",
			"MYR","CAD","TRY","DKK","SGD","BRL","IDR","RON","KRW","NOK",
			"HKD","CZK","AUD","PHP","CNY","HRK","BGN","NZD","JPY","INR",
			"THB","RUB"};
	
	public static Map<String, Double> loadCurrency(String fromcc, CacheProvider cache) {
    	final String currencyUrl = "https://api.exchangeratesapi.io/latest?base=" + fromcc;
    	Map<String, Double> ratesMap = new HashMap<String, Double>();
		try {
			logger.log(Level.INFO, "Calling " + currencyUrl + "...");
			String resp = HttpUtils.processFileRequest(new URL(currencyUrl));							
			if (StringUtils.startsWith(resp, "{")) {
				JSONObject root = new JSONObject(resp);
				if (root.has("error")) {
					logger.log(Level.SEVERE, "Currency " + fromcc + " response error: " + root.getString("error"));
				} else {
					JSONObject rates = root.getJSONObject("rates");
					for (Iterator<String> keys=rates.keys();keys.hasNext();) {
						String key = keys.next();
						ratesMap.put(key, rates.getDouble(key));
					}
				}
				logger.log(Level.INFO, "Saving to cache entry CURRENCY_BASE_" + fromcc  + " ...");
				cache.put("CURRENCY_BASE_" + fromcc, ratesMap, 1);
			} else {
				logger.log(Level.WARNING, currencyUrl + " received following response from the server: " + resp);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
		return ratesMap;
    }
	
	public static void loadAllCurrencies(CacheProvider cache) {
		 for (String currency : currencies) {
    		 loadCurrency(currency, cache);
    	 }
	}
}
