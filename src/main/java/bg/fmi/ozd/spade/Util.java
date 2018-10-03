
package bg.fmi.ozd.spade;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class Util {

  /**
   * Pruning is a process of removing patterns that have less occurrences than a provided number(minimum support).
   * 
   * @param frequentPatternsDirectory - The directory that contains the frequent patterns.
   * @param minSupport - the minimum support.
   * @return
   */
  public static long pruneNonFrequentPatterns(Path frequentPatternsDirectory, long minSupport) {
    long counterPrunedItems = 0;
    try (Stream<Path> filenames = Files.walk(frequentPatternsDirectory)) {
      counterPrunedItems = filenames.skip(1)// The folder name
          .filter(filename -> {
            try {
              if (sizeOfIdList(filename.toFile()) < minSupport)
                return true;
            } catch (Exception e) {
              e.printStackTrace();
            }
            return false;
          }).peek(filename -> {
            try {
              Files.delete(filename);
            } catch (Exception e) {
              e.printStackTrace(); // Something went wrong
            }
          }).count();
    } catch (IOException ex) {
      ex.printStackTrace();// Something went wrong
    }
    return counterPrunedItems;

  }

  private static long sizeOfIdList(File idListFile) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(idListFile));
    long lines = 0;
    while (reader.readLine() != null)
      lines++;
    reader.close();
    return lines;
  
  }

  public static void deleteFileOrFolder(final Path path) throws IOException {
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(final Path file, final IOException e) {
        return handleException(e);
      }

      private FileVisitResult handleException(final IOException e) {
        e.printStackTrace(); // replace with more robust error handling
        return TERMINATE;
      }

      @Override
      public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
        if (e != null)
          return handleException(e);
        Files.delete(dir);
        return CONTINUE;
      }
    });
  };

}
