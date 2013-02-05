package mpi.eudico.server.corpora.clomdefault;

import mpi.eudico.server.corpora.location.DataLocator;
import mpi.eudico.server.corpora.location.DirectoryTree;

/**
   <p>MK:02/06/10<br>
	DataLocator is used in DataManager is used in Corpus is used in CorpusManager.
	CHAT uses the same puppet-chain, and also wraps a DirectoryTree.
	DirectoryTree was a generic class and than geared to CHAT format.<br>
	There is no such puppet-chain for DOBES.
	DataLocator is an empty class.
   </p>

  */
public class DefaultDataLocator extends DataLocator implements java.io.Serializable {

	/**
	 * The base locator of (the top level of?) a Default corpus
	 */
	private DirectoryTree tree;

	public DefaultDataLocator(DirectoryTree tree) {
		this.tree = tree;
	}

	/**
	   Returns a reference to base locator
	 */
	final public DirectoryTree getDirectoryTree() {
		return this.tree;
	}
}
