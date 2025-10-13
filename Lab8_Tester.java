import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.*;
import java.io.*;

public class Lab8_Tester {

  @Test 
  public void test1(){
    System.out.println("test Station ctor and setup");

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

    assertEquals(true, s1.isAvailable());
    s1.switchAvailable();
    expected = "STATION Museum: pink line, in service: false, previous station: Plaza, next station: Square";
    assertEquals(expected, s1.toString());
    s1.switchAvailable();
    expected = "STATION Museum: pink line, in service: true, previous station: Plaza, next station: Square";
    assertEquals(expected, s1.toString());
  }

  @Test 
  public void test2(){
    System.out.println("test Station equals");

    Station s1 = new Station("pink", "Museum");
    Station s2 = new Station("pink", "Museum");
    Station s3 = new Station("blue", "Museum");
    Station s4 = new Station("pink", "Square");
    assertEquals(true, s1.equals(s1));
    assertEquals(true, s1.equals(s2));
    assertEquals(false, s1.equals(s3));
    assertEquals(false, s1.equals(s4));

    EndStation s5 = new EndStation("pink", "Museum");
    TransferStation s6 = new TransferStation("pink", "Museum");
    assertEquals(true, s1.equals(s5));
    assertEquals(true, s1.equals(s6));
  }

  @Test 
  public void test3(){
    System.out.println("test EndStation setup");

    EndStation s1 = new EndStation("pink", "Museum");
    String expected = "ENDSTATION Museum: pink line, in service: true, previous station: none, next station: none";
    assertEquals(expected, s1.toString());

    assertEquals(true, s1 instanceof Station);
  }

