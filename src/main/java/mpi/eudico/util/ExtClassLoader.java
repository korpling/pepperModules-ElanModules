package mpi.eudico.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;


/**
 * A classloader for loading classes and resources from jars in a specific
 * "extensions" directory.
 *
 * @author Han Sloetjes
 * @version 1.1 include fixes by Martin Schickbichler
 */
public class ExtClassLoader extends URLClassLoader {
    /** the default extensions directory */
    public static final String EXTENSIONS_DIR = System.getProperty("user.dir") +
        File.separator + "extensions";
    private static String extFolder = EXTENSIONS_DIR;
    private static Logger LOG = Logger.getLogger(ExtClassLoader.class.getName());
    private static ExtClassLoader loader;

    //private static URLClassLoader urlLoader;
    // store classes per Jar? or make just one list
    private static HashMap<String, List<Class>> loadedClasses;
    private static HashMap<String, List<URL>> resourceURLS;

    /**
     * Creates a new ExtClassLoader instance
     *
     * @param parent the parent class loader
     */
    private ExtClassLoader(ClassLoader parent) {
        super(new URL[0], parent);

        loadedClasses = new HashMap<String, List<Class>>();
        resourceURLS = new HashMap<String, List<URL>>();

        File plDir = new File(extFolder);

        if (plDir.exists() && plDir.isDirectory()) {
            File[] plfs = plDir.listFiles();

            try {
                for (int i = 0; i < plfs.length; i++) {
                    super.addURL(plfs[i].toURL());

                    //System.out.println("adding URL: "+plfs[i].toURL());
                }
            } catch (MalformedURLException e) {
            }
        }

        loadClasses();
    }

    /**
     * Creates and returns the single instance of this class.
     *
     * @return the single instance of this class
     */
    public static ExtClassLoader getInstance() {
        if (loader == null) {
            loader = new ExtClassLoader(ClassLoader.getSystemClassLoader());
        }

        return loader;
    }

    /**
     * Sets the path to the folder to use to load classes from jar files.
     * Returns silently if the parameter is null or equal to the current path.
     *
     * @param directoryPath the path to the extensions folder
     */
    public static void setExtensionsDirectory(String directoryPath) {
        if ((directoryPath != null) && !directoryPath.equals(extFolder)) {
            File folder = new File(directoryPath);

            if (folder.exists() && folder.isDirectory()) {
                extFolder = directoryPath;
                loader = null; // wait for a call to getInstance to load classes
            } else {
                LOG.warning("The specified folder does not exist: " +
                    directoryPath);
            }
        } else {
            LOG.warning("The folder path is null or equal to current path");
        }
    }

    /**
     * Scans the extensions directory for .jar files and loads all classes
     * found in the jar.  May store url's for all other resources, but for now
     * finding resources is delegated to a URLClassLoader.
     */
    private void loadClasses() {
        try {
            File plDir = new File(extFolder);
            LOG.info("Extensions dir: " + plDir.getAbsolutePath());

            if (plDir.exists() && plDir.isDirectory()) {
                //List jarURLS = new ArrayList(6);
                JarFile jf;
                JarEntry jae;
                String jarUrlPref;

                File[] plfs = plDir.listFiles();

                for (int i = 0; i < plfs.length; i++) {
                    // check if it is a jar??
                    //URL url = new URL("jar:file:/" + plfs[i].getAbsolutePath() + "!/");
                    try {
                        jf = new JarFile(plfs[i].getAbsolutePath());
                        jarUrlPref = "jar:file:/" +
                            plfs[i].getAbsolutePath().replace('\\', '/') +
                            "!/";

                        /*
                           try {
                               jarURLS.add(new URL("jar:file:/" +
                                       plfs[i].getAbsolutePath() + "!/"));
                           } catch (MalformedURLException mue) {
                               LOG.warning(mue.getMessage());
                           }
                         */

                        //System.out.println("JF: " + jf.getName());
                    } catch (IOException ioe) {
                        LOG.warning("Error loading jar: " + ioe.getMessage());

                        continue;
                    } catch (SecurityException se) {
                        LOG.warning("Error loading jar: " + se.getMessage());

                        continue;
                    }

                    Enumeration clEnum = jf.entries();

                    ArrayList<Class> foundClasses = new ArrayList<Class>();
                    ArrayList<URL> foundRes = new ArrayList<URL>();

                    while (clEnum.hasMoreElements()) {
                        jae = (JarEntry) clEnum.nextElement();

                        //System.out.println("JE: " + jae.getName() + " size: " + jae.getSize() + " com size: " + jae.getCompressedSize());
                        if (jae.getName().endsWith(".class") ||
                                jae.getName().endsWith(".CLASS")) {
                            try {
                                InputStream jis = jf.getInputStream(jae);
                                byte[] cbs = new byte[(int) jae.getSize()];
                                jis.read(cbs, 0, cbs.length);

                                try {
                                    /*
                                       Class nextClass = defineClass(jae.getName()
                                                                        .replace('/',
                                                   '.').substring(0,
                                                   jae.getName().lastIndexOf('.')),
                                               cbs, 0, cbs.length);
                                     */
                                    Class nextClass = super.loadClass(jae.getName()
                                                                         .replace('/',
                                                '.')
                                                                         .substring(0,
                                                jae.getName().lastIndexOf('.')));

                                    //System.out.println("Class: " + nextClass);
                                    if (nextClass != null) {
                                        foundClasses.add(nextClass);
                                    }
                                } catch (IndexOutOfBoundsException ioox) {
                                    LOG.warning("Cannot create class: " +
                                        ioox.getMessage());

                                    //eex.printStackTrace(); // any exception
                                } catch (SecurityException se) {
                                    LOG.warning("Cannot create class: " +
                                        se.getMessage());
                                }

                                // a ClassFormatError can be thrown. Should normally not be caught
                            } catch (IOException ioe) {
                                LOG.warning("Cannot read class file from jar: " +
                                    ioe.getMessage());

                                //ioe.printStackTrace();
                            }
                        } else {
                            // store a url
                            try {
                                // jar entry getName returns a path with '/' characters (not '.')
                                foundRes.add(new URL(jarUrlPref +
                                        jae.getName()));
                            } catch (MalformedURLException mue) {
                                LOG.warning("Could not create url for: " +
                                    jae.getName());
                            }
                        }
                    }

                    loadedClasses.put(jf.getName().replace('\\', '/'),
                        foundClasses);
                    resourceURLS.put(jf.getName().replace('\\', '/'), foundRes);
                }

                /*
                   if (jarURLS.size() > 0) {
                       urlLoader = new URLClassLoader((URL[]) jarURLS.toArray(
                                   new URL[] {  }), this);
                   }
                 */
            }
        } catch (Exception ex) {
            LOG.warning("Could not load extension classes: " + ex.getMessage());
        }

        //findResource("a.b.c");
    }

