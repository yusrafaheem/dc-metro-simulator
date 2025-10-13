public class Station {
    protected String line;
    protected String name;
    protected Station prev;
    protected Station next;
    protected boolean inService;

    public Station(String line, String name) {
        this.line = line;
        this.name = name;
        this.prev = null;
        this.next = null;
        this.inService = true;
    }

    public void addNext(Station nextStation) {
        this.next = nextStation;
        if (nextStation != null && nextStation.prev != this) {
            nextStation.prev = this;
        }
    }

    public void addPrev(Station prevStation) {
        this.prev = prevStation;
        if (prevStation != null && prevStation.next != this) {
            prevStation.next = this;
        }
    }

    // Connect this station and the given station together (prev/next links)
    public void connect(Station other) {
        if (this.next == null) {
            this.addNext(other);
        } else if (this.prev == null) {
            this.addPrev(other);
        } else {
            // if both prev and next are taken, try to add to other side of 'other'
            if (other.next == null) {
                other.addNext(this);
            } else if (other.prev == null) {
                other.addPrev(this);
            }
        }
    }

    public boolean isAvailable() {
        return inService;
    }

    public void switchAvailable() {
        inService = !inService;
    }

    // Check equality based on line and name only (ignore case optional)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!(obj instanceof Station)) return false;
        Station other = (Station) obj;
        return this.name.equals(other.name) && this.line.equals(other.line);
    }

    // Calculate trip length (number of stations) between this station and the target station
    // Only counts direct next/prev links on the same line (no transfer handling here)
    public int tripLength(Station target) {
        if (this.equals(target)) return 0;
        int length = 0;

        // Try forward traversal (next)
        Station current = this;
        while (current != null && !current.equals(target)) {
            current = current.next;
            length++;
        }
        if (current != null && current.equals(target)) {
            return length;
        }

        // Try backward traversal (prev)
        length = 0;
        current = this;
        while (current != null && !current.equals(target)) {
            current = current.prev;
            length++;
        }
        if (current != null && current.equals(target)) {
            return length;
        }

        // Not found on this line (trip via transfer stations should be handled elsewhere)
        return -1;
    }

    @Override
    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        String status = inService ? "true" : "false";
        return "STATION " + name + ": " + line + " line, in service: " + status +
               ", previous station: " + prevName + ", next station: " + nextName;
    }
}
