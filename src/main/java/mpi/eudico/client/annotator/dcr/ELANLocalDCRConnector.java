package mpi.eudico.client.annotator.dcr;

import mpi.dcr.DCSmall;

import mpi.dcr.LocalDCRConnector;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.util.ClientLogger;

//import mpi.eudico.util.IoUtil;

//import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;


/**
 * Singleton class for local storage (reading and writing) of part of the
 * information  of a selected set of data categories.
 *
 * @author Han Sloetjes
 */
public class ELANLocalDCRConnector extends LocalDCRConnector {
    private static ELANLocalDCRConnector connector;

    /** the path to the local selection or cahce file */
    String cacheName = Constants.ELAN_DATA_DIR + File.separator +
        "DCSelection.xml";
    private DCRCacheReaderWriter cacheRW;

    /**
     * Creates a new ELANLocalDCRConnector instance
     */
    private ELANLocalDCRConnector() {
        super();
        name = "ELAN Local DCR Connector";

        try {
            cacheRW = new DCRCacheReaderWriter();
            cacheRW.setFilePath(cacheName);
            readDCS();
        } catch (ParserConfigurationException pce) {
            // log
        }
    }

    /**
     * Returns the single instance of this class.
     *
     * @return the single instance of this connector
     */
    public static ELANLocalDCRConnector getInstance() {
        if (connector == null) {
            connector = new ELANLocalDCRConnector();
        }

        return connector;
    }

    /**
     * Load selection from cache.
     *
     * @see mpi.dcr.LocalDCRConnector#readDCS()
     */
    protected void readDCS() {
        try {
            //DataCategorySelection sel = new DataCategorySelection(new File(cacheName));
            List storedDCS = cacheRW.read();

            if ((storedDCS == null) || (storedDCS.size() == 0)) {
                // log
            	
            } else {
                DCSmall small1 = null;

                for (int i = 0; i < storedDCS.size(); i++) {
                    small1 = (DCSmall) storedDCS.get(i);
                    catList.add(small1);
                }
            }
        } catch (Exception ex) { // any exception
        	ClientLogger.LOG.warning("No data categories found in the cache...: " + ex.getMessage());
        }
    }

    /**
     * Save in an xml file.
     *
     * @see mpi.dcr.LocalDCRConnector#saveDCS()
     * @see DCRCacheReaderWriter
     */
    protected void saveDCS() {
        if (catList == null) {
            return;
        }

        if (cacheRW != null) {
            try {
                cacheRW.save(catList);
            } catch (ParserConfigurationException pce) {
            	ClientLogger.LOG.warning("Could not save the DCR cache: " + pce.getMessage());
            } catch (IOException ioe) {
            	ClientLogger.LOG.warning("Could not save the DCR cache: " + ioe.getMessage());
            }
        }
    }

