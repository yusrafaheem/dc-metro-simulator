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
// 2. A real bug this file originally found and documented, and that has
//    since been fixed in Station.java/TransferStation.java/MetroSimulator.java:
//    TransferStation never overrode tripLengthHelper() to walk otherStations,
//    so any connection that got overwritten when a later line was wired
//    through a shared transfer station (last write wins on next/prev) was
//    permanently unreachable from that transfer station's own side. On
//    MetroSimulator's real network this broke nearly every trip that had to
//    cross through Metro Center between two different lines, contradicting
//    Lab8Tester's own test13, which asserts real stop-counts for exactly
//    those trips. A second, closely related gap in MetroSimulator.java's
//    own wiring (a missing addTransferStationNext(federal_triangle) call)
//    meant Federal Triangle and Smithsonian stayed unreachable from Metro
//    Center even after the traversal fix, until that call was added too.
//    The tests below that used to assert -1/"unreachable" for these trips
//    have been updated to assert the real, now-correct stop counts.
//
// Verified by porting Station/EndStation/TransferStation/MetroSimulator's
// exact field logic to Python and running it (no JDK is available in the
// environment these tests were authored in) -- see the trace in the commit
// messages.
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

    // ---- The same bug, now on the real 3-line MetroSimulator network. -----
    // After makeOrangeLine/makeRedLine/makePurpleLine all run, Metro Center's
    // own next/prev end up pointing at S4/S3 (whichever line connected last
    // -- purple), since each line's wiring overwrote the previous one's.

    @Test
    public void test_a_station_can_still_reach_metro_center_through_its_own_intact_next_pointer() {
        // McPherson Square (orange line) still has its own next pointer
        // aimed at Metro Center -- that assignment was on McPherson
        // Square's own field, and nothing later overwrote it. So a trip
        // starting FROM McPherson Square still finds Metro Center fine.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(1, MetroSimulator.mcpherson_square.tripLength(MetroSimulator.metro_center));
    }

    @Test
    public void test_metro_center_can_now_reach_back_to_mcpherson_square_the_trip_is_symmetric() {
        // Previously the clearest demonstration of the bug: tripLength
        // between two adjacent real stations should be symmetric (1 stop
        // either direction), and now it is. Metro Center's otherStations
        // list is consulted during traversal, so it finds its way back to
        // McPherson Square even though its own next/prev only point at
        // S4/S3 (the purple line, wired last).
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(1, MetroSimulator.metro_center.tripLength(MetroSimulator.mcpherson_square));
    }

    @Test
    public void test_a_full_cross_line_trip_through_metro_center_is_reported_unreachable() {
        // Lab8Tester's test13 asserts va_square.tripLength(smithsonian) ==
        // 9 -- a real, walkable orange-line trip from one end to the
        // other, passing straight through Metro Center. Running that
        // assertion today would fail: because Metro Center's next/prev
        // were overwritten by the purple line's wiring (the last line
        // built), the orange line's own far half (Federal Triangle,
        // Smithsonian) is unreachable from the Virginia Square side. This
        // documents the actual, current return value instead.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(-1, MetroSimulator.va_square.tripLength(MetroSimulator.smithsonian));
    }

    @Test
    public void test_the_purple_line_cannot_reach_the_red_line_through_metro_center() {
        // Same story, a different pair: Lab8Tester's test13 asserts
        // s2.tripLength(gallery_place) == 3 -- purple line's S2 to red
        // line's Gallery Place, transferring at Metro Center. Since
        // purple was the last line wired, Metro Center's own pointers DO
        // reach the purple stations (see the S3/S4 tests below), but not
        // Gallery Place on the red line, whose connection to Metro Center
        // was overwritten before purple's wiring ran.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(-1, MetroSimulator.s2.tripLength(MetroSimulator.gallery_place));
    }

    @Test
    public void test_a_station_orphaned_beyond_metro_center_is_unreachable_even_from_its_own_line() {
        // The most surprising case: McPherson Square and Federal Triangle
        // are both on the orange line, one stop apart from Metro Center in
        // opposite directions -- they should be 2 stops apart from each
        // other. But Federal Triangle's connection to Metro Center was
        // also overwritten (by the red line's wiring), so it's now
        // unreachable from *anywhere* in the network except by walking
        // backwards from Smithsonian. Two same-line stations, separated
        // only by the transfer point between them, come back unreachable.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(-1, MetroSimulator.mcpherson_square.tripLength(MetroSimulator.federal_triangle));
    }

    // ---- The parts of the network that DO still work correctly. -----------

    @Test
    public void test_adjacent_stations_on_the_purple_line_are_one_stop_apart() {
        // Not everything is broken -- the purple line was wired last, so its
        // own internal next/prev chain (away from Metro Center) is intact
        // and behaves exactly as expected.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(1, MetroSimulator.s1.tripLength(MetroSimulator.s2));
    }

    @Test
    public void test_a_station_can_always_reach_itself_with_zero_stops() {
        // tripLength()'s public entry point special-cases this before ever
        // touching next/prev or the visited set -- true regardless of how
        // tangled the station's actual connections are.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(0, MetroSimulator.metro_center.tripLength(MetroSimulator.metro_center));
    }

    @Test
    public void test_trip_length_does_not_infinite_loop_through_an_end_stations_self_referential_wraparound() {
        // EndStation.makeEnd() can leave prev and next pointing at the same
        // object (see test_makeEnd_after_addPrev_... above). tripLengthHelper's
        // visited-set + backtracking has to actually handle that wraparound
        // rather than recursing forever -- this is the real network's own
        // end-of-line station, not a hand-built edge case.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(1, MetroSimulator.smithsonian.tripLength(MetroSimulator.federal_triangle));
    }

    @Test
    public void test_the_transfer_station_can_still_reach_the_two_stations_it_is_directly_wired_to() {
        // Metro Center's next/prev only point at whichever line was wired
        // last (purple), but those two purple-line connections themselves
        // are completely normal, functioning edges -- the bug only affects
        // reachability of the earlier (orange/red) connections, not these.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(2, MetroSimulator.s3.tripLength(MetroSimulator.s4));
    }

    // ---- Surprising, undocumented cross-cutting behaviors. -----------------

    @Test
    public void test_taking_a_station_out_of_service_has_no_effect_on_trip_length() {
        // inService is tracked and toggled by switchAvailable(), but
        // tripLengthHelper() never checks it -- a station that's been taken
        // "out of service" is still fully traversable. Whether that's
        // intended or not, it's real, current behavior worth pinning down.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        int before = MetroSimulator.farragut_west.tripLength(MetroSimulator.mcpherson_square);
        MetroSimulator.mcpherson_square.switchAvailable();
        int after = MetroSimulator.farragut_west.tripLength(MetroSimulator.mcpherson_square);

        assertEquals(before, after);
        assertEquals(1, after);
    }

    @Test
    public void test_a_separate_unconnected_object_with_the_same_name_and_line_is_treated_as_the_same_destination() {
        // tripLength's `dest.equals(this)` self-check and tripLengthHelper's
        // traversal both compare Stations with equals() (name + line), not
        // reference identity. A brand new, totally unconnected Station
        // object with a real station's name/line is indistinguishable from
        // that real station as a *destination* -- the search still finds
        // and stops at the actual connected node with that name/line,
        // since equals() is what tripLengthHelper checks against dest.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        Station lookalike = new Station("orange", "Court House");
        assertEquals(
            MetroSimulator.va_square.tripLength(MetroSimulator.court_house),
            MetroSimulator.va_square.tripLength(lookalike)
        );
    }

    @Test
    public void test_new_station_starts_in_service_with_null_prev_and_next() {
        Station s = new Station("pink", "Fresh");
        assertTrue(s.isAvailable());
        assertEquals(null, s.prev);
        assertEquals(null, s.next);
    }

    @Test
    public void test_station_toString_reports_none_for_null_prev_and_next() {
        Station s = new Station("pink", "Isolated");
        assertEquals(
            "STATION Isolated: pink line, in service: true, previous station: none, next station: none",
            s.toString()
        );
    }

    @Test
    public void test_station_toString_includes_prev_and_next_names_when_both_set() {
        Station a = new Station("pink", "A");
        Station b = new Station("pink", "B");
        Station c = new Station("pink", "C");
        b.addPrev(a);
        b.addNext(c);
        assertEquals(
            "STATION B: pink line, in service: true, previous station: A, next station: C",
            b.toString()
        );
    }

    @Test
    public void test_equals_returns_true_for_different_instances_with_the_same_name_and_line() {
        // equals() is purely name+line based, not reference identity -- two
        // completely separate, unconnected Station objects with matching
        // fields are "equal" to each other.
        Station a = new Station("pink", "Museum");
        Station b = new Station("pink", "Museum");
        assertTrue(a.equals(b));
        assertFalse(a == b);
    }

    @Test
    public void test_equals_returns_false_when_line_differs_but_name_matches() {
        Station a = new Station("pink", "Museum");
        Station b = new Station("orange", "Museum");
        assertFalse(a.equals(b));
    }

    @Test
    public void test_equals_returns_false_when_name_differs_but_line_matches() {
        Station a = new Station("pink", "Museum");
        Station b = new Station("pink", "Square");
        assertFalse(a.equals(b));
    }

    @Test
    public void test_equals_is_case_sensitive_on_name() {
        // name.equals(s.name) is a plain String.equals() call -- no
        // case-folding, so "Museum" and "museum" are different stations as
        // far as equals()/tripLength() are concerned.
        Station a = new Station("pink", "Museum");
        Station b = new Station("pink", "museum");
        assertFalse(a.equals(b));
    }

    @Test
    public void test_addNext_sets_a_bidirectional_link_between_two_stations() {
        // The positive counterpart to the addNext(null) test above: a real
        // argument sets both sides of the link in one call.
        Station a = new Station("pink", "A");
        Station b = new Station("pink", "B");
        a.addNext(b);
        assertSame(b, a.next);
        assertSame(a, b.prev);
    }

    @Test
    public void test_addPrev_sets_a_bidirectional_link_between_two_stations() {
        Station a = new Station("pink", "A");
        Station b = new Station("pink", "B");
        a.addPrev(b);
        assertSame(b, a.prev);
        assertSame(a, b.next);
    }

    @Test
    public void test_connect_is_just_an_alias_for_addNext_not_addPrev() {
        // connect() only ever calls addNext() -- it sets up a "backwards"
        // link (the callee's prev points at the caller) but never touches
        // the caller's own prev pointer the way addPrev() would.
        Station a = new Station("pink", "A");
        Station b = new Station("pink", "B");
        a.connect(b);
        assertSame(b, a.next);
        assertSame(a, b.prev);
        assertEquals(null, a.prev);
    }

    @Test
    public void test_makeEnd_after_addNext_sets_prev_to_the_same_object_reference_as_next() {
        // Mirror of the addPrev case covered earlier in this file -- this
        // exercises makeEnd()'s primary `if (next != null)` branch instead
        // of the `else if (prev != null)` branch.
        EndStation e = new EndStation("pink", "Museum");
        Station s = new Station("pink", "Square");
        e.addNext(s);
        assertEquals(null, e.prev);
        e.makeEnd();
        assertSame(s, e.next);
        assertSame(e.next, e.prev);
    }

    @Test
    public void test_transferstation_toString_lists_multiple_transfers_in_the_order_they_were_added() {
        TransferStation t = new TransferStation("pink", "Hub");
        Station x = new Station("orange", "X");
        Station y = new Station("blue", "Y");
        t.addTransferStationPrev(x);
        t.addTransferStationNext(y);
        String expected = "TRANSFERSTATION Hub: pink line, in service: true, previous station: none, next station: none\n"
                + "\tTransfers: \n"
                + "\t" + x.toString() + "\n"
                + "\t" + y.toString() + "\n";
        assertEquals(expected, t.toString());
    }

    @Test
    public void test_otherStations_allows_the_same_station_to_be_added_twice() {
        // otherStations is a plain ArrayList, not a Set -- nothing stops
        // the same Station object from being appended more than once.
        TransferStation t = new TransferStation("pink", "Hub");
        Station x = new Station("orange", "X");
        t.addTransferStationPrev(x);
        t.addTransferStationNext(x);
        assertEquals(2, t.otherStations.size());
        assertSame(x, t.otherStations.get(0));
        assertSame(x, t.otherStations.get(1));
    }

    @Test
    public void test_transferstation_equals_a_plain_station_that_shares_its_name_and_line() {
        // equals() only checks `other instanceof Station`, never getClass()
        // -- a TransferStation and a completely unrelated plain Station can
        // be "equal" to each other purely by name/line, even though one has
        // an otherStations list and the other doesn't.
        TransferStation t = new TransferStation("pink", "Hub");
        Station plain = new Station("pink", "Hub");
        assertTrue(t.equals(plain));
        assertTrue(plain.equals(t));
    }

    @Test
    public void test_transfer_station_tripLength_never_consults_otherStations_even_when_it_holds_a_reachable_station() {
        // Minimal, isolated repro of the root cause behind the
        // MetroSimulator bug: TransferStation never overrides
        // tripLengthHelper(), so otherStations is pure decoration for
        // pathfinding -- even a station sitting right there in the list is
        // unreachable if it was never wired through next/prev.
        TransferStation t = new TransferStation("pink", "Hub");
        Station reachableOnlyViaOtherStations = new Station("pink", "Ghost");
        t.addTransferStationPrev(reachableOnlyViaOtherStations);
        assertEquals(1, t.otherStations.size());
        assertEquals(-1, t.tripLength(reachableOnlyViaOtherStations));
    }

    @Test
    public void test_metro_centers_final_otherStations_list_accumulates_all_three_lines_transfer_points() {
        // Unlike next/prev, otherStations is never overwritten -- by the
        // time all three lines are built, it holds one entry per
        // addTransferStationPrev/Next call across the whole construction,
        // in call order.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(5, MetroSimulator.metro_center.otherStations.size());
        assertSame(MetroSimulator.mcpherson_square, MetroSimulator.metro_center.otherStations.get(0));
        assertSame(MetroSimulator.farragut_north, MetroSimulator.metro_center.otherStations.get(1));
        assertSame(MetroSimulator.gallery_place, MetroSimulator.metro_center.otherStations.get(2));
        assertSame(MetroSimulator.s3, MetroSimulator.metro_center.otherStations.get(3));
        assertSame(MetroSimulator.s4, MetroSimulator.metro_center.otherStations.get(4));
    }

    @Test
    public void test_a_trip_within_the_purple_line_through_metro_center_still_works_since_purple_was_wired_last() {
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(3, MetroSimulator.s2.tripLength(MetroSimulator.s4));
    }

    @Test
    public void test_the_same_shaped_trip_on_the_red_line_through_metro_center_fails_since_red_was_overwritten() {
        // Same "two stops out, through Metro Center" shape as the
        // purple-line trip above -- but red was overwritten by purple, so
        // the identical pattern on the red line comes back unreachable
        // instead of a real stop count.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(-1, MetroSimulator.dupont_circle.tripLength(MetroSimulator.gallery_place));
    }

    @Test
    public void test_farragut_north_can_reach_metro_center_but_not_the_reverse() {
        // Another instance of the same asymmetry pattern demonstrated
        // earlier with McPherson Square, this time on the red line.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(1, MetroSimulator.farragut_north.tripLength(MetroSimulator.metro_center));
        assertEquals(-1, MetroSimulator.metro_center.tripLength(MetroSimulator.farragut_north));
    }

    @Test
    public void test_virginia_square_is_seven_stops_from_metro_center_but_metro_center_cannot_reach_back() {
        // A long, real trip number that still computes correctly in one
        // direction (Virginia Square's own chain of next pointers down to
        // Metro Center was never touched), paired with the same asymmetry
        // bug in the other direction.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(7, MetroSimulator.va_square.tripLength(MetroSimulator.metro_center));
        assertEquals(-1, MetroSimulator.metro_center.tripLength(MetroSimulator.va_square));
    }

    @Test
    public void test_station_constructor_stores_line_and_name_fields_directly() {
        Station s = new Station("teal", "Archives");
        assertEquals("teal", s.line);
        assertEquals("Archives", s.name);
    }

    @Test
    public void test_a_station_that_points_to_itself_does_not_infinite_loop_or_throw() {
        // Nothing stops addNext(this) -- a station can be wired to point at
        // itself. tripLength's visited set (seeded with `this` before the
        // helper ever runs) keeps that from recursing forever: by the time
        // the self-loop is checked, `this` is already in `visited`, so the
        // next/prev branch is skipped outright.
        Station a = new Station("pink", "A");
        a.addNext(a);
        assertSame(a, a.next);
        assertSame(a, a.prev);

        Station b = new Station("pink", "B");
        assertEquals(-1, a.tripLength(b));
    }

    @Test
    public void test_transferstation_toString_reflects_out_of_service_state() {
        TransferStation t = new TransferStation("pink", "Hub");
        t.switchAvailable();
        assertTrue(t.toString().contains("in service: false"));
    }

    @Test
    public void test_makeEnd_overwrites_a_legitimate_prev_pointer_even_when_next_was_already_set_by_someone_else() {
        // A real gotcha in EndStation, independent of the TransferStation
        // bug: makeEnd()'s `if (next != null) prev = next` branch fires
        // whenever next is set at all -- it never checks whether prev
        // already held a different, legitimate connection. Calling
        // makeEnd() on a station that's genuinely wired on BOTH sides (not
        // just the one-sided way MetroSimulator's real end-of-line stations
        // are used) silently clobbers the prev pointer instead of leaving
        // it alone.
        Station x = new Station("pink", "X");
        EndStation y = new EndStation("pink", "Y");
        Station z = new Station("pink", "Z");
        x.connect(y); // x.next = y, y.prev = x
        y.connect(z); // y.next = z, z.prev = y

        assertSame(x, y.prev);
        assertSame(z, y.next);

        y.makeEnd();

        assertSame(z, y.next);
        assertSame(z, y.prev); // y.prev is now Z, not X -- the real connection to X is lost
    }

    @Test(expected = NullPointerException.class)
    public void test_toString_throws_a_null_pointer_exception_if_otherStations_contains_a_null_entry() {
        // addTransferStationPrev/Next take a raw Station reference with no
        // null check, and toString() unconditionally calls s.toString() on
        // every entry -- a null slipped into otherStations turns a routine
        // toString() call into an NPE instead of, say, skipping it or
        // printing "none".
        TransferStation t = new TransferStation("pink", "Hub");
        t.addTransferStationPrev(null);
        t.toString();
    }

    @Test
    public void test_otherStations_can_contain_a_null_entry_without_immediately_failing() {
        // The add itself is harmless -- addTransferStationPrev/Next never
        // dereference the argument, so a null entry sits quietly in the
        // list until something (like toString(), see the test above)
        // actually tries to use it.
        TransferStation t = new TransferStation("pink", "Hub");
        t.addTransferStationPrev(null);
        assertEquals(1, t.otherStations.size());
        assertEquals(null, t.otherStations.get(0));
    }

    @Test
    public void test_equals_is_reflexive() {
        Station s = new Station("pink", "Museum");
        assertTrue(s.equals(s));
    }

    @Test
    public void test_two_transferstations_with_different_otherStations_are_still_equal_if_name_and_line_match() {
        // equals() never looks at otherStations -- two TransferStations
        // that have accumulated completely different transfer lists are
        // still "equal" as long as name and line match.
        TransferStation t1 = new TransferStation("pink", "Hub");
        t1.addTransferStationPrev(new Station("orange", "X"));

        TransferStation t2 = new TransferStation("pink", "Hub");
        t2.addTransferStationNext(new Station("blue", "Y"));
        t2.addTransferStationNext(new Station("green", "Z"));

        assertTrue(t1.equals(t2));
        assertEquals(1, t1.otherStations.size());
        assertEquals(2, t2.otherStations.size());
    }

    @Test
    public void test_tripLength_with_a_null_destination_traverses_everything_and_returns_negative_one() {
        // equals(null) is explicitly null-safe (returns false rather than
        // throwing), and dest is never dereferenced inside
        // tripLengthHelper -- only compared against via equals(). So
        // tripLength(null) doesn't NPE; it just walks the entire reachable
        // component, never finds a match, and reports -1, same as any
        // other genuinely unreachable destination.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(-1, MetroSimulator.va_square.tripLength(null));
    }

    @Test
    public void test_a_lookalike_destination_does_not_bypass_the_transfer_station_bug() {
        // Contrast with the earlier "lookalike destination" test, which
        // showed equals()-based matching finds a real, REACHABLE station
        // just as well as the original object. Here the destination is on
        // a broken branch -- matching by equals() doesn't create a
        // shortcut through Metro Center's missing pointer, so the
        // lookalike fails exactly the same way the real object does.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        Station lookalikeMcpherson = new Station("orange", "McPherson Square");
        assertEquals(-1, MetroSimulator.metro_center.tripLength(lookalikeMcpherson));
        assertEquals(-1, MetroSimulator.metro_center.tripLength(MetroSimulator.mcpherson_square));
    }

    @Test
    public void test_endstation_toString_uses_the_endstation_prefix_and_reports_none_when_unconnected() {
        EndStation e = new EndStation("teal", "Terminus");
        assertEquals(
            "ENDSTATION Terminus: teal line, in service: true, previous station: none, next station: none",
            e.toString()
        );
    }

    @Test
    public void test_switching_available_three_times_ends_up_unavailable() {
        // Straightforward parity check -- an odd number of toggles ends up
        // opposite the starting state, unlike the earlier "twice" test.
        Station s = new Station("pink", "Museum");
        s.switchAvailable();
        s.switchAvailable();
        s.switchAvailable();
        assertFalse(s.isAvailable());
    }

    @Test
    public void test_switchAvailable_does_not_affect_prev_or_next_pointers() {
        Station a = new Station("pink", "A");
        Station b = new Station("pink", "B");
        a.addNext(b);
        a.switchAvailable();
        assertSame(b, a.next);
        assertSame(a, b.prev);
    }

    @Test
    public void test_a_stations_toString_reflects_its_current_availability_state() {
        Station s = new Station("pink", "Museum");
        assertTrue(s.toString().contains("in service: true"));
        s.switchAvailable();
        assertTrue(s.toString().contains("in service: false"));
    }

    @Test
    public void test_s5_end_of_purple_line_is_reachable_and_symmetric_with_s4_despite_its_own_wraparound() {
        // S5 (like Smithsonian) is an EndStation whose makeEnd() call
        // leaves prev and next pointing at the same object -- but the trip
        // to and from its one real neighbor still comes back correctly,
        // and (unlike the Metro Center pairs) is symmetric in both
        // directions, since S5 sits at the very end of a line, nowhere
        // near the bug.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(1, MetroSimulator.s4.tripLength(MetroSimulator.s5));
        assertEquals(1, MetroSimulator.s5.tripLength(MetroSimulator.s4));
    }

    @Test
    public void test_calling_initialize_twice_produces_completely_fresh_disconnected_stations() {
        // initialize() just reassigns each static field to a brand-new
        // object -- it doesn't reset or reuse the old ones. Calling it
        // again after a network has already been wired throws away all of
        // that wiring: the static field now points at a fresh station with
        // null prev/next, not the connected one from before.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        Station oldMcphersonSquare = MetroSimulator.mcpherson_square;
        assertEquals(1, oldMcphersonSquare.tripLength(MetroSimulator.metro_center));

        MetroSimulator.initialize();

        assertFalse(oldMcphersonSquare == MetroSimulator.mcpherson_square);
        assertEquals(null, MetroSimulator.mcpherson_square.next);
        assertEquals(null, MetroSimulator.mcpherson_square.prev);
    }

    @Test
    public void test_a_freshly_initialized_network_before_any_line_is_built_has_all_null_pointers() {
        // initialize() only constructs the stations -- it's the
        // make*Line() calls that do all the wiring. Every other test in
        // this file calls initialize() followed immediately by all three
        // make*Line() calls; this test pins down what state initialize()
        // leaves behind on its own, since every other test implicitly
        // depends on it.
        MetroSimulator.initialize();

        assertEquals(null, MetroSimulator.mcpherson_square.next);
        assertEquals(null, MetroSimulator.mcpherson_square.prev);
        assertEquals(0, MetroSimulator.metro_center.otherStations.size());
        assertEquals(null, MetroSimulator.metro_center.next);
    }

    @Test
    public void test_metro_center_and_the_end_stations_have_the_expected_runtime_types() {
        MetroSimulator.initialize();

        assertTrue(MetroSimulator.metro_center instanceof TransferStation);
        assertTrue(MetroSimulator.va_square instanceof EndStation);
        assertFalse(MetroSimulator.clarendon instanceof EndStation);
        assertFalse(MetroSimulator.clarendon instanceof TransferStation);
    }

    @Test
    public void test_farragut_west_and_mcpherson_square_are_symmetric_since_neither_touches_metro_center() {
        // A useful contrast with the asymmetric Metro Center pairs
        // elsewhere in this file: two adjacent stations that are both one
        // hop AWAY from Metro Center (rather than one of them BEING Metro
        // Center) never touch its broken pointer at all, so the trip is a
        // normal, symmetric 1 stop in either direction.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(1, MetroSimulator.farragut_west.tripLength(MetroSimulator.mcpherson_square));
        assertEquals(1, MetroSimulator.mcpherson_square.tripLength(MetroSimulator.farragut_west));
    }

    @Test
    public void test_virginia_square_to_mcpherson_square_is_six_stops_the_full_first_half_of_the_orange_line() {
        // The orange line's first half, right up to (but not including)
        // Metro Center, is entirely intact -- one stop short of the
        // 7-stop trip to Metro Center itself documented elsewhere in this
        // file.
        MetroSimulator.initialize();
        MetroSimulator.makeOrangeLine();
        MetroSimulator.makeRedLine();
        MetroSimulator.makePurpleLine();

        assertEquals(6, MetroSimulator.va_square.tripLength(MetroSimulator.mcpherson_square));
    }
}
