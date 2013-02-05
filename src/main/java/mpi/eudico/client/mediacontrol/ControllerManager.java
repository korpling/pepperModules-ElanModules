package mpi.eudico.client.mediacontrol;

import java.util.Vector;




/**
 * A ControllerManager takes care of informing interested Controllers about
 * media related events
 */
public class ControllerManager extends EventPostingBase {
    private Vector controllers;
    private boolean controllersAreStarted;

    /**
     *
     */
    public ControllerManager() {
        controllers = new Vector();
        controllersAreStarted = false;
    }

    /**
     * Add a Controller that has to be managed
     *
     * @param controller DOCUMENT ME!
     */
    public synchronized void addController(Controller controller) {
        if (!controllers.contains(controller)) {
            controllers.add(controller);
        }
    }

    /**
     * Remove a Controller that no longer has to be managed
     *
     * @param controller DOCUMENT ME!
     */
    public synchronized void removeController(Controller controller) {
        controllers.remove(controller);
    }

    /**
     * Start all managed Controllers
     */
    public void startControllers() {
        if (!controllersAreStarted) {
            for (int i = 0; i < controllers.size(); i++) {
            	/*
            	final Controller ct = ((Controller) controllers.elementAt(i));
            	new Thread(new Runnable(){
            		
            		public void run() {
            			ct.start();
            		}
            	}).start();
            	*/
                ((Controller) controllers.elementAt(i)).start();
            }

            controllersAreStarted = true;
        }
    }

    /**
     * Stop all managed Controllers
     */
    public void stopControllers() {
        if (controllersAreStarted) {
            for (int i = 0; i < controllers.size(); i++) {
                ((Controller) controllers.elementAt(i)).stop();
            }

            controllersAreStarted = false;
        }
    }

    /**
     * Set the stop time for all managed Controllers
     *
     * @param time DOCUMENT ME!
     */
    public void setControllersStopTime(long time) {
        for (int i = 0; i < controllers.size(); i++) {
            ((Controller) controllers.elementAt(i)).setStopTime(time); 
        }
    }
    
    /**
     * Set the media time for all managed Controllers
     *
     * @param time DOCUMENT ME!
     */
    public void setControllersMediaTime(long time) {
        for (int i = 0; i < controllers.size(); i++) {
            ((Controller) controllers.elementAt(i)).setMediaTime(time);
        }
    }

    /**
     * Set the rate for all managed Controllers
     *
     * @param rate DOCUMENT ME!
     */
    public void setControllersRate(float rate) {
        for (int i = 0; i < controllers.size(); i++) {
            ((Controller) controllers.elementAt(i)).setRate(rate);
        }
    }
}
