import java.util.*;

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
        this.prev = null;
        this.next = null;
    }

    public void addNext(Station s) {
        this.next = s;
        if (s != null) {
            s.prev = this;
        }
    }

    public void addPrev(Station s) {
        this.prev = s;
        if (s != null) {
            s.next = this;
        }
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

    @Override
    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        return "STATION " + name + ": " + line + " line, in service: " + inService + ", previous station: " + prevName + ", next station: " + nextName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (!(other instanceof Station)) return false;
        Station s = (Station) other;
        return this.name.equals(s.name) && this.line.equals(s.line);
    }

    // Extra neighbors beyond next/prev, used by tripLengthHelper below.
    // Plain Station has none; TransferStation overrides this to expose
    // otherStations, so a transfer point's connections that got clobbered
    // by a later line's wiring (see TransferStation.java) are still
    // reachable during pathfinding.
    protected List<Station> getTransferNeighbors() {
        return Collections.emptyList();
    }

    // Recursive tripLength method without helper and no queues
    public int tripLength(Station dest) {
        if (this.equals(dest)) {
            return 0;
        }
        Set<Station> visited = new HashSet<>();
        visited.add(this);
        return tripLengthHelper(dest, visited);
    }

    // Helper used internally but private, no change to original interface
    private int tripLengthHelper(Station dest, Set<Station> visited) {
        if (this.equals(dest)) {
            return 0;
        }

        int minDist = Integer.MAX_VALUE;

        // Check next station
        if (next != null && !visited.contains(next)) {
            visited.add(next);
            int dist = next.tripLengthHelper(dest, visited);
            if (dist >= 0 && dist < minDist) {
                minDist = dist + 1;
            }
            visited.remove(next);
        }

        // Check prev station
        if (prev != null && !visited.contains(prev)) {
            visited.add(prev);
            int dist = prev.tripLengthHelper(dest, visited);
            if (dist >= 0 && dist < minDist) {
                minDist = dist + 1;
            }
            visited.remove(prev);
        }

        // Check any extra transfer neighbors (empty for a plain Station,
        // otherStations for a TransferStation -- fixes the bug where
        // transfer connections overwritten in next/prev were permanently
        // unreachable from the transfer station's own side).
        for (Station other : getTransferNeighbors()) {
            if (other != null && !visited.contains(other)) {
                visited.add(other);
                int dist = other.tripLengthHelper(dest, visited);
                if (dist >= 0 && dist < minDist) {
                    minDist = dist + 1;
                }
                visited.remove(other);
            }
        }

        return (minDist == Integer.MAX_VALUE) ? -1 : minDist;
    }
}
