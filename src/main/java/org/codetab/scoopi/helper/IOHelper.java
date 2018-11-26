package org.codetab.scoopi.helper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.MoreFiles;

public class IOHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(IOHelper.class);

    /**
     * Search for file in classpath if not found then in file system and return
     * the InputStream. This method works even when resource packaged as jar.
     * @param fileName
     * @return InputStream of file
     * @throws FileNotFoundException
     *             if file is not found in classpath or in fs
     */
    public InputStream getInputStream(final String fileName)
            throws FileNotFoundException {
        InputStream stream = IOHelper.class.getResourceAsStream(fileName);
        if (isNull(stream)) {
            stream = ClassLoader.getSystemResourceAsStream(fileName);
            if (stream == null) {
                stream = new FileInputStream(new File(fileName));
            }
        }
        return stream;
    }

    public Reader getReader(final String fileName)
            throws FileNotFoundException {
        return new InputStreamReader(getInputStream(fileName));
    }

    /**
     * Search for file in classpath if not found then in file system and return
     * its URL.
     * @param path
     * @return URL
     * @throws FileNotFoundException
     */
    public URL getURL(final String path) throws FileNotFoundException {
        URL url = IOHelper.class.getResource(path);
        if (isNull(url)) {
            url = ClassLoader.getSystemResource(path);
            if (isNull(url)) {
                Path fsPath = Paths.get(path);
                if (Files.exists(fsPath)) {
                    try {
                        url = fsPath.toUri().toURL();
                    } catch (MalformedURLException e) {
                        // can't test
                        LOGGER.debug("{}", e);
                    }
                }
            }
        }
        if (isNull(url)) {
            throw new FileNotFoundException(path);
        }
        return url;
    }

    /**
     * Search for file in classpath if not found then in file system and return
     * the file.
     * @param fileName
     * @return File
     * @throws FileNotFoundException
     * @throws URISyntaxException
     */
    public File getFile(final String fileName)
            throws FileNotFoundException, URISyntaxException {
        URL url = getURL(fileName);
        return new File(url.toURI());
    }

    /**
     * Get URL of resource
     * @param fileName
     * @return URL of resource or null if not found
     */
    public URL getResourceURL(final String fileName) {
        URL url = IOHelper.class.getResource(fileName);
        if (isNull(url)) {
            url = ClassLoader.getSystemResource(fileName);
        }
        return url;
    }

    /**
     * Get list of files in a dir in classpath or file system. The list is path
     * to the file which may be
     * <ul>
     * <li>resource file in classpath or in jar - /defs/example/task.yml</li>
     * <li>fs file - /tmp/xyz.txt</li>
     * </ul>
     * <p>
     * Use IOHelper.getInputStream() method to transparently create InputStream
     * </p>
     * @param dir
     *            in classpath or fs
     * @param extensions
     *            file extensions to include
     * @return list of file in dir
     * @throws URISyntaxException
     * @throws IOException
     */
    public Collection<String> getFilesInDir(final String dir,
            final String[] extensions) throws URISyntaxException, IOException {
        Path dirPath;
        FileSystem jarFs = null;
        URL dirURL = getResourceURL(dir);
        if (isNull(dirURL)) {
            // fs dir
            dirPath = Paths.get(dir);
        } else {
            // classpath dir
            URI uri = dirURL.toURI();
            if (uri.getScheme().equals("jar")) {
                // classpath dir - in jar
                jarFs = FileSystems.newFileSystem(uri,
                        Collections.<String, Object>emptyMap());
                dirPath = jarFs.getPath(dir);
            } else {
                // classpath dir - normal
                dirPath = Paths.get(uri);
            }
        }

        // traverse dir and add matching (ext) regular files
        Iterable<Path> paths = MoreFiles.fileTraverser().breadthFirst(dirPath);
        Collection<String> files = new ArrayList<String>();
        List<String> suffixes = Arrays.asList(extensions);
        for (Path path : paths) {
            if (Files.isRegularFile(path)) {
                if (suffixes.contains(MoreFiles.getFileExtension(path))) {
                    files.add(path.toAbsolutePath().toString());
                }
            }
        }
        // close jarFs if created
        if (nonNull(jarFs)) {
            jarFs.close();
        }
        return files;
    }

    /**
     * Creates parent directory if not exists and returns print writer.
     * @param fileName
     *            file name
     * @return PrintWriter
     * @throws IOException
     */
    public PrintWriter getPrintWriter(final String fileName)
            throws IOException {
        File file = new File(fileName);
        FileUtils.forceMkdirParent(file);
        return new PrintWriter(file);
    }
}
