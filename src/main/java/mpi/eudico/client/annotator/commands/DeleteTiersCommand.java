package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.util.AnnotationRecreator;

import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;

import java.awt.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;


/**
 * Removes a tier from the transcription.
 *
 * @author Han Sloetjes
 */
public class DeleteTiersCommand implements UndoableCommand {
    private String commandName;

    //state 
    //private TierImpl tier;
    private TierImpl[] tiers;
    private Vector[] depTiers;
    private ArrayList[] annotationsNodes;
    private Map[] colorPrefs;
    private Map[] fontPrefs;

    // receiver
    private TranscriptionImpl transcription;

    /**
     * A command to delete a tier (and depending tiers) from a transcription.
     *
     * @param name the name of the command
     */
    public DeleteTiersCommand(String name) {
        commandName = name;
    }

    /**
     * Adds the removed tier to the transcription.
     */
    public void undo() {
        if ((transcription != null) && (tiers != null)) {
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            setWaitCursor(true);

            TierImpl deptier;

            for (int i = 0; i < tiers.length; i++) {
            	//tier = tiers[i];
				if (transcription.getTierWithId(tiers[i].getName()) == null) {
					transcription.addTier(tiers[i]);
				}
				if (depTiers[i] != null) {
					for (int j = 0; j < depTiers[i].size(); j++) {
						deptier = (TierImpl) depTiers[i].get(j);

						if (transcription.getTierWithId(deptier.getName()) == null) {
							transcription.addTier(deptier);
						}
					}
				}
				if (annotationsNodes[i].size() > 0) {
					transcription.setNotifying(false);

					DefaultMutableTreeNode node;

					if (tiers[i].hasParentTier()) {
						AnnotationRecreator.createAnnotationsSequentially(
								transcription, annotationsNodes[i], true);
					} else {
						for (int j = 0; j < annotationsNodes[i].size(); j++) {
							node = (DefaultMutableTreeNode) annotationsNodes[i]
									.get(j);
							AnnotationRecreator.createAnnotationFromTree(
									transcription, node, true);
						}
					}

					transcription.setNotifying(true);
				}
				
				// restore preferences ??
	            if (colorPrefs != null) {
	            	Object colorsObj = Preferences.get("TierColors", transcription);
	        		if (colorsObj instanceof Map) {
	        			Map colors = (Map) colorsObj;
	        			colors.putAll(colorPrefs[i]);
	        			
	        			Preferences.set("TierColors", colors, transcription, true);
	        		}
	            }
	            if (fontPrefs != null) {
	            	Object fontsObj = Preferences.get("TierFonts", transcription);
	            	if (fontsObj instanceof Map) {
	            		Map fonts = (Map) fontsObj;
	            		fonts.putAll(fontPrefs[i]);
	            		
	            		Preferences.set("TierFonts", fonts, transcription, true);
	            	}
	            }
			}
			setWaitCursor(false);

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
            
            
        }
    }

    /**
     * Again removes the tier from the transcription.
     */
    public void redo() {
        for (int i = 0; i < tiers.length; i++) {
			//tier = tiers[i];
			if ((transcription != null) && (tiers[i] != null)) {
				transcription.removeTier(tiers[i]);
				if (depTiers[i] != null) {
					for (int j = 0; j < depTiers[i].size(); j++) {
						transcription.removeTier((TierImpl) depTiers[i].get(j));
					}
				}
				// again delete preferences
				Object colorsObj = Preferences.get("TierColors", transcription);
				if (colorsObj instanceof Map) {
					Map colors = (Map) colorsObj;
					TierImpl t;
					for (int j = 0; j < depTiers[i].size(); j++) {
						t = (TierImpl) depTiers[i].get(j);
						colors.remove(t.getName());
						;
					}
					colors.remove(tiers[i].getName());
					//Preferences.set("TierColors", colors, transcription);
				}
				Object fontsObj = Preferences.get("TierFonts", transcription);
				if (fontsObj instanceof Map) {
					Map fonts = (Map) fontsObj;
					TierImpl t;
					for (int j = 0; j < depTiers[i].size(); j++) {
						t = (TierImpl) depTiers[i].get(j);
						fonts.remove(t.getName());
					}
					fonts.remove(tiers[i].getName());
				}
			}
		}
    }

