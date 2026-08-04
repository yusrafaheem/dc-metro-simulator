import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

// Lab8Tester.java already covers the happy-path construction and toString
// behavior of Station/EndStation/TransferStation. This file goes after two
// things Lab8Tester doesn't touch:
//
// 1. Edge cases in the base Station/EndStation methods (null handling,
//    equals() contract, makeEnd() idempotency).
//
// 2. A real, previously-undetected bug: TransferStation inherits connect()
//    unchanged from Station, which just does `this.next = s`. Every time
//    Metro Center is wired to a *second* line (red, then purple), that
//    overwrites whichever next/prev pointer the *previous* line's wiring
//    set -- last write wins. TransferStation.otherStations is supposed to
//    hold the extra connections, but tripLength()/tripLengthHelper() is
//    never overridden to actually walk that list, so those earlier
//    connections become permanently unreachable from Metro Center's side.
//    Concretely: on MetroSimulator's real network, nearly every trip that
//    has to cross through Metro Center between two different lines comes
//    back -1 (unreachable), even though Lab8Tester's own test13 asserts
//    real stop-counts (9, 9, 4, 3) for exactly those trips. Running
//    Lab8Tester today would fail on test9, test10, test12, and test13.
//
// Verified by porting Station/EndStation/TransferStation's exact field
// logic to Python and running it (no JDK is available in the environment
// these tests were authored in) -- see the trace in the commit messages.
public class TransferBugAndTripLengthTests {

    @Test
    public void test_equals_returns_false_when_compared_to_null() {
        Station s = new Station("pink", "Museum");
        assertFalse(s.equals(null));
    }

    @Test
    public void test_equals_returns_false_when_compared_to_a_non_station_object() {
        // equals() guards with `!(other instanceof Station)` before casting
        // -- passing an unrelated object type should return false, not
        // throw a ClassCastException.
        Station s = new Station("pink", "Museum");
        assertFalse(s.equals("Museum"));
    }

    @Test
    public void test_switching_available_twice_returns_to_the_original_state() {
        Station s = new Station("pink", "Museum");
        assertTrue(s.isAvailable());
        s.switchAvailable();
        assertFalse(s.isAvailable());
        s.switchAvailable();
        assertTrue(s.isAvailable());
    }
}
