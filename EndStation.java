// End stations are special stations that are at the ends of a line
public class EndStation extends Station {

    public EndStation(String line, String name) {
        super(line, name);
    }

    // This method makes the end station "end" properly by connecting
    // previous and next to each other so the line loops around the ends
    public void makeEnd() {
        if (prev != null) {
            next = prev;
        } else if (next != null) {
            prev = next;
        }
    }

    // Override toString to show ENDSTATION instead of just STATION
    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        String status = inService ? "true" : "false";
        return "ENDSTATION " + name + ": " + line + " line, in service: " + status + ", previous station: " + prevName + ", next station: " + nextName;
    }
}

