
package bg.fmi.ozd.spade.generators.file;

import static bg.fmi.ozd.spade.SpadeAlgorithm.MAX_LENGTH_OF_PATTERN;
import static bg.fmi.ozd.spade.SpadeAlgorithm.MIN_SUPPORT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;

import bg.fmi.ozd.spade.Util;
import bg.fmi.ozd.spade.generators.PatternsGenerator;

/**
 * This class is generating new patterns. The frequent items and the generated patterns are stored in files.
 * 
 * @author Petar
 *
 */
public class PatternGeneratorInFiles implements PatternsGenerator {

  /**
   * The directory where all frequent items are stored.
   */
  private Path frequentItems;

  /**
   * The directory where all the generated patterns will be stored.
   */
  private Path workingDirectory;

  private int startFrom;

  public PatternGeneratorInFiles(Path frequentItems, Path workingDirectory) {
    this.frequentItems = frequentItems;
    this.workingDirectory = workingDirectory;
    startFrom = 1;
  }

  public PatternGeneratorInFiles(Path frequentItems, Path workingDirectory, int startFrom) {
    this.frequentItems = frequentItems;
    this.workingDirectory = workingDirectory;
    this.startFrom = startFrom;
  }

  public void generate() throws IOException {
    for (int i = startFrom; i < MAX_LENGTH_OF_PATTERN; i++) {
      int index = i + 1;
      System.out.println("Start: Generating all patterns with length " + index);
      long timeNow = System.currentTimeMillis();
      Path generateAllPaternsWithGivenLength = generateAllPaternsWithGivenLength(i);
      long timeToGeneratePatterns = System.currentTimeMillis() - timeNow;
      System.out.println("Done: Generating all patterns with length " + index + ", time: " + timeToGeneratePatterns);

      System.out.println("Start: Pruning of all patterns with length " + index);
      timeNow = System.currentTimeMillis();
      System.out.println("Patterns pruned: " + Util.pruneNonFrequentPatterns(generateAllPaternsWithGivenLength, MIN_SUPPORT));
      long timeToPrunePatterns = System.currentTimeMillis() - timeNow;
      System.out.println("Done: Pruning of all patterns with length " + index + ", time: " + timeToPrunePatterns);
    }
  }

  /**
   * Generates all patterns with given length.
   * 
   * @param length
   * @return
   * @throws IOException
   */
  public Path generateAllPaternsWithGivenLength(int length) throws IOException {
    Path generatedPatternsDirectory = Files.createDirectories(Paths.get(workingDirectory.toString(), String.valueOf(length + 1)));
    try (Stream<Path> paths = Files.walk(Paths.get(workingDirectory.toString(), String.valueOf(length)))) {
      paths.skip(1)// The folder name
          .forEach(path -> {
            try (Stream<Path> filenames = Files.walk(frequentItems)) {
              filenames.skip(1)// The folder name
                  .forEach(filename -> {
                    try {
                      Path newSequence = Paths.get(workingDirectory.toString(), String.valueOf(length + 1),
                          path.getFileName().toString() + "!" + filename.getFileName().toString());
                      temporalJoinOfIdLists(path.normalize().toString(), filename.normalize().toString(), newSequence);
                    } catch (Exception e) {
                      e.printStackTrace();
                    }
                  });
            } catch (IOException ex) {
              ex.printStackTrace();
            }
          });
    }
    return generatedPatternsDirectory;

  }

  /**
   * Join two Id-lists.
   * 
   * @param pattern - the file name that contains the id-list of a patterns.
   * @param itemToAddToPattern - the file name that contains the id-list the frequent items.
   * @param result
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void temporalJoinOfIdLists(String pattern, String itemToAddToPattern, Path result) throws FileNotFoundException, IOException {

    try (BufferedReader brSeqN = new BufferedReader(new FileReader(pattern));
        BufferedReader brSeq1 = new BufferedReader(new FileReader(itemToAddToPattern));) {
      String lineSeqN = brSeqN.readLine();
      String lineSeq1 = brSeq1.readLine();

      boolean isFileCreated = false;
      while (lineSeqN != null && lineSeq1 != null && !lineSeqN.isEmpty() && !lineSeq1.isEmpty()) {
        // process the line.
        String[] table1 = lineSeq1.split(" ");
        int sid1 = Integer.parseInt(table1[0]);
        int eid1 = Integer.parseInt(table1[1]);

        String[] tableN = lineSeqN.split(" ");
        int sidN = Integer.parseInt(tableN[0]);
        int eidN = Integer.parseInt(tableN[1]);

        if (sidN > sid1) {
          lineSeq1 = brSeq1.readLine();
        } else if (sidN < sid1) {
          lineSeqN = brSeqN.readLine();
        } else {
          if (eidN < eid1) {
            if (isFileCreated) {
            } else {
              Files.createFile(result);
              isFileCreated = true;
            }
            Files.write(result, new String(sidN + " " + eid1 + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
            lineSeq1 = brSeq1.readLine();
          } else {
            lineSeq1 = brSeq1.readLine();
          }
        }
      }
    }
  }

  /**
   * Join two Id-lists.
   * 
   * @param pattern - the file name that contains the id-list of a patterns.
   * @param itemToAddToPattern - the file name that contains the id-list the frequent items.
   * @param result
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void temporalJoinOfIdListsOpt(String pattern, String itemToAddToPattern, Path result) throws FileNotFoundException, IOException {

    BufferedWriter bufferedWriter = null;
    try (BufferedReader brSeqN = new BufferedReader(new FileReader(pattern));
        BufferedReader brSeq1 = new BufferedReader(new FileReader(itemToAddToPattern));) {
      String lineSeqN = brSeqN.readLine();
      String lineSeq1 = brSeq1.readLine();

      boolean isFileCreated = false;
      while (lineSeqN != null && lineSeq1 != null && !lineSeqN.isEmpty() && !lineSeq1.isEmpty()) {
        // process the line.
        String[] table1 = lineSeq1.split(" ");
        int sid1 = Integer.parseInt(table1[0]);
        int eid1 = Integer.parseInt(table1[1]);

        String[] tableN = lineSeqN.split(" ");
        int sidN = Integer.parseInt(tableN[0]);
        int eidN = Integer.parseInt(tableN[1]);

        if (sidN > sid1) {
          lineSeq1 = brSeq1.readLine();
        } else if (sidN < sid1) {
          lineSeqN = brSeqN.readLine();
        } else {
          if (eidN < eid1) {
            if (!isFileCreated) {
              Files.createFile(result);
              bufferedWriter = new BufferedWriter(new FileWriter(result.toFile()));
              isFileCreated = true;
            }
            bufferedWriter.write(sidN + " " + eid1 + System.lineSeparator());
            lineSeq1 = brSeq1.readLine();
          } else {
            lineSeq1 = brSeq1.readLine();
          }
        }
      }
      if (bufferedWriter != null) {
        bufferedWriter.close();
      }
    }
  }

}
