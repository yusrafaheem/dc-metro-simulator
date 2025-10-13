import java.util.ArrayList;

// This class is for normal metro stations
public class Station {
    String line;        // color of the metro line
    String name;        // station name
    boolean inService;  // true if station is working
    Station prev;       // previous station on the line
    Station next;       // next station on the line

    // Constructor to make a new Station
    public Station(String line, String name) {
        this.line = line;
        this.name = name;
        this.inService = true; // station starts in service
        this.prev = null;
        this.next = null;
    }

    // Add a next station and update that station's prev link too
    public void addNext(Station nextStation) {
        this.next = nextStation;
        nextStation.prev = this;
    }

    // Add a previous station and update that station's next link too
    public void addPrev(Station prevStation) {
        this.prev = prevStation;
        prevStation.next = this;
    }

    // Connect this station and another station both ways
    public void connect(Station other) {
        this.next = other;
        other.prev = this;
    }

    // Switch if the station is available or not
    public void switchAvailable() {
        this.inService = !this.inService;
    }

    // Check if the station is available
    public boolean isAvailable() {
        return inService;
    }

    // To check if two stations are equal (same line and name)
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Station)) return false;
        Station other = (Station) obj;
        return this.line.equals(other.line) && this.name.equals(other.name);
    }

    // A helper method to find the trip length (number of stops) to another station
    public int tripLength(Station destination) {
        ArrayList<Station> visited = new ArrayList<>();
        return tripLengthHelper(destination, visited);
    }

    // Recursive helper method to do the actual trip length calculation
    private int tripLengthHelper(Station destination, ArrayList<Station> visited) {
        // If we reached the destination
        if (this.equals(destination)) {
            return 0;
        }

        // If already visited this station, no need to go again (avoid loops)
        if (visited.contains(this)) {
            return -1;  // means no path found
        }

        // Mark this station as visited
        visited.add(this);

        // Try next station if it exists
        if (next != null) {
            int result = next.tripLengthHelper(destination, new ArrayList<>(visited));
            if (result != -1) {
                return 1 + result;
            }
        }

        // Try previous station if it exists
        if (prev != null) {
            int result = prev.tripLengthHelper(destination, new ArrayList<>(visited));
            if (result != -1) {
                return 1 + result;
            }
        }

        // No path found from here
        return -1;
    }

    // To print the station details like in the test cases
    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        String status = inService ? "true" : "false";
        return "STATION " + name + ": " + line + " line, in service: " + status + ", previous station: " + prevName + ", next station: " + nextName;
    }
}

