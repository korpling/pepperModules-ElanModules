package mpi.eudico.client.annotator.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/**
 * A browser displaying a list of installed fonts. If a font is selected a 
 * list is created with the Unicode blocks that are, at least partly, supported
 * by that font. It is also possible to past text in a text area after which 
 * all fonts that can diaplay that text are listed.
 * 
 * $Id: FontGui.java 20115 2010-09-29 12:34:59Z wilelb $
 * @author $Author$
 * @version $Revision$
 */
public class FontGui extends JFrame implements ActionListener, ListSelectionListener {
    /** Holds value of property DOCUMENT ME! */
    JTextArea _jtext = new JTextArea("Enter UNICODE Text");

    /** Holds value of property DOCUMENT ME! */
    JList _lstUniInfo = null;

    /** Holds value of property DOCUMENT ME! */
    Vector _vUniInfo = new Vector();

    /** Holds value of property DOCUMENT ME! */
    JList _lstFontUniInfo = null;

    /** Holds value of property DOCUMENT ME! */
    Vector vFontUniInfo = new Vector();

    /** Holds value of property DOCUMENT ME! */
    JList _lstsysfonts = null;

    /** Holds value of property DOCUMENT ME! */
    JList _lstrenderfonts = null;

    /** Holds value of property DOCUMENT ME! */
    JLabel _labFontInfo = null;

    /** Holds value of property DOCUMENT ME! */
    Vector _vrendfonts = null;

    /** Holds value of property DOCUMENT ME! */
    java.awt.Font[] _fonts = null;

    /** Holds value of property DOCUMENT ME! */
    Vector _vUniBlock = null;

    /** Holds value of property DOCUMENT ME! */
    FontTable _fonttable = null;

