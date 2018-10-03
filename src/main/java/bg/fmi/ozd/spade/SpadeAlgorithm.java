
package bg.fmi.ozd.spade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bg.fmi.ozd.spade.generators.PatternsGenerator;
import bg.fmi.ozd.spade.generators.file.PatternGeneratorInFiles;
import bg.fmi.ozd.spade.generators.inmemory.InMemoryPatternGenerator;
import bg.fmi.ozd.spade.generators.inmemory.PartiallyInMemoryPatternGenerator;
import bg.fmi.ozd.spade.input.InputOfSequences;

public class SpadeAlgorithm {

  //Configure this properties
  public static final String WORKING_DIRECTORY = "C:/Projects/spade/sequences/";
  public static final String INPUT_FILE = "C:/Projects/spade/input/test";

  public static final Path INPUT_FILE_PATH = Paths.get(INPUT_FILE);
  public static final Path FREQUENT_ITEMS_DIRECTORY = Paths.get(WORKING_DIRECTORY, "1");

  public static final int MIN_SUPPORT = 300;
  public static final int MAX_LENGTH_OF_PATTERN = 9;
  public static final int START_FROM = 1;

  public static void main(String[] args) throws IOException {

    generateFrequentItems();

    System.out.println("Start: Generating Patterns");
    long timeNow = System.currentTimeMillis();
     PatternsGenerator patternGenerator = new PatternGeneratorInFiles(FREQUENT_ITEMS_DIRECTORY, Paths.get(WORKING_DIRECTORY), START_FROM);
//     PatternsGenerator patternGenerator = new PartiallyInMemoryPatternGenerator(FREQUENT_ITEMS_DIRECTORY, Paths.get(WORKING_DIRECTORY));
//    PatternsGenerator patternGenerator = new InMemoryPatternGenerator(FREQUENT_ITEMS_DIRECTORY);
    patternGenerator.generate();
    long timeForGenerating = System.currentTimeMillis() - timeNow;
    System.out.println("Done: Generating Patterns, time: " + timeForGenerating);

  }

  private static void generateFrequentItems() throws IOException {
    System.out.println("Start: Cleanup previous executions files");
    long startTimeBegining = System.currentTimeMillis();
    Util.deleteFileOrFolder(Paths.get(WORKING_DIRECTORY));
    long timeForCleanup = System.currentTimeMillis() - startTimeBegining;
    System.out.println("Done: Cleanup previous executions files, time: " + timeForCleanup);

    Files.createDirectory(Paths.get(WORKING_DIRECTORY));// Frequent patterns will be stored here
    InputOfSequences inputOfSequences = new InputOfSequences(INPUT_FILE_PATH, " ");

    System.out.println("Start: Saving Frequent Items In Files");
    long timeNow = System.currentTimeMillis();
    long itemsCount = inputOfSequences.saveItemsInFiles(FREQUENT_ITEMS_DIRECTORY);
    long timeForItemsProcessing = System.currentTimeMillis() - timeNow;
    System.out.println("Processed Items (all occurrences, not the unique items): " + itemsCount);
    System.out.println("Done: Saving Frequent Items In Files, time: " + timeForItemsProcessing);

    System.out.println("Start: Pruning of Frequent Items");
    timeNow = System.currentTimeMillis();
    long prunedItemsCount = Util.pruneNonFrequentPatterns(FREQUENT_ITEMS_DIRECTORY, MIN_SUPPORT);
    System.out.println("Pruned items: " + prunedItemsCount);
    long timeForItemsPruning = System.currentTimeMillis() - timeNow;
    System.out.println("Done: Pruning of Frequent Items, time: " + timeForItemsPruning);
  }
}
