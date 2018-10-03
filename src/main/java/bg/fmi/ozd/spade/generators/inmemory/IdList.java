
package bg.fmi.ozd.spade.generators.inmemory;

import java.util.ArrayList;
import java.util.List;

public class IdList {

  private String pattern;

  private List<ItemOccurrence> occurrences = new ArrayList<ItemOccurrence>();

  public IdList(String pattern) {
    this.pattern = pattern;
  }

  public void addOccurrence(ItemOccurrence occurrence) {
    occurrences.add(occurrence);
  }

  public List<ItemOccurrence> getOccurrences() {
    return occurrences;
  }

  public String getPattern() {
    return pattern;
  }

  @Override
  public String toString() {
    return "IdList [pattern=" + pattern + ", occurrences=" + occurrences + "]";
  }

}
