package mpi.eudico.webserviceclient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.util.ProgressListener;
import mpi.eudico.server.corpora.util.ProcessReport;

/**
 * Utility class for calling REST webservices.
 * 
 * @author Han Sloetjes
 *
 */
public class WsClientRest {
	private final String boundary = "DaDa0x";
	private final String nl = "\r\n";
	
	/**
	 * No arg constructor.
	 */
	public WsClientRest() {
		super();
	}
	
	/**
	 * Calls a web service using POST.
	 * 
	 * @param urlString (base) url
	 * @param params the parameters of the call
	 * @param requestProperties properties for the request
	 * @param files files to upload
	 * @param pr a progress report to add to
	 * @param progListener  progress listener for monitoring the progress
	 * @param beginProg the current position of the progress
	 * @param progExtent the total progress extent for this call 
	 * @return the response as a string
	 * @throws IOException
	 */
	public String callServicePostMethodWithFiles(String urlString, Map<String, String> params, 
			Map<String, String> requestProperties, Map<String, File> files, 
			 ProcessReport pr, ProgressListener progListener, 
			float beginProg, float progExtent) throws IOException {
		if (urlString == null) {
			if (pr != null) {
				pr.append("callServicePostMethod: the webservice url is null.\n");
				return null;
			}
		}
		// step 1: build the complete url string, including params
		URL url = null;
		if (params == null || params.size() == 0) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					System.out.println("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		} else {
			// append the params, if they need to be url encoded, that should be done beforehand
			StringBuilder urlBuilder = new StringBuilder(urlString);
			urlBuilder.append('?');
			Iterator<String> paramIter = params.keySet().iterator();
			String key = null;
			String val = null;
			int i = 0;
			while (paramIter.hasNext()) {
				key = paramIter.next();
				val = params.get(key);
				if (key != null && val != null) {
					if (i > 0) {// don't add a & for the first parameter
						urlBuilder.append('&');
					}
					urlBuilder.append(key);
					urlBuilder.append('=');
					urlBuilder.append(val);
				}
				i++;
			}
			try {
				url = new URL(urlBuilder.toString());
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					System.out.println("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		}
		// step 2: create HttpUrlConnection
		try {
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	        httpConn.setDefaultUseCaches(false);//??
	        httpConn.setUseCaches(false);
	        httpConn.setDoInput(true);
	        httpConn.setDoOutput(true);
	        httpConn.setRequestMethod("POST");
	        httpConn.setInstanceFollowRedirects( false );
	        
	        // specific request properties
	        if (requestProperties != null && requestProperties.size() > 0) {
	        	Iterator<String> propIter = requestProperties.keySet().iterator();
				String key = null;
				String val = null;
	        	while (propIter.hasNext()) {
	        		key = propIter.next();
	        		val = requestProperties.get(key);
	        		if (key != null && val != null) {
	        			httpConn.setRequestProperty(key, val);
	        		}
	        	}
	        } else {// defaults
	        	httpConn.setRequestProperty("User-Agent", "ELAN");
	        	httpConn.setRequestProperty( "Connection", "Keep-Alive");
	        }
	        
	        // POST with files
	        if (files != null) {
	        	// specify a boundary and calculate the total length
	        	//httpConn.setFixedLengthStreamingMode((int) totalLength);
	        	//httpConn.setRequestProperty( "Content-Type", "multipart/form-data;boundary=" + boundary);
	        } else {
	        	
	        }
	        
		} catch (ProtocolException pe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + pe.getMessage() + "\n");
			} else {
				System.out.println("Could not contact the server: " + pe.getMessage());
			}
		} catch (IOException ioe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + ioe.getMessage() + "\n");
			} else {
				System.out.println("Could not contact the server: " + ioe.getMessage());
			}
		}
		// step 3: set properties
		// step 4: create strings for the upload message, calculate size
		// step 5: start upload, monitor progress
		// step 6: read response, return content
		return null;
	}

	/**
	 * Calls a web service using POST method to upload a string object.
	 * 
	 * @param urlString (base) url
	 * @param params the parameters of the call
	 * @param requestProperties properties for the request
	 * @param text string to upload
	 * @param pr a progress report to add to
	 * @param progListener  progress listener for monitoring the progress
	 * @param beginProg the current position of the progress
	 * @param progExtent the total progress extent for this call 
	 * @return the response as a string
	 * @throws IOException
	 */
	public String callServicePostMethodWithString(String urlString, Map<String, String> params, 
			Map<String, String> requestProperties, String text, 
			 ProcessReport pr, ProgressListener progListener, 
			float beginProg, float progExtent) throws IOException {
		if (urlString == null) {
			if (pr != null) {
				pr.append("callServicePostMethod: the webservice url is null.\n");
				return null;
			}
		}

		// step 1: build the complete url string, including params
		URL url = null;
		if (params == null || params.size() == 0) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					System.out.println("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		} else {
			// append the params, if they need to be url encoded, that should be done beforehand
			StringBuilder urlBuilder = new StringBuilder(urlString);
			urlBuilder.append('?');
			Iterator<String> paramIter = params.keySet().iterator();
			String key = null;
			String val = null;
			int i = 0;
			while (paramIter.hasNext()) {
				key = paramIter.next();
				val = params.get(key);
				if (key != null && val != null) {
					if (i > 0) {// don't add a & for the first parameter
						urlBuilder.append('&');
					}
					urlBuilder.append(key);
					urlBuilder.append('=');
					urlBuilder.append(val);
				}
				i++;
			}
			try {
				url = new URL(urlBuilder.toString());
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					System.out.println("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		}
		// step 2: create HttpUrlConnection
		try {
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	        httpConn.setDefaultUseCaches(false);//??
	        httpConn.setUseCaches(false);
	        httpConn.setDoInput(true);
	        httpConn.setDoOutput(true);
	        httpConn.setRequestMethod("POST");
	        httpConn.setInstanceFollowRedirects( false );
	        // step 3: set properties
	        // specific request properties
	        if (requestProperties != null && requestProperties.size() > 0) {
	        	Iterator<String> propIter = requestProperties.keySet().iterator();
				String key = null;
				String val = null;
	        	while (propIter.hasNext()) {
	        		key = propIter.next();
	        		val = requestProperties.get(key);
	        		if (key != null && val != null) {
	        			httpConn.setRequestProperty(key, val);
	        		}
	        	}
	        } else {// defaults
	        	httpConn.setRequestProperty("User-Agent", "ELAN");
	        	httpConn.setRequestProperty( "Connection", "Keep-Alive");
	        }
	        
	        // POST with String object
	        if (text != null) {
	        	// step 4: start upload, monitor progress
	        	// open connection, create output writer and write the text
	        	try {
	                Writer osw = new BufferedWriter(new OutputStreamWriter(httpConn.getOutputStream(),
	                        "UTF-8"));
	                osw.write(text);
	                if (progListener != null) {
	                	progListener.progressUpdated(this, (int) (100 * (beginProg + (progExtent / 2))), 
	                			"Upload complete, waiting for response");
	                }
	                osw.flush();
	        	} catch (IOException ioe) {
	    			if (pr != null) {
	    				pr.append("Could not upload the text: " + ioe.getMessage() + "\n");
	    			} else {
	    				System.out.println("Could not upload the text: " + ioe.getMessage());
	    			}
	        	}
	        } else {
	        	if (pr != null) {
    				pr.append("There is no text to upload." + "\n");
    			} else {
    				System.out.println("There is no text to upload.");
    			}
	        }
	        
	        // step 5: read response, return content
	        int respCode = httpConn.getResponseCode();
	        if (respCode == HttpURLConnection.HTTP_OK) {
	        	BufferedReader procReader = new BufferedReader(
	        			new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
	        	StringBuilder builder = new StringBuilder(1000);
	        	String line = null;
	        	final String nl = "\n";
	        	while ((line = procReader.readLine()) != null) {
	        		builder.append(line);
	        		builder.append(nl);
	        	}
	        	try {
	        		procReader.close();
	        		httpConn.disconnect();//??
	        	} catch (IOException ignoreEx){}
	        	// write log or report
	        	if (pr != null) {
    				pr.append("Successfully received the response text." + "\n");
    			} else {
    				System.out.println("Successfully received the response text.");
    			}
	        	
	        	if (progListener != null) {
                	progListener.progressUpdated(this, (int) (100 * (beginProg + progExtent)), 
                			"Received the response text.");
                }
	        	return builder.toString();
	        } else {
	        	if (pr != null) {
    				pr.append("The server returned an error: " + respCode + "\n");
    			} else {
    				System.out.println("The server returned an error: " + respCode);
    			}
	        }
		} catch (ProtocolException pe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + pe.getMessage() + "\n");
			} else {
				System.out.println("Could not contact the server: " + pe.getMessage());
			}
		} catch (IOException ioe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + ioe.getMessage() + "\n");
			} else {
				System.out.println("Could not contact the server: " + ioe.getMessage());
			}
		}
		
		return null;
	}

	/**
	 * 
	 * @param urlString
	 * @param params
	 * @param requestProperties
	 * @param pr
	 * @param progListener
	 * @param beginProg
	 * @param progExtent
	 * @return
	 * @throws IOException
	 */
	public String callServicePostMethod(String urlString, Map<String, String> params, 
			Map<String, String> requestProperties,  
			 ProcessReport pr, ProgressListener progListener, 
			float beginProg, float progExtent) throws IOException {
		if (urlString == null) {
			if (pr != null) {
				pr.append("callServicePostMethod: the webservice url is null.\n");
				return null;
			}
		}
		// step 1: build the complete url string, including params
		URL url = null;
		if (params == null || params.size() == 0) {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					System.out.println("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		} else {
			// append the params, if they need to be url encoded, that should be done beforehand
			StringBuilder urlBuilder = new StringBuilder(urlString);
			urlBuilder.append('?');
			Iterator<String> paramIter = params.keySet().iterator();
			String key = null;
			String val = null;
			int i = 0;
			
			while (paramIter.hasNext()) {
				key = paramIter.next();
				val = params.get(key);
				if (key != null && val != null) {
					if (i > 0) {// don't add a & for the first parameter
						urlBuilder.append('&');
					}
					urlBuilder.append(key);
					urlBuilder.append('=');
					urlBuilder.append(val);
				}
				i++;
			}
			
			try {
				url = new URL(urlBuilder.toString());
			} catch (MalformedURLException mue) {
				if (pr != null) {
					pr.append("Could not create a valid URL: " + mue.getMessage() + "\n");
				} else {
					System.out.println("Could not create a valid URL: " + mue.getMessage());
				}
				return null; // no show
			}
		}
		// step 2: create HttpUrlConnection
		try {
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	        httpConn.setDefaultUseCaches(false);//??
	        httpConn.setUseCaches(false);
	        httpConn.setDoInput(true);
	        httpConn.setDoOutput(true);
	        httpConn.setRequestMethod("POST");
	        httpConn.setInstanceFollowRedirects( false );
	        
	        // step 3: set properties / specific request properties
	        if (requestProperties != null && requestProperties.size() > 0) {
	        	Iterator<String> propIter = requestProperties.keySet().iterator();
				String key = null;
				String val = null;
				
	        	while (propIter.hasNext()) {
	        		key = propIter.next();
	        		val = requestProperties.get(key);
	        		if (key != null && val != null) {
	        			httpConn.setRequestProperty(key, val);
	        		}
	        	}
	        } else {// defaults
	        	httpConn.setRequestProperty("User-Agent", "ELAN");
	        	httpConn.setRequestProperty( "Connection", "Keep-Alive");
	        }
	        
	        // step 4: connect
	        httpConn.connect();
	        
	        // step 5: read response, return content
	        int respCode = httpConn.getResponseCode();
	        // this is probably not a good general test, can't assume this
	        if (respCode == HttpURLConnection.HTTP_OK) {
	        	//System.out.println("Content-Type: " + httpConn.getContentType());// extract the charset from the content-type?
	        	BufferedReader procReader = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
	        	StringBuilder outputBuilder = new StringBuilder();
				String line = null;
				
				while ((line = procReader.readLine()) != null) {
					outputBuilder.append(line);
				}
				
				try {
					procReader.close();
				} catch (Throwable t) {
					// catch whatever
				}
				
	        	if (pr != null) {
	        		pr.append("Succesfully connected to the server: " + respCode + "\n");
	        	} else {
	        		System.out.println("Succesfully connected to the server: " + respCode);
	        	}
	        	
	        	if (progListener != null) {
	        		progListener.progressUpdated(this, (int)(beginProg + progExtent), "Succesfully connected to the server");
	        	}
	        	
				return outputBuilder.toString();
	        } else {
	        	if (pr != null) {
	        		pr.append("Server returned error code: " + respCode + "\n");
	        	} else {
	        		System.out.println("Server returned error code: " + respCode);
	        	}
	        }
	        
		} catch (ProtocolException pe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + pe.getMessage() + "\n");
			} else {
				System.out.println("Could not contact the server: " + pe.getMessage());
			}
		} catch (IOException ioe) {
			if (pr != null) {
				pr.append("Could not contact the server: " + ioe.getMessage() + "\n");
			} else {
				System.out.println("Could not contact the server: " + ioe.getMessage());
			}
		}

		return null;
	}
	
}
