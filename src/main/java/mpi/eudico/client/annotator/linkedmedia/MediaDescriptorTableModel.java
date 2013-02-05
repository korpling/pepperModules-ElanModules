package mpi.eudico.client.annotator.linkedmedia;

import mpi.eudico.client.annotator.ElanLocale;

import mpi.eudico.client.annotator.util.FileUtility;

import mpi.eudico.server.corpora.clomimpl.abstr.MediaDescriptor;

import java.util.ArrayList;
import java.util.Vector;


/**
 * A TableModel for a table displaying mediadescriptor information.
 *
 * @author Han Sloetjes
 */
public class MediaDescriptorTableModel extends LinkedFilesTableModel {
    private Vector descriptors;

    /**
     * Constructs an empty MediaDescriptorTableModel.
     */
    public MediaDescriptorTableModel() {
        this(new Vector(0));
    }

    /**
     * Constructs a MediaDescriptorTableModel and fills the model with  the
     * data from the specified Vector.
     *
     * @param descriptors the collection of MediaDescriptors
     */
    public MediaDescriptorTableModel(Vector descriptors) {
        this.descriptors = (descriptors != null) ? descriptors : new Vector();

        columnIds = new ArrayList();
        columnIds.add(ElanLocale.getString(LABEL_PREF + NAME));
        columnIds.add(ElanLocale.getString(LABEL_PREF + URL));
        columnIds.add(ElanLocale.getString(LABEL_PREF + MIME_TYPE));
        columnIds.add(ElanLocale.getString(LABEL_PREF + EXTRACTED_FROM));
        columnIds.add(ElanLocale.getString(LABEL_PREF + OFFSET));
        columnIds.add(ElanLocale.getString(LABEL_PREF + MASTER_MEDIA));
        columnIds.add(ElanLocale.getString(LABEL_PREF + LINK_STATUS));

        types = new ArrayList(columnIds.size());
        types.add(String.class);
        types.add(String.class);
        types.add(String.class);
        types.add(String.class);
        types.add(Integer.class);
        types.add(Boolean.class);
        types.add(Boolean.class);

        initData();
    }

    /**
     * Initialises Lists of row data from the media descriptors.
     */
    private void initData() {
        data = new ArrayList(descriptors.size());

        for (int i = 0; i < descriptors.size(); i++) {
            MediaDescriptor desc = (MediaDescriptor) descriptors.get(i);
            ArrayList rowData = new ArrayList(getColumnCount());

            String url = desc.mediaURL;
            String name = FileUtility.fileNameFromPath(url);
            rowData.add(name);
            rowData.add(url);
            rowData.add(desc.mimeType);
            rowData.add((desc.extractedFrom != null) ? desc.extractedFrom : N_A);

            if ((desc.extractedFrom != null) && (i > 0)) {
                rowData.add(new Integer((int) getInheritedOffset(desc)));
            } else {
                rowData.add(new Integer((int) desc.timeOrigin));
            }

            // the first mediadescriptor holds the master media
            rowData.add((i == 0) ? Boolean.TRUE : Boolean.FALSE);

            // check if the file exists
            boolean linked = MediaDescriptorUtil.checkLinkStatus(desc);
            rowData.add(new Boolean(linked));

            data.add(rowData);
        }
    }

    /**
     * If the specified descriptor is extracted from another file,  return the
     * time origin of that other descriptor.
     *
     * @param desc the media descriptor of an audio file
     *
     * @return the time offset of the video file the audio file is extracted
     *         from
     */
    private long getInheritedOffset(MediaDescriptor desc) {
        if (desc == null) {
            return 0L;
        }

        if (desc.extractedFrom == null) {
            return desc.timeOrigin;
        }

        // only inherit if the "extracted from" md is the master media player
        if (descriptors.size() > 0) {
            MediaDescriptor md = (MediaDescriptor) descriptors.get(0);

            if (desc.extractedFrom.equals(md.mediaURL)) {
                return md.timeOrigin;
            }
        }

        /*
           MediaDescriptor md;
           for (int i = 0; i < descriptors.size(); i++) {
               md = (MediaDescriptor) descriptors.get(i);
               if (desc.extractedFrom.equals(md.mediaURL)) {
                   return md.timeOrigin;
               }
           }
         */
        return desc.timeOrigin;
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
     * Adds a row with the data of the MediaDescriptor to the model.
     *
     * @param desc the new MediaDescriptor
     *
     * @see #addMediaDescriptor(MediaDescriptor)
     */
    public void addRow(MediaDescriptor desc) {
        if (desc == null) {
            return;
        }

        descriptors.add(desc);

        ArrayList rowData = new ArrayList(getColumnCount());
        int i = data.size();
        String url = desc.mediaURL;
        String name = FileUtility.fileNameFromPath(url);
        rowData.add(name);
        rowData.add(url);
        rowData.add(desc.mimeType);
        rowData.add((desc.extractedFrom != null) ? desc.extractedFrom : N_A);
        rowData.add(new Integer((int) desc.timeOrigin));

        // the first mediadescriptor holds the master media
        rowData.add((i == 0) ? Boolean.TRUE : Boolean.FALSE);

        // check if the file exists
        boolean linked = MediaDescriptorUtil.checkLinkStatus(desc);
        rowData.add(new Boolean(linked));

        data.add(rowData);
        fireTableDataChanged();
    }

    /**
     * Adds a MediaDescriptor to the Vector of MediaDescriptors.
     *
     * @param md the new MediaDescriptor
     *
     * @see #addRow(MediaDescriptor)
     */
    public void addMediaDescriptor(MediaDescriptor md) {
        addRow(md);
    }

    /**
     * Notification that the data in some MediaDescriptor has been changed  so
     * the row value list should be updated.
     */
    public void rowDataChanged() {
        initData();
        fireTableDataChanged();
    }
}
