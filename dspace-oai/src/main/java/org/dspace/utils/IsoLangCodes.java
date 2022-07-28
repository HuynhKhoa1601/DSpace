/* Created for LINDAT/CLARIN */
package org.dspace.utils;

import com.hp.hpl.jena.util.ResourceUtils;
import org.apache.solr.client.solrj.io.stream.JSONTupleStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class IsoLangCodes {

	/** log4j logger */
	private static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(IsoLangCodes.class);

	private static Map<String, String> isoLanguagesMap = null;
	static {
		getLangMap();
	}

	private static Map<String, String> getLangMap() {
		if (isoLanguagesMap == null) {
			synchronized (IsoLangCodes.class){
				isoLanguagesMap = buildMap();
			}
		}
		return isoLanguagesMap;
	}

	private static Map<String, String> buildMap(){
		Map<String, String> map = new HashMap<String, String>();
		System.out.println("Getting lang_codes.txt");
		final InputStream langCodesInputStream = Thread.currentThread()
				.getContextClassLoader().getResourceAsStream("lang_codes.txt");
		if (langCodesInputStream != null) {
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(langCodesInputStream, StandardCharsets.UTF_8))) {
				String line;
				while ((line = reader.readLine()) != null) {
					String[] splitted = line.split(":");
					map.put(splitted[1], splitted[0]);
				}
			} catch (IOException e) {
				log.error(e);
			}
		}
		return map;
	}

	public static String getLangForCode(String langCode) {
		return getLangMap().get(langCode);
	}

}
