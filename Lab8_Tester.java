import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Lab8Tester runs all the JUnit tests to verify your metro system classes:
 * Station, EndStation, TransferStation, and MetroSimulator.
 */
public class Lab8Tester {

  // Test basic Station creation, connections, and availability toggling
  @Test
  public void test1() {
    System.out.println("Test 1: Station constructor and setup");

    Station s1 = new Station("pink", "Museum");
    String expected = "STATION Museum: pink line, in service: true, previous station: none, next station: none";
    assertEquals(expected, s1.toString());

    Station s2 = new Station("green", "Square");
    Station s3 = new Station("blue", "Plaza");
    s1.addNext(s2);
    expected = "STATION Museum: pink line, in service: true, previous station: none, next station: Square";
    assertEquals(expected, s1.toString());
    expected = "STATION Square: green line, in service: true, previous station: Museum, next station: none";
    assertEquals(expected, s2.toString());

    s1.addPrev(s3);
    expected = "STATION Museum: pink line, in service: true, previous station: Plaza, next station: Square";
    assertEquals(expected, s1.toString());
    expected = "STATION Plaza: blue line, in service: true, previous station: none, next station: Museum";
    assertEquals(expected, s3.toString());

    // Check availability toggling
    assertEquals(true, s1.isAvailable());
    s1.switchAvailable();
    expected = "STATION Museum: pink line, in service: false, previous station: Plaza, next station: Square";
    assertEquals(expected, s1.toString());
    s1.switchAvailable();
    expected = "STATION Museum: pink line, in service: true, previous station: Plaza, next station: Square";
    assertEquals(expected, s1.toString());
  }

  // Test the equals method for Station and subclasses
  @Test
  public void test2() {
    System.out.println("Test 2: Station equals");

    Station s1 = new Station("pink", "Museum");
    Station s2 = new Station("pink", "Museum");
    Station s3 = new Station("blue", "Museum");
    Station s4 = new Station("pink", "Square");

    assertEquals(true, s1.equals(s1));  // same object
    assertEquals(true, s1.equals(s2));  // same name and line
    assertEquals(false, s1.equals(s3)); // different line
    assertEquals(false, s1.equals(s4)); // different name

    EndStation s5 = new EndStation("pink", "Museum");
    TransferStation s6 = new TransferStation("pink", "Museum");
    // Should be true because equality only checks name and line, not class
    assertEquals(true, s1.equals(s5));
    assertEquals(true, s1.equals(s6));
  }

  // Test basic EndStation setup
  @Test
  public void test3() {
    System.out.println("Test 3: EndStation setup");

    EndStation s1 = new EndStation("pink", "Museum");
    String expected = "ENDSTATION Museum: pink line, in service: true, previous station: none, next station: none";
    assertEquals(expected, s1.toString());

    // EndStation is a subclass of Station
    assertEquals(true, s1 instanceof Station);
  }

  // Test the makeEnd() method on EndStation
  @Test
  public void test4() {
    System.out.println("Test 4: EndStation makeEnd");

    EndStation s1 = new EndStation("pink", "Museum");
    Station s2 = new Station("pink", "Square");
    s1.addNext(s2);

    String expected = "ENDSTATION Museum: pink line, in service: true, previous station: none, next station: Square";
    assertEquals(expected, s1.toString());

    s1.makeEnd();
    expected = "ENDSTATION Museum: pink line, in service: true, previous station: Square, next station: Square";
    assertEquals(expected, s1.toString());

    s1 = new EndStation("pink", "Museum");
    s2 = new Station("pink", "Square");
    s2.addNext(s1);

    expected = "ENDSTATION Museum: pink line, in service: true, previous station: Square, next station: none";
    assertEquals(expected, s1.toString());

    s1.makeEnd();
    expected = "ENDSTATION Museum: pink line, in service: true, previous station: Square, next station: Square";
    assertEquals(expected, s1.toString());
  }

