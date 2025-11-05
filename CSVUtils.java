import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVUtils {

    // Reads a CSV file and returns a list of maps (each row is a map of columnName -> value)
    public static List<Map<String, String>> readCSV(String filePath) throws IOException {
        List<Map<String, String>> flights = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String headerLine = br.readLine(); // Read the first line (headers)
            if (headerLine == null) {
                throw new IOException("CSV file is empty!");
            }

            // Split header into individual column names
            String[] headers = headerLine.split(",");

            String line;
            while ((line = br.readLine()) != null) {
                // Ignore empty lines
                if (line.trim().isEmpty()) continue;

                // Split each row into values
                String[] values = line.split(",");

                // Create a map to store the column -> value pair
                Map<String, String> row = new HashMap<>();

                // Match each column header with its corresponding value
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }

                flights.add(row);
            }
        }

        return flights;
    }

    // Optional: for quick testing of CSV reading
    public static void main(String[] args) {
        try {
            List<Map<String, String>> data = readCSV("flights.csv");
            for (Map<String, String> row : data) {
                System.out.println(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
