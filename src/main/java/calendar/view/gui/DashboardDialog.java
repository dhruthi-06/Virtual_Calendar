package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import calendar.util.CalendarAnalytics;
import calendar.util.CalendarAnalytics.AnalyticsResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Dialog for displaying calendar analytics dashboard.
 */
public class DashboardDialog extends JDialog {

  private final CalendarGuiController controller;
  private JTextField startDateField;
  private JTextField endDateField;
  private JPanel resultsPanel;
  private JLabel errorLabel;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * Constructs the DashboardDialog.
   *
   * @param parent the parent JFrame
   * @param controller the application controller
   */
  public DashboardDialog(JFrame parent, CalendarGuiController controller) {
    super(parent, "Calendar Dashboard", true);
    this.controller = controller;
    initializeUi();
  }

  private void initializeUi() {
    setSize(800, 700);
    setLocationRelativeTo(getParent());
    setLayout(new BorderLayout(15, 15));

    JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
    mainContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    mainContainer.add(createInputPanel(), BorderLayout.NORTH);
    mainContainer.add(createResultsPanel(), BorderLayout.CENTER);
    mainContainer.add(createButtonPanel(), BorderLayout.SOUTH);

    add(mainContainer);
  }

  private JPanel createInputPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Date Range"));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.anchor = GridBagConstraints.WEST;


    gbc.gridx = 0;
    gbc.gridy = 0;
    panel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
    gbc.gridx = 1;
    startDateField = new JTextField(15);
    startDateField.setText(LocalDate.now().minusMonths(1).format(DATE_FORMATTER));
    panel.add(startDateField, gbc);


    gbc.gridx = 0;
    gbc.gridy = 1;
    panel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
    gbc.gridx = 1;
    endDateField = new JTextField(15);
    endDateField.setText(LocalDate.now().format(DATE_FORMATTER));
    panel.add(endDateField, gbc);


    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    errorLabel = new JLabel(" ");
    errorLabel.setForeground(Color.RED);
    errorLabel.setFont(errorLabel.getFont().deriveFont(Font.PLAIN, 11f));
    panel.add(errorLabel, gbc);

