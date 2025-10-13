import java.util.ArrayList;

public class MetroSimulator {

    // all the stations are made here
    public static EndStation va_square;
    public static Station clarendon;
    public static Station court_house;
    public static Station rosslyn;
    public static Station foggy_bottom;
    public static Station farragut_west;
    public static Station mcpherson_square;
    public static TransferStation metro_center;
    public static Station federal_triangle;
    public static EndStation smithsonian;

    public static EndStation woodley_park;
    public static Station dupont_circle;
    public static Station farragut_north;
    // metro center already here
    public static Station gallery_place;
    public static EndStation judiciary_square;

    public static EndStation s1;
    public static Station s2;
    public static Station s3;
    // metro center again
    public static Station s4;
    public static EndStation s5;

    public static void main(String[] args){
        initialize();  // make all stations
        makeOrangeLine();  // connect orange line
        makeRedLine();     // connect red line
        makePurpleLine();  // connect purple line

        int stops = va_square.tripLength(metro_center);  // find stops between these two
        System.out.println("There are " + stops + " stops between Virginia Square and Metro Center");
    }

    // create all stations with names and colors
    public static void initialize(){
        va_square = new EndStation("orange", "Virginia Square");
        clarendon = new Station("orange", "Clarendon");
        court_house = new Station("orange", "Court House");
        rosslyn = new Station("orange", "Rosslyn");
        foggy_bottom = new Station("orange", "Foggy Bottom");
        farragut_west = new Station("orange", "Farragut West");
        mcpherson_square = new Station("orange", "McPherson Square");
        metro_center = new TransferStation("orange/red/purple", "Metro Center");
        federal_triangle = new Station("orange", "Federal Triangle");
        smithsonian = new EndStation("orange", "Smithsonian");

        woodley_park = new EndStation("red", "Woodley Park");
        dupont_circle = new Station("red", "Dupont Circle");
        farragut_north = new Station("red", "Farragut North");
        gallery_place = new Station("red", "Gallery Place");
        judiciary_square = new EndStation("red", "Judiciary Square");

        s1 = new EndStation("purple", "S1");
        s2 = new Station("purple", "S2");
        s3 = new Station("purple", "S3");
        s4 = new Station("purple", "S4");
        s5 = new EndStation("purple", "S5");
    }

    // connect stations for orange line
    public static EndStation makeOrangeLine(){
        va_square.connect(clarendon);
        clarendon.connect(court_house);
        court_house.connect(rosslyn);
        rosslyn.connect(foggy_bottom);
        foggy_bottom.connect(farragut_west);
        farragut_west.connect(mcpherson_square);
        mcpherson_square.connect(metro_center);
        metro_center.addTransferStationPrev(mcpherson_square); // add metro_center prev link for transfer
        metro_center.connect(federal_triangle);
        federal_triangle.connect(smithsonian);
        smithsonian.makeEnd(); // to close the loop at the end station
        va_square.makeEnd();    // to close the loop at the other end station
        return va_square;
    }

    // connect stations for red line
    public static EndStation makeRedLine(){
        woodley_park.connect(dupont_circle);
        dupont_circle.connect(farragut_north);
        farragut_north.connect(metro_center);
        metro_center.addTransferStationPrev(farragut_north); // add red line prev link to metro_center
        metro_center.connect(gallery_place);
        metro_center.addTransferStationNext(gallery_place);  // add red line next link to metro_center
        gallery_place.connect(judiciary_square);
        judiciary_square.makeEnd();
        woodley_park.makeEnd();
        return woodley_park;
    }

    // connect stations for purple line
    public static EndStation makePurpleLine(){
        s1.connect(s2);
        s2.connect(s3);
        s3.connect(metro_center);
        metro_center.addTransferStationPrev(s3); // add purple line prev link
        metro_center.connect(s4);
        metro_center.addTransferStationNext(s4); // add purple line next link
        s4.connect(s5);
        s5.makeEnd();
        s1.makeEnd();
        return s1;
    }
}

