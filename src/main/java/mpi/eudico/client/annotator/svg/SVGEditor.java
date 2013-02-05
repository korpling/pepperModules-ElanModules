package mpi.eudico.client.annotator.svg;

import mpi.eudico.client.annotator.Constants;
import mpi.eudico.client.annotator.ElanLocale;
import mpi.eudico.client.annotator.Preferences;
import mpi.eudico.client.annotator.ViewerManager2;
import mpi.eudico.client.annotator.commands.Command;
import mpi.eudico.client.annotator.commands.ELANCommandFactory;
import mpi.eudico.client.annotator.player.ElanMediaPlayer;
//import mpi.eudico.client.annotator.player.JMFGraphicMediaPlayer;
//import mpi.eudico.client.annotator.player.QTMediaPlayer;
import mpi.eudico.client.annotator.player.VideoFrameGrabber;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Transcription;

import mpi.eudico.server.corpora.clomimpl.abstr.SVGAlignableAnnotation;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.text.NumberFormat;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;


/**
 * A dialog for editing graphical annotations.
 *
 * @author Han Sloetjes
 */
public class SVGEditor extends JDialog implements ActionListener, ItemListener {
    /** the bounds of the editor dialog */
    protected Rectangle dialogBounds;

    /** the transcription the edited annotation is part of */
    protected Transcription transcription;

    /** the viewermanager */
    protected ViewerManager2 viewerManager;

    /** the annotation that is being edited */
    protected SVGAlignableAnnotation annotation;

    /** an internal frame displaying a list of library objects/shapes */
    protected JInternalFrame libraryFrame;

    /** a table storing icons using it's id value as a key */
    protected Hashtable iconTable;
    private Hashtable libTable = null;

    /** a JList containing icons of the objects in the library */
    protected JList iconList;

    /** the internal frame containing the actual editor panel */
    protected JInternalFrame editFrame;

    /** the panel that handles all editing operations */
    protected EditorPanel editorPanel;

    /** the size of the icons in the library icon list */
    protected final Dimension iconDimension = new Dimension(26, 26);

    /** the zoomlevels of the editor panel */
    public final int[] ZOOMLEVELS = new int[] { 50, 75, 100, 150, 200 };

    /** the color to use for the stroke of 2d annotations */
    public final Color STROKE_COLOR = Color.red;
    private int msPerFrame = 40; //initial value is the PAL value
    private long currentFrame = 0;
    private JButton deleteButton;
    private JButton cutButton;
    private JButton copyButton;
    private JButton pasteButton;
    private JButton firstFrameButton;
    private JButton lastFrameButton;
    private JButton frameBackButton;
    private JButton frameForwardButton;
    private JToolBar statusBar;
    private JLabel xValue;
    private JLabel yValue;
    private JLabel wValue;
    private JLabel hValue;
    private JLabel dValue;
    private JLabel mxValue;
    private JLabel myValue;
    private NumberFormat numbFormat;

    /**
     * Creates a new SVGEditor instance
     *
     * @param transcription the transcription
     * @param annotation the annotation that has a reference to a 2d object
     */
    public SVGEditor(Transcription transcription,
        SVGAlignableAnnotation annotation) {
        super(ELANCommandFactory.getRootFrame(transcription), true);

        //super();
        this.annotation = annotation;
        this.transcription = transcription;
        loadPreferences();
        initDialog();
        setBounds(dialogBounds);
        setTitle(ElanLocale.getString("GraphicsEditor.Title"));
        setVisible(true);
    }

