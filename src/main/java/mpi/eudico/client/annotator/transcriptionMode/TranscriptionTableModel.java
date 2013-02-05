package mpi.eudico.client.annotator.transcriptionMode;

import java.util.List;

import javax.swing.table.DefaultTableModel;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clomimpl.abstr.AbstractAnnotation;

/**
 * Table model for the transcription table
 * 
 * @author aarsom
 *  
 */
public class TranscriptionTableModel extends DefaultTableModel {
	
	public final static String COLUMN0 = ElanLocale.getString(ELANCommandFactory.TRANS_TABLE_CLM_NO);
	public final static String COLUMN_PREFIX = ElanLocale.getString("TranscriptionTable.ColumnPrefix");
	private String columnIdentifiers[] = {COLUMN0};
	private List<String> nonEditableTiers;	
	private boolean autoCreateAnn = true;

	
	 public TranscriptionTableModel(){ 		
		 for (int i = 0; i < columnIdentifiers.length; i++) {
			this.addColumn(columnIdentifiers[i]);
		}
		 setColumnIdentifiers(columnIdentifiers);
	 }
	 
	 public boolean isCellEditable(int row, int column) {		
		 Object obj = getValueAt(row, column);
		 String tierName = getTierName(row, column);
		 if(tierName != null && nonEditableTiers != null && nonEditableTiers.contains(tierName)){
			 return false;			 
		 }	
		 if(obj instanceof String){
			 return autoCreateAnn;
		 } else{
			 return ( obj instanceof Annotation);
		 }
	 }
	 
	 public void updateModel(final List<String> columnNames){
		 
		 if(getColumnCount()-1 == columnNames.size()){
			 String columnsNames[] = new String[columnNames.size()+1];
			 columnsNames[0] = COLUMN0;
			 for(int i=0; i < columnNames.size(); i++){
				 columnsNames[i+1] = COLUMN_PREFIX + (i+1)+" : " +columnNames.get(i);
			 }			 
			 setColumnIdentifiers(columnsNames);	
			 columnIdentifiers = columnsNames;
		 } else {
			 String columnsNames[] = new String[columnNames.size()+1];
			 columnsNames[0] = COLUMN0;
			 for(int i=0; i < columnNames.size(); i++){
				 columnsNames[i+1] = COLUMN_PREFIX + (i+1)+" : " +columnNames.get(i);
				 this.addColumn(columnsNames[i+1]);
			 }			 
			 setColumnIdentifiers(columnsNames);
			 columnIdentifiers = columnsNames;
		 }
	 }
	 
	 public String[] getColumnIdentifiers(){
		 return columnIdentifiers;
	 }
	 
	 public void setNonEditableTiers(List<String> nonEditableTiers){
		this.nonEditableTiers = nonEditableTiers; 
	 }
	 
	 public void setAutoCreateAnnotations(boolean create){
		 autoCreateAnn = create; 
	 }
	 
	 public boolean isAnnotationsCreatedAutomatically(){
		 return autoCreateAnn;
	 }
	 
	 /**Returns the name of the tier in the given cell
	  * 
	  * @param row
	  * @param column
	  * @return
	  */
	 private String getTierName(int row, int column){ 
	   	String tierName = null;	    	
	   	Object val = getValueAt(row, column);
	   	if(val instanceof Annotation){
	   		AbstractAnnotation ann = (AbstractAnnotation)val;
	   		 tierName = ann.getTier().getName();
	   	} else if(val instanceof String){
	   		// structure of value is  " CREATE_ANN;tierName;beginTime : endTime";
	   		String value = (String) getValueAt(row, column);			
	   		String s[] = value.split(";");	
	   		tierName = s[1];
	   	}
	   	return tierName;
	 }

}
