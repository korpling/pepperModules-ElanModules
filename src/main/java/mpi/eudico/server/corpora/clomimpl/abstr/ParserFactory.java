/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package mpi.eudico.server.corpora.clomimpl.abstr;

import mpi.eudico.server.corpora.clomimpl.chat.CHATParser;
//import mpi.eudico.server.corpora.clomimpl.cgn2acm.CGN2ACMParser;
import mpi.eudico.server.corpora.clomimpl.delimitedtext.DelimitedTextParser;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF21Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF22Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF23Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF24Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF25Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF26Parser;
import mpi.eudico.server.corpora.clomimpl.dobes.EAF27Parser;
import mpi.eudico.server.corpora.clomimpl.flex.FlexParser;
import mpi.eudico.server.corpora.clomimpl.shoebox.ShoeboxParser;
import mpi.eudico.server.corpora.clomimpl.shoebox.ToolboxParser;
import mpi.eudico.server.corpora.clomimpl.transcriber.Transcriber14Parser;

/**
 * @author hennie
 *
 * @version Dec 2006: constant for EAF24 added
 * @version Nov 2007 constant for EAF25 and CSV (tsb-del. text) added
 * @version May 2008 constant for EAF26 added
 */
public class ParserFactory {

	public static final int EAF21 = 0;
	public static final int CHAT = 1;
	public static final int SHOEBOX = 2;
	public static final int TRANSCRIBER = 3;
	public static final int CGN = 4;
	public static final int WAC = 5;
	public static final int EAF22 = 6;
	public static final int EAF23 = 7;
	public static final int EAF24 = 8;
	public static final int EAF25 = 9;
	public static final int CSV = 10;
	public static final int EAF26 = 11;
	public static final int TOOLBOX = 12;
	public static final int FLEX = 13;
	public static final int EAF27 = 14;
	
	public static Parser getParser(int parserCode) {
		switch (parserCode) {
		case EAF21:
			return new EAF21Parser();
		case EAF22:
			return new EAF22Parser();
		case EAF23:
			return new EAF23Parser();
		case EAF24:
			return new EAF24Parser();
		case EAF25:
			return new EAF25Parser();
		case CHAT:
			return new CHATParser();
		case SHOEBOX:
			return new ShoeboxParser();
		case TRANSCRIBER:
			return new Transcriber14Parser();
		case CGN: {
			//done this way to avoid explicit dependencies!
			Parser parser = null;
			try{
				parser = (Parser) Class.forName("mpi.eudico.server.corpora.clomimpl.cgn2acm.CGN2ACMParser").newInstance();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return parser;
		}
		case CSV:
			return new DelimitedTextParser();
		case EAF26:
			return new EAF26Parser();
		case TOOLBOX:
			return new ToolboxParser();
		case FLEX:
			return new FlexParser();
		case EAF27:
			return new EAF27Parser();
		default:
			return null;
		}		
	}
}
