package mpi.eudico.client.annotator.gui;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanLocaleListener;
import mpi.eudico.client.annotator.InlineEditBoxListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.im.ImUtil;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.ExternalReference;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceGroup;
import mpi.eudico.server.corpora.clomimpl.abstr.ExternalReferenceImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.util.ControlledVocabulary;
import mpi.eudico.util.ExternalCV;
import mpi.eudico.util.ExternalCVEntry;

import mpi.eudico.util.CVEntry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import java.awt.Container;


/**
 * A class that provides and configures user interface components for  editing
 * annotation values. Elan Viewer components that offer the possibility of
 * editing annotation values can use this class to get a suitable editor
 * component. Depending on the language (locale) of the annotation it will
 * activate  special input method components.
 *
 * @author MPI
 * @version jun 2004 additions related to the use of controlled vocabularies
 * @version sep 2005 when an annotation's Locale is the system default and the 
 * the edit box's is also the system default, the language isn't set anymore.
 * Tis way it is possible to use other system specific im's / keyboards on 
 * MacOS as well.
 */
public class InlineEditBox extends JPanel implements ActionListener,
    MouseListener, MenuListener, KeyListener, ElanLocaleListener {
    /** action command constant */
    private static final String EDIT_MENU_DET = "Detach Editor";

    /** action command constant */
    private static final String EDIT_MENU_ATT = "Attach Editor";

    /** action command constant */
    private static final String EDIT_MENU_CMT = "Commit Changes";

    /** action command constant */
    private static final String EDIT_MENU_CNL = "Cancel Changes";

    /** A logger to replace System.out calls. */
    private static final Logger LOG = Logger.getLogger(InlineEditBox.class.getName());

    /** the textarea in use when the editor is used in attached mode */
    final private JTextArea textArea = new JTextArea("", 2, 1);

    /** the scrollpane for the attached-mode textarea */
    final private JScrollPane textAreaScrollPane = new JScrollPane(textArea);

    /** the textarea in use when the editor is used in detached mode */
    final private JTextArea exttextArea = new JTextArea("", 2, 1);

    /** the scrollpane for the detached-mode textarea */
    final private JScrollPane exttextAreaScrollPane = new JScrollPane(exttextArea); 

    /**
     * a focus listener that lets the attached-mode textarea request the
     * keyboard focus
     */
    final private FocusListener intFocusListener = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (!isUsingControlledVocabulary) {
                    textArea.requestFocus();
                    textArea.getCaret().setVisible(true);
                } else {
                    if (cvEntryComp != null) {
                        cvEntryComp.grabFocus();
                    }
                }
            }
            
            public void focusLost(FocusEvent e) {
				if (!isEditing) {
					transferFocusUpCycle();
				}
            }
        };

    /**
     * a focus listener that lets the detached-mode textarea request the
     * keyboard focus
     */
    final private FocusListener extFocusListener = new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (!isUsingControlledVocabulary) {
                    exttextArea.requestFocus();
                    exttextArea.getCaret().setVisible(true);
                } else {
                    if (cvEntryComp != null) {
                        cvEntryComp.grabFocus();
                    }
                }
            }
            
			public void focusLost(FocusEvent e) {
				//transferFocusUpCycle();
			}
        };

    private JPopupMenu popupMenu = new JPopupMenu("Select Language");
    
    private final static int EDIT_COMMITTED = 0;
    private final static int EDIT_CANCELED = 1;
