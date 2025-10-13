// EndStation is a special type of Station that is at the end of a metro line
public class EndStation extends Station {

    // Constructor calls the Station constructor to set line color and name
    public EndStation(String line, String name) {
        super(line, name);
    }

    // This method "loops" the line by connecting prev and next stations at the end
    // So the line doesn't just stop — prev and next point to each other
    public void makeEnd() {
        if (prev != null) {
            // If there is a previous station, make 'next' point back to it
            next = prev;
        } else if (next != null) {
            // Otherwise, if there's a next station, make 'prev' point to it
            prev = next;
        }
    }

    // toString returns a nice description of this EndStation for debugging
    public String toString() {
        // Use "none" if prev or next don't exist to avoid null errors
        String prevName = (prev == null) ? "none" : prev.name;
        String nextName = (next == null) ? "none" : next.name;
        // Show if this station is currently in service
        String status = inService ? "true" : "false";
        // Return a string that says this is an ENDSTATION with its details
        return "ENDSTATION " + name + ": " + line + " line, in service: " + status + 
               ", previous station: " + prevName + ", next station: " + nextName;
    }
}
