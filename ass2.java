// Abstract Logger Example
abstract class Logger {
    // Abstract method
    public abstract void log(String message);
}

// Console Logger class
class ConsoleLogger extends Logger {
    @Override
    public void log(String message) {
        System.out.println("Console Log: " + message);
    }
}

// File Logger class
import java.io.FileWriter;
import java.io.IOException;

class FileLogger extends Logger {
    @Override
    public void log(String message) {
        try {
            FileWriter writer = new FileWriter("log.txt", true);
            writer.write("File Log: " + message + "\n");
            writer.close();
            System.out.println("Message logged to file successfully!");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}

// Main Class
public class LoggerDemo {
    public static void main(String[] args) {
        // Create objects
        Logger console = new ConsoleLogger();
        Logger file = new FileLogger();

        // Logging messages
        console.log("This is a console log message.");
        file.log("This is a file log message.");
    }
}
