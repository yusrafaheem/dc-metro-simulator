// TransferStation.java

import java.util.ArrayList;
import java.util.List;

public class TransferStation extends Station {

    // List to hold transfer stations connected to this transfer station
    ArrayList<Station> otherStations;

    public TransferStation(String line, String name) {
        super(line, name);
        otherStations = new ArrayList<>();
    }

    // Add a transfer station considered "previous" (but not affecting prev/next pointers)
    public void addTransferStationPrev(Station station) {
        otherStations.add(station);
    }

    // Add a transfer station considered "next"
    public void addTransferStationNext(Station station) {
        otherStations.add(station);
    }

    // Fix for the transfer bug: tripLengthHelper (in Station) now also
    // walks whatever getTransferNeighbors() returns, and here that's
    // otherStations -- the connections that got clobbered on this same
    // object's own next/prev fields when a later line was wired through
    // it are still reachable via this list.
    @Override
    protected List<Station> getTransferNeighbors() {
        return otherStations;
    }

    @Override
    public String toString() {
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("TRANSFERSTATION %s: %s line, in service: %s, previous station: %s, next station: %s\n",
                name, line, inService, prevName, nextName));
        sb.append("\tTransfers: \n");
        for (Station s : otherStations) {
            sb.append("\t").append(s.toString()).append("\n");
        }
        return sb.toString();
    }
}
