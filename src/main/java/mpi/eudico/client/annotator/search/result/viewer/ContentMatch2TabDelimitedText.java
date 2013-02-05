package mpi.eudico.client.annotator.search.result.viewer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import mpi.eudico.client.util.Transcription2TabDelimitedText;
import mpi.search.content.result.model.ContentMatch;

public class ContentMatch2TabDelimitedText {

	static public void exportMatches(List matches, File exportFile) throws IOException{
		exportMatches(matches, exportFile, "UTF-8");
	}
	
	/**
     * Exports a List of Matches to Tab limited text (as exportAnnotations, but
     * with file name of match)
     *
     * @param matches
     * @param exportFile
     *
     * @throws IOException
     */
    static public void exportMatches(List matches, File exportFile, String encoding)
        throws IOException {
        if (exportFile == null) {
            return;
        }

        FileOutputStream out = new FileOutputStream(exportFile);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out,
                    encoding));

        for (int i = 0; i < matches.size(); i++) {
            if (matches.get(i) instanceof ContentMatch) {
                ContentMatch match = (ContentMatch) matches.get(i);

                if (!"".equals(match.getFileName())) {
                    writer.write(match.getFileName() + Transcription2TabDelimitedText.TAB);
                }

                writer.write(match.getTierName() + Transcription2TabDelimitedText.getTabString(match));
            }
        }

        writer.close();
    }
}
