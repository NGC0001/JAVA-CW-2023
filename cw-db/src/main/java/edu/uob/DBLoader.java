package edu.uob;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.Exception;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/** This class reads databases from files. */
public class DBLoader {
    public static void loadFrom(String storageFolderPath) {
        try {
            // Paths.get throws RuntimeException:InvalidPathException
            // Files.list throws IOException, RuntimeException:SecurityException
            List<String> tables = Files.list(Paths.get(storageFolderPath))
                .filter(path -> isTableFileName(path.getFileName().toString()))
                .map(path -> path.toFile()) // throws RuntimeException:UnsupportedOperationException
                .filter(file -> file.isFile()) // throws RuntimeException:SecurityException
                .map(file -> loadTableFile(file))
                .filter(table -> table != null)
                .collect(Collectors.toList());
            tables.forEach(table -> System.out.println(table));
        } catch(IOException ioe) {
            System.err.println("While loading databases from " + storageFolderPath + " : " + ioe);
        } catch(Exception e) {
            System.err.println(e);
        }
    }

    private static boolean isTableFileName(String filename) {
        return true;
    }

    private static String loadTableFile(File file) {
        // FileReader throws IOException
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while((line = bufferedReader.readLine()) != null) { // throws IOException
                System.out.println(file.getPath() + ": " + line);
            }
        } catch (IOException ioe) {
            System.err.println("While reading table from " + file.getPath() + " : " + ioe);
            return null;
        }
        return file.getPath();
    }
}
