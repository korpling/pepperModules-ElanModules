package mpi.eudico.client.annotator.transcriptionMode;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.util.TableSubHeaderObject;
import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.type.Constraint;

/**
 * Cell Renderer for the transcription table
 * 
 * @author aarsom
 */
public class TranscriptionTableCellRenderer extends DefaultTableCellRenderer {
	
	JTextArea area;
	private boolean showTierNames = true;
	private boolean colorOnlyOnNoColumn = false;
	private Transcription transcription;
	public final static Color NO_ANN_BG = new Color(230,230,250);
	private final JLabel EMPTY_LABEL = new JLabel();
	private final Color DEF_LABEL_BG;
	private final Color DEF_AREA_BG;
	private List<String> nonEditableTiers;
	private boolean autoCreate = true;
	
	private final String TOOL_TIP_TEXT_PARTICIPANT = ElanLocale.getString("TranscriptionTable.ToolTipTextForParticipant");
	
	public TranscriptionTableCellRenderer(Transcription transcription){
		this.transcription = transcription;
		EMPTY_LABEL.setOpaque(true);
		DEF_LABEL_BG = new Color (EMPTY_LABEL.getBackground().getRed(), EMPTY_LABEL.getBackground().getGreen(), 
				EMPTY_LABEL.getBackground().getBlue());
		area = new JTextArea();
		DEF_AREA_BG = area.getBackground();
    	area.setLineWrap(true);
    	area.setWrapStyleWord(true);    	
    	area.setMargin(new Insets(0,3,0,3));    	  
	}
	
	public Component getTableCellRendererComponent(JTable table,
       Object value, boolean isSelected, boolean hasFocus, int row,
       int column)
    {   			
		
    	// for the No column
    	if(table.getColumnName(column).equals(ElanLocale.getString(ELANCommandFactory.TRANS_TABLE_CLM_NO))){    		
    		setForeground(Color.BLACK);
        	setFont(table.getFont().deriveFont(Font.PLAIN, table.getFont().getSize()));
        	setText(value.toString());
        	setHorizontalAlignment(CENTER);
        	setVerticalAlignment(TOP);        	     	
        	setOpaque(true); 
        	if(table.getSelectedRow() == row){        			
        		setBackground(Color.LIGHT_GRAY);
        	} else {
        		setBackground(DEF_LABEL_BG);
        	}
        	
        	if(!showTierNames && colorOnlyOnNoColumn){ 	     
        		TierImpl parentTier = null;             		
        		Object val;
        		for(int i= 1; i< table.getColumnCount(); i++){        			
        			val =  table.getValueAt(row, i);
        			if(val instanceof Annotation){
        				TierImpl tier = (TierImpl) ((Annotation)val).getTier();        				
        				if(tier.getLinguisticType().getConstraints() == null || tier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION){
        					parentTier = tier;
        				} else {
        					parentTier = (TierImpl) tier.getParentTier();
        					while( parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
        						parentTier = (TierImpl) parentTier.getParentTier();
        					}
        				}        				
        				break;
        			}
        		}        		
        	
        		if(parentTier == null){	   
        			for(int i = 1; i< table.getColumnCount(); i++){        			
            			val =  table.getValueAt(row, i);
            			if(val instanceof String){  
            				// structure of value is  " CREATE_ANN;tierName;beginTime : endTime";            				
            				String text = val.toString();			
            				String s[] = text.split(";");		
            			    String tierName = s[1];
            			    TierImpl tier = (TierImpl) transcription.getTierWithId(tierName);
            			    if(tier == null){
            			    	continue;
            			    }
            			    if(tier.getLinguisticType().getConstraints() == null || tier.getLinguisticType().getConstraints().getStereoType() != Constraint.SYMBOLIC_ASSOCIATION){
            					parentTier = tier;
            				} else {
            					parentTier = (TierImpl) tier.getParentTier();
            					while( parentTier.getLinguisticType().getConstraints() != null && parentTier.getLinguisticType().getConstraints().getStereoType() == Constraint.SYMBOLIC_ASSOCIATION){
            						parentTier = (TierImpl) parentTier.getParentTier();
            					}
            				} 
            			    break;
            			}
        			}
        		}
        		
        		if(parentTier != null){        			
        			setBackground((Color)((TranscriptionTable)table).getBrightestFontColorForTier(parentTier.getName()));
        			if(parentTier.getParticipant() != null){
        				setToolTipText(TOOL_TIP_TEXT_PARTICIPANT + " : " + parentTier.getParticipant());
        			}
        			
        			if(table.getSelectedRow() == row){            				
        				setBackground((Color)((TranscriptionTable)table).getBrighterFontColorForTier(parentTier.getName()));
        				
            		}
        		}
        	}        	
        	return this;
    	}
    	
    	// tierName rows
    	if (value instanceof TableSubHeaderObject) {
    		setFont(table.getFont().deriveFont(Font.PLAIN, table.getFont().getSize() + 2));
    		setText(value.toString());
    		setHorizontalAlignment(LEFT);
    		setVerticalAlignment(EMPTY_LABEL.getVerticalAlignment());
    		setOpaque(true);
    		if(value.toString().trim().length() > 0){
    			Color  c = (Color)((TranscriptionTable)table).getFontColorForTier(value.toString());
    			setForeground(c);
    			setBackground((Color)((TranscriptionTable)table).getBrightestFontColorForTier(value.toString()));
    		} else {
    			setBackground(DEF_LABEL_BG);
    		}

    		return this;
        } 
    	
    	// for the empty cell when there is no child tier available, 
    	if(value == null ||
    			(value instanceof String && !autoCreate)){    		
    		return EMPTY_LABEL;
    	}  
    	
    	area.setText(getRenderedText(value));    	    	
    	area.setToolTipText(getToolTipText(value));
    	
    	String tierName = ((TranscriptionTable)table).getTierName(row, column);
    	if(tierName != null && nonEditableTiers!= null && nonEditableTiers.contains(tierName)){    		
    		area.setBackground(DEF_LABEL_BG);
    		return area;
    	}
    	area.setBackground(DEF_AREA_BG);
    	
    	if (value instanceof Annotation) {       		
    		Font f = ((TranscriptionTable)table).getFontForTier(((Annotation)value).getTier().getName());    		
    		if(f != null){
    			area.setFont(new Font(f.getFontName(), f.getStyle(), table.getFont().getSize()));
    		}else{
    			area.setFont(table.getFont());
    		} 
    		
    		if(!showTierNames && !colorOnlyOnNoColumn){
    			Color  c = (Color)((TranscriptionTable)table).getFontColorForTier(((Annotation)value).getTier().getName());    	
    			area.setBackground((Color)((TranscriptionTable)table).getBrightestFontColorForTier(((Annotation)value).getTier().getName()));
    		}
    	} else if (value instanceof String) { 
    		if(showTierNames ){    			
    			area.setBackground(NO_ANN_BG);
    		}else {
    			if(colorOnlyOnNoColumn){      				
    				area.setBackground(NO_ANN_BG);
    			} 
    		}
    	}
        return area;
    }
    
