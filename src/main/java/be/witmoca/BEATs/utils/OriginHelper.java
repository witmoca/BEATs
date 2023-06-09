package be.witmoca.BEATs.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

public class OriginHelper {
	private static final String[] DISPLAY_ORIGINS;
	private static final Map<String,String> ORIGIN_MAP; // (displayname of country),(2 letter country code as specified in Locale.getISOCountries())
	
	static {
		String countries[] = Locale.getISOCountries();
		List<String> origins = new ArrayList<String>();
		Map<String,String> originMap = new HashMap<String,String>();
		
		//Add unknown / empty
		origins.add(Lang.getUI("action.unknown"));
		originMap.put(Lang.getUI("action.unknown"), "");
		
		for(String countryCode : countries) {	
			String displayName = new Locale("",countryCode).getDisplayCountry(Lang.getLocale());
			origins.add(displayName);
			originMap.put(displayName, countryCode);
		}
		DISPLAY_ORIGINS = (origins.toArray(new String[0]));
		Arrays.sort(DISPLAY_ORIGINS,1, DISPLAY_ORIGINS.length);
		ORIGIN_MAP = originMap;
	}
	
	public static String[] getDisplayOriginList() {
		return DISPLAY_ORIGINS;
	}
	
	public static String getOriginCodeFromDisplayString(String displayCountry) {
		return ORIGIN_MAP.getOrDefault(displayCountry, "");
	}
	
	public static String getDisplayStringFromOriginCode(String countryCode) {
		for(Entry<String,String> entry : ORIGIN_MAP.entrySet()) {
			if (entry.getValue().compareTo(countryCode) == 0)
				return entry.getKey();
		}
		return Lang.getUI("action.unknown");
	}
}

