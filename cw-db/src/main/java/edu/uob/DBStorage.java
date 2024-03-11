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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** This class reads databases from files. */
public class DBStorage {
    private static final String tableFileNameSuffix = "tab";

    private static boolean isValidTableFileName(String filename) {
      Pattern tableFileNamePattern = Pattern.compile("(.+)\\.(.+)\\." + tableFileNameSuffix);
      Matcher tableFileNameMatcher = tableFileNamePattern.matcher(filename);
      if (!tableFileNameMatcher.matches()) { return false; }
      String dabaseName = tableFileNameMatcher.group(1);
      String tableName = tableFileNameMatcher.group(2);
      if (!dabaseName.equals(dabaseName.toLowerCase())) { return false; } // ensure lowercase
      if (!tableName.equals(tableName.toLowerCase())) { return false; } // ensure lowercase
      return Grammar.isValidDatabaseName(dabaseName) && Grammar.isValidTableName(tableName);
    }

    public static void loadDatabasesFrom(String storageFolderPath) {
        if (storageFolderPath == null) { return; }
        try {
            // Paths.get throws RuntimeException:InvalidPathException
            // Files.list throws IOException, RuntimeException:SecurityException
            List<Table> tables = Files.list(Paths.get(storageFolderPath))
                .filter(path -> isValidTableFileName(path.getFileName().toString()))
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
