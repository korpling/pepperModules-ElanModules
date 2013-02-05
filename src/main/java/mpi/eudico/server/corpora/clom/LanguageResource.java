package mpi.eudico.server.corpora.clom;


import java.io.InputStream;


/**
 * interface LanguageResource
 * models raw data resource such as a transcript, media file, label file etc.
 * @version Aug 2005 Identity and IdentityManager removed
 */
public interface LanguageResource extends TreeViewable {

// for the moment empty
// public void setFormat(String fmt);
// public String getFormat();

   /**
    * get the URL specification of the content
    * return null if not available
    */
   public String getFullPath();

   public String getContentType();
   public InputStream getContentStream();

}
