package mpi.eudico.server.corpora.clom;

/**
 * Annotations implementing this interface are extended by a reference
 * to an external SVG-Element
 * @author Alexander Klassmann
 * @version 14/08/2003
 */
public interface SVGAnnotation {
	public void setSVGElementID(String id);
	public String getSVGElementID();	
}