    /**
     * Create a menubar, toolbar, desktop pane and some iframes.
     */
    protected void initDialog() {
        numbFormat = NumberFormat.getInstance();
        numbFormat.setMaximumFractionDigits(3);
        getContentPane().setLayout(new BorderLayout());
        setJMenuBar(createMenuBar());
        getContentPane().add(createToolBar(), BorderLayout.NORTH);
        getContentPane().add(createDesktopPane(), BorderLayout.CENTER);
        getContentPane().add(createStatusBar(), BorderLayout.SOUTH);

        //setContentPane(createDesktopPane());
        initLibrary();

        // add containers
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    cancelEdit();
                }
            });
        updateToolBarShapeButtons();
        updateFramePositionButtons();
        updateObjectStatus();
    }

    /**
     * Creates the menubar.
     *
     * @return the menubar
     */
    protected JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu(ElanLocale.getString("Menu.Edit"));
        menuBar.add(editMenu);

        JMenuItem cancelMI = new JMenuItem(ElanLocale.getString(
                    "GraphicsEditor.Menu.Cancel"));
        cancelMI.addActionListener(this);
        cancelMI.setActionCommand("cancel");
        cancelMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
        editMenu.add(cancelMI);

        JMenuItem commitMI = new JMenuItem(ElanLocale.getString(
                    "GraphicsEditor.Menu.Commit"));
        commitMI.addActionListener(this);
        commitMI.setActionCommand("commit");
        commitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editMenu.add(commitMI);

        return menuBar;
    }

    /**
     * Creates a toolbar containing buttons and other ui elements for the
     * editing operations.
     *
     * @return a toolbar
     */
    protected JToolBar createToolBar() {
        JToolBar bar = new JToolBar(JToolBar.HORIZONTAL);

        // create icons
        ImageIcon selIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/SelectTool16.gif"));
        ImageIcon rectIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/RectangleTool16.gif"));
        ImageIcon ovalIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/OvalTool16.gif"));
        ImageIcon lineIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/LineTool16.gif"));
        ImageIcon deleteIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Delete16.gif"));
        ImageIcon cutIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Cut16.gif"));
        ImageIcon copyIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Copy16.gif"));
        ImageIcon pasteIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/Paste16.gif"));
        ImageIcon firstFrameIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/GoToPreviousScrollviewButton.gif"));
        ImageIcon lastFrameIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/GoToNextScrollviewButton.gif"));
        ImageIcon nextFrameIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/NextButton.gif"));
        ImageIcon prevFrameIcon = new ImageIcon(this.getClass().getResource("/mpi/eudico/client/annotator/resources/PreviousButton.gif"));

        // create buttons
        deleteButton = new JButton(deleteIcon);
        deleteButton.setActionCommand("delete");
        deleteButton.addActionListener(this);
        deleteButton.setEnabled(false);
        bar.add(deleteButton);
        bar.addSeparator();
        cutButton = new JButton(cutIcon);
        cutButton.setActionCommand("cut");
        cutButton.addActionListener(this);
        cutButton.setEnabled(false);
        bar.add(cutButton);
        copyButton = new JButton(copyIcon);
        copyButton.setActionCommand("copy");
        copyButton.addActionListener(this);
        copyButton.setEnabled(false);
        bar.add(copyButton);
        pasteButton = new JButton(pasteIcon);
        pasteButton.setActionCommand("paste");
        pasteButton.addActionListener(this);
        pasteButton.setEnabled(false);
        bar.add(pasteButton);
        bar.addSeparator();

        JToggleButton selButton = new JToggleButton(selIcon, true);
        selButton.setActionCommand("select");
        selButton.addActionListener(this);

        JToggleButton rectButton = new JToggleButton(rectIcon, false);
        rectButton.setActionCommand("recttool");
        rectButton.addActionListener(this);

        JToggleButton ovalButton = new JToggleButton(ovalIcon, false);
        ovalButton.setActionCommand("ovaltool");
        ovalButton.addActionListener(this);

        JToggleButton lineButton = new JToggleButton(lineIcon, false);
        lineButton.setActionCommand("linetool");
        lineButton.addActionListener(this);

        ButtonGroup toolGroup = new ButtonGroup();
        toolGroup.add(selButton);
        toolGroup.add(rectButton);
        toolGroup.add(ovalButton);
        toolGroup.add(lineButton);
        bar.add(selButton);
        bar.add(rectButton);
        bar.add(ovalButton);
        bar.add(lineButton);
        bar.addSeparator();

        firstFrameButton = new JButton(firstFrameIcon);
        firstFrameButton.setActionCommand("first");
        firstFrameButton.addActionListener(this);
        firstFrameButton.setEnabled(false);
        bar.add(firstFrameButton);
        frameBackButton = new JButton(prevFrameIcon);
        frameBackButton.setActionCommand("prev");
        frameBackButton.addActionListener(this);
        frameBackButton.setEnabled(false);
        bar.add(frameBackButton);
        frameForwardButton = new JButton(nextFrameIcon);
        frameForwardButton.setActionCommand("next");
        frameForwardButton.addActionListener(this);
        bar.add(frameForwardButton);
        lastFrameButton = new JButton(lastFrameIcon);
        lastFrameButton.setActionCommand("last");
        lastFrameButton.addActionListener(this);
        bar.add(lastFrameButton);
        bar.addSeparator();

        JPanel zoomPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(1, 3, 1, 3);
        gbc.anchor = GridBagConstraints.WEST;
        zoomPanel.add(new JLabel(ElanLocale.getString("Menu.Zoom")), gbc);

        JComboBox zoomBox = new JComboBox();
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        for (int i = 0; i < ZOOMLEVELS.length; i++) {
            model.addElement(ZOOMLEVELS[i] + "%");
        }

        model.setSelectedItem("100%");
        zoomBox.setModel(model);
        zoomBox.setPreferredSize(new Dimension(60, 24));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        zoomPanel.add(zoomBox, gbc);
        zoomBox.addItemListener(this);

        bar.add(zoomPanel);

        //bar.add(zoomBox);
        return bar;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected JToolBar createStatusBar() {
        statusBar = new JToolBar(JToolBar.HORIZONTAL);
        statusBar.setFloatable(false);

        JPanel objStatusPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        Insets insets = new Insets(2, 2, 2, 2);
        gbc.gridx = 0;
        gbc.insets = insets;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel xLabel = createLabel("X: ", null);
        objStatusPanel.add(xLabel, gbc);

        int lw = xLabel.getFontMetrics(xLabel.getFont()).stringWidth("0000");
        Dimension labdim = new Dimension(lw, Constants.DEFAULTFONT.getSize() +
                2);
        xValue = createLabel(null, labdim);
        gbc.gridx++;
        objStatusPanel.add(xValue, gbc);

        JLabel yLabel = createLabel("Y: ", null);
        yValue = createLabel(null, labdim);
        gbc.gridx++;
        objStatusPanel.add(yLabel, gbc);
        gbc.gridx++;
        objStatusPanel.add(yValue, gbc);

        JLabel wLabel = createLabel("W: ", null);
        gbc.gridx++;
        objStatusPanel.add(wLabel, gbc);
        wValue = createLabel(null, labdim);
        gbc.gridx++;
        objStatusPanel.add(wValue, gbc);

        JLabel hLabel = createLabel("H: ", null);
        gbc.gridx++;
        objStatusPanel.add(hLabel, gbc);
        hValue = createLabel(null, labdim);
        gbc.gridx++;
        objStatusPanel.add(hValue, gbc);

        JLabel dLabel = createLabel("D: ", null);
        gbc.gridx++;
        objStatusPanel.add(dLabel, gbc);
        dValue = createLabel(null, labdim);
        dValue.setPreferredSize(new Dimension(labdim.width * 2, labdim.height));
        gbc.gridx++;
        objStatusPanel.add(dValue, gbc);

        JPanel sep = new JPanel();
        Dimension sepSize = new Dimension(10, labdim.height);
        sep.setPreferredSize(sepSize);
        sep.setMinimumSize(sepSize);
        gbc.gridx++;
        objStatusPanel.add(sep, gbc);

        JLabel mxLabel = createLabel("MX: ", null);
        mxValue = createLabel(null, labdim);

        JLabel myLabel = createLabel("MY: ", null);
        myValue = createLabel(null, labdim);

        gbc.gridx++;
        objStatusPanel.add(mxLabel, gbc);
        gbc.gridx++;
        objStatusPanel.add(mxValue, gbc);
        gbc.gridx++;
        objStatusPanel.add(myLabel, gbc);
        gbc.gridx++;
        objStatusPanel.add(myValue, gbc);
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        objStatusPanel.add(new JPanel(), gbc);
        statusBar.add(objStatusPanel);

        return statusBar;
    }

    /**
     * Create an initialised JLabel.
     *
     * @param s the text for the label
     * @param dim the preferred dimension
     *
     * @return a JLabel
     */
    protected JLabel createLabel(String s, Dimension dim) {
        JLabel l = new JLabel(s);
        l.setFont(Constants.DEFAULTFONT);

        if (dim != null) {
            l.setPreferredSize(dim);
            l.setMinimumSize(dim);
        }

        return l;
    }

    /**
     * Creates a desktop pane with some internal frames; a frame  holding the
     * editor panel and a frame with a list of library objects.
     *
     * @return a desktop pane
     */
    protected JDesktopPane createDesktopPane() {
        JDesktopPane desktop = new JDesktopPane();
        desktop.setBackground(Constants.DEFAULTBACKGROUNDCOLOR);
        Object rect;
        
        /*
        libraryFrame = new JInternalFrame(ElanLocale.getString(
                    "GraphicsEditor.Library"), true);
        desktop.add(libraryFrame);

        if ((rect = Preferences.get("LibraryIFrameBounds", null)) != null) {
            libraryFrame.setBounds((Rectangle) rect);
        } else {
            libraryFrame.setBounds(350, 0, 180, 350);
        }

        libraryFrame.setVisible(true);
        */
        editFrame = new JInternalFrame("", true);
        desktop.add(editFrame);

        if ((rect = Preferences.get("EditorIFrameBounds", null)) != null) {
            editFrame.setBounds((Rectangle) rect);
        } else {
            editFrame.setBounds(0, 0, 350, 350);
        }

        editorPanel = new EditorPanel(grabFirstFrameImage(),
                annotation.getShape());

        JScrollPane scrollPane = new JScrollPane(editorPanel);
        editFrame.getContentPane().add(scrollPane);
        editFrame.setVisible(true);

        return desktop;
    }

    /**
     * Creates icons from the symbols in the library to display  in a list. The
     * symbols are scaled, preserving the aspect ratio.
     */
    protected void initLibrary() {
        Object o = SVGParserAndStore.getLibrary(transcription);

        if (o instanceof Hashtable) {
            libTable = (Hashtable) o;
        }

        if ((libTable != null) && (libTable.size() > 0)) {
            //StaticRenderer renderer;
            BufferedImage iconImg = null;

            Graphics2D g2d = null;
            iconTable = new Hashtable(libTable.size());

            AffineTransform at;
            Enumeration keys = libTable.keys();
            String id;
            Shape shape;

            //GraphicsNode node;
            ImageIcon icon;

            while (keys.hasMoreElements()) {
                id = (String) keys.nextElement();
                shape = (Shape) libTable.get(id);

                if (shape == null) {
                    continue;
                }

                iconImg = new BufferedImage(iconDimension.width,
                        iconDimension.height, BufferedImage.TYPE_INT_RGB);
                g2d = iconImg.createGraphics();
                g2d.setColor(Color.white);
                g2d.fillRect(0, 0, iconDimension.width, iconDimension.height);

                Rectangle2D bounds = shape.getBounds2D();
                float scaleX = (float) ((iconDimension.width - 6) / bounds.getWidth());
                float scaleY = (float) ((iconDimension.height - 6) / bounds.getHeight());
                float scale = (scaleX >= scaleY) ? scaleY : scaleX;
                float realIconWidth = (float) (scale * bounds.getWidth());
                float realIconHeight = (float) (scale * bounds.getHeight());
                float transX = (float) (-bounds.getX() +
                    ((iconDimension.width - 6 - realIconWidth) / 2f));
                float transY = (float) (-bounds.getY() +
                    ((iconDimension.height - 6 - realIconHeight) / 2f));

                //System.out.println("Icon: w: " + realIconWidth + " h: " + realIconHeight + " tx: " + transX + " ty: " + transY);
                at = AffineTransform.getTranslateInstance(3 + transX, 3 +
                        transY);
                at.scale(scale, scale);
                g2d.setTransform(at);
                g2d.setColor(STROKE_COLOR);
                g2d.draw(shape);

                //shape.paint(g2d, true);
                icon = new ImageIcon(iconImg);

                if (icon != null) {
                    iconTable.put(id, icon);
                }
            }

            iconList = new JList(iconTable.keySet().toArray());
            iconList.setFixedCellHeight(iconDimension.height + 4);
            iconList.setDragEnabled(true);
            iconList.setCellRenderer(new LibraryIconRenderer());
            iconList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // add a mouselistener; doubleclick means adding a reference to the selected symbol
            iconList.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent me) {
                        if (me.getClickCount() > 1) {
                            if (me.getSource() == iconList) {
                                Object o = iconList.getSelectedValue();

                                if (o instanceof String &&
                                        (editorPanel != null)) {
                                    editorPanel.addGraphicAnnotation((String) o);
                                }
                            }
                        }
                    }
                });
        }

        if (iconList != null) {
            JScrollPane iconPane = new JScrollPane(iconList);
            libraryFrame.getContentPane().add(iconPane);
        }
    }

    /**
     * Tries to retrieve the image of the first frame of the active annotation.
     * The ElanMediaPlayer should provide this image.
     *
     * @return a BufferedImage containing the first video frame within this
     *         annotation's boundaries
     */
    protected Image grabFirstFrameImage() {
        ElanMediaPlayer player = ELANCommandFactory.getViewerManager(transcription)
                                                   .getMasterMediaPlayer();
        
        if (player instanceof VideoFrameGrabber) {
        	return ((VideoFrameGrabber) player).getCurrentFrameImage();
        }
        /*
        // have to cast to JMFGraphicMediaPlayer for now
        if (player instanceof JMFGraphicMediaPlayer) {
            JMFGraphicMediaPlayer jmfPlayer = (JMFGraphicMediaPlayer) player;
            msPerFrame = (int) jmfPlayer.getMilliSecondsPerSample();
            currentFrame = annotation.getBeginTimeBoundary() / msPerFrame;

            return jmfPlayer.getFrameImageForTime(annotation.getBeginTimeBoundary());
        }

        if (player instanceof QTMediaPlayer) {
            QTMediaPlayer qtPlayer = (QTMediaPlayer) player;
            msPerFrame = (int) qtPlayer.getMilliSecondsPerSample();
            currentFrame = annotation.getBeginTimeBoundary() / msPerFrame;

            return qtPlayer.getFrameImageForTime(annotation.getBeginTimeBoundary());
        }
        */

        return null;
    }

    /**
     * Tries to retrieve the image corresponding with specified time.
     *
     * @param time the requested media time
     *
     * @return the corresponding video image or null if the operation did not
     *         succeed
     */
    protected Image getFrameForTime(long time) {
        ElanMediaPlayer player = ELANCommandFactory.getViewerManager(transcription)
                                                   .getMasterMediaPlayer();
        
        if (player instanceof VideoFrameGrabber) {
        	return ((VideoFrameGrabber) player).getFrameImageForTime(time);
        }

        // have to cast to JMFGraphicMediaPlayer for now
        /* not necessary anymore
        if (player instanceof JMFGraphicMediaPlayer) {
            JMFGraphicMediaPlayer jmfPlayer = (JMFGraphicMediaPlayer) player;

            return jmfPlayer.getFrameImageForTime(time);
        }

        if (player instanceof QTMediaPlayer) {
            QTMediaPlayer qtPlayer = (QTMediaPlayer) player;

            return qtPlayer.getFrameImageForTime(time);
        }
         */
        return null;
    }

    /**
     * Handles actions of menuitems and buttons.
     *
     * @param ae the action event
     */
    public void actionPerformed(ActionEvent ae) {
        String command = ae.getActionCommand();

        if (command.equals("cancel")) {
            cancelEdit();
        } else if (command.equals("commit")) {
            commitEdit();
        } else if (command.equals("delete")) {
            if (editorPanel != null) {
                editorPanel.setShape(null);
                updateToolBarShapeButtons();
            }
        } else if (command.equals("cut")) {
            cut();
        } else if (command.equals("copy")) {
            copy();
        } else if (command.equals("paste")) {
            paste();
        } else if (command.equals("select")) {
            if (editorPanel != null) {
                editorPanel.setToolMode(EditorPanel.SELECT_TOOL);
            }
        } else if (command.equals("recttool")) {
            if (editorPanel != null) {
                editorPanel.setToolMode(EditorPanel.RECT_TOOL);
            }
        } else if (command.equals("ovaltool")) {
            if (editorPanel != null) {
                editorPanel.setToolMode(EditorPanel.ELLIPSE_TOOL);
            }
        } else if (command.equals("linetool")) {
            if (editorPanel != null) {
                editorPanel.setToolMode(EditorPanel.LINE_TOOL);
            }
        } else if (command.equals("next")) {
            if (editorPanel != null) {
                nextFrame();
            }
        } else if (command.equals("prev")) {
            if (editorPanel != null) {
                previousFrame();
            }
        } else if (command.equals("first")) {
            if (editorPanel != null) {
                firstFrame();
            }
        } else if (command.equals("last")) {
            if (editorPanel != null) {
                lastFrame();
            }
        }
    }

    /**
     * Handle zooming.
     *
     * @param ie the item event
     */
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            String zoomString = (String) ie.getItem();
            int index;

            if ((index = zoomString.indexOf('%')) > 0) {
                zoomString = zoomString.substring(0, index);
            }

            int zoom = 100;

            try {
                zoom = Integer.parseInt(zoomString);
            } catch (NumberFormatException nfe) {
                System.err.println("Error parsing the zoom level");
            }

            if (editorPanel != null) {
                editorPanel.setZoom(zoom);
            }
        }
    }

    /**
     * Check the state of the editor , the loaded frame and the clipboard and
     * update the toolbar buttons.
     */
    public void updateToolBarShapeButtons() {
        if (editorPanel != null) {
            if (editorPanel.getShape() != null) {
                deleteButton.setEnabled(true);
                cutButton.setEnabled(true);
                copyButton.setEnabled(true);
            } else {
                deleteButton.setEnabled(false);
                cutButton.setEnabled(false);
                copyButton.setEnabled(false);
            }

            if (getClipboardContents() instanceof Shape) {
                pasteButton.setEnabled(true);
            } else {
                pasteButton.setEnabled(false);
            }
        }
    }

    /**
     * Update the object's coordinates etc. in the statusbar.
     */
    public void updateObjectStatus() {
        if (editorPanel != null) {
            if (editorPanel.getShape() != null) {
                Shape s = editorPanel.getShape();
                xValue.setText("" + s.getBounds().x);
                yValue.setText("" + s.getBounds().y);
                wValue.setText("" + s.getBounds().width);
                hValue.setText("" + s.getBounds().height);

                double d = Math.sqrt(Math.pow(s.getBounds().width, 2) +
                        Math.pow(s.getBounds().height, 2));
                dValue.setText("" + numbFormat.format(d));
            } else {
                xValue.setText(null);
                yValue.setText(null);
                wValue.setText(null);
                hValue.setText(null);
                dValue.setText(null);
            }
        }
    }

    /**
     * Updates the coordinates of the mouse position in the statusbar.
     *
     * @param xPos the x position of the mouse
     * @param yPos the y position of the mouse
     */
    public void updateMouseStatus(int xPos, int yPos) {
        mxValue.setText("" + xPos);
        myValue.setText("" + yPos);
    }

    /**
     * Enables and disables buttons that handle the activation of frame  images
     * that exist within the boundaries of the current annotation.
     */
    protected void updateFramePositionButtons() {
        if (currentFrame == (annotation.getBeginTimeBoundary() / msPerFrame)) {
            firstFrameButton.setEnabled(false);
            frameBackButton.setEnabled(false);
            frameForwardButton.setEnabled(true);
            lastFrameButton.setEnabled(true);
        } else {
            firstFrameButton.setEnabled(true);
            frameBackButton.setEnabled(true);

            if (currentFrame == ((annotation.getEndTimeBoundary() - 1) / msPerFrame)) {
                frameForwardButton.setEnabled(false);
                lastFrameButton.setEnabled(false);
            } else {
                frameForwardButton.setEnabled(true);
                lastFrameButton.setEnabled(true);
            }
        }
    }

    /**
     * Tries to load the next frame. The next frame's begin time should be
     * within the boundaries of the current annotation.
     */
    private void nextFrame() {
        long next = currentFrame + 1;
        long time = next * msPerFrame;

        if ((time >= annotation.getBeginTimeBoundary()) &&
                (time < annotation.getEndTimeBoundary())) {
            Image nextImage = getFrameForTime(time);
            currentFrame++;

            if (editorPanel != null) {
                editorPanel.setImage(nextImage);
            }
        }

        updateFramePositionButtons();
    }

    /**
     * Tries to load the previous frame. The previous frame's end time should
     * be within the boundaries of the current annotation.
     */
    private void previousFrame() {
        long prev = currentFrame - 1;
        long time = prev * msPerFrame;

        if ((time >= annotation.getBeginTimeBoundary()) &&
                (time < annotation.getEndTimeBoundary())) {
            Image prevImage = getFrameForTime(time);
            currentFrame--;

            if (editorPanel != null) {
                editorPanel.setImage(prevImage);
            }
        }

        updateFramePositionButtons();
    }

    /**
     * Tries to laod the frame that corresponds to the begin time of the
     * current  annotation.
     */
    private void firstFrame() {
        Image firstImage = getFrameForTime(annotation.getBeginTimeBoundary());
        currentFrame = annotation.getBeginTimeBoundary() / msPerFrame;

        if (editorPanel != null) {
            editorPanel.setImage(firstImage);
        }

        updateFramePositionButtons();
    }

    /**
     * Tries to load the frame that corresponds to the end time of the current
     * annotation.
     */
    private void lastFrame() {
        Image lastImage = getFrameForTime(annotation.getEndTimeBoundary());
        currentFrame = (annotation.getEndTimeBoundary() - 1) / msPerFrame;

        if (editorPanel != null) {
            editorPanel.setImage(lastImage);
        }

        updateFramePositionButtons();
    }

    /**
     * Copy the current shape to the clipboard and remove it from  the editor.
     */
    private void cut() {
        if (editorPanel != null) {
            if (canAccessSystemClipboard()) {
                Shape s = editorPanel.getShape();

                if (s != null) {
                    ShapeTransferable st = new ShapeTransferable(s, editorPanel);
                    Clipboard board = getToolkit().getSystemClipboard();
                    board.setContents(st, null);
                    editorPanel.setShape(null);
                    updateToolBarShapeButtons();
                }
            }
        }
    }

    /**
     * Copy the current shape to the clipboard.
     */
    private void copy() {
        if (editorPanel != null) {
            if (canAccessSystemClipboard()) {
                Shape s = editorPanel.getShape();

                if (s != null) {
                    Shape copy = null;

                    if (s instanceof RectangularShape) {
                        copy = (Shape) ((RectangularShape) s).clone();
                    } else if (s instanceof GeneralPath) {
                        copy = (Shape) ((GeneralPath) s).clone();
                    }

                    //add Polygon, Line, Point??
                    if (copy != null) {
                        ShapeTransferable st = new ShapeTransferable(copy,
                                editorPanel);
                        Clipboard board = getToolkit().getSystemClipboard();
                        board.setContents(st, null);
                        updateToolBarShapeButtons();
                    }
                }
            }
        }
    }

    /**
     * If there is a Shape object on the clipboard paste it to the editor.
     */
    private void paste() {
        if (editorPanel != null) {
            Object contents = getClipboardContents();

            if (contents instanceof Shape) {
                editorPanel.setShape((Shape) contents);
            }

            updateToolBarShapeButtons();
        }
    }

    /**
     * Performs a check on the accessibility of the system clipboard.
     *
     * @return true if the system clipboard is accessible, false otherwise
     */
    protected boolean canAccessSystemClipboard() {
        SecurityManager sm = System.getSecurityManager();

        if (sm != null) {
            try {
                sm.checkSystemClipboardAccess();

                return true;
            } catch (SecurityException se) {
                se.printStackTrace();

                return false;
            }
        }

        return true;
    }

    /**
     * Returns the contents of the system clipboard if it is accessible.
     *
     * @return the contents of the system clipboard or null
     */
    private Object getClipboardContents() {
        if (canAccessSystemClipboard()) {
            Transferable t = getToolkit().getSystemClipboard().getContents(null);

            try {
                DataFlavor df = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                        ";class=" + Shape.class.getName());

                return t.getTransferData(df);
            } catch (Exception e) {
                //System.out.println("Could not get contents from the clipboard.");
                //e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Cancel the edit operation.
     */
    protected void cancelEdit() {
        savePreferences();
        setVisible(false);
        dispose();
    }

    /**
     * Commit the changes made to the graphic annotation.
     */
    protected void commitEdit() {
        Shape shape = editorPanel.getShape();
        Command c = ELANCommandFactory.createCommand(transcription,
                ELANCommandFactory.MODIFY_GRAPHIC_ANNOTATION);
        c.execute(annotation, new Object[] { shape });
        savePreferences();
        setVisible(false);
        dispose();
    }

    /**
     * Load preferences.
     */
    protected void loadPreferences() {
        dialogBounds = (Rectangle) Preferences.get("GraphicsEditorBounds", null);

        if (dialogBounds == null) {
            Window owner = this.getOwner();

            if (owner != null) {
                //center relative to parent
                Dimension dim = owner.getSize();
                Point loc = owner.getLocation();
                dialogBounds = new Rectangle(loc.x + (dim.width / 4),
                        loc.y + (dim.height / 4), dim.width / 2, dim.height / 2);
            } else {
                dialogBounds = new Rectangle(0, 0, 600, 500);
            }
        }
    }

    /**
     * Save preferences.
     */
    protected void savePreferences() {
        Preferences.set("GraphicsEditorBounds", getBounds(), null);

        if (libraryFrame != null) {
            Preferences.set("LibraryIFrameBounds", libraryFrame.getBounds(),
                null);
        }

        if (editFrame != null) {
            Preferences.set("EditorIFrameBounds", editFrame.getBounds(), null);
        }
    }

    //#####################################################################//

    /**
     * A ListCellRenderer that uses a JLabel with an icon to render the cell.
     * It gets  the icon from a Hashtable using the value String as a key.
     *
     * @author Han Sloetjes
     */
    protected class LibraryIconRenderer extends DefaultListCellRenderer {
        /**
         * Creates a new LibraryIconRenderer instance
         */
        LibraryIconRenderer() {
            super();
            setIconTextGap(8);
        }

        /**
         * Returns a JLabel based list-cell renderer component.
         *
         * @param list the list this component is part of
         * @param value the value
         * @param index the index in the list
         * @param isSelected the selected state of this index
         * @param cellHasFocus the focused state of this index
         *
         * @return a list-cell renderer component
         */
        public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);

            //setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));
            if (value instanceof String) {
                ImageIcon icon = (ImageIcon) iconTable.get(value);

                if (icon != null) {
                    setIcon(icon);
                }
            }

            return this;
        }
    }

    //end list renderer
    //#######################################################################//

    /**
     * The panel that actual handles the edting of the graphical annotations.
     * Because it needs access to a lot of stuff that is administered by the
     * SVGEditor, this is implemented as an internal class.
     *
     * @author Han Sloetjes
     */
    protected class EditorPanel extends JPanel implements MouseListener,
        MouseMotionListener {
        // tool constants

        /** the select and drag and resize tool */
        public final static int SELECT_TOOL = 0;

        /** the Rectangle tool */
        public final static int RECT_TOOL = 1;

        /** the Ellipse tool */
        public final static int ELLIPSE_TOOL = 2;

        /** the Line tool */
        public final static int LINE_TOOL = 3;

        /** the select and drag tool */
        public final static int MOVE_TOOL = 10;

        /** the drag point tool */
        public final static int MOVE_POINT_TOOL = 11;

        /** the north west resize tool */
        public final static int RESIZE_NW_TOOL = 20;

        /** the north resize tool */
        public final static int RESIZE_N_TOOL = 21;

        /** the north east resize tool */
        public final static int RESIZE_NE_TOOL = 22;

        /** the east resize tool */
        public final static int RESIZE_E_TOOL = 23;

        /** the south east resize tool */
        public final static int RESIZE_SE_TOOL = 24;

        /** the south resize tool */
        public final static int RESIZE_S_TOOL = 25;

        /** the south west resize tool */
        public final static int RESIZE_SW_TOOL = 26;

        /** the west resize tool */
        public final static int RESIZE_W_TOOL = 27;

        /** the clean video frame image */
        protected Image image;

        /**
         * the image including graphical objects from other annotations than
         * the  current active annotation
         */
        protected BufferedImage compoundImg;

        /** a graphics object of the compoundImg */
        protected Graphics2D comG2d;

        /**
         * The GraphicsNodes don't seem to paint correctly when using
         * transparency on  a Graphics2D object, so use a seperate
         * BufferedImage for the annotations
         */

        //protected BufferedImage svgImg;
        //protected Graphics2D svgG2d;
        //private final Color clearColor = new Color(255, 255, 255, 0);
        protected final AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                0.5f);

        /** Holds value of property DOCUMENT ME! */
        private final BasicStroke selectionStroke = new BasicStroke(1.0f,
                BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                new float[] { 2, 3 }, 1.0f);
        private int zoom = 100;
        private float zoomFactor = 1.0f;
        private AffineTransform trans = new AffineTransform();

        /** the graphical shape that is referenced by the active annotation */
        protected Shape currentShape;

        /** a clone from the currentShape for editing purposes */
        protected Shape copyShape;

        /** the bounding box of the current shape */
        protected Rectangle2D bBox;

        /** the active area for a line object, for dragging */
        protected Polygon lineBox;

        /** the corner markers of the selected shape */
        protected Rectangle2D[] corners;

        /** Holds value of property DOCUMENT ME! */
        private final int MARKER_SIZE = 6;

        /** Holds value of property DOCUMENT ME! */
        private final int MIN_RESIZE_SIZE = 8;

        /** the bounds of the editable space, the size of a video frame */
        private Rectangle editRect;
        private Point editPoint;
        private boolean dragging = false;
        private Point dragStart;
        private Rectangle2D resizeStartRect;
        private int toolMode = SELECT_TOOL;

        /** the select tool can have different submodes, move and resize */
        private int toolSubMode = MOVE_TOOL;

        /**
         * Creates a new EditorPanel instance
         */
        public EditorPanel() {
            super();
            setBackground(Color.white);
            addMouseListener(this);
            addMouseMotionListener(this);
            setTransferHandler(new IDTransferHandler());
            editRect = new Rectangle();
            corners = new Rectangle2D[] {
                    new Rectangle(0, 0, MARKER_SIZE, MARKER_SIZE),
                    new Rectangle(0, 0, MARKER_SIZE, MARKER_SIZE),
                    new Rectangle(0, 0, MARKER_SIZE, MARKER_SIZE),
                    new Rectangle(0, 0, MARKER_SIZE, MARKER_SIZE)
                };
        }

        /**
         * Creates a new EditorPanel instance
         *
         * @param image an image from the video
         * @param shape the current 2d annotation
         */
        public EditorPanel(Image image, Shape shape) {
            this();
            setImage(image);
            currentShape = shape;
            setShape(shape);
        }

        /**
         * Set the image that is displayed underneath the 2d annotations.
         *
         * @param image the background image, a frame image from the media
         */
        public void setImage(Image image) {
            this.image = image;

            if (image != null) {
                setPreferredSize(new Dimension(
                        (int) (image.getWidth(null) * zoomFactor),
                        (int) (image.getHeight(null) * zoomFactor)));
                editRect.setRect(0, 0, getPreferredSize().width,
                    getPreferredSize().height);
            }

            paintBuffer();
        }

        /**
         * Paint the background image and 2d annotations other than the current
         * active annotation that occur on the current frame in the media.
         */
        protected void paintBuffer() {
            if ((compoundImg == null) ||
                    (compoundImg.getWidth() != getPreferredSize().width)) {
                compoundImg = new BufferedImage(getPreferredSize().width,
                        getPreferredSize().height, BufferedImage.TYPE_INT_RGB);
                comG2d = compoundImg.createGraphics();
                comG2d.scale(zoomFactor, zoomFactor);
            }

            if (image != null) {
                comG2d.drawImage(image, 0, 0, this);
            }

            comG2d.setComposite(alpha);
            comG2d.setColor(STROKE_COLOR);

            // draw other annotations using transparency
            long currentFrameTime = SVGEditor.this.currentFrame * SVGEditor.this.msPerFrame;

            //long time = annotation.getBeginTimeBoundary();
            Iterator tierIt = transcription.getTiers().iterator();

            while (tierIt.hasNext()) {
                TierImpl tier = (TierImpl) tierIt.next();

                if (tier == annotation.getTier()) {
                    continue;
                }

                if ((tier.getLinguisticType() != null) &&
                        tier.getLinguisticType().hasGraphicReferences()) {
                    Vector anns = tier.getAnnotations();
                    Iterator annIter = anns.iterator();

                    while (annIter.hasNext()) {
                        Annotation a = (Annotation) annIter.next();

                        if (currentFrameTime > -1) {
                            if ((a.getBeginTimeBoundary() <= currentFrameTime) &&
                                    (a.getEndTimeBoundary() > currentFrameTime)) {
                                if (((SVGAlignableAnnotation) a).getShape() != null) {
                                    comG2d.draw(((SVGAlignableAnnotation) a).getShape());

                                    break;
                                }
                            }
                        } else {
                            if ((a.getBeginTimeBoundary() <= annotation.getBeginTimeBoundary()) &&
                                    (a.getEndTimeBoundary() > annotation.getBeginTimeBoundary())) {
                                if (((SVGAlignableAnnotation) a).getShape() != null) {
                                    comG2d.draw(((SVGAlignableAnnotation) a).getShape());

                                    break;
                                }
                            }
                        }
                    }
                }
            }

            comG2d.setComposite(AlphaComposite.Src);
            repaint();
        }

        /**
         * Returns the current image.
         *
         * @return the current image
         */
        public Image getImage() {
            return image;
        }

        /**
         * Overrides the <code>JComponent</code> method by painting the
         * abckground image  and the current 2d shape that is being edited.
         *
         * @param g the object to render to
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;

            if (compoundImg != null) {
                g2d.drawImage(compoundImg, 0, 0, this);
            }

            if (copyShape != null) {
                g2d.setColor(STROKE_COLOR);
                g2d.scale(zoomFactor, zoomFactor);

                //copyShape.paint(g2d, true);
                g2d.draw(copyShape);

                if (bBox != null) {
                    g2d.setColor(Constants.ACTIVEANNOTATIONCOLOR);
                    g2d.setStroke(createScaledStroke());

                    if (copyShape instanceof Line2D) {
                        if (!dragging) {
                            for (int i = 0; i < corners.length; i++) {
                                if (corners[i].contains(
                                            ((Line2D) copyShape).getP1()) ||
                                        corners[i].contains(
                                            ((Line2D) copyShape).getP2())) {
                                    g2d.fill(corners[i]);
                                }
                            }
                        }
                    } else {
                        g2d.draw(bBox);

                        if (!dragging) {
                            for (int i = 0; i < corners.length; i++) {
                                g2d.fill(corners[i]);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Set the <code>Shape</code> that is referenced by the current active
         * annotation.
         *
         * @param shape the referenced shape
         */
        public void setShape(Shape shape) {
            //currentShape = shape;
            if (shape != null) {
                if (shape instanceof RectangularShape) {
                    copyShape = (RectangularShape) ((RectangularShape) shape).clone();
                } else if (shape instanceof GeneralPath) {
                    copyShape = (GeneralPath) ((GeneralPath) shape).clone();
                } else if (shape instanceof Line2D) {
                    copyShape = (Line2D) ((Line2D) shape).clone();
                    adjustActiveLineArea();
                }

                // add Polygon, Point??
                if (copyShape != null) {
                    bBox = copyShape.getBounds2D();
                    adjustAnchors();
                }
            } else {
                copyShape = null;
                bBox = null;
            }

            repaint();
            updateObjectStatus();
        }

        /**
         * Returns the edited shape. When the changes are commited the shape is
         * added to the annotation.
         *
         * @return the current shape or null
         */
        public Shape getShape() {
            return copyShape;
        }

        /**
         * Tells the editor pane to create a new Shape from an object in the
         * library. While the editor only accepts a reference to one single
         * symbol this is effectively the same as setGraphicalAnnotation.
         *
         * @param id the id of the symbol
         */
        public void addGraphicAnnotation(String id) {
            if (libTable != null) {
                Shape s = (Shape) libTable.get(id);

                if (s != null) {
                    setShape(s);

                    // center the shape
                    if (copyShape != null) {
                        double w = copyShape.getBounds().getWidth();
                        double h = copyShape.getBounds().getHeight();

                        if (copyShape instanceof RectangularShape) {
                            ((RectangularShape) copyShape).setFrame((image.getWidth(
                                    null) / 2) - (w / 2),
                                (image.getHeight(null) / 2) - (h / 2), w, h);
                            bBox.setRect(copyShape.getBounds2D());
                            adjustAnchors();
                        } else if (copyShape instanceof Line2D) {
                            int wi = image.getWidth(null);
                            int he = image.getHeight(null);
                            ((Line2D) copyShape).setLine((wi / 2) - (wi / 4),
                                (he / 2) - (he / 4), (wi / 2) + (wi / 4),
                                (he / 2) + (he / 4));
                            adjustActiveLineArea();
                        }

                        updateObjectStatus();
                    }
                }
            }
        }

        /**
         * Returns the current zoomlevel.
         *
         * @return the current zoomlevel
         */
        public int getZoom() {
            return zoom;
        }

        /**
         * Sets the current zoomlevel.
         *
         * @param zoom the new zoomlevel
         */
        public void setZoom(int zoom) {
            this.zoom = zoom;
            zoomFactor = zoom / 100f;
            trans.scale(zoomFactor, zoomFactor);
            setPreferredSize(new Dimension(
                    (int) (image.getWidth(null) * zoomFactor),
                    (int) (image.getHeight(null) * zoomFactor)));
            editRect.setRect(0, 0, getPreferredSize().width,
                getPreferredSize().height);
            paintBuffer();
            revalidate();
        }

        /**
         * Sets the tool mode, i.e. the current active tool.
         *
         * @param mode the new active tool mode
         */
        public void setToolMode(int mode) {
            toolMode = mode;
        }

        /**
         * Returns the current tool mode.
         *
         * @return the current tool mode
         */
        public int getToolMode() {
            return toolMode;
        }

        /**
         * Handles mouseClicked events.
         *
         * @param e the mouseClicked event
         */
        public void mouseClicked(MouseEvent e) {
        }

        /**
         * Handles mousePressed events.
         *
         * @param me the mousePressed event
         */
        public void mousePressed(MouseEvent me) {
            Point p = me.getPoint();

            // inverse transform, i.e. adjust for the zoomfactor
            p.x = (int) (p.x / zoomFactor);
            p.y = (int) (p.y / zoomFactor);

            dragStart = p;
            dragging = true;

            switch (toolMode) {
            case SELECT_TOOL:

                if (bBox != null) {
                    switch (toolSubMode) {
                    case MOVE_TOOL:

                        if (copyShape instanceof RectangularShape) {
                            if (!bBox.contains(p)) {
                                dragging = false;
                            }
                        } else if (copyShape instanceof Line2D) {
                            if (!lineBox.contains(p)) {
                                dragging = false;
                            }
                        }

                        break;

                    case MOVE_POINT_TOOL:

                        if (copyShape instanceof Line2D) {
                            Point2D p1 = ((Line2D) copyShape).getP1();
                            Point2D p2 = ((Line2D) copyShape).getP2();

                            for (int i = 0; i < corners.length; i++) {
                                if (corners[i].contains(p) &&
                                        corners[i].contains(p1)) {
                                    editPoint = new Point((int) p1.getX(),
                                            (int) p1.getY());

                                    break;
                                } else if (corners[i].contains(p) &&
                                        corners[i].contains(p2)) {
                                    editPoint = new Point((int) p2.getX(),
                                            (int) p2.getY());

                                    break;
                                }
                            }
                        }

                        break;

                    default:
                        resizeStartRect = (Rectangle2D) bBox.clone();
                    }
                }

                break;

            case RECT_TOOL:

                if (editRect.contains(me.getPoint())) {
                    copyShape = new Rectangle(p.x, p.y, 1, 1);
                    bBox = copyShape.getBounds2D();
                    SVGEditor.this.updateToolBarShapeButtons();
                }

                break;

            case ELLIPSE_TOOL:

                if (editRect.contains(me.getPoint())) {
                    copyShape = new Ellipse2D.Float(p.x, p.y, 1, 1);
                    bBox = copyShape.getBounds2D();
                    SVGEditor.this.updateToolBarShapeButtons();
                }

                break;

            case LINE_TOOL:

                if (editRect.contains(me.getPoint())) {
                    copyShape = new Line2D.Float(p.x, p.y, p.x + 1, p.y + 1);
                    bBox = copyShape.getBounds2D();
                    adjustActiveLineArea();
                    SVGEditor.this.updateToolBarShapeButtons();
                }

                break;

            default:
                // do nothing	
            }

            updateObjectStatus();
            updateMouseStatus(p.x, p.y);
        }

        /**
         * Handles the mouseReleased event.
         *
         * @param me the mouseReleased event
         */
        public void mouseReleased(MouseEvent me) {
            adjustAnchors();
            adjustActiveLineArea();
            dragging = false;
            setCursor(Cursor.getDefaultCursor());
            repaint();
            updateObjectStatus();
        }

        /**
         * Stub.
         *
         * @param e the mouseEntered event
         */
        public void mouseEntered(MouseEvent e) {
        }

        /**
         * Stub.
         *
         * @param e the mouseExited event
         */
        public void mouseExited(MouseEvent e) {
            updateMouseStatus(0, 0);
        }

        /**
         * Handles the mouseDragged event.
         *
         * @param me the mouseDragged event
         */
        public void mouseDragged(MouseEvent me) {
            boolean shiftDown = me.isShiftDown();

            if (dragging) {
                Point p = me.getPoint();

                // inverse transform, i.e. adjust for the zoomfactor
                p.x = (int) (p.x / zoomFactor);
                p.y = (int) (p.y / zoomFactor);

                if (editRect.contains(me.getPoint())) {
                    // constrain mouse events to the image size 
                    switch (toolMode) {
                    case SELECT_TOOL:

                        if (bBox != null) {
                            int xdif = p.x - dragStart.x;
                            int ydif = p.y - dragStart.y;

                            switch (toolSubMode) {
                            case MOVE_TOOL:
                                dragMoveShape(p, xdif, ydif, shiftDown);

                                break;

                            case RESIZE_NW_TOOL:
                                dragResizeNW(xdif, ydif, shiftDown);

                                break;

                            case RESIZE_NE_TOOL:
                                dragResizeNE(xdif, ydif, shiftDown);

                                break;

                            case RESIZE_SE_TOOL:
                                dragResizeSE(xdif, ydif, shiftDown);

                                break;

                            case RESIZE_SW_TOOL:
                                dragResizeSW(xdif, ydif, shiftDown);

                                break;

                            case MOVE_POINT_TOOL:
                                dragLinePoint(p, shiftDown);

                                break;
                            }
                        }

                        break;

                    case RECT_TOOL:

                    // fall through
                    case ELLIPSE_TOOL:
                        dragCreateShape(p, shiftDown);

                        break;

                    case LINE_TOOL:
                        dragCreateLine(p, shiftDown);
                    }
                }

                updateMouseStatus(p.x, p.y);
            }

            updateObjectStatus();
        }

        /**
         * Update the cursor.
         *
         * @param me the mouseMoved event
         */
        public void mouseMoved(MouseEvent me) {
            if (!dragging) {
                Point p = me.getPoint();
                p.x = (int) (p.x / zoomFactor);
                p.y = (int) (p.y / zoomFactor);
                updateMouseStatus(p.x, p.y);

                if (toolMode == SELECT_TOOL) {
                    if (copyShape instanceof RectangularShape) {
                        if (bBox != null) {
                            if (corners[0].contains(p)) {
                                setCursor(Cursor.getPredefinedCursor(
                                        Cursor.NW_RESIZE_CURSOR));
                                toolSubMode = RESIZE_NW_TOOL;

                                return;
                            }

                            if (corners[1].contains(p)) {
                                setCursor(Cursor.getPredefinedCursor(
                                        Cursor.NE_RESIZE_CURSOR));
                                toolSubMode = RESIZE_NE_TOOL;

                                return;
                            }

                            if (corners[2].contains(p)) {
                                setCursor(Cursor.getPredefinedCursor(
                                        Cursor.SE_RESIZE_CURSOR));
                                toolSubMode = RESIZE_SE_TOOL;

                                return;
                            }

                            if (corners[3].contains(p)) {
                                setCursor(Cursor.getPredefinedCursor(
                                        Cursor.SW_RESIZE_CURSOR));
                                toolSubMode = RESIZE_SW_TOOL;

                                return;
                            }

                            if (bBox.contains(p)) {
                                setCursor(Cursor.getPredefinedCursor(
                                        Cursor.MOVE_CURSOR));
                            } else {
                                setCursor(Cursor.getDefaultCursor());
                            }

                            toolSubMode = MOVE_TOOL;

                            return;
                        }
                    } else if (copyShape instanceof Line2D) {
                        // first try the end points
                        Point2D p1 = ((Line2D) copyShape).getP1();
                        Point2D p2 = ((Line2D) copyShape).getP2();

                        for (int i = 0; i < corners.length; i++) {
                            if (corners[i].contains(p) &&
                                    (corners[i].contains(p1) ||
                                    corners[i].contains(p2))) {
                                setCursor(Cursor.getPredefinedCursor(
                                        Cursor.MOVE_CURSOR));
                                toolSubMode = MOVE_POINT_TOOL;

                                return;
                            }
                        }

                        // next poll the active line area
                        if (lineBox.contains(p)) {
                            setCursor(Cursor.getPredefinedCursor(
                                    Cursor.MOVE_CURSOR));
                            toolSubMode = MOVE_TOOL;

                            return;
                        }
                    }

                    setCursor(Cursor.getDefaultCursor());
                    toolSubMode = MOVE_TOOL;
                }
            }
        }

        /**
         * Create a scaled stroke for the current zoom level
         *
         * @return DOCUMENT ME!
         */
        private BasicStroke createScaledStroke() {
            if (zoomFactor == 1.0f) {
                return selectionStroke;
            } else {
                return new BasicStroke(1.0f / zoomFactor,
                    selectionStroke.getEndCap(), selectionStroke.getLineJoin(),
                    selectionStroke.getMiterLimit(),
                    selectionStroke.getDashArray(), 1.0f);
            }
        }

        /**
         * Update the coordinates of the rectangles that mark the bounding
         * box's corner
         */
        private void adjustAnchors() {
            if (bBox != null) {
                corners[0].setRect(bBox.getMinX() - (MARKER_SIZE / 2),
                    bBox.getMinY() - (MARKER_SIZE / 2), MARKER_SIZE, MARKER_SIZE);
                corners[1].setRect(bBox.getMaxX() - (MARKER_SIZE / 2),
                    bBox.getMinY() - (MARKER_SIZE / 2), MARKER_SIZE, MARKER_SIZE);
                corners[2].setRect(bBox.getMaxX() - (MARKER_SIZE / 2),
                    bBox.getMaxY() - (MARKER_SIZE / 2), MARKER_SIZE, MARKER_SIZE);
                corners[3].setRect(bBox.getMinX() - (MARKER_SIZE / 2),
                    bBox.getMaxY() - (MARKER_SIZE / 2), MARKER_SIZE, MARKER_SIZE);
            }
        }

        /**
         * Update the coordinates of the polygon that marks the line's active
         * area
         */
        private void adjustActiveLineArea() {
            if (copyShape instanceof Line2D) {
                Point2D p1 = ((Line2D) copyShape).getP1();
                Point2D p2 = ((Line2D) copyShape).getP2();
                double w = p1.getX() - p2.getX();
                double h = p1.getY() - p2.getY();
                double f = 0.0;

                if ((w == 0) || (h == 0)) {
                    f = 1.0;
                } else {
                    f = (h / w);
                }

                Rectangle2D r1 = new Rectangle2D.Double(p1.getX() -
                        MARKER_SIZE, p1.getY() - MARKER_SIZE, 2 * MARKER_SIZE,
                        2 * MARKER_SIZE);
                Rectangle2D r2 = new Rectangle2D.Double(p2.getX() -
                        MARKER_SIZE, p2.getY() - MARKER_SIZE, 2 * MARKER_SIZE,
                        2 * MARKER_SIZE);

                if (lineBox != null) {
                    lineBox.reset();
                } else {
                    lineBox = new Polygon();
                }

                if (f >= 0) {
                    lineBox.addPoint((int) r1.getMaxX(), (int) r1.getMinY());
                    lineBox.addPoint((int) r2.getMaxX(), (int) r2.getMinY());
                    lineBox.addPoint((int) r2.getMinX(), (int) r2.getMaxY());
                    lineBox.addPoint((int) r1.getMinX(), (int) r1.getMaxY());
                } else {
                    lineBox.addPoint((int) r1.getMinX(), (int) r1.getMinY());
                    lineBox.addPoint((int) r2.getMinX(), (int) r2.getMinY());
                    lineBox.addPoint((int) r2.getMaxX(), (int) r2.getMaxY());
                    lineBox.addPoint((int) r1.getMaxX(), (int) r1.getMaxY());
                }
            }
        }

        /**
         * Create a Rectangle or Ellipse while dragging the mouse.
         *
         * @param p the current mouse position
         * @param constrain <code>true</code> ensures that width and height of
         *        the shape are equal
         */
        private void dragCreateShape(Point p, boolean constrain) {
            int nx;
            int ny;
            int nw;
            int nh;

            if (dragStart.x <= p.x) {
                nx = dragStart.x;
                nw = p.x - nx;
            } else {
                nx = p.x;
                nw = dragStart.x - nx;
            }

            if (dragStart.y <= p.y) {
                ny = dragStart.y;
                nh = p.y - ny;
            } else {
                ny = p.y;
                nh = dragStart.y - ny;
            }

            if (constrain) {
                if (nw >= nh) {
                    nh = nw;
                } else {
                    nw = nh;
                }

                if (dragStart.x > p.x) {
                    nx = dragStart.x - nw;
                }

                if (dragStart.y > p.y) {
                    ny = dragStart.y - nh;
                }
            }

            ((RectangularShape) copyShape).setFrame(nx, ny, nw, nh);
            bBox.setRect(copyShape.getBounds2D());
            repaint();
        }

        /**
         * Create a Line while dragging the mouse.
         *
         * @param p the current mouse position
         * @param constrain <code>true</code> ensures that the line will be
         *        either horizontal or vertical
         */
        private void dragCreateLine(Point p, boolean constrain) {
            if (constrain) {
                int nx;
                int ny;
                int nw;
                int nh;
                nx = p.x;
                ny = p.y;
                nw = dragStart.x - p.x;
                nh = dragStart.y - p.y;

                if (Math.abs(nw) >= Math.abs(nh)) {
                    ny = dragStart.y;
                } else {
                    nx = dragStart.x;
                }

                ((Line2D) copyShape).setLine(dragStart.x, dragStart.y, nx, ny);
            } else {
                ((Line2D) copyShape).setLine(dragStart.x, dragStart.y, p.x, p.y);
            }

            bBox.setRect(copyShape.getBounds2D());
            repaint();
        }

        /**
         * Moves the selected shape while dragging the mouse
         *
         * @param p the current mouse position
         * @param xdif the distance between the current mouse x position and
         *        the previous mouse x position
         * @param xdif the distance between the current mouse y position and
         *        the previous mouse y position
         * @param constrain <code>true</code> ensures that the movement will be
         *        horizontal or vertical only
         */
        private void dragMoveShape(Point p, int xdif, int ydif,
            boolean constrain) {
            if (constrain) {
                // only move horizontally or vertically
                if (Math.abs(xdif) >= Math.abs(ydif)) {
                    ydif = 0;
                } else {
                    xdif = 0;
                }
            }

            if (copyShape instanceof RectangularShape) {
                ((RectangularShape) copyShape).setFrame(copyShape.getBounds2D()
                                                                 .getX() +
                    xdif, copyShape.getBounds2D().getY() + ydif,
                    copyShape.getBounds2D().getWidth(),
                    copyShape.getBounds().getHeight());
            } else if (copyShape instanceof Line2D) {
                Point2D p1 = ((Line2D) copyShape).getP1();
                Point2D p2 = ((Line2D) copyShape).getP2();
                ((Line2D) copyShape).setLine(p1.getX() + xdif,
                    p1.getY() + ydif, p2.getX() + xdif, p2.getY() + ydif);
                lineBox.translate(xdif, ydif);
            }

            bBox.setRect(copyShape.getBounds());
            dragStart = p;
            repaint();
        }

        /**
         * Adjusts the size of a rectangular shape by changing its upper-left
         * corner
         *
         * @param xdif the distance between the current mouse x position and
         *        the mouse x position at the start of the drag operation
         * @param ydif the distance between the current mouse y position and
         *        the mouse y position at the start of the drag operation
         * @param constrain <code>true</code> ensures that the aspect ratio of
         *        the shape is preserved
         */
        private void dragResizeNW(int xdif, int ydif, boolean constrain) {
            double xPoint = resizeStartRect.getMinX();
            double yPoint = resizeStartRect.getMinY();
            double width = resizeStartRect.getWidth();
            double height = resizeStartRect.getHeight();

            if (constrain) {
                // preserve aspect ratio, use xdif as the base value
                double ar = width / height;
                ydif = (int) (xdif / ar);
            }

            if (copyShape instanceof RectangularShape) {
                if ((width - xdif) > MIN_RESIZE_SIZE) {
                    xPoint += xdif;
                    width -= xdif;
                } else {
                    xPoint = copyShape.getBounds().getMinX();
                    width = copyShape.getBounds().getWidth();
                }

                if ((height - ydif) > MIN_RESIZE_SIZE) {
                    yPoint += ydif;
                    height -= ydif;
                } else {
                    yPoint = copyShape.getBounds().getMinY();
                    height = copyShape.getBounds().getHeight();
                }

                ((RectangularShape) copyShape).setFrame(xPoint, yPoint, width,
                    height);

                //dragStart = p;
                bBox.setRect(copyShape.getBounds());
            }

            repaint();
        }

        /**
         * Adjusts the size of a rectangular shape by changing its upper-right
         * corner
         *
         * @param xdif the distance between the current mouse x position and
         *        the mouse x position at the start of the drag operation
         * @param ydif the distance between the current mouse y position and
         *        the mouse y position at the start of the drag operation
         * @param constrain <code>true</code> ensures that the aspect ratio of
         *        the shape is preserved
         */
        private void dragResizeNE(int xdif, int ydif, boolean constrain) {
            double xPoint = resizeStartRect.getMinX();
            double yPoint = resizeStartRect.getMinY();
            double width = resizeStartRect.getWidth();
            double height = resizeStartRect.getHeight();

            if (constrain) {
                // preserve aspect ratio, use xdif as the base value
                double ar = width / height;
                ydif = (int) (-xdif / ar);
            }

            if (copyShape instanceof RectangularShape) {
                if ((width + xdif) > MIN_RESIZE_SIZE) {
                    width += xdif;
                } else {
                    width = copyShape.getBounds().getWidth();
                }

                if ((height - ydif) > MIN_RESIZE_SIZE) {
                    yPoint += ydif;
                    height -= ydif;
                } else {
                    yPoint = copyShape.getBounds().getMinY();
                    height = copyShape.getBounds().getHeight();
                }

                ((RectangularShape) copyShape).setFrame(xPoint, yPoint, width,
                    height);

                //dragStart = p;
                bBox.setRect(copyShape.getBounds());
            }

            repaint();
        }

        /**
         * Adjusts the size of a rectangular shape by changing its lower-right
         * corner
         *
         * @param xdif the distance between the current mouse x position and
         *        the mouse x position at the start of the drag operation
         * @param ydif the distance between the current mouse y position and
         *        the mouse y position at the start of the drag operation
         * @param constrain <code>true</code> ensures that the aspect ratio of
         *        the shape is preserved
         */
        private void dragResizeSE(int xdif, int ydif, boolean constrain) {
            double xPoint = resizeStartRect.getMinX();
            double yPoint = resizeStartRect.getMinY();
            double width = resizeStartRect.getWidth();
            double height = resizeStartRect.getHeight();

            if (constrain) {
                // preserve aspect ratio, use xdif as the base value
                double ar = width / height;
                ydif = (int) (xdif / ar);
            }

            if (copyShape instanceof RectangularShape) {
                if ((width + xdif) > MIN_RESIZE_SIZE) {
                    width += xdif;
                } else {
                    width = copyShape.getBounds().getWidth();
                }

                if ((height + ydif) > MIN_RESIZE_SIZE) {
                    height += ydif;
                } else {
                    height = copyShape.getBounds().getHeight();
                }

                ((RectangularShape) copyShape).setFrame(xPoint, yPoint, width,
                    height);

                //dragStart = p;
                bBox.setRect(copyShape.getBounds());
            }

            repaint();
        }

        /**
         * Adjusts the size of a rectangular shape by changing its lower-left
         * corner
         *
         * @param xdif the distance between the current mouse x position and
         *        the mouse x position at the start of the drag operation
         * @param ydif the distance between the current mouse y position and
         *        the mouse y position at the start of the drag operation
         * @param constrain <code>true</code> ensures that the aspect ratio of
         *        the shape is preserved
         */
        private void dragResizeSW(int xdif, int ydif, boolean constrain) {
            double xPoint = resizeStartRect.getMinX();
            double yPoint = resizeStartRect.getMinY();
            double width = resizeStartRect.getWidth();
            double height = resizeStartRect.getHeight();

            if (constrain) {
                // preserve aspect ratio, use xdif as the base value		
                double ar = width / height;
                ydif = (int) (-xdif / ar);
            }

            if (copyShape instanceof RectangularShape) {
                if ((width - xdif) > MIN_RESIZE_SIZE) {
                    xPoint += xdif;
                    width -= xdif;
                } else {
                    xPoint = copyShape.getBounds().getMinX();
                    width = copyShape.getBounds().getWidth();
                }

                if ((height + ydif) > MIN_RESIZE_SIZE) {
                    height += ydif;
                } else {
                    height = copyShape.getBounds().getHeight();
                }

                ((RectangularShape) copyShape).setFrame(xPoint, yPoint, width,
                    height);

                //dragStart = p;
                bBox.setRect(copyShape.getBounds());
            }

            repaint();
        }

        /**
         * Adjusts one of the end points of a line.
         *
         * @param p the new position of the dragged point
         * @param constrain <code>true</code> ensures that the line is/stays
         *        horizontal/vertical
         */
        private void dragLinePoint(Point p, boolean constrain) {
            if (copyShape instanceof Line2D && (editPoint != null)) {
                Point2D p1 = ((Line2D) copyShape).getP1();
                Point2D p2 = ((Line2D) copyShape).getP2();
                int p1x = (int) p1.getX();
                int p1y = (int) p1.getY();
                int p2x = (int) p2.getX();
                int p2y = (int) p2.getY();

                if (!constrain) {
                    if ((editPoint.x == p1x) && (editPoint.y == p1y)) {
                        ((Line2D) copyShape).setLine(p.getX(), p.getY(), p2x,
                            p2y);
                    } else if ((editPoint.x == p2x) && (editPoint.y == p2y)) {
                        ((Line2D) copyShape).setLine(p1x, p1y, p.getX(),
                            p.getY());
                    }

                    editPoint.setLocation(p);
                } else {
                    // point p1 is dragged
                    if ((editPoint.x == p1x) && (editPoint.y == p1y)) {
                        if (p1x == p2x) {
                            // vertical, only change p1y
                        } else if (p1y == p2y) {
                        }

                        ((Line2D) copyShape).setLine(p.getX(), p.getY(), p2x,
                            p2y);
                    } else if ((editPoint.x == p2x) && (editPoint.y == p2y)) {
                        ((Line2D) copyShape).setLine(p1x, p1y, p.getX(),
                            p.getY());
                    }

                    if (p1x == p2x) {
                        // vertical line 						
                        if ((editPoint.x == p1x) && (editPoint.y == p1y)) {
                            // point p1 is dragged, only change p1y
                            ((Line2D) copyShape).setLine(p1x, p.y, p2x, p2y);
                            editPoint.setLocation(p1x, p.y);
                        } else if ((editPoint.x == p2x) &&
                                (editPoint.y == p2y)) {
                            // point p2 is dragged, only change p2y
                            ((Line2D) copyShape).setLine(p1x, p1y, p2x, p.y);
                            editPoint.setLocation(p2x, p.y);
                        }
                    } else if (p1y == p2y) {
                        // horizontal line
                        if ((editPoint.x == p1x) && (editPoint.y == p1y)) {
                            // point p1 is dragged, only change p1x
                            ((Line2D) copyShape).setLine(p.x, p1y, p2x, p2y);
                            editPoint.setLocation(p.x, p1y);
                        } else if ((editPoint.x == p2x) &&
                                (editPoint.y == p2y)) {
                            // point p2 is dragged, only change p2x
                            ((Line2D) copyShape).setLine(p1x, p1y, p.x, p2y);
                            editPoint.setLocation(p.x, p2y);
                        }
                    } else {
                        // the line's direction wasn't constrained before
                        if (Math.abs(p1x - p2x) >= Math.abs(p1y - p2y)) {
                            // horizontal
                            if ((editPoint.x == p1x) && (editPoint.y == p1y)) {
                                // point p1 is dragged
                                ((Line2D) copyShape).setLine(p.x, p2y, p2x, p2y);
                                editPoint.setLocation(p.x, p2y);
                            } else if ((editPoint.x == p2x) &&
                                    (editPoint.y == p2y)) {
                                // point p2 is dragged
                                ((Line2D) copyShape).setLine(p1x, p1y, p.x, p1y);
                                editPoint.setLocation(p.x, p1y);
                            }
                        } else {
                            // vertical
                            if ((editPoint.x == p1x) && (editPoint.y == p1y)) {
                                // point p1 is dragged
                                ((Line2D) copyShape).setLine(p2x, p.y, p2x, p2y);
                                editPoint.setLocation(p2x, p.y);
                            } else if ((editPoint.x == p2x) &&
                                    (editPoint.y == p2y)) {
                                // point p2 is dragged
                                ((Line2D) copyShape).setLine(p1x, p1y, p1x, p.y);
                                editPoint.setLocation(p1x, p.y);
                            }
                        }
                    }
                }

                bBox.setRect(copyShape.getBounds());
                adjustActiveLineArea();
                repaint();
            }
        }
    }

    //end EditorPanel
    //####################################################################//	

    /**
     * A simple TransferHandler that enables drag and drop of symbol id's from
     * the library list to the editor panel.
     *
     * @author Han Sloetjes
     */
    protected class IDTransferHandler extends TransferHandler {
        /**
         * Imports data when a compatible Transferable has been dropped  on the
         * editor component.
         *
         * @param c the editor panel component
         * @param t the transferable object
         *
         * @return true when the component is an EditorPanel and the transfer
         *         data is of type String, false otherwise
         */
        public boolean importData(JComponent c, Transferable t) {
            if (canImport(c, t.getTransferDataFlavors())) {
                try {
                    Object o = t.getTransferData(DataFlavor.stringFlavor);

                    if (o instanceof String &&
                            c instanceof SVGEditor.EditorPanel) {
                        ((SVGEditor.EditorPanel) c).addGraphicAnnotation((String) o);

                        return true;
                    }
                } catch (UnsupportedFlavorException ufe) {
                    //System.out.println("The imported data is not supported.");
                } catch (IOException ioe) {
                    //System.out.println("The data could not be imported.");
                }
            }

            return false;
        }

        /**
         * Checks whether or not one of the data flavors can be handled.
         *
         * @param c the component
         * @param flavors the DataFlavors of a Transferable object
         *
         * @return true if an import can succeed, false otherwise
         */
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(DataFlavor.stringFlavor)) {
                    return true;
                }
            }

            return false;
        }
    }

    //end IDTransferHandler

    /**
     * A class that implements Transferable using a java.awt.Shape  as the
     * transferable object.
     *
     * @author Han Sloetjes
     */
    protected class ShapeTransferable implements Transferable {
        /** Holds value of property DOCUMENT ME! */
        JComponent component;

        /** Holds value of property DOCUMENT ME! */
        Shape shape;

        /**
         * Creates a new ShapeTransferable instance
         *
         * @param shape DOCUMENT ME!
         * @param component DOCUMENT ME!
         */
        ShapeTransferable(Shape shape, JComponent component) {
            this.shape = shape;
            this.component = component;
        }

        /**
         * Returns an array containing one <code>DataFlavor</code> object of
         * type <code>DataFlavor.javaJVMLocalObjectMimeType</code>.
         *
         * @return an array of DataFlavor objects
         *
         * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
         */
        public DataFlavor[] getTransferDataFlavors() {
            DataFlavor[] flavors = new DataFlavor[1];
            Class shapeType = shape.getClass();
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                ";class=" + shapeType.getName();

            try {
                flavors[0] = new DataFlavor(mimeType);
            } catch (ClassNotFoundException cnfe) {
                flavors = new DataFlavor[0];
            }

            return flavors;
        }

        /**
         * Returns whether the specified data flavor is supported for this
         * object.
         *
         * @param flavor the requested flavor for the data
         *
         * @return true if this <code>DataFlavor</code> is supported, false
         *         otherwise
         *
         * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
         */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            Class shapeType = shape.getClass();

            if ("application".equals(flavor.getPrimaryType()) &&
                    "x-java-jvm-local-objectref".equals(flavor.getSubType()) &&
                    flavor.getRepresentationClass().isAssignableFrom(shapeType)) {
                return true;
            }

            return false;
        }

        /**
         * Returns the object from this Transferable if it supports the
         * specified DataFlavor.
         *
         * @param flavor the DataFlavor
         *
         * @return the shape contained by this Transferable
         *
         * @throws UnsupportedFlavorException DOCUMENT ME!
         * @throws IOException DOCUMENT ME!
         *
         * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
         */
        public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }

            return shape;
        }
    }

    // end ShapeTransferable
}
