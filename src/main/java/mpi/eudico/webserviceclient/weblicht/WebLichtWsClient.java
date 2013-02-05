package mpi.eudico.webserviceclient.weblicht;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mpi.eudico.server.corpora.util.ServerLogger;
import mpi.eudico.webserviceclient.WsClientRest;

/**
 * A class to access the WebLicht services.
 *  
 * @author Han Sloetjes
 */
public class WebLichtWsClient {
	public static String baseUrl = "http://weblicht.sfs.uni-tuebingen.de/rws/";
	// url for converting plain text to tcf
	private String convertUrl = "convert-all/qp";
	
	private WsClientRest wsClient;

	/**
	 * No arg constructor
	 */
	public WebLichtWsClient() {
		super();
		wsClient = new WsClientRest();
	}
	
	/**
	 * Converts plain text to TCF.
	 * 
	 * @param text the plain text
	 * @return the TCF as string
	 */
	public String convertPlainText(String text) {
		if (text == null || text.length() == 0) {
			return text;
		}
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("informat", "plaintext");
		params.put("outformat", "tcf04");
		params.put("language", "de");
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("Accept", "text/xml");
		properties.put("User-Agent", "ELAN");
		properties.put("Connection", "Keep-Alive");
		
		try {
			String result = wsClient.callServicePostMethodWithString(baseUrl + convertUrl, params, properties, 
				text, null, null, 0, 0);
			//System.out.println(result);
			return result;
		} catch (IOException ioe) {
			// log 
			ServerLogger.LOG.warning("Call failed: " + ioe.getMessage());
		}
		
		return null;
	}
	
	/**
	 * Calls WebLicht components that take TCF as input and returns TCF. 
	 * 
	 * @param toolUrl the tool specific part of the url
	 * @param tcfString the input tcf string
	 * 
	 * @return the returned content
	 */
	public String callWithTCF(String toolUrl, String tcfString) {
		if (toolUrl == null || tcfString == null) {
			ServerLogger.LOG.warning("No url or input specified.");
			return null;
		}
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("Accept", "text/xml");
		properties.put("User-Agent", "ELAN");
		properties.put("Connection", "Keep-Alive");
		
		try {
			String result = wsClient.callServicePostMethodWithString(baseUrl + toolUrl, 
					null, properties, 
				tcfString, null, null, 0, 0);
			//System.out.println(result);
			return result;
		} catch (IOException ioe) {
			// log 
			ServerLogger.LOG.warning("Call failed: " + ioe.getMessage());
		}
		
		return null;
	}
	
}
