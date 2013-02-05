package mpi.eudico.client.annotator.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * A buffer for storing in memory the formatted contents of the last 
 * n log records.
 * 
 * @author Han Sloetjes
 */
public class LogBufferHandler extends Handler {
	private List<String> buffer;
	private SimpleFormatter formatter;
	private int size = 1000;
	private boolean closed = false;
	
	/**
	 * No-arg constructor, default size of the buffer is 1000.
	 */
	public LogBufferHandler() {
		super();
		buffer = new ArrayList<String>(200);
		formatter = new SimpleFormatter();
	}

	/**
	 * Constructor with a parameter to set the size of the buffer.
	 * 
	 * @param maxSize the maximum buffer size 
	 */
	public LogBufferHandler(int maxSize) {
		super();
		size = maxSize;
		buffer = new ArrayList<String>(Math.min(200, size));
		formatter = new SimpleFormatter();
	}
	
	/**
	 * Adds the contents of the LogRecord, a String, to the buffer.
	 * If the buffer is > the maximum size the first element is removed.
	 * @param record the log record
	 */
	public synchronized void publish(LogRecord record) {
		if (!closed) {
		buffer.add(formatter.format(record));
			if (buffer.size() > size) {
				buffer.remove(0);
			}
		}
	}

	/**
	 * Closes the handler. First flushes and then empties and removes the buffer. 
	 */
	public void close() throws SecurityException {
		if (!closed) {
			flush();
			buffer.clear();
		}
	}

	/**
	 * Does nothing, currently.
	 */
	public void flush() {
		
		
	}
	
	/**
	 * Returns a string buffer containing the current logs.
	 * 
	 * @return a string builder containing the buffer
	 */
	public synchronized StringBuilder getLogBuffer() {
		if (!closed) {
			StringBuilder builder = new StringBuilder();
			for (String s : buffer) {
				builder.append(s);
			}
			return builder;
		}
		return null;
	}
	
}