  @Test 
  public void test4(){
    System.out.println("test EndStation makeEnd");

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

  @Test 
  public void test5(){
    System.out.println("test TransferStation setup");

    TransferStation s1 = new TransferStation("pink", "Museum");
    String expected = "TRANSFERSTATION Museum: pink line, in service: true, previous station: none, next station: none\n\tTransfers: \n";
    assertEquals(expected, s1.toString());

    boolean result = false;
    if (s1 instanceof Station)
      result = true;
    assertEquals(true, result);
  }

  @Test 
  public void test6(){
    System.out.println("test add TransferStation");

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

  @Test 
  public void test7(){
    System.out.println("test Station connect");

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

  @Test 
  public void test8(){
    System.out.println("test orange line");

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
    expected = "STATION Farragut West: orange line, in service: true, previous station: Foggy Bottom, next station: McPherson Square";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "STATION McPherson Square: orange line, in service: true, previous station: Farragut West, next station: Metro Center";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "TRANSFERSTATION Metro Center: orange/red/purple line, in service: true, previous station: McPherson Square, next station: Federal Triangle\n" +
                    "\tTransfers: \n";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "STATION Federal Triangle: orange line, in service: true, previous station: Metro Center, next station: Smithsonian";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "ENDSTATION Smithsonian: orange line, in service: true, previous station: Federal Triangle, next station: Federal Triangle";
    assertEquals(expected, next.toString());
  }

  @Test 
  public void test9(){
    System.out.println("test red line");

    MetroSimulator.initialize();
    MetroSimulator.makeOrangeLine();
    EndStation red = MetroSimulator.makeRedLine();
    String expected = "ENDSTATION Woodley Park: red line, in service: true, previous station: Dupont Circle, next station: Dupont Circle";
    assertEquals(expected, red.toString());

    Station next = red.next;
    expected = "STATION Dupont Circle: red line, in service: true, previous station: Woodley Park, next station: Farragut North";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "STATION Farragut North: red line, in service: true, previous station: Dupont Circle, next station: Metro Center";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "TRANSFERSTATION Metro Center: orange/red/purple line, in service: true, previous station: McPherson Square, next station: Federal Triangle\n" +
                    "\tTransfers: \n\tSTATION Farragut North: red line, in service: true, previous station: Dupont Circle, next station: Metro Center\n" + 
                    "\tSTATION Gallery Place: red line, in service: true, previous station: Metro Center, next station: Judiciary Square\n";
    assertEquals(expected, next.toString());
    next = ((TransferStation) next).otherStations.get(1);
    expected = "STATION Gallery Place: red line, in service: true, previous station: Metro Center, next station: Judiciary Square";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "ENDSTATION Judiciary Square: red line, in service: true, previous station: Gallery Place, next station: Gallery Place";
    assertEquals(expected, next.toString());
  }

  @Test 
  public void test10(){
    System.out.println("test purple line");

    MetroSimulator.initialize();
    MetroSimulator.makeOrangeLine();
    MetroSimulator.makeRedLine();
    EndStation purple = MetroSimulator.makePurpleLine();

    String expected = "ENDSTATION S1: purple line, in service: true, previous station: S2, next station: S2";
    assertEquals(expected, purple.toString());

    Station next = purple.next;
    expected = "STATION S2: purple line, in service: true, previous station: S1, next station: S3";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "STATION S3: purple line, in service: true, previous station: S2, next station: Metro Center";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "TRANSFERSTATION Metro Center: orange/red/purple line, in service: true, previous station: McPherson Square, next station: Federal Triangle\n" +
                    "\tTransfers: \n\tSTATION Farragut North: red line, in service: true, previous station: Dupont Circle, next station: Metro Center\n" + 
                    "\tSTATION Gallery Place: red line, in service: true, previous station: Metro Center, next station: Judiciary Square\n" +
                    "\tSTATION S3: purple line, in service: true, previous station: S2, next station: Metro Center\n" +
                    "\tSTATION S4: purple line, in service: true, previous station: Metro Center, next station: S5\n";
    assertEquals(expected, next.toString());
    next = ((TransferStation) next).otherStations.get(3);
    expected = "STATION S4: purple line, in service: true, previous station: Metro Center, next station: S5";
    assertEquals(expected, next.toString());
    next = next.next;
    expected = "ENDSTATION S5: purple line, in service: true, previous station: S4, next station: S4";
    assertEquals(expected, next.toString());
  }

  @Test 
  public void test11(){
    System.out.println("test short straight trips");

    EndStation s1 = new EndStation("pink", "Museum");
    Station s2 = new Station("pink", "Square");
    Station s3 = new Station("pink", "Hill");
    EndStation s4 = new EndStation("pink", "Plaza");
    s1.connect(s2);
    s2.connect(s3);
    s3.connect(s4);
    s1.makeEnd();
    s4.makeEnd();

    assertEquals(1, s1.tripLength(s2));
    assertEquals(3, s1.tripLength(s4));
    assertEquals(0, s1.tripLength(s1));

  }

  @Test 
  public void test12(){
    System.out.println("test short transfer trips");

    EndStation s1 = new EndStation("pink", "S1");
    Station s2 = new Station("pink", "S2");
    Station s3 = new Station("pink", "S3");
    TransferStation transfer = new TransferStation("blue/pink", "Transfer");
    EndStation s4 = new EndStation("pink", "S4");
    s1.connect(s2);
    s2.connect(s3);
    transfer.addTransferStationPrev(s3);
    transfer.addTransferStationNext(s4);
    s1.makeEnd();
    s4.makeEnd();

    EndStation b1 = new EndStation("blue", "B1");
    Station b2 = new Station("blue", "B2");
    Station b3 = new Station("blue", "B3");
    Station b4 = new Station("blue", "B4");
    EndStation b5 = new EndStation("blue", "B5");
    b1.connect(b2);
    b2.connect(b3);
    transfer.addTransferStationPrev(b3);
    transfer.connect(b4);
    b4.connect(b5);
    b1.makeEnd();
    b5.makeEnd();

    assertEquals(4, s1.tripLength(s4));
    assertEquals(4, s1.tripLength(b3));
    assertEquals(4, s1.tripLength(b4));
    assertEquals(5, s1.tripLength(b5));
    assertEquals(5, b1.tripLength(b5));
  }


  @Test 
  public void test13(){
    System.out.println("test long trips");

    MetroSimulator.initialize();
    MetroSimulator.makeOrangeLine();
    MetroSimulator.makeRedLine();
    MetroSimulator.makePurpleLine();

    assertEquals(9, MetroSimulator.va_square.tripLength(MetroSimulator.smithsonian));
    assertEquals(9, MetroSimulator.va_square.tripLength(MetroSimulator.judiciary_square));
    assertEquals(4, MetroSimulator.foggy_bottom.tripLength(MetroSimulator.s4));
    assertEquals(3, MetroSimulator.s2.tripLength(MetroSimulator.gallery_place));
  }

}