    return panel;
  }

  private JPanel createResultsPanel() {
    JPanel container = new JPanel(new BorderLayout());
    container.setBorder(BorderFactory.createTitledBorder("Analytics Results"));

    resultsPanel = new JPanel();
    resultsPanel.setLayout(new GridBagLayout());
    resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JScrollPane scrollPane = new JScrollPane(resultsPanel);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(new Dimension(750, 450));

    container.add(scrollPane, BorderLayout.CENTER);

    return container;
  }

  private JPanel createButtonPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(10, 0));

    JButton calculateButton = new JButton("Calculate Analytics");
    calculateButton.setBackground(new Color(66, 133, 244));
    calculateButton.setForeground(Color.WHITE);
    calculateButton.setFont(calculateButton.getFont().deriveFont(Font.BOLD, 12f));
    calculateButton.addActionListener(e -> calculateAndDisplay());

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dispose());

    panel.add(calculateButton, BorderLayout.WEST);
    panel.add(closeButton, BorderLayout.EAST);

    return panel;
  }

  private void calculateAndDisplay() {
    errorLabel.setText(" ");

    String startDateStr = startDateField.getText().trim();
    String endDateStr = endDateField.getText().trim();

    if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
      errorLabel.setText("Please enter both start and end dates");
      return;
    }

    LocalDate startDate;
    LocalDate endDate;

    try {
      startDate = LocalDate.parse(startDateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      errorLabel.setText("Invalid start date format. Use YYYY-MM-DD");
      return;
    }

    try {
      endDate = LocalDate.parse(endDateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      errorLabel.setText("Invalid end date format. Use YYYY-MM-DD");
      return;
    }

    if (startDate.isAfter(endDate)) {
      errorLabel.setText("Start date cannot be after end date");
      return;
    }

    AnalyticsResult analytics = controller.getAnalytics(startDate, endDate);
    if (analytics == null) {
      errorLabel.setText("Failed to calculate analytics. Please check if a calendar is selected.");
      return;
    }

    displayAnalytics(analytics, startDate, endDate);
  }

  private void displayAnalytics(AnalyticsResult analytics, LocalDate startDate, LocalDate endDate) {
    resultsPanel.removeAll();

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.gridx = 0;
    gbc.gridy = 0;

    Font headerFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    final Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);


    JLabel header = new JLabel("Calendar Dashboard - " + startDate.format(DATE_FORMATTER)
        + " to " + endDate.format(DATE_FORMATTER));
    header.setFont(headerFont);
    gbc.gridwidth = 2;
    resultsPanel.add(header, gbc);
    gbc.gridy++;
    gbc.gridwidth = 1;

    addSpacer(gbc, 10);


    addMetricLabel(gbc, "Total number of events:", labelFont);
    addMetricValue(gbc, String.valueOf(analytics.getTotalEvents()));
    addSpacer(gbc, 5);

    addSectionHeader(gbc, "Events by Subject:", headerFont);
    var eventsBySubject = analytics.getEventsBySubject();
    if (eventsBySubject.isEmpty()) {
      addMetricValue(gbc, "(none)");
    } else {
      eventsBySubject.entrySet().stream()
          .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
          .forEach(entry -> {
            addMetricLabel(gbc, "  " + entry.getKey() + ":", labelFont);
            addMetricValue(gbc, String.valueOf(entry.getValue()));
          });
    }
    addSpacer(gbc, 5);

    addSectionHeader(gbc, "Events by Weekday:", headerFont);
    String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
        "Saturday", "Sunday"};
    for (String day : weekdays) {
      int count = analytics.getEventsByWeekday().getOrDefault(day, 0);
      addMetricLabel(gbc, "  " + day + ":", labelFont);
      addMetricValue(gbc, String.valueOf(count));
    }
    addSpacer(gbc, 5);


    addSectionHeader(gbc, "Events by Week:", headerFont);
    var eventsByWeek = analytics.getEventsByWeek();
    if (eventsByWeek.isEmpty()) {
      addMetricValue(gbc, "(none)");
    } else {
      eventsByWeek.entrySet().stream()
          .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
          .forEach(entry -> {
            addMetricLabel(gbc, "  Week " + entry.getKey() + ":", labelFont);
            addMetricValue(gbc, String.valueOf(entry.getValue()));
          });
    }
    addSpacer(gbc, 5);


    addSectionHeader(gbc, "Events by Month:", headerFont);
    var eventsByMonth = analytics.getEventsByMonth();
    if (eventsByMonth.isEmpty()) {
      addMetricValue(gbc, "(none)");
    } else {
      String[] monthNames = {"January", "February", "March", "April", "May", "June",
          "July", "August", "September", "October", "November", "December"};
      eventsByMonth.entrySet().stream()
          .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
          .forEach(entry -> {
            String monthName = monthNames[entry.getKey() - 1];
            addMetricLabel(gbc, "  " + monthName + ":", labelFont);
            addMetricValue(gbc, String.valueOf(entry.getValue()));
          });
    }
    addSpacer(gbc, 5);


    addMetricLabel(gbc, "Average events per day:", labelFont);
    addMetricValue(gbc, String.format("%.2f", analytics.getAverageEventsPerDay()));
    addSpacer(gbc, 5);


    final LocalDate busiestDay = analytics.getBusiestDay();
    final LocalDate leastBusyDay = analytics.getLeastBusyDay();
    addMetricLabel(gbc, "Busiest day:", labelFont);
    addMetricValue(gbc, busiestDay != null ? busiestDay.format(DATE_FORMATTER) : "(none)");
    addMetricLabel(gbc, "Least busy day:", labelFont);
    addMetricValue(gbc, leastBusyDay != null ? leastBusyDay.format(DATE_FORMATTER) : "(none)");
    addSpacer(gbc, 5);


    addMetricLabel(gbc, "Online events:", labelFont);
    addMetricValue(gbc, String.format("%.2f%%", analytics.getOnlinePercentage()));
    addMetricLabel(gbc, "Not online events:", labelFont);
    addMetricValue(gbc, String.format("%.2f%%", analytics.getOfflinePercentage()));

    resultsPanel.revalidate();
    resultsPanel.repaint();
  }

  private void addSectionHeader(GridBagConstraints gbc, String text, Font font) {
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    JLabel label = new JLabel(text);
    label.setFont(font);
    label.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
    resultsPanel.add(label, gbc);
    gbc.gridy++;
    gbc.gridwidth = 1;
  }

  private void addMetricLabel(GridBagConstraints gbc, String text, Font font) {
    gbc.gridx = 0;
    JLabel label = new JLabel(text);
    label.setFont(font);
    resultsPanel.add(label, gbc);
  }

  private void addMetricValue(GridBagConstraints gbc, String text) {
    gbc.gridx = 1;
    JLabel label = new JLabel(text);
    label.setFont(label.getFont().deriveFont(Font.PLAIN, 12f));
    resultsPanel.add(label, gbc);
    gbc.gridy++;
  }

  private void addSpacer(GridBagConstraints gbc, int height) {
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    resultsPanel.add(new JLabel(" "), gbc);
    gbc.gridy++;
    gbc.gridwidth = 1;
  }
}

