import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// ------------------ DATABASE CONNECTION ------------------
class DBConnection {
    public static Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/airline_system";
        String user = "root";   // your MySQL username
        String pass = "12345";  // your MySQL password
        return DriverManager.getConnection(url, user, pass);
    }
}

// ------------------ MAIN GUI CLASS ------------------
public class AirlineReservationSystemUI extends JFrame {
    private JTable flightTable;
    private DefaultTableModel flightModel;
    private JButton bookBtn, refreshBtn, viewReservationsBtn;
    private Connection conn;

    public AirlineReservationSystemUI() {
        setTitle("✈ Airline Reservation System");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        try {
            conn = DBConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "DB Connection Failed: " + e.getMessage());
            System.exit(0);
        }

        // ------------------ FLIGHT TABLE ------------------
        flightModel = new DefaultTableModel(new String[]{"Flight No", "From", "To", "Seats", "Price"}, 0);
        flightTable = new JTable(flightModel);
        refreshFlights();

        JScrollPane scrollPane = new JScrollPane(flightTable);
        add(scrollPane, BorderLayout.CENTER);

        // ------------------ BUTTON PANEL ------------------
        JPanel buttonPanel = new JPanel();
        bookBtn = new JButton("Book Ticket");
        refreshBtn = new JButton("Refresh Flights");
        viewReservationsBtn = new JButton("View Reservations");

        buttonPanel.add(bookBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(viewReservationsBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // ------------------ ACTIONS ------------------
        refreshBtn.addActionListener(e -> refreshFlights());
        bookBtn.addActionListener(e -> bookTicket());
        viewReservationsBtn.addActionListener(e -> viewReservations());
    }

    // ------------------ REFRESH FLIGHTS ------------------
    private void refreshFlights() {
        try {
            flightModel.setRowCount(0);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM flights");
            while (rs.next()) {
                flightModel.addRow(new Object[]{
                        rs.getString("flight_no"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getInt("seats"),
                        "₹" + rs.getDouble("price")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Loading Flights: " + e.getMessage());
        }
    }

    // ------------------ BOOK TICKET ------------------
    private void bookTicket() {
        int selectedRow = flightTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a flight first!");
            return;
        }

        String flightNo = (String) flightModel.getValueAt(selectedRow, 0);

        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM flights WHERE flight_no=?");
            ps.setString(1, flightNo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int flightId = rs.getInt("flight_id");
                int availableSeats = rs.getInt("seats");
                double price = rs.getDouble("price");

                if (availableSeats <= 0) {
                    JOptionPane.showMessageDialog(this, "No seats available!");
                    return;
                }

                JTextField nameField = new JTextField();
                JTextField ageField = new JTextField();
                JTextField genderField = new JTextField();

                Object[] message = {
                        "Passenger Name:", nameField,
                        "Age:", ageField,
                        "Gender:", genderField
                };

                int option = JOptionPane.showConfirmDialog(this, message, "Enter Passenger Details", JOptionPane.OK_CANCEL_OPTION);

                if (option == JOptionPane.OK_OPTION) {
                    String name = nameField.getText();
                    int age = Integer.parseInt(ageField.getText());
                    String gender = genderField.getText();

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

                    // update seat count
                    PreparedStatement ps3 = conn.prepareStatement(
                            "UPDATE flights SET seats=seats-1 WHERE flight_id=?");
                    ps3.setInt(1, flightId);
                    ps3.executeUpdate();

                    JOptionPane.showMessageDialog(this,
                            "✅ Ticket Booked Successfully!\n" +
                            "Passenger: " + name + "\nFlight: " + flightNo +
                            "\nPrice: ₹" + price,
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    refreshFlights();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Booking Ticket: " + e.getMessage());
        }
    }

    // ------------------ VIEW RESERVATIONS ------------------
    private void viewReservations() {
        try {
            JFrame viewFrame = new JFrame("🧾 All Reservations");
            viewFrame.setSize(700, 400);
            viewFrame.setLocationRelativeTo(this);

            DefaultTableModel model = new DefaultTableModel(new String[]{
                    "Reservation ID", "Passenger", "Flight No", "From", "To", "Price"
            }, 0);

            JTable table = new JTable(model);

            String query = """
                    SELECT r.reservation_id, p.name, f.flight_no, f.origin, f.destination, f.price
                    FROM reservations r
                    JOIN passengers p ON r.passenger_id = p.passenger_id
                    JOIN flights f ON r.flight_id = f.flight_id
                    """;

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("reservation_id"),
                        rs.getString("name"),
                        rs.getString("flight_no"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        "₹" + rs.getDouble("price")
                });
            }

            JScrollPane pane = new JScrollPane(table);
            viewFrame.add(pane);
            viewFrame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error Viewing Reservations: " + e.getMessage());
        }
    }

    // ------------------ MAIN ------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AirlineReservationSystemUI().setVisible(true));
    }
}
