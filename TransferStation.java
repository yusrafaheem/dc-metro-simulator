import java.util.ArrayList;

public class TransferStation extends Station {
    ArrayList<Station> otherStations;

    public TransferStation(String line, String name) {
        super(line, name);
        otherStations = new ArrayList<>();
    }

    public void addTransferStationPrev(Station s) {
        if (!otherStations.contains(s)) {
            otherStations.add(s);
            s.next = this;
        }
    }

    public void addTransferStationNext(Station s) {
        if (!otherStations.contains(s)) {
            otherStations.add(s);
            s.prev = this;
        }
    }

    protected int tripLengthHelper(Station destination, ArrayList<Station> visited) {
        if (this.equals(destination)) return 0;
        if (visited.contains(this)) return -1;

        visited.add(this);

        if (next != null) {
            int result = next.tripLengthHelper(destination, new ArrayList<>(visited));
            if (result != -1) return 1 + result;
        }

        if (prev != null) {
            int result = prev.tripLengthHelper(destination, new ArrayList<>(visited));
            if (result != -1) return 1 + result;
        }

        for (Station s : otherStations) {
            if (!visited.contains(s)) {
                int result = s.tripLengthHelper(destination, new ArrayList<>(visited));
                if (result != -1) return 1 + result;
            }
        }

        return -1;
    }

    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        String status = inService ? "true" : "false";

        String s = "TRANSFERSTATION " + name + ": " + line + " line, in service: " + status + ", previous station: " + prevName + ", next station: " + nextName + "\n\tTransfers: \n";

        for (Station t : otherStations) {
            String tPrev = (t.prev == null) ? "none" : t.prev.name;
            String tNext = (t.next == null) ? "none" : t.next.name;
            String tStatus = t.inService ? "true" : "false";

            String type = "STATION";
            if (t instanceof EndStation) type = "ENDSTATION";
            else if (t instanceof TransferStation) type = "TRANSFERSTATION";

            s += "\t" + type + " " + t.name + ": " + t.line + " line, in service: " + tStatus + ", previous station: " + tPrev + ", next station: " + tNext;
            if (type.equals("TRANSFERSTATION")) s += "\n\tTransfers: \n\n";
            else s += "\n";
        }

        return s;
    }
}

