Package record;

import java.sql.*;
import java.util.*;

class DBConnection {
 public static Connection getConnection() throws Exception {
     String url = "jdbc:mysql://localhost:3306/airline_system";
     String user = "root";        // change to your MySQL username
     String pass = "12345";        // change to your MySQL password
     return DriverManager.getConnection(url, user, pass);
 }
}

//------------------ FLIGHT CLASS ------------------
class Flight {
 int id;
 String flightNo, origin, destination;
 int seats;
 double price;

 public Flight(int id, String flightNo, String origin, String destination, int seats, double price) {
     this.id = id;
     this.flightNo = flightNo;
     this.origin = origin;
     this.destination = destination;
     this.seats = seats;
     this.price = price;
 }

 public void display() {
     System.out.printf("%-6s | %-10s | %-10s | %-4d | ₹%.2f\n",
             flightNo, origin, destination, seats, price);
 }
}

//------------------ RESERVATION SYSTEM ------------------
class ReservationSystem {
 private Connection conn;

 public ReservationSystem() throws Exception {
     conn = DBConnection.getConnection();
 }

 public void showFlights() throws Exception {
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT * FROM flights");
     System.out.println("\n===== AVAILABLE FLIGHTS =====");
     System.out.printf("%-6s | %-10s | %-10s | %-4s | %s\n", "Flight", "From", "To", "Seat", "Price");
     System.out.println("---------------------------------------------");
     while (rs.next()) {
         System.out.printf("%-6s | %-10s | %-10s | %-4d | ₹%.2f\n",
                 rs.getString("flight_no"),
                 rs.getString("origin"),
                 rs.getString("destination"),
                 rs.getInt("seats"),
                 rs.getDouble("price"));
     }
 }

 public void bookTicket() throws Exception {
     Scanner sc = new Scanner(System.in);

     showFlights();
     System.out.print("\nEnter Flight Number to Book: ");
     String flightNo = sc.nextLine();

     // find flight
     PreparedStatement ps = conn.prepareStatement("SELECT * FROM flights WHERE flight_no=?");
     ps.setString(1, flightNo);
     ResultSet rs = ps.executeQuery();

     if (rs.next()) {
         int flightId = rs.getInt("flight_id");
         int availableSeats = rs.getInt("seats");

         if (availableSeats <= 0) {
             System.out.println("❌ Sorry, no seats available on this flight!");
             return;
         }

         System.out.print("Enter Passenger Name: ");
         String name = sc.nextLine();
         System.out.print("Enter Age: ");
         int age = sc.nextInt();
         sc.nextLine(); // clear buffer
         System.out.print("Enter Gender: ");
         String gender = sc.nextLine();

         // insert passenger
         PreparedStatement ps1 = conn.prepareStatement(
                 "INSERT INTO passengers (name, age, gender) VALUES (?, ?, ?)",
                 Statement.RETURN_GENERATED_KEYS);
         ps1.setString(1, name);
         ps1.setInt(2, age);
         ps1.setString(3, gender);
         ps1.executeUpdate();

         ResultSet keys = ps1.getGeneratedKeys();
         keys.next();
         int passengerId = keys.getInt(1);

         // insert reservation
         PreparedStatement ps2 = conn.prepareStatement(
                 "INSERT INTO reservations (passenger_id, flight_id) VALUES (?, ?)");
         ps2.setInt(1, passengerId);
         ps2.setInt(2, flightId);
         ps2.executeUpdate();

         // update flight seat
         PreparedStatement ps3 = conn.prepareStatement(
                 "UPDATE flights SET seats=seats-1 WHERE flight_id=?");
         ps3.setInt(1, flightId);
         ps3.executeUpdate();

         System.out.println("\n✅ Ticket Booked Successfully!");
         System.out.println("----- TICKET DETAILS -----");
         System.out.println("Passenger: " + name);
         System.out.println("Flight: " + flightNo);
         System.out.println("From: " + rs.getString("origin") + " → To: " + rs.getString("destination"));
         System.out.println("Price: ₹" + rs.getDouble("price"));
         System.out.println("--------------------------");
     } else {
         System.out.println("⚠️ Flight not found!");
     }
 }

 public void viewReservations() throws Exception {
     String query = """
             SELECT r.reservation_id, p.name, f.flight_no, f.origin, f.destination, f.price
             FROM reservations r
             JOIN passengers p ON r.passenger_id = p.passenger_id
             JOIN flights f ON r.flight_id = f.flight_id
             """;
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery(query);
     System.out.println("\n===== ALL RESERVATIONS =====");
     while (rs.next()) {
         System.out.printf("Reservation #%d | %s | Flight: %s | %s → %s | ₹%.2f\n",
                 rs.getInt("reservation_id"),
                 rs.getString("name"),
                 rs.getString("flight_no"),
                 rs.getString("origin"),
                 rs.getString("destination"),
                 rs.getDouble("price"));
     }
 }
}

//------------------ MAIN CLASS ------------------
public class airline_projecy{
 public static void main(String[] args) {
     try {
         ReservationSystem system = new ReservationSystem();
         Scanner sc = new Scanner(System.in);

         while (true) {
             System.out.println("\n====== AIRLINE RESERVATION SYSTEM ======");
             System.out.println("1. View Available Flights");
             System.out.println("2. Book a Ticket");
             System.out.println("3. View All Reservations");
             System.out.println("4. Exit");
             System.out.print("Enter your choice: ");
             int choice = sc.nextInt();
             sc.nextLine(); // clear buffer

             switch (choice) {
                 case 1 -> system.showFlights();
                 case 2 -> system.bookTicket();
                 case 3 -> system.viewReservations();
                 case 4 -> {
                     System.out.println("👋 Thank you for using the system!");
                     return;
                 }
                 default -> System.out.println("⚠️ Invalid choice! Try again.");
             }
         }
     } catch (Exception e) {
         e.printStackTrace();
     }
 }
}
