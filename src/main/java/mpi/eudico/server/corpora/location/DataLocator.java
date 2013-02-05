package mpi.eudico.server.corpora.location;

import java.io.Serializable;


/**
 * DataLocator specifies an access point to data for a specific Corpus. It
 * stores an access point to data such as a URL or a JDBC Connection. Each
 * specific Corpus is supposed to have it's own subclass of DataLocator. All
 * Corpus data objects have a (list of) DataLocator reference(s) associated
 * with them, one for each Identity using the object. This mapping is
 * administered by a LocatorManager.
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 11-Sep-1998
 */
public class DataLocator implements Serializable {
}
