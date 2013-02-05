package mpi.eudico.client.annotator.commands;

import mpi.eudico.client.annotator.ElanLayoutManager;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.export.ExportRecogTiersDialog;
import mpi.eudico.client.annotator.timeseries.TSTrackManager;
import mpi.eudico.client.util.TableSubHeaderObject;

import mpi.eudico.server.corpora.clom.Transcription;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/**
 * DOCUMENT ME!
 * $Id: ELANCommandFactory.java 31993 2012-07-15 21:22:23Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class ELANCommandFactory {
	// all Hashtables have Transcription as their key
	private static Hashtable commandActionHash = new Hashtable();
	private static Hashtable undoCAHash = new Hashtable();
	private static Hashtable redoCAHash = new Hashtable();
	private static Hashtable commandHistoryHash = new Hashtable();

	//	private UndoCA undoCA = null;
	//	private RedoCA redoCA = null;
	//	private CommandHistory commandHistory;
	// the viewer manager for this document / frame
	//	public ViewerManager2 viewerManager = null;
	//	public ElanLayoutManager layoutManager = null;
	private static Hashtable viewerManagerHash = new Hashtable();
	private static Hashtable layoutManagerHash = new Hashtable();
	private static Hashtable trackManagerHash = new Hashtable();

	// root frame for dialogs
	//	public JFrame frame = null;
	private static Hashtable rootFrameHash = new Hashtable();
	
	// a table for the available languages 
	private static final Hashtable languages = new Hashtable();

	// list of commands/command actions

	/** Holds value of property DOCUMENT ME! */
	public static final String SET_TIER_NAME = "CommandActions.SetTierName";

	/** Holds value of property DOCUMENT ME! */
	public static final String CHANGE_TIER = "Menu.Tier.ChangeTier";

	/** Holds value of property DOCUMENT ME! */
	public static final String ADD_TIER = "Menu.Tier.AddNewTier";

	/** Holds value of property DOCUMENT ME! */
	public static final String DELETE_TIER = "Menu.Tier.DeleteTier";
	public static final String DELETE_TIERS = "Menu.Tier.DeleteTiers"; 

	/** Holds value of property DOCUMENT ME! */
	public static final String EDIT_TIER = "CommandActions.EditTier";
	
	public static final String IMPORT_TIERS = "Menu.Tier.ImportTiers";
	
	public static final String ADD_PARTICIPANT= "Menu.Tier.AddParticipant";
	
	public static final String ADD_PARTICIPANT_DLG = "AddParticipantDlg";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String EDIT_TYPE = "CommandActions.EditType";
	
	public static final String IMPORT_TYPES =  "Menu.Type.ImportTypes";

	/** Holds value of property DOCUMENT ME! */
	public static final String ADD_TYPE = "Menu.Type.AddNewType";

	/** Holds value of property DOCUMENT ME! */
	public static final String CHANGE_TYPE = "Menu.Type.ChangeType";

	/** Holds value of property DOCUMENT ME! */
	public static final String DELETE_TYPE = "Menu.Type.DeleteType";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String ADD_CV = "CommandActions.AddCV";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String CHANGE_CV = "CommandActions.ChangeCV";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String DELETE_CV = "CommandActions.DeleteCV";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String REPLACE_CV = "CommandActions.ReplaceCV";
	
	public static final String MERGE_CVS = "CommandActions.MergeCV";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String ADD_CV_ENTRY = "CommandActions.AddCVEntry";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String CHANGE_CV_ENTRY = "CommandActions.ChangeCVEntry";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String DELETE_CV_ENTRY = "CommandActions.DeleteCVEntry";
	
	/** only for Command: String is currently not in the language files! */
	public static final String MOVE_CV_ENTRIES = "MoveEntries";
	
	/** only for Command: String is currently not in the language files! */
	public static final String REPLACE_CV_ENTRIES = "ReplaceEntries";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String EDIT_CV_DLG = "Menu.Edit.EditCV"; 

	/** Holds value of property DOCUMENT ME! */
	public static final String NEW_ANNOTATION = "Menu.Annotation.NewAnnotation";
	public static final String NEW_ANNOTATION_REC = "Menu.Annotation.NewAnnotationRecursive";
	public static final String CREATE_DEPEND_ANN = "Menu.Annotation.CreateDependingAnnotations";

	public static final String NEW_ANNOTATION_ALT = "NA_Alt";
	/** Holds value of property DOCUMENT ME! */
	public static final String NEW_ANNOTATION_BEFORE = "Menu.Annotation.NewAnnotationBefore";

	/** Holds value of property DOCUMENT ME! */
	public static final String NEW_ANNOTATION_AFTER = "Menu.Annotation.NewAnnotationAfter";

	/** Holds value of property DOCUMENT ME! */
	public static final String MODIFY_ANNOTATION = "Menu.Annotation.ModifyAnnotation";
	public static final String MODIFY_ANNOTATION_ALT = "MA_Alt";
	
	public static final String MODIFY_ANNOTATION_DC = "Menu.Annotation.ModifyAnnotationDatCat";
	public static final String MODIFY_ANNOTATION_DC_DLG = "ModifyAnnotationDCDlg";
	
	public static final String SPLIT_ANNOTATION = "Menu.Annotation.SplitAnnotation";

	/** Holds value of property DOCUMENT ME! */
	public static final String MODIFY_ANNOTATION_DLG = "ModifyAnnotationDialog";
    public static final String REMOVE_ANNOTATION_VALUE = "Menu.Annotation.RemoveAnnotationValue";

	/** Holds value of property DOCUMENT ME! */
	public static final String DELETE_ANNOTATION = "Menu.Annotation.DeleteAnnotation";
	public static final String DELETE_ANNOTATION_ALT = "DA_Alt"; 
	public static final String DELETE_ANNOS_IN_SELECTION = "Menu.Annotation.DeleteAnnotationsInSelection";
	public static final String DELETE_ANNOS_LEFT_OF = "Menu.Annotation.DeleteAnnotationsLeftOf";
	public static final String DELETE_ANNOS_RIGHT_OF = "Menu.Annotation.DeleteAnnotationsRightOf";
	public static final String DELETE_ALL_ANNOS_LEFT_OF = "Menu.Annotation.DeleteAllLeftOf";
	public static final String DELETE_ALL_ANNOS_RIGHT_OF = "Menu.Annotation.DeleteAllRightOf";
	public static final String DELETE_MULTIPLE_ANNOS = "Menu.Annotation.DeleteSelectedAnnotations";
	
	public static final String  COPY_ANNOTATION = "Menu.Annotation.CopyAnnotation";
	public static final String  COPY_ANNOTATION_TREE = "Menu.Annotation.CopyAnnotationTree";
	public static final String  PASTE_ANNOTATION = "Menu.Annotation.PasteAnnotation";
	public static final String  PASTE_ANNOTATION_HERE = "Menu.Annotation.PasteAnnotationHere";
	public static final String  PASTE_ANNOTATION_TREE = "Menu.Annotation.PasteAnnotationTree";
	public static final String  PASTE_ANNOTATION_TREE_HERE = "Menu.Annotation.PasteAnnotationTreeHere";
	public static final String  DUPLICATE_ANNOTATION = "Menu.Annotation.DuplicateAnnotation";
	public static final String  DUPLICATE_REMOVE_ANNOTATION = "Menu.Annotation.DuplicateRemoveAnnotation";
	public static final String  MERGE_ANNOTATION_WN = "Menu.Annotation.MergeWithNext";
	public static final String  MERGE_ANNOTATION_WB = "Menu.Annotation.MergeWithBefore";
	public static final String  MOVE_ANNOTATION_TO_TIER = "Menu.Annotation.MoveAnnotationToTier";// not visible in ui
	/** Holds value of property DOCUMENT ME! */
    public static final String COPY_TO_NEXT_ANNOTATION = "CommandActions.CopyToNextAnnotation";    

	/** Holds value of property DOCUMENT ME! */
	public static final String MODIFY_ANNOTATION_TIME = "CommandActions.ModifyAnnotationTime";

	/** Holds value of property DOCUMENT ME! */
	public static final String MODIFY_GRAPHIC_ANNOTATION = "Menu.Annotation.ModifyGraphicAnnotation";

	/** Holds value of property DOCUMENT ME! */
	public static final String MODIFY_GRAPHIC_ANNOTATION_DLG = "ModifyGraphicAnnotationDialog";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String SHIFT_ALL_DLG = "ShiftAllAnn";
	public static final String SHIFT_ANN_DLG = "ShiftAnn";
	public static final String SHIFT_ANN_ALLTIER_DLG = "ShiftAnnAllTier";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String TRANS_TABLE_CLM_NO = "TranscriptionTable.Column.No";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String SHIFT_ANNOTATIONS = "CommandActions.ShiftAnnotations";
	public static final String SHIFT_ALL_ANNOTATIONS = "Menu.Annotation.ShiftAll";
	public static final String SHIFT_ALL_ANNOTATIONS_LROf = "CommandActions.ShiftAnnotationsLROf";
	public static final String SHIFT_ACTIVE_ANNOTATION = "Menu.Annotation.ShiftActiveAnnotation";
	public static final String SHIFT_ANNOS_IN_SELECTION = "Menu.Annotation.ShiftAnnotationsInSelection";
	public static final String SHIFT_ANNOS_LEFT_OF = "Menu.Annotation.ShiftAnnotationsLeftOf";
	public static final String SHIFT_ANNOS_RIGHT_OF = "Menu.Annotation.ShiftAnnotationsRightOf";
	public static final String SHIFT_ALL_ANNOS_LEFT_OF = "Menu.Annotation.ShiftAllLeftOf";
	public static final String SHIFT_ALL_ANNOS_RIGHT_OF = "Menu.Annotation.ShiftAllRightOf";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String GRID_VIEWER = "Menu.View.Viewers.Grid";
	public static final String TEXT_VIEWER = "Menu.View.Viewers.Text";
	public static final String SUBTITLE_VIEWER = "Menu.View.Viewers.Subtitles";
	public static final String LEXICON_VIEWER = "LexiconEntryViewer.Lexicon";
	public static final String AUDIO_RECOGNIZER = "Menu.View.Viewers.Audio";
	public static final String VIDEO_RECOGNIZER = "Menu.View.Viewers.Video";	
	public static final String METADATA_VIEWER = "Menu.View.Viewers.MetaData";
	public static final String SIGNAL_VIEWER = "Menu.View.Viewers.Signal";	
	public static final String INTERLINEAR_VIEWER = "Menu.View.Viewers.InterLinear";
	public static final String INTERLINEAR_LEXICON_VIEWER = "Menu.View.Viewers.InterLinearize";
	public static final String TIMESERIES_VIEWER = "Menu.View.Viewers.TimeSeries";
	
		
	/** Holds value of property DOCUMENT ME! */
	public static final String TOKENIZE_DLG = "Menu.Tier.Tokenize";
	public static final String ANN_ON_DEPENDENT_TIER = "Menu.Tier.AnnotationsOnDependentTiers";
	public static final String ANN_FROM_OVERLAP = "Menu.Tier.AnnotationsFromOverlaps";
	public static final String ANN_FROM_OVERLAP_CLAS = "Menu.Tier.AnnotationsFromOverlapsClas";
	public static final String ANN_FROM_SUBTRACTION = "Menu.Tier.AnnotationsFromSubtraction";
	public static final String ANN_FROM_GAPS = "Menu.Tier.AnnotationsFromGaps";
	public static final String COMPARE_ANNOTATORS_DLG = "Menu.Tier.CompareAnnotators";
	public static final String CHANGE_CASE = "Menu.Tier.ChangeCase";
	
	/** only used as internal identifier for a command! */
	public static final String ANN_ON_DEPENDENT_TIER_COM = "AnnsOnDependentTier";
	/** only used as internal identifier for a command! */
	public static final String ANN_FROM_OVERLAP_COM = "AnnsFromOverlaps";
	public static final String ANN_FROM_SUBTRACTION_COM = "AnnsFromSubtraction";
	public static final String ANN_FROM_OVERLAP_COM_CLAS = "AnnsFromOverlapsClas";
	/** only used as internal identifier for a command! */
	public static final String ANN_FROM_GAPS_COM = "AnnsFromGaps";
	/** only used as internal identifier for a command! */
	public static final String CHANGE_CASE_COM = "ChangeCase";
	
	public static final String MERGE_TIERS = "Menu.Tier.MergeTiers";
	/** only internal */
	public static final String MERGE_TIERS_DLG = "MergeTiersDlg";
	
	public static final String MERGE_TIER_GROUP = "Menu.Tier.MergeTierGroup";
	/** only internal */
	public static final String MERGE_TIER_GROUP_DLG = "MergeTierGroupDlg";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String TOKENIZE_TIER = "CommandActions.Tokenize";

    /** Holds value of property DOCUMENT ME! */
    public static final String REGULAR_ANNOTATION_DLG = "Menu.Tier.RegularAnnotation";   

    /** Holds value of property DOCUMENT ME! */
    public static final String REGULAR_ANNOTATION = "CommandActions.RegularAnnotation";
    
	/** Holds value of property DOCUMENT ME! */
	public static final String SHOW_TIMELINE = "Menu.View.ShowTimeline";

	/** Holds value of property DOCUMENT ME! */
	public static final String SHOW_INTERLINEAR = "Menu.View.ShowInterlinear";

	/** Holds value of property DOCUMENT ME! */
	public static final String SHOW_MULTITIER_VIEWER = "Commands.ShowMultitierViewer";

	/** Holds value of property DOCUMENT ME! */
	public static final String GOTO_DLG = "Menu.Search.GoTo";

	/** Holds value of property DOCUMENT ME! */
	public static final String SEARCH_DLG = "Menu.Search.Find";

	/** Holds string for search in multiple files */
	public static final String SEARCH_MULTIPLE_DLG = "Menu.Search.Multiple";
	
	/** Holds string for structured search in multiple files */
	public static final String STRUCTURED_SEARCH_MULTIPLE_DLG = "Menu.Search.StructuredMultiple";
	
	/** Command action name of replacing matches with string */
	public static final String REPLACE = "CommandActions.Replace";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String TIER_DEPENDENCIES = "Menu.View.Dependencies";

	/** Holds value of property DOCUMENT ME! */
	public static final String SHORTCUTS = "Menu.View.Shortcuts";
	
    /** Holds value of property DOCUMENT ME! */
    public static final String SPREADSHEET = "Menu.View.SpreadSheet";
    
    /** Holds value of property DOCUMENT ME! */
    public static final String STATISTICS = "Menu.View.Statistics";

	/** Holds value of property DOCUMENT ME! */
	public static final String SYNC_MODE = "Menu.Options.SyncMode";

	/** Holds value of property DOCUMENT ME! */
	public static final String ANNOTATION_MODE = "Menu.Options.AnnotationMode";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String TRANSCRIPTION_MODE = "Menu.Options.TranscriptionMode";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String SEGMENTATION_MODE = "Menu.Options.SegmentationMode";
	
	public static final String INTERLINEARIZATION_MODE = "Menu.Options.InterlinearizationMode";

	/** Holds value of property DOCUMENT ME! */
	public static final String BULLDOZER_MODE = "Menu.Options.BulldozerMode";

	/** Holds value of property DOCUMENT ME! */
	public static final String TIMEPROP_NORMAL = "Menu.Options.NormalPropagationMode";

	/** Holds value of property DOCUMENT ME! */
	public static final String SHIFT_MODE = "Menu.Options.ShiftMode";

	/** Holds value of property DOCUMENT ME! */
	public static final String SELECTION_MODE = "CommandActions.SelectionMode";

	/** Holds value of property DOCUMENT ME! */
	public static final String LOOP_MODE = "CommandActions.LoopMode";

	/** Holds value of property DOCUMENT ME! */
	public static final String CLEAR_SELECTION = "Menu.Play.ClearSelection";
	public static final String CLEAR_SELECTION_ALT = "CS_Alt";
	public static final String CLEAR_SELECTION_AND_MODE = "Menu.Play.ClearSelectionAndMode";

	/** Holds value of property DOCUMENT ME! */
	public static final String PLAY_SELECTION = "Menu.Play.PlaySelection";

	/** Holds value of property DOCUMENT ME! */
	public static final String PLAY_AROUND_SELECTION = "CommandActions.PlayAroundSelection";

	/** Holds value of property DOCUMENT ME! */
	public static final String PLAY_AROUND_SELECTION_DLG =
		"Menu.Options.PlayAroundSelectionDialog";
	
	public static final String PLAYBACK_TOGGLE_DLG = "Menu.Options.PlaybackToggleDialog";
	
	public static final String PLAYBACK_TOGGLE = "PLAYBACK_TOGGLE";
	
	public static final String PLAYBACK_RATE_TOGGLE = "CommandActions.PlaybackRateToggle";
	
	public static final String PLAYBACK_VOLUME_TOGGLE = "CommandActions.PlaybackVolumeToggle";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String SET_PAL = "Menu.Options.FrameLength.PAL";

	/** Holds value of property DOCUMENT ME! */
	public static final String SET_NTSC = "Menu.Options.FrameLength.NTSC";

	/** Holds value of property DOCUMENT ME! */
	public static final String NEXT_FRAME = "Menu.Play.Next";

	/** Holds value of property DOCUMENT ME! */
	public static final String PREVIOUS_FRAME = "Menu.Play.Previous";

	/** Holds value of property DOCUMENT ME! */
	public static final String PLAY_PAUSE = "Menu.Play.PlayPause";

	/** Holds value of property DOCUMENT ME! */
	public static final String GO_TO_BEGIN = "Menu.Play.GoToBegin";

	/** Holds value of property DOCUMENT ME! */
	public static final String GO_TO_END = "Menu.Play.GoToEnd";

	/** Holds value of property DOCUMENT ME! */
	public static final String PREVIOUS_SCROLLVIEW = "Menu.Play.GoToPreviousScrollview";

	/** Holds value of property DOCUMENT ME! */
	public static final String NEXT_SCROLLVIEW = "Menu.Play.GoToNextScrollview";

	/** Holds value of property DOCUMENT ME! */
	public static final String PIXEL_LEFT = "Menu.Play.1PixelLeft";

	/** Holds value of property DOCUMENT ME! */
	public static final String PIXEL_RIGHT = "Menu.Play.1PixelRight";

	/** Holds value of property DOCUMENT ME! */
	public static final String SECOND_LEFT = "Menu.Play.1SecLeft";

	/** Holds value of property DOCUMENT ME! */
	public static final String SECOND_RIGHT = "Menu.Play.1SecRight";

	/** Holds value of property DOCUMENT ME! */
	public static final String SELECTION_BOUNDARY = "Menu.Play.ToggleCrosshairInSelection";
	/** alternative due to keyboard problems */
	public static final String SELECTION_BOUNDARY_ALT = "SB_Alt";
	/** Holds value of property DOCUMENT ME! */
	public static final String SELECTION_CENTER = "Menu.Play.MoveCrosshairToCenterOfSelection";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String ACTIVE_ANNOTATION = "Commands.ActiveAnnotation";// not in language file
	public static final String ACTIVE_ANNOTATION_EDIT = "CommandActions.OpenInlineEditBox";

	/** Holds value of property DOCUMENT ME! */
	public static final String PREVIOUS_ANNOTATION = "CommandActions.PreviousAnnotation";
	public static final String PREVIOUS_ANNOTATION_EDIT = "CommandActions.PreviousAnnotationEdit";

	/** Holds value of property DOCUMENT ME! */
	public static final String NEXT_ANNOTATION = "CommandActions.NextAnnotation";
	public static final String NEXT_ANNOTATION_EDIT = "CommandActions.NextAnnotationEdit";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String COPY_CURRENT_TIME = "CommandActions.CopyCurrentTime";

	/** Holds value of property DOCUMENT ME! */
	public static final String ANNOTATION_UP = "CommandActions.AnnotationUp";

	/** Holds value of property DOCUMENT ME! */
	public static final String ANNOTATION_DOWN = "CommandActions.AnnotationDown";

	/** Holds value of property DOCUMENT ME! */
	public static final String SET_LOCALE = "Menu.Options.Language";

	/** Holds value of property DOCUMENT ME! */
	public static final String CATALAN = "Catal\u00E0";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String DUTCH = "Nederlands";

	/** Holds value of property DOCUMENT ME! */
	public static final String ENGLISH = "English";
	
	public static final String SPANISH = "Espa\u00F1ol";
	
	public static final String SWEDISH = "Svenska";
	
	public static final String GERMAN ="Deutsch";
	
	public static final String PORTUGUESE = "Portugu\u00EAs";
	public static final String FRENCH = "Fran\u00E7ais";
	public static final String JAPANESE = "\u65e5\u672c\u8a9e";
	public static final String CHINESE_SIMPL = "\uFEFF\u7B80\u4F53\u4E2D\u6587";
	public static final String RUSSIAN = "\u0420\u0443\u0441\u0441\u043a\u0438\u0439";
	
	public static final String CUSTOM_LANG = "Menu.Options.Language.Custom";

	/** Holds value of property DOCUMENT ME! */
	public static final String SAVE = "Menu.File.Save";

	/** Holds value of property DOCUMENT ME! */
	public static final String SAVE_AS = "Menu.File.SaveAs";

	/** Holds value of property DOCUMENT ME! */
	public static final String SAVE_AS_TEMPLATE = "Menu.File.SaveAsTemplate";
	
	public static final String SAVE_SELECTION_AS_EAF = "Menu.File.SaveSelectionAsEAF";

	/** Holds value of property DOCUMENT ME! */
	public static final String STORE = "Commands.Store";

	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_TAB = "Menu.File.Export.Tab";

	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_TEX = "Menu.File.Export.TeX";

	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_TIGER = "Menu.File.Export.Tiger";

	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_QT_SUB = "Menu.File.Export.QtSub";
	public static final String EXPORT_SUBTITLES = "Menu.File.Export.Subtitles";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_SMIL_RT = "Menu.File.Export.Smil.RealPlayer";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_SMIL_QT = "Menu.File.Export.Smil.QuickTime";

	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_SHOEBOX = "Menu.File.Export.Shoebox";

	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_CHAT = "Menu.File.Export.CHAT";

	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_MEDIA = "Menu.File.Export.Media";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String EXPORT_IMAGE_FROM_WINDOW = "Menu.File.Export.ImageFromWindow";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String BACKUP = "CommandActions.Backup";

	/** Holds value of property DOCUMENT ME! */
	public static final String BACKUP_NEVER = "Menu.File.Backup.Never";
	
	public static final String BACKUP_1 = "Menu.File.Backup.1";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String BACKUP_5 = "Menu.File.Backup.5";

	/** Holds value of property DOCUMENT ME! */
	public static final String BACKUP_10 = "Menu.File.Backup.10";

	/** Holds value of property DOCUMENT ME! */
	public static final String BACKUP_20 = "Menu.File.Backup.20";

	/** Holds value of property DOCUMENT ME! */
	public static final String BACKUP_30 = "Menu.File.Backup.30";

	/** Holds value of property DOCUMENT ME! */
	public static final String PRINT = "Menu.File.Print";

	/** Holds value of property DOCUMENT ME! */
	public static final String PREVIEW = "Menu.File.PrintPreview";

	/** Holds value of property DOCUMENT ME! */
	public static final String PAGESETUP = "Menu.File.PageSetup";

	/** Holds value of property DOCUMENT ME! */
	public static final String REDO = "Menu.Edit.Redo";

	/** Holds value of property DOCUMENT ME! */
	public static final String UNDO = "Menu.Edit.Undo";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String LINKED_FILES_DLG = "Menu.Edit.LinkedFiles";
	
	/** Holds value of property DOCUMENT ME! */
	public static final String CHANGE_LINKED_FILES = "CommandActions.ChangeLinkedFiles";
	public static final String ADD_SEGMENTATION = "CommandActions.AddSegmentation";
	
	public static final String FILTER_TIER = "Menu.Tier.FilterTier";
	public static final String FILTER_TIER_DLG = "Menu.Tier.FilterTierDlg";
	
	public static final String EXPORT_TRAD_TRANSCRIPT = "Menu.File.Export.TraditionalTransript";
	public static final String EXPORT_INTERLINEAR = "Menu.File.Export.Interlinear";
	public static final String EXPORT_HTML = "Menu.File.Export.HTML";
	public static final String REPARENT_TIER_DLG = "Menu.Tier.ReparentTierDialog";
	public static final String REPARENT_TIER = "Menu.Tier.ReparentTier";
	public static final String COPY_TIER = "Menu.Tier.CopyTier";
	public static final String COPY_TIER_DLG = "Menu.Tier.CopyTierDialog";
	public static final String MERGE_TRANSCRIPTIONS = "Menu.File.MergeTranscriptions";
	
	public static final String NEXT_ACTIVE_TIER = "CommandActions.NextActiveTier";
	public static final String PREVIOUS_ACTIVE_TIER = "CommandActions.PreviousActiveTier";
	public static final String ACTIVE_TIER = "ActiveTier";
	public static final String CLOSE = "Menu.File.Close";
	
	public static final String SYNTAX_VIEWER = "CommandActions.SyntaxViewer";
	
	public static final String PUBLISH_DOC = "Menu.P2P.PublishDocument";
	public static final String DISCOVER_DOC = "Menu.P2P.DiscoverDocument";
	public static final String EXT_TRACK_DATA = "CommandActions.ExtractTrackData";
	public static final String KIOSK_MODE = "Menu.Options.KioskMode";
	public static final String IMPORT_PRAAT_GRID = "Menu.File.Import.PraatTiers";
	public static final String IMPORT_PRAAT_GRID_DLG = "Praat_Grid_Dlg";
	public static final String EXPORT_PRAAT_GRID = "Menu.File.Export.Praat";
	public static final String IMPORT_RECOG_TIERS = "Menu.File.Import.RecognizerTiers";
	public static final String REMOVE_ANNOTATIONS_OR_VALUES = "Menu.Tier.RemoveAnnotationsOrValues";
	public static final String REMOVE_ANNOTATIONS_OR_VALUES_DLG = "RemoveAnnotationsOrValuesDlg";   
	public static final String LABEL_AND_NUMBER = "Menu.Tier.LabelAndNumber";
  	public static final String LABEL_N_NUM_DLG = "LabelNumDlg";
	public static final String SEGMENTS_2_TIER_DLG = "Seg2TierDlg";
	public static final String SEGMENTS_2_TIER = "CommandActions.SegmentsToTiers";
	public static final String KEY_CREATE_ANNOTATION = "CommandActions.KeyCreateAnnotation";
	public static final String EXPORT_WORDS = "Menu.File.Export.WordList";
	public static final String EXPORT_PREFS = "Menu.Edit.Preferences.Export";
	public static final String IMPORT_PREFS = "Menu.Edit.Preferences.Import";
	public static final String FONT_BROWSER = "Menu.View.FontBrowser";
	public static final String EXPORT_TOOLBOX = "Menu.File.Export.Toolbox";
	public static final String EXPORT_FILMSTRIP = "Menu.File.Export.FilmStrip";
	public static final String EXPORT_RECOG_TIER = "Menu.File.Export.RecognizerTiers";
	public static final String CENTER_SELECTION = "TimeLineViewer.CenterSelection";
	public static final String SET_AUTHOR = "Menu.Edit.Author";

	public static final String MOVE_ANNOTATION_LBOUNDARY_LEFT ="CommandActions.Annotation_LBound_Left";
	public static final String MOVE_ANNOTATION_LBOUNDARY_RIGHT ="CommandActions.Annotation_LBound_Right";
	public static final String MOVE_ANNOTATION_RBOUNDARY_LEFT ="CommandActions.Annotation_RBound_Left";
	public static final String MOVE_ANNOTATION_RBOUNDARY_RIGHT ="CommandActions.Annotation_RBound_Right";		
	
	// action keys for global, document independent actions
	public static final String NEXT_WINDOW = "Menu.Window.Next";
	public static final String PREV_WINDOW = "Menu.Window.Previous";
	public static final String EDIT_PREFS = "Menu.Edit.Preferences.Edit";
	public static final String EDIT_SHORTCUTS = "Menu.Edit.Preferences.Shortcut";
	public static final String REPLACE_MULTIPLE = "Menu.Search.FindReplaceMulti";
	public static final String NEW_DOC = "Menu.File.New";
	public static final String OPEN_DOC = "Menu.File.Open";
	public static final String EXPORT_TOOLBOX_MULTI = "Menu.File.Export.Toolbox";
	public static final String EXPORT_PRAAT_MULTI = "Menu.File.Export.Praat";
	public static final String EXPORT_TAB_MULTI = "Menu.File.Export.Tab";
	public static final String EXPORT_ANNLIST_MULTI = "Menu.File.Export.AnnotationListMulti";
	public static final String EXPORT_WORDLIST_MULTI = "Menu.File.Export.WordList";
	public static final String EXPORT_TIERS_MULTI = "Menu.File.Export.Tiers";
	public static final String EXPORT_OVERLAPS_MULTI = "Menu.File.Export.OverlapsMulti";
	public static final String IMPORT_SHOEBOX = "Menu.File.Import.Shoebox";
	public static final String IMPORT_TOOLBOX = "Menu.File.Import.Toolbox";
	public static final String IMPORT_CHAT = "Menu.File.Import.CHAT";
	public static final String IMPORT_TRANS = "Menu.File.Import.Transcriber";
	public static final String IMPORT_TAB = "Menu.File.Import.Delimited";
	public static final String IMPORT_FLEX = "Menu.File.Import.FLEx";
	public static final String EXIT = "Menu.File.Exit";
	public static final String HELP = "Menu.Help.Contents";
	public static final String ABOUT ="Menu.Help.About";
	public static final String CLIP_MEDIA = "Menu.File.Export.MediaWithScript";
	public static final String ADD_TRACK_AND_PANEL = "AddTSTrackAndPanel";
	// hier... add to shortcuts
	public static final String CREATE_NEW_MULTI = "Menu.File.MultiEAFCreation";
	public static final String EDIT_MULTIPLE_FILES = "Menu.File.Process.EditMF";
	public static final String SCRUB_MULTIPLE_FILES = "Menu.File.ScrubTranscriptions";
	public static final String ANNOTATION_OVERLAP_MULTI = "Menu.File.MultipleFileAnnotationFromOverlaps";
	public static final String ANNOTATION_SUBTRACTION_MULTI = "Menu.File.MultipleFileAnnotationFromSubtraction";
	public static final String STATISTICS_MULTI = "Menu.File.MultiFileStatistics";
	
	public static final String EDIT_LEX_SRVC_DLG = "Menu.Edit.EditLexSrvc";
	public static final String ADD_LEX_LINK = "CommandActions.AddLexLink";
	public static final String CHANGE_LEX_LINK = "CommandActions.ChangeLexLink";
	public static final String DELETE_LEX_LINK = "CommandActions.DeleteLexLink";
	public static final String PLAY_STEP_AND_REPEAT = "Menu.Play.PlayStepAndRepeat";	
	
	// Transcription mode actions
	public static final String COMMIT_CHANGES = "TranscriptionMode.Actions.CommitChanges";
	public static final String CANCEL_CHANGES = "TranscriptionMode.Actions.CancelChanges";
	public static final String MOVE_UP = "TranscriptionMode.Actions.MoveUp";
	public static final String MOVE_DOWN = "TranscriptionMode.Actions.MoveDown";
	public static final String MOVE_LEFT = "TranscriptionMode.Actions.MoveLeft";
	public static final String MOVE_RIGHT = "TranscriptionMode.Actions.MoveRight";
	public static final String PLAY_FROM_START = "TranscriptionMode.Actions.PlayFromStart";
	public static final String HIDE_TIER = "TranscriptionTable.Label.HideLinkedTiers";
	public static final String FREEZE_TIER = "TranscriptionMode.Actions.FreezeTier";
	public static final String EDIT_IN_ANN_MODE = "TranscriptionTableEditBox.EditInAnnotationMode";
	
	// Segmentation mode actions	
	public static final String SEGMENT = "SegmentationMode.Actions.Segment";
	
	public static final String COMMON_SHORTCUTS = "Shortcuts.Common";
	
	public static final String UPDATE_ELAN = "Menu.Options.CheckForUpdate";
	public static final String WEBSERVICES_DLG = "Menu.Options.WebServices";
	
	/** a list of commandactions that have a keyboard shortcut 
	 * changes in this list should be followed by changes in 
	 * getShortCutText */
	private static final String[] commandConstants =
		{
			NEW_ANNOTATION,
			NEW_ANNOTATION_ALT,
			NEW_ANNOTATION_BEFORE,
			NEW_ANNOTATION_AFTER,
			KEY_CREATE_ANNOTATION,
			COPY_ANNOTATION,
			COPY_ANNOTATION_TREE,
			COPY_CURRENT_TIME,
			PASTE_ANNOTATION,
			PASTE_ANNOTATION_HERE,
			PASTE_ANNOTATION_TREE,
			PASTE_ANNOTATION_TREE_HERE,
			DUPLICATE_ANNOTATION,
			COPY_TO_NEXT_ANNOTATION,
			MODIFY_ANNOTATION,
			MODIFY_ANNOTATION_ALT,
			MODIFY_ANNOTATION_TIME,
			SHIFT_ACTIVE_ANNOTATION,
			REMOVE_ANNOTATION_VALUE,
			DELETE_ANNOTATION,
			DELETE_ANNOTATION_ALT,
			PREVIOUS_ANNOTATION,
			PREVIOUS_ANNOTATION_EDIT,
			NEXT_ANNOTATION,
			NEXT_ANNOTATION_EDIT,
			ANNOTATION_UP,
			ANNOTATION_DOWN,
			ADD_TIER,
			DELETE_TIER,
			DELETE_TIERS,
			PREVIOUS_ACTIVE_TIER,
			NEXT_ACTIVE_TIER,
			ADD_TYPE,
			CLEAR_SELECTION,
			CLEAR_SELECTION_ALT,
			CLEAR_SELECTION_AND_MODE,
			SELECTION_BOUNDARY,
			SELECTION_BOUNDARY_ALT,
			SELECTION_CENTER,
			SELECTION_MODE,
			CENTER_SELECTION,
			PLAY_PAUSE,
			PLAY_SELECTION,
			PLAY_AROUND_SELECTION,
			PIXEL_LEFT,
			PIXEL_RIGHT,
			PREVIOUS_FRAME,
			NEXT_FRAME,
			SECOND_LEFT,
			SECOND_RIGHT,
			PREVIOUS_SCROLLVIEW,
			NEXT_SCROLLVIEW,
			GO_TO_BEGIN,
			GO_TO_END,
			GOTO_DLG,
			LOOP_MODE,
			SAVE,
			SAVE_AS,
			SAVE_AS_TEMPLATE,
			PRINT,
			PREVIEW,
			PAGESETUP,
			SEARCH_DLG,
			SEARCH_MULTIPLE_DLG,
			STRUCTURED_SEARCH_MULTIPLE_DLG,
			PLAYBACK_RATE_TOGGLE,
			PLAYBACK_VOLUME_TOGGLE,
			LINKED_FILES_DLG,
			EDIT_CV_DLG,
			MOVE_ANNOTATION_LBOUNDARY_LEFT,
			MOVE_ANNOTATION_LBOUNDARY_RIGHT,
			MOVE_ANNOTATION_RBOUNDARY_LEFT,
			MOVE_ANNOTATION_RBOUNDARY_RIGHT };

	
	/*    public ELANCommandFactory(JFrame fr, ViewerManager2 vm, ElanLayoutManager lm) {
	   commandActions = new Hashtable();
	   commandHistory = new CommandHistory(CommandHistory.historySize);
	   frame = fr;
	   viewerManager = vm;
	   layoutManager = lm;
	   }
	 */
	
	static {
		languages.put(CATALAN, ElanLocale.CATALAN);
		languages.put(CHINESE_SIMPL, ElanLocale.CHINESE_SIMP);
		languages.put(DUTCH, ElanLocale.DUTCH);
		languages.put(ENGLISH, ElanLocale.ENGLISH);
		languages.put(RUSSIAN, ElanLocale.RUSSIAN);
		languages.put(SPANISH, ElanLocale.SPANISH);
		languages.put(SWEDISH, ElanLocale.SWEDISH);
		languages.put(GERMAN, ElanLocale.GERMAN);
		languages.put(PORTUGUESE, ElanLocale.PORTUGUESE);
		languages.put(FRENCH, ElanLocale.FRENCH);
		languages.put(JAPANESE, ElanLocale.JAPANESE);
		languages.put(CUSTOM_LANG, ElanLocale.CUSTOM);
	}
	
	public static void addDocument(JFrame fr, ViewerManager2 vm, ElanLayoutManager lm) {
		Transcription t = vm.getTranscription();

		if (rootFrameHash.get(t) == null) {
			rootFrameHash.put(t, fr);
		}

		if (viewerManagerHash.get(t) == null) {
			viewerManagerHash.put(t, vm);
		}

		if (layoutManagerHash.get(t) == null) {
			layoutManagerHash.put(t, lm);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param vm DOCUMENT ME!
	 */
	public static void removeDocument(ViewerManager2 vm) {
		if (vm != null) {
			Transcription t = vm.getTranscription();

			commandActionHash.remove(t);
			undoCAHash.remove(t);
			redoCAHash.remove(t);
			commandHistoryHash.remove(t);
			viewerManagerHash.remove(t);
			layoutManagerHash.remove(t);
			rootFrameHash.remove(t);
			trackManagerHash.remove(t);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param forTranscription DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static JFrame getRootFrame(Transcription forTranscription) {
		if(forTranscription == null){
			return null;
		}
		return (JFrame) (rootFrameHash.get(forTranscription));
	}

	// TEST METHOD FOR DEALING WITH JXTA MESSAGES

	/*    public static CommandAction getCommandAction(String transcriptionID, String caName) {
	   CommandAction ca = null;
	
	   if (transcriptionID.equals("fake")) {
	       Transcription t = null;
	
	       Set keySet = viewerManagerHash.keySet();
	
	       Iterator iter = keySet.iterator();
	       while (iter.hasNext()) {
	           t = (Transcription) iter.next();
	       }
	
	       if (t != null) {
	           ca = getCommandAction(t, caName);
	       } else {
	           System.out.println("no transcription found for id: " + transcriptionID);
	       }
	   }
	   return ca;
	   }
	 */
	public static ViewerManager2 getViewerManager(Transcription forTranscription) {
		return (ViewerManager2) (viewerManagerHash.get(forTranscription));
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param forTranscription DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static ElanLayoutManager getLayoutManager(Transcription forTranscription) {
		return (ElanLayoutManager) (layoutManagerHash.get(forTranscription));
	}

	/**
	 * Creation of the a track manager is postponed until it is necessary: when at least 
	 * one time series source has been added.
	 * 
	 * @param forTranscription the document / transcription
	 * @param trackManager the manager for tracks and track sources
	 */
	public static void addTrackManager(Transcription forTranscription, TSTrackManager trackManager) {
		if (forTranscription != null && trackManager != null) {
			trackManagerHash.put(forTranscription, trackManager);		
		}
	}
	
	/**
	 * Returns the time series track manager for the transcription.
	 * 
	 * @param forTranscription the transcription
	 * @return the track manager or null
	 */
	public static TSTrackManager getTrackManager(Transcription forTranscription) {
		return (TSTrackManager) trackManagerHash.get(forTranscription);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tr DOCUMENT ME!
	 * @param caName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static CommandAction getCommandAction(Transcription tr, String caName) {
		CommandAction ca = null;

		if (commandActionHash.get(tr) == null) {
			commandActionHash.put(tr, new Hashtable());
		}

		if (commandHistoryHash.get(tr) == null) {
			commandHistoryHash.put(tr, new CommandHistory(CommandHistory.historySize));
		}

		ViewerManager2 viewerManager = (ViewerManager2) viewerManagerHash.get(tr);
		ElanLayoutManager layoutManager = (ElanLayoutManager) layoutManagerHash.get(tr);

		if (caName.equals(SET_TIER_NAME)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SET_TIER_NAME));

			if (ca == null) {
				ca = new SetTierNameCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SET_TIER_NAME, ca);
			}
		}
		else if (caName.equals(ADD_TIER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(ADD_TIER));

			if (ca == null) {
				ca = new AddTierDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ADD_TIER, ca);
			}
		}
		else if (caName.equals(CHANGE_TIER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CHANGE_TIER));

			if (ca == null) {
				ca = new ChangeTierDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CHANGE_TIER, ca);
			}
		}
		else if (caName.equals(DELETE_TIER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_TIER));

			if (ca == null) {
				ca = new DeleteTierDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_TIER, ca);
			}
		}
		else if (caName.equals(DELETE_TIERS)) {
	           ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_TIERS));

	           if (ca == null) {
	               ca = new DeleteTierDlgCA(viewerManager);
	               ((Hashtable) commandActionHash.get(tr)).put(DELETE_TIERS, ca);
	           }
	       } 
		else if (caName.equals(ADD_PARTICIPANT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(ADD_PARTICIPANT));

			if (ca == null) {
				ca = new AddParticipantCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ADD_PARTICIPANT, ca);
			}
		}
		else if (caName.equals(IMPORT_TIERS)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(IMPORT_TIERS));

			if (ca == null) {
				ca = new ImportTiersDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(IMPORT_TIERS, ca);
			}
		}
		else if (caName.equals(ADD_TYPE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(ADD_TYPE));

			if (ca == null) {
				ca = new AddLingTypeDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ADD_TYPE, ca);
			}
		}
		else if (caName.equals(CHANGE_TYPE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CHANGE_TYPE));

			if (ca == null) {
				ca = new ChangeLingTypeDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CHANGE_TYPE, ca);
			}
		}
		else if (caName.equals(DELETE_TYPE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_TYPE));

			if (ca == null) {
				ca = new DeleteLingTypeDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_TYPE, ca);
			}
		}
		else if (caName.equals(IMPORT_TYPES)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(IMPORT_TYPES));

			if (ca == null) {
				ca = new ImportTypesDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(IMPORT_TYPES, ca);
			}
		}
		else if (caName.equals(EDIT_CV_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EDIT_CV_DLG));

			if (ca == null) {
				ca = new EditCVDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EDIT_CV_DLG, ca);
			}
		}
		else if (caName.equals(NEW_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEW_ANNOTATION));

			if (ca == null) {
				ca = new NewAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEW_ANNOTATION, ca);
			}
		}
		else if (caName.equals(CREATE_DEPEND_ANN)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CREATE_DEPEND_ANN));

			if (ca == null) {
				ca = new CreateDependentAnnotationsCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CREATE_DEPEND_ANN, ca);
			}
		}
		else if (caName.equals(NEW_ANNOTATION_REC)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEW_ANNOTATION_REC));

			if (ca == null) {
				ca = new NewAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEW_ANNOTATION_REC, ca);
			}
		}
		else if (caName.equals(NEW_ANNOTATION_ALT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEW_ANNOTATION_ALT));

			if (ca == null) {
				ca = new NewAnnotationAltCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEW_ANNOTATION_ALT, ca);
			}
		}
		else if (caName.equals(NEW_ANNOTATION_BEFORE)) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(NEW_ANNOTATION_BEFORE));

			if (ca == null) {
				ca = new AnnotationBeforeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEW_ANNOTATION_BEFORE, ca);
			}
		}
		else if (caName.equals(NEW_ANNOTATION_AFTER)) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEW_ANNOTATION_AFTER));

			if (ca == null) {
				ca = new AnnotationAfterCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEW_ANNOTATION_AFTER, ca);
			}
		}
		else if (caName.equals(MODIFY_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MODIFY_ANNOTATION));

			if (ca == null) {
				ca = new ModifyAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MODIFY_ANNOTATION, ca);
			}
		}
		else if (caName.equals(MODIFY_ANNOTATION_ALT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MODIFY_ANNOTATION_ALT));

			if (ca == null) {
				ca = new ModifyAnnotationAltCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MODIFY_ANNOTATION_ALT, ca);
			}
		}
		else if (caName.equals(SPLIT_ANNOTATION)) {
            ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SPLIT_ANNOTATION));

            if (ca == null) {
                ca = new SplitAnnotationCA(viewerManager);
                ((Hashtable) commandActionHash.get(tr)).put(SPLIT_ANNOTATION,
                    ca);
            }
        }
		else if (caName.equals(REMOVE_ANNOTATION_VALUE)) {
            ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(REMOVE_ANNOTATION_VALUE));

            if (ca == null) {
                ca = new RemoveAnnotationValueCA(viewerManager);
                ((Hashtable) commandActionHash.get(tr)).put(REMOVE_ANNOTATION_VALUE,
                    ca);
            }
        }
		else if (caName.equals(DELETE_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_ANNOTATION));

			if (ca == null) {
				ca = new DeleteAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_ANNOTATION, ca);
			}
		}
		else if (caName.equals(DELETE_ANNOTATION_ALT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_ANNOTATION_ALT));

			if (ca == null) {
				ca = new DeleteAnnotationAltCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_ANNOTATION_ALT, ca);
			}
		}
		else if (caName.equals(DELETE_ANNOS_IN_SELECTION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_ANNOS_IN_SELECTION));

			if (ca == null) {
				ca = new DeleteAnnotationsInSelectionCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_ANNOS_IN_SELECTION, ca);
			}
		}
		else if (caName.equals(DELETE_ANNOS_LEFT_OF)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_ANNOS_LEFT_OF));

			if (ca == null) {
				ca = new DeleteAnnotationsLeftOfCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_ANNOS_LEFT_OF, ca);
			}
		}
		else if (caName.equals(DELETE_ANNOS_RIGHT_OF)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_ANNOS_RIGHT_OF));

			if (ca == null) {
				ca = new DeleteAnnotationsRightOfCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_ANNOS_RIGHT_OF, ca);
			}
		}
		else if (caName.equals(DELETE_ALL_ANNOS_LEFT_OF)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_ALL_ANNOS_LEFT_OF));

			if (ca == null) {
				ca = new DeleteAllAnnotationsLeftOfCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_ALL_ANNOS_LEFT_OF, ca);
			}
		}
		else if (caName.equals(DELETE_ALL_ANNOS_RIGHT_OF)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DELETE_ALL_ANNOS_RIGHT_OF));

			if (ca == null) {
				ca = new DeleteAllAnnotationsRightOfCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DELETE_ALL_ANNOS_RIGHT_OF, ca);
			}
		}
		else if (caName.equals(DUPLICATE_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DUPLICATE_ANNOTATION));

			if (ca == null) {
				ca = new DuplicateAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DUPLICATE_ANNOTATION, ca);
			}
		}
		else if (caName.equals(MERGE_ANNOTATION_WN)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MERGE_ANNOTATION_WN));

			if (ca == null) {
				ca = new MergeAnnotationWithNextCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MERGE_ANNOTATION_WN, ca);
			}
		}
		else if (caName.equals(MERGE_ANNOTATION_WB)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MERGE_ANNOTATION_WB));

			if (ca == null) {
				ca = new MergeAnnotationWithBeforeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MERGE_ANNOTATION_WB, ca);
			}
		}
		else if (caName.equals(COPY_TO_NEXT_ANNOTATION)) {
	        ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(COPY_TO_NEXT_ANNOTATION));

	        if (ca == null) {
	            ca = new CopyToNextAnnotationCA(viewerManager);
	            ((Hashtable) commandActionHash.get(tr)).put(COPY_TO_NEXT_ANNOTATION, ca);
	        }
	    }
		else if (caName.equals(COPY_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(COPY_ANNOTATION));

			if (ca == null) {
				ca = new CopyAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(COPY_ANNOTATION, ca);
			}
		}
		else if (caName.equals(COPY_ANNOTATION_TREE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(COPY_ANNOTATION_TREE));

			if (ca == null) {
				ca = new CopyAnnotationTreeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(COPY_ANNOTATION_TREE, ca);
			}
		}
		else if (caName.equals(PASTE_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PASTE_ANNOTATION));

			if (ca == null) {
				ca = new PasteAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PASTE_ANNOTATION, ca);
			}
		}
		else if (caName.equals(PASTE_ANNOTATION_HERE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PASTE_ANNOTATION_HERE));

			if (ca == null) {
				ca = new PasteAnnotationHereCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PASTE_ANNOTATION_HERE, ca);
			}
		}
		else if (caName.equals(PASTE_ANNOTATION_TREE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PASTE_ANNOTATION_TREE));

			if (ca == null) {
				ca = new PasteAnnotationTreeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PASTE_ANNOTATION_TREE, ca);
			}
		}
		else if (caName.equals(PASTE_ANNOTATION_TREE_HERE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PASTE_ANNOTATION_TREE_HERE));

			if (ca == null) {
				ca = new PasteAnnotationTreeHereCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PASTE_ANNOTATION_TREE_HERE, ca);
			}
		}
		else if (caName.equals(MODIFY_ANNOTATION_TIME)) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(MODIFY_ANNOTATION_TIME));

			if (ca == null) {
				ca = new ModifyAnnotationTimeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MODIFY_ANNOTATION_TIME, ca);
			}
		}
		else if (caName.equals(MODIFY_ANNOTATION_DC_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(MODIFY_ANNOTATION_DC_DLG));

			if (ca == null) {
				ca = new ModifyAnnotationDatCatCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MODIFY_ANNOTATION_DC_DLG, ca);
			}
		}
		else if (caName == MODIFY_GRAPHIC_ANNOTATION) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(MODIFY_GRAPHIC_ANNOTATION));

			if (ca == null) {
				ca = new ModifyGraphicAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MODIFY_GRAPHIC_ANNOTATION, ca);
			}
		}
		else if (caName == SHIFT_ALL_ANNOTATIONS) {
			ca = 
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
								.get(SHIFT_ALL_ANNOTATIONS));
			if (ca == null) {
				ca = new ShiftAllAnnotationsDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SHIFT_ALL_ANNOTATIONS, ca);
			}
		}
		else if (caName == SHIFT_ACTIVE_ANNOTATION) {
			ca = 
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
								.get(SHIFT_ACTIVE_ANNOTATION));
			if (ca == null) {
				ca = new ShiftActiveAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SHIFT_ACTIVE_ANNOTATION, ca);
			}
		}
		else if (caName == SHIFT_ANNOS_IN_SELECTION) {
			ca = 
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
								.get(SHIFT_ANNOS_IN_SELECTION));
			if (ca == null) {
				ca = new ShiftAnnotationsInSelectionCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SHIFT_ANNOS_IN_SELECTION, ca);
			}
		}
		else if (caName == SHIFT_ANNOS_LEFT_OF) {
			ca = 
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
								.get(SHIFT_ANNOS_LEFT_OF));
			if (ca == null) {
				ca = new ShiftAnnotationsLeftOfCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SHIFT_ANNOS_LEFT_OF, ca);
			}
		}
		else if (caName == SHIFT_ANNOS_RIGHT_OF) {
			ca = 
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
								.get(SHIFT_ANNOS_RIGHT_OF));
			if (ca == null) {
				ca = new ShiftAnnotationsRightOfCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SHIFT_ANNOS_RIGHT_OF, ca);
			}
		}
		else if (caName == SHIFT_ALL_ANNOS_LEFT_OF) {
			ca = 
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
								.get(SHIFT_ALL_ANNOS_LEFT_OF));
			if (ca == null) {
				ca = new ShiftAllAnnotationsLeftOfCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SHIFT_ALL_ANNOS_LEFT_OF, ca);
			}
		}
		else if (caName == SHIFT_ALL_ANNOS_RIGHT_OF) {
			ca = 
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
								.get(SHIFT_ALL_ANNOS_RIGHT_OF));
			if (ca == null) {
				ca = new ShiftAllAnnotationsRightOfCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SHIFT_ALL_ANNOS_RIGHT_OF, ca);
			}
		}
		else if (caName == TOKENIZE_DLG) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(TOKENIZE_DLG));

			if (ca == null) {
				ca = new TokenizeDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(TOKENIZE_DLG, ca);
			}
		}
		else if (caName == REGULAR_ANNOTATION_DLG) {
	        ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(REGULAR_ANNOTATION_DLG));

	        if (ca == null) {
	            ca = new RegularAnnotationDlgCA(viewerManager);
	            ((Hashtable) commandActionHash.get(tr)).put(REGULAR_ANNOTATION_DLG, ca);
	        }
	    }
		else if (caName == REMOVE_ANNOTATIONS_OR_VALUES) {
	        ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(REMOVE_ANNOTATIONS_OR_VALUES));

	        if (ca == null) {
	            ca = new RemoveAnnotationsOrValuesCA(viewerManager);
	            ((Hashtable) commandActionHash.get(tr)).put(REMOVE_ANNOTATIONS_OR_VALUES, ca);
	        }
	    }
		else if (caName == ANN_FROM_OVERLAP) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(ANN_FROM_OVERLAP));

			if (ca == null) {
				ca = new AnnotationsFromOverlapsDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ANN_FROM_OVERLAP, ca);
			}
		}
		else if (caName == ANN_FROM_SUBTRACTION) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(ANN_FROM_SUBTRACTION));

			if (ca == null) {
				ca = new AnnotationsFromSubtractionDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ANN_FROM_SUBTRACTION, ca);
			}
		}
		//temp
		else if (caName == ANN_FROM_OVERLAP_CLAS) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(ANN_FROM_OVERLAP_CLAS));

			if (ca == null) {
				ca = new AnnotationsFromOverlapsClasDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ANN_FROM_OVERLAP_CLAS, ca);
			}
		}
		else if (caName == MERGE_TIERS) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(MERGE_TIERS));

			if (ca == null) {
				ca = new MergeTiersDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MERGE_TIERS, ca);
			}
		}
		else if (caName == MERGE_TIER_GROUP) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(MERGE_TIER_GROUP));

			if (ca == null) {
				ca = new MergeTierGroupDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MERGE_TIER_GROUP, ca);
			}
		}
		else if (caName == ANN_ON_DEPENDENT_TIER) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(ANN_ON_DEPENDENT_TIER));

			if (ca == null) {
				ca = new CreateAnnsOnDependentTiersDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ANN_ON_DEPENDENT_TIER, ca);
			}
		}
		else if (caName == ANN_FROM_GAPS) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(ANN_FROM_GAPS));

			if (ca == null) {
				ca = new AnnotationsFromGapsDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ANN_FROM_GAPS, ca);
			}
		}
		else if (caName == COMPARE_ANNOTATORS_DLG) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(COMPARE_ANNOTATORS_DLG));

			if (ca == null) {
				ca = new CompareAnnotatorsDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(COMPARE_ANNOTATORS_DLG, ca);
			}
		}
		else if (caName.equals(SHOW_TIMELINE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SHOW_TIMELINE));

			if (ca == null) {
				ca = new ShowTimelineCA(viewerManager, layoutManager);
				((Hashtable) commandActionHash.get(tr)).put(SHOW_TIMELINE, ca);
			}
		}
		else if (caName.equals(SHOW_INTERLINEAR)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SHOW_INTERLINEAR));

			if (ca == null) {
				ca = new ShowInterlinearCA(viewerManager, layoutManager);
				((Hashtable) commandActionHash.get(tr)).put(SHOW_INTERLINEAR, ca);
			}
		}	
		else if (caName.equals(SEARCH_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SEARCH_DLG));

			if (ca == null) {
				ca = new SearchDialogCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SEARCH_DLG, ca);
			}
		}		    
		else if (caName.equals(GOTO_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(GOTO_DLG));

			if (ca == null) {
				ca = new GoToDialogCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(GOTO_DLG, ca);
			}
		}
		else if (caName.equals(TIER_DEPENDENCIES)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(TIER_DEPENDENCIES));

			if (ca == null) {
				ca = new TierDependenciesCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(TIER_DEPENDENCIES, ca);
			}
		}
		else if (caName.equals(SPREADSHEET)) {
            ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SPREADSHEET));

            if (ca == null) {
                ca = new SpreadSheetCA(viewerManager);
                ((Hashtable) commandActionHash.get(tr)).put(SPREADSHEET, ca);
            }
        }
		else if (caName.equals(STATISTICS)) {
            ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(STATISTICS));

            if (ca == null) {
                ca = new StatisticsCA(viewerManager);
                ((Hashtable) commandActionHash.get(tr)).put(STATISTICS, ca);
            }
        }
		else if (caName.equals(SYNC_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SYNC_MODE));

			if (ca == null) {
				//ca = new SyncModeCA(viewerManager, layoutManager);
				ca = new ChangeModeCA(viewerManager, layoutManager, SYNC_MODE);
				((Hashtable) commandActionHash.get(tr)).put(SYNC_MODE, ca);
			}
		}
		else if (caName.equals(ANNOTATION_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(ANNOTATION_MODE));

			if (ca == null) {
				//ca = new AnnotationModeCA(viewerManager, layoutManager);
				ca = new ChangeModeCA(viewerManager, layoutManager, ANNOTATION_MODE);
				((Hashtable) commandActionHash.get(tr)).put(ANNOTATION_MODE, ca);
			}
		}
		else if (caName.equals(TRANSCRIPTION_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(TRANSCRIPTION_MODE));

			if (ca == null) {
				//ca = new TranscriptionModeCA(viewerManager, layoutManager);
				ca = new ChangeModeCA(viewerManager, layoutManager, TRANSCRIPTION_MODE);
				((Hashtable) commandActionHash.get(tr)).put(TRANSCRIPTION_MODE, ca);
			}
		}
		else if (caName.equals(SEGMENTATION_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SEGMENTATION_MODE));

			if (ca == null) {
				//ca = new SegmentationModeCA(viewerManager, layoutManager);
				ca = new ChangeModeCA(viewerManager, layoutManager, SEGMENTATION_MODE);
				((Hashtable) commandActionHash.get(tr)).put(SEGMENTATION_MODE, ca);
			}
		}
		else if (caName.equals(INTERLINEARIZATION_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(INTERLINEARIZATION_MODE));

			if (ca == null) {
				ca = new ChangeModeCA(viewerManager, layoutManager, INTERLINEARIZATION_MODE);
				((Hashtable) commandActionHash.get(tr)).put(INTERLINEARIZATION_MODE, ca);
			}
		}
		
		else if (caName.equals(SELECTION_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SELECTION_MODE));

			if (ca == null) {
				ca = new SelectionModeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SELECTION_MODE, ca);
			}
		}
		else if (caName.equals(LOOP_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(LOOP_MODE));

			if (ca == null) {
				ca = new LoopModeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(LOOP_MODE, ca);
			}
		}
		else if (caName.equals(BULLDOZER_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(BULLDOZER_MODE));

			if (ca == null) {
				ca = new BulldozerModeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(BULLDOZER_MODE, ca);
			}
		}
		else if (caName.equals(TIMEPROP_NORMAL)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(TIMEPROP_NORMAL));

			if (ca == null) {
				ca = new NormalTimePropCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(TIMEPROP_NORMAL, ca);
			}
		}
		else if (caName.equals(SHIFT_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SHIFT_MODE));

			if (ca == null) {
				ca = new ShiftModeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SHIFT_MODE, ca);
			}
		}
		else if (caName.equals(SET_PAL)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SET_PAL));

			if (ca == null) {
				ca = new SetPALCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SET_PAL, ca);
			}
		}
		else if (caName.equals(SET_NTSC)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SET_NTSC));

			if (ca == null) {
				ca = new SetNTSCCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SET_NTSC, ca);
			}
		}
		else if (caName.equals(CLEAR_SELECTION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CLEAR_SELECTION));

			if (ca == null) {
				ca = new ClearSelectionCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CLEAR_SELECTION, ca);
			}
		}
		else if (caName.equals(CLEAR_SELECTION_ALT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CLEAR_SELECTION_ALT));

			if (ca == null) {
				ca = new ClearSelectionAltCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CLEAR_SELECTION_ALT, ca);
			}
		}
		else if (caName.equals(CLEAR_SELECTION_AND_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CLEAR_SELECTION_AND_MODE));

			if (ca == null) {
				ca = new ClearSelectionAndModeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CLEAR_SELECTION_AND_MODE, ca);
			}
		}
		else if (caName.equals(PLAY_SELECTION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PLAY_SELECTION));

			if (ca == null) {
				ca = new PlaySelectionCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PLAY_SELECTION, ca);
			}
		}
		else if (caName.equals(PLAY_AROUND_SELECTION)) {
			ca =
				(CommandAction) (((Hashtable) commandActionHash.get(tr))
					.get(PLAY_AROUND_SELECTION));

			if (ca == null) {
				ca = new PlayAroundSelectionCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PLAY_AROUND_SELECTION, ca);
			}
		}
		else if (caName.equals(NEXT_FRAME)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEXT_FRAME));

			if (ca == null) {
				ca = new NextFrameCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEXT_FRAME, ca);
			}
		}
		else if (caName.equals(PREVIOUS_FRAME)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PREVIOUS_FRAME));

			if (ca == null) {
				ca = new PreviousFrameCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PREVIOUS_FRAME, ca);
			}
		}
		else if (caName.equals(PLAY_PAUSE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PLAY_PAUSE));

			if (ca == null) {
				ca = new PlayPauseCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PLAY_PAUSE, ca);
			}
		}
		else if (caName.equals(GO_TO_BEGIN)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(GO_TO_BEGIN));

			if (ca == null) {
				ca = new GoToBeginCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(GO_TO_BEGIN, ca);
			}
		}
		else if (caName.equals(GO_TO_END)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(GO_TO_END));

			if (ca == null) {
				ca = new GoToEndCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(GO_TO_END, ca);
			}
		}
		else if (caName.equals(PREVIOUS_SCROLLVIEW)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PREVIOUS_SCROLLVIEW));

			if (ca == null) {
				ca = new PreviousScrollViewCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PREVIOUS_SCROLLVIEW, ca);
			}
		}
		else if (caName.equals(NEXT_SCROLLVIEW)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEXT_SCROLLVIEW));

			if (ca == null) {
				ca = new NextScrollViewCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEXT_SCROLLVIEW, ca);
			}
		}
		else if (caName.equals(PIXEL_LEFT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PIXEL_LEFT));

			if (ca == null) {
				ca = new PixelLeftCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PIXEL_LEFT, ca);
			}
		}
		else if (caName.equals(PIXEL_RIGHT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PIXEL_RIGHT));

			if (ca == null) {
				ca = new PixelRightCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PIXEL_RIGHT, ca);
			}
		}
		else if (caName.equals(SECOND_LEFT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SECOND_LEFT));

			if (ca == null) {
				ca = new SecondLeftCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SECOND_LEFT, ca);
			}
		}
		else if (caName.equals(SECOND_RIGHT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SECOND_RIGHT));

			if (ca == null) {
				ca = new SecondRightCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SECOND_RIGHT, ca);
			}
		}
		else if (caName.equals(SELECTION_BOUNDARY)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SELECTION_BOUNDARY));

			if (ca == null) {
				ca = new ActiveSelectionBoundaryCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SELECTION_BOUNDARY, ca);
			}
		}
		else if (caName.equals(SELECTION_CENTER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SELECTION_CENTER));

			if (ca == null) {
				ca = new ActiveSelectionCenterCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SELECTION_CENTER, ca);
			}
		}
		else if (caName.equals(SELECTION_BOUNDARY_ALT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SELECTION_BOUNDARY_ALT));

			if (ca == null) {
				ca = new ActiveSelectionBoundaryAltCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SELECTION_BOUNDARY_ALT, ca);
			}
		}
		else if (caName.equals(PREVIOUS_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PREVIOUS_ANNOTATION));

			if (ca == null) {
				ca = new PreviousAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PREVIOUS_ANNOTATION, ca);
			}
		}
		else if (caName.equals(PREVIOUS_ANNOTATION_EDIT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PREVIOUS_ANNOTATION_EDIT));

			if (ca == null) {
				ca = new PreviousAnnotationEditCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PREVIOUS_ANNOTATION_EDIT, ca);
			}
		}
		else if (caName.equals(NEXT_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEXT_ANNOTATION));

			if (ca == null) {
				ca = new NextAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEXT_ANNOTATION, ca);
			}
		}
		else if (caName.equals(NEXT_ANNOTATION_EDIT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEXT_ANNOTATION_EDIT));

			if (ca == null) {
				ca = new NextAnnotationEditCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEXT_ANNOTATION_EDIT, ca);
			}
		}
		
		else if (caName.equals(ACTIVE_ANNOTATION_EDIT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(ACTIVE_ANNOTATION_EDIT));

			if (ca == null) {
				ca = new ActiveAnnotationEditCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ACTIVE_ANNOTATION_EDIT, ca);
			}
		}
		else if (caName.equals(ANNOTATION_UP)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(ANNOTATION_UP));

			if (ca == null) {
				ca = new AnnotationUpCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ANNOTATION_UP, ca);
			}
		}
		else if (caName.equals(ANNOTATION_DOWN)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(ANNOTATION_DOWN));

			if (ca == null) {
				ca = new AnnotationDownCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(ANNOTATION_DOWN, ca);
			}
		}
		else if (caName.equals(SAVE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SAVE));

			if (ca == null) {
				ca = new SaveCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SAVE, ca);
			}
		}
		else if (caName.equals(SAVE_AS)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SAVE_AS));

			if (ca == null) {
				ca = new SaveAsCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SAVE_AS, ca);
			}
		}
		else if (caName.equals(SAVE_AS_TEMPLATE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SAVE_AS_TEMPLATE));

			if (ca == null) {
				ca = new SaveAsTemplateCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SAVE_AS_TEMPLATE, ca);
			}
		}
		else if (caName.equals(SAVE_SELECTION_AS_EAF)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SAVE_SELECTION_AS_EAF));

			if (ca == null) {
				ca = new SaveSelectionAsEafCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SAVE_SELECTION_AS_EAF, ca);
			}
		}
		else if (caName.equals(BACKUP)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(BACKUP));

			if (ca == null) {
				ca = new BackupCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(BACKUP, ca);
			}
		}
		else if (caName.equals(BACKUP_NEVER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(BACKUP_NEVER));

			if (ca == null) {
				ca = new BackupNeverCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(BACKUP_NEVER, ca);
			}
		}
		else if (caName.equals(BACKUP_1)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(BACKUP_1));

			if (ca == null) {
				ca = new Backup1CA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(BACKUP_1, ca);
			}
		}
		else if (caName.equals(BACKUP_5)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(BACKUP_5));

			if (ca == null) {
				ca = new Backup5CA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(BACKUP_5, ca);
			}
		}
		else if (caName.equals(BACKUP_10)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(BACKUP_10));

			if (ca == null) {
				ca = new Backup10CA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(BACKUP_10, ca);
			}
		}
		else if (caName.equals(BACKUP_20)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(BACKUP_20));

			if (ca == null) {
				ca = new Backup20CA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(BACKUP_20, ca);
			}
		}
		else if (caName.equals(BACKUP_30)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(BACKUP_30));

			if (ca == null) {
				ca = new Backup30CA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(BACKUP_30, ca);
			}
		}
		else if (caName.equals(PRINT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PRINT));

			if (ca == null) {
				ca = new PrintCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PRINT, ca);
			}
		}
		else if (caName.equals(PREVIEW)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PREVIEW));

			if (ca == null) {
				ca = new PrintPreviewCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PREVIEW, ca);
			}
		}
		else if (caName.equals(PAGESETUP)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PAGESETUP));

			if (ca == null) {
				ca = new PageSetupCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PAGESETUP, ca);
			}
		}
		else if (caName.equals(EXPORT_TAB)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_TAB));

			if (ca == null) {
				ca = new ExportTabDelDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_TAB, ca);
			}
		}
		else if (caName.equals(EXPORT_TEX)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_TEX));

			if (ca == null) {
				ca = new ExportTeXDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_TEX, ca);
			}
		}
		else if (caName.equals(EXPORT_TIGER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_TIGER));

			if (ca == null) {
				ca = new ExportTigerDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_TIGER, ca);
			}
		}
		else if (caName.equals(EXPORT_QT_SUB)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_QT_SUB));

			if (ca == null) {
				ca = new ExportQtSubCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_QT_SUB, ca);
			}
		}
		else if (caName.equals(EXPORT_SMIL_RT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_SMIL_RT));

			if (ca == null) {
				ca = new ExportSmilCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_SMIL_RT, ca);
			}
		}
		else if(caName.equals(EXPORT_SMIL_QT)){
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_SMIL_QT));

			if (ca == null) {
				ca = new ExportSmilQTCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_SMIL_QT, ca);
			}
		}
		else if (caName.equals(EXPORT_SHOEBOX)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_SHOEBOX));

			if (ca == null) {
				ca = new ExportShoeboxCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_SHOEBOX, ca);
			}
		}
		else if (caName.equals(EXPORT_CHAT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_CHAT));

			if (ca == null) {
				ca = new ExportCHATCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_CHAT, ca);
			}
		}
		else if (caName.equals(EXPORT_MEDIA)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_MEDIA));

			if (ca == null) {
				ca = new ExportMediaCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_MEDIA, ca);
			}
		}
		else if (caName.equals(EXPORT_IMAGE_FROM_WINDOW)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_IMAGE_FROM_WINDOW ));

			if (ca == null) {
				ca = new ExportImageFromWindowCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_IMAGE_FROM_WINDOW, ca);
			}
		}
		else if (caName.equals(PUBLISH_DOC)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PUBLISH_DOC));

			if (ca == null) {
				ca = new PublishDocumentCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PUBLISH_DOC, ca);
			}
		}
		else if (caName.equals(DISCOVER_DOC)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(DISCOVER_DOC));

			if (ca == null) {
				ca = new DiscoverDocumentCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(DISCOVER_DOC, ca);
			}
		}
		else if (caName.equals(LINKED_FILES_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(LINKED_FILES_DLG));

			if (ca == null) {
				ca = new LinkedFilesDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(LINKED_FILES_DLG, ca);
			}
		}
		else if (caName.equals(PLAYBACK_RATE_TOGGLE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PLAYBACK_RATE_TOGGLE));
		
			if (ca == null) {
				ca = new PlaybackRateToggleCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PLAYBACK_RATE_TOGGLE, ca);
			}
		}
		else if (caName.equals(PLAYBACK_VOLUME_TOGGLE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PLAYBACK_VOLUME_TOGGLE));
		
			if (ca == null) {
				ca = new PlaybackVolumeToggleCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PLAYBACK_VOLUME_TOGGLE, ca);
			}
		}		
		else if (caName.equals(FILTER_TIER_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(FILTER_TIER_DLG));
		
			if (ca == null) {
				ca = new FilterTierDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(FILTER_TIER_DLG, ca);
			}
		}
		else if (caName.equals(EXPORT_TRAD_TRANSCRIPT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_TRAD_TRANSCRIPT));
		
			if (ca == null) {
				ca = new ExportTradTranscriptDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_TRAD_TRANSCRIPT, ca);
			}
		}
		else if (caName.equals(EXPORT_INTERLINEAR)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_INTERLINEAR));
		
			if (ca == null) {
				ca = new ExportInterlinearDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_INTERLINEAR, ca);
			}
		}
		else if (caName.equals(EXPORT_HTML)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_HTML));
		
			if (ca == null) {
				ca = new ExportHTMLDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_HTML, ca);
			}
		}
		else if (caName.equals(REPARENT_TIER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(REPARENT_TIER));
		
			if (ca == null) {
				ca = new ReparentTierDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(REPARENT_TIER, ca);
			}
		}
		else if (caName.equals(COPY_CURRENT_TIME)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(COPY_CURRENT_TIME));
		
			if (ca == null) {
				ca = new CopyCurrentTimeToPasteBoardCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(COPY_CURRENT_TIME, ca);
			}
		}
		else if (caName.equals(COPY_TIER_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(COPY_TIER_DLG));
		
			if (ca == null) {
				ca = new CopyTierDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(COPY_TIER_DLG, ca);
			}
		}
		else if (caName.equals(NEXT_ACTIVE_TIER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(NEXT_ACTIVE_TIER));
		
			if (ca == null) {
				ca = new NextActiveTierCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(NEXT_ACTIVE_TIER, ca);
			}
		}
		else if (caName.equals(PREVIOUS_ACTIVE_TIER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PREVIOUS_ACTIVE_TIER));
		
			if (ca == null) {
				ca = new PreviousActiveTierCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PREVIOUS_ACTIVE_TIER, ca);
			}
		}
		else if (caName.equals(MERGE_TRANSCRIPTIONS)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MERGE_TRANSCRIPTIONS));
		
			if (ca == null) {
				ca = new MergeTranscriptionDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MERGE_TRANSCRIPTIONS, ca);
			}
		}	
		else if (caName.equals(SYNTAX_VIEWER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SYNTAX_VIEWER));
		
			if (ca == null) {				
					if(SyntaxViewerCommand.isEnabled()){
						ca = new SyntaxViewerCA(viewerManager);
						((Hashtable) commandActionHash.get(tr)).put(SYNTAX_VIEWER, ca);
					}				
			}
		}
		else if (caName.equals(CLOSE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CLOSE));
		
			if (ca == null) {				
				ca = new CloseCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CLOSE, ca);				
			}
		}
		else if (caName.equals(KIOSK_MODE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(KIOSK_MODE));
		
			if (ca == null) {				
				ca = new KioskModeCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(KIOSK_MODE, ca);				
			}
		}
		else if (caName.equals(IMPORT_PRAAT_GRID)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(IMPORT_PRAAT_GRID));
		
			if (ca == null) {				
				ca = new ImportPraatGridCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(IMPORT_PRAAT_GRID, ca);				
			}
		}
		else if (caName.equals(EXPORT_PRAAT_GRID)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_PRAAT_GRID));
		
			if (ca == null) {				
				ca = new ExportPraatGridCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_PRAAT_GRID, ca);				
			}
		}
		else if (caName.equals(LABEL_AND_NUMBER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(LABEL_AND_NUMBER));
		
			if (ca == null) {				
				ca = new LabelAndNumberCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(LABEL_AND_NUMBER, ca);				
			}
		}
		else if (caName.equals(KEY_CREATE_ANNOTATION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(KEY_CREATE_ANNOTATION));
		
			if (ca == null) {				
				ca = new KeyCreateAnnotationCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(KEY_CREATE_ANNOTATION, ca);				
			}
		}
		else if (caName.equals(EXPORT_WORDS)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_WORDS));
		
			if (ca == null) {				
				ca = new ExportWordsDialogCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_WORDS, ca);				
			}
		}
		else if (caName.equals(IMPORT_PREFS)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(IMPORT_PREFS));
		
			if (ca == null) {				
				ca = new ImportPrefsCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(IMPORT_PREFS, ca);				
			}
		}
		else if (caName.equals(EXPORT_PREFS)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_PREFS));
		
			if (ca == null) {				
				ca = new ExportPrefsCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_PREFS, ca);				
			}
		}
		else if (caName.equals(EXPORT_TOOLBOX)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_TOOLBOX));
		
			if (ca == null) {				
				ca = new ExportToolboxDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_TOOLBOX, ca);				
			}
		}
		else if (caName.equals(EXPORT_SUBTITLES)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_SUBTITLES));
		
			if (ca == null) {				
				ca = new ExportSubtitlesCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_SUBTITLES, ca);				
			}
		}
		else if (caName.equals(EXPORT_FILMSTRIP)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_FILMSTRIP));
		
			if (ca == null) {				
				ca = new ExportFilmStripCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_FILMSTRIP, ca);				
			}
		}
		else if (caName.equals(CENTER_SELECTION)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CENTER_SELECTION));
		
			if (ca == null) {				
				ca = new CenterSelectionCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CENTER_SELECTION, ca);				
			}
		}
		else if (caName.equals(SET_AUTHOR)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(SET_AUTHOR));
		
			if (ca == null) {				
				ca = new SetAuthorCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(SET_AUTHOR, ca);				
			}
		}
		else if (caName.equals(CHANGE_CASE)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CHANGE_CASE));
		
			if (ca == null) {				
				ca = new ChangeCaseDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CHANGE_CASE, ca);				
			}
		}
		else if (caName.equals(CLIP_MEDIA)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(CLIP_MEDIA));
		
			if (ca == null) {				
				ca = new ClipMediaCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(CLIP_MEDIA, ca);				
			}
		}
		else if (caName.equals(EXPORT_RECOG_TIER)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EXPORT_RECOG_TIER));
		
			if (ca == null) {				
				ca = new ExportTiersForRecognizerCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EXPORT_RECOG_TIER, ca);				
			}
		}
		else if (caName.equals(IMPORT_RECOG_TIERS)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(IMPORT_RECOG_TIERS));
		
			if (ca == null) {				
				ca = new ImportRecogTiersCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(IMPORT_RECOG_TIERS, ca);				
			}
		}
		// For opening a Edit Lexicon Service Dialog:
		else if (caName.equals(EDIT_LEX_SRVC_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(EDIT_LEX_SRVC_DLG));

			if(ca == null) {
				ca = new EditLexSrvcDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(EDIT_LEX_SRVC_DLG, ca);
			}
		}
		
		else if (caName.equals(MOVE_ANNOTATION_LBOUNDARY_LEFT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MOVE_ANNOTATION_LBOUNDARY_LEFT));

			if(ca == null) {
				ca = new MoveActiveAnnLBoundarytoLeftCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MOVE_ANNOTATION_LBOUNDARY_LEFT, ca);
			}
		}
		
		else if (caName.equals(MOVE_ANNOTATION_LBOUNDARY_RIGHT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MOVE_ANNOTATION_LBOUNDARY_RIGHT));

			if(ca == null) {
				ca = new MoveActiveAnnLBoundarytoRightCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MOVE_ANNOTATION_LBOUNDARY_RIGHT, ca);
			}
		}
		else if (caName.equals(MOVE_ANNOTATION_RBOUNDARY_LEFT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MOVE_ANNOTATION_RBOUNDARY_LEFT));

			if(ca == null) {
				ca = new MoveActiveAnnRBoundarytoLeftCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MOVE_ANNOTATION_RBOUNDARY_LEFT, ca);
			}
		}
		
		else if (caName.equals(MOVE_ANNOTATION_RBOUNDARY_RIGHT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(MOVE_ANNOTATION_RBOUNDARY_RIGHT));

			if(ca == null) {
				ca = new MoveActiveAnnRBoundarytoRightCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(MOVE_ANNOTATION_RBOUNDARY_RIGHT, ca);
			}
		}
		else if (caName.equals(PLAY_STEP_AND_REPEAT)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(PLAY_STEP_AND_REPEAT));

			if(ca == null) {
				ca = new PlayStepAndRepeatCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(PLAY_STEP_AND_REPEAT, ca);
			}
		}
		else if (caName.equals(WEBSERVICES_DLG)) {
			ca = (CommandAction) (((Hashtable) commandActionHash.get(tr)).get(WEBSERVICES_DLG));

			if(ca == null) {
				ca = new WebServicesDlgCA(viewerManager);
				((Hashtable) commandActionHash.get(tr)).put(WEBSERVICES_DLG, ca);
			}
		}
		return ca;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tr DOCUMENT ME!
	 * @param cName DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static Command createCommand(Transcription tr, String cName) {
		Command c = null;

		if (cName.equals(SET_TIER_NAME)) {
			c = new SetTierNameCommand(cName);
		}

		if (cName.equals(EDIT_TIER)) {
			c = new EditTierDlgCommand(cName);
		}
		else if (cName.equals(CHANGE_TIER)) {
			c = new ChangeTierAttributesCommand(cName);
		}
		else if (cName.equals(ADD_TIER)) {
			c = new AddTierCommand(cName);
		}
		else if (cName.equals(DELETE_TIER)) {
			c = new DeleteTierCommand(cName);
		}
		else if (cName.equals(DELETE_TIERS)) {
	        c = new DeleteTiersCommand(cName);
	    } 
		else if (cName.equals(ADD_PARTICIPANT)) {
			c = new AddParticipantCommand(cName);
		}		
		else if (cName.equals(ADD_PARTICIPANT_DLG)) {
			c = new AddParticipantDlgCommand(cName);
		}
		else if (cName.equals(IMPORT_TIERS)) {
			c = new ImportTiersCommand(cName);
		}
		else if (cName.equals(EDIT_TYPE)) {
			c = new EditLingTypeDlgCommand(cName);
		}
		else if (cName.equals(ADD_TYPE)) {
			c = new AddTypeCommand(cName);
		}
		else if (cName.equals(CHANGE_TYPE)) {
			c = new ChangeTypeCommand(cName);
		}
		else if (cName.equals(DELETE_TYPE)) {
			c = new DeleteTypeCommand(cName);
		}
		else if (cName.equals(IMPORT_TYPES)) {
			c = new ImportLinguisticTypesCommand(cName);
		}
		else if (cName.equals(EDIT_CV_DLG)) {
			c = new EditCVDlgCommand(cName);
		}
		else if (cName.equals(ADD_CV)) {
			c = new AddCVCommand(cName);
		}
		else if (cName.equals(CHANGE_CV)) {
			c = new ChangeCVCommand(cName);
		}
		else if (cName.equals(DELETE_CV)) {
			c = new DeleteCVCommand(cName);
		}
		else if (cName.equals(REPLACE_CV)) {
			c = new ReplaceCVCommand(cName);
		}
		else if (cName.equals(ADD_CV_ENTRY)) {
			c = new AddCVEntryCommand(cName);
		}
		else if (cName.equals(CHANGE_CV_ENTRY)) {
			c = new ChangeCVEntryCommand(cName);
		}
		else if (cName.equals(DELETE_CV_ENTRY)) {
			c = new DeleteCVEntryCommand(cName);
		}
		else if (cName.equals(MOVE_CV_ENTRIES)) {
			c = new MoveCVEntriesCommand(cName);
		}
		else if (cName.equals(REPLACE_CV_ENTRIES)) {
			c = new ReplaceCVEntriesCommand(cName);
		}
		else if (cName.equals(MERGE_CVS)) {
			c = new MergeCVSCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATION)) {
			c = new NewAnnotationCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATION_REC)) {
			c = new NewAnnotationRecursiveCommand(cName);
		}
		else if (cName.equals(CREATE_DEPEND_ANN)) {
			c = new CreateDependentAnnotationsCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATION_BEFORE)) {
			c = new AnnotationBeforeCommand(cName);
		}
		else if (cName.equals(NEW_ANNOTATION_AFTER)) {
			c = new AnnotationAfterCommand(cName);
		}
		else if (cName.equals(DUPLICATE_ANNOTATION)) {
			c = new DuplicateAnnotationCommand(cName);
		}
		else if (cName.equals(MERGE_ANNOTATION_WN)) {
			c = new MergeAnnotationsCommand(cName);
		} 
		else if (cName.equals(MERGE_ANNOTATION_WB)) {
			c = new MergeAnnotationsCommand(cName);
		}
		else if (cName.equals(COPY_TO_NEXT_ANNOTATION)) {
            c = new CopyPreviousAnnotationCommand(cName);   
        }
		else if (cName.equals(COPY_CURRENT_TIME)) {
			c = new CopyCurrentTimeToPasteBoardCommand(cName);
		}
		else if (cName.equals(COPY_ANNOTATION)) {
			c = new CopyAnnotationCommand(cName);
		}
		else if (cName.equals(COPY_ANNOTATION_TREE)) {
			c = new CopyAnnotationTreeCommand(cName);
		}
		else if (cName.equals(PASTE_ANNOTATION)) {
			c = new PasteAnnotationCommand(cName);
		}
		else if (cName.equals(PASTE_ANNOTATION_HERE)) {
			c = new PasteAnnotationCommand(cName);
		}
		else if (cName.equals(PASTE_ANNOTATION_TREE)) {
			c = new PasteAnnotationTreeCommand(cName);
		}
		else if (cName.equals(PASTE_ANNOTATION_TREE_HERE)) {
			c = new PasteAnnotationTreeCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION_DLG)) {
			c = new ModifyAnnotationDlgCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION)) {
			c = new ModifyAnnotationCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION_DC_DLG)) {
			c = new ModifyAnnotationDatCatDlgCommand(cName);
		}
		else if (cName.equals(MODIFY_ANNOTATION_DC)) {
			c = new ModifyAnnotationDatCatCommand(cName);
		}
		else if (cName.equals(SPLIT_ANNOTATION)) {
			c = new SplitAnnotationCommand(cName);
		}
		else if (cName.equals(REMOVE_ANNOTATION_VALUE)) {
	        c = new RemoveAnnotationValueCommand(cName);
	    }
		else if (cName.equals(DELETE_ANNOTATION)) {
			c = new DeleteAnnotationCommand(cName);
		}
		else if (cName.equals(DELETE_ANNOS_IN_SELECTION)) {
			c = new DeleteAnnotationsCommand(cName);
		}
		else if (cName.equals(DELETE_MULTIPLE_ANNOS)) {
			c = new DeleteSelectedAnnotationsCommand(cName);
		}		
		else if (cName.equals(MODIFY_ANNOTATION_TIME)) {
			c = new ModifyAnnotationTimeCommand(cName);
		}
		else if (cName == MODIFY_GRAPHIC_ANNOTATION_DLG) {
			c = new ModifyGraphicAnnotationDlgCommand(cName);
		}
		else if (cName == MODIFY_GRAPHIC_ANNOTATION) {
			c = new ModifyGraphicAnnotationCommand(cName);
		}
		else if (cName == MOVE_ANNOTATION_TO_TIER) {
			c = new MoveAnnotationToTierCommand(cName);
		}
		else if (cName == SHIFT_ALL_ANNOTATIONS) {
			c = new ShiftAllAnnotationsCommand(cName);
		}
		else if (cName == SHIFT_ANNOTATIONS) {
			c = new ShiftAnnotationsCommand(cName);
		}
		else if (cName == SHIFT_ALL_ANNOTATIONS_LROf) {
			c = new ShiftAnnotationsLROfCommand(cName);
		}
		else if (cName == SHIFT_ALL_DLG) {
			c = new ShiftAllAnnotationsDlgCommand(cName);
		}
		else if (cName == SHIFT_ANN_DLG) {
			c = new ShiftAnnotationsDlgCommand(cName);
		}
		else if (cName == SHIFT_ANN_ALLTIER_DLG) {
			c = new ShiftAnnotationsLROfDlgCommand(cName);
		}
		else if (cName == TOKENIZE_DLG) {
			c = new TokenizeDlgCommand(cName);
		}
		else if (cName == REGULAR_ANNOTATION_DLG) {
	        c = new RegularAnnotationDlgCommand(cName);
	    } 
		else if (cName == REMOVE_ANNOTATIONS_OR_VALUES) {
	        c = new RemoveAnnotationsOrValuesCommand(cName);
	    } 
		else if (cName == REGULAR_ANNOTATION) {
	        c = new RegularAnnotationCommand(cName);
	    }
		else if (cName == TOKENIZE_TIER) {
			c = new TokenizeCommand(cName);
		}
		else if (cName == ANN_FROM_OVERLAP ) {
			c = new AnnotationsFromOverlapsUndoableCommand(cName);
		}
		
		else if (cName == ANN_FROM_SUBTRACTION) {
			c = new AnnotationsFromSubtractionUndoableCommand(cName);
		}
		else if (cName == ANN_FROM_OVERLAP_COM ) {
			c = new AnnotationsFromOverlapsDlgCommand(cName);
		}
		else if (cName == ANN_FROM_SUBTRACTION_COM) {
			c = new AnnotationsFromOverlapsDlgCommand(cName, true);
		}
		// temp
		else if (cName == ANN_FROM_OVERLAP_CLAS) {
			c = new AnnotationsFromOverlapsClasCommand(cName);
		}
		else if (cName == ANN_FROM_OVERLAP_COM_CLAS) {
			c = new AnnotationsFromOverlapsClasDlgCommand(cName);
		}
		// temp
		else if (cName == MERGE_TIERS_DLG) {
			c = new MergeTiersDlgCommand(cName);
		}
		else if (cName == MERGE_TIERS) {
			c = new MergeTiersCommand(cName);
		}
		else if (cName == MERGE_TIER_GROUP_DLG) {
			c = new MergeTierGroupDlgCommand(cName);
		}
		else if (cName == MERGE_TIER_GROUP) {
			c = new MergeTierGroupCommand(cName);
		}
		else if (cName == ANN_ON_DEPENDENT_TIER) {
			c = new CreateAnnsOnDependentTiersCommand(cName);
		}
		else if (cName == ANN_ON_DEPENDENT_TIER_COM) {
			c = new CreateAnnsOnDependentTiersDlgCommand(cName);
		}
		else if (cName == ANN_FROM_GAPS) {
			c = new AnnotationsFromGapsCommand(cName);
		}
		else if (cName == ANN_FROM_GAPS_COM) {
			c = new AnnotationsFromGapsDlgCommand(cName);
		}
		else if (cName == COMPARE_ANNOTATORS_DLG) {
			c = new CompareAnnotatorsDlgCommand(cName);
		}
		else if (cName.equals(SHOW_MULTITIER_VIEWER)) {
			c = new ShowMultitierViewerCommand(cName);
		}
		else if (cName.equals(SEARCH_DLG)) {
			c = new SearchDialogCommand(cName);
		}
		else if(cName.equals(REPLACE)){
		    c = new ReplaceCommand(cName);
		}
		else if (cName.equals(GOTO_DLG)) {
			c = new GoToDialogCommand(cName);
		}
		else if (cName.equals(TIER_DEPENDENCIES)) {
			c = new TierDependenciesCommand(cName);
		}
		else if (cName.equals(SPREADSHEET)) {
	         c = new SpreadSheetCommand(cName);
	    } 
		else if (cName.equals(STATISTICS)) {
	         c = new StatisticsCommand(cName);
	    } 
		else if (cName.equals(SYNC_MODE) ||
				cName.equals(ANNOTATION_MODE) ||
				cName.equals(TRANSCRIPTION_MODE)||
				cName.equals(SEGMENTATION_MODE) ||
				cName.equals(INTERLINEARIZATION_MODE)) {
			c = new ChangeModeCommand(cName);
		}
		else if (cName.equals(SELECTION_MODE)) {
			c = new SelectionModeCommand(cName);
		}
		else if (cName.equals(LOOP_MODE)) {
			c = new LoopModeCommand(cName);
		}
		else if (cName.equals(BULLDOZER_MODE)) {
			c = new BulldozerModeCommand(cName);
		}
		else if (cName.equals(TIMEPROP_NORMAL)) {
			c = new NormalTimePropCommand(cName);
		}
		else if (cName.equals(SHIFT_MODE)) {
			c = new ShiftModeCommand(cName);
		}
		else if (cName.equals(SET_PAL)) {
			c = new SetMsPerFrameCommand(cName);
		}
		else if (cName.equals(SET_NTSC)) {
			c = new SetMsPerFrameCommand(cName);
		}
		else if (cName.equals(CLEAR_SELECTION)) {
			c = new ClearSelectionCommand(cName);
		}
		else if (cName.equals(CLEAR_SELECTION_AND_MODE)) {
			c = new ClearSelectionAndModeCommand(cName);
		}
		else if (cName.equals(PLAY_SELECTION)) {
			c = new PlaySelectionCommand(cName);
		}
		else if (cName.equals(NEXT_FRAME)) {
			c = new NextFrameCommand(cName);
		}
		else if (cName.equals(PREVIOUS_FRAME)) {
			c = new PreviousFrameCommand(cName);
		}
		else if (cName.equals(PLAY_PAUSE)) {
			c = new PlayPauseCommand(cName);
		}
		else if (cName.equals(GO_TO_BEGIN)) {
			c = new GoToBeginCommand(cName);
		}
		else if (cName.equals(GO_TO_END)) {
			c = new GoToEndCommand(cName);
		}
		else if (cName.equals(PREVIOUS_SCROLLVIEW)) {
			c = new PreviousScrollViewCommand(cName);
		}
		else if (cName.equals(NEXT_SCROLLVIEW)) {
			c = new NextScrollViewCommand(cName);
		}
		else if (cName.equals(PIXEL_LEFT)) {
			c = new PixelLeftCommand(cName);
		}
		else if (cName.equals(PIXEL_RIGHT)) {
			c = new PixelRightCommand(cName);
		}
		else if (cName.equals(SECOND_LEFT)) {
			c = new SecondLeftCommand(cName);
		}
		else if (cName.equals(SECOND_RIGHT)) {
			c = new SecondRightCommand(cName);
		}
		else if (cName.equals(SELECTION_BOUNDARY)) {
			c = new ActiveSelectionBoundaryCommand(cName);
		} 
		else if (cName.equals(SELECTION_CENTER)) {
			c = new ActiveSelectionCenterCommand(cName);
		}
		else if (cName.equals(ACTIVE_ANNOTATION)) {
			c = new ActiveAnnotationCommand(cName);
		}
		else if (cName.equals(ACTIVE_ANNOTATION_EDIT)) {
			c = new ActiveAnnotationEditCommand(cName);
		}
		else if (cName.equals(STORE)) {
			c = new StoreCommand(cName);
		}
		else if (cName.equals(BACKUP)) {
			c = new SetBackupDelayCommand(cName);
		}
		else if (cName.equals(PRINT)) {
			c = new PrintCommand(cName);
		}
		else if (cName.equals(PREVIEW)) {
			c = new PrintPreviewCommand(cName);
		}
		else if (cName.equals(PAGESETUP)) {
			c = new PageSetupCommand(cName);
		}
		else if (cName.equals(EXPORT_TAB)) {
			c = new ExportTabDelDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_TEX)) {
			c = new ExportTeXDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_TIGER)) {
			c = new ExportTigerDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_SMIL_RT)){
			c = new ExportSmilCommand(cName);
		}
		else if (cName.equals(EXPORT_SMIL_QT)){
			c = new ExportSmilQTCommand(cName);
		}
		else if (cName.equals(EXPORT_QT_SUB)){
			c = new ExportQtSubCommand(cName);
		}		
		else if (cName.equals(EXPORT_MEDIA)){
			c = new ExportMediaCommand(cName);
		}
		else if (cName.equals(EXPORT_IMAGE_FROM_WINDOW)){
			c = new ExportImageFromWindowCommand(cName);
		}			
		else if (cName.equals(EXPORT_SHOEBOX)) {
			c = new ExportShoeboxCommand(cName);
		}
		else if (cName.equals(EXPORT_CHAT)) {
			c = new ExportCHATCommand(cName);
		}
		else if (cName.equals(PUBLISH_DOC)) {
			c = new PublishDocumentCommand(cName);
		}
		else if (cName.equals(DISCOVER_DOC)) {
			c = new DiscoverDocumentCommand(cName);
		}
		else if (cName.equals(LINKED_FILES_DLG)) {
			c = new LinkedFilesDlgCommand(cName);
		}		
		else if (cName.equals(CHANGE_LINKED_FILES)) {
			c = new ChangeLinkedFilesCommand(cName);
		}
		else if (cName.equals(PLAYBACK_RATE_TOGGLE)) {
			c = new PlaybackRateToggleCommand(cName);
		}
		else if (cName.equals(PLAYBACK_VOLUME_TOGGLE)) {
			c = new PlaybackVolumeToggleCommand(cName);
		}
		else if (cName.equals(ADD_SEGMENTATION)) {
			c = new AddSegmentationCommand(cName);
		}
		else if (cName.equals(FILTER_TIER_DLG)) {
			c = new FilterTierDlgCommand(cName);
		}
		else if (cName.equals(FILTER_TIER)) {
			c = new FilterTierCommand(cName);
		}
		else if (cName.equals(EXPORT_TRAD_TRANSCRIPT)) {
			c = new ExportTradTranscriptDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_INTERLINEAR)) {
			c = new ExportInterlinearDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_HTML)) {
			c = new ExportHTMLDlgCommand(cName);
		}
		else if (cName.equals(REPARENT_TIER_DLG)) {
			c = new ReparentTierDlgCommand(cName);
		}
		else if (cName.equals(COPY_TIER_DLG)) {
			c = new CopyTierDlgCommand(cName);
		}
		else if (cName.equals(COPY_TIER) || cName.equals(REPARENT_TIER)) {
			c = new CopyTierCommand(cName);
		}
		else if (cName.equals(SAVE_SELECTION_AS_EAF)) {
			c = new SaveSelectionAsEafCommand(cName);
		}
		else if (cName.equals(ACTIVE_TIER)) {
			c = new ActiveTierCommand(cName);
		}
		else if (cName.equals(MERGE_TRANSCRIPTIONS)) {
			c = new MergeTranscriptionsDlgCommand(cName);
		}
		else if (cName.equals(SYNTAX_VIEWER)){
			c = new SyntaxViewerCommand(cName);
		}
		else if (cName.equals(EXT_TRACK_DATA)) {
			c = new ExtractTrackDataCommand(cName);
		}
		else if (cName.equals(CLOSE)) {
			c = new CloseCommand(cName);
		}
		else if (cName.equals(KIOSK_MODE)) {
			c = new KioskModeCommand(cName);
		}
		else if (cName.equals(IMPORT_PRAAT_GRID)) {
			c = new ImportPraatGridCommand(cName);
		}
		else if (cName.equals(IMPORT_PRAAT_GRID_DLG)) {
			c = new ImportPraatGridDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_PRAAT_GRID)) {
			c = new ExportPraatGridCommand(cName);
		}
		else if (cName.equals(LABEL_N_NUM_DLG)) {
			c = new LabelAndNumberDlgCommand(cName);
		}
		else if (cName.equals(REMOVE_ANNOTATIONS_OR_VALUES_DLG)) {
			c = new RemoveAnnotationsOrValuesDlgCommand(cName);
		}
		else if (cName.equals(LABEL_AND_NUMBER)) {
			c = new LabelAndNumberCommand(cName);
		}
		else if (cName.equals(EXPORT_WORDS)) {
			c = new ExportWordsDialogCommand(cName);
		}
		else if (cName.equals(IMPORT_PREFS)) {
			c = new ImportPrefsCommand(cName);
		}
		else if (cName.equals(EXPORT_PREFS)) {
			c = new ExportPrefsCommand(cName);
		}
		else if (cName.equals(EXPORT_TOOLBOX)) {
			c = new ExportToolboxDlgCommand(cName);
		}
		else if (cName.equals(EXPORT_SUBTITLES)) {
			c = new ExportSubtitlesCommand(cName);
		}
		else if (cName.equals(EXPORT_FILMSTRIP)) {
			c = new ExportFilmStripDlgCommand(cName);
		}
		else if (cName.equals(SEGMENTS_2_TIER_DLG)) {
			c = new SegmentsToTiersDlgCommand(cName);
		}
		else if (cName.equals(SEGMENTS_2_TIER)) {
			c = new SegmentsToTiersCommand(cName);
		}
		else if (cName.equals(CENTER_SELECTION)) {
			c = new CenterSelectionCommand(cName);
		}
		else if (cName.equals(SET_AUTHOR)) {
			c = new SetAuthorCommand(cName);
		}
		else if (cName.equals(CHANGE_CASE)) {
			c = new ChangeCaseCommand(cName);
		}
		else if (cName.equals(CHANGE_CASE_COM)) {
			c = new ChangeCaseDlgCommand(cName);
		}
		else if (cName.equals(CLIP_MEDIA)) {
			c = new ClipMediaCommand(cName);
		}
		else if (cName.equals(EXPORT_RECOG_TIER)) {
			c = new ExportTiersForRecognizerCommand(cName);
		}
		else if (cName.equals(ADD_TRACK_AND_PANEL)) {
			c = new AddTSTrackAndPanelCommand(cName);
		}
		else if (cName.equals(IMPORT_RECOG_TIERS)) {
			c = new ImportRecogTiersCommand(cName);
		}
		// For Lexicon Service editing:
		else if (cName.equals(EDIT_LEX_SRVC_DLG)) {
			c = new EditLexSrvcDlgCommand(cName);
		} 
		else if (cName.equals(ADD_LEX_LINK)) {
			c = new AddLexLinkCommand(cName);
		} 
		else if (cName.equals(CHANGE_LEX_LINK)) {
			c = new ChangeLexLinkCommand(cName);
		} 
		else if (cName.equals(DELETE_LEX_LINK)) {
			c = new DeleteLexLinkCommand(cName);
		}
		else if (cName.equals(PLAY_STEP_AND_REPEAT)) {
			c = new PlayStepAndRepeatCommand(cName);
		}
		else if (cName.equals(WEBSERVICES_DLG)) {
			c = new WebServicesDlgCommand(cName);
		}
			
		if ((c != null) && (c instanceof UndoableCommand)) {
		    ((CommandHistory) commandHistoryHash.get(tr)).addCommand(c);
		}

		return c;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tr DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static UndoCA getUndoCA(Transcription tr) {
		UndoCA undoCA = (UndoCA) undoCAHash.get(tr);

		if (undoCAHash.get(tr) == null) {
			undoCA =
				new UndoCA(
					((ViewerManager2) viewerManagerHash.get(tr)),
					((CommandHistory) commandHistoryHash.get(tr)));
			((CommandHistory) commandHistoryHash.get(tr)).setUndoCA(undoCA);

			undoCAHash.put(tr, undoCA);
		}

		return undoCA;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param tr DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static RedoCA getRedoCA(Transcription tr) {
		RedoCA redoCA = (RedoCA) redoCAHash.get(tr);

		if (redoCA == null) {
			redoCA =
				new RedoCA(
					((ViewerManager2) viewerManagerHash.get(tr)),
					((CommandHistory) commandHistoryHash.get(tr)));
			((CommandHistory) commandHistoryHash.get(tr)).setRedoCA(redoCA);

			redoCAHash.put(tr, redoCA);
		}

		return redoCA;
	}
	
	/**
	 * Returns the Locale for the specified key.
	 * 
	 * @param key a CommandAction language key
	 * @return the associated Locale, defaults to English
	 */
	public static Locale getLocaleForKey(Object key) {
		if (key != null) {
			Locale l = (Locale) languages.get(key);
			if (l != null) {
				return l;
			}
		}
		// default english
		return ElanLocale.ENGLISH;
	}

	/**
	 * Returns a Set view of the registered Locales.
	 * @return a Set view of the registered Locales
	 */
	public static Collection getLocales() {
	    return languages.values();
	}
	
	/**
	 * Refinement of the shortcuts table texts with grouping of related actions and with 
	 * a sub-header per group.
	 * The method now returns a 2 dim. array of Objects instead of Strings
	 * 
	 * @param tr the transcription
	 * @return a 2 dimensional array of Objects
	 */
	public static Object[][] getShortCutText(Transcription tr) {
	    ArrayList shortCuts = new ArrayList();
	    ArrayList descriptions = new ArrayList();
		CommandAction ca;
		KeyStroke acc;
		String accString;
		String descString;
		int index = 0;
		
		// start with subheader for the annotation editing group
		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.AnnotationEdit")));
		descriptions.add(new TableSubHeaderObject(null));
		for (int i = 0; i < 20; i++) {
			ca = getCommandAction(tr, commandConstants[i]);
			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);

			if (acc != null) {
				accString = convertAccKey(acc);
				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);

				if (descString == null) {
					descString = "";
				}

				if (accString != null) {
					shortCuts.add(accString);
					descriptions.add(descString);
				}
			}
			index = i;
		}
		// annotation navigation group
		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.AnnotationNavigation")));
		descriptions.add(new TableSubHeaderObject(null));
		for (int i = ++index, j = 0; j < 6; i++, j++) {
			ca = getCommandAction(tr, commandConstants[i]);
			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);

			if (acc != null) {
				accString = convertAccKey(acc);
				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);

				if (descString == null) {
					descString = "";
				}

				if (accString != null) {
					shortCuts.add(accString);
					descriptions.add(descString);
				}
			}
			index = i;
		}
		// tier and type
		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.TierType")));
		descriptions.add(new TableSubHeaderObject(null));
		for (int i = ++index, j = 0; j < 5; i++, j++) {
			ca = getCommandAction(tr, commandConstants[i]);
			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);

			if (acc != null) {
				accString = convertAccKey(acc);
				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);

				if (descString == null) {
					descString = "";
				}

				if (accString != null) {
					shortCuts.add(accString);
					descriptions.add(descString);
				}
			}
			index = i;
		}
		// selection
		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Selection")));
		descriptions.add(new TableSubHeaderObject(null));
		for (int i = ++index, j = 0; j < 7; i++, j++) {
			ca = getCommandAction(tr, commandConstants[i]);
			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);

			if (acc != null) {
				accString = convertAccKey(acc);
				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);

				if (descString == null) {
					descString = "";
				}

				if (accString != null) {
					shortCuts.add(accString);
					descriptions.add(descString);
				}
			}
			index = i;
		}		
		// media navigation group
		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.MediaNavigation")));
		descriptions.add(new TableSubHeaderObject(null));
		for (int i = ++index, j = 0; j < 15; i++, j++) {
			ca = getCommandAction(tr, commandConstants[i]);
			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);

			if (acc != null) {
				accString = convertAccKey(acc);
				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);

				if (descString == null) {
					descString = "";
				}

				if (accString != null) {
					shortCuts.add(accString);
					descriptions.add(descString);
				}
			}
			index = i;
		}				
		// document group
		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Document")));
		descriptions.add(new TableSubHeaderObject(null));
		
		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_N, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		descString = ElanLocale.getString("Menu.File.NewToolTip");

		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}

		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_O, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		descString = ElanLocale.getString("Menu.File.OpenToolTip");

		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}
			
		for (int i = ++index, j = 0; j < 6; i++, j++) {
			ca = getCommandAction(tr, commandConstants[i]);
			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);

			if (acc != null) {
				accString = convertAccKey(acc);
				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);

				if (descString == null) {
					descString = "";
				}

				if (accString != null) {
					shortCuts.add(accString);
					descriptions.add(descString);
				}
			}
			index = i;
		}
		
		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 
			ActionEvent.SHIFT_MASK));
		descString = ElanLocale.getString("Menu.Window.NextToolTip");

		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}

		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 
			ActionEvent.SHIFT_MASK));
		descString = ElanLocale.getString("Menu.Window.PreviousToolTip");

		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}
			
		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_W, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		descString = ElanLocale.getString("Menu.File.CloseToolTip");

		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}

		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		descString = ElanLocale.getString("Menu.File.ExitToolTip");

		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}
		// miscellaneous
		shortCuts.add(new TableSubHeaderObject(ElanLocale.getString("Frame.ShortcutFrame.Sub.Misc")));
		descriptions.add(new TableSubHeaderObject(null));
		
		ca = getUndoCA(tr);
		accString = convertAccKey((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY));
		descString = ElanLocale.getString("Menu.Edit.Undo");

		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}

		ca = getRedoCA(tr);
		accString = convertAccKey((KeyStroke) ca.getValue(Action.ACCELERATOR_KEY));
		descString = ElanLocale.getString("Menu.Edit.Redo");

		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}

		for (int i = ++index, j = 0; j < 7; i++, j++) {
			ca = getCommandAction(tr, commandConstants[i]);
			acc = (KeyStroke) ca.getValue(Action.ACCELERATOR_KEY);

			if (acc != null) {
				accString = convertAccKey(acc);
				descString = (String) ca.getValue(Action.SHORT_DESCRIPTION);

				if (descString == null) {
					descString = "";
				}

				if (accString != null) {
					shortCuts.add(accString);
					descriptions.add(descString);
				}
			}
			index = i;
		}
		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_H, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		descString = ElanLocale.getString("Menu.Help.Contents");
		if (accString != null) {
			shortCuts.add(accString);
			descriptions.add(descString);
		}
			
		accString = convertAccKey(KeyStroke.getKeyStroke(KeyEvent.VK_1, 
				ActionEvent.ALT_MASK + ActionEvent.SHIFT_MASK +
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		accString = accString.substring(0, accString.length() - 2);
		shortCuts.add(accString);
		descriptions.add(ElanLocale.getString("MultiTierViewer.ShiftToolTip"));
		
		// create array
		Object[][] resultTable = new Object[shortCuts.size()][2];

		for (int j = 0; j < shortCuts.size(); j++) {
			resultTable[j][0] = shortCuts.get(j);
			resultTable[j][1] = descriptions.get(j);
		}
	    return resultTable;
	}

	//Input is something like: 'Keycode Ctrl+Alt+ShiftB-P'
	//Matching output: 'Ctrl+Alt+Shift+B'
	//
	//The order of Ctrl, Alt and Shift is always like this, regardless of the order
	//when the accelerator was made.
	/**
	 * The String representation has changed in J1.5. Therefore the construction 
	 * of the shortcut (accelerator) text is now based only only the modifiers 
	 * and the KeyCode or KeyChar.
	 */
	public static String convertAccKey(KeyStroke acc) {
		// special case for the Mac
		if (System.getProperty("os.name").startsWith("Mac")) {
			return convertMacAccKey(acc);
		}
		int modifier = acc.getModifiers();
		String nwAcc = "";
		if ((modifier & InputEvent.CTRL_MASK) != 0) {
			nwAcc += "Ctrl+";
		}
		if ((modifier & InputEvent.ALT_MASK) != 0) {
			nwAcc += "Alt+";
		}
		if ((modifier & InputEvent.SHIFT_MASK) != 0) {
			nwAcc += "Shift+";
		}
		if (acc.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
			nwAcc += KeyEvent.getKeyText(acc.getKeyCode());
		} else {
			nwAcc += String.valueOf(acc.getKeyChar());
		}
		/*
		String strAcc = "" + acc;
		
		//remove 'Keycode ' at begin and '-P' at end
		strAcc = strAcc.substring(8, strAcc.length() - 2);

		int indexShift = strAcc.indexOf("Shift");
		int indexAlt = strAcc.indexOf("Alt");
		int indexCtrl = strAcc.indexOf("Ctrl");

		String strAccEnd = "";

		//insert a '+' at the right position
		if (indexShift != -1) {
			strAccEnd = strAcc.substring(indexShift + 5);
			strAcc = strAcc.substring(0, indexShift + 5);
			strAcc = strAcc + "+";
			strAcc = strAcc + strAccEnd;
		}
		else if (indexAlt != -1) {
			strAccEnd = strAcc.substring(indexAlt + 3);
			strAcc = strAcc.substring(0, indexAlt + 3);
			strAcc = strAcc + "+";
			strAcc = strAcc + strAccEnd;
		}
		else if (indexCtrl != -1) {
			strAccEnd = strAcc.substring(indexCtrl + 4);
			strAcc = strAcc.substring(0, indexCtrl + 4);
			strAcc = strAcc + "+";
			strAcc = strAcc + strAccEnd;
		}
		System.out.println("Sh: " + strAcc);
		System.out.println("New Sh: " + nwAcc);
		*/
		return nwAcc;
	}
	
	/**
	 * @see #convertAccKey(KeyStroke)
	 * @param acc the KeyStroke
	 * @return a String representation
	 */
	private static String convertMacAccKey(KeyStroke acc) {
		int modifier = acc.getModifiers();
		String nwAcc = "";
		if ((modifier & InputEvent.META_MASK) != 0) {
			nwAcc += "Command+";
		}
		if ((modifier & InputEvent.CTRL_MASK) != 0) {
			nwAcc += "Ctrl+";
		}
		if ((modifier & InputEvent.ALT_MASK) != 0) {
			nwAcc += "Alt+";
		}
		if ((modifier & InputEvent.SHIFT_MASK) != 0) {
			nwAcc += "Shift+";
		}
		if (acc.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
			nwAcc += KeyEvent.getKeyText(acc.getKeyCode());
		} else {
			nwAcc += String.valueOf(acc.getKeyChar());
		}
		
		//String strAcc = "" + acc;
		
		//mac sometimes gives 'Option' instead of 'Alt'
		//change this ??
		/*
		if (indexOption != -1) {
			String strTempEnd = strAcc.substring(indexOption + 6);
			strAcc = strAcc.substring(0, indexOption);
			strAcc = strAcc + "Alt";
			strAcc = strAcc + strTempEnd;
		}
		*/
		/*
		//remove 'Keycode ' at begin and '-P' at end
		strAcc = strAcc.substring(8, strAcc.length() - 2);
		int indexOption = strAcc.indexOf("Option");
		int indexCommand = strAcc.indexOf("Command");
		
		int indexShift = strAcc.indexOf("Shift");
		int indexAlt = strAcc.indexOf("Alt");
		int indexCtrl = strAcc.indexOf("Ctrl");

		String strAccEnd = "";

		//insert a '+' at the right position
		if (indexShift != -1) {
			strAccEnd = strAcc.substring(indexShift + 5);
			strAcc = strAcc.substring(0, indexShift + 5);
			strAcc = strAcc + "+" + strAccEnd;
		}
		else if (indexAlt != -1) {
			strAccEnd = strAcc.substring(indexAlt + 3);
			strAcc = strAcc.substring(0, indexAlt + 3);
			strAcc = strAcc + "+" + strAccEnd;
		}
		else if (indexOption != -1) {
			strAccEnd = strAcc.substring(indexAlt + 6);
			strAcc = strAcc.substring(0, indexAlt + 6);
			strAcc = strAcc + "+" + strAccEnd;
		}
		else if (indexCtrl != -1) {
			strAccEnd = strAcc.substring(indexCtrl + 4);
			strAcc = strAcc.substring(0, indexCtrl + 4);
			strAcc = strAcc + "+" + strAccEnd;
		}
		else if (indexCommand != -1) {
			strAccEnd = strAcc.substring(indexCommand + 7);
			strAcc = strAcc.substring(0, indexCommand + 7);
			strAcc = strAcc + "+" + strAccEnd;
		}
		*/
		return nwAcc;
	}
}
