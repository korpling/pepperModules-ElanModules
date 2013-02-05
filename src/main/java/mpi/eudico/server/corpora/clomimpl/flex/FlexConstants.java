package mpi.eudico.server.corpora.clomimpl.flex;

/**
 * Constants for parsing Flex files
 * 
 * @author Han Sloetjes
 * @version 1.0
  */
public interface FlexConstants {
    /** the element document */
    public static final String DOC = "document";
    
    /** the element interlinear-text */
    public static final String IT = "interlinear-text";

    /** the element paragraph */
    public static final String PARAGR = "paragraph";

    /** the element phrase */
    public static final String PHRASE = "phrase";

    /** the element word */
    public static final String WORD = "word";

    /** the element morph */
    public static final String MORPH = "morph";

    /** the element item */
    public static final String ITEM = "item";

    /** the attribute type */
    public static final String TYPE = "type";

    /** the attribute lang */
    public static final String LANG = "lang";

    /** the element language */
    public static final String LANGUAGE = "language";

    /** the attribute txt */
    public static final String TXT = "txt";
    
    /** a general gloss */
    public static final String GLS = "gloss";
    
    /** the attribute punct */
    public static final String PUNCT = "punct";
    
    /** the attribute guid */
    public static final String GUID = "guid";
    
    public static final String FLEX_GUID_ANN_PREFIX = "a_";
    
}