    /**
     * Save in an xml file. Using classes from the ISO12620 package.
     * Unfifnished!
     *
     * @see mpi.dcr.LocalDCRConnector#saveDCS()
     */
    /*
    protected void saveDCS_Old() {
        if (catList == null) {
            return;
        }

        Document doc = null;
        DCR dcr = new DCR();
        dcr.setType(DCR.TYPE_DCR);

        DataCategorySelection sel = new DataCategorySelection();
        sel.setType(DataCategorySelection.TYPE_DCS);

        try {
            doc = sel.makeDocument();
            System.out.println("DE: " + doc.getDocumentElement());
        } catch (ParserConfigurationException pce) {
            // log.error
            pce.printStackTrace();

            return;
        }

        // hier... maybe don't need summaries and selection??
        // or don't use data categories?
        //DataCategorySelection sel = new DataCategorySelection();
        //sel.setType(DataCategorySelection.TYPE_DCS);
        DataCategorySummary sum = null;
        DataCategory dc = null;
        Identifier identifier = null;
        Description desc = null;
        Definition def = null;
        Profile[] profs = null;
        BroaderConceptGeneric bcg = null;
        DCSmall small = null;

        DataCategory[] dcArray = new DataCategory[catList.size()];

        for (int i = 0; i < catList.size(); i++) {
            small = (DCSmall) catList.get(i);
            sum = new DataCategorySummary();
            sum.setType(DataCategorySummary.TYPE_DCSUMMARY);
            identifier = new Identifier();
            identifier.setType(Identifier.TYPE_IDENTIFIER);
            identifier.setContentByString(small.getIdentifier());
            sum.setIdentifier(identifier);
            sum.setIdByString(small.getId());
            //dc = new DataCategory();//??
            //dc.setIdByString(small.getId());
            def = new Definition();
            def.setType(Definition.TYPE_DEFINITION);

            if (small.getDesc() != null) {
                def.setContentByString(small.getDesc());
            }

            desc = new Description();
            desc.setType(Description.TYPE_DESC);
            desc.addDefinition(def);

            if (small.getBroaderDCId() != null) {
                bcg = new BroaderConceptGeneric();
                bcg.setType(BroaderConceptGeneric.TYPE_BROADERCONCEPTGENERIC);
                bcg.setContentByString(small.getBroaderDCId());
                desc.setBroaderConceptGeneric(bcg);
            }

            if ((small.getProfiles() != null) &&
                    (small.getProfiles().length > 0)) {
                profs = new Profile[small.getProfiles().length];

                for (int j = 0; j < small.getProfiles().length; j++) {
                    profs[j] = new Profile();
                    profs[j].setType(Profile.TYPE_PROFILE);
                    profs[j].setContentByString(small.getProfiles()[j]);
                }

                desc.setProfiles(profs);
            }

            dc = new DataCategory();
            dc.setDescription(desc);
            dc.setType(DataCategory.TYPE_DC);
            dc.setId(small.getId());
            dcArray[i] = dc;
            sum.setRegistrationAuthority(new RegistrationAuthority());
            sum.setRegistrationStatus(new RegistrationStatus());
            sum.setVersion(new Version());
            //sel.addDataCategorySummaries(sum);
            sum.makeElement(doc.getDocumentElement());
            desc.makeElement(doc.getDocumentElement());
        }

        //try {
        //dcr.setDataCategories(dcArray);
        
//           DCR dcr = new DCR();
//           dcr.setType(DCR.TYPE_DCR);
//           dcr.setDataCategories(dcArray);
//        
//           Document doc = sel.makeDocument();
//           doc = dcr.makeDocument();
         

        // write document
        try {
            IoUtil.writeEncodedFile("UTF-8", cacheName, doc.getDocumentElement());
        } catch (Exception ioe) {
        }

        //} catch (ParserConfigurationException pce) {
        // log error
        //}
    }
	*/
    /*
       create a custom reader and writer, or keep all the information that has been sent
       example:
       <struct type="DC" id="1496">
           <struct type="AI">
               <struct type="AR"><brack><feat type="identifier">confirm</feat><feat type="version">0.0.0</feat><feat type="registrationAuthority">Private</feat></brack><feat type="registrationStatus">candidate</feat><brack><feat type="creationDate">2004-09-21</feat><feat type="changeDescription"></feat></brack><brack><feat type="lastChangeDate">0000-00-00</feat><feat type="changeDescription"></feat></brack><feat type="origin"></feat><feat type="administrationStatus">Private</feat>
               </struct>
           </struct>
           <struct type="Desc"><feat type="definition" xml:lang="en">'Confirm' is the communicative function of a dialogue act where the speaker has the goal of informing the addressee that the semantic content of the act is true, while believing that the addressee has a weak belief that this is the case.</feat><feat type="definition" xml:lang="nl">Confirm' blabla</feat><feat type="profile">Dialog</feat><feat type="profile">Semantic</feat><feat type="broaderConceptGeneric">answer</feat><feat type="conceptualDomain">dialogueActs</feat>
           </struct>
       </struct>
     */
}