//    private final static int EDIT_INTERUPPTED = 2;
    
    private InlineEditBoxListener listener;
    
    private JDialog externalDialog = null;
    private Rectangle dialogBounds;
    private Locale[] allLocales;
    private int numberOfLocales;
    private String oldText;
    private boolean attached = true;
    private Annotation annotation;
    private Point position;   
    private Locale annotationLocale;
    private boolean attachable;
    private boolean isUsingControlledVocabulary = false;   

    // fields for Locale changes
    private JMenu editMenu;
    private JMenu editorMenu;
    private JMenu selectLanguageMenu;
    private JMenuItem attachMI;
    private JMenuItem commitMI;
    private JMenuItem cancelMI;
    private JMenuItem closeMI;
    private JMenuItem detachPUMI;
    private JMenuItem commitPUMI;
    private JMenuItem cancelPUMI;
	private JMenuItem selectAllPUMI;
    private JMenuItem cutMI;
    private JMenuItem copyMI;
    private JMenuItem pasteMI;
    private JMenuItem cutPUMI;
    private JMenuItem copyPUMI;
    private JMenuItem pastePUMI;
    private JMenuItem selectAllMI;
    private JMenuBar menuBar;
    private JMenuItem toggleSuggestMI;

    /** a JList in a scrollpane */
    private CVEntryComponent cvEntryComp;
    private int minCVWidth = 120;
    private int minCVHeight = 120;
    
    private List<KeyStroke> keyStrokesNotToBeConsumed = new ArrayList<KeyStroke>();
    private List<KeyStroke> defaultRegisteredKeyStrokes = new ArrayList<KeyStroke>();
    /**
     * this field can be either a JPanel (this), a JScrollPane, a JTextArea , a
     * JComboBox or any other component that can be added to the layout of a
     * viewer (component)
     */
    private JComponent editorComponent;

    //temp
    private Font uniFont = Constants.DEFAULTFONT;
    
    private boolean isEditing = false;
    private boolean enterCommits = false;// historic default
    
    private String oriValue;
    private int cursorPos;
    
    private boolean restoreOriValue = false;
    
    /**
     * When this editor is not created by a viewer, it will always be created
     * as a  "detached" dialog.
     *
     * @param attachable whether or not this editor can be attached to a
     *        viewer component.
     */
    public InlineEditBox(boolean attachable) {
        init();

        this.attachable = attachable;
    }

    /**
     * Creates a new InlineEditBox instance
     */
    public InlineEditBox() {
        init();
        attached = false;
    }    
 
    public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksList){    	
    	keyStrokesNotToBeConsumed.clear();
    	keyStrokesNotToBeConsumed.addAll(ksList);
    }

    /**
     * DOCUMENT ME!
     */
    public void init() {
        KeyStroke[] kss = textArea.getRegisteredKeyStrokes();
        for(KeyStroke ks : kss) {
        	ActionListener al = textArea.getActionForKeyStroke(ks);
        	if (al != null) {
        		defaultRegisteredKeyStrokes.add(ks);
        	}
        }
        
    	Object val = Preferences.get("InlineEdit.EnterCommits", null);

        if (val instanceof Boolean) {
            enterCommits = ((Boolean) val).booleanValue();
        }
        attachable = true;
        setLayout(new BorderLayout());

        try {
            allLocales = ImUtil.getLanguages();
            numberOfLocales = (allLocales == null) ? 0 : allLocales.length;
        } catch (java.lang.NoSuchMethodError nsme) {
            // The SPI extensions have not been present at startup.
            //String msg = "Setup incomplete: you won't be able to set languages for editing.";
            String msg = ElanLocale.getString("InlineEditBox.Message.SPI") +
                "\n" + ElanLocale.getString("InlineEditBox.Message.SPI2");
            JOptionPane.showMessageDialog(null, msg, null,
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception exc) {
            LOG.warning("InlineEditBox::init::ParentIMBug::FIXME");
            LOG.warning(exc.getMessage());
        }

        textArea.addMouseListener(this);
        textArea.setLineWrap(false);
        textAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        add(textAreaScrollPane, BorderLayout.CENTER);
        textArea.getCaret().setVisible(true);
        textArea.addKeyListener(this);
        textArea.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {                	
                	if (annotationLocale != null && !annotationLocale.equals(Locale.getDefault()) && 
                			!annotationLocale.equals(textArea.getLocale())) {
                		ImUtil.setLanguage(textArea, annotationLocale);
                		textArea.setFont(uniFont);
                	}
                }
				public void focusLost(FocusEvent e) {				
					if (!isEditing) {
						transferFocusUpCycle();
					}
//					else{						
//						if(!popupMenu.isVisible() && attached && 
//							FrameManager.getInstance().getActiveFrame().isFocused() ){
//							
//							notifyListener(EDIT_INTERUPPTED);	
//						}
//					}			
				}
            });

        exttextArea.setLineWrap(true);
        exttextArea.setWrapStyleWord(true);
        exttextArea.addKeyListener(this);
        exttextArea.addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
					if (annotationLocale != null && !annotationLocale.equals(Locale.getDefault()) && 
							!annotationLocale.equals(exttextArea.getLocale())) {
                    	ImUtil.setLanguage(exttextArea, annotationLocale);
                    	exttextArea.setFont(uniFont.deriveFont(20.0f));
					}
                }
				public void focusLost(FocusEvent e) {
					if (!isEditing) {
						transferFocusUpCycle();
					}					
				}
            });

        createPopupMenu();

        exttextAreaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        exttextAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        textAreaScrollPane.addFocusListener(intFocusListener);
        addFocusListener(intFocusListener);
    }
    
    /**
     * Adda a inlineEditListener
     */
    public void addInlineEditBoxListener(InlineEditBoxListener inLineListener){
    	listener = inLineListener;
    }
    
    /**
     * Removes the inlineEditBoxListener
     */
    public void removeInlineEditBoxListener(InlineEditBoxListener inLineListener){
    	listener = null;
    }
    
    /**
     * Notifies the listener about the change
     */
    private void notifyListener(int edit_Type){
    	if(listener != null){
    		switch(edit_Type){
    		case EDIT_COMMITTED:
    			listener.editingCommitted();
    			break;
    		case EDIT_CANCELED:
    			listener.editingCancelled();
    			break;
//    		case EDIT_INTERUPPTED:
//    			listener.editingInterrupted();
//    			break;
    		}
    	}
    }

    /**
     * Creates a modal JDialog when editing is done in detached mode.
     */
    public void createExternalDialog() {
        try {
            externalDialog = new JDialog(ELANCommandFactory.getRootFrame(
                        (Transcription) annotation.getTier().getParent()),
                    ElanLocale.getString("InlineEditBox.Title"), true);
            externalDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            externalDialog.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent event) {
                    cancelEdit();
                 }
            });
            
            if (menuBar == null) {
                createJMenuBar();
            }
            
    		if (isUsingControlledVocabulary) {
            	toggleSuggestMI.setVisible(true);
            } else {
            	toggleSuggestMI.setVisible(false);
            }
    		
            externalDialog.setJMenuBar(menuBar);
            externalDialog.addFocusListener(extFocusListener);
            externalDialog.setSize(300, 300);
        } catch (Exception ex) {
            LOG.warning("Could not create external dialog: " + ex.getMessage());
        }
    }

    /**
     * Creates a popup menu.
     */
    public void createPopupMenu() {
        detachPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Detach"));
        detachPUMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                ActionEvent.SHIFT_MASK));
        detachPUMI.setActionCommand(EDIT_MENU_DET);
        detachPUMI.addActionListener(this);
        popupMenu.add(detachPUMI);
        commitPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Commit"));
        commitPUMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        commitPUMI.setActionCommand(EDIT_MENU_CMT);
        commitPUMI.addActionListener(this);
        popupMenu.add(commitPUMI);
        cancelPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Cancel"));
        cancelPUMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        cancelPUMI.setActionCommand(EDIT_MENU_CNL);
        cancelPUMI.addActionListener(this);
        popupMenu.add(cancelPUMI);

        popupMenu.addSeparator();

        cutPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Cut"));
        cutPUMI.setActionCommand("cut");
        cutPUMI.addActionListener(this);
        popupMenu.add(cutPUMI);
        copyPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        copyPUMI.setActionCommand("copy");
        copyPUMI.addActionListener(this);
        popupMenu.add(copyPUMI);
        pastePUMI = new JMenuItem(ElanLocale.getString(
                    "InlineEditBox.Edit.Paste"));
        pastePUMI.setActionCommand("paste");
        pastePUMI.addActionListener(this);
        popupMenu.add(pastePUMI);
        selectAllPUMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.SelectAll"));
        selectAllPUMI.setActionCommand("selectAll");
        selectAllPUMI.addActionListener(this);
        popupMenu.add(selectAllPUMI);

        popupMenu.addSeparator();

        JMenuItem newItem;

        for (int i = 0; i < numberOfLocales; i++) {
            if (i == 0 && allLocales[i] == Locale.getDefault()) {
                newItem = new JMenuItem(allLocales[i].getDisplayName() + " (System default)");
                newItem.setActionCommand(allLocales[i].getDisplayName());
            } else {
                newItem = new JMenuItem(allLocales[i].getDisplayName());    
            }
            
            popupMenu.add(newItem);
            newItem.addActionListener(this);
        }
    }

    private JMenuBar createJMenuBar() {
        menuBar = new JMenuBar();
        editorMenu = new JMenu(ElanLocale.getString("InlineEditBox.Menu.Editor"));
        editMenu = new JMenu(ElanLocale.getString("Menu.Edit"));
        editMenu.addMenuListener(this);
        selectLanguageMenu = new JMenu(ElanLocale.getString(
                    "InlineEditBox.Menu.Select"));

        if (attachable == true) {
            attachMI = new JMenuItem(ElanLocale.getString(
                        "InlineEditBox.Attach"));
            attachMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                    ActionEvent.SHIFT_MASK));
            attachMI.setActionCommand(EDIT_MENU_ATT);
            attachMI.addActionListener(this);
            editorMenu.add(attachMI);
        }

        commitMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Commit"));
        commitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        commitMI.setActionCommand(EDIT_MENU_CMT);
        commitMI.addActionListener(this);
        editorMenu.add(commitMI);
        cancelMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Cancel"));
        cancelMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        cancelMI.setActionCommand(EDIT_MENU_CNL);
        cancelMI.addActionListener(this);
        editorMenu.add(cancelMI);

        closeMI = new JMenuItem(ElanLocale.getString("Button.Close"));
        closeMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
    			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        closeMI.setActionCommand("close");
        closeMI.addActionListener(this);
        editorMenu.add(closeMI);
        
		// Menu item for toggling the suggest panel
        editorMenu.add(new JSeparator());
        toggleSuggestMI = new JMenuItem(ElanLocale
        		.getString("InlineEditBox.ToggleSuggestPanel"));
        toggleSuggestMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        toggleSuggestMI.setActionCommand("toggleSuggest");
        toggleSuggestMI.addActionListener(this);
        editorMenu.add(toggleSuggestMI);
        
        cutMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Cut"));
        cutMI.setActionCommand("cut");
        cutMI.addActionListener(this);
        editMenu.add(cutMI);
        copyMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        copyMI.setActionCommand("copy");
        copyMI.addActionListener(this);
        editMenu.add(copyMI);
        pasteMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.Paste"));
        pasteMI.setActionCommand("paste");
        pasteMI.addActionListener(this);
        editMenu.add(pasteMI);
        
        selectAllMI = new JMenuItem(ElanLocale.getString("InlineEditBox.Edit.SelectAll"));
        selectAllMI.setActionCommand("selectAll");
        selectAllMI.addActionListener(this);
        editMenu.add(selectAllMI);

        JMenuItem newItem;

        for (int i = 0; i < numberOfLocales; i++) {
            newItem = new JMenuItem(allLocales[i].getDisplayName());
            selectLanguageMenu.add(newItem);
            newItem.addActionListener(this);
        }

        menuBar.add(editorMenu);
        menuBar.add(editMenu);
        menuBar.add(selectLanguageMenu);

        return menuBar;
    }

    /**
     * Returns whether or not the current annotation's value is restricted by a
     * ControlledVocabulary.
     *
     * @return true if a CV has to be used, false otherwise
     */
    public boolean isUsingControlledVocabulary() {
        return isUsingControlledVocabulary;
    }

    /**
     * Overrides setVisible(boolean) in JComponent.
     *
     * @param vis the visibility value
     */
    public void setVisible(boolean vis) {
        super.setVisible(vis);

        if (externalDialog != null) {
            externalDialog.setVisible(vis);
        }

        if (vis == false) {
            //closeIM();
            setLocation(-10, -10);
           
        }
    }

    /**
     * Overrides setSize(Dimension) in Component by performing a check to
     * guarantee a minimal  size.
     *
     * @param d the requested size
     */
    public void setSize(Dimension d) {
        // set useable sizes for edit region
        if (d.getWidth() < 60) {
            d = new Dimension(60, d.height);
        }

        if (d.getHeight() < 38) {
            d = new Dimension(d.width, 38);
        }

        //AK 16/08/2002 unfortunately all those setSizes are somewhere necessary
        super.setSize(d);
        setPreferredSize(d);
        textAreaScrollPane.setPreferredSize(d);
        textAreaScrollPane.setSize(d);
    }

    /**
     * Overrides setFont(Font) in Component by also setting the font for the
     * textareas.
     *
     * @param font the Font to use
     */
    public void setFont(Font font) {
        super.setFont(font);
        uniFont = font;

        // setFont() is used at intializing superclass - textarea not yet instantiated
        if (textArea != null) {
            textArea.setFont(font);
        }

        if (exttextArea != null) {
            exttextArea.setFont(font);
        }
    }
    
    /**
     * Sets the annotation that is to be edited. 
     * When <code>forceOpenCV</code> is true an 'open' text edit box will be used
     * even if the linguistic type has an associated ControlledVocabulery.
     * 
     * @param ann the annotation to be edited
     * @param forceOpenCV if true the associated CV will be ignored, 
     * editing will be open
     */
    public void setAnnotation (Annotation ann, boolean forceOpenCV) {
		annotation = ann;
		oldText = ann.getValue();
		//textArea.setText(oldText.trim());
		textArea.setText(oldText);// don't trim, otherwise it's difficult to remove spaces newlines etc.

		try {
			annotationLocale = ((TierImpl) annotation.getTier()).getDefaultLocale();
			if (forceOpenCV) {
				isUsingControlledVocabulary = false;
			} else {			
				isUsingControlledVocabulary = ((TierImpl) annotation.getTier()).getLinguisticType()
												   .isUsingControlledVocabulary();
			}
		} catch (Exception e) {
			LOG.warning(
				"Could not establish Default Language of Tier. Using System Default instead.");
			annotationLocale = null;//??
			isUsingControlledVocabulary = false;
		}

		if (attached) {
			if (!isUsingControlledVocabulary) {
				textArea.setEditable(true);
				textArea.setCaretPosition(textArea.getText().length());

				//textArea.requestFocus();
			} else {
				textArea.setEditable(false);

				if (cvEntryComp == null) {
					cvEntryComp = new CVEntryComponent(JScrollPane.class);
				}

				cvEntryComp.setAnnotation(annotation);
			}
		}
    }

    /**
     * Sets the annotation that is to be edited.
     *
     * @param ann the annotation to be edited
     */
    public void setAnnotation(Annotation ann) {
    	if(annotation != null && ann == annotation){
    		return ;
    	} else {
    		setAnnotation(ann, false);
    	}
    }

    /**
     * Checks whether the annotation's value has been edited.
     *
     * @return true if the annotation's value has been edited, false otherwise
     */
    public boolean annotationModified() {
        return attached ? (!oldText.equals(textArea.getText()))
                        : (!oldText.equals(exttextArea.getText()));
    }

    /**
     * Returns true if the internal TextArea is open
     *
     * @return true if the component is attached to a viewer's layout
     */
    public boolean isAttached() {
        return attached;
    }

    /**
     * Sets internal TextArea (resp. "this") visible(false) and opens external
     * TextArea (resp. "externalDialog")
     */
    public void detachEditor() {
        if (attachable && !attached) {
            return;
        }

        attached = false;
        position = getLocation();
        createExternalDialog();

        if (dialogBounds != null) {
            externalDialog.setBounds(dialogBounds);
        } else {
            Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            Rectangle frameDim = externalDialog.getBounds();
            externalDialog.setLocation((screenDim.width - frameDim.width) / 2,
                (screenDim.height - frameDim.height) / 2);
        }

        ImUtil.setLanguage(textArea, Locale.getDefault());
        setVisible(false);
        
        if (!isUsingControlledVocabulary) {
            externalDialog.getContentPane().removeAll();
            externalDialog.getContentPane().add(exttextAreaScrollPane);
            exttextArea.setEditable(true);
            exttextArea.setText(textArea.getText());
            exttextArea.setCaretPosition(exttextArea.getText().length());
            exttextArea.setFont(textArea.getFont().deriveFont(20.0f));
        } else {
            if (cvEntryComp == null) {
                cvEntryComp = new CVEntryComponent(JScrollPane.class);
                cvEntryComp.setAnnotation(annotation);
            } else {
                //cvEntryComp.setAnnotation(annotation);
                //cvEntryComp.setDelegate(JScrollPane.class);
            }

            exttextArea.setEditable(false);
            cvEntryComp.removePopupListener();
            cvEntryComp.setFont(getFont());
            externalDialog.getContentPane().removeAll();
            externalDialog.getContentPane().add(cvEntryComp.getEditorComponent());
        }

        externalDialog.setVisible(true);
    }

    /**
     * Returns the editor the editor to the previous attached state and dispose
     * the external dialog.
     */
    protected void attachEditor() {
        attached = true;
        dialogBounds = externalDialog.getBounds();
        externalDialog.dispose();
        externalDialog = null;
        
        setLocation(position);
        setVisible(true);

        if (!isUsingControlledVocabulary) {
            textArea.setText(exttextArea.getText());
            textArea.requestFocus();
        } else {
            cvEntryComp.addPopupListener();

            if (editorComponent == this) {
                removeAll();
                add(cvEntryComp.getEditorComponent(), BorderLayout.CENTER);
            }

            startEdit();
        }
    }

    /**
     * Resets elements and cleans up without applying any changes in the  value
     * of the annotation.  HB, 11 oct 01, changed from protected to public
     */
    public void cancelEdit() {
    	isEditing = false;
        closeIM();

        if (attached) {
            setVisible(false);
        } else {
            if (externalDialog != null) {
                dialogBounds = externalDialog.getBounds();
                setVisible(false);
                externalDialog.dispose();
                externalDialog = null;
            }

            attached = true;
        }
       
        notifyListener(EDIT_CANCELED);	
    }

    /**
     * Checks for modifications, applies the modification if any, resets and
     * cleans  up a bit.
     */
    public void commitEdit() {
		isEditing = false;
        closeIM();
        
        Object extRef = null;
        if (isUsingControlledVocabulary && (cvEntryComp != null)) {
        	if (cvEntryComp.getSelectedEntry() != null) {
	            if (attached) {
	                textArea.setText(cvEntryComp.getSelectedEntryValue());
	            } else {
	                exttextArea.setText(cvEntryComp.getSelectedEntryValue());
	            }
	            extRef = cvEntryComp.getSelectedEntry().getExternalRef();
	            if(cvEntryComp.getSelectedEntry() instanceof ExternalCVEntry) {
	            	String entryId = ((ExternalCVEntry) cvEntryComp.getSelectedEntry()).getId();
	            	ExternalReference extRefCv = new ExternalReferenceImpl(entryId, ExternalReference.CVE_ID);
	            	ExternalReferenceGroup tmpExtRefGrp = new ExternalReferenceGroup();
	            	tmpExtRefGrp.addReference(extRefCv);
	            	if (extRef != null) {
						try {
							Object clone = ((ExternalReference) extRef).clone();
							if (clone instanceof ExternalReference) {
								tmpExtRefGrp.addReference((ExternalReference) clone);
							}
							extRef = tmpExtRefGrp;
						} catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					} else {
						extRef = tmpExtRefGrp;
					}
	            }
        	}
        }

        // remove an ExternalCV reference by passing a null value.
        if (extRef == null && ((AbstractAnnotation) annotation).getExtRef() != null) {
        	extRef = new ExternalReferenceImpl(null, ExternalReference.CVE_ID);
        }
        
        String newText = "";
        boolean modified = annotationModified();

        if (attached) {
            if (modified) {
                newText = textArea.getText();
            }

            setVisible(false);
        } else {
            if (modified) {
                newText = exttextArea.getText();
            }

            dialogBounds = externalDialog.getBounds();
            setVisible(false);
            externalDialog.dispose();
            externalDialog = null;
            attached = true;
        }

        if (modified) {
            Command c = ELANCommandFactory.createCommand(((Transcription) annotation.getTier()
                                                                                        .getParent()),
                        ELANCommandFactory.MODIFY_ANNOTATION);
            Object[] args = new Object[] { oldText, newText, extRef };
            c.execute(annotation, args);
        }
       
        notifyListener(EDIT_COMMITTED);		
    }

    /**
     * Restores the default locale (because of InputMethod stuff) of the
     * textareas, unless the language hasn't been changed when the annotation was set.
     */
    private void closeIM() {
    	if (annotationLocale != null) {
	        if (attached) {
	        	if (!textArea.getLocale().equals(Locale.getDefault())) {
					ImUtil.setLanguage(textArea, Locale.getDefault());
	        	}           
	        } else {
				if (!exttextArea.getLocale().equals(Locale.getDefault())) {
	            	ImUtil.setLanguage(exttextArea, Locale.getDefault());
				}
	        }
    	}
    }

    /**
     * Forwards the cut action to either the <code>textArea</code> or the
     * <code>exttextArea</code>, depending on the attached/detached state.
     */
    private void doCut() {
        if (attached) {
            textArea.cut();
        } else {
            exttextArea.cut();
        }
    }

    /**
     * Forwards the copy action to either the <code>textArea</code> or the
     * <code>exttextArea</code>, depending on the attached/detached state.
     */
    private void doCopy() {
        if (attached) {
            textArea.copy();
        } else {
            exttextArea.copy();
        }
    }

    /**
     * Forwards the paste action to either the <code>textArea</code> or the
     * <code>exttextArea</code>, depending on the attached/detached state.
     */
    private void doPaste() {
        if (attached) {
            textArea.paste();
        } else {
            exttextArea.paste();
        }
    }
    
	/**
	 * Forwards the select all action to either the <code>textArea</code> or the
	 * <code>exttextArea</code>, depending on the attached/detached state.
	 */
    private void doSelectAll() {
		if (attached) {
			textArea.selectAll();
		} else {
			exttextArea.selectAll();
		}
    }

    /**
     * Enables/disables edit menu items in the popup menu.<br>
     * Check the contents of the <code>textArea</code> component and the
     * system clipboard.
     */
    private void updatePopup() {   	
        if ((textArea.getSelectedText() == null) ||
                (textArea.getSelectedText().length() == 0)) {
            cutPUMI.setEnabled(false);
            copyPUMI.setEnabled(false);
        } else {
            cutPUMI.setEnabled(true);
            copyPUMI.setEnabled(true);
        }

        if (isTextOnClipboard()) {
            pastePUMI.setEnabled(true);
        } else {
            pastePUMI.setEnabled(false);
        }
        
		if (textArea.getText() == null || 
				textArea.getText().length() == 0) {
			selectAllPUMI.setEnabled(false);
		} else {
			selectAllPUMI.setEnabled(true);
		}
    }

    /**
     * Enables/disables edit menu items in the popup menu.<br>
     * Check the contents of the <code>textArea</code> component and the
     * system clipboard.
     */
    private void updateMenuBar() {
        if ((exttextArea.getSelectedText() == null) ||
                (exttextArea.getSelectedText().length() == 0)) {
            cutMI.setEnabled(false);
            copyMI.setEnabled(false);
        } else {
            cutMI.setEnabled(true);
            copyMI.setEnabled(true);
        }

        if (isTextOnClipboard()) {
            pasteMI.setEnabled(true);
        } else {
            pasteMI.setEnabled(false);
        }
        
		if (exttextArea.getText() == null || 
				exttextArea.getText().length() == 0) {
			selectAllMI.setEnabled(false);
		} else {
			selectAllMI.setEnabled(true);
		}
    }

    /**
     * Checks whether the contents of the system clipboard can be paste into  a
     * textcomponent.
     *
     * @return true if there is contents of type text, false otherwise
     */
    private boolean isTextOnClipboard() {
        Transferable contents = null;

        try {
            contents = Toolkit.getDefaultToolkit().getSystemClipboard()
                              .getContents(this);
        } catch (IllegalStateException ise) {
            LOG.warning("Could not access the system clipboard.");
        }

        if (contents != null) {
            DataFlavor[] flavors = contents.getTransferDataFlavors();
            DataFlavor best = DataFlavor.selectBestTextFlavor(flavors);

            if (best != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the (configured) editor component, ready to be used in a
     * viewer's  layout.
     *
     * @return the editor component
     *
     * @see #configureEditor(Class, Font, Dimension)
     * @see #startEdit()
     */
    public JComponent getEditorComponent() {
        if (editorComponent == null) {
            return this;
        }

        return editorComponent;
    }

    /**
     * Sets up and configures a certain kind of editor component.<br>
     *
     * @param preferredComponent DOCUMENT ME!
     * @param font DOCUMENT ME!
     * @param size DOCUMENT ME!
     *
     * @see #getEditorComponent()
     * @see #startEdit()
     */
    public void configureEditor(Class preferredComponent, Font font,
        Dimension size) {
        if (preferredComponent == JPanel.class) {
            // configures "this"
            editorComponent = this;

            if (isUsingControlledVocabulary) {
                if (cvEntryComp == null) {
                    cvEntryComp = new CVEntryComponent(JScrollPane.class);
                    cvEntryComp.setAnnotation(annotation);
                } else {
                    if (!(cvEntryComp.getEditorComponent() instanceof JScrollPane)) {
                        cvEntryComp.setDelegate(preferredComponent);
                    }
                }

                if (font != null) {
                    cvEntryComp.setFont(font);
                }

                cvEntryComp.addPopupListener();
                // default CVWidth reveals up to 18 characters, but this depends on the font etc.
                int l = cvEntryComp.getMaxEntryLength();
                int cvw = minCVWidth;
                if (l > 18) {
                	cvw = (int) ((l / 18f) * minCVWidth);
                	//cvw = cvw < minCVWidth ? minCVWidth : cvw;
                }
                int w;
                int h;

                if (size == null) {
                    w = cvw;
                    h = minCVHeight;
                } else {
                    w = size.width;
                    h = size.height;

                    if (w < cvw) {
                        w = cvw;
                    }

                    if (h < minCVHeight) {
                        h = minCVHeight;
                    }
                }

                removeAll();
                add(cvEntryComp.getEditorComponent(), BorderLayout.CENTER);
                this.setSize(w, h);
                validate();
            } else {
                if (font != null) {
                    setFont(font);
                }

                removeAll();
                add(textAreaScrollPane, BorderLayout.CENTER);

                if (size != null) {
                    setSize(size);
                }

                validate();
            }
        } else if (preferredComponent == JScrollPane.class) {
            // configure the scrollpane of either the attached-mode textfield
            // or the CVEntry list
            if (isUsingControlledVocabulary) {
                if (cvEntryComp == null) {
                    cvEntryComp = new CVEntryComponent(preferredComponent);
                    cvEntryComp.setAnnotation(annotation);
                } else {
                    if (!(cvEntryComp.getEditorComponent() instanceof JScrollPane)) {
                        cvEntryComp.setDelegate(preferredComponent);
                    }
                }

                if (font != null) {
                    cvEntryComp.setFont(font);
                }

                cvEntryComp.addPopupListener();

                if (size != null) {
                    cvEntryComp.getEditorComponent().setSize(size);
                }

                editorComponent = cvEntryComp.getEditorComponent();
            } else {
                if (font != null) {
                    setFont(font);
                }

                if (size != null) {
                    setSize(size);
                }

                editorComponent = textAreaScrollPane;
            }
        } else if (preferredComponent == JComboBox.class) {
            if (isUsingControlledVocabulary) {
                if (cvEntryComp == null) {
                    cvEntryComp = new CVEntryComponent(preferredComponent);
                    cvEntryComp.setAnnotation(annotation);
                } else {
                    if (!(cvEntryComp.getEditorComponent() instanceof JComboBox)) {
                        cvEntryComp.setDelegate(preferredComponent);
                    }
                }

                if (font != null) {
                    cvEntryComp.setFont(font);
                }

                if (size != null) {
                    cvEntryComp.getEditorComponent().setSize(size);
                }

                editorComponent = cvEntryComp.getEditorComponent();
            }
        }
    }

    /**
     * Makes the editorComponent visible and tries to grabFocus.<br>
     * This should be called after configuring and getting the editor
     * component
     *
     * @see #configureEditor(Class, Font, Dimension)
     * @see #getEditorComponent()
     */
    public void startEdit() {
		isEditing = true;
        if (editorComponent == this) {
            setVisible(true);
            requestFocus();
        } else {
            if (isUsingControlledVocabulary) {
                cvEntryComp.grabFocus();
            } else {
                editorComponent.requestFocus();
            }
        }		
    }

    /**
     * Sets the flag that determines that Enter commits without modifier.
     * 
     * @param enterCommits the Enter commits flag
     */
    public void setEnterCommits(boolean enterCommits) {
    	    this.enterCommits = enterCommits;
    }
    
    /**
     * Menu items' ActionPerformed handling.
     *
     * @param e the action event
     */
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals(EDIT_MENU_DET)) {
            detachEditor();
        } else if (command.equals(EDIT_MENU_ATT)) {
            if (attachable == true) {
                attachEditor();
            }
        } else if (command.equals(EDIT_MENU_CNL) || command.equals("close")) {
            cancelEdit();
        } else if (command.equals(EDIT_MENU_CMT)) {
            commitEdit();
        } else if (command.equals("cut")) {
            doCut();
        } else if (command.equals("copy")) {
            doCopy();
        } else if (command.equals("paste")) {
            doPaste();
        } else if (command.equals("selectAll")) {
			doSelectAll();
		} else if (command.equals("toggleSuggest")){
        	cvEntryComp.toggleSuggestPanel(externalDialog.getContentPane());
        } else {
            for (int i = 0; i < numberOfLocales; i++) {
                if (command.equals(allLocales[i].getDisplayName())) {
                    annotationLocale = allLocales[i];

                    if (attached) {
                        ImUtil.setLanguage(textArea, annotationLocale);
                        textArea.setFont(uniFont);
                    } else {
                        ImUtil.setLanguage(exttextArea, annotationLocale);
                        exttextArea.setFont(uniFont.deriveFont(20.0f));
                    }

                    break;
                }
            }
        }
    }

    /**
     * Mouse event handling for popping up the popup menu.
     *
     * @param e the mouse event
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Stub
     *
     * @param e the mouse event
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Stub
     *
     * @param e the mouse event
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Stub
     *
     * @param e the mouse event
     */
    public void mousePressed(MouseEvent e) {
		if (javax.swing.SwingUtilities.isRightMouseButton(e) ||
			 e.isPopupTrigger()) {
			updatePopup();
			popupMenu.show(textArea, e.getX(), e.getY());

			popupMenu.setVisible(true);
		}
    }

    /**
     * Stub
     *
     * @param e the mouse event
     */
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * M_P all e.consume() calls outcommented because the events are needed to
     * deselect a newly  made / edited annotation in EudicoAnnotationFrame
     * with the Escape key M_P 25 june 2003 Just the first outcommented. The
     * last when has to be consumed.
     *
     * @param e the key event
     */
    public void keyPressed(KeyEvent e) {    		
    	KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);   
    	
    	if(keyStrokesNotToBeConsumed.contains(ks)){    
    		oriValue = ((JTextArea)e.getSource()).getText();
    		cursorPos = ((JTextArea)e.getSource()).getCaretPosition();
    		restoreOriValue = false;
    		//temp
    		if (e.getKeyCode() == KeyEvent.VK_SPACE && (e.getModifiers() == KeyEvent.SHIFT_MASK
    				|| e.getModifiers() == KeyEvent.ALT_MASK ||e.getModifiers() == 0)) {
    			restoreOriValue = true;
    		} 
    		return;
    	}
    	
        // KB Cancel Changes
    	else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            //e.consume();
            cancelEdit();
        }
        // KB Detach
        else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && e.isShiftDown()) {
            if (attachable == true) {
                e.consume();

                // thread is necessary to avoid the dialog blocking events still in the eventqueue!
                if (attached) {
                    SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                detachEditor();
                            }
                        });
                } else {
                    attachEditor();
                }
            }
        }
        // KB Confirm
        else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && 
			(e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
            e.consume();
            commitEdit();
        } /*else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && e.isMetaDown() &&
                System.getProperty("os.name").startsWith("Mac OS")) {
            commitEdit(); // hack for osx metakey
        }*/
        else if ((e.getKeyCode() == KeyEvent.VK_ENTER) && enterCommits) {
            e.consume();
            commitEdit();
        } else if (defaultRegisteredKeyStrokes.contains(ks)) {
        	// don't consume
        }
        /*else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || 
        		e.getKeyCode() == KeyEvent.VK_DELETE ||
        		e.getKeyCode() == KeyEvent.VK_LEFT ||
        		e.getKeyCode() == KeyEvent.VK_RIGHT ||
        		e.getKeyCode() == KeyEvent.VK_UP ||
        		e.getKeyCode() == KeyEvent.VK_DOWN ||
        		e.getKeyCode() == KeyEvent.VK_PAGE_DOWN ||
        		e.getKeyCode() == KeyEvent.VK_PAGE_UP ||
        		e.getKeyCode() == KeyEvent.VK_HOME ||
        		e.getKeyCode() == KeyEvent.VK_END ||
        		e.getKeyCode() == KeyEvent.VK_ENTER ) {
        } */
        // June 2010 capture the standard undo/redo events to prevent undo/redo being called in the enclosing program
        // while the edit box is active. Ideally an UndoManager should be installed for the edit box.
        // maybe better to use the user defined shortcuts for undo and redo
