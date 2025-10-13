
public class EndStation extends Station {

    public EndStation(String line, String name) {
        super(line, name);
    }

    public void makeEnd() {
        if (this.next != null) {
            this.prev = this.next;
        } else if (this.prev != null) {
            this.next = this.prev;
        }
        // If both are null, do nothing
    }

    @Override
    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        return String.format("ENDSTATION %s: %s line, in service: %s, previous station: %s, next station: %s",
            name, line, inService, prevName, nextName);
    }
}
