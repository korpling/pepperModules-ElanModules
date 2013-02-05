/*
 * $Id: Lookup.java 5095 2005-11-24 08:56:03Z hasloe $
 */

/**
 * This class is an implementation of the java.awt.im.spi.InputMethod.     The
 * locales which defined here will be available at the JVM, if the class is
 * inside     on of the extension folders at startup.     The input methods
 * provided by this class share one property:     all have over-the-spot
 * lookup windows which are     composed by a word or a character. The
 * lookupwindow contains a list of characters.     The user selects a
 * character from the lookupwindow by pressing enter or space.     The user
 * navigates through the lookupwindow with up, down, and PgUp and PgDn.
 */
package mpi.eudico.client.im.spi.lookup;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import java.text.AttributedString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;



/**
 * DOCUMENT ME!
 *
 * @author $Author$
 * @version $Revision$
 */
public class Lookup implements InputMethod {
    /** The size of the lookup window in elements. */
    static final int lookupWindowSizeInElements = 10;

    /**
     * The International Phonetic Association has standardised a phonetic
     * alphabet. These constants refer to the IPA alphabet in the revision as
     * of 1996. The default input method is RTR.
     * 
     * <p>
     * Here is the only place, where the IPA-96 locales should be defined. All
     * other Eudico classes should refer to this constant. Locales are using
     * lowercase letters only. This is described somewhere. Suns default
     * namings for the two chinese locales are politically stupid. I therefore
     * have to define my one names. Strange enough, in the upper left icon,
     * the name "Simplified..." is used, but I cannot find a way in the API to
     * display it... I may have time to sort that out later
     * </p>
     */
    public static final Locale IPA96_RTR = new Locale("ipa-96", "", "rtr");

    /** Holds value of property DOCUMENT ME! */
    public static final Locale CHINESE_SIM = new Locale("chinese", "",
            "simplified");

    /** Holds value of property DOCUMENT ME! */
    public static final Locale CHINESE_TRA = new Locale("chinese", "",
            "traditional");

    /**
     * This array defines the locales for which input methods are implemented.
     */
    static Locale[] SUPPORTED_LOCALES = {
        Lookup.IPA96_RTR, Lookup.CHINESE_SIM, Lookup.CHINESE_TRA
    };

    //Locale.SIMPLIFIED_CHINESE, 
    //Locale.TRADITIONAL_CHINESE 

    /** resources for the locales. */
    private static Hashtable hashedFilenames;

    static {
        hashedFilenames = new java.util.Hashtable();
        hashedFilenames.put(Lookup.SUPPORTED_LOCALES[0], "ipa96.u8");
        hashedFilenames.put(Lookup.SUPPORTED_LOCALES[1], "PinyinSC.u8");
        hashedFilenames.put(Lookup.SUPPORTED_LOCALES[2], "PinyinTC.u8");
    }

    //private Common common; START
    // windows - shared by all instances of this input method
    private static Window statusWindow;
    private static Label statusWindowLabel;
    private static boolean statusWindowIsShown = false;
    private static HashMap pinyinHash;

    // lookup information - per instance
    private String[] lookupCandidates;
    private Locale[] lookupLocales;
    private int lookupCandidateCount;
    private int lookupCandidateIndex; // the number of elements in the lookup window 
    private LookupList lookupList;
    private int lookupSelection; // the index in the current window... bad!

    // per-instance state
    private InputMethodContext inputMethodContext;

    //private boolean active; // not yet used
    private Locale locale;
    private boolean converted;
    private StringBuffer rawText = new StringBuffer();
    private String convertedText;

    //private Common common; STOP

    /**
     * @see java.awt.im.spi.InputMethod
     */
    public Lookup() throws IOException, UnsupportedEncodingException {
    }

    /**
     * DOCUMENT ME!
     *
     * @param s DOCUMENT ME!
     */
    private static final void debugln(String s) {
        // System.getProperty("debug") seems to return null on Linux if "debug" not defined
        if (!"true".equals(System.getProperty("debug"))) {
            return;
        }

        System.out.println(s);
    }

    /**
     * DOCUMENT ME!
     *
     * @param locale DOCUMENT ME!
     */
    private final void updateStatusWindow(Locale locale) {
        if (statusWindowLabel == null) {
            return; // gnagnagna
        }

        statusWindowLabel.setText((locale == null) ? "default"
                                                   : locale.getDisplayName());
    }

