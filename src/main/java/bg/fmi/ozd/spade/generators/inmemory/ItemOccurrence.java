
package bg.fmi.ozd.spade.generators.inmemory;

public class ItemOccurrence {

  private long sequenceId;
  private long elementId;

  public ItemOccurrence(long sequenceId, long elementId) {
    this.sequenceId = sequenceId;
    this.elementId = elementId;
  }

  public long getSequenceId() {
    return sequenceId;
  }

  public long getElementId() {
    return elementId;
  }

  @Override
  public String toString() {
    return sequenceId + " " + elementId;
  }

}
