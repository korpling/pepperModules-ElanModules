package mpi.eudico.client.annotator.imports.multiplefiles;

import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.gui.multistep.MultiStepPane;
import mpi.eudico.client.annotator.util.FileExtension;

public class MFToolboxImportStep1 extends AbstractMFImportStep1 {

	public MFToolboxImportStep1(MultiStepPane mp) {
		super(mp);
	}

	@Override
	protected Object[] getMultipleFiles() {
		return getMultipleFiles(ElanLocale.getString("MultiFileImport.Toolbox.Select"),
   			FileExtension.TOOLBOX_TEXT_EXT, "LastUsedShoeboxTypDir");         
	}

}
