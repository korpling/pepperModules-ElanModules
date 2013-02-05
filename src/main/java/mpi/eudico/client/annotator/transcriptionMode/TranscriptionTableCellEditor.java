package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import mpi.eudico.client.util.TableSubHeaderObject;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Cell Editor for the transcription table
 * 
 * @author Aarthy Somasundaram
 *
 */
public class TranscriptionTableCellEditor extends DefaultCellEditor {	
	
	private static final String EMPTY = "";
	private Annotation annotation;
	private TranscriptionTableEditBox inlineEditBox;
	private TranscriptionViewer viewer;
	private int startEditInOneClick = 1;
	
	/**
	 * Creates an instance of TranscriptionTableCellEditor
	 *
	 * @param viewer	 
	 */
	public TranscriptionTableCellEditor(TranscriptionViewer viewer) {
		super(new JTextField());
		getComponent().setEnabled(false);
		this.viewer =viewer;
		setClickCountToStart(startEditInOneClick);
	}

	public Component getTableCellEditorComponent(
		JTable table,
		Object value,
		boolean isSelected,
		int row,
		int column) {	
		annotation = null;
		if (inlineEditBox == null) {
			inlineEditBox = new TranscriptionTableEditBox(viewer, (TranscriptionTable)table);
		}
		
		if (value instanceof Annotation) {
			annotation = (Annotation) value;
			configureEditBox(table, row, column);
			viewer.updateMedia(annotation.getBeginTimeBoundary(),annotation.getEndTimeBoundary());
			if(viewer.isAutoPlayBack()){
				viewer.playInterval(annotation.getBeginTimeBoundary(),annotation.getEndTimeBoundary());
			}
			inlineEditBox.startEdit();	
			return inlineEditBox.getEditorComponent();
		} else if (value instanceof String && value.toString().contains(TranscriptionViewer.CREATE_ANN)) {
			try {				
				annotation = createAnnotation( table, row, column);
				if (annotation != null) {					
					table.setValueAt(annotation, row, column);
					configureEditBox(table, row, column);
					viewer.updateMedia(annotation.getBeginTimeBoundary(),annotation.getEndTimeBoundary());
					if(viewer.isAutoPlayBack()){
						viewer.playInterval(annotation.getBeginTimeBoundary(),annotation.getEndTimeBoundary());
					}					
					
//					SwingUtilities.invokeLater(new Runnable() {
//						public void run() {
//							if (inlineEditBox != null) {
//								inlineEditBox.startEdit();
//							}
//						}
//					});
					inlineEditBox.startEdit();					
					return inlineEditBox.getEditorComponent();
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
				//LOG.warning(LogUtil.formatStackTrace(ex));
				return getComponent();
			}
		}
			
		return getComponent();
	}
	
	 public TranscriptionTableEditBox getEditorComponent(){
	       if (inlineEditBox != null) {
	            return inlineEditBox;
	        }
	        return null;
	 }
	
	/**
	 * Creates a new annotation on the current selected cell
	 * 
	 * @param table 
	 * @param row
	 * @param column
	 * 
	 * @return newAnnotation, the new created annotation, can be null
	 */
	private Annotation createAnnotation(JTable table, int row, int column){	
		
		String currentColumn = table.getColumnName(column);		
		int columnNo = -1;
		
		for(int i=1; i< table.getColumnCount(); i++){
			String  columnName= TranscriptionTableModel.COLUMN_PREFIX + i;
			if(currentColumn.startsWith(columnName)){
				columnNo = i;
				break;
			}			
		}
		
		int columnIndexInMap = columnNo - 1 ;
				
		AbstractAnnotation ann = null;
		Annotation newAnnotation = null;
		TierImpl currentTier = null;
		long beginTime = 0L;
		long endTime = 0L;
		
		Object val;
		for(int i= 1; i< table.getColumnCount(); i++){
			if(i == column){
				continue;
			}			
			val =  table.getValueAt(row, i);
			if(val instanceof Annotation){
				 ann = (AbstractAnnotation)val;
				 break;
			}
		}
		
		// if there is no annotation for reference, get the time info from the
		// string value of the current cell			
		if(ann == null){				
			// structure of value is  " CREATE_ANN;tierName;beginTime : endTime";
			String value = (String) table.getValueAt(row, column);			
			String s[] = value.split(";");		
			String timeValue[] = s[2].split(":");
			if(timeValue != null){
				for(int i=0; i < timeValue.length; i++){
					String time =timeValue[i];
					try {
						long t = Long.parseLong(time.trim());
						if(i==0){
							beginTime = t;
						} else if(i==1){
							endTime = t;
						}
					} catch (NumberFormatException e) {
						System.out.println(e);
					}
				}
				
				if(viewer.isTierNamesShown()){
					for(int i = row-1; i>=0; i--){
						Object obj = table.getValueAt(i, column);
						if(obj instanceof TableSubHeaderObject){
							String name = obj.toString();
							currentTier = (TierImpl) (viewer.getViewerManager().getTranscription()).getTierWithId(name);
							break;
						}					
					}
				} else {
					String tierName = s[1];
					currentTier = (TierImpl) (viewer.getViewerManager().getTranscription()).getTierWithId(tierName);					
				}
			}
		} else {
			beginTime = ann.getBeginTimeBoundary();
			endTime = ann.getEndTimeBoundary();
			TierImpl linkedTier = (TierImpl) ann.getTier();			
			if(linkedTier.getLinguisticType().getConstraints() != null &&
					linkedTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){				
				
				TierImpl parentTier = (TierImpl) linkedTier.getParentTier();
				while(parentTier.getLinguisticType().getConstraints() != null && 
						parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
					parentTier = (TierImpl) parentTier.getParentTier();
				}
				currentTier = (viewer.getTierMap().get(parentTier).get(columnIndexInMap));
			} else {
				currentTier =  (viewer.getTierMap().get(linkedTier).get(columnIndexInMap));
			}
		}
			
		if(currentTier != null){
			if(currentTier.isTimeAlignable()){
				newAnnotation =  currentTier.createAnnotation(beginTime, endTime);
			} else {				
				long time =	(beginTime + endTime) / 2;
				newAnnotation = ((TierImpl) currentTier).createAnnotation(time, time);
			}
		}		
		return newAnnotation;
	}

	/**
	 * Configures the edit box, for the active cell
	 * in the table
	 * 
	 * @param table
	 * @param row
	 * @param column
	 */
	private void configureEditBox(JTable table, int row, int column) {			
		inlineEditBox.setAnnotation(annotation);
		//table.setRowHeight(row, (int) (1.5 * table.getRowHeight(row)));
		//table.setRowHeight(row, (int) (table.getRowHeight(row)));
		Font ff = null;
		if (table instanceof TranscriptionTable) {
			ff = ((TranscriptionTable) table).getFontForTier( ((Annotation)table.getValueAt(row, column)).getTier().getName() );
			if(ff == null){
				ff = table.getFont();
			}
			
			ff = new Font(ff.getFontName(), ff.getStyle(), ((TranscriptionTable) table).getFontSize());
		}
		
		if (inlineEditBox.isUsingControlledVocabulary()) {
			table.setRowHeight(row, 120);
			inlineEditBox.configureEditor(
					JScrollPane.class, ff,
				table.getCellRect(row, column, true).getSize());
		} else {			
			inlineEditBox.configureEditor(
				JTextArea.class,	ff,
				table.getCellRect(row, column, true).getSize());
		}
	}
	
	public Object getCellEditorValue() {
		if (annotation != null) {
			return annotation;
		}
		else {
			return EMPTY;
		}
	}
	
	public void showPopUp(Component comp, int x, int y) {
		if (inlineEditBox != null)
			inlineEditBox.showPopUp(comp, x, y);
	}

	public void updateLocale() {
		if (inlineEditBox != null)
			inlineEditBox.updateLocale();
	}
	
	public void commitChanges() {
		if (inlineEditBox != null)
			inlineEditBox.commitChanges();
	}

	public void cancelCellEditing() {
		//super.cancelCellEditing();
		if (inlineEditBox != null) {
			inlineEditBox.cancelEdit();
		}
	}
}
