
package bg.fmi.ozd.spade.generators.inmemory;

import static bg.fmi.ozd.spade.SpadeAlgorithm.MIN_SUPPORT;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import bg.fmi.ozd.spade.generators.PatternsGenerator;

/**
 * This class is generating new patterns. The objects(frequent items and generated patterns) are stored in-memory.
 *
 * @author Petar
 *
 */
public class InMemoryPatternGenerator implements PatternsGenerator {

  private static final Path OUTPUT_FILE = Paths.get("C:\\Projects\\spade\\output.txt");

  /**
   * The directory where all frequent items are stored.
   */
  private final Path frequentItems;

  /**
   * This map holds information about the already generated patterns.(<length of pattern >:<List of all patterns with that length>).
   */
  private final Map<Integer, List<IdList>> generatedPatternsByLength = new HashMap<>();

  /**
   *
   * @param frequentItems - The directory where all frequent items are stored.
   */
  public InMemoryPatternGenerator(Path frequentItems) {
    this.frequentItems = frequentItems;
  }

  @Override
  public void generate() throws IOException {
    final List<IdList> frequentItemsIdLists = loadFrequentItemsInMemory();
    generatedPatternsByLength.put(1, frequentItemsIdLists);

    long newPatterns = 0;
    int i = 1;
    do {
      System.out.println("Start: Generating all patterns with length " + i);
      long timeNow = System.currentTimeMillis();
      newPatterns = generateAllPaternsWithGivenLengthOptimized(i, frequentItemsIdLists);
      final long timeToGeneratePatterns = System.currentTimeMillis() - timeNow;
      System.out.println("Done: Generating all patterns with length " + i + ", time: " + timeToGeneratePatterns);

      System.out.println("Start: Pruning of all patterns with length " + i);
      timeNow = System.currentTimeMillis();
      final long prunedItems = pruneNonFrequentItems(MIN_SUPPORT);
      System.out.println("Patterns pruned: " + prunedItems);
      final long timeToPrunePatterns = System.currentTimeMillis() - timeNow;
      System.out.println("Done: Pruning of all patterns with length " + i + ", time: " + timeToPrunePatterns);
      newPatterns -= prunedItems;
      i++;
    } while (newPatterns > 0);
    generatedPatternsByLength.remove(i); // The last added pattern is empty, so removing it.
    System.out.println("Start: Saving Output in file");
    final long timeNow = System.currentTimeMillis();
    saveOutputToFile();
    final long timeToSaveOutput = System.currentTimeMillis() - timeNow;
    System.out.println("Done: Saving Output in file, time:" + timeToSaveOutput);
  }

  private void saveOutputToFile() {
    int elementCounter = 1;
    try {
      while (true) {
        final List<IdList> patternIdList = generatedPatternsByLength.get(elementCounter);
        if (patternIdList == null) {
          break;
        }
        Files.write(OUTPUT_FILE, new String("Patterns with length: " + elementCounter + System.lineSeparator()).getBytes(),
            StandardOpenOption.APPEND);
        for (final IdList idList : patternIdList) {
          Files.write(OUTPUT_FILE, new String(" -> pattern: " + idList.getPattern() + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
          for (final ItemOccurrence item : idList.getOccurrences()) {
            Files.write(OUTPUT_FILE, new String(" -- " + item + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
          }
        }
        elementCounter++;
      }
    } catch (final IOException e) {
      e.printStackTrace();
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
    final List<IdList> frequentItemsIdLists = new ArrayList<IdList>();
    try (Stream<Path> paths = Files.walk(frequentItems)) {
      paths.skip(1)// skip folder name
          .forEach(frequentItem -> {
            final IdList idList = new IdList(frequentItem.getFileName().toString());
            try (Stream<String> lines = Files.lines(frequentItem, StandardCharsets.UTF_8)) {
              lines.forEach(line -> {
                final String[] itemOccurrence = line.split(" ");
                final long sid = Integer.parseInt(itemOccurrence[0]);
                final long eid = Integer.parseInt(itemOccurrence[1]);
                idList.addOccurrence(new ItemOccurrence(sid, eid));
              });
            } catch (final IOException e) {
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
  private long generateAllPaternsWithGivenLengthOptimized(int length, List<IdList> frequentItemsIdLists) throws IOException {
    long generatedNewPatterns = 0;
    generatedPatternsByLength.putIfAbsent(length + 1, new ArrayList<IdList>());
    final List<IdList> allPatternsWithLength = generatedPatternsByLength.get(length);
    for (final IdList idList : allPatternsWithLength) {
      for (final IdList frequentItemIdList : frequentItemsIdLists) {
        final IdList newPattern = new IdList(idList.getPattern() + "!" + frequentItemIdList.getPattern());
        temporalJoinOfIdListsForInMemory(idList, frequentItemIdList, newPattern);
        generatedPatternsByLength.get(length + 1).add(newPattern);
        generatedNewPatterns++;
      }
    }
    return generatedNewPatterns;
  }

  /**
   * Pruning is a process of removing patterns that have less occurrences than a provided number(minimum support).
   *
   * @param minSupport
   * @return
   */
  private long pruneNonFrequentItems(int minSupport) {
    long counter = 0;
    for (final Entry<Integer, List<IdList>> patternIdListByLentgh : generatedPatternsByLength.entrySet()) {
      final List<IdList> patterns = patternIdListByLentgh.getValue();
      final List<IdList> patternsToRemove = new ArrayList<IdList>();
      for (final IdList patternIdList : patterns) {
        if (patternIdList.getOccurrences().size() < minSupport) {
          patternsToRemove.add(patternIdList);
          counter++;
        }
      }
      patterns.removeAll(patternsToRemove);
    }
    return counter;
  }

  /**
   * Join two Id-lists.
   *
   * @param idList
   * @param itemIdList
   * @param result - id-list that is a result of the two id-lists
   * @throws FileNotFoundException
   * @throws IOException
   */
  private void temporalJoinOfIdListsForInMemory(IdList idList, IdList itemIdList, IdList result) throws FileNotFoundException, IOException {

    int iteratorIdList = 0;
    int iterator = 0;
    final List<ItemOccurrence> itemOccurrences = itemIdList.getOccurrences();
    final List<ItemOccurrence> itemOccurrencesOfPattern = idList.getOccurrences();

    while (iteratorIdList < itemOccurrencesOfPattern.size() && iterator < itemOccurrences.size()) {
      final ItemOccurrence itemOccurrence = itemOccurrences.get(iterator);

      final int sid1 = (int) itemOccurrence.getSequenceId();
      final int eid1 = (int) itemOccurrence.getElementId();

      final ItemOccurrence itemOccurrenceInPattern = itemOccurrencesOfPattern.get(iteratorIdList);
      final int sidN = (int) itemOccurrenceInPattern.getSequenceId();
      final int eidN = (int) itemOccurrenceInPattern.getElementId();

      if (sidN > sid1) {
        iterator++;
      } else if (sidN < sid1) {
        iteratorIdList++;
      } else {
        if (eidN < eid1) {
          result.addOccurrence(new ItemOccurrence(sidN, eid1));
        }
        iterator++;
      }
    }
  }

}
