package mpi.eudico.server.corpora.util;

/**
 * A report that stores the messages in a StringBuffer, without performing any
 * formatting itself.
 *
 * @author Han Sloetjes
 * @version 1.0
 */
public class SimpleReport implements ProcessReport {
    private StringBuffer buffer;
    private String name;

    /**
     * Creates a new SimpleReport instance
     */
    public SimpleReport() {
        super();
        buffer = new StringBuffer();
    }

    /**
     * Creates a new SimpleReport instance
     *
     * @param name the name of the report
     */
    public SimpleReport(String name) {
        this();
        this.name = name;
    }

    /**
     * Appends the message followed by a newline character.
     *
     * @param message the report message
     */
    public void append(String message) {
        if (message != null) {
            buffer.append(message);
            buffer.append('\n');
        }
    }

    /**
     * Clears the messages from the buffer.
     */
    public void clearReport() {
        buffer.delete(0, buffer.length());
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return (name == null ? "" : name);
    }

    /**
     * Returns the report as a single string.
     *
     * @return the report as a string
     */
    public String getReportAsString() {
        return buffer.toString();
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }
}
