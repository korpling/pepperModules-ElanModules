package mpi.eudico.client.annotator.viewer;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.gui.InlineEditBox;
import mpi.eudico.client.mediacontrol.ControllerEvent;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.event.ACMEditEvent;
import mpi.eudico.server.corpora.event.ACMEditListener;
import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
//import javax.swing.text.DefaultHighlighter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;


/**
 * Viewer for text of a selected tier with highligting where the cursor is.
 * @version Aug 2005 Identity removed
 */
public class TextViewer extends AbstractViewer implements SingleTierViewer,
    ACMEditListener, ActionListener {
    private JMenu fontMenu;
    private ButtonGroup fontSizeBG;
    private int fontSize;
    private JPopupMenu popup;
    private JMenuItem centerMI;
    private JTextArea taText;
    private JScrollPane jspText;
    private TierImpl tier;
    private Vector annotations = new Vector();
    //private int index = 0;
    private long begintime = 0;
    private long endtime = 0;
    private long[] arrTagTimes;
    private int[] arrTagPositions;
    private String tierText = "";
    private Highlighter highlighter;
	private StyledHighlightPainter selectionPainter;
	private StyledHighlightPainter currentPainter;
	private StyledHighlightPainter activeAnnotationPainter;
	private ValueHighlightPainter valuePainter;
	private Object selectionHighLightInfo;
	private Object currentHighLightInfo;
	private Object activeHighLightInfo;
	private List<Object> valueHighLightInfos;
    private int indexActiveAnnotationBegin = 0;
    private int indexActiveAnnotationEnd = 0;
    private int indexSelectionBegin = 0;
    private int indexSelectionEnd = 0;
    private int indexMediaTime = 0;
    private boolean bVisDotted; //used for visualization with dots
    private int extraLength; //used for visualization with dots
    
    private boolean centerVertically = true;
    private final Color transparent;
    private boolean enterCommits = false;
    private final String DOTS = "\u0020\u0020\u00B7\u0020\u0020";

    /**
     * Constructor
     */
    public TextViewer() {
        bVisDotted = true;
        extraLength = 4;
		transparent = new Color(
			Constants.SELECTIONCOLOR.getRed(),
			Constants.SELECTIONCOLOR.getGreen(),
			Constants.SELECTIONCOLOR.getBlue(),
			0);
		
        try {
            setLayout(new BorderLayout());

            taText = new JTextArea(4, 10) { //don't eat up any key events
                        protected boolean processKeyBinding(KeyStroke ks,
                            KeyEvent e, int condition, boolean pressed) {
                            return false;
                        }
                        
                        /**
                         * Override to set rendering hints.
                         */
                        /* only for J 1.4, new solutions for 1.5 and 1.6
                        public void paintComponent(Graphics g) {
                        	if (g instanceof Graphics2D) {
                                ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        	}
                        	super.paintComponent(g);
                        }
                        */
                    };

            taText.setFont(Constants.DEFAULTFONT);
            fontSize = 12;
            taText.setLineWrap(true);
            taText.setWrapStyleWord(true);
            taText.setForeground(Constants.DEFAULTFOREGROUNDCOLOR);
            taText.setEditable(false);
            taText.addMouseListener(new TextViewerMouseListener(taText));
            taText.addMouseMotionListener(new TextViewerMouseMotionListener());
            taText.getCaret().setSelectionVisible(false);
            taText.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
            taText.setSelectionColor(taText.getBackground());
            highlighter = taText.getHighlighter();
            selectionPainter = new StyledHighlightPainter(Constants.SELECTIONCOLOR, 1, StyledHighlightPainter.FILLED);
			selectionPainter.setVisible(false);
            currentPainter = new StyledHighlightPainter(Constants.CROSSHAIRCOLOR, 0);
            currentPainter.setVisible(false);
            activeAnnotationPainter = new StyledHighlightPainter(Constants.ACTIVEANNOTATIONCOLOR, 1);
            activeAnnotationPainter.setVisible(false);
            valuePainter = new ValueHighlightPainter(null, 0);
            valuePainter.setVisible(false);
            valueHighLightInfos = new ArrayList<Object>();
            
			currentHighLightInfo =  highlighter.addHighlight(0, 0, currentPainter);
			activeHighLightInfo = highlighter.addHighlight(0, 0, activeAnnotationPainter);
			selectionHighLightInfo = highlighter.addHighlight(0, 0, selectionPainter);
			highlighter.addHighlight(0, 0, valuePainter);

            jspText = new JScrollPane(taText);
            jspText.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jspText.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

            //            jspText.setViewportView(taText);
            add(jspText, BorderLayout.CENTER);

            ///////////////////////////////////////////////////////////////////////
            //
            // Temporary code
            // highlighter.addHighlight in doUpdate gives an error when tabpane is
            // not visible. When not visible (in sync mode) the size of tabpane is
            // set to 0. So check size in doUpdate and if 0 return.
            //
            ///////////////////////////////////////////////////////////////////////
            addComponentListener(new TextViewerComponentListener());

            setVisible(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * AR notification that the selection has changed method from SelectionUser
     * not implemented in AbstractViewer
     */
    public void updateSelection() {
        doUpdate();
    }

    /**
     * AR heeft dit hier neergezet, zie abstract viewer voor get en set
     * methodes van ActiveAnnotation. Update method from ActiveAnnotationUser
     */
    public void updateActiveAnnotation() {
        doUpdate();
    }

    /**
     * AR heeft dit hier neergezet Er moet nog gepraat worden over wat hier te
     * doen valt
     *
     * @param e DOCUMENT ME!
     */
    public void ACMEdited(ACMEditEvent e) {
    	if (tier == null) {
    		return;
    	}
        switch (e.getOperation()) {
        case ACMEditEvent.ADD_ANNOTATION_HERE:
        case ACMEditEvent.ADD_ANNOTATION_BEFORE:
        case ACMEditEvent.ADD_ANNOTATION_AFTER: {
        	// check on which tier, from this tier to root tier
        	// update what is necessary
        	if (e.getInvalidatedObject() instanceof TierImpl) {
        		TierImpl t = (TierImpl) e.getInvalidatedObject();
        		if (t == tier || tier.hasAncestor(t)) {
        			updateAnnotations();
        		}
        	}
        	
        	break;
        }
        /* do nothing
        case ACMEditEvent.CHANGE_ANNOTATION_TIME: {
        	if (e.getInvalidatedObject() instanceof AlignableAnnotation) {
                TierImpl invTier = (TierImpl) ((AlignableAnnotation) e.getInvalidatedObject()).getTier();
        	}
        	break;
        }
        */
        case ACMEditEvent.CHANGE_ANNOTATION_VALUE: {
        	// check for this tier only
        	if (e.getInvalidatedObject() instanceof Annotation) {
                TierImpl invTier = (TierImpl) ((Annotation) e.getInvalidatedObject()).getTier();
                if (invTier == tier) {
                	updateAnnotations();
                }
        	}
        	break;
        }
        	
        case ACMEditEvent.CHANGE_ANNOTATIONS:
        case ACMEditEvent.REMOVE_ANNOTATION: {
        	// always update everything
            setTier(getTier());// expensive reads and sets preferences
            if (tier == null) {
            	doUpdate();
            }
        	break;
        }
        }
    }

    /**
     * method from ElanLocaleListener not implemented in AbstractViewer
     */
    public void updateLocale() {
        createPopup();
    }

    private void createPopup() {
        popup = new JPopupMenu("");
		
		fontSizeBG = new ButtonGroup();
		fontMenu = new JMenu(ElanLocale.getString("Menu.View.FontSize"));
        
		JRadioButtonMenuItem fontRB;
		
		for (int i = 0; i < Constants.FONT_SIZES.length; i++) {
			fontRB = new JRadioButtonMenuItem(String.valueOf(Constants.FONT_SIZES[i]));
			fontRB.setActionCommand("font" + Constants.FONT_SIZES[i]);
			if (fontSize == Constants.FONT_SIZES[i]) {
				fontRB.setSelected(true);
			}
			fontRB.addActionListener(this);
			fontSizeBG.add(fontRB);
			fontMenu.add(fontRB);
		}

        popup.add(fontMenu);

        popup.addSeparator();

        //add visualization toggle
        JMenuItem menuItem = new JMenuItem(ElanLocale.getString(
                    "TextViewer.ToggleVisualization"));
        menuItem.setActionCommand("TOGGLEVISUALIZATION");
        menuItem.addActionListener(this);
		popup.add(menuItem);
		
		centerMI = new JCheckBoxMenuItem(ElanLocale.getString(
			"TextViewer.CenterVertical"));
		centerMI.setSelected(centerVertically);
		centerMI.setActionCommand("centerVert");
		centerMI.addActionListener(this);
        
        popup.add(centerMI);
        popup.addSeparator();
        JMenuItem copyItem = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        copyItem.setActionCommand("copy");
        copyItem.addActionListener(this);
        popup.add(copyItem);
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e) {
        String strAction = e.getActionCommand();

        if (strAction.indexOf("font") != -1) {
            int index = strAction.indexOf("font") + 4;

            try {
                fontSize = Integer.parseInt(strAction.substring(index));
                //repaint();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //taText.setFont(getFont().deriveFont((float) fontSize));
			taText.setFont(taText.getFont().deriveFont((float) fontSize));
            setPreference("TextViewer.FontSize", new Integer(fontSize), 
            		getViewerManager().getTranscription());
        } else if (strAction.equals("TOGGLEVISUALIZATION")) {
            bVisDotted = !bVisDotted;
            
            setPreference("TextViewer.DotSeparated", new Boolean(bVisDotted), 
            		getViewerManager().getTranscription());
            
            setTier(getTier());
        } else if (strAction == "centerVert") {
        	setCenteredVertically(centerMI.isSelected());
            setPreference("TextViewer.CenterVertical", new Boolean(centerVertically), 
            		getViewerManager().getTranscription());
        } else if (strAction.equals("copy")) {
        	taText.copy();
        }
    }

    /**
     * AR notification that some media related event happened method from
     * ControllerListener not implemented in AbstractViewer
     *
     * @param event DOCUMENT ME!
     */
    public void controllerUpdate(ControllerEvent event) {
        doUpdate();
    }
    
    /**
     * Change font size and dot-separation.
     */
	public void preferencesChanged() {
		Integer fontSi = (Integer) getPreference("TextViewer.FontSize", 
				getViewerManager().getTranscription());
		if (fontSi != null) {
			setFontSize(fontSi.intValue());
		}
		Boolean dotSep = (Boolean) getPreference("TextViewer.DotSeparated", 
				getViewerManager().getTranscription());
		if (dotSep != null) {
			setDotSeparated(dotSep.booleanValue());
		}
		Boolean vertCent = (Boolean) getPreference("TextViewer.CenterVertical", 
				getViewerManager().getTranscription());
		if (vertCent != null) {
			setCenteredVertically(vertCent.booleanValue());
		}
		
		loadFont(tier);
		
        Object val = Preferences.get("InlineEdit.EnterCommits", null);

        if (val instanceof Boolean) {
            enterCommits = ((Boolean) val).booleanValue();
        }
        // make sure the colors for cv entries are updated
        if (this.tier != null) {
        	Object cvPrefs = Preferences.get("CV.Prefs", (Transcription) tier.getParent());
        	if (cvPrefs != null && tier.getLinguisticType().getControlledVocabylaryName() != null) {
        		setTier(tier);
        	}
        }
	}
	
	/**
	 * Loads the font that has been set for the specified tier.
	 * 
	 * @param tier the selected tier
	 */
	private void loadFont(Tier tier) {
		if (tier != null) {
			Object fonts = Preferences.get("TierFonts", getViewerManager().getTranscription());
			if (fonts instanceof HashMap) {
				Font tf = (Font) ((HashMap) fonts).get(tier.getName());
				if (tf != null) {
					taText.setFont(new Font(tf.getName(), Font.PLAIN, fontSize));
				} else {
					taText.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
				}
			}
		} else {
			taText.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
		}
	}
	
	/**
	 * Calculates the position and size of the crosshair painter and requests 
	 * the text area to scroll the resulting rectangle to be visible in the view.
	 * In this calculation the <code>centerVertically</code> value is taken into
	 * account. 
	 */
    private void scrollIfNeeded() {
        try {
            Highlighter.Highlight[] h_arr = highlighter.getHighlights();

            for (int i = 0; i < h_arr.length; i++) {
                if (h_arr[i].getPainter() == currentPainter) {
                    int idx = h_arr[i].getStartOffset();
					int ide = h_arr[i].getEndOffset();
					
					Rectangle rect = taText.modelToView(idx);
					Rectangle endRect = taText.modelToView(ide);
					if (rect == null || endRect == null) {
						return;
					}
					Rectangle union = rect.union(endRect);
					
					if (centerVertically) {
						int restY = jspText.getViewport().getHeight() - union.height;
						union.y = union.y - restY / 2;
						if (union.y < 0) {
							union.y = 0;
						}
						// the +1 for some reason results in a smoother scrolling...
						union.height = union.height + restY + 1;
						
						taText.scrollRectToVisible(union);
					} else {					
	                    //taText.scrollRectToVisible(taText.modelToView(idx));
						taText.scrollRectToVisible(union);
					}
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getIndexBegin(int index, long beginTime) {
        int indexj;
        int retIndex = -1;

        for (int j = -2; j <= 2; j++) {
            indexj = index + j;

            if ((indexj >= 0) && (indexj < arrTagTimes.length)) {
                if (arrTagTimes[indexj] <= beginTime) {
                    retIndex = indexj;
                }
            }
        }

        if (retIndex == -1) {
            return index;
        } else {
            return retIndex;
        }
    }

    private int getIndexEnd(int index, long endTime) {
        int indexj;
        int retIndex = -1;

        for (int j = 2; j >= -2; j--) {
            indexj = index + j;

            if ((indexj >= 0) && (indexj < arrTagTimes.length)) {
                if (arrTagTimes[indexj] >= endTime) {
                    retIndex = indexj;
                }
            }
        }

        if (retIndex == -1) {
            return index;
        } else {
            return retIndex;
        }
    }

    /**
     * Update the complete text viewer Determines whether current time is in a
     * selection Sets the correct value in the text viewer
     */
    public void doUpdate() {
        ///////////////////////////////////////////////////////////////////////
        //
        // Temporary code
        // highlighter.addHighlight in doUpdate gives an error when tabpane is
        // not visible. When not visible (in sync mode) the size of tabpane is
        // set to 0. So check size in doUpdate and if <= 0 return.
        //
        ///////////////////////////////////////////////////////////////////////
        Dimension dim = getSize();

        if ((dim.height <= 0) || (dim.width <= 0)) {
            return;
        }

        ///////////////////////////////////////////////////////////////////////
        //
        // End temporary code
        //
        ///////////////////////////////////////////////////////////////////////
        if (arrTagTimes == null) {
            return;
        }

        //boolean bFound = false;
        long mediatime = getMediaTime();
        //int annotations_size = annotations.size();
        int indexj = 0;
        int index = 0;
        long activeAnnotationBeginTime = 0;
        long activeAnnotationEndTime = 0;
        indexActiveAnnotationBegin = 0;
        indexActiveAnnotationEnd = 0;

        long selectionBeginTime = getSelectionBeginTime();
        long selectionEndTime = getSelectionEndTime();

        Annotation activeAnnotation = getActiveAnnotation();

        if ((activeAnnotation != null) && (activeAnnotation.getTier() == tier)) {
            activeAnnotationBeginTime = activeAnnotation.getBeginTimeBoundary();
            activeAnnotationEndTime = activeAnnotation.getEndTimeBoundary();
        }

        //select the appropriate text
        try {
            if ((activeAnnotation != null) &&
                    (activeAnnotation.getTier() == tier)) {
                //VALUES FOR HIGHLIGHTING ACTIVE ANNOTATION
                //use fast search to determine approximate index from array
                index = Math.abs(Arrays.binarySearch(arrTagTimes,
                            activeAnnotationBeginTime));

                //look for the 2 surrounding indexes
                //index from active annotation begin
                indexActiveAnnotationBegin = getIndexBegin(index,
                        activeAnnotationBeginTime);

                index = Math.abs(Arrays.binarySearch(arrTagTimes,
                            activeAnnotationEndTime));

                //index from active annotation end
                indexActiveAnnotationEnd = getIndexEnd(index,
                        activeAnnotationEndTime);
            }

            if ((selectionBeginTime == 0) && (selectionEndTime == 0)) {
                //selection is cleared
                indexSelectionBegin = 0;
                indexSelectionEnd = 0;
            } else {
                //VALUES FOR HIGHLIGHTING SELECTION
                //use fast search to determine approximate index from array
                index = Math.abs(Arrays.binarySearch(arrTagTimes,
                            selectionBeginTime));

                //look for the 2 surrounding indexes
                //index from selection begin
                indexSelectionBegin = getIndexBegin(index, selectionBeginTime);

                index = Math.abs(Arrays.binarySearch(arrTagTimes,
                            selectionEndTime));

                //index from selection end
                indexSelectionEnd = getIndexEnd(index, selectionEndTime);

                if (indexSelectionEnd >= arrTagPositions.length) {
                    indexSelectionEnd = arrTagPositions.length - 1;
                }
            }

            //VALUES FOR HIGHLIGHTING CURRENT MEDIATIME
            index = Math.abs(Arrays.binarySearch(arrTagTimes, mediatime));

            for (int j = -2; j <= 2; j++) {
                indexj = index + j;

                //needed to check whether no 'crosshair' should be drawn at left of first annotation
                if (indexj < 0) {
                    indexMediaTime = -1;

                    break;
                }

                if ((indexj >= 0) && (indexj < arrTagTimes.length)) {
                    if (arrTagTimes[indexj] <= mediatime) {
                        indexMediaTime = indexj;
                    }
                }
            }
            
            // adjust painters
			if ((indexMediaTime >= 0) &&
					((indexMediaTime + 1) < arrTagPositions.length)) {
				currentPainter.setVisible(true);
				highlighter.changeHighlight(currentHighLightInfo, arrTagPositions[indexMediaTime],
						arrTagPositions[indexMediaTime + 1] - extraLength);
			} else {
				currentPainter.setVisible(false);
				// position the crosshair painter even if it is before the first annotation
				// or after the last; this way the painter's position can be used 
				// to scroll to begin or end of the document
				if (indexMediaTime < 0 && arrTagPositions.length > 1) {
					highlighter.changeHighlight(currentHighLightInfo, arrTagPositions[0], 
						arrTagPositions[1]);
				} else if (indexMediaTime  + 1 >= arrTagPositions.length && 
					arrTagPositions.length > 1) {
					highlighter.changeHighlight(currentHighLightInfo, 
						arrTagPositions[arrTagPositions.length - 2], 
						arrTagPositions[arrTagPositions.length - 1]);
				}				
			}

			if ((indexActiveAnnotationBegin >= 0) &&
				(indexActiveAnnotationBegin < arrTagPositions.length) &&
				(indexActiveAnnotationEnd >= 0) &&
				(indexActiveAnnotationEnd < arrTagPositions.length)) {
				if (activeAnnotation != null) {
					activeAnnotationPainter.setVisible(true);
					highlighter.changeHighlight(activeHighLightInfo,
							arrTagPositions[indexActiveAnnotationBegin],
						arrTagPositions[indexActiveAnnotationEnd] -
						extraLength);
				} else {
					activeAnnotationPainter.setVisible(false);
				}
			} else {
				activeAnnotationPainter.setVisible(false);
			}
			if ((indexSelectionBegin >= 0) &&
					(indexSelectionBegin < arrTagPositions.length) &&
					(indexSelectionEnd >= 0) &&
					(indexSelectionEnd < arrTagPositions.length)) {
					selectionPainter.setVisible(true);
					highlighter.changeHighlight(selectionHighLightInfo,
						arrTagPositions[indexSelectionBegin],
						arrTagPositions[indexSelectionEnd] - extraLength);
			} else {
				selectionPainter.setVisible(false);
			}
			
			/*
            if ((indexMediaTime >= 0) &&
                    ((indexMediaTime + 1) < arrTagPositions.length) &&
                    (indexSelectionBegin >= 0) &&
                    (indexSelectionBegin < arrTagPositions.length) &&
                    (indexSelectionEnd >= 0) &&
                    (indexSelectionEnd < arrTagPositions.length) &&
                    (indexActiveAnnotationBegin >= 0) &&
                    (indexActiveAnnotationBegin < arrTagPositions.length) &&
                    (indexActiveAnnotationEnd >= 0) &&
                    (indexActiveAnnotationEnd < arrTagPositions.length)) {
                taText.getHighlighter().removeAllHighlights();

                //first highlight current, because first highlight blocks the second highlight
                //i.e., you can't put a second highlight 'over' an existing highlight
                highlighter.addHighlight(arrTagPositions[indexMediaTime],
                    arrTagPositions[indexMediaTime + 1] - extraLength,
                    currentPainter);

                if (activeAnnotation != null) {
                    highlighter.addHighlight(arrTagPositions[indexActiveAnnotationBegin],
                        arrTagPositions[indexActiveAnnotationEnd] -
                        extraLength, activeAnnotationPainter);
                }

                highlighter.addHighlight(arrTagPositions[indexSelectionBegin],
                    arrTagPositions[indexSelectionEnd] - extraLength,
                    selectionPainter);
            } else if (((indexMediaTime == -1) ||
                    (indexMediaTime == (arrTagPositions.length - 1))) &&
                    (indexSelectionBegin >= 0) &&
                    (indexSelectionBegin < arrTagPositions.length) &&
                    (indexSelectionEnd >= 0) &&
                    (indexSelectionEnd < arrTagPositions.length) &&
                    (indexActiveAnnotationBegin >= 0) &&
                    (indexActiveAnnotationBegin < arrTagPositions.length) &&
                    (indexActiveAnnotationEnd >= 0) &&
                    (indexActiveAnnotationEnd < arrTagPositions.length)) {
                //for example, indexMediaTime = -1 at left of first annotation
                taText.getHighlighter().removeAllHighlights();

                if (activeAnnotation != null) {
                    highlighter.addHighlight(arrTagPositions[indexActiveAnnotationBegin],
                        arrTagPositions[indexActiveAnnotationEnd] -
                        extraLength, activeAnnotationPainter);
                }

                highlighter.addHighlight(arrTagPositions[indexSelectionBegin],
                    arrTagPositions[indexSelectionEnd] - extraLength,
                    selectionPainter);
            }
            */
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //repaint();

        scrollIfNeeded();
        
        repaint();
    }
    
    private void updateAnnotations() {
    	annotations = this.tier.getAnnotations();
        for (Object obj : valueHighLightInfos) {
        	highlighter.removeHighlight(obj);
        }
        valueHighLightInfos.clear();
        
        buildArrayAndText();
        taText.setText(tierText);
        
        // now that the text has been set, the indices of the cv value highlights have
        // to be repositioned. Get the begin positions from the painter. Do it here instead of in doUpdate
        if (valueHighLightInfos.size() > 0) {
        	Map<Integer, Color> cm = valuePainter.getColors();
        	if (cm != null) {
        		ArrayList<Integer> kl =new ArrayList<Integer>(cm.keySet());
        		Collections.sort(kl);
        		if (kl.size() == valueHighLightInfos.size()) {
        			int j = 0;
        			int pos = 0;
        			for (int i = 0; i < kl.size(); i++) {
        				pos = kl.get(i);
        				while (j < arrTagPositions.length - 1) {
        					if (arrTagPositions[j] == pos) {
        						try {
        							highlighter.changeHighlight(valueHighLightInfos.get(i), pos, arrTagPositions[j + 1] - extraLength);
        							j++;
        						} catch (BadLocationException ble) {
        							
        						}
        						break;
        					}
        					j++;
        				}
        			}
        		}
        	}
        }
        
		// call doUpdate after the TextArea has finished updating itself
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				doUpdate();
			}
		});
    }

    /**
     * Sets the tier which is shown in the text viewer
     *
     * @param tier The tier which should become visible
     */
    public void setTier(Tier tier) {
        // added by AR
        if (tier == null) {
            this.tier = null;
            annotations = new Vector();
            setPreference("TextViewer.TierName", tier, 
            		getViewerManager().getTranscription());
            taText.setFont(Constants.DEFAULTFONT.deriveFont((float) fontSize));
        } else {        	
            this.tier = (TierImpl) tier;

            try {
                annotations = this.tier.getAnnotations();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            setPreference("TextViewer.TierName", tier.getName(), 
            		getViewerManager().getTranscription());
            
            //preferencesChanged();// leads to eternal loop in case of CV colors!
            loadFont(this.tier);
        }

        for (Object obj : valueHighLightInfos) {
        	highlighter.removeHighlight(obj);
        }
        valueHighLightInfos.clear();
        
        buildArrayAndText();
        taText.setText(tierText);
        
        // now that the text has been set, the indices of the cv value highlights have
        // to be repositioned. Get the begin positions from the painter. Do it here instead of in doUpdate
        if (valueHighLightInfos.size() > 0) {
        	Map<Integer, Color> cm = valuePainter.getColors();
        	if (cm != null) {
        		ArrayList<Integer> kl =new ArrayList<Integer>(cm.keySet());
        		Collections.sort(kl);
        		if (kl.size() == valueHighLightInfos.size()) {
        			int j = 0;
        			int pos = 0;
        			for (int i = 0; i < kl.size(); i++) {
        				pos = kl.get(i);
        				while (j < arrTagPositions.length - 1) {
        					if (arrTagPositions[j] == pos) {
        						try {
        							highlighter.changeHighlight(valueHighLightInfos.get(i), pos, arrTagPositions[j + 1] - extraLength);
        							j++;
        						} catch (BadLocationException ble) {
        							
        						}
        						break;
        					}
        					j++;
        				}
        			}
        		}
        	}
        }
        
		// call doUpdate after the TextArea has finished updating itself
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				doUpdate();
			}
		});
				
        //doUpdate();
    }

    /**
     * Gets the tier which is shown in the subtitle viewer
     *
     * @return DOCUMENT ME!
     */
    public Tier getTier() {
        return tier;
    }
    
	/**
	 * Returns the current font size.
	 * 
	 * @return the current font size
	 */
	public int getFontSize() {
		return fontSize;
	}

	/**
	 * Sets the font size.
	 * 
	 * @param size the new font size
	 */
	public void setFontSize(int size) {
		fontSize = size;
		if (fontSizeBG != null) {
			Enumeration en = fontSizeBG.getElements();
			JMenuItem item;
			String value;
			while (en.hasMoreElements()) {
				item = (JMenuItem) en.nextElement();
				value = item.getText();
				try {
					int v = Integer.parseInt(value);
					if (v == fontSize) {
						item.setSelected(true);
						taText.setFont(taText.getFont().deriveFont((float) fontSize));
						break;
					}
				} catch (NumberFormatException nfe) {
					//// do nothing
				}
			}
		} else {		
			createPopup();
			taText.setFont(taText.getFont().deriveFont((float) fontSize));
		}
	}
	
	/**
	 * Returns whether the annotation values are visualized separated by a dot.
	 * 
	 * @return true if the annotation values are visualized separated by a dot, 
	 * false otherwise 
	 */
	public boolean isDotSeparated() {
		return bVisDotted;
	}
	
	/**
	 * Sets the visualization of the annotation values.
	 * 
	 * @param dotted when true the annotations are separated by dots
	 */
	public void setDotSeparated(boolean dotted) {
		if (dotted != bVisDotted) {
			bVisDotted = dotted;
			setTier(getTier());
		}
	}
	
	/**
	 * Sets whether the crosshair should be centered vertically in  
	 * the view. When this value is <code>false</code> the crosshair 
	 * will most of the time be drawn at he bottom of the view.
 	 *   
	 * @param centered the center vertically value
	 */
	public void setCenteredVertically(boolean centered) {
		centerVertically = centered;
		
		if (centerMI.isSelected() != centerVertically) {
			centerMI.setSelected(centerVertically);
		}
		
		scrollIfNeeded();
		repaint();
	}
	
	/**
	 * Returns whether the crosshair is vertically centered .
	 * 
	 * @return the center vertically value
	 */
	public boolean isCenteredVertically() {
		return centerVertically;
	}

    /**
     * Builds an array with all begintimes and endtimes from a tag Used for
     * searching (quickly) a particular tag
     */
    private void buildArrayAndText() {
        tierText = "";
        StringBuilder builder = new StringBuilder(256);
        
        int annotations_size = annotations.size();
        arrTagTimes = new long[2 * annotations_size];
        arrTagPositions = new int[2 * annotations_size];

        int arrIndexTimes = 0;
        int arrIndexPositions = 0;
        // add highlights for CVEntry values with colours
        Color c = null;
        CVEntry[] relEntries = null;
        HashMap<Integer, Color> colors = null;
        if (tier != null) {
	        String cvName = tier.getLinguisticType().getControlledVocabylaryName();
	        if (cvName != null) {
	        	ControlledVocabulary cv = ((TranscriptionImpl)tier.getParent()).getControlledVocabulary(cvName);
	        	if (cv != null) {
	        		relEntries = cv.getEntries();
	        		colors = new HashMap<Integer, Color>();
	        	}
	        }
        }

        try {
        	Annotation ann;
            for (int i = 0; i < annotations_size; i++) {
                ann = (Annotation) annotations.elementAt(i);
                String strTagValue = ann.getValue();
                c = null;
                if (relEntries != null) {
                	for (int j = 0; j < relEntries.length; j++) {
                		if (relEntries[j].getValue().equals(strTagValue)) {
                			c = relEntries[j].getPrefColor();
                			break;
                		}
                	}
                }
                
                //next line for JDK1.4 only
                //strTagValue = strTagValue.replaceAll("\n", "");
                strTagValue = strTagValue.replace('\n', ' ');

                //building text
                if (bVisDotted) {
                    //tierText += (strTagValue + DOTS);
                    builder.append(strTagValue).append(DOTS);
                    extraLength = 4;
                } else {
                    //tierText += (strTagValue + " ");
                    builder.append(strTagValue).append(' ');
                    extraLength = 0;
                }

                begintime = ann.getBeginTimeBoundary();
                endtime = ann.getEndTimeBoundary();

                arrTagTimes[arrIndexTimes++] = begintime;
                arrTagTimes[arrIndexTimes++] = endtime;

                int taglength = ((String) (strTagValue)).length() +
                    extraLength;

                if ((arrIndexPositions == 0) || (arrIndexPositions == 1)) {
                    arrTagPositions[arrIndexPositions++] = 0;
                    arrTagPositions[arrIndexPositions++] = taglength;
                } else {
                    arrTagPositions[arrIndexPositions] = arrTagPositions[arrIndexPositions -
                        1] + 1; // 1 space
                    arrIndexPositions++;
                    arrTagPositions[arrIndexPositions] = arrTagPositions[arrIndexPositions -
                        1] + taglength;
                    arrIndexPositions++;
                }
                
                if (c != null) {
                	colors.put(arrTagPositions[arrIndexPositions - 2], c);
                	Object hl = highlighter.addHighlight(arrTagPositions[arrIndexPositions - 2], 
                			arrTagPositions[arrIndexPositions - 1] - extraLength, valuePainter);
                	valueHighLightInfos.add(hl);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        valuePainter.setColors(colors);// when null it removes previous colors
        valuePainter.setVisible(colors != null && colors.size() > 0);
        
        tierText = builder.toString();
        //used for testing purposes
//        for (int tel=0; tel < arrTagTimes.length; tel++)
//        {
//            System.out.println("arrTagTimes[" + tel + "]: " + arrTagTimes[tel] + " --- " + "arrTagPositions[" + tel + "]: " + arrTagPositions[tel]);
//        }
    }

    /**
     * Handles mouse actions on the text viewer
     */
    private class TextViewerMouseListener extends MouseAdapter {
        private InlineEditBox inlineEditBox = null;
		private JComponent comp;
        /**
         * Creates a new TextViewerMouseListener instance
         *
         * @param c the parent component
         */
		public TextViewerMouseListener(JComponent c) {
			comp = c;
		}
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mousePressed(MouseEvent e) {
            stopPlayer();

            if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
                Point p = e.getPoint();
                SwingUtilities.convertPointToScreen(p, comp);

                int x = e.getPoint().x;
                int y = e.getPoint().y;

                popup.show(comp, x, y);
            }
        }

		/**
		 * Finds the index of the active annotation in the array of tags.
		 * 
		 * @return the index
		 */
        private int getArrayIndex() {
            int index = -1;

            int annotations_size = annotations.size();

            for (int i = 0; i < annotations_size; i++) {
            	Annotation ann = (Annotation) annotations.elementAt(i);

                // 4 dec 2003: there isn't always a selection to rely on
                //exact time always exists in this case
                /*
                if (getSelectionBeginTime() == tag.getBeginTime()) {
                    index = i;

                    break;
                }
                
                // temp: use the active annotation
                else */
                // HS jul 2004 only use the active annotation to find the particular index
                if ((getActiveAnnotation() != null) &&
                        (getActiveAnnotation().getBeginTimeBoundary() == ann.getBeginTimeBoundary())) {
                    index = i;

                    break;
                }
            }

            return index;
        }

        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e) || e.isPopupTrigger()) {
				return;
			}
			
            int annotations_size = annotations.size();

            //bring up edit dialog
            if (e.getClickCount() == 2) {
                if (inlineEditBox == null) {
                    inlineEditBox = new InlineEditBox(false);
                    inlineEditBox.setLocale(ElanLocale.getLocale());
                } else {
                	   inlineEditBox.setEnterCommits(enterCommits);
                }

                Annotation annotation = null;
                int index = getArrayIndex();

                if (index >= 0 && index < annotations_size) {
                    annotation = (Annotation) annotations.elementAt(index);
                    if (e.isShiftDown()) {
                    	// open CV
						inlineEditBox.setAnnotation(annotation, true);
                    } else {
						inlineEditBox.setAnnotation(annotation);
                    }
                    inlineEditBox.setFont(taText.getFont());
                    
                    inlineEditBox.detachEditor();
                }

                return;
            }

            //setting selection color to background because of strange behaviour from JTextArea
            //JTextArea's own selection system gets in the way with our selection.
            //For example, a fast three-click will select a complete line.
            //A workaround: set the selection color to the background color.
            //So the JTextArea still shows its own selection , you just don't see it.
            //One problem left: making a selection in the JTextArea by dragging a mouse.
            //You can't see your selection while you're making it because of the color.
            //To avoid this the selection color of the JTextArea is set to our
            //selection color in the mouseDragged method of TextViewerMouseMotionListener.
            //And here is the place to set it back again.
            //taText.setSelectionColor(taText.getBackground());
			taText.setSelectionColor(transparent);

            if (!taText.getText().equals("")) {
                int selectionStartPosition = taText.getSelectionStart();
                int selectionEndPosition = taText.getSelectionEnd();

                int indexSelectionStart = 0;
                int indexSelectionEnd = 0;
                int indexj = 0;

                //index from selection begin
                int index = Math.abs(Arrays.binarySearch(arrTagPositions,
                            selectionStartPosition));

                for (int j = -2; j <= 2; j++) {
                    indexj = index + j;

                    if ((indexj >= 0) && (indexj < arrTagPositions.length)) {
                        if (arrTagPositions[indexj] <= selectionStartPosition) {
                            indexSelectionStart = indexj;
                        }
                    }
                }

                //if clicked
                if ((selectionStartPosition == selectionEndPosition) &&
                        ((indexSelectionStart + 1) < arrTagTimes.length)) {
                    //don't change order of setting things!
                    // HS 4 dec 2003: changed in order to use application wide implementation of
                    // setActiveAnnotation (i.e. don't set the selection when a RefAnnotation is made active)
                    //setSelection(arrTagTimes[indexSelectionStart], arrTagTimes[indexSelectionStart + 1]);
                    index = (int) Math.ceil(indexSelectionStart / 2);

                    if (index < annotations_size) {
						Annotation ann = (Annotation) annotations.elementAt(index);
                        setActiveAnnotation(ann);
                    } else {
                        setMediaTime(arrTagTimes[indexSelectionStart]);
                    }

                    return;
                }

                //if dragged
                index = Math.abs(Arrays.binarySearch(arrTagPositions,
                            selectionEndPosition));

                for (int j = 2; j >= -2; j--) {
                    indexj = index + j;

                    if ((indexj >= 0) && (indexj < arrTagTimes.length)) {
                        if (arrTagPositions[indexj] >= selectionEndPosition) {
                            indexSelectionEnd = indexj;
                        }
                    }
                }

                //if dragged outside right border, then determine the new indexSelectionEnd
                int indexNewJ;

                if ((indexj + 2) < arrTagTimes.length) {
                    indexNewJ = indexj + 2;
                } else if ((indexj + 1) < arrTagTimes.length) {
                    indexNewJ = indexj + 1;
                } else {
                    indexNewJ = indexj;
                }

                if (indexSelectionEnd == 0) {
                    indexSelectionEnd = indexNewJ;
                }

                if ((indexSelectionStart < indexSelectionEnd) &&
                        (indexSelectionEnd < arrTagTimes.length)) {
                    setSelection(arrTagTimes[indexSelectionStart],
                        arrTagTimes[indexSelectionEnd]);
                    setMediaTime(arrTagTimes[indexSelectionStart]);
                }
            }
        }
    }
     //end of TextViewerMouseListener

    /**
     * Handles mouse motion actions on the text viewer
     */
    private class TextViewerMouseMotionListener extends MouseMotionAdapter {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void mouseDragged(MouseEvent e) {
            //see comment in mouseReleased from TextViewerMouseListener
            taText.setSelectionColor(Constants.SELECTIONCOLOR);
        }
    }
     //end of TextViewerMouseMotionListener

    /**
     * DOCUMENT ME!
     * $Id: TextViewer.java 34664 2012-12-06 12:52:23Z hasloe $
     * @author $Author$
     * @version $Revision$
     */
    private class TextViewerComponentListener extends ComponentAdapter {
        /**
         * DOCUMENT ME!
         *
         * @param e DOCUMENT ME!
         */
        public void componentResized(ComponentEvent e) {
            doUpdate();
        }
    }

}