    public void setShowTierNames(boolean bool){
    	showTierNames = bool;
    }
    
    public void setNonEditableTiers(List<String> tiers){
    	nonEditableTiers = tiers;    	
    }
    
    public void setAutoCreateAnnotations(boolean create){
    	autoCreate = create;    	
    }
    
    public void showColorOnlyOnNoColumn(boolean bool){
    	colorOnlyOnNoColumn = bool;
    }
    
    private String getRenderedText(Object value) {
        return ((value instanceof Annotation)
        ? ((Annotation) value).getValue()
        : ((value instanceof String) ? "" : null));
    }
    
    private String getToolTipText(Object value) {   
    	String toolTipText = null;
    	if(value instanceof Annotation){    
    		if(!showTierNames){
    			String participant = ((TierImpl)((Annotation) value).getTier()).getParticipant();
    			toolTipText = ((Annotation) value).getTier().getName() ;    		
    			if(participant !=null){
    				toolTipText += " : " + participant;
    			}
    		}else {
    			toolTipText = ((Annotation) value).getBeginTimeBoundary()+" - " + ((Annotation) value).getEndTimeBoundary();
    		}
    	} else if (value instanceof String){
    		// structure of value is  " CREATE_ANN;tierName;beginTime : endTime";
    		if(!showTierNames){
    			String values[] = ((String) value).split(";");
    			if(values.length >= 2){
    				TierImpl tier = (TierImpl) transcription.getTierWithId(values[1]);
    				String participant = tier.getParticipant();
    				toolTipText = tier.getName() ;    		
    				if(participant !=null){
    					toolTipText += " : " + participant;
    				}
    			}    			
    		} else {
    			long beginTime = -1;
    			long endTime = -1;
    			String values[] = ((String) value).split(";");
    			if(values.length >= 3){
    				String timeValue[] = values[2].split(":");    			
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
    				}
    				if(beginTime > -1 && endTime > -1){
    					toolTipText = beginTime+" - " + endTime;
    				}
    			}
    		}
    	}else {
    		return null;
    	}
      return toolTipText;
    }
 }    

