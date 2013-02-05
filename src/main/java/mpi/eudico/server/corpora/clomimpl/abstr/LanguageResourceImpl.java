package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clom.LanguageResource;

import java.io.InputStream;


/**
 * LanguageResourceImpl implements generic part of Language Resources
 *
 * @author Daan Broeder
 * @version 27-Sept-2000
 * @version Aug 2005 Identity removed
 */
public abstract class LanguageResourceImpl 
    implements LanguageResource {
    /**
     * mime_type: classification of the resource data to enable tools to
     * recognise it
     */
    protected String mime_type = "";

    /**
     * Creates a new LanguageResourceImpl instance
     */
    LanguageResourceImpl() {
    }

    /**
     * set the mime_type for the LR content data
     *
     * @param s DOCUMENT ME!
     */
    public void setContentType(String s) {
        mime_type = s;
    }

    /**
     * return the associated mime_type of the LR content
     *
     * @return DOCUMENT ME!
     */
    public String getContentType() {
        return mime_type;
    }

    /**
     * return the LR content in the for of an InputStream
     *
     * @return DOCUMENT ME!
     */
    public InputStream getContentStream() {

        return null;
    }

    /**
     * getFullPath() get the localistation specification of the content data
     * return null if not available
     *
     * @return DOCUMENT ME!
     */
    public String getFullPath() {
        //default implementation
        return null;
    }
}
 //LanguageResourceImpl