    /**
     * <b>Note: </b>it is assumed the types and order of the arguments are
     * correct.
     *
     * @param receiver the transcription
     * @param arguments the arguments:  <ul><li>arg[0] = the tier to remove
     *        (TierImpl)</li> </ul>
     */
    public void execute(Object receiver, Object[] arguments) {
        if (receiver instanceof TranscriptionImpl) {
            transcription = (TranscriptionImpl) receiver;
        } else {
            return;
        }

        tiers = new TierImpl[arguments.length];
        depTiers = new Vector[arguments.length];
        annotationsNodes = new ArrayList[arguments.length];
        colorPrefs = new Map[arguments.length];
        fontPrefs = new Map[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
			//tier = (TierImpl) arguments[i];
			tiers[i] = (TierImpl) arguments[i];
			if (tiers[i] != null) {
				
				depTiers[i] = tiers[i].getDependentTiers();
				// first store all annotations
				annotationsNodes[i] = new ArrayList();
				Vector annos = tiers[i].getAnnotations();
				Iterator anIter = annos.iterator();
				AbstractAnnotation ann;
				while (anIter.hasNext()) {
					ann = (AbstractAnnotation) anIter.next();
					annotationsNodes[i].add(AnnotationRecreator
							.createTreeForAnnotation(ann));
				}
				// then remove the tiers			
				if (depTiers[i] != null) {
					for (int j = 0; j < depTiers[i].size(); j++) {
						transcription.removeTier((TierImpl) depTiers[i].get(j));
					}
				}
				transcription.removeTier(tiers[i]);
				// store preferred colors
				Object colorsObj = Preferences.get("TierColors", transcription);
				if (colorsObj instanceof Map) {
					Map colors = (Map) colorsObj;
					colorPrefs[i] = new HashMap(colors.size());
					Object col;
					TierImpl t;
					for (int j = 0; j < depTiers[i].size(); j++) {
						t = (TierImpl) depTiers[i].get(j);
						col = colors.remove(t.getName());
						;
						if (col != null) {
							colorPrefs[i].put(t.getName(), col);
						}
					}
					col = colors.remove(tiers[i].getName());
					if (col != null) {
						colorPrefs[i].put(tiers[i].getName(), col);
					}
				}
				Object fontsObj = Preferences.get("TierFonts", transcription);
				if (fontsObj instanceof Map) {
					Map fonts = (Map) fontsObj;
					fontPrefs[i] = new HashMap(fonts.size());
					Object fon;
					TierImpl t;
					for (int j = 0; j < depTiers[i].size(); j++) {
						t = (TierImpl) depTiers[i].get(j);
						fon = fonts.remove(t.getName());
						if (fon != null) {
							fontPrefs[i].put(t.getName(), fon);
						}
					}
					fon = fonts.remove(tiers[i].getName());
					if (fon != null) {
						fontPrefs[i].put(tiers[i].getName(), fon);
					}
				}
			}
		}
    }

    /**
     * Returns the name of the command.
     *
     * @return the name of the command
     */
    public String getName() {
        return commandName;
    }

    /**
     * Changes the cursor to either a 'busy' cursor or the default cursor.
     *
     * @param showWaitCursor when <code>true</code> show the 'busy' cursor
     */
    private void setWaitCursor(boolean showWaitCursor) {
        if (showWaitCursor) {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getPredefinedCursor(
                    Cursor.WAIT_CURSOR));
        } else {
            ELANCommandFactory.getRootFrame(transcription).getRootPane()
                              .setCursor(Cursor.getDefaultCursor());
        }
    }
}
