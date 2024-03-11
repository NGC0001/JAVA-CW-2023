package edu.uob;
import edu.uob.Table.TableException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
            List<Table> tables = Files.list(Paths.get(storageFolderPath))
                .filter(path -> isTableFileName(path.getFileName().toString()))
                .map(path -> path.toFile()) // throws RuntimeException:UnsupportedOperationException
                .filter(file -> file.isFile()) // throws RuntimeException:SecurityException
                .map(file -> loadTableFromFile(file))
                .filter(table -> table != null)
                .collect(Collectors.toList());
            tables.forEach(table -> System.out.println(table));
        } catch(IOException ioe) {
            System.err.println("while loading databases from " + storageFolderPath + ": " + ioe);
        } catch(Exception e) {
            System.err.println(e);
        }
    }

    // TODO
    private static boolean isTableFileName(String filename) {
        return true;
    }

    private static Table loadTableFromFile(File file) {
        Table table = null;
        // FileReader throws IOException
        try (FileReader fileReader = new FileReader(file);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line = null;
            while((line = bufferedReader.readLine()) != null) { // throws IOException
                if ((line = line.trim()).length() == 0) { continue; }
                if (table == null) {
                    table = Table.createFromString(line);
                    continue;
                }
                if (!table.addEntityFromString(line)) {
                    System.err.println("failed to add entity from line: " + line);
                }
            }
        } catch (TableException te) {
            System.err.println("failed to create table from " + file.getPath() + ": " + te);
            return null;
        } catch (IOException ioe) {
            System.err.println("while reading table from " + file.getPath() + ": " + ioe);
            return null;
        }
        return table;
    }
}
