package mpi.eudico.client.im;

import guk.im.GateIM;
import guk.im.GateIMDescriptor;

import java.awt.AWTException;
import java.awt.Component;
import java.util.HashSet;
import java.util.Locale;

import mpi.eudico.client.im.spi.lookup.Lookup;
import mpi.eudico.client.im.spi.lookup.LookupDescriptor;
/**
   <p>
   This is a MPI-PL utility for the input methods of Java1.3 (java.awt.im.spi).
   Clients of this Class are editors.
   An editor will either let the user select the language via a menu or
   will use other information to set the language.
   </p>
   
   <p>
   The class encapsulates the GUK input method and the input methods written 
   at the MPI-PL. A client will not notice which input method is used.
   </p>
   At this point in time, some languages require a special font.
   Therefore, the method setLanguage() has to be used, which sets the font.   
*/
public class ImUtil {

	// for testing if the requested locale exists.
	private static final HashSet allLocales = new HashSet();
	private static final int unicodeStandardFontSize = 20;
	private static final int unicodeStandardFontStyle = java.awt.Font.PLAIN;
	public static boolean showKeyboard = true;
	/* HS 01-2004 use all available input methods */
	// private static final ArrayList localesList = new ArrayList(30);

	static {
		// read all locales
		try {
			GateIMDescriptor gukDescriptor = new GateIMDescriptor();
			Locale[] gukLocales = gukDescriptor.getAvailableLocales();
			for (int i = 0; i < gukLocales.length; i++) {
				//localesList.add(gukLocales[i]);
				allLocales.add(gukLocales[i].toString());
			}
		}
		catch (AWTException ae) { /* nop */
		}
		LookupDescriptor lupDescriptor = new LookupDescriptor();
		Locale[] lupLocales = lupDescriptor.getAvailableLocales();
		for (int i = 0; i < lupLocales.length; i++) {
			//localesList.add(lupLocales[i]);
			allLocales.add(lupLocales[i].toString());
		}
	}

	/**
	   The EUDICO set of languages, implemented as Java Locales.
	   Clients are allowed to use individual Locales, and the web lexicon client actually
	   does. Other EUDICO code should refer to the getLanguages() function below, which 
	   defines an ordered set of these Locales.
	*/

	/*
	  Locales defined in mpi.eudico.client.im.spi.lookup.
	  The Chinese Locales are taken from Sun. 
	*/
	public static final Locale IPA96_RTR = Lookup.IPA96_RTR;

	/*
	  The following locales are implemented in GUK.
	  GUK loads locales only at startup. 
	  At compile-time, there are no constants I could refer to.
	  ImUtil assumes that GUK supports a specific locale.
	  I took the locale variant from 
	  GUK version 1.1, file guk/resources/guk/im/data/im.list
	  If a new version of GUK is used, the Locale has to be verified.
	  The Locale must match character per character, in all 3 arguments.
	*/
	public static final Locale IPA96_SAMPA = new Locale("IPA-96", "", "SAMPA");
	public static final Locale Cyrillic = new Locale("RU", "", "YAWERTY (Phonetic)");
	public static final Locale Arabic1 = new Locale("AR", "", "MLT Arabic");
	public static final Locale Arabic2 = new Locale("AR", "", "Windows");
	public static final Locale Hebrew = new Locale("HE", "", "Standard");
	public static final Locale GEORGIAN_HEI = new Locale("ka", "", "Heinecke");
	public static final Locale GEORGIAN_IMNA = new Locale("ka", "", "Imnaishvili Arrangement");
	public static final Locale GEORGIAN_MLT = new Locale("ka", "", "MLT");
	public static final Locale KOREAN = new Locale("ko", "", "Standard Hangul");
	public static final Locale TURKISH = new Locale("tr", "", "Standard");
	public static final Locale IPA_EXT_VK = new Locale("ipa-ext", "", "IPA Extended");
	public static final Locale ENGLISH = new Locale("en", "", "ASCII");
	

