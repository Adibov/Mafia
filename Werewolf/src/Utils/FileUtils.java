package Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * handle reading, writing etc on files
 * @author Adibov
 * @version 1.0
 */
public class FileUtils {
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private FileOutputStream fileOutputStream;
    private FileInputStream fileInputStream;

    /**
     * class constructor
     * @param relativePath relativePath of the file that this object has to interact with
     */
    public FileUtils(String relativePath) {
        String absolutePath = FileUtils.getAbsolutePath(relativePath);
        try {
            fileOutputStream = new FileOutputStream(relativePath);
            fileInputStream = new FileInputStream(relativePath);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectInputStream = new ObjectInputStream(fileInputStream);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get absolute path from given relative path
     * @param relativePath given relative path
     * @return absolute path
     */
    public static String getAbsolutePath(String relativePath) {
        return Paths.get(relativePath).toAbsolutePath().toString();
    }

    /**
     * create a folder with the given path if doesn't exist
     * @param relativePath relative path
     */
    public static void createFolder(String relativePath) {
        String absolutePath = FileUtils.getAbsolutePath(relativePath);
        File newFolder = new File(absolutePath);
        if (!newFolder.exists() && !newFolder.mkdirs())
            throw new SecurityException("Cannot make new folder.");
    }

    /**
     * delete file with the given path
     * @param relativePath given path
     */
    public static void deleteFile(String relativePath) {
        Path path = Paths.get(relativePath).toAbsolutePath();
        try {
            Files.delete(path);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * check if the given file exists
     * @param relativePath given relative path
     * @return true if file exists
     */
    public static boolean isFileExists(String relativePath) {
        String absolutePath = FileUtils.getAbsolutePath(relativePath);
        File file = new File(absolutePath);
        return file.isFile();
    }

    /**
     * check if the given directory exists
     * @param relativePath given relative path
     * @return true if directory exists
     */
    public static boolean isFolderExists(String relativePath) {
        String absolutePath = FileUtils.getAbsolutePath(relativePath);
        File file = new File(absolutePath);
        return file.isDirectory();
    }

    /**
     * file separator getter
     * @return file separator
     */
    public static String getFileSeparator() {
        return System.getProperty("file.separator");
    }

    /**
     * create a file with the given name
     * @param relativePath new relativePath
     */
    public static void createFile(String relativePath) {
        String  absolutePath = FileUtils.getAbsolutePath(relativePath);
        File newFile = new File(absolutePath);
        try {
            if (!newFile.exists() && !newFile.createNewFile())
                throw new SecurityException("Cannot make new file.");
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    /**
     * write the given object to the file
     * @param object object file
     */
    public synchronized void writeToFile(Object object) {
        try {
            objectOutputStream.writeObject(object);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * read a serializable object from the file
     * @return read Object, returns null if cannot read any object
     */
    public synchronized Object readFromFile() {
        Object readObject = null;
        if (isStreamEmpty())
            return null;

        try {
            System.out.println("Hey: " + fileInputStream.available());
            readObject = objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return readObject;
    }

    /**
     * check if stream is empty
     * @return true, if stream is empty
     */
    public boolean isStreamEmpty() {
        try {
            return fileInputStream.available() == 0;
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
        return false;
    }
}