    /**
     * Searches the loaded classes for implementors (or extenders) of the class
     * with the given name. Returns null if the given class cannot be found.
     *
     * @param name the fully qualified name of the class
     *
     * @return an array of Class objects
     */
    public Class[] getImplementingClasses(String name) {
        if (name == null) {
            return null;
        }

        Class superClass = null;

        try {
            superClass = Class.forName(name);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();

            return null;
        }

        return getImplementingClasses(superClass);
    }

    /**
     * Searches the loaded classes for implementors (or extenders) of the class
     * with the given name. Returns null if the given class is null.
     *
     * @param superClass the Class to find implementors/subclasses of
     *
     * @return an array of Class objects of subclasses of the specifiec class
     */
    public Class[] getImplementingClasses(Class superClass) {
        if (superClass == null) {
            return null;
        }

        Class cl;
        List<Class> li;
        List<Class> cList = new ArrayList<Class>();
        Object name;

        Iterator setIt = loadedClasses.keySet().iterator();

        while (setIt.hasNext()) {
            name = setIt.next();
            li = loadedClasses.get(name);

            if (li == null) {
                continue;
            }

            for (int i = 0; i < li.size(); i++) {
                cl = (Class) li.get(i);

                if (superClass.isAssignableFrom(cl)) {
                    cList.add(cl);
                }
            }
        }

        return (Class[]) cList.toArray(new Class[] {  });
    }

    /**
     * Finds the class.
     *
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        if (name == null) {
            throw new ClassNotFoundException("No class found for null");
        }

        return super.findClass(name);

        /*
           Class cl;
           List li;
           Object key;
           Iterator setIt = loadedClasses.keySet().iterator();
           while (setIt.hasNext()) {
               key = setIt.next();
               li = (List) loadedClasses.get(key);
               if (li == null) {
                   continue;
               }
               for (int i = 0; i < li.size(); i++) {
                   cl = (Class) li.get(i);
                   if (cl.getName().equals(name)) {
                       return cl;
                   }
               }
           }
           throw new ClassNotFoundException("No class found for name: " + name);
         */
    }

    /**
     * Finds the stored resource url. If not found returns null; the parent
     * classloader  and bootstrap classloader have  already been tried before
     * this method is called.
     *
     * @see java.lang.ClassLoader#findResource(java.lang.String)
     */
    public URL findResource(String name) {
        if (name == null) {
            return null;
        }

        if (!name.startsWith("/")) {
            name = name.replace('.', '/');
        } else {
            name = name.substring(1);
        }

        String res;
        URL url;
        List li;
        Object key;

        Iterator setIt = resourceURLS.keySet().iterator();

        while (setIt.hasNext()) {
            key = setIt.next();
            li = (List) resourceURLS.get(key);

            if (li == null) {
                continue;
            }

            for (int i = 0; i < li.size(); i++) {
                url = (URL) li.get(i);

                if (url != null) {
                    res = url.toString();

                    int index = res.indexOf("!/");

                    if ((index > -1) && (index < (res.length() - 2))) {
                        res = res.substring(index + 2);

                        if (name.equals(res)) {
                            return url;
                        }
                    }
                }
            }
        }

        return null;

        /*
           if (urlLoader != null) {
               return urlLoader.findResource(name);
           }
         */
    }
}
