import java.util.ArrayList;

// Transfer stations connect multiple lines, so they have other stations too
public class TransferStation extends Station {
    ArrayList<Station> otherStations;  // other stations connected for transfer

    public TransferStation(String line, String name) {
        super(line, name);
        otherStations = new ArrayList<>();
    }

    // Add another station connected before this transfer station
    public void addTransferStationPrev(Station s) {
        otherStations.add(s);
        s.next = this;
    }

    // Add another station connected after this transfer station
    public void addTransferStationNext(Station s) {
        otherStations.add(s);
        s.prev = this;
    }

    // Override tripLengthHelper to check transfer stations too
    @Override
    protected int tripLengthHelper(Station destination, ArrayList<Station> visited) {
        // Check if this is destination
        if (this.equals(destination)) {
            return 0;
        }

        // Check if visited already to avoid loops
        if (visited.contains(this)) {
            return -1;
        }

        visited.add(this);

        // Check next station
        if (next != null) {
            int result = next.tripLengthHelper(destination, new ArrayList<>(visited));
            if (result != -1) {
                return 1 + result;
            }
        }

        // Check previous station
        if (prev != null) {
            int result = prev.tripLengthHelper(destination, new ArrayList<>(visited));
            if (result != -1) {
                return 1 + result;
            }
        }

        // Check all other transfer stations connected to this one
        for (Station s : otherStations) {
            if (!visited.contains(s)) {
                int result = s.tripLengthHelper(destination, new ArrayList<>(visited));
                if (result != -1) {
                    return 1 + result;
                }
            }
        }

        // No path found
        return -1;
    }

    // Override toString to show TRANSFERSTATION and all transfer stations
    @Override
    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        String status = inService ? "true" : "false";

        String s = "TRANSFERSTATION " + name + ": " + line + " line, in service: " + status + ", previous station: " + prevName + ", next station: " + nextName + "\n\tTransfers: \n";

        for (Station t : otherStations) {
            String[] lines = t.line.split("/");
            String lineColor = lines[0];
            String tPrev = (t.prev == null) ? "none" : t.prev.name;
            String tNext = (t.next == null) ? "none" : t.next.name;
            String tStatus = t.inService ? "true" : "false";

            String stationType = "STATION";
            if (t instanceof EndStation) stationType = "ENDSTATION";
            else if (t instanceof TransferStation) stationType = "TRANSFERSTATION";

            s += "\t" + stationType + " " + t.name + ": " + t.line + " line, in service: " + tStatus + ", previous station: " + tPrev + ", next station: " + tNext;
            if (stationType.equals("TRANSFERSTATION")) s += "\n\tTransfers: \n\n";
            else s += "\n";
        }
        return s;
    }
}