    /**
     * Sideeffect:     pinyinHash Precondition:   locale is not null and known
     * Postcondition:  pinyinHash is set up. Errorcondition: missig resource
     * --> IOException
     *
     * @param locale DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    private final void initializeHash(Locale locale) throws IOException {
        synchronized (getClass()) {
            Lookup.pinyinHash = new HashMap();

            // Read UTF8 character stream
            BufferedReader datafile = new BufferedReader(new InputStreamReader(
                        getClass().getResourceAsStream((String) hashedFilenames.get(
                                locale)), "UTF8"));

            String buffer;

            while ((buffer = datafile.readLine()) != null) {
                int index = buffer.indexOf("\t");
                String pinyin = buffer.substring(0, index);
                ArrayList newlist = new ArrayList();
                int oldindex = index + 1;

                do {
                    index = buffer.indexOf(" ", oldindex);

                    if (index == -1) {
                        index = buffer.length();
                    }

                    String hanzi = buffer.substring(oldindex, index);

                    if (hanzi.length() > 0) {
                        newlist.add(hanzi);
                    }

                    oldindex = index + 1;
                } while (oldindex < buffer.length());

                Lookup.pinyinHash.put(pinyin.intern(), newlist);
            }

            datafile.close();
        }
    }

    /**
     * Add this character to the raw text and look it up in the hash.
     *
     * @param ch DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private final boolean wordResultsInHash(char ch) {
        return pinyinHash.containsKey(this.rawText +
            new Character(ch).toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param ch DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private final boolean lookupCharacter(char ch) {
        if (wordResultsInHash(ch)) {
            /*
               The user starts a pinyin word.
               A lookup list will be opened.
             */
            rawText.append(ch);
            sendText(false);

