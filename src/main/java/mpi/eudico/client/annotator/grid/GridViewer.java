package mpi.eudico.client.annotator.grid;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.InlineEditBoxListener;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.viewer.SingleTierViewer;

import mpi.eudico.server.corpora.clom.AnnotationCore;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.ConcatAnnotation; //Added Coralie Villes

import mpi.eudico.server.corpora.clomimpl.abstr.AnnotationCoreImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;
import mpi.eudico.server.corpora.event.ACMEditEvent;

import mpi.eudico.util.CVEntry;
import mpi.eudico.util.ControlledVocabulary;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;

/**
 * This class adds functionality for showing all annotations of one tier plus
 * corresponding annotations on children tiers
 * @version Aug 2005 Identity removed
 */
public class GridViewer extends AbstractEditableGridViewer
    implements SingleTierViewer, ActionListener, InlineEditBoxListener{
    /** Holds value of property DOCUMENT ME! */
    public static final int SINGLE_TIER_MODE = 0;

    //add association and subdivision mode to saw every dependance between tiers mod. Coralie Villes
    /** Holds value of property DOCUMENT ME! */
    public static final int MULTI_TIER_ASSOCIATION_MODE = 1;
    public static final int MULTI_TIER_SUBDIVISION_MODE = 2;
    
    /** Holds value of property DOCUMENT ME! */
    public static final int MULTI_TIER_MODE = 1;
    private int mode = SINGLE_TIER_MODE;

    /** Holds value of property DOCUMENT ME! */
    private final String EMPTY = "";
    private TierImpl tier;

    /**
     * Holds visibility values for the count, begintime, endtime and duration
     * columns in multi tier mode
     */
    private List childTiers = new ArrayList();

    /**
     * Stores the names of child tiers the moment they are added to the table.
     * This 'old' name can then be used to find the right column after a
     * change  of the tier name.
     */
    private Map childTierNames = new HashMap();
    private Set storedInvisibleColumns = new HashSet();

    /**
     * Constructor
     */
    public GridViewer() {
        super(new AnnotationTable(new GridViewerTableModel()));
        
        // register with popup menu for user prefs changes
        if (popup != null) {
        	popup.addActionListener(this);
        }
        // default for MultiTier-View
        storedInvisibleColumns.add(GridViewerTableModel.BEGINTIME);
        storedInvisibleColumns.add(GridViewerTableModel.ENDTIME);
        storedInvisibleColumns.add(GridViewerTableModel.DURATION);
    }

    /**
     * DOCUMENT ME!
     *
     * @param annotations DOCUMENT ME!
     */
    public void updateDataModel(List annotations) {
        removeChildrenColumns();
        childTiers.clear();
        childTierNames.clear();
        super.updateDataModel(annotations);
    }

    /**
     * To be called when the viewer is closing
     */
    public void isClosing(){
    	if(table != null){
    		if(table.isEditing() && gridEditor != null){
    			Object val = Preferences.get("InlineEdit.DeselectCommits", null);
    			if (val instanceof Boolean && (Boolean)val) {
    				gridEditor.commitEdit();
    			}else {
    				gridEditor.cancelCellEditing();
    			}
    		}
    	}
    }
    
    /**
     * Checks the kind of edit that has happened and updates the table when
     * necessary.
     *
     * @param e the ACMEditEvent
     */
    public void ACMEdited(ACMEditEvent e) {
        if (tier == null) {
            return;
        }

        switch (e.getOperation()) {
        case ACMEditEvent.ADD_ANNOTATION_HERE:

            if (isCreatingAnnotation) {
                // if the new annotation is created by this gridviewer return
                isCreatingAnnotation = false;

                return;
            }

        // fallthrough
        case ACMEditEvent.ADD_ANNOTATION_BEFORE:

        // fallthrough
        case ACMEditEvent.ADD_ANNOTATION_AFTER:

        // fallthrough  
        case ACMEditEvent.CHANGE_ANNOTATION_TIME:

            // TierImpl invTier = (TierImpl) e.getInvalidatedObject();
            // annotationsChanged(invTier);
            // jul 2004: redo all; we can not rely on the fact that only
            // dependent
            // tiers will be effected by this operation...
            // (problem: unaligned annotations on time-subdivision tiers)
            annotationsChanged(null);

            break;

        case ACMEditEvent.CHANGE_ANNOTATIONS:

        // fallthrough
        case ACMEditEvent.REMOVE_ANNOTATION:

            // it is not possible to determine what tiers have been effected
            // update the whole data model
            annotationsChanged(null);

            break;

        case ACMEditEvent.CHANGE_TIER:

            // a tier is invalidated the kind of change is unknown
            TierImpl invalTier = (TierImpl) e.getInvalidatedObject();
            tierChanged(invalTier);

            break;

        case ACMEditEvent.ADD_TIER:

        // fallthrough
        case ACMEditEvent.REMOVE_TIER:

            TierImpl ti = (TierImpl) e.getModification();
            tierChanged(ti);

            break;

        default:
            super.ACMEdited(e);
        }
    }

    /**
     * If a change in the specified tier could have effected any of the tiers
     * in the table rebuild the table data model entirely. The change could be
     * a change in the name, or in the tier hierarchy or whatever.
     *
     * @param changedTier the invalidated tier
     */
    private void tierChanged(TierImpl changedTier) {
        if (mode == SINGLE_TIER_MODE) {
            if (changedTier == tier) {
                setTier(changedTier);
            }
        } else {
            setTier(tier);
        }
    }

    /**
     * Sets the tier to the selected tier in the combobox.
     *
     * @param tier the current tier for the grid/table
     */
    public void setTier(Tier tier) {
        // stop editing
        gridEditor.cancelCellEditing();

        this.tier = (TierImpl) tier;

        // added by AR
        if (tier == null) {
            updateDataModel(new ArrayList());
            table.setFontsForTiers(null);
            setPreference("GridViewer.TierName", tier, 
            		getViewerManager().getTranscription());
        } else {
            List annotations = null;

            try {
                annotations = this.tier.getAnnotations();
            } catch (Exception ex) {
                LOG.warning("Could not get the annotations: " + ex.getMessage());
            }

            updateDataModel(annotations);

          //mod. Coralie Villes add the possibility of showing associated tiers or subdivised tiers
            if (mode == MULTI_TIER_ASSOCIATION_MODE) {
            	extractChildTiers(this.tier, Constraint.SYMBOLIC_ASSOCIATION);
                addExtraColumns();
            }
            if (mode == MULTI_TIER_SUBDIVISION_MODE) {
                extractChildTiers(this.tier, Constraint.SYMBOLIC_SUBDIVISION);
                addExtraColumns();
            }
            
            setPreference("GridViewer.TierName", tier.getName(), 
            		getViewerManager().getTranscription());
            preferencesChanged();
        }

        updateSelection();
        doUpdate();
    }

    /**
     * In multi tier mode finds those child tiers of the current tier that have
     * a LinguisticType that answer to the Constraint given in parameter. These tiers
     * appear in the table as an extra column.
     *
     * @param tier current tier
     * @param int constraint linguistic type of the tier (symbolic_association or symbolic_subdivision)
     */
    protected void extractChildTiers(TierImpl tier, int constraint) {
    	if (tier != null) {
        	List depTiers=tier.getDependentTiers();
            Iterator tierIt = depTiers.iterator();
            TierImpl t;

            while (tierIt.hasNext()) {
                t = (TierImpl) tierIt.next();

                if (t.getParentTier() == tier) {
                    if (t.getLinguisticType().getConstraints().getStereoType() == constraint) {
                        childTiers.add(t);
                        childTierNames.put(t, t.getName());
                        extractChildTiers(t, constraint);
                    }
                }
            }
        }
    }

    /**
     * Update method from ActiveAnnotationUser.
     */
    public void updateActiveAnnotation() {
        if (tier == null) {
            return;
        }

        if (getActiveAnnotation() == null) {
            repaint();

            return;
        }

        if (mode == SINGLE_TIER_MODE) {
            super.updateActiveAnnotation();
        } else {
            if ((getActiveAnnotation().getTier() != tier) &&
                    !childTiers.contains(getActiveAnnotation().getTier())) {
                repaint();

                return;
            }
        }

        doUpdate();
    }

    /**
     * DOCUMENT ME!
     */
    protected void addExtraColumns() {
        if (childTiers.size() == 0) {
            return;
        }

        Tier tierChild = null;

        // update vector with extra columns
        int vecChildren_size = childTiers.size();

        for (int i = 0; i < vecChildren_size; i++) {
            tierChild = (Tier) childTiers.get(i);
            handleExtraColumn(tierChild);
        }
    }

    private void handleExtraColumn(Tier childTier) {
        try {
            List v = null;
            if (mode == MULTI_TIER_ASSOCIATION_MODE) {
            	v = createChildAnnotationVector(childTier);
            } else if (mode == MULTI_TIER_SUBDIVISION_MODE) {
            	v = createChildAnnotationVectorS(childTier);
            }
            String name = childTier.getName();
            dataModel.addChildTier(name, v);

            int columnIndex = dataModel.findColumn(name);
            TableColumn tc = new TableColumn();
            tc.setHeaderValue(name);
            tc.setIdentifier(name);
            table.addColumn(tc);

            int curIndex = table.getColumnModel().getColumnIndex(name);
            table.moveColumn(curIndex, columnIndex);
            updateColumnModelIndices();
            table.setColumnVisible(name, true);
        } catch (Exception ex) {
            LOG.warning("Could not handle the extra column for the child tier: " + ex.getMessage());
        }
    }

    /**
     * Fills a Vector for a child tier with the same size as the parent tier's
     * annotations Vector. At the indices where the childtier has no child
     * annotation an empty String is inserted.
     *
     * @param childTier the dependent tier
     * Changed : Coralie Villes to allow displaying children of subdivision tiers
     */
    private List createChildAnnotationVectorS(Tier childTier) {
    	List<AnnotationCore> cv = new ArrayList<AnnotationCore>(dataModel.getRowCount());

    	List<AnnotationCore> existingChildren = ((TierImpl) childTier).getAnnotations();
    	long begin;
    	long end;
    	int i=0;
    	// get begin and end time per row and check
    	List<AnnotationCore> annotationList = new ArrayList<AnnotationCore>();
		int k = 0;
		for (int j = 0; j < dataModel.getRowCount(); j++) {
    		AnnotationCore annotation = dataModel.getAnnotationCore(j);
    		begin = annotation.getBeginTimeBoundary();
    		end = annotation.getEndTimeBoundary();
    		
    		for (; k < existingChildren.size(); k++) {
    			AnnotationCore child = existingChildren.get(k);
    			
        		if (child.getBeginTimeBoundary() >= begin && child.getEndTimeBoundary() <= end){
        			annotationList.add(child);
        		} else if (child.getBeginTimeBoundary() >= end) {
        			if (annotationList.size() > 0) {
        				cv.add(new ConcatAnnotation(annotationList));
        				annotationList.clear();
        			} else {
        				cv.add(new AnnotationCoreImpl(EMPTY,begin, end));
        			}
        			
        			break;
        		}
    		}
    		// when reaching the last of existing children test if there is something in the list
    		// k can be == to the size as a result of the increment at the end of the block
    		if (k == existingChildren.size() && annotationList.size() > 0) {
    			cv.add(new ConcatAnnotation(annotationList));
    			annotationList.clear();// not necessary
    		}
    		if (cv.size() < j) {
    			cv.add(new AnnotationCoreImpl(EMPTY,begin, end));
    		}
		}
    	return cv;
    }
    
    /**
     * Fills a Vector for a child tier with the same size as the parent tier's
     * annotations Vector. At the indices where the childtier has no child
     * annotation an empty String is inserted.
     *
     * @param childTier the dependent tier
     *
     * @return a Vector filled with child annotations and/or empty strings
     */
    private List createChildAnnotationVector(Tier childTier) {
        List cv = new ArrayList(dataModel.getRowCount());

        List existingChildren = ((TierImpl) childTier).getAnnotations();
        AnnotationCore annotation;
        AnnotationCore childAnnotation;
        long begin;

        for (int i = 0, j = 0; i < dataModel.getRowCount(); i++) {
            annotation = dataModel.getAnnotationCore(i);
            begin = annotation.getBeginTimeBoundary();

            if (j < existingChildren.size()) {
                childAnnotation = (AnnotationCore) existingChildren.get(j);

                if (childAnnotation.getBeginTimeBoundary() == begin) {
                    cv.add(childAnnotation);
                    j++;
                } else {
                    cv.add(EMPTY);
                }
            } else {
                cv.add(EMPTY);
            }
        }

        return cv;
    }

    /**
     * Sets the edit mode. On a change of the edit mode the current visible
     * columns are stored and the previous visible columns are restored.
     *
     * @param mode the new edit mode, one of SINGLE_TIER_MODE or
     *        MULTI_TIER_MODE
     */
    public void setMode(int mode) {
        if (this.mode == mode) {
            return;
        }

        this.mode = mode;

        Set invisibleColumns = getInvisibleColumns();
        setInvisibleColumns(storedInvisibleColumns);
        storedInvisibleColumns = invisibleColumns;
        
        setPreference("GridViewer.MultiTierMode", 
        		new Boolean(mode == MULTI_TIER_ASSOCIATION_MODE || mode == MULTI_TIER_SUBDIVISION_MODE), 
        		getViewerManager().getTranscription());
        if (mode == MULTI_TIER_SUBDIVISION_MODE) {
        	setPreference("GridViewer.MultiTierMode.Subdivision", new Boolean(true), 
        			getViewerManager().getTranscription());
        } else {
        	setPreference("GridViewer.MultiTierMode.Subdivision", new Boolean(false), 
        			getViewerManager().getTranscription());
        }
    }
    
    /**
     * Returns the current display mode.
     * 
     * @return the current display mode, SINGLE_TIER_MODE, MULTI_TIER_ASSOCIATION_MODE or MULTI_TIER_SUBDIVISION_MODE
     */
    public int getMode() {
    	return mode;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected Set getInvisibleColumns() {
        Set invisibleColumns = new HashSet();
        TableColumn tc;
        invisibleColumns.clear();

        for (int i = 0; i < table.getColumnCount(); i++) {
            tc = table.getColumnModel().getColumn(i);

            if (!table.isColumnVisible((String) tc.getIdentifier())) {
                invisibleColumns.add(tc.getIdentifier());
            }
        }

        return invisibleColumns;
    }

    /**
     * DOCUMENT ME!
     *
     * @param invisibleColumns DOCUMENT ME!
     */
    protected void setInvisibleColumns(Set invisibleColumns) {
        TableColumn tc;

        for (int i = 0; i < table.getColumnCount(); i++) {
            tc = table.getColumnModel().getColumn(i);
            table.setColumnVisible(dataModel.getColumnName(i),
                !invisibleColumns.contains(tc.getIdentifier()));
        }
    }

    /**
     * Check whether the invalidated tier is displayed in the table and update
     * the table if so.
     *
     * @param invTier the invalidated tier
     */
    protected void annotationsChanged(TierImpl invTier) {
        if ((invTier == null) || (invTier == tier) ||
                invTier.getDependentTiers().contains(tier) ||
                childTiers.contains(invTier)) {
            List annotations = tier.getAnnotations();
            dataModel.updateAnnotations(annotations);

            for (int i = 0; i < childTiers.size(); i++) {
                Tier childTier = (Tier) childTiers.get(i);
                List vec = null;
                if (mode == MULTI_TIER_ASSOCIATION_MODE) {
                	vec = createChildAnnotationVector(childTier);
                } else if (mode == MULTI_TIER_SUBDIVISION_MODE) {
                	vec = createChildAnnotationVectorS(childTier);
                }
                dataModel.addChildTier(childTier.getName(), vec);
            }

            updateSelection();
            doUpdate();
        }
    }

    /**
     * DOCUMENT ME!
     */
    private void removeChildrenColumns() {
        if (childTiers.size() > 0) {
            for (int i = 0; i < childTiers.size(); i++) {
                TierImpl t = (TierImpl) childTiers.get(i);
                String columnID = (String) childTierNames.get(t);

                try {
                    table.removeColumn(table.getColumn(columnID));
                    updateColumnModelIndices();
                } catch (IllegalArgumentException iae) {
                    LOG.warning("Column not found: " + iae.getMessage());
                }
            }
        }
    }

    /**
     * When adding/removing and/or moving a table column the table column model
     * indices don't seem to be updated automatically.
     */
    private void updateColumnModelIndices() {
        Enumeration ten = table.getColumnModel().getColumns();
        TableColumn tabcol = null;
        int tableIndex;

        while (ten.hasMoreElements()) {
            tabcol = (TableColumn) ten.nextElement();
            tableIndex = table.getColumnModel().getColumnIndex(tabcol.getIdentifier());
            tabcol.setModelIndex(tableIndex);
        }
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
     * Apply fontsize (tier name and multi mode are handled by layout manager).
     * 
	 * @see mpi.eudico.client.annotator.grid.AbstractEditableGridViewer#preferencesChanged()
	 */
	public void preferencesChanged() {
		Integer fontSi = (Integer) getPreference("GridViewer.FontSize", 
				getViewerManager().getTranscription());
		if (fontSi != null) {
			setFontSize(fontSi.intValue());
		}
		// preferred fonts
		Object fo = getPreference("TierFonts", getViewerManager().getTranscription());
		if (fo instanceof HashMap && tier != null) {
			HashMap foMap = (HashMap) fo;
			HashMap gridMap = new HashMap(5);
			
			Iterator keyIt = foMap.keySet().iterator();
			String key = null;
			Font ft = null;
			
			while (keyIt.hasNext()) {
				key = (String) keyIt.next();
				ft = (Font) foMap.get(key);
				
				if (key != null && ft != null) {
					if (key.equals(tier.getName())) {
						gridMap.put(GridViewerTableModel.ANNOTATION, ft.getName());
					} 
					//else if (childTierNames.containsKey(key)) {
						gridMap.put(key, ft.getName());
					//}
				}
			}
			table.setFontsForTiers(gridMap);
		}
		// Controlled Vocabulary based colors 
		Object cvPrefObj = Preferences.get("CV.Prefs", getViewerManager().getTranscription());
		if (cvPrefObj instanceof HashMap) {// there are preferred colors, get them from the vocabularies?
			//HashMap cvPrefMap = (HashMap) cvPrefObj;
			//HashMap<String, String> tierCVMap = new HashMap<String, String>();//could maintain a tier to CV mapping
			HashMap<String, Map<String, Color>> colMap = new HashMap<String, Map<String, Color>>();
			//iterate over tiers!
			List tiers = getViewerManager().getTranscription().getTiers();
			String cvName;
			TierImpl t;
			ControlledVocabulary cv;
			CVEntry[] entries;
			// iterate over tiers, though this potentially leads to duplicates of CV entry to color mappings
			for (int i = 0; i < tiers.size(); i++) {
				t = (TierImpl) tiers.get(i);
				// could check whether the tier is in the table model
				if (t != this.tier && !childTiers.contains(t)) {
					continue;
				}
				cvName = t.getLinguisticType().getControlledVocabylaryName();
				if (cvName != null) {
					//tierCVMap.put(t.getName(), cvName);					
					cv = ((TranscriptionImpl) getViewerManager().getTranscription()).getControlledVocabulary(cvName);
					if (cv != null) {
						entries = cv.getEntries();
						HashMap<String, Color> eMap = new HashMap<String, Color>(entries.length);
						for (CVEntry cve : entries) {
							if (cve.getPrefColor() != null) {
								eMap.put(cve.getValue(), cve.getPrefColor());
							}
						}
						if (t == this.tier) {
							colMap.put(GridViewerTableModel.ANNOTATION, eMap);// used in Single tier mode
						}
						colMap.put(t.getName(), eMap);
					}
				}
			}
			table.setColorsForAnnotations(colMap);
		}
		//
        Object val = getPreference("InlineEdit.EnterCommits", null);

        if (val instanceof Boolean) {
            gridEditor.setEnterCommits(((Boolean) val).booleanValue());
        }
        
        val = Preferences.get("InlineEdit.DeselectCommits", null);

        if (val instanceof Boolean) {
            table.setDeselectCommits(((Boolean) val).booleanValue());
        }
        
        val = getPreference("GridViewer.TimeFormat", getViewerManager().getTranscription());
        
        if (val instanceof String) {
        	table.setTimeFormat((String) val);
        	
        	if (popup != null) {
        		popup.setTimeFormat((String) val);
        	}
        }
        
		doLayout();
	}

	/**
	 * The viewer is registered with the popup menu in order to be notified of 
	 * user preferences changes.
	 * 
	 * @param e the event
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("TOGGLETIMEFORMAT")) {
	        if (dataModel instanceof GridViewerTableModel) {
	            String timeFormat = ((GridViewerTableModel) dataModel).getTimeFormat();

	            if (GridViewerTableModel.HHMMSSsss.equals(timeFormat)) {
	                setPreference("GridViewer.TimeFormat",
	                    Constants.HHMMSSMS_STRING, getViewerManager().getTranscription());
	            } else if (GridViewerTableModel.MILLISECONDS.equals(timeFormat)) {
	                setPreference("GridViewer.TimeFormat",
	                    Constants.MS_STRING, getViewerManager().getTranscription());
	            } else {
	                setPreference("GridViewer.TimeFormat", null, 
	                		getViewerManager().getTranscription());
	            }
	        }
		} else if (e.getActionCommand().equals(Constants.HHMMSSMS_STRING)) {
			 setPreference("GridViewer.TimeFormat",
	                   Constants.HHMMSSMS_STRING, getViewerManager().getTranscription());
		} else if (e.getActionCommand().equals(Constants.PAL_STRING)) {
			 setPreference("GridViewer.TimeFormat",
					 "PAL", getViewerManager().getTranscription());			
		} else if (e.getActionCommand().equals(Constants.NTSC_STRING)) {
			setPreference("GridViewer.TimeFormat",
					 "NTSC", getViewerManager().getTranscription());			
		} else if (e.getActionCommand().equals(Constants.MS_STRING)) {
			setPreference("GridViewer.TimeFormat",
	                   Constants.MS_STRING, getViewerManager().getTranscription());		
		} else if (e.getActionCommand().equals(Constants.SSMS_STRING)) {
			setPreference("GridViewer.TimeFormat",
	                   Constants.SSMS_STRING, getViewerManager().getTranscription());		
		} else if (e.getActionCommand().indexOf("font") != -1) {
			setPreference("GridViewer.FontSize", new Integer(table.getFontSize()),
					getViewerManager().getTranscription());
		}
		
	}
	
//	public void editingInterrupted() {
//		isClosing();		
//	}
	
	 public void setKeyStrokesNotToBeConsumed(List<KeyStroke> ksList){
	    	gridEditor.setKeyStrokesNotToBeConsumed(ksList);
	    }
	
	public void editingCommitted() {
		 if (table != null && table.isEditing()) {
			 table.editingStopped(new ChangeEvent(this));
	     }
	}

	public void editingCancelled() {
		if (table != null && table.isEditing()) {
           table.editingCanceled(new ChangeEvent(this));
       }
	}
}
