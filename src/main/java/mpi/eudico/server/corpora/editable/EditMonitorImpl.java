package mpi.eudico.server.corpora.editable;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * DOCUMENT ME!
 * $Id: EditMonitorImpl.java 4063 2005-07-26 13:49:37Z hasloe $
 * @author $Author$
 * @version $Revision$
 */
public class EditMonitorImpl implements EditMonitor,
    ActionListener {
    private JFrame messageFrame;
    private JPanel messagePanel;
    private boolean notResponded;
    private boolean inEditMode;

    /**
     * Creates a new EditMonitorImpl instance
     *
     * @param messageFrame DOCUMENT ME!
     */
    public EditMonitorImpl(JFrame messageFrame) {
        this.messageFrame = messageFrame;

        messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(new JLabel(
                "Some usefull message with time to respond information"));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
        JButton b1 = new JButton("I am still editing");
        b1.addActionListener(this);
        buttonPanel.add(b1);

        JButton b2 = new JButton("I stopped editing");
        b2.addActionListener(this);
        buttonPanel.add(b2);

        messagePanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Empty method just to check from a remote object if the RMI connection
     * still exists by calling this method. If the RMI connection is broken
     * the the caller gets a RemoteException.
     */
    public void isCallable() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isInEditMode() {
        Container originalContentPane = messageFrame.getContentPane();
        long questionTime = System.currentTimeMillis();
        notResponded = true;
        inEditMode = false;
        messageFrame.setContentPane(messagePanel);
        messageFrame.setSize(400, 300);
        messageFrame.setVisible(true);

        while (notResponded &&
                ((System.currentTimeMillis() - questionTime) < EditPreferences.MAX_EDIT_MODE_CONFIRMATION_RESPONSE_TIME)) {
            try {
                Thread.currentThread().sleep(500);
            } catch (Exception e) {
            }
        }

        messageFrame.setContentPane(originalContentPane);
        messageFrame.pack();

        return inEditMode;
    }

    /**
     * DOCUMENT ME!
     *
     * @param e DOCUMENT ME!
     */
    public void actionPerformed(ActionEvent e) {
        notResponded = false;

        if (e.getActionCommand().equals("I am still editing")) {
            inEditMode = true;
        } else {
            inEditMode = false;
        }
    }
}
