// Station.java

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

    public void addNext(Station nextStation) {
        this.next = nextStation;
        if (nextStation != null) {
            nextStation.prev = this;
        }
    }

    public void addPrev(Station prevStation) {
        this.prev = prevStation;
        if (prevStation != null) {
            prevStation.next = this;
        }
    }

    public boolean isAvailable() {
        return inService;
    }

    public void switchAvailable() {
        inService = !inService;
    }

    public void connect(Station other) {
        // Connect current station's next to other, and other's prev to current
        this.next = other;
        if (other != null) {
            other.prev = this;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Station)) return false;
        Station other = (Station) obj;
        return this.line.equals(other.line) && this.name.equals(other.name);
    }

    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        return String.format("STATION %s: %s line, in service: %s, previous station: %s, next station: %s",
            name, line, inService, prevName, nextName);
    }

    public int tripLength(Station destination) {
        // Default: use BFS to find shortest path, but test expects small trips only so linear is enough
        // We'll implement BFS to handle transfers later
        if (this.equals(destination)) return 0;

        java.util.Queue<Station> queue = new java.util.LinkedList<>();
        java.util.Set<Station> visited = new java.util.HashSet<>();

        queue.add(this);
        visited.add(this);

        java.util.Map<Station, Integer> distance = new java.util.HashMap<>();
        distance.put(this, 0);

        while (!queue.isEmpty()) {
            Station current = queue.poll();
            int dist = distance.get(current);

            if (current.equals(destination)) {
                return dist;
            }

            if (current.next != null && !visited.contains(current.next)) {
                queue.add(current.next);
                visited.add(current.next);
                distance.put(current.next, dist + 1);
            }
            if (current.prev != null && !visited.contains(current.prev)) {
                queue.add(current.prev);
                visited.add(current.prev);
                distance.put(current.prev, dist + 1);
            }

            // TransferStation subclass will override tripLength for transfers
        }
        return -1; // Not reachable
    }
}
