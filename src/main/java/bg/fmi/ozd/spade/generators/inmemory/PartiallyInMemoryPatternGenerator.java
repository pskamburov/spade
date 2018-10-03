
package bg.fmi.ozd.spade.generators.inmemory;

import static bg.fmi.ozd.spade.SpadeAlgorithm.MAX_LENGTH_OF_PATTERN;
import static bg.fmi.ozd.spade.SpadeAlgorithm.MIN_SUPPORT;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import bg.fmi.ozd.spade.Util;
import bg.fmi.ozd.spade.generators.PatternsGenerator;

/**
 * This class is generating new patterns. The frequent items are stored in-memory ,and the generated patterns are stored in files.
 * 
 * @author Petar
 *
 */
public class PartiallyInMemoryPatternGenerator implements PatternsGenerator {

  private Path frequentItems;
  private Path workingDirectory;

  public PartiallyInMemoryPatternGenerator(Path frequentItems, Path workingDirectory) {
    this.frequentItems = frequentItems;
    this.workingDirectory = workingDirectory;
  }

  public void generate() throws IOException {
    List<IdList> frequentItemsIdLists = loadFrequentItemsInMemory();
    for (int i = 1; i < MAX_LENGTH_OF_PATTERN; i++) {
      System.out.println("Start: Generating all patterns with length " + i);
      long timeNow = System.currentTimeMillis();
      Path generateAllPaternsWithGivenLength = generateAllPaternsWithGivenLengthOptimized(i, frequentItemsIdLists);
      long timeToGeneratePatterns = System.currentTimeMillis() - timeNow;
      System.out.println("Done: Generating all patterns with length " + i + ", time: " + timeToGeneratePatterns);

      System.out.println("Start: Pruning of all patterns with length " + i);
      timeNow = System.currentTimeMillis();
      System.out.println("Patterns pruned: " + Util.pruneNonFrequentPatterns(generateAllPaternsWithGivenLength, MIN_SUPPORT));
      long timeToPrunePatterns = System.currentTimeMillis() - timeNow;
      System.out.println("Done: Pruning of all patterns with length " + i + ", time: " + timeToPrunePatterns);
    }
  }

  /**
   * Load the frequent items from the provided directory into the memory. The map that contains all the patterns now contains object: <1>: < id-lists
   * of all frequent items>.
   * 
   * @return
   * @throws IOException
   */
  private List<IdList> loadFrequentItemsInMemory() throws IOException {
    List<IdList> frequentItemsIdLists = new ArrayList<IdList>();
    try (Stream<Path> paths = Files.walk(frequentItems)) {
      paths.skip(1)// skip folder name
          .forEach(frequentItem -> {
            IdList idList = new IdList(frequentItem.getFileName().toString());
            try (Stream<String> lines = Files.lines(frequentItem, StandardCharsets.UTF_8)) {
              lines.forEach(line -> {
                String[] itemOccurrence = line.split(" ");
                long sid = Integer.parseInt(itemOccurrence[0]);
                long eid = Integer.parseInt(itemOccurrence[1]);
                idList.addOccurrence(new ItemOccurrence(sid, eid));
              });
            } catch (IOException e) {
              e.printStackTrace();
            }
            frequentItemsIdLists.add(idList);
          });
    }
    return frequentItemsIdLists;
  }

  /**
   * Generates all patterns with given length.
   * 
   * @param length
   * @param frequentItemsIdLists
   * @return
   * @throws IOException
   */
  private Path generateAllPaternsWithGivenLengthOptimized(int length, List<IdList> frequentItemsIdLists) throws IOException {
    Path generatedPatternsDirectory = Files.createDirectories(Paths.get(workingDirectory.toString(), String.valueOf(length + 1)));
    try (Stream<Path> paths = Files.walk(Paths.get(workingDirectory.toString(), String.valueOf(length)))) {
      paths.skip(1)// The folder name
          .forEach(path -> {
            for (IdList idList : frequentItemsIdLists) {
              try {
                Path newSequence = Files.createFile(
                    Paths.get(workingDirectory.toString(), String.valueOf(length + 1), path.getFileName().toString() + "!" + idList.getPattern()));
                temporalJoinOfIdListsForInMemory(path.normalize().toString(), idList, newSequence);
              } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          });
    }
    return generatedPatternsDirectory;

  }

  /**
   * Join two Id-lists.
   * 
   * @param pattern - the file name that contains the id-list.
   * @param itemIdList
   * @param result - id-list that is a result of the two id-lists
   * @throws FileNotFoundException
   * @throws IOException
   */
  private static void temporalJoinOfIdListsForInMemory(String pattern, IdList itemIdList, Path result) throws FileNotFoundException, IOException {

    try (BufferedReader brSeqN = new BufferedReader(new FileReader(pattern))) {
      String lineSeqN = brSeqN.readLine();

      int iterator = 0;
      List<ItemOccurrence> itemOccurrences = itemIdList.getOccurrences();

      while (lineSeqN != null && !lineSeqN.isEmpty() && iterator < itemOccurrences.size()) {
        ItemOccurrence itemOccurrence = itemOccurrences.get(iterator);

        int sid1 = (int) itemOccurrence.getSequenceId();
        int eid1 = (int) itemOccurrence.getElementId();

        String[] tableN = lineSeqN.split(" ");
        int sidN = Integer.parseInt(tableN[0]);
        int eidN = Integer.parseInt(tableN[1]);

        if (sidN > sid1) {
          iterator++;
        } else if (sidN < sid1) {
          lineSeqN = brSeqN.readLine();
        } else {
          if (eidN < eid1) {
            Files.write(result, new String(sidN + " " + eid1 + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
          }
          iterator++;
        }
      }
    }
  }

}