	/**
	   An ImUtil client requests a list of supported locales with getLanguages().
	   The order in which the user sees the locales is defined here.
	   @param visual the visual component of the editor.
	   @return an array of languages as {@see java.util.Locale}.
	 */
	public static final Locale[] getLanguages(Component component) {
		Locale defaultLocale;
		if ((component != null) && (component.getLocale() != null))
			defaultLocale = component.getLocale();
		else
			defaultLocale = Locale.getDefault();

		/* use thie when all available languages should be returned
		if (!localesList.contains(defaultLocale)) {
			localesList.add(0, defaultLocale);
		}
		
		try {
			Locale[] result = (Locale[])localesList.toArray(new Locale[0]);
			return result;
		} catch (ArrayStoreException ase){
			System.out.println("Warning: could not load locales");
			return new Locale[]{defaultLocale};
		}
		*/
		return new Locale[] {
			defaultLocale,
			ImUtil.Arabic1,
			ImUtil.Arabic2,
			Lookup.CHINESE_SIM,
			Lookup.CHINESE_TRA,
			ImUtil.ENGLISH,
			ImUtil.GEORGIAN_HEI,
			ImUtil.GEORGIAN_IMNA,
			ImUtil.GEORGIAN_MLT,
			ImUtil.Hebrew,
			Lookup.IPA96_RTR,
			ImUtil.IPA96_SAMPA,
			ImUtil.IPA_EXT_VK,
			ImUtil.KOREAN,
			ImUtil.Cyrillic,
			ImUtil.TURKISH
			};

		//		Locale[] result = null;
		/****
		 * HACK ALERT 
		 *    OSX had issues with the asian input dialog
		 * 	as well as font-support issues
		 *      for now leave out CHINESE_* locales
		 */
		/*
				if (System.getProperty("os.name").startsWith("Mac"))
				{
			    		Locale[] result2 = {
						defaultLocale,
						ImUtil.Cyrillic,
						ImUtil.Arabic2,
						ImUtil.Arabic1,
						ImUtil.Hebrew
						};
					result = result2;
					
				} else {
		
					Locale[] result1 = {
						defaultLocale,
						Lookup.IPA96_RTR,
						ImUtil.IPA96_SAMPA,
						Lookup.CHINESE_TRA,
						Lookup.CHINESE_SIM,
						ImUtil.Cyrillic,
						ImUtil.Arabic2,
						ImUtil.Arabic1,
						ImUtil.Hebrew
					};
					result = result1;
				}
				*/
		/*
		 * END HACK
		 */
		// See Lookup.jar for reason for this mess
		//Locale.SIMPLIFIED_CHINESE, 
		//Locale.TRADITIONAL_CHINESE,
		// return result;
	}

	/**
	   An ImUtil client requests a list of supported locales with getLanguages().
	   The order in which the user sees the locales is defined here.
	   @return an array of languages as {@see java.util.Locale}.
	 */
	public static final Locale[] getLanguages() {
		return getLanguages(null);
	}

	/**
	   An ImUtil client must contain a Component, 
	   for which the input method and the font will be set
	   @param component the component for which the font has to be set.
	   @param language the language to be set. 
	*/
	public static final void setLanguage(Component component, Locale language) {
		// test IM
		// if (!(component.getLocale().toString().equals(language.toString()) ||
		//	  allLocales.contains(language.toString()))) {
		/*
		  Markus:
		  not longer true since Chinese hack.
		  ----
		  JOptionPaneUtil.showError
		  ("Setup incomplete: cannot find language '"+language.toString()+"'!");
		  return;
		*/
		// }

		// set IM
		component.setLocale(language);

		int fontSize, fontStyle;
		if (component.getFont() != null){
			fontSize = component.getFont().getSize();
			fontStyle = component.getFont().getStyle();
		}
		else{
			fontSize = unicodeStandardFontSize;
			fontStyle = unicodeStandardFontStyle;
		}

		
		try {
			if (component.getInputContext() != null) {
				component.getInputContext().selectInputMethod(language);
			}
			// set font: language, OS, ...
			component.setFont(new java.awt.Font("Arial Unicode MS", fontStyle, fontSize));
			if (System.getProperty("os.name").equals("Windows 98")) {
				if (language == ImUtil.IPA96_RTR) {
					component.setFont(new java.awt.Font("Lucida Sans Unicode", fontStyle, fontSize));
				}
			}

			// set virtual keyboard
			if (component.getInputContext() != null) {
				Object imObject = component.getInputContext().getInputMethodControlObject();
				if (imObject != null && imObject instanceof GateIM && showKeyboard) {
					((GateIM) imObject).setMapVisible(true);
				}
			}

		}
		catch (NullPointerException npe) {
			System.out.println(
				"Component "
					+ component.getClass()
					+ " has no InputContext - no input method set!");
		}
	}

}