  // Test basic TransferStation setup
  @Test
  public void test5() {
    System.out.println("Test 5: TransferStation setup");

    TransferStation s1 = new TransferStation("pink", "Museum");
    String expected = "TRANSFERSTATION Museum: pink line, in service: true, previous station: none, next station: none\n\tTransfers: \n";
    assertEquals(expected, s1.toString());

    assertEquals(true, s1 instanceof Station);
  }

  // Test adding transfer stations to a TransferStation
  @Test
  public void test6() {
    System.out.println("Test 6: Add TransferStation connections");

    TransferStation s1 = new TransferStation("pink", "Museum");

    Station s2 = new Station("blue", "Square");
    s1.addTransferStationPrev(s2);
    String expected = "TRANSFERSTATION Museum: pink line, in service: true, previous station: none, next station: none\n\tTransfers: \n" +
                      "\tSTATION Square: blue line, in service: true, previous station: none, next station: Museum\n";
    assertEquals(expected, s1.toString());

    EndStation s3 = new EndStation("green", "Plaza");
    s1.addTransferStationNext(s3);
    expected = "TRANSFERSTATION Museum: pink line, in service: true, previous station: none, next station: none\n\tTransfers: \n" +
               "\tSTATION Square: blue line, in service: true, previous station: none, next station: Museum\n" +
               "\tENDSTATION Plaza: green line, in service: true, previous station: Museum, next station: none\n";
    assertEquals(expected, s1.toString());

    TransferStation s4 = new TransferStation("yellow", "Hill");
    s1.addTransferStationPrev(s4);
    expected = "TRANSFERSTATION Museum: pink line, in service: true, previous station: none, next station: none\n\tTransfers: \n" +
               "\tSTATION Square: blue line, in service: true, previous station: none, next station: Museum\n" +
               "\tENDSTATION Plaza: green line, in service: true, previous station: Museum, next station: none\n" +
               "\tTRANSFERSTATION Hill: yellow line, in service: true, previous station: none, next station: Museum\n\tTransfers: \n\n";
    assertEquals(expected, s1.toString());
  }

  // Test the connect method for Stations (previous and next)
  @Test
  public void test7() {
    System.out.println("Test 7: Station connect method");

    Station s1 = new Station("pink", "Museum");
    String expected = "STATION Museum: pink line, in service: true, previous station: none, next station: none";
    assertEquals(expected, s1.toString());

    Station s2 = new Station("green", "Square");
    s1.connect(s2);
    expected = "STATION Museum: pink line, in service: true, previous station: none, next station: Square";
    assertEquals(expected, s1.toString());
    expected = "STATION Square: green line, in service: true, previous station: Museum, next station: none";
    assertEquals(expected, s2.toString());

    Station s3 = new Station("blue", "Plaza");
    s3.connect(s1);
    expected = "STATION Museum: pink line, in service: true, previous station: Plaza, next station: Square";
    assertEquals(expected, s1.toString());
    expected = "STATION Plaza: blue line, in service: true, previous station: none, next station: Museum";
    assertEquals(expected, s3.toString());
  }

  // Test the Orange metro line as configured in MetroSimulator
  @Test
  public void test8() {
    System.out.println("Test 8: Orange line stations");

    MetroSimulator.initialize();
    EndStation orange = MetroSimulator.makeOrangeLine();

    String expected = "ENDSTATION Virginia Square: orange line, in service: true, previous station: Clarendon, next station: Clarendon";
    assertEquals(expected, orange.toString());

    Station next = orange.next;
    expected = "STATION Clarendon: orange line, in service: true, previous station: Virginia Square, next station: Court House";
    assertEquals(expected, next.toString());

    next = next.next;
    expected = "STATION Court House: orange line, in service: true, previous station: Clarendon, next station: Rosslyn";
    assertEquals(expected, next.toString());

    next = next.next;
    expected = "STATION Rosslyn: orange line, in service: true, previous station: Court House, next station: Foggy Bottom";
    assertEquals(expected, next.toString());

    next = next.next;
    expected = "STATION Foggy Bottom: orange line, in service: true, previous station: Rosslyn, next station: Farragut West";
    assertEquals(expected, next.toString());

    next = next.next;
    expected = "STATION Farragut West: orange line, in service: true, previous station: Foggy

