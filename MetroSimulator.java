public class MetroSimulator {
    
    // Make all stations here as static so we can access them anywhere
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
    // Metro Center is shared and already defined above
    public static Station gallery_place;
    public static EndStation judiciary_square;

    public static EndStation s1;
    public static Station s2;
    public static Station s3;
    // Metro Center again, shared
    public static Station s4;
    public static EndStation s5;

    // Main method to start program and test trip lengths
    public static void main(String[] args) {
        initialize();      // Make stations
        makeOrangeLine();  // Connect orange line stations
        makeRedLine();     // Connect red line stations
        makePurpleLine();  // Connect purple line stations

        // Example test: How many stops from Virginia Square to Metro Center
        int stops = va_square.tripLength(metro_center);
        System.out.println("There are " + stops + " stops between Virginia Square and Metro Center");
    }

    // Make the stations but don't connect them yet
    public static void initialize() {
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

    // Connect stations on the Orange Line in order
    public static EndStation makeOrangeLine() {
        va_square.connect(clarendon);
        clarendon.connect(court_house);
        court_house.connect(rosslyn);
        rosslyn.connect(foggy_bottom);
        foggy_bottom.connect(farragut_west);
        farragut_west.connect(mcpherson_square);
        mcpherson_square.connect(metro_center);
        metro_center.connect(federal_triangle);
        federal_triangle.connect(smithsonian);

        // Make the ends of orange line loop back properly
        va_square.makeEnd();
        smithsonian.makeEnd();

        return va_square;
    }

    // Connect stations on the Red Line in order
    public static EndStation makeRedLine() {
        woodley_park.connect(dupont_circle);
        dupont_circle.connect(farragut_north);
        farragut_north.connect(metro_center);
        metro_center.connect(gallery_place);
        gallery_place.connect(judiciary_square);

        // Make the ends of red line loop back properly
        woodley_park.makeEnd();
        judiciary_square.makeEnd();

        return woodley_park;
    }

    // Connect stations on the Purple Line in order
    public static EndStation makePurpleLine() {
        s1.connect(s2);
        s2.connect(s3);
        s3.connect(metro_center);
        metro_center.connect(s4);
        s4.connect(s5);

        // Make the ends of purple line loop back properly
        s1.makeEnd();
        s5.makeEnd();

        return s1;
    }
}