            return true;
        }

        return false;
    }

    /**
     * smart scrolling
     *
     * @param up DOCUMENT ME!
     */
    private final void UpDownHandler(boolean up) {
        debugln("lookupCandidateIndex " + lookupCandidateIndex);
        debugln("lookupCandidateCount " + lookupCandidateCount);
        debugln("lookupSelection " + lookupSelection);

        if (up) {
            // are we allowed to move up?
            if ((lookupSelection + lookupCandidateIndex) == 0) {
                return;
            }

            // smart scroll
            if ((lookupSelection == 1) && (lookupCandidateIndex != 0)) {
                scrollHandler(up, 1);

                // you moved the elements: don't move the cursor
                return;
            }
        } else {
            // are we allowed to move down?
            if ((lookupCandidateCount - lookupCandidateIndex) < lookupWindowSizeInElements) {
                // we are the last scroll window, which will not be filled 
                // completely with elements in general.
                if ((lookupSelection + lookupCandidateIndex) == (lookupCandidateCount -
                        1)) {
                    return;
                }
            } else {
                // there is more scroll space
                if (lookupSelection == (lookupWindowSizeInElements - 2)) {
                    // we will let the elements move backward
                    scrollHandler(up, 1);

                    // so we have to keep the cursor still
                    return;
                }

                // end of the road
                if (lookupSelection == (lookupWindowSizeInElements - 1)) {
                    return;
                }
            }
        }

        // change variable
        lookupSelection -= (up ? 1 : (-1));
        lookupList.selectCandidate(lookupSelection);
    }

    /**
     * scroll the lookup window for the size of the lookup window
     *
     * @param up whether to scroll up or down
     */
    private final void scrollHandler(boolean up) {
        scrollHandler(up, Lookup.lookupWindowSizeInElements);
    }

    /**
     * scroll the lookup window for a given number of elements
     *
     * @param up whether to scroll up or down
     * @param jumpsize number of elements to scroll
     */
    private final void scrollHandler(boolean up, int jumpsize) {
        if (up) {
            // Move look up list back, if possible
            if (lookupCandidateIndex - jumpsize >= 0) {
                lookupCandidateIndex -= jumpsize;
                lookupList.updateCandidates(lookupCandidateIndex);
            } else {
            	int scroll = jumpsize - lookupCandidateIndex;
				lookupCandidateIndex = 0;
				lookupList.updateCandidates(lookupCandidateIndex);
				if (lookupSelection - scroll > 0) {
					lookupSelection -= scroll;
				} else {
					lookupSelection = 0;
				}				
				lookupList.selectCandidate(lookupSelection);
            }
        } else {
            // Move look up list forward, if possible

            if ((lookupCandidateIndex + jumpsize) < lookupCandidateCount) {
				int scroll = 0;
				if (lookupCandidateIndex + jumpsize > lookupCandidateCount - jumpsize) {
					scroll = lookupCandidateCount - lookupCandidateIndex -jumpsize - 1;
				}
                lookupCandidateIndex += jumpsize;
				
				if (lookupCandidateIndex > lookupCandidateCount - jumpsize + 1) {
					lookupCandidateIndex = lookupCandidateCount - jumpsize + 1;
				}
				lookupList.updateCandidates(lookupCandidateIndex);
                // the last scrolled lookup windows is not completely filled in general
                lookupSelection += scroll;
                if ((lookupSelection + lookupCandidateIndex) >= lookupCandidateCount) {
                    lookupSelection = lookupCandidateCount - 1 -
                        lookupCandidateIndex;
                }
				lookupList.selectCandidate(lookupSelection);
            } else {
				lookupCandidateIndex = lookupCandidateCount - jumpsize + 1;
				lookupList.updateCandidates(lookupCandidateIndex);

				lookupSelection = lookupCandidateCount - 1 -
						lookupCandidateIndex;
				lookupList.selectCandidate(lookupSelection);
            }
        }
    }

    /**
     * Handle non-character keys, such as arrows
     *
     * @param e DOCUMENT ME!
     */
    private final void handlePressedKey(KeyEvent e) {
        if (lookupList == null) {
            return;
        }

        /*
           There is a lookup list.
           The user continues a pinyin word
         */

        // Two _KP_ (keypad) constants added for Linux support
        switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
            UpDownHandler(true);

            break;

        case KeyEvent.VK_KP_UP:
            UpDownHandler(true);

            break; // Linux?

        case KeyEvent.VK_DOWN:
            UpDownHandler(false);

            break;

        case KeyEvent.VK_KP_DOWN:
            UpDownHandler(false);

            break; // Linux?

        case KeyEvent.VK_PAGE_UP:
            scrollHandler(true);

            break;

        case KeyEvent.VK_PAGE_DOWN:
            scrollHandler(false);

            break;

            //case KeyEvent.VK_CONTROL:  System.out.println("hello control");
        }

        e.consume(); //any other non-character keys
    }

    /**
     * Handle a typed character key.
     *
     * @param e DOCUMENT ME!
     */
    private final void handleTypedKey(KeyEvent e) {
        char ch = e.getKeyChar();
        int chkc = e.getKeyCode();

        if ((lookupList != null) && lookupList.isVisible()) {
            /* There is a lookup list.
               The user continues a pinyin word
             */
            if ((' ' == ch) || (KeyEvent.VK_ENTER == ch)) {
                selectCandidate(this.lookupSelection);
                commit();
                closeLookupWindow();
                e.consume();

                return;
            }

			if (KeyEvent.VK_ESCAPE == ch) {
				cancelEdit();
				e.consume();
				return;
			}
			
            if (ch == '\b') {
                if (rawText.length() != 0) {
                    rawText.setLength(rawText.length() - 1);
                    sendText(false);
                }

                e.consume();

                return;
            }

            lookupCharacter(ch);

            /*
               The user typed a character, but the resulting
               pinyin word does not exist.
               we don't want such a word to be written.
               The user should be notified, but I think it is quiet
               obvious doing nothing.
               beep would be too harsh
               anyhow, if there is a lookup list: always consume, no matter if the word
               results in the hash or not
             */
            e.consume();

            return;
        } else {
            // There is no lookup list.		
            // There should be no rawText, otherwise, there is something wrong.
            if (rawText.length() != 0) {
                System.out.println("ask Markus: why is there rawText?");
            }

            // We may have to open a lookup list.
            if (lookupCharacter(ch)) {
                /*
                   The user starts a pinyin word.
                   A lookup list will be opened.
                 */
                e.consume();
            } else {
                /*
                   The character does not exist in the hash, not even as the beginning of a
                   word.
                   Pass through the underlying editor unchanged, and unconsumed.
                 */
            }
        }
    }

    /**
     * DOCUMENT ME!
     */
    private final void commit() {
        sendText(true);
        rawText.setLength(0);
        convertedText = null;
        converted = false;
        closeLookupWindow();
    }

    /**
     * Dispatches a more or less empty InputMethodEvent, resulting in
     * cancellation of the edit operation. Resets object values and closes the
     * LookupList  (when neccessary).
     */
    private void cancelEdit() {
        inputMethodContext.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
            null, 0, TextHitInfo.leading(0), null);
        rawText.setLength(0);
        convertedText = null;
        converted = false;
        closeLookupWindow();
    }

    /**
     * DOCUMENT ME!
     *
     * @param committed DOCUMENT ME!
     */
    private final void sendText(boolean committed) {
        String text;
        InputMethodHighlight highlight;
        int committedCharacterCount = 0;

        if (converted) {
            text = convertedText;
            highlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
        } else if (rawText.length() > 0) {
            text = new String(rawText);
            highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;

            // Redo list of characters in look up window based on latest pinyin string
            String lookupName;
            lookupName = rawText.toString().toLowerCase();

            ArrayList templist = (ArrayList) (pinyinHash.get(lookupName.intern()));

            if (templist == null) {
                Toolkit.getDefaultToolkit().beep();

                // TODO was soll das? //////////////////////////
                templist = (ArrayList) (pinyinHash.get("a"));
            }

            //System.out.println(lookupName + " " + templist.size()) ;
            lookupCandidates = new String[templist.size()];

            for (int k = 0; k < lookupCandidates.length; k++) {
                lookupCandidates[k] = (String) templist.get(k);
            }

            if (lookupCandidates != null) {
                lookupCandidateCount = lookupCandidates.length;

                lookupSelection = 0;
                lookupCandidateIndex = 0;

                if (lookupList != null) {
                    lookupList.setVisible(false);
                    lookupList = null;
                }

                openLookupWindow();
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        } else {
            text = "";
            highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
            closeLookupWindow();
        }

        AttributedString as = new AttributedString(text);

        if (committed) {
            committedCharacterCount = text.length();
        } else if (text.length() > 0) {
            as.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, highlight);
        }

        inputMethodContext.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
            as.getIterator(), committedCharacterCount,
            TextHitInfo.leading(text.length()), null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param candidate DOCUMENT ME!
     */
    private final void selectCandidate(int candidate) {
        lookupSelection = lookupCandidateIndex + candidate;
        lookupList.selectCandidate(lookupSelection);
        convertedText = lookupCandidates[lookupSelection];
        converted = true;
        sendText(false);
    }

    /**
     * DOCUMENT ME!
     */
    private final void openLookupWindow() {
        lookupList = new LookupList(this, inputMethodContext, lookupCandidates,
                lookupCandidateCount);
        lookupList.selectCandidate(lookupSelection);
    }

    /**
     * DOCUMENT ME!
     */
    private final void closeLookupWindow() {
        if (lookupList != null) {
            lookupList.setVisible(false);
            lookupList = null;
        }
    }

    private void hideLookupWindow() {
        if (lookupList != null) {
            lookupList.setVisible(false);
        }
    }

    private void showLookupWindow() {
        if (lookupList != null) {
            lookupList.setVisible(true);
        }
    }

    /**
     * @see java.awt.im.spi.InputMethod#activate
     */
    public void activate() {
        if (statusWindowIsShown) {
            if (!Lookup.statusWindow.isVisible()) {
                Lookup.statusWindow.setVisible(true);
            }

            this.updateStatusWindow(this.locale);
        }

        showLookupWindow();
    }

    /**
     * HS 05-feb-2004 On deactivation the editing is canceled. This prevents
     * all kinds of exceptions that occur when the user switches to another
     * application while the LookupList is open.  The argument
     * <code>isTemporary</code> is ignored; the editing is always canceled.
     *
     * @param isTemporary ignored
     *
     * @see java.awt.im.spi.InputMethod#deactivate
     * @see cancelEdit
     */
    public void deactivate(boolean isTemporary) {
        cancelEdit();

        /*
           if (isTemporary) {
               hideLookupWindow();
           } else {
                   hideWindows();
           }
         */
    }

    /**
     * @see java.awt.im.spi.InputMethod#dispatchEvent
     */
    public void dispatchEvent(AWTEvent event) {
        if (event instanceof KeyEvent) {
            switch (((KeyEvent) event).getID()) {
            case KeyEvent.KEY_TYPED:
                this.handleTypedKey((KeyEvent) event);

                break;

            case KeyEvent.KEY_PRESSED:
                this.handlePressedKey((KeyEvent) event);

                break;
            }
        }

        if (event instanceof MouseEvent) {
            // MOUSE_PRESSED results in hiding the window...
            // I don't see MOUSE_PRESSED here...
            // a lot of work to do.
            //HS 03-feb-2004 they come in, when the inline edit box is "attached"
            MouseEvent mevent = (MouseEvent) event;

            if (mevent.getID() == MouseEvent.MOUSE_PRESSED) {
                int y = mevent.getY();

                if ((lookupList != null) && (y >= lookupList.INSIDE_INSET) &&
                        (y < (lookupList.INSIDE_INSET +
                        (lookupCandidateCount * lookupList.LINE_SPACING)))) {
                    selectCandidate((y - lookupList.INSIDE_INSET) / lookupList.LINE_SPACING);
                    mevent.consume();
                    commit(); //??

                    //closeLookupWindow();
                }
            }
        }
    }

    /**
     * @see java.awt.im.spi.InputMethod#dispose
     */
    public void dispose() {
        hideWindows();
    }

    /**
     * @see java.awt.im.spi.InputMethod#endComposition
     */
    public void endComposition() {
        if (this.rawText.length() != 0) {
            this.commit(); // TODO do we want that?
        }

        hideWindows();
    }

    /**
     * @see java.awt.im.spi.InputMethod#getControlObject
     */
    public Object getControlObject() {
        return null;
    }

    /**
     * @see java.awt.im.spi.InputMethod#getLocale
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * @see java.awt.im.spi.InputMethod#hideWindows
     */
    public void hideWindows() {
        this.closeLookupWindow();

        if (statusWindowIsShown) {
            Lookup.statusWindow.hide();
        }
    }

    /**
     * @see java.awt.im.spi.InputMethod#isCompositionEnabled
     */
    public boolean isCompositionEnabled() {
        return true;
    }

    /**
     * @see java.awt.im.spi.InputMethod#notifyClientWindowChange
     */
    public void notifyClientWindowChange(Rectangle location) {
    }

    /**
     * @see java.awt.im.spi.InputMethod#reconvert
     */
    public void reconvert() {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.awt.im.spi.InputMethod#removeNotify
     */
    public void removeNotify() {
    }

    /**
     * @see java.awt.im.spi.InputMethod#setCharacterSubsets
     */
    public void setCharacterSubsets(Character.Subset[] subsets) {
    }

    /**
     * @see java.awt.im.spi.InputMethod#setCompositionEnabled
     */
    public void setCompositionEnabled(boolean enable) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.awt.im.spi.InputMethod#setInputMethodContext
     */
    public void setInputMethodContext(InputMethodContext context) {
        this.inputMethodContext = context;

        if ((statusWindow == null) && statusWindowIsShown) {
            statusWindow = inputMethodContext.createInputMethodWindow("Language",
                    false);
            statusWindowLabel = new Label();
            statusWindowLabel.setBackground(Color.white);
            statusWindowLabel.setSize(200, 50);
            statusWindow.add(statusWindowLabel);
            updateStatusWindow(this.locale);
            statusWindow.pack();

            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            statusWindow.setLocation(d.width - statusWindow.getWidth(),
                d.height - statusWindow.getHeight());
        }
    }

    /**
     * @see java.awt.im.spi.InputMethod#setLocale
     */
    public boolean setLocale(Locale locale) {
        //System.out.println("Lookup.java: request for " + locale);
        //System.out.println("Lookup.java: was  " + this.locale);
        if (locale == null) {
            return false;
        }

        if (locale == this.locale) {
            return true;
        }

        if (!hashedFilenames.containsKey(locale)) {
            return false;
        }

        try {
            initializeHash(locale);
            this.updateStatusWindow(locale);
            this.closeLookupWindow();
            this.locale = locale;

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