//        else if ( (e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_Y) && 
//    			(e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
//        	// do nothing unless UndoManager is active 
//        	e.consume();
//        }
        else {
        	// consume the event to prevent actions with keyboard shortcuts to be triggered?
        	// no, that disables e.g. the backspace key on a mac        	
        	if(ks.getModifiers() == KeyEvent.CTRL_DOWN_MASK || 
        			ks.getModifiers() == KeyEvent.ALT_DOWN_MASK ||
        			ks.getModifiers() == KeyEvent.META_DOWN_MASK)   		
        		e.consume();
        }
    }

    /**
     * Stub
     *
     * @param e the key event
     */
    public void keyReleased(KeyEvent e) {    
    	if(restoreOriValue){    		
    		((JTextArea)e.getSource()).setText(oriValue);
    		((JTextArea)e.getSource()).setCaretPosition(cursorPos);
    		restoreOriValue = false;
    	}
    }

    /**
     * Stub
     *
     * @param e the key event
     */
    public void keyTyped(KeyEvent e) {      
    }

    /**
     * Updates menu items of the menubar by checking the system clipboard.
     *
     * @param e the menu event
     */
    public void menuSelected(MenuEvent e) {
        updateMenuBar();
    }

    /**
     * Stub
     *
     * @param e the menu event
     */
    public void menuDeselected(MenuEvent e) {
    }

    /**
     * Stub
     *
     * @param e the menu event
     */
    public void menuCanceled(MenuEvent e) {
    }

    /**
     * Updates UI elements after a change in the selected Locale.
     */
    public void updateLocale() {
        detachPUMI.setText(ElanLocale.getString("InlineEditBox.Detach"));
        commitPUMI.setText(ElanLocale.getString("InlineEditBox.Commit"));
        cancelPUMI.setText(ElanLocale.getString("InlineEditBox.Cancel"));
        cutPUMI.setText(ElanLocale.getString("InlineEditBox.Edit.Cut"));
        copyPUMI.setText(ElanLocale.getString("InlineEditBox.Edit.Copy"));
        pastePUMI.setText(ElanLocale.getString("InlineEditBox.Edit.Paste"));

        if (menuBar != null) {
            editorMenu.setText(ElanLocale.getString("InlineEditBox.Menu.Editor"));
            editMenu.setText(ElanLocale.getString("Menu.Edit"));
            selectLanguageMenu.setText(ElanLocale.getString(
                    "InlineEditBox.Menu.Select"));
            attachMI.setText(ElanLocale.getString("InlineEditBox.Attach"));
            commitMI.setText(ElanLocale.getString("InlineEditBox.Commit"));
            cancelMI.setText(ElanLocale.getString("InlineEditBox.Cancel"));
            closeMI.setText(ElanLocale.getString("Button.Close"));
            cutMI.setText(ElanLocale.getString("InlineEditBox.Edit.Cut"));
            copyMI.setText(ElanLocale.getString("InlineEditBox.Edit.Copy"));
            pasteMI.setText(ElanLocale.getString("InlineEditBox.Edit.Paste"));
        }
    }

    //////////////////////////
    // inner class: a component for selecting an entry from a list of entries of a cv
    //////////////////////////

    /**
     * A class that provides a component for the selection of an entry from  a ControlledVocabulary.<br>
     * The current possible delegate components are a JScrollPane containing
     * a JList or a JComboBox
     *
     * @author Han Sloetjes
     */
    class CVEntryComponent implements KeyListener, ActionListener, DocumentListener {
        /** the list containing the cv entries */
        private JList entryList;

        /** the model for the list */
        private DefaultListModel entryListModel;

        /** the scrollpane for the list */
        private JScrollPane scrollPane;

        /** popup menu for detach, commit and cancel */
        private JPopupMenu popup;

        /** moudelistener for bringing up the popup menu */
        private MouseListener popupListener;

        /** mouse listener that handles a double click on a list entry */
        private MouseListener doubleClickListener;

        /** menu items for detaching, cancelling and committing */
        private JMenuItem detachMI;

        /** menu items for detaching, cancelling and committing */
        private JMenuItem cancelMI;

        /** menu items for detaching, cancelling and committing */
        private JMenuItem commitMI;
        
		/** menu item to toggle the suggest panel */
        private JMenuItem toggleSuggestMI;

        /** a combo box editor component */
        private JComboBox box;

        /** a model for the combo box */
        private DefaultComboBoxModel entryBoxModel;

        /**
         * the component to use for editing, either a scrollpane containing a
         * JList  or a JComboBox
         */
        private JComponent delegate;

        /**
         * the array of CV entries from the ControlledVocabulary referenced by
         * the  LinguisticType in use by the Tier containing the current
         * annotation
         */
        private CVEntry[] entries;

        /** the annotation to edit */
        private Annotation annotation;
        
        /** the length in numbers of characters of the longest entry **/
        private int maxEntryLength = 0;
        // By Micha:
        /** the textfield for the suggestion (by Micha) */
		private JTextField textField;

		private Document textFieldDoc;

		/** the panel for the suggestion (by Micha) */
		private JPanel suggestPanel;

		private volatile Thread t;
        
        private String oldPartial = new String();

		private JList suggestEntryList;

		private DefaultListModel suggestEntryListModel;

		private JScrollPane suggestScrollPane;

		private MouseAdapter suggestPanelPopupListener;

		private MouseAdapter suggestPanelDoubleClickListener;

        /**
         * Creates a new entrylist and initializes components.<br>
         * Components are being initialized depending on the type of the
         * argument.
         *
         * @param componentClass the type of component to use for edit
         *        operations
         */
        public CVEntryComponent(Class componentClass) {
            initComponents(componentClass);
        }

        /**
         * Returns the current delegate component.
         *
         * @return the delegate component for editing actions
         */
        public JComponent getEditorComponent() {
            return delegate;
        }

        /**
         * Sets which type of component should be used for editing. Can depend
         * on the kind of viewer that created the InlineEditBox  and of the
         * attached / detached state.
         *
         * @param compClass the type of component to use for editing
         */
        void setDelegate(Class compClass) {
            if (delegate.getClass() == compClass) {
                return;
            }

            if (compClass == JComboBox.class) {
                if (box == null) {
                    initComponents(compClass);
                }

                delegate = box;

                // make sure it is filled with the current entries
                entryBoxModel.removeAllElements();
                fillModel(true);

                if (entryList != null) {
                    box.setSelectedItem(entryList.getSelectedValue());
                }
            } else if (compClass == JScrollPane.class) {
                if (entryList == null) {
                    initComponents(compClass);
                }

                delegate = scrollPane;
                entryListModel.clear();
                fillModel(true);

                if (box != null) {
                    entryList.setSelectedValue(box.getSelectedItem(), true);
                }
            } else if (compClass == JPanel.class) {
    			if (suggestEntryList == null) {
    				initComponents(compClass);
    			}
    			
    			delegate = suggestPanel;
    			
    			suggestEntryListModel.clear();
    			fillModel(true);

    			if (box != null) {
    				suggestEntryList.setSelectedValue(box.getSelectedItem(), true);
    			}
    		}
        }

        /**
         * Tries to ensure that the selected item is visible in the
         * scrollpane's viewport. Applies only to the JList component.
         */
        public void ensureSelectionIsVisible() {
            if (delegate instanceof JScrollPane && entryList != null) {
                entryList.ensureIndexIsVisible(entryList.getSelectedIndex());
            } else if (delegate instanceof JPanel && suggestEntryList != null) {
    			suggestEntryList.ensureIndexIsVisible(suggestEntryList.getSelectedIndex());
     	    }
        }

        /**
         * When this list is in a detached dialog it doesn't need the popup
         * because all options are in the menu bar. Applies only to the JList
         * component.
         */
        public void removePopupListener() {
            if (entryList != null) {
                entryList.removeMouseListener(popupListener);
            }
            if (suggestEntryList != null) {
    			suggestEntryList.removeMouseListener(suggestPanelPopupListener);
    			textField.removeMouseListener(suggestPanelPopupListener);
    		}
        }

        /**
         * When this list is not in a dialog menu items for detaching,
         * committing  and cancelling need to be provided.  Applies only to
         * the JList component.
         */
        public void addPopupListener() {
            if (entryList != null) {
                MouseListener[] listeners = entryList.getMouseListeners();

                for (int i = 0; i < listeners.length; i++) {
                    if (listeners[i] == popupListener) {
                        return;
                    }
                }

                entryList.addMouseListener(popupListener);
            }
    		if (suggestEntryList != null) {
    			MouseListener[] listeners = suggestEntryList.getMouseListeners();

    			for (int i = 0; i < listeners.length; i++) {
    				if (listeners[i] == suggestPanelPopupListener) {
    					return;
    				}
    			}

    			suggestEntryList.addMouseListener(suggestPanelPopupListener);
    			textField.addMouseListener(suggestPanelPopupListener);
    		}
        }

        /**
         * Initializes either a list in a scrollpane (with a popup menu etc) or
         * a combo box.  Adds listeners.
         *
         * @param component the type of component to use for editing
         */
        private void initComponents(Class component) {
            if (component == JScrollPane.class) {
                if (entryList == null) {
                    entryListModel = new DefaultListModel();
                    entryList = new JList(entryListModel);
                    entryList.setFont(InlineEditBox.this.getFont());
                    entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    scrollPane = new JScrollPane(entryList);
                    
                    if (popup == null) {
            			createPopupMenu();
            		}/*
                    popup = new JPopupMenu();
                    detachMI = new JMenuItem(ElanLocale.getString(
                                "InlineEditBox.Detach"));
                    detachMI.addActionListener(this);
                    detachMI.setAccelerator(KeyStroke.getKeyStroke(
                            KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK));
                    popup.add(detachMI);
                    commitMI = new JMenuItem(ElanLocale.getString(
                                "InlineEditBox.Commit"));
                    commitMI.addActionListener(this);
                    commitMI.setAccelerator(KeyStroke.getKeyStroke(
                            KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
                    popup.add(commitMI);
                    cancelMI = new JMenuItem(ElanLocale.getString(
                                "InlineEditBox.Cancel"));
                    cancelMI.addActionListener(this);
                    cancelMI.setAccelerator(KeyStroke.getKeyStroke(
                            KeyEvent.VK_ESCAPE, 0));
                    popup.add(cancelMI);
					*/
                    popupListener = new MouseAdapter() {
                                public void mousePressed(MouseEvent e) {
                                    if (SwingUtilities.isRightMouseButton(e) || 
                                    	e.isPopupTrigger()) {
                                        CVEntryComponent.this.popup.show(CVEntryComponent.this.entryList,
                                            e.getX(), e.getY());
                                        CVEntryComponent.this.popup.setVisible(true);
                                    }
                                }
                            };

                    doubleClickListener = new MouseAdapter() {
                                public void mouseClicked(MouseEvent e) {
                                    if (e.getClickCount() > 1) {
                                        InlineEditBox.this.commitEdit();
                                    }
                                }
                            };

                    entryList.addMouseListener(popupListener);
                    entryList.addMouseListener(doubleClickListener);

                    entryList.addKeyListener(this);
                    entryList.addListSelectionListener(new ListSelectionListener(){
                    	public void valueChanged(ListSelectionEvent lse) {
							CVEntryComponent.this.ensureSelectionIsVisible();
                    	}
                    });
                    delegate = scrollPane;
                }
            } else if (component == JComboBox.class) {
                if (box == null) {
                    entryBoxModel = new DefaultComboBoxModel();
                    box = new JComboBox(entryBoxModel);                    
                    
                    box.addActionListener(this);
                    box.addKeyListener(this);
                    delegate = box;
                }
            } else if (component == JPanel.class) {
    			if (suggestEntryList == null) {
    				suggestEntryListModel = new DefaultListModel();
    				suggestEntryList = new JList(suggestEntryListModel);
    				suggestEntryList.setFont(InlineEditBox.this.getFont());
    				suggestEntryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    				suggestEntryList.setFocusable(false);
    				suggestScrollPane = new JScrollPane(suggestEntryList);

    				textField = new JTextField();
    				textField.addKeyListener(this);
    				textFieldDoc = textField.getDocument();
    				textFieldDoc.addDocumentListener(this);
    				suggestPanel = new JPanel(new BorderLayout());
    				suggestPanel.add(textField, BorderLayout.NORTH);
    				suggestPanel.add(suggestScrollPane, BorderLayout.CENTER);
    				
    				if (popup == null) {
    					createPopupMenu();
    				}
    				
    				suggestPanelPopupListener = new MouseAdapter() {
    					public void mousePressed(MouseEvent e) {
    						if (SwingUtilities.isRightMouseButton(e) || 
    							e.isPopupTrigger()) {
    							CVEntryComponent.this.popup.show(CVEntryComponent.this.suggestEntryList,
    								e.getX(), e.getY());
    							CVEntryComponent.this.popup.setVisible(true);
    						}
    					}
    				};
    				
    				suggestPanelDoubleClickListener = new MouseAdapter() {
    							public void mouseClicked(MouseEvent e) {
    								if (e.getClickCount() > 1) {
    									InlineEditBox.this.commitEdit();
    								}
    							}
    						};

    				suggestEntryList.addMouseListener(suggestPanelPopupListener);
    				textField.addMouseListener(suggestPanelPopupListener);
    				suggestEntryList.addMouseListener(suggestPanelDoubleClickListener);

    				suggestEntryList.addListSelectionListener(new ListSelectionListener(){
    					public void valueChanged(ListSelectionEvent lse) {
    						CVEntryComponent.this.ensureSelectionIsVisible();
    					}
    				});

    				delegate = suggestPanel;
    			}
    		}
        }

        /**
         * Sets the font for the entry list component.
         *
         * @param f the font
         */
        public void setFont(Font f) {
            if (delegate == box) {
                box.setFont(f);
            } else if (delegate == scrollPane) {
                entryList.setFont(f);
            } else if (delegate == suggestPanel) {
            	textField.setFont(f);
            	suggestEntryList.setFont(f);
            }
        }

        /**
         * Gets the entry array with the entries in the cv referenced by the
         * linguistic type of the tier.
         * HS Jan 2011: try to re-use the (potentially long) list of cv entries
         *
         * @param annotation the active annotation
         */
        public void setAnnotation(Annotation annotation) {
        	ControlledVocabulary oldCV = null;
        	
        	if (this.annotation != null) {
        		TierImpl tier = (TierImpl) this.annotation.getTier();
                TranscriptionImpl trans = (TranscriptionImpl) tier.getParent();
                oldCV = trans.getControlledVocabulary(tier.getLinguisticType()
                                  .getControlledVocabylaryName());
        	}
        	
            this.annotation = annotation;
            ControlledVocabulary cv = null;
            
            if (annotation != null) {
                TierImpl tier = (TierImpl) annotation.getTier();
                TranscriptionImpl trans = (TranscriptionImpl) tier.getParent();
                cv = trans.getControlledVocabulary(tier.getLinguisticType()
                                .getControlledVocabylaryName());
            }
            
            if (cv != null) {
            	// reload local CV's anyway
            	if (cv != oldCV || !(cv instanceof ExternalCV)) {
            		entries = cv.getEntries();
                    if (entryListModel != null) {
                        entryListModel.clear();
                    }

                    if (entryBoxModel != null) {
                        entryBoxModel.removeAllElements();
                    }
                    fillModel(true);
            	} else {
            		// else reuse existing list, currently only for external CV because there is 
            		// no notification of changes in local CV's yet
                    fillModel(false);
            	}               
            } else { // cv == null
            	entries = new CVEntry[]{};
                if (entryListModel != null) {
                    entryListModel.clear();
                }

                if (entryBoxModel != null) {
                    entryBoxModel.removeAllElements();
                }
                fillModel(false);
            }
        }

        /**
         * Fills the model of either the combo box or the list with the entries
         * of the current Controlled Vocabulary.
         */
        private void fillModel(boolean reload) {
            String value = null;

            if (annotation != null) {
                value = annotation.getValue();
            }

            if (delegate == scrollPane) {
            	if (reload) {
	                for (int i = 0; i < entries.length; i++) {
	                    entryListModel.addElement(entries[i]);
	
	                    if ((value != null) && value.equals(entries[i].getValue())) {
	                        entryList.setSelectedIndex(i);
	                    }
	                    
	                    if (entries[i].getValue() != null && entries[i].getValue().length() > maxEntryLength) {
	                    	maxEntryLength = entries[i].getValue().length();
	                    }
	                }
            	} else {
            		// select the current value
            		if (value != null) {
            			CVEntry entry;
            			for (int i = 0; i < entryListModel.size(); i++) {
            				entry = (CVEntry) entryListModel.getElementAt(i);
            				if (value.equals(entry.getValue())) {
            					entryList.setSelectedIndex(i);
            					break;
            				}
            			}
            		}
            	}   
                
            	/* FIX ME : temporary fix to avoid committing the default selected value
                            to the annotation while recursive annotations are created */
            	
//            	if ((entries.length > 0) && (entryList.getSelectedIndex() < 0)) {
//            		entryList.setSelectedIndex(0);
//            	}            	
            	entryList.setSelectedIndex(-1);     
            	
            } else if (delegate == box) {
            	if (reload) {
	                for (int i = 0; i < entries.length; i++) {
	                    entryBoxModel.addElement(entries[i]);
	
	                    if ((value != null) && value.equals(entries[i].getValue())) {
	                        entryBoxModel.setSelectedItem(entries[i]);
	                    }
	                    
	                    if (entries[i].getValue() != null && entries[i].getValue().length() > maxEntryLength) {
	                    	maxEntryLength = entries[i].getValue().length();
	                    }
	                }
            	} else {
            		if (value != null) {
            			CVEntry entry;
            			for (int i = 0; i < entryBoxModel.getSize(); i++) {
            				entry = (CVEntry) entryBoxModel.getElementAt(i);
            				if (value.equals(entry.getValue())) {
            					box.setSelectedIndex(i);
            					break;
            				}
            			}
            		}
            	}
            	
            	/* FIX ME : temporary fix to avoid committing the default selected value
                            to the annotation while recursive annotations are created */
            	
//            	if ((entries.length > 0) && (box.getSelectedIndex() < 0)) {
//                    box.setSelectedIndex(0);
//            	}    
                box.setSelectedIndex(-1);                
                
            } else if (delegate == suggestPanel) {
    			textField.setText(value); // This triggers a
    			// insertUpdate of the Document, which triggers
    			// the filling of the suggestEntryList
    		}
        }

        /**
         * Tries to grant the focus to the delegate component.
         */
        public void grabFocus() {
            if (delegate == box) {
                box.requestFocus();
            } else if (delegate == scrollPane) {
                entryList.requestFocus();
                entryList.ensureIndexIsVisible(entryList.getSelectedIndex());
            } else if (delegate == suggestPanel) {
    			textField.requestFocus();
    		}
        }

        /**
         * Returns the currently selected entry value.
         *
         * @return the currently selected entry value or null
         */
        public String getSelectedEntryValue() {
            String value = null;

            if (delegate == scrollPane) {
                if (entryList.getSelectedValue() != null) {
                    value = ((CVEntry) entryList.getSelectedValue()).getValue();
                }
            } else if (delegate == box) {
                if (box.getSelectedItem() != null) {
                    value = ((CVEntry) box.getSelectedItem()).getValue();
                } else {
                    value = annotation.getValue();
                }
            } else if (delegate == suggestPanel) {
    			if (suggestEntryList.getSelectedValue() != null) {
    				value = ((CVEntry) suggestEntryList.getSelectedValue()).getValue();
    			}
    		}

            return value;
        }
        
        /**
         * Returns the currently selected entry.
         *
         * @return the currently selected entry or null
         */
        public CVEntry getSelectedEntry() {
        	CVEntry value = null;

            if (delegate == scrollPane) {
                if (entryList.getSelectedValue() != null) {
                    value = (CVEntry) entryList.getSelectedValue();
                }
            } else if (delegate == box) {
                if (box.getSelectedItem() != null) {
                    value = (CVEntry) box.getSelectedItem();
                }
            } else if (delegate == suggestPanel) {
    			if (suggestEntryList.getSelectedValue() != null) {
    				value = (CVEntry) suggestEntryList.getSelectedValue();
    			} else {
    				
    			}
    		}

            return value;
        }
        
        /**
         * Returns the number of characters of the longest entry value.
         * 
         * @return the length of the longest entry
         */
        public int getMaxEntryLength() {
        	return maxEntryLength;
        }

        /**
         * KeyPressed handling.
         *
         * @param e the key event
         */
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (e.isShiftDown()) {
                    InlineEditBox.this.detachEditor();
                } /*else if (
                	(e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0){
                    InlineEditBox.this.commitEdit();
                } */else {
                	// in all other cases commit when the Enter key is typed
                	// Check the delegate to know where to get the value from
            		if (delegate == scrollPane) {
            			if (entryList.getSelectedValue() != null) {
            				InlineEditBox.this.commitEdit();
            			} else {
            				InlineEditBox.this.cancelEdit();
            			}
            		} else {
            			if (suggestEntryList.getSelectedValue() != null) {
            				InlineEditBox.this.commitEdit();
            			} else {
            				InlineEditBox.this.cancelEdit();
            			}
            		}
                }
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                InlineEditBox.this.cancelEdit();
            } else if(e.getKeyCode() == KeyEvent.VK_DOWN && e.getSource() == textField) {
    			suggestEntryList.setSelectedIndex(suggestEntryList.getSelectedIndex() + 1);
    			suggestEntryList.ensureIndexIsVisible(suggestEntryList.getSelectedIndex());
    		} else if(e.getKeyCode() == KeyEvent.VK_UP && e.getSource() == textField) {
    			if(suggestEntryList.getSelectedIndex() <= 0) {
    				suggestEntryList.clearSelection();
    			} else {
    				suggestEntryList.setSelectedIndex(suggestEntryList.getSelectedIndex() - 1);
    				suggestEntryList.ensureIndexIsVisible(suggestEntryList.getSelectedIndex());
    			}
    		} else if(e.getKeyCode() == KeyEvent.VK_U && 
    				(e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
    			if(InlineEditBox.this.isAttached()) {
    				toggleSuggestPanel(InlineEditBox.this);
    			}
    		} else {
            	if (entries != null) {
            		int code = e.getKeyCode();
            		for (CVEntry cve : entries) {
            			if (cve.getShortcutKeyCode() == code) {
            				if (delegate == scrollPane) {
            					entryList.setSelectedValue(cve, false);
            				} else if (delegate == box) {
            					box.setSelectedItem(cve);
            				}
            				InlineEditBox.this.commitEdit();
            				break;
            			}
            		}
            	}
            }
        }

        /**
         * Key released handling: do nothing.
         *
         * @param e the key event
         */
        public void keyReleased(KeyEvent e) {
        }

        /**
         * Key typed handling: do nothing.
         *
         * @param e the key event
         */
        public void keyTyped(KeyEvent e) {
        }

        /**
         * Action handling.
         *
         * @param ae the action event
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == detachMI) {
                if (attachable) {
                    if (attached) {
                        InlineEditBox.this.detachEditor();
                    } else {
                        InlineEditBox.this.attachEditor();
                    }
                }
            } else if (ae.getSource() == commitMI) {
                InlineEditBox.this.commitEdit();
            } else if (ae.getSource() == cancelMI) {
                InlineEditBox.this.cancelEdit();
            } else if (ae.getSource() == box) {	    		
                if ((ae.getID() == ActionEvent.ACTION_PERFORMED) &&
                        (ae.getModifiers() == InputEvent.BUTTON1_MASK)) {
                    // prevent that the first click / doubleclick on the combo box
                    // causes a commit
                    if (box.isPopupVisible()) {
                        InlineEditBox.this.commitEdit();
                    }
                }
            } else if (ae.getSource() == toggleSuggestMI){
    			toggleSuggestPanel(InlineEditBox.this);
    		}
        }
        
        /**
    	 * Toggles between the scrollPane and the suggestPanel
    	 * 
    	 * @param container (the container of the scrollPane or suggestPanel)
    	 */
    	private void toggleSuggestPanel(Container container) {
    		container.removeAll();
    		
			if (delegate == scrollPane) {
				//System.err.println("DEBUG: delegate == scrollPane");
				setDelegate(JPanel.class);
				container.add(getEditorComponent(), BorderLayout.CENTER);
			} else if (delegate == suggestPanel) {
				//System.err.println("DEBUG: delegate == suggestPanel");
				setDelegate(JScrollPane.class);
				container.add(getEditorComponent(), BorderLayout.CENTER);
			}
			
			grabFocus();

			container.repaint();
			container.validate();	
		}
    	
    	/**
    	 * Creates the popup menu for the scrollPane and suggestPanel
    	 */
    	private void createPopupMenu() {
    		popup = new JPopupMenu();
            detachMI = new JMenuItem(ElanLocale.getString(
                        "InlineEditBox.Detach"));
            detachMI.addActionListener(this);
            detachMI.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, ActionEvent.SHIFT_MASK));
            popup.add(detachMI);
            commitMI = new JMenuItem(ElanLocale.getString(
                        "InlineEditBox.Commit"));
            commitMI.addActionListener(this);
            commitMI.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            popup.add(commitMI);
            cancelMI = new JMenuItem(ElanLocale.getString(
                        "InlineEditBox.Cancel"));
            cancelMI.addActionListener(this);
            cancelMI.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_ESCAPE, 0));
            popup.add(cancelMI);
            
            popup.add(new JSeparator());
            toggleSuggestMI = new JMenuItem(ElanLocale.getString(
            	"InlineEditBox.ToggleSuggestPanel"));
            toggleSuggestMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U,
            		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            toggleSuggestMI.addActionListener(this);
            popup.add(toggleSuggestMI);
    	}
    	
    	/* (non-Javadoc)
    	 * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
    	 */
    	public void insertUpdate(DocumentEvent e) {
    		if(e.getDocument() == textFieldDoc && !oldPartial.equals(textField.getText())) {
    			findSuggestions();
    		}
        }
        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent e) {
        	if(e.getDocument() == textFieldDoc && !oldPartial.equals(textField.getText())) {
    			findSuggestions();
    		}
        }
        /* (non-Javadoc)
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent e) {
        }
        
        /**
         * Starts a thread that finds entry suggestions
         * 
         * @param
         */
        private void findSuggestions() {
           	oldPartial = textField.getText();
			if(t != null && t.isAlive()) {
				Thread tmpT = t;
				t = null;
				if(tmpT != null) tmpT.interrupt();
				try {
					tmpT.join();
				} catch(Exception ie) {
					System.out.println(ie);
				}
			}
			t = new Thread(new SuggestionsFinder(oldPartial));
			t.start();
        }
        
        /**
		 * Finds entries that (partially) match contents of the text field of the
		 * suggest panel.
		 * Reason for extending Runnable: if the number of entries is very large,
		 * searching would interfere typing. 
		 * 
		 * NOT THREAD SAVE it reads the entries array from class CVEntryComponent
		 * 
		 * @author Micha Hulsbosch
		 *
		 */
		private class SuggestionsFinder implements Runnable {
    		
    		private String part;
    		
    		public SuggestionsFinder(String part) {
    			this.part = part;
    		}
    		
    		public void run() {
				ArrayList suggestions = new ArrayList();
				Boolean suggestSearchMethodFlag = false;
				Boolean suggestSearchInDescFlag = false;
				Boolean suggestIgnoreCaseFlag = false;
				Object val = Preferences
						.get("SuggestPanel.EntryContains", null);
				if (val instanceof Boolean) {
					suggestSearchMethodFlag = ((Boolean) val).booleanValue();
				}
				val = Preferences.get("SuggestPanel.SearchDescription", null);
				if (val instanceof Boolean) {
					suggestSearchInDescFlag = ((Boolean) val).booleanValue();
				}
				val = Preferences.get("SuggestPanel.IgnoreCase", null);

				if (val instanceof Boolean) {
					suggestIgnoreCaseFlag = ((Boolean) val).booleanValue();
				}
				if (part != null && !part.equals("")) {
					int entriesIndex = 0;
					if (suggestIgnoreCaseFlag) {
						part = part.toLowerCase();
					}
					while (entriesIndex < entries.length
							&& !Thread.currentThread().isInterrupted()) {
						CVEntry next = entries[entriesIndex];
						String entryValue = next.getValue();
						if (suggestIgnoreCaseFlag) {
							entryValue = entryValue.toLowerCase();
						}
						if (suggestSearchMethodFlag) {
							if (entryValue.contains(part)) {
								suggestions.add(next);
							} else if (suggestSearchInDescFlag
									&& next.getDescription().toLowerCase()
											.contains(part)) {
								suggestions.add(next);
							}
						} else {
							if (entryValue.startsWith(part)) {
								suggestions.add(next);
							} else if (suggestSearchInDescFlag
									&& next.getDescription().toLowerCase()
											.contains(part)) {
								suggestions.add(next);
							}
						}
						entriesIndex++;
					}
					if (!Thread.currentThread().isInterrupted()) {
						SwingUtilities.invokeLater(new SuggestionsDisplayer(
								suggestions));
					}
				} else {
					if (!Thread.currentThread().isInterrupted()) {
						SwingUtilities.invokeLater(new SuggestionsDisplayer(
								new ArrayList()));
					}
				}
			} 
    	}
		
		/**
		 * Displays the suggestions in the suggestEntryList
		 * 
		 * Should be invoked in the Event dispatching thread
		 * 
		 * @author Micha Hulsbosch
		 * 
		 */
    	private class SuggestionsDisplayer implements Runnable {
    		ArrayList suggestions;
    		
    		public SuggestionsDisplayer(ArrayList suggestions) {
    			this.suggestions = suggestions;
    			suggestEntryListModel.clear();
    		}
    		
    		public void run() {
    			Iterator suggestionIterator = suggestions.iterator();
    			int suggestionIndex = 0;
    			while (suggestionIterator.hasNext()) {
    				CVEntry nextSuggestion = ((CVEntry) suggestionIterator.next());
    				suggestEntryListModel.addElement(nextSuggestion);
    				if (nextSuggestion.getValue().equals(oldPartial)) {
    					suggestEntryList.setSelectedIndex(suggestionIndex);
    					suggestEntryList.ensureIndexIsVisible(suggestionIndex);
    				}
    				suggestionIndex++;
    				//System.out.println("Added to list: " + nextSuggestion);
    			}
    		}
    	}
    }
    // end of CVEntryComponent
}
