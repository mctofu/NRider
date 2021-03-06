package nrider.interpreter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarFile;

public class CommandInterpreter {
    private final HashMap<String, ICommand> _commandMap = new HashMap<>();

    public CommandInterpreter() {
        // autoload all classes in the nrider.interpreter.command package
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            HashSet<String> packageSet = new HashSet<>();
            packageSet.add("nrider.interpreter.command");
            for (Set<Class<?>> classes : findClasses(classLoader, null, packageSet, null).values()) {
                for (Class<?> c : classes) {
                    ICommand command = (ICommand) c.getDeclaredConstructor().newInstance();
                    _commandMap.put(command.getName(), command);
                }
            }
            // hack for now, could work like normal if it could lookup a reference to the interpreter
            _commandMap.put("help", new Help(this));
        } catch (Exception e) {
            throw new Error("Instantiation error", e);
        }
    }

    protected Collection<ICommand> getCommands() {
        return _commandMap.values();
    }

    public String executeCommand(String command) {
        if (command == null || "".equals(command.trim())) {
            return null;
        }

        StringTokenizer parser = new StringTokenizer(
                command,
                " \t\r\n",
                true
        );

        String delims = " \"\t\r\n";

        String commandName = parser.nextToken();
        ArrayList<String> params = new ArrayList<>();
        boolean inQuote = false;
        while (parser.hasMoreTokens()) {
            String token = parser.nextToken(delims);
            if (!"\"".equals(token)) {
                if (!delims.contains(token)) {
                    params.add(token);
                }
            } else {
                inQuote = !inQuote;
                delims = inQuote ? "\"" : " \"\t\r\n";
            }
        }
        if (inQuote) {
            return "Error parsing command";
        }
        if (_commandMap.containsKey(commandName)) {
            return _commandMap.get(commandName).execute(params.toArray(new String[0]));
        } else {
            return "Unknown command.  Type help for a list of commands";
        }

    }

    // below from Kris Dover <krisdover@hotmail.com>:

    /**
     * Searches the classpath for all classes matching a specified search criteria,
     * returning them in a map keyed with the interfaces they implement or null if they
     * have no interfaces. The search criteria can be specified via interface, package
     * and jar name filter arguments
     * <p>
     *
     * @param classLoader     The classloader whose classpath will be traversed
     * @param interfaceFilter A Set of fully qualified interface names to search for
     *                        or null to return classes implementing all interfaces
     * @param packageFilter   A Set of fully qualified package names to search for or
     *                        or null to return classes in all packages
     * @param jarFilter       A Set of jar file names to search for or null to return
     *                        classes from all jars
     * @return A Map of a Set of Classes keyed to their interface names
     * @throws ClassNotFoundException if the current thread's classloader cannot load
     *                                a requested class for any reason
     */
    public static Map<String, Set<Class<?>>> findClasses(ClassLoader classLoader,
                                                      Set<String> interfaceFilter,
                                                      Set<String> packageFilter,
                                                      Set<String> jarFilter)
            throws ClassNotFoundException {
        Map<String, Set<Class<?>>> classTable = new HashMap<>();
        Object[] classPaths;
        try {
            // get a list of all classpaths
            classPaths = ((java.net.URLClassLoader) classLoader).getURLs();
        } catch (ClassCastException cce) {
            // or cast failed; tokenize the system classpath
            classPaths = System.getProperty("java.class.path", "").split(File.pathSeparator);
        }

        for (Object path : classPaths) {
            Enumeration<?> files = null;
            JarFile module = null;
            // for each classpath ...
            File classPath = new File(path instanceof URL ?
                    URLDecoder.decode(((URL) path).getFile(), StandardCharsets.UTF_8) : path.toString());
            if (classPath.isDirectory() && jarFilter == null) {   // is our classpath a directory and jar filters are not active?
                List<String> dirListing = new ArrayList<>();
                // get a recursive listing of this classpath
                recursivelyListDir(dirListing, classPath, new StringBuffer());
                // an enumeration wrapping our list of files
                files = Collections.enumeration(dirListing);
            } else if (classPath.getName().endsWith(".jar")) {    // is our classpath a jar?
                // skip any jars not list in the filter
                if (jarFilter != null && !jarFilter.contains(classPath.getName())) {
                    continue;
                }
                try {
                    // if our resource is a jar, instantiate a jarfile using the full path to resource
                    module = new JarFile(classPath);
                } catch (MalformedURLException mue) {
                    throw new ClassNotFoundException("Bad classpath. Error: " + mue.getMessage());
                } catch (IOException io) {
                    throw new ClassNotFoundException("jar file '" + classPath.getName() +
                            "' could not be instantiate from file path. Error: " + io.getMessage());
                }
                // get an enumeration of the files in this jar
                files = module.entries();
            }

            // for each file path in our directory or jar
            while (files != null && files.hasMoreElements()) {
                // get each fileName
                String fileName = files.nextElement().toString();
                // we only want the class files
                if (fileName.endsWith(".class")) {
                    // convert our full filename to a fully qualified class name
                    String className = fileName.replaceAll("/", ".").substring(0, fileName.length() - 6);
                    // debug class list
                    //System.out.println(className);
                    // skip any classes in packages not explicitly requested in our package filter
                    if (packageFilter != null && (!className.contains(".") || !packageFilter.contains(className.substring(0, className.lastIndexOf("."))))) {
                        continue;
                    }
                    // get the class for our class name
                    Class<?> theClass;
                    try {
                        theClass = Class.forName(className, false, classLoader);
                    } catch (NoClassDefFoundError e) {
                        System.out.println("Skipping class '" + className + "' for reason " + e.getMessage());
                        continue;
                    }
                    // skip interfaces
                    if (theClass.isInterface()) {
                        continue;
                    }
                    //then get an array of all the interfaces in our class
                    Class<?>[] classInterfaces = theClass.getInterfaces();

                    // for each interface in this class, add both class and interface into the map
                    String interfaceName = null;
                    for (int i = 0; i < classInterfaces.length || (i == 0 && interfaceFilter == null); i++) {
                        if (i < classInterfaces.length) {
                            interfaceName = classInterfaces[i].getName();
                            // was this interface requested?
                            if (interfaceFilter != null && !interfaceFilter.contains(interfaceName)) {
                                continue;
                            }
                        }
                        // is this interface already in the map?
                        if (classTable.containsKey(interfaceName)) {
                            // if so then just add this class to the end of the list of classes implementing this interface
                            classTable.get(interfaceName).add(theClass);
                        } else {
                            // else create a new list initialised with our first class and put the list into the map
                            Set<Class<?>> allClasses = new HashSet<>();
                            allClasses.add(theClass);
                            classTable.put(interfaceName, allClasses);
                        }
                    }

                }
            }

            // close the jar if it was used
            if (module != null) {
                try {
                    module.close();
                } catch (IOException ioe) {
                    throw new ClassNotFoundException("The module jar file '" + classPath.getName() +
                            "' could not be closed. Error: " + ioe.getMessage());
                }
            }

        } // end for loop

        return classTable;
    } // end method

    /**
     * Recursively lists a directory while generating relative paths. This is a helper function for findClasses.
     * Note: Uses a StringBuffer to avoid the excessive overhead of multiple String concatentation
     *
     * @param dirListing   A list variable for storing the directory listing as a list of Strings
     * @param dir          A File for the directory to be listed
     * @param relativePath A StringBuffer used for building the relative paths
     */
    private static void recursivelyListDir(List<String> dirListing, File dir, StringBuffer relativePath) {
        int prevLen; // used to undo append operations to the StringBuffer

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }

            for (File file : files) {
                // store our original relative path string length
                prevLen = relativePath.length();
                // call this function recursively with file list from present
                // dir and relative to appended with present dir
                recursivelyListDir(dirListing, file, relativePath.append(prevLen == 0 ? "" : "/").append(file.getName()));
                //  delete subdirectory previously appended to our relative path
                relativePath.delete(prevLen, relativePath.length());
            }
        } else {
            // this dir is a file; append it to the relativeto path and add it to the directory listing
            dirListing.add(relativePath.toString());
        }
    }
}
