# DC Metro Simulator

A Java simulation of the Washington DC Metrorail system, modeling stations and rail lines as a linked data structure. Built for a data structures / OOP coursework assignment.

## What it does

- **Station.java** — base class for a metro stop: tracks its line, name, in-service status, and links to the previous/next station. Includes a recursive `tripLength()` method that finds the number of stops between any two stations on the network.
- - **EndStation.java** — a `Station` subclass representing a line terminus, where the "next" link wraps back to close the line.
  - - **TransferStation.java** — a `Station` subclass representing a station shared by multiple lines (e.g. Metro Center), holding references to the corresponding stations on each connecting line.
    - - **MetroSimulator.java** — builds a small model of the network (Orange, Red, and Purple lines meeting at Metro Center) and demonstrates trip-length lookups between stations.
      - - **Lab8Tester.java** — test suite for the station network and pathfinding logic.
       
        - ## Example
       
        - `MetroSimulator.main()` builds the network and prints the number of stops between Virginia Square and Metro Center along the Orange Line.
       
        - ## Design notes
       
        - Stations are connected as a doubly linked list per line, with `TransferStation` bridging multiple lines together. `tripLength()` performs a recursive search (tracking visited stations) to find the shortest number of stops between two points on the network.
