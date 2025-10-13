public class EndStation extends Station {

    public EndStation(String line, String name) {
        super(line, name);
    }

    public void makeEnd() {
        if (prev != null) {
            next = prev;
        } else if (next != null) {
            prev = next;
        }
    }

    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        String status = inService ? "true" : "false";
        return "ENDSTATION " + name + ": " + line + " line, in service: " + status + ", previous station: " + prevName + ", next station: " + nextName;
    }
}
