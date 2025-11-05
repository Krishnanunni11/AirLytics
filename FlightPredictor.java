import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class FlightPredictor extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField filterText;
    private JLabel totalFlightsLabel;
    private JLabel delayedFlightsLabel;
    private JProgressBar progressBar;
    private JButton uploadBtn;
    private JComboBox<String> statusDropdown;

    public FlightPredictor() {
        setTitle("Flight Delay Predictor");
        setSize(900, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        uploadBtn = new JButton("Upload Flight CSV");
        filterText = new JTextField(20);
        JLabel filterLabel = new JLabel("Filter by Airline:");

        JLabel statusLabel = new JLabel("Filter by Status:");
        String[] statusOptions = {"All", "DELAYED", "ON TIME"};
        statusDropdown = new JComboBox<>(statusOptions);

        topPanel.add(uploadBtn);
        topPanel.add(filterLabel);
        topPanel.add(filterText);
        topPanel.add(statusLabel);
        topPanel.add(statusDropdown);

        String[] columns = {"Flight No", "Airline", "Origin", "Destination", "Dep Hour", "Weather", "Prediction"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setDefaultRenderer(Object.class, new DelayStatusCellRenderer());
        JScrollPane tableScroll = new JScrollPane(table);

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalFlightsLabel = new JLabel("Total Flights: 0");
        delayedFlightsLabel = new JLabel("Delayed Flights: 0");
        summaryPanel.add(totalFlightsLabel);
        summaryPanel.add(Box.createHorizontalStrut(20));
        summaryPanel.add(delayedFlightsLabel);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);

        setLayout(new BorderLayout(10, 10));
        add(topPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(summaryPanel, BorderLayout.WEST);
        bottomPanel.add(progressBar, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        uploadBtn.addActionListener(e -> openFileAndPredict());

        filterText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        statusDropdown.addActionListener(e -> applyFilter());
    }

    private void applyFilter() {
        String airlineText = filterText.getText();
        String selectedStatus = (String) statusDropdown.getSelectedItem();

        RowFilter<DefaultTableModel, Object> airlineFilter = null;
        RowFilter<DefaultTableModel, Object> statusFilter = null;

        if (!airlineText.trim().isEmpty()) {
            airlineFilter = RowFilter.regexFilter("(?i)" + airlineText, 1);
        }

        if ("DELAYED".equals(selectedStatus)) {
            statusFilter = RowFilter.regexFilter("DELAYED", 6);
        } else if ("ON TIME".equals(selectedStatus)) {
            statusFilter = RowFilter.regexFilter("ON TIME", 6);
        }

        if (airlineFilter != null && statusFilter != null) {
            sorter.setRowFilter(RowFilter.andFilter(List.of(airlineFilter, statusFilter)));
        } else if (airlineFilter != null) {
            sorter.setRowFilter(airlineFilter);
        } else if (statusFilter != null) {
            sorter.setRowFilter(statusFilter);
        } else {
            sorter.setRowFilter(null);
        }

        updateSummaryLabels();
    }

    private void openFileAndPredict() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            PredictionWorker worker = new PredictionWorker(file);
            worker.addPropertyChangeListener(evt -> {
                if ("progress".equals(evt.getPropertyName())) {
                    int progress = (Integer) evt.getNewValue();
                    progressBar.setValue(progress);
                }
            });
            worker.execute();
        }
    }

    private void updateSummaryLabels() {
        int total = sorter.getViewRowCount();
        int delayedCount = 0;
        for (int i = 0; i < total; i++) {
            int modelRow = table.convertRowIndexToModel(i);
            String prediction = (String) tableModel.getValueAt(modelRow, 6);
            if ("DELAYED".equals(prediction)) {
                delayedCount++;
            }
        }
        totalFlightsLabel.setText("Total Flights: " + total);
        delayedFlightsLabel.setText("Delayed Flights: " + delayedCount);
    }

    private class DelayStatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
            int modelRow = tbl.convertRowIndexToModel(row);
            String prediction = (String) tableModel.getValueAt(modelRow, 6);
            if ("DELAYED".equals(prediction)) {
                comp.setBackground(new Color(255, 102, 102));
            } else if ("ON TIME".equals(prediction)) {
                comp.setBackground(new Color(153, 255, 153));
            } else {
                comp.setBackground(Color.WHITE);
            }
            if (isSelected) {
                comp.setBackground(comp.getBackground().darker());
            }
            return comp;
        }
    }

    private class PredictionWorker extends SwingWorker<Void, Void> {
        private final File file;

        public PredictionWorker(File file) {
            this.file = file;
        }

        @Override
        protected Void doInBackground() throws Exception {
            List<Map<String, String>> flights = CSVUtils.readCSV(file.getAbsolutePath());
            tableModel.setRowCount(0);
            SwingUtilities.invokeLater(() -> {
                progressBar.setVisible(true);
                progressBar.setValue(0);
            });

            int total = flights.size();
            int count = 0;

            for (Map<String, String> flight : flights) {
                boolean delayed = FlightPredictor.callPredictionAPI(flight);
                SwingUtilities.invokeLater(() -> {
                    tableModel.addRow(new Object[]{
                            flight.get("flight_no"),
                            flight.get("airline"),
                            flight.get("origin"),
                            flight.get("destination"),
                            flight.get("dep_hour"),
                            flight.get("weather"),
                            delayed ? "DELAYED" : "ON TIME"
                    });
                });
                count++;
                int progress = (int) ((count / (double) total) * 100);
                setProgress(progress);
            }
            return null;
        }

        @Override
        protected void done() {
            SwingUtilities.invokeLater(() -> progressBar.setVisible(false));
            updateSummaryLabels();
            try {
                get();
            } catch (InterruptedException | ExecutionException e) {
                JOptionPane.showMessageDialog(FlightPredictor.this, "Error: " + e.getCause().getMessage());
            }
        }
    }

    public static boolean callPredictionAPI(Map<String, String> flightData) throws Exception {
        URL url = new URL("http://localhost:5000/predict");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");

        JSONObject json = new JSONObject();
        json.put("airline", flightData.get("airline"));
        json.put("origin", flightData.get("origin"));
        json.put("destination", flightData.get("destination"));
        json.put("dep_hour", Integer.parseInt(flightData.get("dep_hour")));
        json.put("weather", flightData.get("weather"));

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = json.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        InputStream response = conn.getInputStream();
        JSONTokener tokener = new JSONTokener(response);
        JSONObject resJson = new JSONObject(tokener);
        return resJson.getBoolean("delayed");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlightPredictor().setVisible(true));
    }
}






