package mpi.eudico.server.corpora.clomimpl.dobes;

import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
   <p>
   You explicitely have to add the ANNOTATION_DOCUMENT to the EAF DOM:
   <code>
   Element e = newAnnotationDocument(new Date()+"", "gregor", "version 1.0");
   daf.appendChild(e);
   </code>


   <p>
   To write the DOM to file, use
   getDocumentElement(), which returns all including Elements.
   <code>
   System.out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
   System.out.println("<!DOCTYPE Trans SYSTEM \"EAFv2.0.dtd\">");
   System.out.println(""+daf.getDocumentElement());
   </code>


   The arguments of the newELEMENT() methods must not be null, if not otheriwse
   stated. A RuntimeException will be thrown if an argument is null.
   This is much better than to silently write incomplete data.
   
   @version jun 2004 Support for Controlled Vocabulary elements added
   @version Feb 2006 support for LinkedFileDescriptors and stereotype Included_In added
   @version Jan 2007 support for Annotator attribute added to the tier element
                     added element PROPERTY within the HEADER element
   @version Nov 2007 added support for attribute RELATIVE_MEDIA_URL of MEDIA_DESCRIPTOR and
 *                   RELATIVE_LINK_URL of LINKED_FILE_DESCRIPTOR
 * @version May 2008: added support for references to concepts in the ISO Data Category Registry.
 * This applies to annotations (alignable and ref), CV entries and Linguistic Types. 
 * The methods are no longer final so that the claa can be extended.
*/
public class EAF26 {

	/**
		Three Time Units
	 */
	public final static String TIME_UNIT_MILLISEC = "milliseconds";
	public final static String TIME_UNIT_NTSC     = "NTSC-frames";
	public final static String TIME_UNIT_PAL      = "PAL-frames";

	/**
	   The DOM document variable doc is a private,
	   all newELEMENT method calls will modify it.
	 */
	protected Document doc;

	/**
	   EAF is per se a subtype of org.w3c.dom.Document.
	   It is not inheriting from Document, because only two methods have to be
	   supported.
	 */

	/** see org.w3c.dom.Element.getDocumentElement() */
	public Element getDocumentElement() { return this.doc.getDocumentElement(); }

	/** see org.w3c.dom.Element.appendChild() */
	public Node appendChild(Node e) { return this.doc.appendChild(e); }

	/**
	   All methods "newELEMENT()" are returning an obkect of type org.w3c.dom.Element
	   with name ELEMENT.
	   Technically speaking, they encapsulate
	   the two methods Element.setAttribute() and Element.createTextNode() and do some
	   housekeeping.
	*/

	/**
	   Use result in <your daf variable>.appendChild();
	   @return a new Element ANNOTATION_DOCUMENT.
	   @param creationDate Creation date of this annotation
	   @param author Author of this annotation.
	   @param version --->documentation missing<---
	 */


	public Element newAnnotationDocument
		(String creationDate, String author, String version) {
		if (creationDate == null) throw new RuntimeException("EAF");
		if (author == null) throw new RuntimeException("EAF");
		if (version == null) throw new RuntimeException("EAF");

		Element result = this.doc.createElement("ANNOTATION_DOCUMENT");
		result.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		result.setAttribute("xsi:noNamespaceSchemaLocation", "http://www.mpi.nl/tools/elan/EAFv2.6.xsd");
		result.setAttribute("DATE",   creationDate);
		result.setAttribute("AUTHOR", author);
		result.setAttribute("VERSION", version);
		result.setAttribute("FORMAT", "2.6");
		return result;
    }


