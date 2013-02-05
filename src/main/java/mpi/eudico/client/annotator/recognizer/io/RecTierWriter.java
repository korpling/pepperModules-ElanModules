package mpi.eudico.client.annotator.recognizer.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mpi.eudico.client.annotator.recognizer.data.RSelection;
import mpi.eudico.client.annotator.recognizer.data.Segment;
import mpi.eudico.client.annotator.recognizer.data.Segmentation;
import mpi.eudico.client.annotator.recognizer.data.SelectionComparator;
import mpi.eudico.client.util.MutableInt;
import mpi.eudico.util.TimeFormatter;

/**
 * A class for writing segments/selections/tiers to the AVATecH 
 * XML Tier format or AVATecH CSV format.
 * 
 * @author Han Sloetjes
 */
public class RecTierWriter {
	/**
	 * Constructor
	 */
	public RecTierWriter() {
		super();
	}

	/**
	 * Writes the specified selections to an xml file. Assumes there is one "tier", 
	 * or one column in the columns attribute. 
	 * Note: currently no check are done on ovelaps in segments! 
	 * 
	 * @param outputFile the destination file
	 * @param segments the segments/selections, in ascending time order
	 */
	public void write(File outputFile, List<RSelection> segments) throws IOException {
		if (outputFile == null) {
			new IOException("Cannot write to file: file is null");
		}
		String outName = outputFile.getName().toLowerCase();
		boolean xmlOut = !(outName.endsWith("csv") || outName.endsWith("txt"));
		final String SC = ";";
		/*
		discover the number of columns based on repetition of the same time values?
		create a list of occurrences per combination of begin time - end time and
		either take the maximum count as number of columns or infer based on distribution
		the optimal number of columns padding in some cases ignoring selections in others 
		*/
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8")));// utf-8 is always supported, I guess
		if (xmlOut) {
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.print("<TIER xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
			writer.print("xsi:noNamespaceSchemaLocation=\"file:avatech-tier.xsd\" ");
			writer.println("columns=\"tier_1\">");
		} else {
			writer.println("\"#starttime\";\"#endtime\";" + "\"tier_1\"");
		}
		
		if (segments != null && segments.size() > 0) {
			RSelection iter;
			// resolve overlaps??
			for (int i = 0; i < segments.size(); i++) {
				iter = segments.get(i);
				if (xmlOut) {
					writer.print("\t<span start=\"" + TimeFormatter.toSSMSString(iter.beginTime) + "\" ");
					writer.print("end=\"" + TimeFormatter.toSSMSString(iter.endTime) + "\">");
					if (iter instanceof Segment && ((Segment)iter).label != null) {
						writer.print("<v>" + ((Segment)iter).label + "</v>");
					} else {
						writer.print("<v></v>");
					}
					writer.println("</span>");
				} else {
					writer.print(TimeFormatter.toSSMSString(iter.beginTime) + SC);
					writer.print(TimeFormatter.toSSMSString(iter.endTime) + SC);
					if (iter instanceof Segment && ((Segment)iter).label != null) {
						writer.print("\"" + ((Segment)iter).label + "\"");
					} else {
						writer.println();
					}
				}
			}
		}
		if (xmlOut) {
			writer.println("</TIER>");
		} else {
			
		}
		
		writer.close();
	}
	
	/**
	 * Writes the specified segmentations (tiers) and selections to an xml file. 
	 * Assumes the Segmentations come before the selections in the list.
	 * Tries to detect whether the segmentations have the same time spans. 
	 * 
	 * @param outputFile the destination file
	 * @param segments the segments/selections, in ascending time order
	 * @param includeSelections if false only write the tiers/segmentations. otherwise add individual selections
	 */
	public void write(File outputFile, List<Object> segments, boolean includeSelections) throws IOException {
		if (outputFile == null) {
			new IOException("Cannot write to file: file is null");
		}
		String outName = outputFile.getName().toLowerCase();
		boolean xmlOut = !(outName.endsWith("csv") || outName.endsWith("txt"));
		final String SC = ";";
		
		int numTiers = 0;
		int numColumns = 1;
		List<Segmentation> tiers = null;
		Map<RSelection, MutableInt> selMap = null;
		
		if (segments != null && segments.size() > 0) {
			tiers = new ArrayList<Segmentation>(6);
			
			for (Object obj : segments) {
				if (obj instanceof Segmentation) {
					tiers.add((Segmentation) obj);
				}
			}
			numTiers = tiers.size();
			if (numTiers > 1) {
				selMap = new HashMap<RSelection, MutableInt>();
				Segmentation seg = tiers.get(0);
				for (RSelection sel : seg.getSegments()) {
					selMap.put(sel, new MutableInt(1));
				}
				MutableInt val;
				RSelection key;
				for (int i = 1; i < tiers.size(); i++) {
					seg = tiers.get(i);
					for (RSelection sel : seg.getSegments()) {
						Iterator<RSelection> keyIt = selMap.keySet().iterator();
						boolean found = false;
						while (keyIt.hasNext()) {
							key = keyIt.next();
							if (key.beginTime == sel.beginTime && key.endTime == sel.endTime) {
								found = true;
								selMap.get(key).intValue++;
								break;
							}
						}
						if (!found) {
							selMap.put(sel, new MutableInt(1));
						}

					}
				}
				int numKeys = selMap.size();
				int numOccur = 0;
				Iterator<MutableInt> iter = selMap.values().iterator();
				while (iter.hasNext()) {
					numOccur += iter.next().intValue;
				}
				// if more than 50%? of the segments occur in all tiers (on average) treat them as columns
				if (numKeys > 0 && (numOccur / (float) numKeys) >= 1.5) {
					numColumns = numTiers;
				}
			}
		}
		
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), "UTF-8")));// utf-8 is always supported, I guess
		if (xmlOut) {
			writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			writer.print("<TIER xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
			writer.print("xsi:noNamespaceSchemaLocation=\"file:avatech-tier.xsd\" ");
			writer.print("columns=\"");
		} else {
			writer.print("\"#starttime\";\"#endtime\";");
		}
		
		if (numColumns > 1) {
			for (int i = 0; i < numColumns; i++) {
				if (xmlOut) {
					writer.print(tiers.get(i).getName().replaceAll(" ", "_"));
				} else {
					writer.print("\"" + tiers.get(i).getName() + "\"");
				}
				if (i != numColumns - 1) {
					if (xmlOut) {
						writer.print(" ");
					} else {
						writer.print(SC);
					}
				}
			}
			if (xmlOut) {
				writer.println("\">");
			} else {
				writer.println();
			}
		} else {
			if (xmlOut) {
				if (tiers.size() > 0) {
					//writer.println(tiers.get(0).getName().replaceAll(" ", "_") + "\">");
					writer.println("all_tiers\">");
				} else {
					writer.println("tier_1\">");
				}
			} else {
				if (tiers.size() > 0) {
					//writer.println( "\"" + tiers.get(0).getName() + "\"");
					writer.println("\"all_tiers\"");
				} else {
					writer.println("\"tier_1\"");
				}
			}
		}
		
		// write the spans
		if (numColumns == 1) {// one column
			List<RSelection> segs = new ArrayList<RSelection>();
			
			for (Segmentation s : tiers) {
				segs.addAll(s.getSegments());
			}
			if (includeSelections) {
				for (Object obj : segments) {
					if (obj instanceof RSelection) {
						segs.add((RSelection) obj);
					}
				}
			}
			Collections.sort(segs, new SelectionComparator());

			RSelection iter;
			// resolve overlaps??
			for (int i = 0; i < segs.size(); i++) {
				iter = segs.get(i);
				if (xmlOut) {
					writer.print("\t<span start=\"" + TimeFormatter.toSSMSString(iter.beginTime) + "\" ");
					writer.print("end=\"" + TimeFormatter.toSSMSString(iter.endTime) + "\">");
					if (iter instanceof Segment && ((Segment)iter).label != null) {
						writer.print("<v>" + ((Segment)iter).label + "</v>");
					} else {
						writer.print("<v></v>");
					}
					writer.println("</span>");
				} else {
					writer.print(TimeFormatter.toSSMSString(iter.beginTime) + SC);
					writer.print(TimeFormatter.toSSMSString(iter.endTime) + SC);
					if (iter instanceof Segment && ((Segment)iter).label != null) {
						writer.println("\"" + ((Segment)iter).label + "\"");
					} else {
						writer.println();
					}
				}
			}
		} else if (numColumns > 1) {
			// the selMap should not be null
			List<RSelection> segs = new ArrayList<RSelection>(selMap.size());
			segs.addAll(selMap.keySet());
			
			if (includeSelections) { // the selections will be added to all tiers, overlaps are not checked 
				for (Object obj : segments) {
					if (obj instanceof RSelection) {
						segs.add((RSelection) obj);
					}
				}				
			}
			Collections.sort(segs, new SelectionComparator());
			
			int[] counters = new int[numTiers];
			// explicitly set to 0?
			Arrays.fill(counters, 0);
			
			RSelection iter;
			RSelection curSel;
			Segmentation curTier;
			List<RSelection> curSelList;

			for (int i = 0; i < segs.size(); i++) {
				iter = segs.get(i);
				if (xmlOut) {
					writer.print("\t<span start=\"" + TimeFormatter.toSSMSString(iter.beginTime) + "\" ");
					writer.print("end=\"" + TimeFormatter.toSSMSString(iter.endTime) + "\">");
				} else {
					writer.print(TimeFormatter.toSSMSString(iter.beginTime) + SC);
					writer.print(TimeFormatter.toSSMSString(iter.endTime) + SC);
				}
				
				for (int j = 0; j < numTiers; j++) {
					curTier = tiers.get(j);
					curSelList = curTier.getSegments();
					if (counters[j] < curSelList.size()) {
						curSel = curSelList.get(counters[j]);
						if (curSel.beginTime == iter.beginTime && curSel.endTime == iter.endTime) {
							if (curSel instanceof Segment && ((Segment)curSel).label != null) {
								if (xmlOut) {
									writer.print("<v>" + ((Segment)curSel).label + "</v>");
								} else {
									writer.print("\"" + ((Segment)curSel).label + "\"");
								}
							} else {
								if (xmlOut) {
									writer.print("<v></v>");
								}
							}
							counters[j]++;
							//break;
						} else {// fill in empty
							if (xmlOut) {
								writer.print("<v></v>");
							} 
						}
					} else { // no more segments for this tier, fill in empty
						if (xmlOut) {
							writer.print("<v></v>");
						}
					}
					
					if (!xmlOut && j < numTiers -1) {
						writer.print(SC);
					}
				}
				if (xmlOut) {
					writer.println("</span>");
				} else {
					writer.println();
				}
			}
			
		}
		if (xmlOut) {
			writer.println("</TIER>");
		}
		
		writer.close();
	}
}
