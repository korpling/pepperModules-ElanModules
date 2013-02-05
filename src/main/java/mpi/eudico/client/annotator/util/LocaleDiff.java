package mpi.eudico.client.annotator.util;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import mpi.eudico.client.annotator.commands.ELANCommandFactory;


public class LocaleDiff {
    
    public LocaleDiff() {
        
    }
    
    public void run() {
        ResourceBundle english = ResourceBundle.getBundle(
                //"mpi.search.resources.SearchLanguage", new Locale("", "", ""));
    			"mpi.eudico.client.annotator.resources.ElanLanguage", new Locale("", "", ""));
        
        Locale loc = null;
        PropertyResourceBundle other = null;
        Enumeration engEn = null;
        Collection allLocs = ELANCommandFactory.getLocales();
        Iterator locIt = allLocs.iterator();
        while (locIt.hasNext()) {
            loc = (Locale) locIt.next();
            if (loc.equals(Locale.ENGLISH)) {
                continue;
            }
            try {
                other = (PropertyResourceBundle) ResourceBundle.getBundle(
                        //"mpi.search.resources.SearchLanguage", loc);
                		"mpi.eudico.client.annotator.resources.ElanLanguage", loc);
            } catch (Exception ee) {
                System.out.println("Could not load resource: " + ee.getMessage());
                continue;
            }
            
            engEn = english.getKeys();
            String key;

            ArrayList list = new ArrayList();
            while (engEn.hasMoreElements()) {
                key = (String) engEn.nextElement();
                
                // filter out mnemonics
                try {
                    Integer.valueOf(english.getString(key));
                    continue;
                } catch (NumberFormatException nfe) {
                    // do nothing, ok
                }
                
                if (other.handleGetObject(key) == null) {
                    list.add((key + "=" + english.getString(key)));
                }
            }
            Collections.sort(list);
            writeProperties(loc, list);
            //writeProperties(loc, buf);
        }
    }
    
    private void writeProperties(Locale loc, StringBuffer props) {
        try {
        	//File f = new File(System.getProperty("user.home") + File.separator + "SearchDiff_" +
            File f = new File(System.getProperty("user.home") + File.separator + "LangDiff_" +
                    loc.getLanguage() + ".properties");
            FileWriter writer = new FileWriter(f);
            writer.write(props.toString());
            writer.close();
        } catch (Exception e) {
            System.out.println("Could not write file: " + e.getMessage());
        }
    }
    
    private void writeProperties(Locale loc, ArrayList props) {
        try {
        	//File f = new File(System.getProperty("user.home") + File.separator + "SearchDiff_" +
            File f = new File(System.getProperty("user.home") + File.separator + "LangDiff_" +
                    loc.getLanguage() + ".properties");
            FileWriter writer = new FileWriter(f);
            for (int i = 0; i < props.size(); i++) {
                writer.write((String) props.get(i) + "\n");
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("Could not write file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new LocaleDiff().run();
    }
}
