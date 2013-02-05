package mpi.eudico.client.annotator.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ElanMediaPlayerController;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.commands.ShortcutsUtil;
import mpi.eudico.client.annotator.grid.GridViewer;
import mpi.eudico.client.annotator.gui.ResizeComponent;
import mpi.eudico.client.annotator.recognizer.gui.AbstractRecognizerPanel;
import mpi.eudico.client.annotator.recognizer.gui.AudioRecognizerPanel;
import mpi.eudico.client.annotator.recognizer.gui.VideoRecognizerPanel;
import mpi.eudico.client.annotator.viewer.AbstractViewer;
import mpi.eudico.client.annotator.viewer.InterlinearViewer;
import mpi.eudico.client.annotator.viewer.LexiconEntryViewer;
import mpi.eudico.client.annotator.viewer.MetadataViewer;
import mpi.eudico.client.annotator.viewer.MultiTierControlPanel;
import mpi.eudico.client.annotator.viewer.MultiTierViewer;
import mpi.eudico.client.annotator.viewer.SignalViewer;
import mpi.eudico.client.annotator.viewer.SingleTierViewer;
import mpi.eudico.client.annotator.viewer.SingleTierViewerPanel;
import mpi.eudico.client.annotator.viewer.SubtitleViewer;
import mpi.eudico.client.annotator.viewer.TextViewer;
import mpi.eudico.client.annotator.viewer.TimeLineViewer;
import mpi.eudico.client.annotator.viewer.TimeSeriesViewer;


/**
 * Creates annotation mode layout 
 *
 * @author Aarthy Somasundaram
 */
public class AnnotationManager implements ModeLayoutManager {	
	
    private ElanLayoutManager layoutManager;
    Container container;
    private ViewerManager2 viewerManager;  

    private boolean showTimeLineViewer;
    private boolean showInterlinearViewer;  
    
    // viewerlist that only contains detachable viewers
    List viewerList;
    private ElanMediaPlayerController mediaPlayerController;
   
    private SignalViewer signalViewer;
    JComponent signalComponent;
    private TimeLineViewer timeLineViewer;
    private InterlinearViewer interlinearViewer;
    TimeSeriesViewer timeseriesViewer;
    JComponent timeseriesComponent;
    JSplitPane wav_tsSplitPane;
    private JPanel timeLineComponent;
    private MultiTierControlPanel multiTierControlPanel;
    private JSplitPane timeLineSplitPane;
    private ResizeComponent vertMediaResizer;

    private JTabbedPane tabPane;
    private JTabbedPane leftTabPane;
    private SingleTierViewerPanel gridPanel;
    private SingleTierViewerPanel textPanel;
    private JPanel subtitlePanel;
    private JComponent lexiconPanel;
    private JComponent audioRecognizerPanel;
    private JComponent videoRecognizerPanel;
    private JPanel controlPanel;
    private JComponent metadataPanel;    
    
    private boolean mediaInCentre = false;   
    private boolean gridViewerLeft;
    private boolean textViewerLeft;
    private boolean subtitleViewerLeft;
    private boolean lexiconViewerLeft;
    private boolean audioRecognizerLeft;    
    private boolean videoRecognizerLeft;
    private boolean metaDataLeft; 
    
    private  List<String> viewerSortOrder;
    
    private boolean oneRowForVisuals = false;
    private boolean preferenceChanged = false;
    private int minTabWidth = 150;       
    private int numOfPlayers;
	private List<KeyStroke> ksNotToBeConsumed;

	public AnnotationManager(ViewerManager2 viewerManager,
			ElanLayoutManager elanLayoutManager) {
        this.viewerManager = viewerManager;
        this.layoutManager = elanLayoutManager;        
        container = layoutManager.getContainer();       
        viewerList = new ArrayList(4);
		showTimeLineViewer = true;
		viewerSortOrder = new ArrayList<String>();
	}

