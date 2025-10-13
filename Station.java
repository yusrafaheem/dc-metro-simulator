public class Station {

    protected String line;
    protected String name;
    protected boolean inService;
    protected Station prev;
    protected Station next;

    public Station(String line, String name) {
        this.line = line;
        this.name = name;
        this.inService = true;
    }

    public void addNext(Station s) {
        this.next = s;
        s.prev = this;
    }

    public void addPrev(Station s) {
        this.prev = s;
        s.next = this;
    }

    public void connect(Station s) {
        this.addNext(s);
    }

    public boolean isAvailable() {
        return inService;
    }

    public void switchAvailable() {
        inService = !inService;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Station)) return false;
        Station other = (Station) o;
        return this.name.equals(other.name) && this.line.equals(other.line);
    }

    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        String status = inService ? "true" : "false";
        return "STATION " + name + ": " + line + " line, in service: " + status + ", previous station: " + prevName + ", next station: " + nextName;
    }

    public int tripLength(Station destination) {
        return tripLengthHelper(destination, new java.util.ArrayList<>());
    }

    protected int tripLengthHelper(Station destination, java.util.ArrayList<Station> visited) {
        if (this.equals(destination)) {
            return 0;
        }

        if (visited.contains(this)) {
            return -1;
        }

        visited.add(this);

        if (next != null) {
            int result = next.tripLengthHelper(destination, new java.util.ArrayList<>(visited));
            if (result != -1) return 1 + result;
        }

        if (prev != null) {
            int result = prev.tripLengthHelper(destination, new java.util.ArrayList<>(visited));
            if (result != -1) return 1 + result;
        }

        return -1;
    }
}
