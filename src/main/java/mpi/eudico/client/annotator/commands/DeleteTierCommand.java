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
public class DeleteTierCommand implements UndoableCommand {
    private String commandName;

    //state 
    private TierImpl tier;
    private Vector depTiers;
    private ArrayList annotationsNodes;
    private Map colorPrefs;
    private Map fontPrefs;

    // receiver
    private TranscriptionImpl transcription;

    /**
     * A command to delete a tier (and depending tiers) from a transcription.
     *
     * @param name the name of the command
     */
    public DeleteTierCommand(String name) {
        commandName = name;
    }

    /**
     * Adds the removed tier to the transcription.
     */
    public void undo() {
        if ((transcription != null) && (tier != null)) {
            int curPropMode = 0;

            curPropMode = transcription.getTimeChangePropagationMode();

            if (curPropMode != Transcription.NORMAL) {
                transcription.setTimeChangePropagationMode(Transcription.NORMAL);
            }

            setWaitCursor(true);

            TierImpl deptier;

            if (transcription.getTierWithId(tier.getName()) == null) {
                transcription.addTier(tier);
            }

            if (depTiers != null) {
                for (int i = 0; i < depTiers.size(); i++) {
                    deptier = (TierImpl) depTiers.get(i);

                    if (transcription.getTierWithId(deptier.getName()) == null) {
                        transcription.addTier(deptier);
                    }
                }
            }

            if (annotationsNodes.size() > 0) {
                transcription.setNotifying(false);

                DefaultMutableTreeNode node;

                if (tier.hasParentTier()) {
                    AnnotationRecreator.createAnnotationsSequentially(transcription,
                        annotationsNodes, true);
                } else {
                    for (int i = 0; i < annotationsNodes.size(); i++) {
                        node = (DefaultMutableTreeNode) annotationsNodes.get(i);
                        AnnotationRecreator.createAnnotationFromTree(transcription,
                            node, true);
                    }
                }

                transcription.setNotifying(true);
            }

            setWaitCursor(false);

            // restore the time propagation mode
            transcription.setTimeChangePropagationMode(curPropMode);
            
            // restore preferences ??
            if (colorPrefs != null) {
            	Object colorsObj = Preferences.get("TierColors", transcription);
        		if (colorsObj instanceof Map) {
        			Map colors = (Map) colorsObj;
        			colors.putAll(colorPrefs);
        			
        			Preferences.set("TierColors", colors, transcription, true);
        		}
            }
            if (fontPrefs != null) {
            	Object fontsObj = Preferences.get("TierFonts", transcription);
            	if (fontsObj instanceof Map) {
            		Map fonts = (Map) fontsObj;
            		fonts.putAll(fontPrefs);
            		
            		Preferences.set("TierFonts", fonts, transcription, true);
            	}
            }
        }
    }

    /**
     * Again removes the tier from the transcription.
     */
    public void redo() {
        if ((transcription == null) || (tier == null)) {
            return;
        }

        transcription.removeTier(tier);

        if (depTiers != null) {
            for (int i = 0; i < depTiers.size(); i++) {
                transcription.removeTier((TierImpl) depTiers.get(i));
            }
        }
        
        // again delete preferences
		Object colorsObj = Preferences.get("TierColors", transcription);
		if (colorsObj instanceof Map) {
			Map colors = (Map) colorsObj;
			TierImpl t;
			for (int i = 0; i < depTiers.size(); i++) {
				t = (TierImpl) depTiers.get(i);
				colors.remove(t.getName());;
			}
			colors.remove(tier.getName());
			//Preferences.set("TierColors", colors, transcription);
		}
		Object fontsObj = Preferences.get("TierFonts", transcription);
		if (fontsObj instanceof Map) {
			Map fonts = (Map) fontsObj;
			TierImpl t;
			for (int i = 0; i < depTiers.size(); i++) {
				t = (TierImpl) depTiers.get(i);
				fonts.remove(t.getName());
			}
			fonts.remove(tier.getName());
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

        tier = (TierImpl) arguments[0];

        if (tier == null) {
            return;
        }

        depTiers = tier.getDependentTiers();

        // first store all annotations
        annotationsNodes = new ArrayList();

        Vector annos = tier.getAnnotations();
        Iterator anIter = annos.iterator();
        AbstractAnnotation ann;

        while (anIter.hasNext()) {
            ann = (AbstractAnnotation) anIter.next();
            annotationsNodes.add(AnnotationRecreator.createTreeForAnnotation(
                    ann));
        }

        // then remove the tiers			
        if (depTiers != null) {
            for (int i = 0; i < depTiers.size(); i++) {
                transcription.removeTier((TierImpl) depTiers.get(i));
            }
        }

        transcription.removeTier(tier);
        // store preferred colors
		Object colorsObj = Preferences.get("TierColors", transcription);
		if (colorsObj instanceof Map) {
			Map colors = (Map) colorsObj;
			colorPrefs = new HashMap(colors.size());
			Object col;
			TierImpl t;
			for (int i = 0; i < depTiers.size(); i++) {
				t = (TierImpl) depTiers.get(i);
				col = colors.remove(t.getName());;
				if (col != null) {
					colorPrefs.put(t.getName(), col);
				}
			}
			col = colors.remove(tier.getName());
			if (col != null) {
				colorPrefs.put(tier.getName(), col);
			}
		}
		Object fontsObj = Preferences.get("TierFonts", transcription);
		if (fontsObj instanceof Map) {
			Map fonts = (Map) fontsObj;
			fontPrefs = new HashMap(fonts.size());
			Object fon;
			TierImpl t;
			for (int i = 0; i < depTiers.size(); i++) {
				t = (TierImpl) depTiers.get(i);
				fon = fonts.remove(t.getName());
				if (fon != null) {
					fontPrefs.put(t.getName(), fon);
				}
			}
			fon = fonts.remove(tier.getName());
			if (fon != null) {
				fontPrefs.put(tier.getName(), fon);
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
