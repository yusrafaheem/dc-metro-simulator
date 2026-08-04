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

    @Test
    public void test_addNext_with_null_clears_the_next_pointer_without_throwing() {
        // addNext guards `s.prev = this` behind a null check, but
        // `this.next = s` runs unconditionally -- passing null should just
        // clear the pointer, not throw a NullPointerException.
        Station a = new Station("pink", "A");
        Station b = new Station("pink", "B");
        a.addNext(b);
        assertEquals(b, a.next);

        a.addNext(null);
        assertEquals(null, a.next);
        // b's own prev link is untouched by this call -- only a.next changed.
        assertSame(a, b.prev);
    }

    @Test
    public void test_addPrev_with_null_clears_the_prev_pointer_without_throwing() {
        // Mirror of the addNext(null) case, on the other pointer.
        Station c = new Station("pink", "C");
        Station d = new Station("pink", "D");
        c.addPrev(d);
        assertEquals(d, c.prev);

        c.addPrev(null);
        assertEquals(null, c.prev);
        assertSame(c, d.next);
    }

    @Test
    public void test_makeEnd_is_a_no_op_when_both_prev_and_next_are_null() {
        // makeEnd()'s comment says "If both are null, do nothing" -- a
        // freshly-constructed, unconnected EndStation should stay
        // unconnected after calling it, not throw or fabricate a
        // self-reference.
        EndStation e = new EndStation("pink", "Isolated");
        e.makeEnd();
        assertEquals(null, e.prev);
        assertEquals(null, e.next);
    }

    @Test
    public void test_makeEnd_is_idempotent_when_called_a_second_time() {
        // After the first makeEnd() call, both prev and next are non-null
        // and equal -- calling it again takes the `if (next != null)`
        // branch and reassigns prev = next, landing on the exact same
        // state rather than drifting or throwing.
        EndStation e = new EndStation("pink", "Museum");
        Station s = new Station("pink", "Square");
        e.addNext(s);
        e.makeEnd();
        Station firstPrev = e.prev;
        Station firstNext = e.next;

        e.makeEnd();
        assertSame(firstPrev, e.prev);
        assertSame(firstNext, e.next);
    }

    @Test
    public void test_makeEnd_after_addPrev_sets_next_to_the_exact_same_object_reference_as_prev() {
        // The addNext-then-makeEnd direction is already exercised in
        // Lab8Tester's test4. This exercises the mirror direction
        // (addPrev, where the missing pointer is next) and checks object
        // *identity*, not just equal-looking toString output -- e.prev and
        // e.next should end up pointing at the literal same object.
        EndStation e = new EndStation("pink", "Museum");
        Station s = new Station("pink", "Square");
        e.addPrev(s);
        assertEquals(null, e.next);

        e.makeEnd();
        assertSame(s, e.prev);
        assertSame(e.prev, e.next);
    }

    @Test
    public void test_otherStations_starts_empty_for_a_new_transfer_station() {
        TransferStation t = new TransferStation("pink", "Museum");
        assertEquals(0, t.otherStations.size());
    }

    @Test
    public void test_addTransferStationPrev_and_addTransferStationNext_both_simply_append_in_call_order() {
        // Despite the two method names implying a directional distinction
        // (like addPrev/addNext do for the base next/prev pointers), both
        // addTransferStationPrev and addTransferStationNext have identical
        // bodies: `otherStations.add(station)`. There's no actual
        // "prev vs next" semantic here -- calling either just appends, in
        // whatever order you called them.
        TransferStation t = new TransferStation("pink", "Museum");
        Station x = new Station("a", "X");
        Station y = new Station("b", "Y");
        Station z = new Station("c", "Z");

        t.addTransferStationPrev(x);
        t.addTransferStationNext(y);
        t.addTransferStationPrev(z);

        assertEquals(3, t.otherStations.size());
        assertSame(x, t.otherStations.get(0));
        assertSame(y, t.otherStations.get(1));
        assertSame(z, t.otherStations.get(2));
    }

    @Test
    public void test_transfer_station_toString_on_a_fresh_instance_has_an_empty_transfers_section() {
        // A more precise version of Lab8Tester's test5, which only checks
        // the full toString of a fresh TransferStation once. This isolates
        // just the "Transfers:" section formatting, which the next several
        // commits build on to interpret otherStations' printed contents.
        TransferStation t = new TransferStation("pink", "Empty");
        String expected = "TRANSFERSTATION Empty: pink line, in service: true, previous station: none, next station: none\n"
                + "\tTransfers: \n";
        assertEquals(expected, t.toString());
    }

    // ---- The core bug: wiring a TransferStation to a second line clobbers
    // its own next/prev pointer from the first line. ------------------------

    @Test
    public void test_connecting_a_transfer_station_to_a_second_line_overwrites_its_own_next_pointer() {
        Station a = new Station("orange", "A");
        TransferStation t = new TransferStation("orange/red", "T");
        a.connect(t);
        Station b = new Station("orange", "B");
        t.connect(b);
        assertSame(b, t.next); // orange line wired t.next to B

        // Now wire a second, unrelated line through the same transfer
        // station object -- t.connect() is plain Station.connect(),
        // inherited unchanged, so this just does `t.next = d`.
        Station d = new Station("red", "D");
        t.connect(d);
        assertSame(d, t.next); // t.next now points at D, not B
        // B still thinks it's connected to T on the other side...
        assertSame(t, b.prev);
        // ...but T no longer has any direct pointer back to B.
    }

    @Test
    public void test_connecting_a_transfer_station_to_a_second_line_overwrites_its_own_prev_pointer() {
        // Mirror of the previous test, on the prev side: connecting INTO a
        // transfer station (rather than the transfer station connecting
        // out) sets its prev via addNext's `s.prev = this` branch. A
        // second unrelated line connecting in the same way overwrites it.
        Station a = new Station("orange", "A");
        TransferStation t = new TransferStation("orange/red", "T");
        a.connect(t);
        assertSame(a, t.prev);

        Station c = new Station("red", "C");
        c.connect(t);
        assertSame(c, t.prev); // t.prev now points at C, not A
        // A still has t as its next...
        assertSame(t, a.next);
        // ...but t has no direct pointer back to A anymore.
    }

    @Test
    public void test_otherStations_holds_the_earlier_lines_connection_even_though_the_direct_pointer_is_lost() {
        // This is exactly the pattern MetroSimulator.makeOrangeLine/
        // makeRedLine/makePurpleLine follow: connect(), then immediately
        // call addTransferStationPrev/Next to also record the connection
        // in otherStations. otherStations DOES correctly retain A here --
        // the data isn't lost. The bug (documented in later commits) is
        // that tripLength() never actually reads otherStations back out.
        Station a = new Station("orange", "A");
        TransferStation t = new TransferStation("orange/red", "T");
        a.connect(t);
        t.addTransferStationPrev(a);

        Station c = new Station("red", "C");
        c.connect(t);
        t.addTransferStationPrev(c);

        assertSame(c, t.prev); // direct pointer overwritten, as shown above
        assertSame(a, t.otherStations.get(0)); // but A is still recorded here
        assertSame(c, t.otherStations.get(1));
    }
}
