package mpi.eudico.p2p;

import java.util.ResourceBundle;

/**
 * @author Han Sloetjes
 */
public class P2PLocale {
//	private static ResourceBundle bundle;

//	static {
		//bundle = ResourceBundle.getBundle("mpi.eudico.p2p.p2p");
//	}

	/**
	 * Returns the resource value for the specified key.
	 * @param key the key
	 * @return the value corresponding to the key
	 */
	public static String getString(String key) {
		return "knoep";
/*		try {
			//return bundle.getString(key);
		} catch (Exception ex) {
			return "";
		}*/
	}
}
