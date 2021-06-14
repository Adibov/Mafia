package Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * handle operations(read/write) on files
 * @author Adibov
 * @version 1.0
 */
public class FileUtils {
    final private static String fileSeparator = System.getProperty("file.separator");

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
        System.out.println(absolutePath);
        File file = new File(absolutePath);
        return file.isDirectory();
    }

    /**
     * file separator getter
     * @return file separator
     */
    public static String getFileSeparator() {
        return fileSeparator;
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
     * write the given object to the file with the given relativePath
     * @param object object file
     * @param relativePath file relativePath
     */
    public static void writeToFile(Object object, String relativePath) {
        String absolutePath = FileUtils.getAbsolutePath(relativePath);
        try (FileOutputStream fileOutputStream = new FileOutputStream(absolutePath)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(object);
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * read a serializable object from the given relativePath
     * @param relativePath given relativePath
     * @return result Object, returns null if cannot read any object
     */
    public static Object readFromFile(String relativePath) {
        Object readObject = null;
        String absolutePath = FileUtils.getAbsolutePath(relativePath);
        try (FileInputStream fileInputStream = new FileInputStream(absolutePath)) {
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            readObject = objectInputStream.readObject();
        }
        catch (IOException | ClassNotFoundException exception) {
            exception.printStackTrace();
        }
        return readObject;
    }
}

