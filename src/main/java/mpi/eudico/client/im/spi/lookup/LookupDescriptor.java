/**
 * A truely easy implementation.
 */
package mpi.eudico.client.im.spi.lookup;

import java.awt.Image;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;

import java.util.Locale;


/**
 * DOCUMENT ME!
 * $Id: LookupDescriptor.java 4210 2005-08-11 11:41:00Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class LookupDescriptor implements InputMethodDescriptor {
    /**
     * Creates a new LookupDescriptor instance
     */
    public LookupDescriptor() {
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getAvailableLocales
     */
    public Locale[] getAvailableLocales() {
        return Lookup.SUPPORTED_LOCALES;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#hasDynamicLocaleList
     */
    public boolean hasDynamicLocaleList() {
        return false;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodDisplayName
     */
    public synchronized String getInputMethodDisplayName(Locale il, Locale dl) {
        return "mpi.nl";
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodIcon
     */
    public Image getInputMethodIcon(Locale inputLocale) {
        return null;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodClassName
     */
    public String getInputMethodClassName() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws Exception DOCUMENT ME!
     */
    public InputMethod createInputMethod() throws Exception {
        return new Lookup();
    }
}