	/**
	   Use result in annotationDocument.appendChild();
	   @return a new Element HEADER.
	   @param mediaFile --->documentation missing<---
	   @param timeUnits --->documentation missing<---
	 */
	public Element newHeader (String mediaFile, String timeUnits) {
		if (mediaFile == null) throw new RuntimeException("EAF");
		if (timeUnits == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("HEADER");
		result.setAttribute("MEDIA_FILE",   mediaFile);
		result.setAttribute("TIME_UNITS", timeUnits);
		return result;
    }


	/**
	   @return a new Element HEADER with a default time unit of milliseconds.
	   @param mediaFile same meaning as above
	 */
	public Element newHeader (String mediaFile) {
		if (mediaFile == null) throw new RuntimeException("EAF");
		return newHeader(mediaFile, EAF26.TIME_UNIT_MILLISEC);
    }


    /**
     * Since eaf 2.1: support MediaDescriptors. For compatibility with
     * ELAN 1.4.1 still maintain mediaFile for some time
     */
    public Element newMediaDescriptor(String mediaURL, String relMediaURL,
    				String mimeType, String timeOrigin, String extractedFrom) {

		if (mediaURL == null) throw new RuntimeException("EAF");
		if (mimeType == null) throw new RuntimeException("EAF");

		Element mdElement = this.doc.createElement("MEDIA_DESCRIPTOR");
		mdElement.setAttribute("MEDIA_URL", mediaURL);
		mdElement.setAttribute("MIME_TYPE", mimeType);

		if (relMediaURL != null && relMediaURL.length() != 0) {
			mdElement.setAttribute("RELATIVE_MEDIA_URL", relMediaURL);
		}
		if (timeOrigin != null) {
			mdElement.setAttribute("TIME_ORIGIN", String.valueOf(timeOrigin));
		}
		if (extractedFrom != null) {
			mdElement.setAttribute("EXTRACTED_FROM", extractedFrom);
		}

		return mdElement;
	}
    
    /**
     * Introduced in eaf 2.3, descriptor of (non a/v) linked files.
     * 
     * @param linkURL the url of the file
     * @param relLinkURL an optional relative file url
     * @param mimeType the mimetype of the file
     * @param origin the time origin or offset
     * @param associatedWith the file this link is associated with
     * @return an linked file element
     */
    public Element newLinkedFileDescriptor(String linkURL, String relLinkURL,
            String mimeType, String origin, String associatedWith) {
        if (linkURL == null) throw new RuntimeException("EAF");
        
        Element lfdElement = this.doc.createElement("LINKED_FILE_DESCRIPTOR");
        lfdElement.setAttribute("LINK_URL", linkURL);
        
        if (mimeType == null) {
            lfdElement.setAttribute("MIME_TYPE", "unknown");
        } else {
            lfdElement.setAttribute("MIME_TYPE", mimeType);
        }
        
        if (relLinkURL != null && relLinkURL.length() != 0) {
        	lfdElement.setAttribute("RELATIVE_LINK_URL", relLinkURL);
        }
        
        if (origin != null) {
            lfdElement.setAttribute("TIME_ORIGIN", origin);
        }
        
        if (associatedWith != null) {
            lfdElement.setAttribute("ASSOCIATED_WITH", associatedWith);
        }
        
        return lfdElement;
    }

    /**
     * Introduced in EAF version 2.5. Document level property with an optional 
     * name attribute and string contents.
     * 
     * @param name the name attribute
     * @param value the content
     * @return the property element
     */
    public Element newProperty(String name, String value) {
    	Element propElement = this.doc.createElement("PROPERTY");
    	
    	if (name != null && name.length() > 0) {
    		propElement.setAttribute("NAME", name);
    	}
    	if (value != null && value.length() > 0) {
    		propElement.appendChild(doc.createTextNode(value));
    	}
    	return propElement;
    }
    
	/**
	   Use result in annotationDocument.appendChild();
	   @return a new Element TIME_ORDER.
	 */
	public Element newTimeOrder () {
		Element result = this.doc.createElement("TIME_ORDER");
		return result;
    }
	/**
	   Use result in time_order.appendChild();
	   @return a new Element TIME_SLOT with a time
	   @param id   --->documentation missing<---
	   @param time a time slot has a precise time. NULLABLE
	 */
	public Element newTimeSlot (String id, long time) {
		if (id == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("TIME_SLOT");
		result.setAttribute("TIME_SLOT_ID", id);
		result.setAttribute("TIME_VALUE", time + "");
		return result;
    }
	/**
	   Use result in time_order.appendChild();
	   @return a new Element TIME_SLOT without time.
	   @param id   --->documentation missing<---
	   (Java remark: long is not an object and therfore cannot be null.)
	 */
	public Element newTimeSlot (String id) {
		if (id == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("TIME_SLOT");
		result.setAttribute("TIME_SLOT_ID", id);
		return result;
    }
	/**
	   Use result in annotationDocument.appendChild();
	   @return a new Element TIER.
	   @param id   --->documentation missing<---
	   @param time --->documentation missing<---
	   @param participant --->documentation missing<--- NULLABLE
	   @param annotator the annotator, can be null
	   @param typeRef --->documentation missing<---
	   @param language --->documentation missing<---
	   @param parent --->documentation missing<--- NULLABLE
	 */
	public Element newTier
		(String id,
		 String participant,
		 String annotator,
		 String typeRef,
		 Locale language,
		 String parent) {
		if (id == null) throw new RuntimeException("EAF");
		if (typeRef == null) throw new RuntimeException("EAF");
		//if (language == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("TIER");
		result.setAttribute("TIER_ID", id);
		if (participant != null && participant.length() > 0) 
			result.setAttribute("PARTICIPANT", participant);
		if (annotator != null && annotator.length() > 0) 
			result.setAttribute("ANNOTATOR", annotator);
		result.setAttribute("LINGUISTIC_TYPE_REF", typeRef);
		if (language != null) {
			result.setAttribute("DEFAULT_LOCALE", language.getLanguage());
		}
		if (parent != null) result.setAttribute("PARENT_REF", parent);
		return result;
    }
	/**
	   Use result in tier.appendChild();
	   @return a new Element ANNOTATION.
	 */
	public Element newAnnotation () {
		Element result = this.doc.createElement("ANNOTATION");
		return result;
    }
	/**
	   Use result in annotation.appendChild();
	   @return a new Element ALIGNABLE_ANNOTATION.
	   @param id   --->documentation missing<---
	   @param beginTimeSlot --->documentation missing<---
	   @param endTimeSlot --->documentation missing<---
	   @param svgId the id of the referenced svg element
	   @param extRefId the ref to the id of an EXTERNAL_REF element
	 */
	public Element newAlignableAnnotation
		(String id,
		 String beginTimeSlot,
		 String endTimeSlot,
		 String svgId,
		 String extRefId) {
		if (id == null) throw new RuntimeException("EAF");
// TO DO add EXT REF
		Element result = this.doc.createElement("ALIGNABLE_ANNOTATION");
		result.setAttribute("ANNOTATION_ID", id);
		result.setAttribute("TIME_SLOT_REF1", beginTimeSlot);
		result.setAttribute("TIME_SLOT_REF2", endTimeSlot);
		if (svgId != null) {
			result.setAttribute("SVG_REF", svgId);
		}
		if (extRefId != null) {
			result.setAttribute("EXT_REF", extRefId);
		}
		return result;
    }
	/**
	   Use result in annotation.appendChild();
	   @return a new Element REF_ANNOTATION.
	   @param id   --->documentation missing<---
	   @param annotationRef --->documentation missing<---
	   @param extRefId the ref to the id of an EXTERNAL_REF element
	 */
	public Element newRefAnnotation
		(String id,
		 String annotationRef,
		 String previousAnnotation,
		 String extRefId) {
		if (id == null) throw new RuntimeException("EAF");
		// TO DO add EXT REF
		if (annotationRef == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("REF_ANNOTATION");
		result.setAttribute("ANNOTATION_ID", id);
		result.setAttribute("ANNOTATION_REF", annotationRef);
		if (previousAnnotation != null) {
			result.setAttribute("PREVIOUS_ANNOTATION", previousAnnotation);
		}
		if (extRefId != null) {
			result.setAttribute("EXT_REF", extRefId);
		}
		return result;
    }
	/**
	   Use result in refAnnotation.appendChild();
	   @return a new Element ANNOTATION_VALUE.
	   @param value   --->documentation missing<---
	 */
	public Element newAnnotationValue (String value) {
		if (value == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("ANNOTATION_VALUE");
		result.appendChild(doc.createTextNode(value));
		return result;
    }
	/**
	   Use result in annotationDocument.appendChild();
	   @return a new Element LINGUISTIC_TYPE.
	   @param id   --->documentation missing<---
	   @param controlledVocabularyName the name of the CV reference
	   @param extRefId the ref to the id of an EXTERNAL_REF element
	 */
	public Element newLinguisticType (String id, boolean timeAlignable, 
		boolean graphicReferences, String constraint, String controlledVocabularyName, String extRefId){
		if (id == null) throw new RuntimeException("EAF");
		// TO DO add EXT REF
		Element result = this.doc.createElement("LINGUISTIC_TYPE");
		result.setAttribute("LINGUISTIC_TYPE_ID", id);
		result.setAttribute("TIME_ALIGNABLE", timeAlignable ? "true" : "false");
		result.setAttribute("GRAPHIC_REFERENCES", graphicReferences ? "true" : "false");
		if (constraint != null) result.setAttribute("CONSTRAINTS", constraint);
		if (controlledVocabularyName != null) {
			result.setAttribute("CONTROLLED_VOCABULARY_REF", controlledVocabularyName);
		}
		if (extRefId != null) {
			result.setAttribute("EXT_REF", extRefId);
		}

		return result;
    }

    	/**
    	 * Use result in annotationDocument.appendChild();
    	 * @return a new Element CONSTRAINT
    	 */
    	public Element newConstraint(String stereotype, String description) {
    		if (stereotype == null) throw new RuntimeException("EAF");
    		Element result = this.doc.createElement("CONSTRAINT");
    		result.setAttribute("STEREOTYPE", stereotype);
    		if (description != null) {
    			result.setAttribute("DESCRIPTION", description);
    		}
    		return result;
    	}

	/**
	   Use result in annotationDocument.appendChild();
	   @return a new Element LOCALE.
	   @param locale   --->documentation missing<---
	 */
	public Element newLocale (Locale l){
		if (l == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("LOCALE");
		result.setAttribute("LANGUAGE_CODE", l.getLanguage());
		if (!l.getCountry().equals("")) result.setAttribute("COUNTRY_CODE", l.getCountry());
		if (!l.getVariant().equals("")) result.setAttribute("VARIANT", l.getVariant());
		return result;
    }
    
    /**
	 * Use result in annotationDocument.appendChild();
	 * 
     * @param conVocId the id (name) of the CV
     * @param description the description of the cv, can be null
     * 
     * @return a new Element CONTROLLED_VOCABULARY.
     */
    public Element newControlledVocabulary (String conVocId, String description) {
    	if (conVocId == null) throw new RuntimeException("EAF");
		Element result = this.doc.createElement("CONTROLLED_VOCABULARY");
		result.setAttribute("CV_ID", conVocId);
		if (description != null) {
			result.setAttribute("DESCRIPTION", description);
		}
    	return result;
    }
    
	/**
	 * Use result in annotationDocument.appendChild();
	 * 
	 * @param value the value of the CVEntry
	 * @param description the description of the entry, can be null
	 * 
	 * @return a new Element CV_ENTRY
	 */
    public Element newCVEntry(String value, String description, String extRef) {
    	if (value == null) throw new RuntimeException("EAF");
    	// TO DO add EXT REF
		Element result = this.doc.createElement("CV_ENTRY");
		result.appendChild(doc.createTextNode(value));
		if (description != null) {
			result.setAttribute("DESCRIPTION", description);
		}
		if (extRef != null) {
			result.setAttribute("EXT_REF", extRef);
		}
		
		return result;
    }

    /**
     * Creates an element for external references; references to externally defined concepts or resources.
     * 
     * @param id the ID of the element
     * @param type the type of reference, one of the types listed in the eaf schema, e.g. "iso12620" 
     * @param value the value of the reference, e.g. a pid or urid of a Data Category. The combination 
     * of type and value must be sufficient to know how to treat the reference.
     * 
     * @return the element
     */
    public Element newExternalReference(String id, String type, String value) {
    	if (id == null || type == null || value == null) throw new RuntimeException("EAF");
    	
    	Element result = this.doc.createElement("EXTERNAL_REF");
    	result.setAttribute("EXT_REF_ID", id);
    	result.setAttribute("TYPE", type);
    	result.setAttribute("VALUE", value);
    	
    	return result;
    }

    /**
	   Remember to append a ANNOTATION_DOCUMENT Element to the doc variable using
	   appendChild();
	*/
    public EAF26() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder        db  = dbf.newDocumentBuilder();
		this.doc = db.newDocument();
    }

}