	public void add(Object object) {
		if(object == null){
			return;
		}
		
       if (object instanceof ElanMediaPlayerController) {
           setMediaPlayerController((ElanMediaPlayerController) object);
       } else if (object instanceof SignalViewer) {
           setSignalViewer((SignalViewer) object);
       } else if (object instanceof TimeLineViewer) {
           setTimeLineViewer((TimeLineViewer) object);
       } else if (object instanceof InterlinearViewer ) {
           setInterlinearViewer((InterlinearViewer) object);
       } else if (object instanceof GridViewer ) {
           addSingleTierViewer((SingleTierViewer) object);
       } else if (object instanceof TextViewer ) {
           addSingleTierViewer((SingleTierViewer) object);
       } else if (object instanceof SubtitleViewer) {
           addSingleTierViewer((SingleTierViewer) object);
       } else if (object instanceof mpi.eudico.p2p.CollaborationPanel) {
			addToTabPane("P2P", (Component) object);
       } else if (object instanceof TimeSeriesViewer) {
       		setTimeSeriesViewer((TimeSeriesViewer) object);
       } else if (object instanceof AudioRecognizerPanel) {
       		audioRecognizerPanel = (JComponent) object;
			addToTabPane(ElanLocale.getString("Tab.AudioRecognizer"), audioRecognizerPanel);
       } else if (object instanceof VideoRecognizerPanel) {
       		videoRecognizerPanel = (JComponent) object;
			addToTabPane(ElanLocale.getString("Tab.VideoRecognizer"), videoRecognizerPanel);
       } else if (object instanceof MetadataViewer) {
    	   metadataPanel = (JComponent) object;
    	   addToTabPane(ElanLocale.getString("Tab.Metadata"), (MetadataViewer) object);
       } else if (object instanceof LexiconEntryViewer) {  
       		lexiconPanel = (JComponent) object;
       		addToTabPane(ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER), (Component) object);
       }
	}
	
	/**
     * DOCUMENT ME!
     *
     * @param mediaPlayerController
     */
    private void setMediaPlayerController(ElanMediaPlayerController mediaPlayerController) {    	
   
        this.mediaPlayerController = mediaPlayerController;
        
        mediaPlayerController.getSliderPanel().addMouseListener(
        		mediaPlayerController.getAnnotationDensityViewer());
        
        // add the control components to the container
        container.add(mediaPlayerController.getPlayButtonsPanel());
        container.add(mediaPlayerController.getTimePanel());
        //container.add(mediaPlayerController.getDurationPanel());
        container.add(mediaPlayerController.getModePanel());
        container.add(mediaPlayerController.getSelectionPanel());
        container.add(mediaPlayerController.getSelectionButtonsPanel());
        container.add(mediaPlayerController.getAnnotationNavigationPanel());
        container.add(mediaPlayerController.getSliderPanel());
        container.add(mediaPlayerController.getAnnotationDensityViewer());  
        container.add(mediaPlayerController.getVolumeIconPanel());
        controlPanel.add(mediaPlayerController.getVolumePanel());
        controlPanel.add(mediaPlayerController.getRatePanel());          
        addToTabPane(ElanLocale.getString("Tab.Controls"), controlPanel);
    }
    
    private void removeMediaPlayerController(){
      // HS remove the density viewer as mouse listener of the slider panel
         mediaPlayerController.getSliderPanel().removeMouseListener(
         		mediaPlayerController.getAnnotationDensityViewer()); 
         
         container.remove(mediaPlayerController.getPlayButtonsPanel());
         container.remove(mediaPlayerController.getTimePanel());
         viewerManager.destroyTimePanel();
         //container.add(mediaPlayerController.getDurationPanel());
         container.remove(mediaPlayerController.getModePanel());
         container.remove(mediaPlayerController.getSelectionPanel());
         container.remove(mediaPlayerController.getSelectionButtonsPanel());
         container.remove(mediaPlayerController.getAnnotationNavigationPanel());
         container.remove(mediaPlayerController.getSliderPanel());
         viewerManager.destroyMediaPlayerControlSlider();
         container.remove(mediaPlayerController.getAnnotationDensityViewer());
         viewerManager.destroyAnnotationDensityViewer();
         container.remove(mediaPlayerController.getVolumeIconPanel());
         
         viewerManager.destroyElanMediaPlayerController();
 		 mediaPlayerController = null;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param timeLineViewer
     */
    private void setTimeLineViewer(TimeLineViewer timeLineViewer) {
        this.timeLineViewer = timeLineViewer;

        if (timeLineComponent == null) {
            timeLineComponent = new JPanel();
            timeLineComponent.setLayout(null);
        }

        if (multiTierControlPanel == null) {
            multiTierControlPanel = viewerManager.getMultiTierControlPanel();
            multiTierControlPanel.setSize(ElanLayoutManager.CONTROL_PANEL_WIDTH, ElanLayoutManager.CONTROL_PANEL_WIDTH);
            ResizeComponent mcpResize = new ResizeComponent(layoutManager, SwingConstants.HORIZONTAL, ResizeComponent.CONTROL_PANEL);
            mcpResize.setSize(8, 16);
            multiTierControlPanel.setResizeComponent(mcpResize);
            timeLineComponent.add(multiTierControlPanel);
        }

        // disable the interlinear viewer if it exists
        if (interlinearViewer != null) {
            viewerManager.disableViewer(interlinearViewer);
        }

        // place the component in the split pane
        timeLineComponent.add(timeLineViewer);
        getTimeLineSplitPane().setBottomComponent(timeLineComponent);

        if (getTimeLineSplitPane().getTopComponent() != null) {
			Integer sigHeight = (Integer) Preferences.get("LayoutManager.SplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
				getTimeLineSplitPane().setDividerLocation(sigHeight.intValue());
			} else {
				getTimeLineSplitPane().setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT);
			}
    		getTimeLineSplitPane().setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT);
        }
        
        doLayout();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param interlinearViewer
     */
    private void setInterlinearViewer(InterlinearViewer interlinearViewer) {
        this.interlinearViewer = interlinearViewer;

        if (timeLineComponent == null) {
            timeLineComponent = new JPanel();
            timeLineComponent.setLayout(null);
        }

        if (multiTierControlPanel == null) {
            multiTierControlPanel = viewerManager.getMultiTierControlPanel();
            multiTierControlPanel.setSize(ElanLayoutManager.CONTROL_PANEL_WIDTH, ElanLayoutManager.CONTROL_PANEL_WIDTH);
            ResizeComponent mcpResize = new ResizeComponent(layoutManager, SwingConstants.HORIZONTAL, ResizeComponent.CONTROL_PANEL);
            mcpResize.setSize(8, 16);
            multiTierControlPanel.setResizeComponent(mcpResize);
            timeLineComponent.add(multiTierControlPanel);
        }

        // disable the timeLine viewer if it exists
        if (timeLineViewer != null) {
            viewerManager.disableViewer(timeLineViewer);
        }

        // place the component in the split pane
        timeLineComponent.add(interlinearViewer);
        getTimeLineSplitPane().setBottomComponent(timeLineComponent);

        doLayout();
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param signalViewer
     */
    private void setSignalViewer(SignalViewer signalViewer) {
        this.signalViewer = signalViewer;
        
        if (timeseriesViewer == null) {
	        if (signalComponent == null) { // dit al voorbakken
	            signalComponent = new JPanel();
	            signalComponent.setLayout(null);
				signalComponent.addComponentListener(layoutManager.new SignalSplitPaneListener());
	        }
	
	        // place the component in the split pane
	        signalComponent.add(signalViewer);
			getTimeLineSplitPane().setTopComponent(signalComponent);
//			int divLoc = signalComponent.getHeight() < ElanLayoutManager.DEF_SIGNAL_HEIGHT ? 
//					ElanLayoutManager.DEF_SIGNAL_HEIGHT : signalComponent.getHeight();
			Integer sigHeight = (Integer) Preferences.get("LayoutManager.SplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
				timeLineSplitPane.setDividerLocation(sigHeight.intValue());
			} else {
				timeLineSplitPane.setDividerLocation(ElanLayoutManager.DEF_SIGNAL_HEIGHT);
			}
//	        timeLineSplitPane.setDividerLocation(divLoc);
        } else {
        	// check the attached/detached state of timeseries viewer
        	/*
			ComponentListener[] cListeners = timeseriesComponent.getComponentListeners();
			for (int i = 0; i < cListeners.length; i++) {
				if (cListeners[i] instanceof SignalSplitPaneListener) {
					timeseriesComponent.removeComponentListener(cListeners[i]);	
				}				
			}
			*/
			int curHeight = timeseriesComponent.getHeight();
			getTimeLineSplitPane().setTopComponent(null);
			
            signalComponent = new JPanel();
            signalComponent.setLayout(null);
            signalComponent.add(signalViewer);
            
			wav_tsSplitPane = getWav_TSSplitPane();
			wav_tsSplitPane.setTopComponent(timeseriesComponent);
			wav_tsSplitPane.setBottomComponent(signalComponent);
			wav_tsSplitPane.setDividerLocation(curHeight / 2);
			getTimeLineSplitPane().setTopComponent(wav_tsSplitPane);
			//timeseriesComponent.addComponentListener(new SignalSplitPaneListener());// has already one
			timeLineComponent.addComponentListener(layoutManager.new SignalSplitPaneListener());
			getTimeLineSplitPane().setDividerLocation(curHeight);
			Integer divLoc = (Integer) Preferences.get("LayoutManager.TSWavSplitPaneDividerLocation", 
					viewerManager.getTranscription());
			Integer spliDivLoc = (Integer) Preferences.get("LayoutManager.SplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (divLoc != null && spliDivLoc != null ) {
				if(wav_tsSplitPane != null){
					wav_tsSplitPane.setDividerLocation(divLoc.intValue());
				}				
				getTimeLineSplitPane().setDividerLocation(spliDivLoc.intValue());
			}
        }

        doLayout();
    }
    
    /**
	 * Sets the one timeseries viewer. This has to be changed if there is a need
	 * for more than one timeseries viewer.
	 * Reused for attaching an existing, previously detached, timeseries viewer.
	 *  
	 * @param viewer the timeseries viewer
	 */
	void setTimeSeriesViewer(TimeSeriesViewer timeseriesViewer) {
		if (this.timeseriesViewer == null) {
			this.timeseriesViewer = timeseriesViewer;

			ViewerLayoutModel vlm = new ViewerLayoutModel(timeseriesViewer, layoutManager);
			viewerList.add(vlm);
			
			boolean detached = false;
			Boolean detObj = (Boolean) Preferences.get("TimeSeriesViewer.Detached", 
					viewerManager.getTranscription());
			if(detObj != null) {
				detached = detObj.booleanValue();
			}
			
			if (detached) {
				vlm.detach();
				timeseriesViewer.setAttached(false);
				return;
			}
		}	
		
		if (signalViewer == null) {
			timeseriesComponent = new JPanel();
			timeseriesComponent.setLayout(null);
			timeseriesComponent.addComponentListener(layoutManager.new SignalSplitPaneListener());
		
	        // place the component in the outer split pane
			timeseriesComponent.add(timeseriesViewer);
			getTimeLineSplitPane().setTopComponent(timeseriesComponent);
			int divLoc = timeseriesComponent.getHeight() < ElanLayoutManager.DEF_SIGNAL_HEIGHT ? 
					ElanLayoutManager.DEF_SIGNAL_HEIGHT : timeseriesComponent.getHeight();
			
			Integer sigHeight = (Integer) Preferences.get("LayoutManager.SplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
				divLoc = sigHeight.intValue();
			}
	        timeLineSplitPane.setDividerLocation(divLoc);
	        timeseriesViewer.setAttached(true);
			if (multiTierControlPanel != null) {
				timeseriesViewer.setVerticalRulerWidth(multiTierControlPanel.getWidth());
			}
		} else {	
			ComponentListener[] cListeners = signalComponent.getComponentListeners();
			for (int i = 0; i < cListeners.length; i++) {
				if (cListeners[i] instanceof ElanLayoutManager.SignalSplitPaneListener) {
					signalComponent.removeComponentListener(cListeners[i]);	
				}				
			}
			
			int curHeight = getTimeLineSplitPane().getDividerLocation();
			timeseriesComponent = new JPanel();
			timeseriesComponent.setLayout(null);
			timeseriesComponent.add(timeseriesViewer);
			getTimeLineSplitPane().setTopComponent(null);
			
			wav_tsSplitPane = getWav_TSSplitPane();
			wav_tsSplitPane.setTopComponent(timeseriesComponent);
			wav_tsSplitPane.setBottomComponent(signalComponent);
			wav_tsSplitPane.setDividerLocation(curHeight / 2);
			
			getTimeLineSplitPane().setTopComponent(wav_tsSplitPane);
			getTimeLineSplitPane().setDividerLocation(curHeight);
			timeseriesComponent.addComponentListener(layoutManager.new SignalSplitPaneListener());
			timeLineComponent.addComponentListener(layoutManager.new SignalSplitPaneListener());
			timeseriesViewer.setAttached(true);
			if (multiTierControlPanel != null) {
				timeseriesViewer.setVerticalRulerWidth(multiTierControlPanel.getWidth());
				timeseriesViewer.setBounds(timeseriesComponent.getBounds());
			}
			
			Integer divLoc = (Integer) Preferences.get("LayoutManager.TSWavSplitPaneDividerLocation", 
					viewerManager.getTranscription());
			if (divLoc != null && wav_tsSplitPane != null) {
				if (divLoc.intValue() < curHeight - 20) {
					wav_tsSplitPane.setDividerLocation(divLoc.intValue());
				}
			}
		}
		
        doLayout();
	}
    
    private void addToTabPane(String tabName, Component component) {  
    	getTabPane().insertTab(tabName, null, component, tabName, getIndexOfComponent(getTabPane(), component));   	    	
    	preferenceChanged = true;
    	doLayout();
    }
    
    private void addSingleTierViewer(SingleTierViewer viewer) {
        SingleTierViewerPanel panel = viewerManager.createSingleTierViewerPanel();
        panel.setViewer(viewer);

        if (viewer instanceof GridViewer) {
            gridPanel = panel;
            addToTabPane(ElanLocale.getString("Tab.Grid"), panel);
        } else if (viewer instanceof TextViewer) {
            textPanel = panel;
            addToTabPane(ElanLocale.getString("Tab.Text"), panel);
        } else if (viewer instanceof SubtitleViewer) {        	
            getSubtitlePanel().add(panel);
        }

        doLayout();
    }
    
    /**
	 * Remove an object from the layout.
	 *
	 * @param object
	 */
	public void remove(Object object) {
		if(object == null){
			return;
		}
		if (object instanceof SignalViewer) {
			removeSignalViewer();
		} else if (object instanceof TimeSeriesViewer) {
			removeTimeSeriesViewer();
		}else if (object instanceof mpi.eudico.p2p.CollaborationPanel) {
			removeFromTabPane((Component) object);
		} else if (object instanceof AudioRecognizerPanel) {
			removeFromTabPane((Component) object);
		} else if (object instanceof VideoRecognizerPanel) {
			removeFromTabPane((Component) object);
		} else if (object instanceof MetadataViewer) {
			removeFromTabPane((Component) object);
		} else if (object instanceof GridViewer) {
			removeFromTabPane(gridPanel);
		} else if (object instanceof TextViewer) {
			removeFromTabPane(textPanel);
		} else if (object instanceof SubtitleViewer) {
			removeFromTabPane(subtitlePanel);
			//subtitlePanel = null;
		} 
        else if (object instanceof LexiconEntryViewer) {
        	removeFromTabPane((Component) object);
        }
	}
    
    private void removeFromTabPane(Component component) {
    	if(component == null){
    		return;
    	}
		if(getTabPane().indexOfComponent(component) >= 0){
			getTabPane().remove(component);
		} else if(getLeftTabPane().indexOfComponent(component) >= 0){
			getLeftTabPane().remove(component);
		}
	}
    
    /**
	 * Removes the SignalViewer from the layout.
	 */
	private void removeSignalViewer() {		
		if (signalViewer != null) {		
			 if(timeLineSplitPane!= null){
					layoutManager.setPreference("LayoutManager.SplitPaneDividerLocation", timeLineSplitPane.getDividerLocation() , viewerManager.getTranscription());
				}
			 
			if (signalComponent != null) {			
				if (wav_tsSplitPane != null && wav_tsSplitPane.getBottomComponent() == signalComponent) {
					Component topComp = wav_tsSplitPane.getTopComponent();
					wav_tsSplitPane.setTopComponent(null);
					wav_tsSplitPane.setBottomComponent(null);
					
					getTimeLineSplitPane().setTopComponent(topComp);
					signalComponent.remove(signalViewer);
					signalViewer = null;
					signalComponent = null;					
				} else {
					signalComponent.remove(signalViewer);
					getTimeLineSplitPane().setTopComponent(null);
					//timeLineSplitPane.setDividerLocation(0);						
					signalViewer = null;
					signalComponent = null;
				}
			}
		}
	}
	
	/**
	 * Removes the timeseries viewer from the viewer list and from the layout.
	 */
	private void removeTimeSeriesViewer() {
		if (timeseriesViewer != null) {
			 if(timeLineSplitPane!= null){
					layoutManager.setPreference("LayoutManager.SplitPaneDividerLocation", timeLineSplitPane.getDividerLocation() , viewerManager.getTranscription());
				}
			ViewerLayoutModel vlm = null;
			for (int i = 0; i < viewerList.size(); i++) {
				vlm = (ViewerLayoutModel) viewerList.get(i);
				if (vlm.viewer == timeseriesViewer) {
					break;
				} else {
					vlm = null;
				}
			}
			
			if (vlm == null) {
				return;
			}
			
			if (vlm.isAttached()) {
				if (wav_tsSplitPane != null) {
    				int curHeight = getTimeLineSplitPane().getDividerLocation();
    				wav_tsSplitPane.setTopComponent(null);
    				wav_tsSplitPane.setBottomComponent(null);
    				timeseriesComponent.remove(timeseriesViewer);
    				timeseriesComponent = null;
    				wav_tsSplitPane = null;
    				
    				if (signalComponent != null) {
    					getTimeLineSplitPane().setTopComponent(signalComponent);
    					getTimeLineSplitPane().setDividerLocation(curHeight);
    					signalComponent.setSize(signalComponent.getWidth(), curHeight);
    				} else {
    					getTimeLineSplitPane().setTopComponent(null);
    				}   				
    			} else {
    				getTimeLineSplitPane().setTopComponent(null);
    				if (timeseriesComponent != null) {
	    				timeseriesComponent.remove(timeseriesViewer);
	    				timeseriesComponent = null;
    				}
    			}
				timeseriesViewer = null;
				doLayout();
			} else {
				vlm.attach();//destroys frame
				viewerList.remove(vlm);
				if(timeseriesComponent != null) {
					timeseriesComponent.remove(vlm.viewer);
					timeseriesComponent = null;
				}
				timeseriesViewer = null;
			}			
		}
		
	}
    
    private JTabbedPane getTabPane() {
        if (tabPane == null) {
            tabPane = new JTabbedPane();            
            container.add(tabPane);
        } 
        return tabPane;
    }
    
    private JTabbedPane getLeftTabPane() {
        if (leftTabPane == null) {
        	leftTabPane = new JTabbedPane();           	
            container.add(leftTabPane);
        }

        return leftTabPane;
    }
    
    /**
     * destroys the left tabpane
     */
    private void destroyLeftPane(){
    	if(leftTabPane != null){
    		container.remove(leftTabPane);
    		leftTabPane = null;
    	}    	
    }
    
	/**
	 * Destroy an object from the layout.
	 *
	 * @param object
	 */
	public boolean destroyAndRemoveViewer(String viewerName) {	
		boolean doLayout  = false;
		
		if(viewerName == null){
			return doLayout;
		}
		
		if (viewerName.equals(ELANCommandFactory.AUDIO_RECOGNIZER)) {
			if(audioRecognizerPanel != null){
				removeFromTabPane(audioRecognizerPanel);				
				audioRecognizerPanel = null;
				
				doLayout = true;
			}
			viewerManager.destroyPanel(ELANCommandFactory.AUDIO_RECOGNIZER);		
		} 
		else if (viewerName.equals(ELANCommandFactory.VIDEO_RECOGNIZER)) {
			if(videoRecognizerPanel != null){
				removeFromTabPane(videoRecognizerPanel);				
				videoRecognizerPanel = null;	
				doLayout = true;
			}			
			viewerManager.destroyPanel(ELANCommandFactory.VIDEO_RECOGNIZER);	
		} 
		else if (viewerName.equals(ELANCommandFactory.METADATA_VIEWER)) {
			if(metadataPanel != null){
				removeFromTabPane(metadataPanel);
				metadataPanel = null;				
				doLayout = true;
			}					
			viewerManager.destroyMetaDataViewer();
		} 
		else if (viewerName.equals(ELANCommandFactory.GRID_VIEWER)) {
			if(gridPanel != null){
				removeFromTabPane(gridPanel);
				((GridViewer)gridPanel.getViewer()).isClosing();
				viewerManager.destroySingleTierViewerPanel(gridPanel);				
				gridPanel = null;
				doLayout = true;
			}
			viewerManager.destroyGridViewer(); 
		} 
		else if (viewerName.equals(ELANCommandFactory.TEXT_VIEWER)) {
			if(textPanel != null){
				removeFromTabPane(textPanel);
				viewerManager.destroySingleTierViewerPanel((SingleTierViewerPanel) textPanel);				
				textPanel = null;
				doLayout = true;
			}
			viewerManager.destroyTextViewer(); 
		} 
		else if (viewerName.equals(ELANCommandFactory.SUBTITLE_VIEWER)) {
			if(subtitlePanel != null){
				removeFromTabPane(subtitlePanel);
				Component panel[] = subtitlePanel.getComponents();
				for(int i=0; i < panel.length; i++){
					if(panel[i] instanceof SingleTierViewerPanel){
						((SubtitleViewer)((SingleTierViewerPanel)panel[i]).getViewer()).isClosing();
						viewerManager.destroySingleTierViewerPanel((SingleTierViewerPanel) panel[i]);
					}
				}				
				subtitlePanel = null;
				doLayout = true;
			}			
			viewerManager.destroySubtitleViewers(); 
		}
		else if (viewerName.equals(ELANCommandFactory.INTERLINEAR_VIEWER)) {
			if(interlinearViewer != null){
				remove(interlinearViewer);
				interlinearViewer.isClosing();
				if(layoutManager.getMode() == ElanLayoutManager.NORMAL_MODE){
					showTimeLineViewer();
				}
				interlinearViewer = null;	
				doLayout = true;
			}	
			viewerManager.destroyInterlinearViewer(); 
			multiTierControlPanel.disableShowInterlinearViewer(true);			
		} 
		else if (viewerName.equals(ELANCommandFactory.LEXICON_VIEWER)) {
			if(lexiconPanel != null){
				removeFromTabPane(lexiconPanel);
				lexiconPanel = null;	
				doLayout = true;
			}	
					
			viewerManager.destroyLexiconViewer();
		}
		else if (viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)) {
			if(signalViewer != null){
				removeSignalViewer();
				doLayout = true;
			}
			viewerManager.destroySignalViewer();			
		}
		else if (viewerName.equals(ELANCommandFactory.TIMESERIES_VIEWER)) {
			if(timeseriesViewer != null){
				layoutManager.remove(layoutManager.getTimeSeriesViewer());
				doLayout = true;
				viewerManager.connectViewer(timeseriesViewer, false);
			}
		}		
		return doLayout;
	}
	
	
	public void createAndAddViewer(String viewerName) {	
		if(viewerName == null){
			return;
		}
		
		if (viewerName.equals(ELANCommandFactory.AUDIO_RECOGNIZER)) {
				add(viewerManager.createAudioRecognizerPanel());
		} 
		else if (viewerName.equals(ELANCommandFactory.VIDEO_RECOGNIZER)) {
			add(viewerManager.createVideoRecognizerPanel());  
		}
		else if (viewerName.equals(ELANCommandFactory.METADATA_VIEWER)) {
			add(viewerManager.createMetadataViewer());			
		} 
		else if (viewerName.equals(ELANCommandFactory.GRID_VIEWER)) {
			add(viewerManager.createGridViewer());	
			if(viewerManager.getGridViewer() != null){
				viewerManager.getGridViewer().setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
			}
		} 
		else if (viewerName.equals(ELANCommandFactory.TEXT_VIEWER)) {
			add(viewerManager.createTextViewer());
		} 
		else if (viewerName.equals(ELANCommandFactory.SUBTITLE_VIEWER)) {
			createSubtitleViewer();
		} 		
		else if (viewerName.equals(ELANCommandFactory.INTERLINEAR_VIEWER)) {
			 add(viewerManager.createInterlinearViewer()); 
			 if(interlinearViewer != null){
				 interlinearViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
				 multiTierControlPanel.disableShowInterlinearViewer(false);
				 showTimeLineViewer();
			 }
		} else if (viewerName.equals(ELANCommandFactory.LEXICON_VIEWER)) {
			add(viewerManager.createLexiconEntryViewer());
		} 
		else if (viewerName.equals(ELANCommandFactory.SIGNAL_VIEWER)) {
			layoutManager.add(viewerManager.createSignalViewer());
		}
		else if (viewerName.equals(ELANCommandFactory.TIMESERIES_VIEWER)) {
			Object val = Preferences.get(ELANCommandFactory.TIMESERIES_VIEWER, null);
			if(val == null || (Boolean)val){
				add(layoutManager.getTimeSeriesViewer());
				if(timeseriesViewer !=null){
					viewerManager.connectViewer(timeseriesViewer, true);
				}
			}
		}
		
	}
    
    private JPanel getSubtitlePanel() {
        if (subtitlePanel == null) {
            subtitlePanel = new JPanel(new GridLayout(0, 1));           
            addToTabPane(ElanLocale.getString("Tab.Subtitles"), subtitlePanel);
        }

        return subtitlePanel;
    }
    
    private JSplitPane getTimeLineSplitPane() {
        if (timeLineSplitPane == null) {
            timeLineSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            timeLineSplitPane.setOneTouchExpandable(true);

            // HS 24 nov set the divider location when a top component is added
            timeLineSplitPane.setDividerLocation(0);
            timeLineSplitPane.setContinuousLayout(true);
            container.add(timeLineSplitPane);
        }

        return timeLineSplitPane;
    }
    
    private JSplitPane getWav_TSSplitPane() {
        if (wav_tsSplitPane == null) {
        	wav_tsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        	wav_tsSplitPane.setOneTouchExpandable(true);
        	wav_tsSplitPane.setBorder(null);

            //set the divider location when a top component is added
        	wav_tsSplitPane.setDividerLocation(0);
        	wav_tsSplitPane.setDividerSize(6);
        	wav_tsSplitPane.setContinuousLayout(true);
        }

        return wav_tsSplitPane;
    }
    
    /**
	 * Returns the TimeLineViewer.
	 * 
	 * @return the TimeLineViewer, can be null
	 */
	public TimeLineViewer getTimeLineViewer() {
		return timeLineViewer;
	}
	
	/**
	 * Returns the InterlinearViewer.
	 * 
	 * @return the InterlinearViewer, can be null
	 */
	public InterlinearViewer getInterlinearViewer() {
		return interlinearViewer;
	}
	
	/**
	 * Returns the TimeSeries Viewer or null.
	 * 
	 * @return the TimeSeries Viewer
	 */
	public TimeSeriesViewer getTimeSeriesViewer() {
		return timeseriesViewer;
	}
	
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public MultiTierControlPanel getMultiTierControlPanel() {
        return multiTierControlPanel;
    }
    
    /**
     * Returns the current visible MultiTierViewer.<br>
     * Note: maybe should return null if in synchronization mode.
     * 
     * @return the timeline viewer or interlinear viewer
     */
    public MultiTierViewer getVisibleMultiTierViewer() {
    	if (showTimeLineViewer) {
    		return timeLineViewer;
    	} else {
    		return interlinearViewer;
    	}
    }
    
    /**
     * returns the index of the give component in the tabpane
     * 
     * @param pane 	     pane in which the component index is needed
     * @param component  the component for which the index is needed
     * @return
     */
    private int getIndexOfComponent(JTabbedPane pane, Component component){   
    	int index =0;
    	if(viewerSortOrder != null && viewerSortOrder.size() > 0){    		
        	for (int i=0; i< viewerSortOrder.size(); i++){
        		if (component == controlPanel) {
            		return pane.getTabCount();
            	}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.GRID_VIEWER))){
        			if(component == gridPanel){
            			break;
            		}else if(pane.indexOfComponent(gridPanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.TEXT_VIEWER))){
        			if(component == textPanel){
            			break;
            		}else if(pane.indexOfComponent(textPanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.SUBTITLE_VIEWER))){
        			if(component == subtitlePanel){
            			break;
            		}else if(pane.indexOfComponent(subtitlePanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER))){
        			if(component == lexiconPanel){
            			break;
            		}else if(pane.indexOfComponent(lexiconPanel) >= 0){
            			index++;
            		}
        		}
        		
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.AUDIO_RECOGNIZER))){
        			if(component == audioRecognizerPanel){
            			break;
            		}else if(pane.indexOfComponent(audioRecognizerPanel) >= 0){
            			index++;
            		}
        		}
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.VIDEO_RECOGNIZER))){
        			if(component == videoRecognizerPanel){
            			break;
            		}else if(pane.indexOfComponent(videoRecognizerPanel) >= 0){
            			index++;
            		}
        		}
        		if(viewerSortOrder.get(i).equalsIgnoreCase(ElanLocale.getString(ELANCommandFactory.METADATA_VIEWER))){
        			if(pane.indexOfComponent(metadataPanel) >= 0){
        				index++;
            		}else if(component == metadataPanel){
            			break;
            			
            		}
        		}        		
        	}
        	
        } else {
        	if(component == gridPanel){
        		return index;  
        	}else{
        		index = pane.indexOfComponent(gridPanel);
            	if(index != 0 ){
            		index = 0; 
            	}
            	else
            		index = 1; 
        	}
        	
        	if(component == textPanel){    		
        		return index;    		
        	}
        	
        	if(component == subtitlePanel){    
        		int i = pane.indexOfComponent(textPanel);
        		if(i >= 0){
        			return i + 1;   
        		}else{
        			return index; 
        		}    		 		
        	}
        	
        	if(component == lexiconPanel){    
        		int i = pane.indexOfComponent(subtitlePanel);
        		if(i >= 0){
        			return i + 1;   
        		}else{
        			return index; 
        		}    		 		
        	}
        	
        	if(component == audioRecognizerPanel){    
        		int i = pane.indexOfComponent(lexiconPanel);
        		if(i >= 0){
        			return i + 1;      
        		}else{
        			i = pane.indexOfComponent(subtitlePanel);
        			if(i >= 0){
        				return i + 1;     
        			} else {
        				return index;   
        			}
        		}    		 		
        	}
        	
        	if(component == videoRecognizerPanel){  
        		int i = pane.indexOfComponent(metadataPanel);
        		if(i >= 0){
        			return i;  
        		}else {
        			i = pane.indexOfComponent(controlPanel);
            		if(i >= 0){
            			return i;  
            		}
            		else{        			
            			return pane.getTabCount();         			
            		}
        		}    		 		
        	}
        	
        	if (component == metadataPanel) {
        		int i = pane.indexOfComponent(controlPanel);
        		if(i >= 0){
        			return i;
        		} else {
        			return pane.getTabCount();
        		}
        	}
        	
        	if (component == controlPanel) {
        		return pane.getTabCount();
        	}
    	}
        	
        return index;
    }
   
    /**
     * ReArranges the viewers in the left and right tabpane
     * according to the changed settings
     */
    private void reArrangeViewers() {  
    	if(mediaInCentre ){//&& layoutManager.getAttachedVisualPlayers().length > 0){ 
    		int selectedIndexLeftPane = 0;
    		int selectedIndexRightPane= 0; 
    		Component selectedLeftComponent = null;
    		Component selectedRightComponent = null;
    		
    		if(getLeftTabPane().getTabCount() > 0){
    			selectedIndexLeftPane = getLeftTabPane().getSelectedIndex();
    			selectedLeftComponent = getLeftTabPane().getSelectedComponent();
    		}
    		if(getTabPane().getTabCount() > 0){
    			selectedIndexRightPane = getTabPane().getSelectedIndex();
    			selectedRightComponent = getTabPane().getSelectedComponent();
    		}    		
    		
    		if( gridPanel != null){
    			String tabName = ElanLocale.getString("Tab.Grid");
    			if(gridViewerLeft ){
    				if(getTabPane().indexOfComponent(gridPanel) >=0 ){
    					getTabPane().remove(gridPanel);    						
    				} 
    				if( getLeftTabPane().indexOfComponent(gridPanel) < 0 ){    						
        				getLeftTabPane().insertTab(tabName, null, gridPanel, tabName, getIndexOfComponent(getLeftTabPane(), gridPanel));         				
    				} else {    					
    					getLeftTabPane().remove(gridPanel);
    					getLeftTabPane().insertTab(tabName, null, gridPanel, tabName, getIndexOfComponent(getLeftTabPane(), gridPanel));    
    				}
    			} else{
    				if(getLeftTabPane().indexOfComponent(gridPanel) >=0 ){
    					getLeftTabPane().remove(gridPanel);    						
        			} 
        			if( getTabPane().indexOfComponent(gridPanel) < 0 ){    						
        				getTabPane().insertTab(tabName, null, gridPanel, tabName, getIndexOfComponent(getTabPane(), gridPanel));  
        			}     				
    			}
    		} 
    		
    		if(textPanel != null){
    			String tabName = ElanLocale.getString("Tab.Text");
    			if(textViewerLeft){
    				if(getTabPane().indexOfComponent(textPanel) >=0 ){
        				getTabPane().remove(textPanel);
        			}    					
    				if ( getLeftTabPane().indexOfComponent(textPanel) < 0 ){        					
        				getLeftTabPane().insertTab(tabName, null, textPanel, tabName, getIndexOfComponent(getLeftTabPane(), textPanel)); 
    				}  else {    					
    					getLeftTabPane().remove(textPanel);
    					getLeftTabPane().insertTab(tabName, null, textPanel, tabName, getIndexOfComponent(getLeftTabPane(), textPanel));    
    				}
    			} else{
    				if(getLeftTabPane().indexOfComponent(textPanel) >=0 ){
    					getLeftTabPane().remove(textPanel);
        			}
    				if ( getTabPane().indexOfComponent(textPanel) < 0 ){        					
        				getTabPane().insertTab(tabName, null, textPanel, tabName, getIndexOfComponent(getTabPane(), textPanel)); 
        			}    					
    			}    				    			
    		}
    			
    		if(subtitlePanel != null){
    			String tabName = ElanLocale.getString("Tab.Subtitles");
    			if(subtitleViewerLeft){    					
        			if(getTabPane().indexOfComponent(subtitlePanel) >=0 ){
            			getTabPane().remove(subtitlePanel);
            		}    					
        			if ( getLeftTabPane().indexOfComponent(subtitlePanel) < 0 ){        					
            			getLeftTabPane().insertTab(tabName, null, subtitlePanel, tabName, getIndexOfComponent(getLeftTabPane(), subtitlePanel));  
            		}  else {    					
    					getLeftTabPane().remove(subtitlePanel);
    					getLeftTabPane().insertTab(tabName, null, subtitlePanel, tabName, getIndexOfComponent(getLeftTabPane(), subtitlePanel));    
    				}
    			} else {
        			if(getLeftTabPane().indexOfComponent(subtitlePanel) >=0 ){
        				getLeftTabPane().remove(subtitlePanel);
            		}
        			if ( getTabPane().indexOfComponent(subtitlePanel) < 0 ){        					
            			getTabPane().insertTab(tabName, null, subtitlePanel, tabName, getIndexOfComponent(getTabPane(), subtitlePanel)); 
            		}    					
        		}     			
    		} 
    		
    		if(lexiconPanel != null){
    			String tabName = ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER);
    			if(lexiconViewerLeft){    					
        			if(getTabPane().indexOfComponent(lexiconPanel) >=0 ){
            			getTabPane().remove(lexiconPanel);
            		}    					
        			if ( getLeftTabPane().indexOfComponent(lexiconPanel) < 0 ){        					
            			getLeftTabPane().insertTab(tabName, null, lexiconPanel, tabName, getIndexOfComponent(getLeftTabPane(), lexiconPanel));  
            		}  else {    					
    					getLeftTabPane().remove(lexiconPanel);
    					getLeftTabPane().insertTab(tabName, null, lexiconPanel, tabName, getIndexOfComponent(getLeftTabPane(), lexiconPanel));    
    				}
    			} else {
        			if(getLeftTabPane().indexOfComponent(lexiconPanel) >=0 ){
        				getLeftTabPane().remove(lexiconPanel);
            		}
        			if ( getTabPane().indexOfComponent(lexiconPanel) < 0 ){        					
            			getTabPane().insertTab(tabName, null, lexiconPanel, tabName, getIndexOfComponent(getTabPane(), lexiconPanel)); 
            		}    					
        		}     			
    		} 
    			
    		if(audioRecognizerPanel != null){
    			String tabName = ElanLocale.getString("Tab.AudioRecognizer");
    			if(audioRecognizerLeft){    					
        			if(getTabPane().indexOfComponent(audioRecognizerPanel) >=0 ){
            			getTabPane().remove(audioRecognizerPanel);
            		}    					
        			if ( getLeftTabPane().indexOfComponent(audioRecognizerPanel) < 0 ){        					
            			getLeftTabPane().insertTab(tabName, null, audioRecognizerPanel, tabName, getIndexOfComponent(getLeftTabPane(), audioRecognizerPanel));  
            		}  else {    					
        				getLeftTabPane().remove(audioRecognizerPanel);
        				getLeftTabPane().insertTab(tabName, null, audioRecognizerPanel, tabName, getIndexOfComponent(getLeftTabPane(), audioRecognizerPanel));    
        			}
        		} else {
        			if(getLeftTabPane().indexOfComponent(audioRecognizerPanel) >=0 ){
        				getLeftTabPane().remove(audioRecognizerPanel);
            		}
        			if ( getTabPane().indexOfComponent(audioRecognizerPanel) < 0 ){        					
            			getTabPane().insertTab(tabName, null, audioRecognizerPanel, tabName, getIndexOfComponent(getTabPane(), audioRecognizerPanel));   
            		}    					
        		}     			
    		} 
    			
    		if(videoRecognizerPanel != null){
    			String tabName = ElanLocale.getString("Tab.VideoRecognizer");
    			if(videoRecognizerLeft){    					
        			if(getTabPane().indexOfComponent(videoRecognizerPanel) >=0 ){
            			getTabPane().remove(videoRecognizerPanel);
            		}    					
        			if ( getLeftTabPane().indexOfComponent(videoRecognizerPanel) < 0 ){        					
            			getLeftTabPane().insertTab(tabName, null, videoRecognizerPanel, tabName, getIndexOfComponent(getLeftTabPane(), videoRecognizerPanel));  
            		}  else {    					
    					getLeftTabPane().remove(videoRecognizerPanel);
    					getLeftTabPane().insertTab(tabName, null, videoRecognizerPanel, tabName, getIndexOfComponent(getLeftTabPane(), videoRecognizerPanel));    
    				}
        		} else {
        			if(getLeftTabPane().indexOfComponent(videoRecognizerPanel) >=0 ){
        				getLeftTabPane().remove(videoRecognizerPanel);
            		}
        			if ( getTabPane().indexOfComponent(videoRecognizerPanel) < 0 ){        					
            			getTabPane().insertTab(tabName, null, videoRecognizerPanel, tabName, getIndexOfComponent(getTabPane(), videoRecognizerPanel));  
            		}    					
        		}     			
    		} 
    			
    		if(metadataPanel != null){
    			String tabName = ElanLocale.getString("Tab.Metadata");
    			if(metaDataLeft){    					
        			if(getTabPane().indexOfComponent(metadataPanel) >=0 ){
            			getTabPane().remove(metadataPanel);
            		}    					
        			if ( getLeftTabPane().indexOfComponent(metadataPanel) < 0 ){        					
            			getLeftTabPane().insertTab(tabName, null, metadataPanel, tabName, getIndexOfComponent(getLeftTabPane(), metadataPanel));
            		} else {    					
    					getLeftTabPane().remove(metadataPanel);
    					getLeftTabPane().insertTab(tabName, null, metadataPanel, tabName, getIndexOfComponent(getLeftTabPane(), metadataPanel));    
    				}
        		} else {
        			if(getLeftTabPane().indexOfComponent(metadataPanel) >=0 ){
        				getLeftTabPane().remove(metadataPanel);
            		}
        			if ( getTabPane().indexOfComponent(metadataPanel) < 0 ){        					
            			getTabPane().insertTab(tabName, null, metadataPanel, tabName, getIndexOfComponent(getTabPane(), metadataPanel));    
            			
            		}    					
        		}     			
    		} 
    		
    		if(getLeftTabPane().indexOfComponent(selectedLeftComponent) > 0){
    			getLeftTabPane().setSelectedComponent(selectedLeftComponent);
    		}else if(getLeftTabPane().getTabCount() > selectedIndexLeftPane){
    			getLeftTabPane().setSelectedIndex(selectedIndexLeftPane);
    		}else {
    			getLeftTabPane().setSelectedIndex(getLeftTabPane().getTabCount()-1);
    		}
    		
    		if(getTabPane().indexOfComponent(selectedRightComponent) > 0){
    			getTabPane().setSelectedComponent(selectedRightComponent);
    		}else if(getTabPane().getTabCount() > selectedIndexRightPane){
    			getTabPane().setSelectedIndex(selectedIndexRightPane);
    		}else {
    			getTabPane().setSelectedIndex(getTabPane().getTabCount()-1);
    		}
    	} else {
    		int selectedIndex= 0; 
    		Component selectedComponent = null;
    		if(getTabPane().getTabCount() > 0){
    			selectedIndex = getTabPane().getSelectedIndex();
    			selectedComponent = getTabPane().getSelectedComponent();
    		}   
    		if(gridPanel != null){
    			String tabName = ElanLocale.getString("Tab.Grid");  
    			if(getTabPane().indexOfComponent(gridPanel) < 0){    				
    				getTabPane().insertTab(tabName, null, gridPanel, tabName, getIndexOfComponent(getTabPane(), gridPanel));
    			} else {
    				getTabPane().remove(gridPanel);
    				getTabPane().insertTab(tabName, null, gridPanel, tabName, getIndexOfComponent(getTabPane(), gridPanel));
    			}
    		}
			if(textPanel != null){
				String tabName = ElanLocale.getString("Tab.Text");
				if(getTabPane().indexOfComponent(textPanel) < 0){	
					getTabPane().insertTab(tabName, null, textPanel, tabName, getIndexOfComponent(getTabPane(), textPanel));
				} else {
    				getTabPane().remove(textPanel);
    				getTabPane().insertTab(tabName, null, textPanel, tabName, getIndexOfComponent(getTabPane(), textPanel));
    			}
			}
			if(subtitlePanel != null){
				String tabName = ElanLocale.getString("Tab.Subtitles");			
				if(getTabPane().indexOfComponent(subtitlePanel) < 0){
					getTabPane().insertTab(tabName, null, subtitlePanel, tabName, getIndexOfComponent(getTabPane(), subtitlePanel));
				} else {
    				getTabPane().remove(subtitlePanel);
    				getTabPane().insertTab(tabName, null, subtitlePanel, tabName, getIndexOfComponent(getTabPane(), subtitlePanel));
    			}
			}
			if(lexiconPanel != null){
				String tabName = ElanLocale.getString(ELANCommandFactory.LEXICON_VIEWER);			
				if(getTabPane().indexOfComponent(lexiconPanel) < 0){
					getTabPane().insertTab(tabName, null, lexiconPanel, tabName, getIndexOfComponent(getTabPane(), lexiconPanel));
				} else {
    				getTabPane().remove(lexiconPanel);
    				getTabPane().insertTab(tabName, null, lexiconPanel, tabName, getIndexOfComponent(getTabPane(), lexiconPanel));
    			}
			}
			if(audioRecognizerPanel != null){
				String tabName = ElanLocale.getString("Tab.AudioRecognizer");			
				if(getTabPane().indexOfComponent(audioRecognizerPanel) < 0){
					getTabPane().insertTab(tabName, null, audioRecognizerPanel, tabName, getIndexOfComponent(getTabPane(), audioRecognizerPanel));
				} else {
    				getTabPane().remove(audioRecognizerPanel);
    				getTabPane().insertTab(tabName, null, audioRecognizerPanel, tabName, getIndexOfComponent(getTabPane(), audioRecognizerPanel));
    			}
			}			
			if(videoRecognizerPanel != null){
				String tabName = ElanLocale.getString("Tab.VideoRecognizer");			
				if(getTabPane().indexOfComponent(videoRecognizerPanel) < 0){	
					getTabPane().insertTab(tabName, null, videoRecognizerPanel, tabName, getIndexOfComponent(getTabPane(), videoRecognizerPanel));
				}else {
    				getTabPane().remove(videoRecognizerPanel);
    				getTabPane().insertTab(tabName, null, videoRecognizerPanel, tabName, getIndexOfComponent(getTabPane(), videoRecognizerPanel));
    			}
			}			
			if(metadataPanel != null){
				String tabName = ElanLocale.getString("Tab.Metadata");
				if(getTabPane().indexOfComponent(metadataPanel) < 0){
					getTabPane().insertTab(tabName, null, metadataPanel, tabName, getIndexOfComponent(getTabPane(), metadataPanel));
				} else {
    				getTabPane().remove(metadataPanel);
    				getTabPane().insertTab(tabName, null, metadataPanel, tabName, getIndexOfComponent(getTabPane(), metadataPanel));
    			}
			}
			
			if(getTabPane().indexOfComponent(selectedComponent) > 0){
    			getTabPane().setSelectedComponent(selectedComponent);
    		}else if(getTabPane().getTabCount() > selectedIndex){
    			getTabPane().setSelectedIndex(selectedIndex);
    		}else {
    			getTabPane().setSelectedIndex(getTabPane().getTabCount()-1);
    		}
    	}
    } 
    
    /**
     * DOCUMENT ME!
     */
    public void showTimeLineViewer() {
        showTimeLineViewer = true;
        showInterlinearViewer = false;

        enableDisableLogic();

        doLayout();
    }

    /**
     * DOCUMENT ME!
     */
    public void showInterlinearViewer() {
        showTimeLineViewer = false;
        showInterlinearViewer = true;

        enableDisableLogic();

        doLayout();
    }
    
    private void enableDisableLogic() {
        if (showTimeLineViewer) {
            if (timeLineViewer != null) {
                viewerManager.enableViewer(timeLineViewer);
            }

            if (interlinearViewer != null) {
            	interlinearViewer.isClosing();
                viewerManager.disableViewer(interlinearViewer);
            }
            layoutManager.setPreference("LayoutManager.VisibleMultiTierViewer", 
            		TimeLineViewer.class.getName(), viewerManager.getTranscription());
            timeLineViewer.preferencesChanged();
        } else if (showInterlinearViewer) {
            if (timeLineViewer != null) {
            	timeLineViewer.isClosing();
                viewerManager.disableViewer(timeLineViewer);
            }

            if (interlinearViewer != null) {
                viewerManager.enableViewer(interlinearViewer);
            }
            layoutManager.setPreference("LayoutManager.VisibleMultiTierViewer", 
            		InterlinearViewer.class.getName(), viewerManager.getTranscription());
            interlinearViewer.preferencesChanged();
        }
    }
    
    /**
     * Detaches the specified viewer or player.
     * 
     * @param object the viewer or player to remove from the main application frame
     */
    public void detach(Object object) {
    	if (object instanceof AbstractViewer) {
    		container.remove((Component)object);

    		if (object instanceof TimeSeriesViewer) {
    			ViewerLayoutModel vlm = null;
    			for (int i = 0; i < viewerList.size(); i++) {
    				vlm = (ViewerLayoutModel) viewerList.get(i);
    				if (vlm.viewer == object) {
    					break;
    				} else {
    					vlm = null;
    				}
    			}
    			if (vlm == null) {
    				return;
    			}
    			
    			if (wav_tsSplitPane != null) {
    				int curHeight = getTimeLineSplitPane().getDividerLocation();
    				wav_tsSplitPane.setTopComponent(null);
    				wav_tsSplitPane.setBottomComponent(null);
    				timeseriesComponent.remove(timeseriesViewer);
    				timeseriesComponent = null;
    				wav_tsSplitPane = null;
    				
    				if (signalComponent != null) {
    					getTimeLineSplitPane().setTopComponent(signalComponent);
    					getTimeLineSplitPane().setDividerLocation(curHeight);
    					signalComponent.setSize(signalComponent.getWidth(), curHeight);
    				} else {
    					getTimeLineSplitPane().setTopComponent(null);
    				}   				
    			} else {
    				getTimeLineSplitPane().setTopComponent(null);
    				if (timeseriesComponent != null) {
	    				timeseriesComponent.remove(timeseriesViewer);
	    				timeseriesComponent = null;
    				}
    			}
    			doLayout();
    			vlm.detach();
    		}
    	}        
    }
    
    /**
     * Attaches the specified viewer or player. 
     *
     * @param object the viewer or player to attach
     */
    public void attach(Object object) {
    	
		if (object instanceof AbstractViewer) {
			// detach from frame/dialog, destroy dialog and add to container
			// use a ViewerLayoutModel
			ViewerLayoutModel vlm = null;
			for (int i = 0; i < viewerList.size(); i++) {
				vlm = (ViewerLayoutModel) viewerList.get(i);
				if (vlm.viewer == object) {
					break;
				} else {
					vlm = null;
				}
			}
			if (vlm == null) {
				return;
			}
			// make sure that the dialog/frame has been destroyed
			vlm.attach();
			
			if (vlm.viewer instanceof TimeSeriesViewer) {
				//setPreference("TimeSeriesViewer.Detached", Boolean.FALSE, viewerManager.getTranscription()); 
				setTimeSeriesViewer((TimeSeriesViewer) vlm.viewer);
			}
			//container.add((Component)object);
		} 
    }
    
	public void doLayout() {
		
		if(!layoutManager.isIntialized()){
			return;
		}
		
        // get the width and height of the usable area
        int containerWidth = container.getWidth();
        int containerHeight = container.getHeight();
        int containerMargin = 3;
        int componentMargin = 5;

        PlayerLayoutModel[] visualPlayers = layoutManager.getAttachedVisualPlayers();
		int numVisualPlayers = visualPlayers.length;	
		
		// first layout the player components, next the tabpane
		int mediaAreaHeight = layoutManager.getMediaAreaHeight();
		int visibleMediaX = containerMargin;
		int visibleMediaY = containerMargin;
		int visibleMediaWidth = 0;
		int visibleMediaHeight = mediaAreaHeight;

		int firstMediaWidth = visibleMediaWidth;
					
		if (oneRowForVisuals) {
			if (numVisualPlayers >= 1) {
				int maxPerMedia = (containerWidth - minTabWidth) / numVisualPlayers;
				int maxUsedHeight = 0;
				float aspectRatio;
				Component visComp;
				for (int i = 0; i < numVisualPlayers && i < 4; i++) {
					visComp = visualPlayers[i].visualComponent;
					aspectRatio = visualPlayers[i].player.getAspectRatio();
					int curWidth = 0, curHeight = 0;
					if (mediaAreaHeight * aspectRatio > maxPerMedia) {
						curWidth = maxPerMedia;
						curHeight = (int) ((float)maxPerMedia / aspectRatio);
						maxUsedHeight = curHeight > maxUsedHeight ? curHeight : maxUsedHeight;
					} else {
						curWidth = (int) (mediaAreaHeight * aspectRatio);
						curHeight = mediaAreaHeight;
					}					
					if (i == 0) {		
						visibleMediaWidth = visibleMediaX + curWidth + componentMargin;
						if(mediaInCentre){							
							visibleMediaX = (containerWidth - visibleMediaWidth*numVisualPlayers)/2;
						}
						 visComp.setBounds(visibleMediaX, visibleMediaY, curWidth, curHeight);
						 firstMediaWidth = curWidth;// used by the time panel
						 //visibleMediaWidth = visibleMediaX + curWidth + componentMargin;
					} else {
						visComp.setBounds(visibleMediaX + visibleMediaWidth, visibleMediaY, curWidth, curHeight);
						visibleMediaWidth = visibleMediaWidth + curWidth + componentMargin;
					}
				}
			}
		} else {
			//if (numVisualPlayers == 0) {
			//	visibleMediaHeight = mediaAreaHeight;
			//}
			if (numVisualPlayers >= 1) {
				// layout the first video
				Component firstVisualComp = visualPlayers[0].visualComponent;
				float aspectRatio = visualPlayers[0].player.getAspectRatio();
				int firstMediaHeight = mediaAreaHeight;
				firstMediaWidth = ElanLayoutManager.MASTER_MEDIA_WIDTH;
				// jan 2007 if the source- or encoded-width of the video is more than twice the MASTER_
				// MEDIA_WIDTH constant, then divide the real source width by 2 for optimal rendering
				if (visualPlayers[0].player.getSourceWidth() > 2 * ElanLayoutManager.MASTER_MEDIA_WIDTH && 
				        mediaAreaHeight == ElanLayoutManager.MASTER_MEDIA_HEIGHT) {
				    firstMediaWidth = visualPlayers[0].player.getSourceWidth() / 2;
				    firstMediaHeight = (int) ((float) firstMediaWidth / aspectRatio);
				    //System.out.println("adj. width: " + firstMediaWidth);
				} else {
				    firstMediaWidth = (int) (firstMediaHeight * aspectRatio);
				}
				//firstMediaHeight = (int) ((float) firstMediaWidth / aspectRatio);
				//if (firstMediaHeight < mediaAreaHeight) {
				//	firstMediaHeight = mediaAreaHeight;
				//	firstMediaWidth = (int) (firstMediaHeight * aspectRatio);
					//System.out.println("height: " + firstMediaHeight + " width: " + firstMediaWidth);
				//}
				visibleMediaWidth = firstMediaWidth + componentMargin;	
				visibleMediaHeight = firstMediaHeight;
				if(numVisualPlayers == 1){
					if(mediaInCentre){	
						visibleMediaX = (containerWidth - visibleMediaWidth)/2;
					}
					firstVisualComp.setBounds(containerMargin+visibleMediaX, visibleMediaY, firstMediaWidth,
							firstMediaHeight);				
				}
				//System.out.println("width: " + firstMediaWidth + " height: " + firstMediaHeight);
			}
			if (numVisualPlayers == 2) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float aspectRatio = visualPlayers[1].player.getAspectRatio();
				int secondMediaWidth = (int) (visibleMediaHeight * aspectRatio);
				int secondMediaHeight = visibleMediaHeight;
				if(mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth + secondMediaWidth))/2;
				}				
				visualPlayers[0].visualComponent.setBounds(containerMargin+visibleMediaX, visibleMediaY, firstMediaWidth,
							visibleMediaHeight);					 
				if (visualPlayers[1].player.getSourceWidth() > 2 * ElanLayoutManager.MASTER_MEDIA_WIDTH && 
						visualPlayers[1].player.getSourceWidth() > visualPlayers[0].player.getSourceWidth()) {
					secondMediaWidth = visualPlayers[1].player.getSourceWidth() / 2;
					secondMediaHeight = (int) ((float) secondMediaWidth / aspectRatio);
					if (secondMediaHeight > visibleMediaHeight) {
						visibleMediaHeight = secondMediaHeight;
					}
				}
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth,
					visibleMediaY, secondMediaWidth, secondMediaHeight);
				visibleMediaWidth += secondMediaWidth + componentMargin;
				//System.out.println("sec width: " + secondMediaWidth + " sec height: " + secondMediaHeight);
			}
			else if (numVisualPlayers == 3) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float secondAR = visualPlayers[1].player.getAspectRatio();
				Component thirdVisualComp = visualPlayers[2].visualComponent;
				float thirdAR = visualPlayers[2].player.getAspectRatio();
				int heightPerPlayer = (visibleMediaHeight - componentMargin) / 2;
				int secondWidth = (int)(secondAR * heightPerPlayer);
				int thirdWidth = (int) (thirdAR * heightPerPlayer);
				int widthPerPlayer = Math.max(secondWidth, thirdWidth);
				if(mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth+widthPerPlayer))/2;
				}
				visualPlayers[0].visualComponent.setBounds(visibleMediaX, visibleMediaY, firstMediaWidth,
							(int) ((float) firstMediaWidth / visualPlayers[0].player.getAspectRatio()));						
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - secondWidth) / 2, visibleMediaY, 
					secondWidth, heightPerPlayer);
				thirdVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - thirdWidth) / 2, 
					visibleMediaY + heightPerPlayer + componentMargin, 
					thirdWidth, heightPerPlayer);
				visibleMediaWidth += widthPerPlayer + componentMargin;
			}
			else if (numVisualPlayers >= 4) {
				Component secondVisualComp = visualPlayers[1].visualComponent;
				float secondAR = visualPlayers[1].player.getAspectRatio();
				Component thirdVisualComp = visualPlayers[2].visualComponent;
				float thirdAR = visualPlayers[2].player.getAspectRatio();
				Component fourthVisualComp = visualPlayers[3].visualComponent;
				float fourthAR = visualPlayers[3].player.getAspectRatio();
				int heightPerPlayer = (visibleMediaHeight - 2 * componentMargin) / 3;
				int secondWidth = (int)(secondAR * heightPerPlayer);
				int thirdWidth = (int) (thirdAR * heightPerPlayer);
				int fourthWidth = (int) (fourthAR * heightPerPlayer);
				int widthPerPlayer = Math.max(secondWidth, thirdWidth);
				widthPerPlayer = Math.max(widthPerPlayer, fourthWidth);
				if(mediaInCentre){
					visibleMediaX = (containerWidth - (visibleMediaWidth+widthPerPlayer))/2;
				}
				visualPlayers[0].visualComponent.setBounds(visibleMediaX, visibleMediaY, firstMediaWidth,
							(int) ((float) firstMediaWidth / visualPlayers[0].player.getAspectRatio()));	
				secondVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - secondWidth) / 2, visibleMediaY, 
					secondWidth, heightPerPlayer);
				thirdVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - thirdWidth) / 2, 
					visibleMediaY + heightPerPlayer + componentMargin, 
					thirdWidth, heightPerPlayer);
				fourthVisualComp.setBounds(visibleMediaX + visibleMediaWidth + 
					(widthPerPlayer - fourthWidth) / 2, 
					visibleMediaY + 2 * heightPerPlayer + 2 * componentMargin, 
					fourthWidth, heightPerPlayer);
				visibleMediaWidth += widthPerPlayer + componentMargin;
			}
	    }
        // layout the tab panel
		
		int tabPaneX = visibleMediaX + visibleMediaWidth;
        int tabPaneY = visibleMediaY;
        int tabPaneWidth = containerWidth - tabPaneX ;
        int tabPaneHeight = visibleMediaHeight;

        if(mediaInCentre){
        	if(numVisualPlayers > 0){
        		tabPaneWidth = visibleMediaX;
        	} else {
        		tabPaneWidth = tabPaneWidth/2;
        		tabPaneX = tabPaneWidth;
        	}    
        	tabPaneX = tabPaneX - containerMargin;
        }        
        
        if(mediaInCentre ){//&& numVisualPlayers > 0){
        	//tabPaneX = visibleMediaX + visibleMediaWidth * numVisualPlayers ;
        	//tabPaneWidth = visibleMediaX;
        	
        	
            getLeftTabPane().setBounds(containerMargin, containerMargin, tabPaneWidth, tabPaneHeight);            
        } else        	
        	destroyLeftPane();
        	
        
        if (tabPane != null) {
            tabPane.setBounds(tabPaneX, tabPaneY, tabPaneWidth, tabPaneHeight);

           if (mediaPlayerController != null && controlPanel != null) {
               controlPanel.setSize(tabPaneWidth, tabPaneHeight);
           }
        } 
        if(numOfPlayers != numVisualPlayers && numOfPlayers == 0){
        	preferenceChanged = true;
        }
        
        if(preferenceChanged || numVisualPlayers == 0){
        	reArrangeViewers(); 
        	preferenceChanged = false;
        }

        int timePanelX = 0;
        int timePanelY = visibleMediaY + visibleMediaHeight + 2;
        int timePanelWidth = 0;
        int timePanelHeight = 0;
        

        if (mediaPlayerController != null) {
            timePanelWidth = mediaPlayerController.getTimePanel()
                                                  .getPreferredSize().width;
            timePanelHeight = mediaPlayerController.getTimePanel()
                                                   .getPreferredSize().height;
			if (numVisualPlayers == 0) {
				timePanelX = containerMargin;
			} else {				
				if(mediaInCentre){		        	
		        	timePanelX = (visibleMediaX + (firstMediaWidth / 2)) -
						(timePanelWidth / 2);
		        } else{
				timePanelX = (containerMargin + (firstMediaWidth / 2)) -
					(timePanelWidth / 2);
		        }
			}
	        
            mediaPlayerController.getTimePanel().setBounds(timePanelX,
                timePanelY, timePanelWidth, timePanelHeight);
        }

        int playButtonsX = ElanLayoutManager.CONTAINER_MARGIN;
        int playButtonsY = timePanelY + timePanelHeight + 4;
        int playButtonsWidth = 0;
        int playButtonsHeight = 0;

        if (mediaPlayerController != null) {
            playButtonsWidth = mediaPlayerController.getPlayButtonsPanel()
                                                    .getPreferredSize().width;
            playButtonsHeight = mediaPlayerController.getPlayButtonsPanel()
                                                     .getPreferredSize().height;

			if (numVisualPlayers > 0) {
				if(mediaInCentre){	
					playButtonsX = (visibleMediaX );
				}else{
					playButtonsX = (containerMargin + (firstMediaWidth / 2)) -
						(playButtonsWidth / 2);				
					if (playButtonsX < ElanLayoutManager.CONTAINER_MARGIN) {
						playButtonsX = ElanLayoutManager.CONTAINER_MARGIN;
					}
				}
			}

            mediaPlayerController.getPlayButtonsPanel().setBounds(playButtonsX,
                playButtonsY, playButtonsWidth, playButtonsHeight);
        }

        int selectionPanelX = playButtonsX + playButtonsWidth + 20;
        int selectionPanelY = visibleMediaY + visibleMediaHeight + 2;
        int selectionPanelWidth = 0;
        int selectionPanelHeight = 0;

        if (mediaPlayerController != null) {
            selectionPanelWidth = 100 +
                mediaPlayerController.getSelectionPanel().getPreferredSize().width;
            selectionPanelHeight = mediaPlayerController.getSelectionPanel()
                                                        .getPreferredSize().height;
            mediaPlayerController.getSelectionPanel().setBounds(selectionPanelX,
                selectionPanelY, selectionPanelWidth, selectionPanelHeight);
        }

        int selectionButtonsX = selectionPanelX;
        int selectionButtonsY = selectionPanelY + selectionPanelHeight + 4;
        int selectionButtonsWidth = 0;
        int selectionButtonsHeight = 0;

        if (mediaPlayerController != null) {
            selectionButtonsWidth = mediaPlayerController.getSelectionButtonsPanel()
                                                         .getPreferredSize().width;
            selectionButtonsHeight = mediaPlayerController.getSelectionButtonsPanel()
                                                          .getPreferredSize().height;
            mediaPlayerController.getSelectionButtonsPanel().setBounds(selectionButtonsX,
                selectionButtonsY, selectionButtonsWidth, selectionButtonsHeight);
        }

        int annotationButtonsX = selectionButtonsX + selectionButtonsWidth + 15;
        int annotationButtonsY = selectionPanelY + selectionPanelHeight + 4;
        int annotationButtonsWidth = 0;
        int annotationButtonsHeight = 0;

        if (mediaPlayerController != null) {
            annotationButtonsWidth = mediaPlayerController.getAnnotationNavigationPanel()
                                                          .getPreferredSize().width;
            annotationButtonsHeight = mediaPlayerController.getAnnotationNavigationPanel()
                                                           .getPreferredSize().height;
            mediaPlayerController.getAnnotationNavigationPanel().setBounds(annotationButtonsX,
                annotationButtonsY, annotationButtonsWidth,
                annotationButtonsHeight);
        }

        int modePanelX = annotationButtonsX + annotationButtonsWidth + 10;
        int modePanelY = annotationButtonsY;
        int modePanelWidth = 0;
        int modePanelHeight = 0;

        if (mediaPlayerController != null) {
            //modePanelWidth = 300; //
        	modePanelWidth = mediaPlayerController.getModePanel().getPreferredSize().width;
            modePanelHeight = mediaPlayerController.getModePanel()
                                                   .getPreferredSize().height;
            if (modePanelHeight > annotationButtonsHeight && annotationButtonsHeight > 0) {
            	modePanelY -= (modePanelHeight - annotationButtonsHeight) / 2;
            }
            mediaPlayerController.getModePanel().setBounds(modePanelX,
                modePanelY, modePanelWidth, modePanelHeight);
        }
        
      //layout for the volume button & slider
		int butVolumeX = modePanelX + modePanelWidth + 10;
		int butVolumeY = annotationButtonsY;
		int butVolumeWidth = 0;
		int butVolumeHeight = 0;
    
		if (mediaPlayerController != null) {
			butVolumeWidth = mediaPlayerController.getButtonSize().width;
			butVolumeHeight =  mediaPlayerController.getButtonSize().height; 
			mediaPlayerController.getVolumeIconPanel().setBounds(butVolumeX, butVolumeY, butVolumeWidth, butVolumeHeight); 
		}
		
        if((butVolumeX+butVolumeWidth) > containerWidth){
        	if (mediaPlayerController != null) {
        		if (numVisualPlayers == 0) {
    				timePanelX = containerMargin;
    			} else {				
    				timePanelX = (containerMargin + (firstMediaWidth / 2)) -
    					(timePanelWidth / 2);
    		    }
        		
                mediaPlayerController.getTimePanel().setBounds(timePanelX,
                    timePanelY, timePanelWidth, timePanelHeight);
        		
        		
        		playButtonsX = (containerMargin + (firstMediaWidth / 2)) -
    						(playButtonsWidth / 2);				
        		if (playButtonsX < ElanLayoutManager.CONTAINER_MARGIN) {
        			playButtonsX = ElanLayoutManager.CONTAINER_MARGIN;
        		}
        		mediaPlayerController.getPlayButtonsPanel().setBounds(playButtonsX,
        				playButtonsY, playButtonsWidth, playButtonsHeight);
            	
        		selectionPanelX = playButtonsX + playButtonsWidth + 20;
                mediaPlayerController.getSelectionPanel().setBounds(selectionPanelX,
                    selectionPanelY, selectionPanelWidth, selectionPanelHeight);
                
                selectionButtonsX = selectionPanelX;
                mediaPlayerController.getSelectionButtonsPanel().setBounds(selectionButtonsX,
                    selectionButtonsY, selectionButtonsWidth, selectionButtonsHeight);
                
                annotationButtonsX = selectionButtonsX + selectionButtonsWidth + 15;
                mediaPlayerController.getAnnotationNavigationPanel().setBounds(annotationButtonsX,
                    annotationButtonsY, annotationButtonsWidth,
                    annotationButtonsHeight);
                
                modePanelX = annotationButtonsX + annotationButtonsWidth + 10;
                mediaPlayerController.getModePanel().setBounds(modePanelX,
                    modePanelY, modePanelWidth, modePanelHeight);
                
                butVolumeX = modePanelX+modePanelWidth;
        		mediaPlayerController.getVolumeIconPanel().setBounds(butVolumeX, butVolumeY, butVolumeWidth, butVolumeHeight); 
        	}
        } 
        
        // resize divider
        int divX = 0 ; 
        int divY = playButtonsY + playButtonsHeight +4; 
        int divHeight = vertMediaResizer.getPreferredSize().height;
        vertMediaResizer.setBounds(divX, divY, containerWidth, divHeight);

        
        int sliderPanelX = ElanLayoutManager.CONTAINER_MARGIN;
        int sliderPanelY = divY + divHeight +4;
        int sliderPanelWidth = 0;
        int sliderPanelHeight = 0;

        if (mediaPlayerController != null) {
            sliderPanelWidth = containerWidth -  (2 * ElanLayoutManager.CONTAINER_MARGIN);
            sliderPanelHeight = mediaPlayerController.getSliderPanel()
                                                     .getPreferredSize().height;
            mediaPlayerController.getSliderPanel().setBounds(sliderPanelX,
                sliderPanelY, sliderPanelWidth, sliderPanelHeight);
        }
        

        int densityPanelX = ElanLayoutManager.CONTAINER_MARGIN;
        int densityPanelY = sliderPanelY - 3;// - ElanLayoutManager.BELOW_BUTTONS_MARGIN; //sliderPanelHeight;
        int densityPanelWidth = sliderPanelWidth;
        int densityPanelHeight = 0;

        if (mediaPlayerController != null) {
            densityPanelHeight = mediaPlayerController.getAnnotationDensityViewer()
                                                      .getPreferredSize().height;
            mediaPlayerController.getAnnotationDensityViewer().setBounds(densityPanelX,
                densityPanelY, densityPanelWidth, densityPanelHeight);
        }

        // layout time line split pane
        int timeLineSplitPaneX = ElanLayoutManager.CONTAINER_MARGIN;
        int timeLineSplitPaneY = densityPanelY + densityPanelHeight + 4;
        int timeLineSplitPaneWidth = 0;
        int timeLineSplitPaneHeight = 0;

        if (timeLineSplitPane != null) {
            timeLineSplitPaneWidth = containerWidth - (2 * ElanLayoutManager.CONTAINER_MARGIN);
            timeLineSplitPaneHeight = containerHeight - timeLineSplitPaneY;
            timeLineSplitPane.setBounds(timeLineSplitPaneX, timeLineSplitPaneY,
                timeLineSplitPaneWidth, timeLineSplitPaneHeight);
        }

        // layout time line pane
        int multiTierControlX = 0;
        int multiTierControlY = 0;
        int multiTierControlWidth = 0;
        int multiTierControlHeight = 0;
        int timeLineX = 0;
        int timeLineY = 0;
        int timeLineWidth = 0;
        int timeLineHeight = 0;
        int interlinearX = 0;
        int interlinearY = 0;
        int interlinearWidth = 0;
        int interlinearHeight = 0;

        if (timeLineComponent != null) {
            int bottomHeight = timeLineSplitPane.getHeight() -
                timeLineSplitPane.getDividerLocation() -
                timeLineSplitPane.getDividerSize();
            Insets insets = timeLineSplitPane.getInsets();
            timeLineComponent.setSize(timeLineSplitPane.getWidth() - insets.left - insets.top, 
            		bottomHeight - insets.bottom);
            timeLineComponent.setPreferredSize(timeLineComponent.getSize());
            multiTierControlWidth = layoutManager.getMultiTierControlPanelWidth();
            multiTierControlHeight = bottomHeight; //timeLineComponent.getHeight();
            multiTierControlPanel.setSize(multiTierControlWidth, multiTierControlHeight);
            multiTierControlPanel.setBounds(multiTierControlX,
                multiTierControlY, multiTierControlWidth, multiTierControlHeight);

            if (showTimeLineViewer) {
                timeLineX = multiTierControlWidth;

                //timeLineWidth = timeLineComponent.getWidth() - multiTierControlWidth;
                timeLineWidth = timeLineSplitPane.getWidth() -
                    multiTierControlWidth;
                timeLineHeight = bottomHeight; //timeLineComponent.getHeight();
            } else {
                interlinearX = multiTierControlWidth;

                //interlinearWidth = timeLineComponent.getWidth() - multiTierControlWidth;
                interlinearWidth = timeLineSplitPane.getWidth() -
                    multiTierControlWidth;
                interlinearHeight = bottomHeight; //timeLineComponent.getHeight();
            }

            if (timeLineViewer != null) {
                timeLineViewer.setBounds(timeLineX, timeLineY, timeLineWidth,
                    timeLineHeight);
				timeLineViewer.setPreferredSize(
					new Dimension(timeLineWidth, timeLineHeight));
                // force a component event on the viewer, does not happen automatically apparently
                timeLineViewer.componentResized(null);
            }

            if (interlinearViewer != null) {
                interlinearViewer.setBounds(interlinearX, interlinearY,
                    interlinearWidth, interlinearHeight);
					interlinearViewer.setPreferredSize(
						new Dimension(interlinearWidth, interlinearHeight));
                // force a component event on the viewer, does not happen automatically apparently
                interlinearViewer.componentResized(null);
            }
        }

        // layout signal pane
        int signalX = multiTierControlWidth;
        int signalY = 0;
        int signalWidth = 0;
        int signalHeight = 0;

        if (wav_tsSplitPane != null && signalComponent != null 
        		&& timeseriesComponent != null) { //overly checked
        	int topHeight = wav_tsSplitPane.getDividerLocation();
        	//layout the 2 viewers in the pane
            int rMargin = 0;

            if (timeLineViewer != null) {
                rMargin = timeLineViewer.getRightMargin();
            }
			signalWidth = timeLineSplitPane.getWidth() - multiTierControlWidth -
				rMargin;
		    //signalHeight = signalComponent.getHeight();
			signalHeight = wav_tsSplitPane.getHeight() - wav_tsSplitPane.getDividerSize()- topHeight;
			signalViewer.setBounds(signalX, signalY, signalWidth, signalHeight);
			signalComponent.setPreferredSize(new Dimension(signalWidth, signalHeight));
			
			int tsWidth = 0;
			timeseriesViewer.setRightMargin(rMargin);
			timeseriesViewer.setVerticalRulerWidth(multiTierControlWidth);
			tsWidth = timeLineSplitPane.getWidth();
			//signalHeight = timeseriesComponent.getHeight();
			//int tsHeight = wav_tsSplitPane.getHeight() - wav_tsSplitPane.getDividerLocation() - 
			//    wav_tsSplitPane.getDividerSize();
			timeseriesViewer.setBounds(0, signalY, tsWidth, topHeight);
			timeseriesComponent.setPreferredSize(new Dimension(tsWidth, topHeight));
			
        } else 
        if ((signalComponent != null) && (signalViewer != null)) {
            int rMargin = 0;

            if (timeLineViewer != null) {
                rMargin = timeLineViewer.getRightMargin();
            } 

			//		 signalWidth = signalComponent.getWidth() - multiTierControlWidth - rMargin;
			signalWidth = timeLineSplitPane.getWidth() - multiTierControlWidth -
				rMargin;
			signalHeight = signalComponent.getHeight();

			signalViewer.setBounds(signalX, signalY, signalWidth, signalHeight);
			signalComponent.setPreferredSize(new Dimension(signalWidth, signalHeight));		
        } else if (timeseriesComponent != null && timeseriesViewer != null){
            int rMargin = 0;
            int tsWidth = 0;
            
            if (timeLineViewer != null) {
                rMargin = timeLineViewer.getRightMargin();
                timeseriesViewer.setRightMargin(rMargin);
            } 

			timeseriesViewer.setVerticalRulerWidth(multiTierControlWidth);
			tsWidth = timeLineSplitPane.getWidth();
			signalHeight = timeseriesComponent.getHeight();
			

			timeseriesViewer.setBounds(0, signalY, tsWidth, signalHeight);
			timeseriesComponent.setPreferredSize(new Dimension(tsWidth, signalHeight));
        }
        
        if (timeLineSplitPane != null) {
        	timeLineSplitPane.resetToPreferredSizes();
        }

        // layout control panel components
        /*
        if (mediaPlayerController != null) {
        	
            int vX = 10;
            int vY = 30;
            int vWidth = controlPanel.getWidth() - (2 * vX);
            int vHeight = mediaPlayerController.getVolumePanel()
                                               .getPreferredSize().height;
            mediaPlayerController.getVolumePanel().setBounds(vX, vY, vWidth,
                vHeight);
			

            int rX = 10;
            int rY = vY + vHeight + 10;
            int rWidth = controlPanel.getWidth() - (2 * vX); //mediaPlayerController.getRatePanel().getPreferredSize().width;
            int rHeight = mediaPlayerController.getRatePanel().getPreferredSize().height;
            mediaPlayerController.getRatePanel().setBounds(rX, rY, rWidth,
                rHeight);
                
        }
        */
        
        if(mediaInCentre){
        	Integer selIndex = (Integer)Preferences.get(
					"LayoutManager.SelectedLeftTabIndex", viewerManager.getTranscription());
			if (selIndex != null) {
				int index = selIndex.intValue();
				if (index >= 0 && index < getLeftTabPane().getTabCount()) {
					getLeftTabPane().setSelectedIndex(index);
				}  else if(getLeftTabPane().getTabCount() > 0){
					getLeftTabPane().setSelectedIndex(0);
				}
			}
        }
        numOfPlayers = numVisualPlayers;
		container.validate();
		
	}

	
	public void updateLocale() {
		 if (tabPane != null) {
	            int nTabs = tabPane.getTabCount();

	            for (int i = 0; i < nTabs; i++) {
	                Component component = tabPane.getComponentAt(i);

	                if (component == gridPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Grid"));
	                } else if (component == textPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Text"));
	                } else if (component == subtitlePanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Subtitles"));
	                } else if (component == controlPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.Controls"));
	                } else if (component == audioRecognizerPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.AudioRecognizer"));
	                } else if (component == videoRecognizerPanel) {
	                    tabPane.setTitleAt(i, ElanLocale.getString("Tab.VideoRecognizer"));
	                } else if (component == metadataPanel) {
	                	tabPane.setTitleAt(i, ElanLocale.getString("Tab.Metadata"));
	                }
	            }
	        }
	}
	
	public void clearLayout() {	
		if(layoutManager.containsComponent(getTabPane())){
			container.remove(getTabPane());
		}
		
		if(layoutManager.containsComponent(getLeftTabPane())){
			container.remove(getLeftTabPane());
		}		
				
		if(layoutManager.containsComponent(timeLineSplitPane)){
			container.remove(timeLineSplitPane);
		}
		
		if(layoutManager.containsComponent(vertMediaResizer)){
			container.remove(vertMediaResizer);
		}	
		
		destroyAndRemoveViewer(ELANCommandFactory.INTERLINEAR_VIEWER);		
		destroyAndRemoveViewer(ELANCommandFactory.GRID_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.TEXT_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.SUBTITLE_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.LEXICON_VIEWER);	
		destroyAndRemoveViewer(ELANCommandFactory.AUDIO_RECOGNIZER);	
		destroyAndRemoveViewer(ELANCommandFactory.VIDEO_RECOGNIZER);
		destroyAndRemoveViewer(ELANCommandFactory.METADATA_VIEWER);		
		
		removeMediaPlayerController();		
		
		if(signalViewer != null){
			container.remove(signalViewer);
		}		
		
		timeseriesViewer = null;
		
		if (timeLineComponent != null) {
			timeLineComponent.removeAll();
		}
		
		multiTierControlPanel = null;	
		
		timeLineViewer = null;
		viewerManager.destroyTimeLineViewer();		
		viewerManager.destroyMultiTierControlPanel();
		
		container.repaint();
	}
	
	public void cleanUpOnClose() {
	}
	
	/**
	 * Called before clearing the layout and switching to new mode or called before 
	 * the file/elan is closed
	 */
	public void isClosing() {
		mediaPlayerController.stopLoop();		
		
		layoutManager.setPreference("LayoutManager.SelectedTabIndex", getTabPane().getSelectedIndex(), viewerManager.getTranscription());
		
		if(mediaInCentre && getLeftTabPane().getSelectedIndex() >= 0){
			layoutManager.setPreference("LayoutManager.SelectedLeftTabIndex", getLeftTabPane().getSelectedIndex(), viewerManager.getTranscription());
		}
		
		Object val = Preferences.get("InlineEdit.DeselectCommits", null);
        if (val instanceof Boolean && (Boolean)val) {
        	if(gridPanel != null){
        		((GridViewer)gridPanel.getViewer()).isClosing();
        	}
        	
        	if(interlinearViewer != null){
        		interlinearViewer.isClosing();
        	}
        	
        	if(subtitlePanel != null){
        		Component panel[] = subtitlePanel.getComponents();
				for(int i=0; i < panel.length; i++){
					if(panel[i] instanceof SingleTierViewerPanel){
						((SubtitleViewer)((SingleTierViewerPanel)panel[i]).getViewer()).isClosing();
					}
				}
        	}
        }
        
        if(timeLineViewer != null){
    		timeLineViewer.isClosing();
    	}
        
        if(audioRecognizerPanel!= null &&  audioRecognizerPanel instanceof AbstractRecognizerPanel){
        	((AbstractRecognizerPanel)audioRecognizerPanel).isClosing();
        }
        
        if(videoRecognizerPanel!= null &&  videoRecognizerPanel instanceof AbstractRecognizerPanel){
        	((AbstractRecognizerPanel)videoRecognizerPanel).isClosing();
        }			
        
        if(timeLineSplitPane!= null){
			layoutManager.setPreference("LayoutManager.SplitPaneDividerLocation", timeLineSplitPane.getDividerLocation() , viewerManager.getTranscription());
		}
		
        if(wav_tsSplitPane != null){
			layoutManager.setPreference("LayoutManager.TSWavSplitPaneDividerLocation", wav_tsSplitPane.getDividerLocation(), viewerManager.getTranscription());
		}
		
        
	}
	
	private void createSubtitleViewer(){
    	int numSubtitles = 4;
        Object val = Preferences.get("NumberOfSubtitleViewers", null);
        if (val instanceof Integer) {
        	numSubtitles = ((Integer) val).intValue();
        }
        
    	SubtitleViewer subtitleViewer;
    	for (int i = 0; i < numSubtitles; i++) {
    		subtitleViewer = viewerManager.createSubtitleViewer();
    		if(subtitleViewer != null){
    			subtitleViewer.setViewerIndex(i + 1);
    			subtitleViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
    			add(subtitleViewer);
    		}else {
    			break;
    		}
    	}
    }
	
	public void initComponents() {	
		
		controlPanel = new JPanel();
		controlPanel.setName(ElanLocale.getString("Tab.Controls"));
		controlPanel.setLayout(new GridLayout(2, 1, 10, 10));
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));    
	
    	vertMediaResizer = new ResizeComponent(layoutManager, SwingConstants.VERTICAL);
    	vertMediaResizer.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));
    	vertMediaResizer.setPreferredSize(new Dimension(container.getWidth(), 7));
   	
    	Component n = vertMediaResizer.getComponent(0);
        vertMediaResizer.remove(n);
        vertMediaResizer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = gbc.CENTER;
        gbc.weightx = 1.0; 
        vertMediaResizer.add(n, gbc);
        
        container.add(vertMediaResizer);
		        
	    Object val = Preferences.get("Media.VideosCentre", null);                
	    if (val instanceof Boolean) {
	    	mediaInCentre = ((Boolean) val).booleanValue();	
	    } 
	    
	    ksNotToBeConsumed = new ArrayList<KeyStroke>();
	    loadKSNottoBeConsumed();
	    
	    viewerManager.getMultiTierControlPanel().preferencesChanged(); 
		
	    layoutManager.add(viewerManager.getMediaPlayerController());
	    viewerManager.getMediaPlayerController().preferencesChanged();
	    
		TimeLineViewer timeLineViewer = viewerManager.createTimeLineViewer();
		add(timeLineViewer); 
		this.timeLineViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
		createAndAddViewer(ELANCommandFactory.INTERLINEAR_VIEWER);	 
        
		// visible multitier viewer
	    String conViewerName = (String) Preferences.get("LayoutManager.VisibleMultiTierViewer",
	    		viewerManager.getTranscription());
	    if(conViewerName == null){
	    	showTimeLineViewer();	
	    	viewerManager.getMultiTierControlPanel().disableShowInterlinearViewer(true);
	    } else if (conViewerName.equals(TimeLineViewer.class.getName())) {
	    	if (timeLineViewer != null) {
	    		showTimeLineViewer();
	    		multiTierControlPanel.setViewer(timeLineViewer);
	    	}
	    } else if (conViewerName.equals(InterlinearViewer.class.getName())) {
	    	if (interlinearViewer != null) {
	    		showInterlinearViewer();
	    		getMultiTierControlPanel().setViewer(interlinearViewer);
	    	}
	    }   
	     
	    createAndAddViewer(ELANCommandFactory.GRID_VIEWER);	  
	    createAndAddViewer(ELANCommandFactory.TEXT_VIEWER);	  
	    createAndAddViewer(ELANCommandFactory.SUBTITLE_VIEWER);	  
	    createAndAddViewer(ELANCommandFactory.LEXICON_VIEWER);	
	    createAndAddViewer(ELANCommandFactory.SIGNAL_VIEWER);		    
	    createAndAddViewer(ELANCommandFactory.AUDIO_RECOGNIZER);
	    createAndAddViewer(ELANCommandFactory.VIDEO_RECOGNIZER);
	    createAndAddViewer(ELANCommandFactory.METADATA_VIEWER);	
	    
	    layoutManager.add(layoutManager.getTimeSeriesViewer());  
	    
	   	timeLineViewer.requestFocus();
	   	
	    preferencesChanged();
	}

	public void preferencesChanged() {
		Integer selIndex = (Integer)Preferences.get(
				"LayoutManager.SelectedTabIndex", viewerManager.getTranscription());
		if (selIndex != null) {
			int index = selIndex.intValue();
			if (index >= 0 && index < tabPane.getTabCount()) {
				tabPane.setSelectedIndex(index);
			}  else {
				tabPane.setSelectedIndex(0);
			}
		}
		
		// visible multitier viewer
		String conViewerName = (String) Preferences.get("LayoutManager.VisibleMultiTierViewer",
				viewerManager.getTranscription());
		if (conViewerName != null && conViewerName.equals(TimeLineViewer.class.getName())) {
			if (timeLineViewer != null) {
				showTimeLineViewer();
				getMultiTierControlPanel().setViewer(timeLineViewer);
			}
		} else if (conViewerName != null && conViewerName.equals(InterlinearViewer.class.getName())) {
			if (interlinearViewer != null) {
				showInterlinearViewer();
				getMultiTierControlPanel().setViewer(interlinearViewer);
			}
		}
		
		Integer sigHeight = (Integer) Preferences.get("LayoutManager.SplitPaneDividerLocation", 
				viewerManager.getTranscription());
		if (sigHeight != null && sigHeight.intValue() > ElanLayoutManager.DEF_SIGNAL_HEIGHT) {
			if ((signalViewer != null || timeseriesViewer != null) && timeLineSplitPane != null) {
				timeLineSplitPane.setDividerLocation(sigHeight.intValue());
			}
		}
		
		sigHeight = (Integer) Preferences.get("LayoutManager.TSWavSplitPaneDividerLocation", 
				viewerManager.getTranscription());
		if (sigHeight != null && wav_tsSplitPane != null) {
			wav_tsSplitPane.setDividerLocation(sigHeight.intValue());
		}
		
		// have to set the tier for single tier viewers here because the viewer and the tier 
		// selection box are separate objects
		if (tabPane != null) {
			for (int i = 0; i < tabPane.getTabCount(); i++) {
				JComponent comp = (JComponent) tabPane.getComponent(i);
				SingleTierViewerPanel panel;
				
				if (comp instanceof SingleTierViewerPanel) {
					panel = (SingleTierViewerPanel) comp;
					if (panel.getViewer() instanceof GridViewer) {	
						Boolean multiGrid = (Boolean) Preferences.get("GridViewer.MultiTierMode", 
								viewerManager.getTranscription());
						if (multiGrid != null && multiGrid) {
							Boolean mtmSubdivision = (Boolean) Preferences.get("GridViewer.MultiTierMode.Subdivision", 
									viewerManager.getTranscription());
							if (mtmSubdivision == null || ! mtmSubdivision) {
								panel.setTierMode(GridViewer.MULTI_TIER_ASSOCIATION_MODE);
							} else {
								panel.setTierMode(GridViewer.MULTI_TIER_SUBDIVISION_MODE);
							}
						}
						String tierName = (String) Preferences.get("GridViewer.TierName",
								viewerManager.getTranscription());
						if (tierName != null) {
							panel.selectTier(tierName);
						}
					} else if (panel.getViewer() instanceof TextViewer){
						String tierName = (String) Preferences.get("TextViewer.TierName",
								viewerManager.getTranscription());
						if (tierName != null) {
							panel.selectTier(tierName);
						}	
					}
				} else if (comp == subtitlePanel) {
					Component[] subComps = comp.getComponents();
					String tierName;
					int index = 0;
					for (int j = 0; j < subComps.length; j++) {
						if (subComps[j] instanceof SingleTierViewerPanel) {
							panel = (SingleTierViewerPanel) subComps[j];							
							if (panel.getViewer() instanceof SubtitleViewer) {
								index = ((SubtitleViewer) panel.getViewer()).getViewerIndex();
								tierName = (String) Preferences.get(("SubTitleViewer.TierName-" + index),
										viewerManager.getTranscription());
								if (tierName != null) {
									panel.selectTier(tierName);
								}
							}							
						}
					}
				} 	
			}		
		}
		
		Boolean sameSize = (Boolean) Preferences.get("Media.VideosSameSize", null);
		
	    if (sameSize != null) {
	        oneRowForVisuals = ((Boolean) sameSize).booleanValue();
	    }
	    
	    Object val = Preferences.get("PreferencesDialog.Viewer.SortOrder", null);
	    if(val instanceof List){
	    	viewerSortOrder = ((List<String>)val);
	    }	        
	    val = Preferences.get("Media.VideosCentre", null);                
	    if (val instanceof Boolean) {
	    	mediaInCentre = ((Boolean) val).booleanValue();	
	    } 
	    
	    if(mediaInCentre){	    	
	    	val = Preferences.get("PreferencesDialog.Viewer.Grid.Right", null);
	    	if (val instanceof Boolean) {
	    		gridViewerLeft = !((Boolean) val).booleanValue();        	
	    	} 
	    
	    	val = Preferences.get("PreferencesDialog.Viewer.Text.Right", null);
	    	if (val instanceof Boolean) {
	    		textViewerLeft = !((Boolean) val).booleanValue();        	
	    	}   
	    
	    	val = Preferences.get("PreferencesDialog.Viewer.Subtitle.Right", null);
	    	if (val instanceof Boolean) {
	    		subtitleViewerLeft = !((Boolean) val).booleanValue();        	
	    	} 
	    	
	    	val = Preferences.get("PreferencesDialog.Viewer.Lexicon.Right", null);
	    	if (val instanceof Boolean) {
	    		lexiconViewerLeft = !((Boolean) val).booleanValue();        	
	    	} 
	    
	    	val = Preferences.get("PreferencesDialog.Viewer.Audio.Right", null);
	    	if (val instanceof Boolean) {
	    		audioRecognizerLeft = !((Boolean) val).booleanValue();        	
	    	}
	    
	    	val = Preferences.get("PreferencesDialog.Viewer.Video.Right", null);
	    	if (val instanceof Boolean) {
	    		videoRecognizerLeft = !((Boolean) val).booleanValue();        	
	    	}  
	    	
	    	val = Preferences.get("PreferencesDialog.Viewer.MetaData.Right", null);
	    	if (val instanceof Boolean) {
	    		metaDataLeft = !((Boolean) val).booleanValue();        	
	    	} 
	    }
	    
	    preferenceChanged = true;		
	}

	public void enableOrDisableMenus(boolean enabled) {
	}	
	
	/**
	 * This methods loads the list of shortcuts for the command actions which are can be
	 * called from a inline editbox while editing. All the other command actions which can be triggered by
	 * a keystroke are ignored because few actions where not relevant while editing. 
	 * 
	 * This list has to updated with all possible actions that can be called
	 * while editing
	 * 
	 * @author aarsom
	 * @since April 2012, for version 4.3.0
	 * 
	 */
	private void loadKSNottoBeConsumed(){		
		ksNotToBeConsumed.clear();
    	ShortcutsUtil scu = ShortcutsUtil.getInstance();
    	KeyStroke ks = null;
    	String modeName = ELANCommandFactory.ANNOTATION_MODE;
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.PLAY_SELECTION, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}  	
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.PLAY_AROUND_SELECTION, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.SELECTION_BOUNDARY, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.SELECTION_CENTER, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.CENTER_SELECTION, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
    	
    	ks = scu.getKeyStrokeForAction(ELANCommandFactory.LOOP_MODE, modeName);
    	if(ks != null){
    		ksNotToBeConsumed.add(ks);
    	}
	}

	/**Updates the shortcuts of all supported actions
	 * while editing in all Inline edit boxes.
	 * 
	 * @author aarsom
	 * @since April 2012, addedfor version 4.3.0	
	 */
	public void shortcutsChanged() {
		loadKSNottoBeConsumed();
		if(timeLineViewer != null){
			timeLineViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
		}
		
		if(viewerManager.getGridViewer() != null){
			viewerManager.getGridViewer().setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
		}
		
		Vector<SubtitleViewer> sViewers = viewerManager.getSubtitleViewers();
		for(SubtitleViewer v : sViewers){
			if(v != null){
				v.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
			}
		}
		
		if(interlinearViewer != null){
			interlinearViewer.setKeyStrokesNotToBeConsumed(ksNotToBeConsumed);
		}	
	}
}