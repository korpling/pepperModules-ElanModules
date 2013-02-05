package mpi.eudico.client.annotator.linkedmedia;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.util.FileUtility;

import mpi.eudico.server.corpora.clomimpl.abstr.LinkedFileDescriptor;

import java.util.ArrayList;
import java.util.Vector;


/**
 * A TableModel for a table displaying information on other, non audio/video
 * file  descriptors.
 *
 * @author Han Sloetjes
 */
public class LFDescriptorTableModel extends LinkedFilesTableModel {
    private Vector descriptors;

    /**
     * Constructs an empty LFDescriptorTableModel.
     */
    public LFDescriptorTableModel() {
        this(new Vector(0));
    }

    /**
     * Constructs a LFDescriptorTableModel and fills the model with  the data
     * from the specified Vector.
     *
     * @param descriptors the collection of LinkedFileDescriptors
     */
    public LFDescriptorTableModel(Vector descriptors) {
        this.descriptors = (descriptors != null) ? descriptors : new Vector();

        columnIds = new ArrayList();
        columnIds.add(ElanLocale.getString(LABEL_PREF + NAME));
        columnIds.add(ElanLocale.getString(LABEL_PREF + URL));
        columnIds.add(ElanLocale.getString(LABEL_PREF + MIME_TYPE));
        columnIds.add(ElanLocale.getString(LABEL_PREF + ASSOCIATED_WITH));
        columnIds.add(ElanLocale.getString(LABEL_PREF + OFFSET));
        columnIds.add(ElanLocale.getString(LABEL_PREF + LINK_STATUS));

        types = new ArrayList(columnIds.size());
        types.add(String.class);
        types.add(String.class);
        types.add(String.class);
        types.add(String.class);
        types.add(Integer.class);
        types.add(Boolean.class);

        initData();
    }

    /**
     * Initialises Lists of row data from the media descriptors.
     */
    private void initData() {
        data = new ArrayList(descriptors.size());

        for (int i = 0; i < descriptors.size(); i++) {
            LinkedFileDescriptor desc = (LinkedFileDescriptor) descriptors.get(i);
            ArrayList rowData = new ArrayList(getColumnCount());

            String url = desc.linkURL;
            String name = FileUtility.fileNameFromPath(url);
            rowData.add(name);
            rowData.add(url);
            rowData.add(desc.mimeType);
            rowData.add((desc.associatedWith != null) ? desc.associatedWith : N_A);
            rowData.add(new Integer((int) desc.timeOrigin));

            // check if the file exists
            boolean linked = FileUtility.fileExists(desc.linkURL);
            rowData.add(new Boolean(linked));

            data.add(rowData);
        }
    }

    /**
     * Note: silently returns instead of throwing an
     * ArrayIndexOutOfBoundsException
     *
     * @param rowIndex the row to remove
     */
    public void removeRow(int rowIndex) {
        if ((rowIndex >= 0) && (rowIndex < data.size())) {
            data.remove(rowIndex);
            descriptors.remove(rowIndex);
            fireTableDataChanged();
        }
    }

    /**
     * Adds a row with the data of the LinkedFileDescriptor to the model.
     *
     * @param desc the new LinkedFileDescriptor
     *
     * @see #addLinkDescriptor(LinkedFileDescriptor)
     */
    public void addRow(LinkedFileDescriptor desc) {
        if (desc == null) {
            return;
        }

        descriptors.add(desc);

        ArrayList rowData = new ArrayList(getColumnCount());
        String url = desc.linkURL;
        String name = FileUtility.fileNameFromPath(url);
        rowData.add(name);
        rowData.add(url);
        rowData.add(desc.mimeType);
        rowData.add((desc.associatedWith != null) ? desc.associatedWith : N_A);
        rowData.add(new Integer((int) desc.timeOrigin));

        // check if the file exists
        boolean linked = FileUtility.fileExists(desc.linkURL);
        rowData.add(new Boolean(linked));

        data.add(rowData);
        fireTableDataChanged();
    }

    /**
     * Adds a LinkedFileDescriptor to the Vector of LinkedFileDescriptor.
     *
     * @param md the new LinkedFileDescriptor
     *
     * @see #addRow(LinkedFileDescriptor)
     */
    public void addLinkDescriptor(LinkedFileDescriptor md) {
        addRow(md);
    }

    /**
     * Notification that the data in some LinkedFileDescriptor has been changed
     * so the row value list should be updated.
     */
    public void rowDataChanged() {
        initData();
        fireTableDataChanged();
    }
}
