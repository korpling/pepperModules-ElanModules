package mpi.eudico.server.corpora.event;

/**
 * ValuesNotListableException is thrown when a CodeType is not of a kind that
 * can list it's possible values. This is either when all text values are
 * legal (free text) or when values are restricted but not listable (for
 * example, syntactical trees).
 *
 * @author Hennie Brugman
 * @author Albert Russel
 * @version 29-Jun-1998
 */
public class ValuesNotListableException extends Exception {
}
