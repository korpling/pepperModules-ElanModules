package mpi.eudico.client.annotator.search.result.model;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.search.content.result.model.AbstractContentMatch;
import javax.swing.tree.TreeNode;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collection;
/**
 * Created on Jul 22, 2004
 * @author Alexander Klassmann
 * @version Jul 22, 2004
 */
public class ElanMatch extends AbstractContentMatch implements TreeNode{
	final private Annotation annotation;
	final private ElanMatch parentMatch;
	// id of constraint which this match belongs to; used to distinguish between matches of sibling constraints
	final private String constraintId;
	final private Vector children = new Vector();
	
	public ElanMatch(ElanMatch parentMatch , Annotation annotation, String constraintId, int indexWithinTier, int[][] substringIndices){
		this(parentMatch, annotation, constraintId, indexWithinTier, "", "", substringIndices);
	}
	
	public ElanMatch(
			ElanMatch parentMatch,
		Annotation annotation,
		String constraintId,
		int indexWithinTier,
		String leftContext,
		String rightContext,
		int[][] substringIndices) {
			
		this.parentMatch = parentMatch;
		this.annotation = annotation;
		this.constraintId = constraintId;
		setIndex(indexWithinTier);

		setLeftContext(leftContext);
		setRightContext(rightContext);
		setMatchedSubstringIndices(substringIndices);
	}
	
	/**
	* Create new ElanMatch instance with parent and children informations mod. Coralie Villes
	*/
    public ElanMatch(ElanMatch parentMatch, Annotation annotation,
            String constraintId, int indexWithinTier, String leftContext,
            String rightContext, int[][] substringIndices, String parentContext,
            String childrenContext) {
        this.parentMatch = parentMatch;
        this.annotation = annotation;
        this.constraintId = constraintId;
        setIndex(indexWithinTier);

        setLeftContext(leftContext);
        setRightContext(rightContext);
        setMatchedSubstringIndices(substringIndices);
        setParentContext(parentContext);
        setChildrenContext(childrenContext);
    }

	public void addChild(ElanMatch subMatch){
		children.add(subMatch);
	}
	
	public void addChildren(Collection subMatches){
		children.addAll(subMatches);
	}
	
	public void setFileName(String fileName){
	    this.fileName = fileName;
	}
	
	public String getConstraintId(){
		return constraintId;
	}
	
	/* (non-Javadoc)
	 * @see mpi.eudico.search.advanced.result.model.Result#getTierName()
	 */
	public String getTierName() {
		String name = "";
		try {
			name = annotation.getTier().getName();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.search.advanced.result.model.Result#getValue()
	 */
	public String getValue() {
		return annotation.getValue();
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.search.advanced.result.model.Result#getBeginTime()
	 */
	public long getBeginTimeBoundary() {
		return annotation.getBeginTimeBoundary();
	}

	/* (non-Javadoc)
	 * @see mpi.eudico.search.advanced.result.model.Result#getEndTime()
	 */
	public long getEndTimeBoundary() {
		return annotation.getEndTimeBoundary();
	}

	public Annotation getAnnotation() {
		return annotation;
	}
	
    public String getParentContext() {
        return parentContext;
    }
    
    public String getChildrenContext() {
        return childrenContext;
    }

	public void setLeftContext(String context) {
		leftContext = context;
	}

	public void setRightContext(String context) {
		rightContext = context;
	}

    public void setParentContext(String context){
    	parentContext=context;
    }
    
    public void setChildrenContext(String context) {
    	childrenContext=context;
	}
    
	public void setMatchedSubstringIndices(int[][] substringIndices) {
		this.matchedSubstringIndices = substringIndices;
	}

	public Enumeration children(){
		return children.elements();
	}
	
	public boolean getAllowsChildren(){
		return true;
	}
	
	public TreeNode getChildAt(int index){
		return (TreeNode) children.get(index);
	}
	
	public int getChildCount(){
		return children.size();
	}
	
	public int getIndex(TreeNode node){
		return children.indexOf(node);
	}
	
	public TreeNode getParent(){
		return parentMatch;
	}
	
	public boolean isLeaf(){
		return children.size() == 0;
	}
	
	public String toString(){
		return annotation.getValue();
		/*
		StringBuffer sb = new StringBuffer();
		TreeNode loopNode = parentMatch;
		while(loopNode != null){
			sb.append("\t");
			loopNode = loopNode.getParent();
		}
		sb.append(annotation.getValue()+"\n");
		for(int i=0; i<children.size(); i++){
			sb.append(children.get(i));
		}
		return sb.toString();*/
	}
	
	public String toHTML(){
		StringBuffer sb = new StringBuffer("<HTML><BODY>");
		TreeNode loopNode = parentMatch;
		while(loopNode != null){
			loopNode = loopNode.getParent();
		}
		sb.append(annotation.getValue()+"<ul>");
		for(int i=0; i<children.size(); i++){
			sb.append(children.get(i));
		}
		sb.append("</ul>");
		sb.append("</BODY></HTML>");
		return sb.toString();
	}
}
