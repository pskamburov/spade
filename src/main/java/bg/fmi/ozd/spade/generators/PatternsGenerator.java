
package bg.fmi.ozd.spade.generators;

import java.io.IOException;

public interface PatternsGenerator {

  /**
   * Generates new patterns.
   * 
   * @throws IOException
   */
  void generate() throws IOException;
}