    /**
     * Creates a new FontGui instance
     */
    public FontGui() {
        super("Unicode Font Finder-Explorer");
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        
        // set initial location and size
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        Insets ins = Toolkit.getDefaultToolkit().getScreenInsets(
        		ge.getDefaultScreenDevice().getDefaultConfiguration());
        setSize(Math.min(500, (dim.width - ins.left - ins.right) / 2), 
        		Math.min(800, dim.height - ins.top - ins.bottom));
        setLocation(ins.left, ins.top);
       
        _fonts = ge.getAllFonts();

        Vector tmv = new Vector();

        for (int i = 0; i < _fonts.length; i++) {
            tmv.add(_fonts[i].getFontName());
            _fonts[i] = new Font(_fonts[i].getFontName(), 0, 18);
        }

        _lstsysfonts = new JList(tmv);
        _lstsysfonts.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _lstsysfonts.addListSelectionListener(this);

        JScrollPane listScroller = new JScrollPane(_lstsysfonts);
        listScroller.setPreferredSize(new Dimension(200, 80));
        listScroller.setMinimumSize(new Dimension(200, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        JLabel label = new JLabel("System Fonts");
        label.setLabelFor(_lstsysfonts);

        _lstFontUniInfo = new JList();
        _lstFontUniInfo.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        _lstFontUniInfo.addListSelectionListener(this);

        JScrollPane listUniScroller = new JScrollPane(_lstFontUniInfo);
        listUniScroller.setPreferredSize(new Dimension(48, 80));
        listUniScroller.setMinimumSize(new Dimension(48, 80));
        listUniScroller.setAlignmentX(LEFT_ALIGNMENT);

        _labFontInfo = new JLabel("Font Unicode Information");
        _labFontInfo.setLabelFor(_lstFontUniInfo);
        //_labFontInfo.setMaximumSize(new Dimension(120, 20));

        JPanel syspanel = new JPanel();
        syspanel.setLayout(new BoxLayout(syspanel, BoxLayout.Y_AXIS));
        syspanel.add(label);
        syspanel.add(Box.createRigidArea(new Dimension(0, 5)));
        syspanel.add(listScroller);
        syspanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        syspanel.add(Box.createRigidArea(new Dimension(0, 5)));
        syspanel.add(_labFontInfo);
        syspanel.add(Box.createRigidArea(new Dimension(0, 5)));
        syspanel.add(listUniScroller);
        //syspanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        _vrendfonts = new Vector();
        _lstrenderfonts = new JList(_vrendfonts);
        _lstrenderfonts.addListSelectionListener(this);
        _lstrenderfonts.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        listScroller = new JScrollPane(_lstrenderfonts);
        listScroller.setPreferredSize(new Dimension(200, 80));
        listScroller.setMinimumSize(new Dimension(200, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        label = new JLabel("Fonts that will Render text");
        label.setLabelFor(_lstsysfonts);

        _lstUniInfo = new JList();
        _lstUniInfo.addListSelectionListener(this);

        JScrollPane uniScroller = new JScrollPane(_lstUniInfo);
        uniScroller.setPreferredSize(new Dimension(48, 80));
        uniScroller.setMinimumSize(new Dimension(48, 80));
        uniScroller.setAlignmentX(LEFT_ALIGNMENT);

        JLabel unilabel = new JLabel("Unicode Information");
        unilabel.setLabelFor(_lstUniInfo);

        JPanel renpanel = new JPanel();
        renpanel.setLayout(new BoxLayout(renpanel, BoxLayout.Y_AXIS));
        renpanel.add(label);
        renpanel.add(Box.createRigidArea(new Dimension(0, 5)));
        renpanel.add(listScroller);
        renpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        renpanel.add(Box.createRigidArea(new Dimension(0, 5)));
        renpanel.add(unilabel);
        renpanel.add(Box.createRigidArea(new Dimension(0, 5)));
        renpanel.add(uniScroller);
//        renpanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel fontsPane = new JPanel();
        fontsPane.setLayout(new BoxLayout(fontsPane, BoxLayout.X_AXIS));
        fontsPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
//        fontsPane.add(Box.createHorizontalGlue());
        fontsPane.add(syspanel);
        fontsPane.add(Box.createRigidArea(new Dimension(10, 0)));
        fontsPane.add(renpanel);

        JScrollPane textScroller = new JScrollPane(_jtext);
        textScroller.setPreferredSize(new Dimension(5490, 100));
        textScroller.setMinimumSize(new Dimension(5490, 100));
        textScroller.setAlignmentX(LEFT_ALIGNMENT);

        label = new JLabel("Paste UNICODE text");
        label.setLabelFor(_jtext);

        JPanel textpanel = new JPanel();
        textpanel.setLayout(new BoxLayout(textpanel, BoxLayout.Y_AXIS));
        textpanel.add(label);
        textpanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textpanel.add(textScroller);
        textpanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));

        JPanel buttonpanel = new JPanel();
        buttonpanel.setLayout(new BoxLayout(buttonpanel, BoxLayout.X_AXIS));
        buttonpanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonpanel.add(Box.createHorizontalGlue());

        JButton btncheck = new JButton("Check");
        btncheck.addActionListener(this);
        buttonpanel.add(btncheck);

        buttonpanel.add(Box.createRigidArea(new Dimension(10, 0)));

        JButton btnclear = new JButton("Clear");
        btnclear.addActionListener(this);
        buttonpanel.add(btnclear);
        
        getContentPane().setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        getContentPane().add(fontsPane, gbc);
        gbc.gridy = 1;
        getContentPane().add(textpanel, gbc);
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        getContentPane().add(buttonpanel, gbc);
        /*
         * end gui maddness
         */
        readUniBlockTable();

        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param ae DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent ae) {
        System.out.println(ae.getActionCommand());

        if (ae.getActionCommand().equals("Clear")) {
            //turn off listeners
            _lstsysfonts.removeListSelectionListener(this);
            _lstFontUniInfo.removeListSelectionListener(this);
            _lstrenderfonts.removeListSelectionListener(this);
            _lstUniInfo.removeListSelectionListener(this);

            // clear text area and font area
            _jtext.setText("");
            _vrendfonts.clear();
            _vUniInfo.clear();
            vFontUniInfo.clear();

            _lstrenderfonts.setListData(_vrendfonts);
            _lstUniInfo.setListData(_vUniInfo);
            _lstFontUniInfo.setListData(vFontUniInfo);
            _lstrenderfonts.invalidate();

            _lstsysfonts.addListSelectionListener(this);
            _lstFontUniInfo.addListSelectionListener(this);
            _lstrenderfonts.addListSelectionListener(this);
            _lstUniInfo.addListSelectionListener(this);
        } else if (ae.getActionCommand().equals("Check")) {
            _lstrenderfonts.setListData(_vrendfonts);
            _lstrenderfonts.invalidate();
            checkUniText();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param lse DOCUMENT ME!
     */
    public void valueChanged(ListSelectionEvent lse) {
        if (lse.getSource() == _lstsysfonts) {
            Font f = _fonts[_lstsysfonts.getSelectedIndex()];
            checkFontCapibilities(f);
            _jtext.setFont(f);

            if (_fonttable != null) {
                _fonttable.setFont(f);
            }
        } else if (lse.getSource() == _lstrenderfonts) {
            Font font = new java.awt.Font((String) _vrendfonts.elementAt(
                        _lstrenderfonts.getSelectedIndex()), 0, 20);
            checkFontCapibilities(font);
            _jtext.setFont(font);

            if (_fonttable != null) {
                _fonttable.setFont(font);
            }
        } else if (lse.getSource() == _lstUniInfo) {
            launchBrowser((UnicodeBlock) _vUniInfo.elementAt(
                    _lstUniInfo.getSelectedIndex()));
            hiliteCharsInRange(_lstUniInfo.getSelectedIndex());
        } else if (lse.getSource() == _lstFontUniInfo) {
            if (_lstFontUniInfo.getSelectedIndex() < 0) {
                return;
            }

            launchBrowser((UnicodeBlock) vFontUniInfo.elementAt(
                    _lstFontUniInfo.getSelectedIndex()));
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void checkUniText() {
        String text = _jtext.getText();

        _vUniInfo.clear();

        for (int ii = 0; ii < text.length(); ii++) {
            UnicodeBlock ub = findUniBlock(text.charAt(ii));

            if (!Character.isWhitespace(text.charAt(ii))) {
                if (!_vUniInfo.contains(ub)) {
                    _vUniInfo.add(ub);
                }
            }
        }

        for (int i = 0; i < _fonts.length; i++) {
            java.awt.Font f = _fonts[i];

            if (f.canDisplayUpTo(text) == -1) {
                if (!_vrendfonts.contains(_fonts[i].getFontName())) {
                    _vrendfonts.add(_fonts[i].getFontName());
                }
            }
        }

        _lstrenderfonts.setListData(_vrendfonts);
        _lstrenderfonts.invalidate();
        _lstUniInfo.setListData(_vUniInfo);
    }

    /**
     * DOCUMENT ME!
     */
    public void readUniBlockTable() {
        _vUniBlock = new Vector();

        BufferedReader cdTable = null;

        try {
            cdTable = new BufferedReader(new InputStreamReader(
                        FontGui.class.getResourceAsStream("/mpi/eudico/client/annotator/resources/Blocks.txt")));

            String s;

            while ((s = cdTable.readLine()) != null) {
                UnicodeBlock ucb = new UnicodeBlock();
                StringTokenizer st = new StringTokenizer(s, ";");
                String tok = st.nextToken();

                //			ucb._foo  = Integer.parseInt(tok,16);
                ucb._start = Integer.parseInt(tok, 16);
                ucb._end = Integer.parseInt(st.nextToken(), 16);
                ucb.desc = st.nextToken();
                _vUniBlock.add(ucb);
            }
        } catch (Exception e) {
            e.printStackTrace();

            return;
        }
    }

    /*
     * fixme:: use java's sort/search methods for this
     */
    public UnicodeBlock findUniBlock(char b) {
        UnicodeBlock ucb = null;

        for (Enumeration e = _vUniBlock.elements(); e.hasMoreElements();) {
            ucb = (UnicodeBlock) e.nextElement();

            if (ucb.inRange(b)) {
                return ucb;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     */
    public static void main(String[] args) {
        FontGui g = new FontGui();
        g.setVisible(true);
    }

    /*
     * loop thru the entire unicode page a compute what  pages
     * the passed font can display
     */
    public void checkFontCapibilities(Font font) {
        int charsup;
        UnicodeBlock ucb = null;
        vFontUniInfo.clear();

        for (Enumeration e = _vUniBlock.elements(); e.hasMoreElements();) {
            ucb = (UnicodeBlock) e.nextElement();
            charsup = 0;

            for (int i = ucb._start; i < ucb._end; i++) {
                if (font.canDisplay((char) i)) {
                    charsup++;
                }
            }

            if (charsup > 0) {
                vFontUniInfo.add(ucb); //+" Can Display "+charsup);
            }
        }

        _labFontInfo.setText(font.getFontName() + " Can Display " +
            vFontUniInfo.size());
        _lstFontUniInfo.setListData(vFontUniInfo);
    }

    private void launchBrowser(UnicodeBlock ucb) {
        if (_fonttable == null) {
            _fonttable = new FontTable(ucb._start, ucb._end, ucb.desc,
                    _jtext.getFont());
            
            Point pt = getLocation();
            Dimension dm = getSize();
            pt.x += (int) dm.getWidth();
            _fonttable.setLocation(pt);

            Dimension db = _fonttable.getSize();
            _fonttable.setSize((int) db.getWidth(), (int) dm.getHeight());
            _fonttable.setVisible(true);
        } else {
            _fonttable.reload(ucb._start, ucb._end, ucb.desc, _jtext.getFont());
            if (!_fonttable.isVisible()) {
            	_fonttable.setVisible(true);
            }
        }
        /*
        Point pt = getLocation();
        Dimension dm = getSize();
        pt.x += (int) dm.getWidth();
        _fonttable.setLocation(pt);

        Dimension db = _fonttable.getSize();
        _fonttable.setSize((int) db.getWidth(), (int) dm.getHeight());
        _fonttable.setVisible(true);
        */
    }

    /*
     * for selected Codepage - hi lite area
     * more waste.. just loop thru the chars again
     */
    private void hiliteCharsInRange(int pos) {
        try {
            Highlighter h = _jtext.getHighlighter();
            h.removeAllHighlights();

            UnicodeBlock ucb = (UnicodeBlock) _vUniInfo.elementAt(pos);
            Highlighter.HighlightPainter redHighlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.gray);
            String text = _jtext.getText();

            for (int i = 0; i < text.length(); i++)
                if (ucb.inRange(text.charAt(i))) {
                    h.addHighlight(i, i + 1, redHighlightPainter);
                }
        } catch (Exception e) {
        }
    }

    /**
     * DOCUMENT ME!
     * $Id: FontGui.java 20115 2010-09-29 12:34:59Z wilelb $
     * @author $Author$
     * @version $Revision$
     */
    private class UnicodeBlock {
        /** Holds value of property DOCUMENT ME! */
        int _start;

        /** Holds value of property DOCUMENT ME! */
        int _end;

        /** Holds value of property DOCUMENT ME! */
        String desc;

        /**
         * DOCUMENT ME!
         *
         * @param b DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public boolean inRange(char b) {
            if ((b >= _start) && (b <= _end)) {
                return true;
            }

            return false;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String toString() {
            String s;
            s = desc + " " + Integer.toHexString((int) _start) + " " +
                Integer.toHexString((int) _end);

            return s;
        }
    }

	/**
	 * @see java.awt.Component#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (!visible) {
			if (_fonttable != null) {
				_fonttable.setVisible(visible);
				_fonttable.dispose();
			}
		}
		super.setVisible(visible);
	}
    
    
}
