package mpi.eudico.server.corpora.lexicon;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


/**
 * A Lexicon structure holding entries and their elements
 * @author Micha Hulsbosch
 *
 */
public class Lexicon implements TreeModel {

	ArrayList<LexiconEntry> entries;
	private String fieldNameOfFocus;
	private DefaultMutableTreeNode currentEntry;
	private String name;
	
	public Lexicon() {
		entries = new ArrayList<LexiconEntry>();
	}
	
	public String toString() {
		return name;
	}

	public void addEntry(LexiconEntry entry) {
		entries.add(entry);
	}
	
	public LexiconEntry getEntry(int index) {
		return entries.get(index);
	}

	public void addTreeModelListener(TreeModelListener listener) {
		// TODO Auto-generated method stub
		
	}

	public Object getChild(Object parent, int index) {
		if(parent instanceof Lexicon) {
			return entries.get(index);
		} else {
			return ((EntryElement) parent).getElements().get(index);
		}
	}

	public int getChildCount(Object parent) {
		if(parent instanceof Lexicon) {
			return entries.size();
		} else {
			return ((EntryElement) parent).getElements().size();
		}
	}

	public int getIndexOfChild(Object parent, Object child) {
		if(parent instanceof Lexicon) {
			return entries.indexOf(child);
		} else {
			return ((EntryElement) parent).getElements().indexOf(child);
		}
	}

	public Object getRoot() {
		return this;
	}

	public boolean isLeaf(Object node) {
		if(node instanceof Lexicon) {
			return false;
		} else {
			return ((EntryElement) node).isField();
		}
	}

	public void removeTreeModelListener(TreeModelListener listener) {
		// TODO Auto-generated method stub
		
	}

	public void valueForPathChanged(TreePath path, Object newvalue) {
		// TODO Auto-generated method stub
		
	}

	public void setName(String name) {
		this.name = name;
	}
}
