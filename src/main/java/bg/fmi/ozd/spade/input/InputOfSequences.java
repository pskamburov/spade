
package bg.fmi.ozd.spade.input;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

/**
 * This class is responsible for the input of the SPADE algorithm. We provide a database(in our case - text file) that contains sequences of items(in
 * a specific format). The class iterates over the provided database and saves the frequent items.
 * 
 * @author Petar
 *
 */
public class InputOfSequences {

  /**
   * The folder that contains the input files that contains sequences of items. The files are in format: A new sequences is on new line, and items in
   * sequence are separated by a given separator.
   */
  private Path sequencesFolder;

  /**
   * The items separator in a sequence.
   */
  private String separatorOfItems;

  /**
   * The directory where all frequent items will be saved.
   */
  private Path frequentItemsDirectory;

  /**
   * Counter for all occurrences of all items(not just the unique items).
   */
  private long counterOccurrencesOfAllItems;

  /**
   * The number of sequences we have processed.
   */
  private int sequenceId;

  /**
   * 
   * @param sequencesFile - The input file that contains the sequences.
   * @param separatorOfItems - The items separator in a sequence.
   */
  public InputOfSequences(Path sequencesFolder, String separatorOfItems) {
    this.sequencesFolder = sequencesFolder;
    this.separatorOfItems = separatorOfItems;
  }

  /**
   * Create an IdList of the provided items, i.e. iterate over the provided input of sequences and creates a file for each item. Then all the
   * occurrences of the item are recorded in the file.
   * 
   * @param pathToSave
   * @return
   */
  public long saveItemsInFiles(Path pathToSave) {
    try {
      frequentItemsDirectory = Files.createDirectory(pathToSave);
    } catch (FileAlreadyExistsException e1) {
      System.err.println("File already exists: " + pathToSave.toString() + ".Please make sure to cleanup previous executions files.");
    } catch (IOException e) {
      e.printStackTrace();// Something went wrong
    }
    try (Stream<Path> paths = Files.walk(sequencesFolder)) {
      paths.skip(1)// The folder name
          .forEach(file -> {
            saveItemsFromGivenFile(file);
          });
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return counterOccurrencesOfAllItems;
  }

  private void saveItemsFromGivenFile(Path file) {
    try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
      lines.forEach(line -> {
        // String line = line.substring(0, line.indexOf("-2")); Used for tests on files with different format.
        line = line.trim();
        int counterItemsInSequence = 0;
        int endIndex = 0;
        while ((endIndex = line.indexOf(separatorOfItems)) > 0) {
          String currentItem = line.substring(0, endIndex);
          line = line.substring(endIndex + separatorOfItems.length());
          try {
            Files.createFile(Paths.get(frequentItemsDirectory.toString(), currentItem));// Create file with the item's id.
          } catch (FileAlreadyExistsException e) {
            // The first occurrence of an item will create the file. So any other occurrences of the same item will just write in this file.
          } catch (IOException e) {
            e.printStackTrace();// Something went wrong
          }
          try {
            // Add the item occurrence in his file.
            Files.write(Paths.get(frequentItemsDirectory.toString(), currentItem),
                new String(sequenceId + " " + (counterItemsInSequence++) + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
          } catch (IOException ex) {
            ex.printStackTrace();// Something went wrong
          }
        }
        sequenceId++;
        counterOccurrencesOfAllItems += counterItemsInSequence + 1;

      });
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Create an IdList of the provided items, i.e. iterate over the provided input of sequences and creates a file for each item. Then all the
   * occurrences of the item are recorded in the file.
   * 
   * @param pathToSave - directory where the files for all items will be stored
   * @return - all processed items(+duplicates)
   */
  public long saveFrequentItemsInFiles(Path pathToSave) {
    try {
      frequentItemsDirectory = Files.createDirectory(pathToSave);
    } catch (FileAlreadyExistsException e1) {
      System.err.println("File already exists: " + pathToSave.toString() + ".Please make sure to cleanup previous executions files.");
    } catch (IOException e) {
      e.printStackTrace();// Something went wrong
    }
    try (Stream<String> lines = Files.lines(sequencesFolder, StandardCharsets.UTF_8)) {
      lines.forEach(line -> {
        // String line = line.substring(0, line.indexOf("-2")); Used for tests on files with different format.
        line = line.trim();
        int counterItemsInSequence = 0;
        int endIndex = 0;
        while ((endIndex = line.indexOf(separatorOfItems)) > 0) {
          String currentItem = line.substring(0, endIndex);
          line = line.substring(endIndex + separatorOfItems.length());
          try {
            Files.createFile(Paths.get(frequentItemsDirectory.toString(), currentItem));// Create file with the item's id.
          } catch (FileAlreadyExistsException e) {
            // The first occurrence of an item will create the file. So any other occurrences of the same item will just write in this file.
          } catch (IOException e) {
            e.printStackTrace();// Something went wrong
          }
          try {
            // Add the item occurrence in his file.
            Files.write(Paths.get(frequentItemsDirectory.toString(), currentItem),
                new String(sequenceId + " " + (counterItemsInSequence++) + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
          } catch (IOException ex) {
            ex.printStackTrace();// Something went wrong
          }
        }
        sequenceId++;
        counterOccurrencesOfAllItems += counterItemsInSequence + 1;

      });
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return counterOccurrencesOfAllItems;
  }

}
