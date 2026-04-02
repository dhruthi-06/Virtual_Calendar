package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Dialog window for creating a new single or recurring event.
 */
public class CreateEventDialog extends JDialog {

  private final CalendarGuiController controller;
  private final LocalDate selectedDate;

  private JTextField subjectField;
  private JLabel subjectErrorLabel;
  private JSpinner startHourSpinner;
  private JSpinner startMinuteSpinner;
  private JSpinner endHourSpinner;
  private JSpinner endMinuteSpinner;
  private JLabel timeErrorLabel;
  private JTextArea descriptionArea;
  private JTextField locationField;
  private JRadioButton publicRadio;
  private JRadioButton privateRadio;

  private JCheckBox isRecurringCheckBox;
  private JPanel recurringPanel;
  private JCheckBox[] weekdayCheckboxes;
  private JLabel recurringErrorLabel;
  private JRadioButton countRadio;
  private JRadioButton untilRadio;
  private JSpinner repeatCountSpinner;
  private JComboBox<Integer> untilYearCombo;
  private JComboBox<Integer> untilMonthCombo;
  private JComboBox<Integer> untilDayCombo;

  /**
   * Constructs the CreateEventDialog.
   *
   * @param parent the parent JFrame.
   * @param controller the application controller.
   * @param selectedDate the date the user has selected in the main calendar view.
   */
  public CreateEventDialog(JFrame parent, CalendarGuiController controller,
                           LocalDate selectedDate) {
    super(parent, "Create Event", true);
    this.controller = controller;
    this.selectedDate = selectedDate;
    initializeUi();
    addValidationListeners();
  }

  private void initializeUi() {
    setSize(600, 800);
    setLocationRelativeTo(getParent());
    setLayout(new BorderLayout(15, 15));

    JPanel mainContainer = createMainContainer();
    add(mainContainer);
  }

  private JPanel createMainContainer() {
    JPanel container = new JPanel(new BorderLayout(10, 10));
    container.setBorder(new EmptyBorder(20, 20, 20, 20));
    container.setBackground(Color.WHITE);

    container.add(createHeader(), BorderLayout.NORTH);
    container.add(createScrollableContent(), BorderLayout.CENTER);
    container.add(createButtons(), BorderLayout.SOUTH);

    return container;
  }

  private JPanel createHeader() {
    String dayName = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    String dateStr = selectedDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        + " " + selectedDate.getDayOfMonth() + ", " + selectedDate.getYear();

    return DialogComponentFactory.createHeaderPanel(
        "New Event - " + dayName + ", " + dateStr,
        new Color(66, 133, 244));
  }

  private JScrollPane createScrollableContent() {
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(Color.WHITE);

    contentPanel.add(buildSubjectSection());
    contentPanel.add(Box.createVerticalStrut(10));
    contentPanel.add(buildTimeSection());
    contentPanel.add(Box.createVerticalStrut(15));
    contentPanel.add(buildDetailsSection());
    contentPanel.add(Box.createVerticalStrut(15));
    contentPanel.add(buildRecurringSection());

    JScrollPane scrollPane = new JScrollPane(contentPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  private JPanel buildSubjectSection() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(createTitledBorder("Event Information"));
    panel.setBackground(Color.WHITE);

    GridBagConstraints gbc = createGbc(0, 0, 0.3);
    panel.add(createBoldLabel("Subject:"), gbc);

    gbc = createGbc(1, 0, 0.7);
    subjectField = new JTextField();
    subjectField.setFont(new Font("Arial", Font.PLAIN, 13));
    subjectField.setPreferredSize(new Dimension(350, 32));
    panel.add(subjectField, gbc);

    gbc = createGbc(1, 1, 0.7);
    gbc.insets = new Insets(2, 15, 10, 15);
    subjectErrorLabel = createErrorLabel();
    panel.add(subjectErrorLabel, gbc);

    return panel;
  }

  private JPanel buildTimeSection() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(createTitledBorder("Schedule"));
    panel.setBackground(Color.WHITE);

    GridBagConstraints gbc = createGbc(0, 0, 0.3);
    panel.add(createBoldLabel("Start Time:"), gbc);

    gbc = createGbc(1, 0, 0.7);
    panel.add(createTimeInputPanel(true), gbc);

    gbc = createGbc(0, 1, 0.3);
    panel.add(createBoldLabel("End Time:"), gbc);

    gbc = createGbc(1, 1, 0.7);
    gbc.insets = new Insets(5, 15, 3, 15);
    panel.add(createTimeInputPanel(false), gbc);

    gbc = createGbc(1, 2, 0.7);
    gbc.insets = new Insets(2, 15, 10, 15);
    timeErrorLabel = createErrorLabel();
    panel.add(timeErrorLabel, gbc);

    return panel;
  }

  /**
   * Creates the panel containing hour and minute spinners for time input.
   *
   * @param isStart true for start time spinners, false for end time spinners.
   * @return the time input JPanel.
   */
  private JPanel createTimeInputPanel(boolean isStart) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    panel.setBackground(Color.WHITE);

    if (isStart) {
      startHourSpinner = new JSpinner(new SpinnerNumberModel(9, 0, 23, 1));
      startMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
      configureSpinner(startHourSpinner);
      configureSpinner(startMinuteSpinner);
      panel.add(startHourSpinner);
      panel.add(createColonLabel());
      panel.add(startMinuteSpinner);
    } else {
      endHourSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 23, 1));
      endMinuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 15));
      configureSpinner(endHourSpinner);
      configureSpinner(endMinuteSpinner);
      panel.add(endHourSpinner);
      panel.add(createColonLabel());
      panel.add(endMinuteSpinner);
    }

    return panel;
  }

  private void configureSpinner(JSpinner spinner) {
    spinner.setFont(new Font("Arial", Font.PLAIN, 14));
    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
  }

  private JLabel createColonLabel() {
    JLabel colon = new JLabel(":");
    colon.setFont(new Font("Arial", Font.BOLD, 16));
    return colon;
  }

  private JPanel buildDetailsSection() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(createTitledBorder("Additional Details (Optional)"));
    panel.setBackground(Color.WHITE);

    GridBagConstraints gbc = createGbc(0, 0, 0.3);
    panel.add(createBoldLabel("Location:"), gbc);

    gbc = createGbc(1, 0, 0.7);
    locationField = new JTextField();
    locationField.setFont(new Font("Arial", Font.PLAIN, 13));
    locationField.setPreferredSize(new Dimension(350, 32));
    panel.add(locationField, gbc);

    gbc = createGbc(0, 1, 0.3);
    gbc.anchor = GridBagConstraints.NORTHWEST;
    panel.add(createBoldLabel("Description:"), gbc);

    gbc = createGbc(1, 1, 0.7);
    gbc.anchor = GridBagConstraints.CENTER;
    panel.add(createDescriptionArea(), gbc);

    gbc = createGbc(0, 2, 0.3);
    gbc.anchor = GridBagConstraints.WEST;
    panel.add(createBoldLabel("Visibility:"), gbc);

    gbc = createGbc(1, 2, 0.7);
    panel.add(createVisibilityPanel(), gbc);

    return panel;
  }

  private JScrollPane createDescriptionArea() {
    descriptionArea = new JTextArea(3, 20);
    descriptionArea.setFont(new Font("Arial", Font.PLAIN, 13));
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

    JScrollPane scrollPane = new JScrollPane(descriptionArea);
    scrollPane.setPreferredSize(new Dimension(350, 80));
    return scrollPane;
  }

  private JPanel createVisibilityPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
    panel.setBackground(Color.WHITE);

    publicRadio = new JRadioButton("Public", true);
    publicRadio.setFont(new Font("Arial", Font.PLAIN, 13));
    publicRadio.setBackground(Color.WHITE);

    privateRadio = new JRadioButton("Private");
    privateRadio.setFont(new Font("Arial", Font.PLAIN, 13));
    privateRadio.setBackground(Color.WHITE);

    final ButtonGroup group = new ButtonGroup();
    group.add(publicRadio);
    group.add(privateRadio);

    panel.add(publicRadio);
    panel.add(privateRadio);

    return panel;
  }

  private JPanel buildRecurringSection() {
    JPanel container = new JPanel();
    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
    container.setBackground(Color.WHITE);

    JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    checkboxPanel.setBackground(Color.WHITE);
    checkboxPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

    isRecurringCheckBox = new JCheckBox("Make this a recurring event");
    isRecurringCheckBox.setFont(new Font("Arial", Font.BOLD, 13));
    isRecurringCheckBox.setBackground(Color.WHITE);
    isRecurringCheckBox.addActionListener(e -> toggleRecurringPanel());
    checkboxPanel.add(isRecurringCheckBox);
    container.add(checkboxPanel);

    recurringPanel = buildRecurringOptions();
    recurringPanel.setVisible(false);
    container.add(recurringPanel);

    return container;
  }

  private JPanel buildRecurringOptions() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
        new EmptyBorder(15, 15, 15, 15)));
    panel.setBackground(new Color(250, 250, 250));

    panel.add(createWeekdayPanel());
    panel.add(createRecurringErrorPanel());
    panel.add(Box.createVerticalStrut(5));
    panel.add(createFrequencyLabel());
    panel.add(Box.createVerticalStrut(5));
    panel.add(createCountPanel());
    panel.add(Box.createVerticalStrut(5));
    panel.add(createUntilPanel());

    return panel;
  }

  private JPanel createWeekdayPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    panel.setBackground(new Color(250, 250, 250));

    JLabel label = new JLabel("Repeat on:");
    label.setFont(new Font("Arial", Font.BOLD, 13));
    panel.add(label);

    String[] weekdayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    weekdayCheckboxes = new JCheckBox[7];
    for (int i = 0; i < 7; i++) {
      weekdayCheckboxes[i] = new JCheckBox(weekdayNames[i]);
      weekdayCheckboxes[i].setFont(new Font("Arial", Font.PLAIN, 12));
      weekdayCheckboxes[i].setBackground(new Color(250, 250, 250));
      weekdayCheckboxes[i].addActionListener(e -> validateRecurring());
      panel.add(weekdayCheckboxes[i]);
    }

    return panel;
  }

  private JPanel createRecurringErrorPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBackground(new Color(250, 250, 250));
    recurringErrorLabel = createErrorLabel();
    panel.add(recurringErrorLabel);
    return panel;
  }

  private JPanel createFrequencyLabel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBackground(new Color(250, 250, 250));
    JLabel label = new JLabel("Ends:");
    label.setFont(new Font("Arial", Font.BOLD, 13));
    panel.add(label);
    return panel;
  }

  private JPanel createCountPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    panel.setBackground(new Color(250, 250, 250));

    final ButtonGroup group = new ButtonGroup();
    countRadio = new JRadioButton("After", true);
    countRadio.setFont(new Font("Arial", Font.PLAIN, 13));
    countRadio.setBackground(new Color(250, 250, 250));
    group.add(countRadio);

    repeatCountSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
    repeatCountSpinner.setFont(new Font("Arial", Font.PLAIN, 13));
    ((JSpinner.DefaultEditor) repeatCountSpinner.getEditor()).getTextField().setColumns(3);

    panel.add(countRadio);
    panel.add(repeatCountSpinner);
    panel.add(new JLabel("occurrences"));

    return panel;
  }

  private JPanel createUntilPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    panel.setBackground(new Color(250, 250, 250));

    final ButtonGroup group = new ButtonGroup();
    if (countRadio != null) {
      group.add(countRadio);
    }

    untilRadio = new JRadioButton("On");
    untilRadio.setFont(new Font("Arial", Font.PLAIN, 13));
    untilRadio.setBackground(new Color(250, 250, 250));
    group.add(untilRadio);
    panel.add(untilRadio);

    LocalDate futureDate = LocalDate.now().plusMonths(1);

    Integer[] years = new Integer[10];
    for (int i = 0; i < 10; i++) {
      years[i] = futureDate.getYear() + i;
    }
    untilYearCombo = new JComboBox<>(years);
    untilYearCombo.setFont(new Font("Arial", Font.PLAIN, 13));

    Integer[] months = new Integer[12];
    for (int i = 0; i < 12; i++) {
      months[i] = i + 1;
    }
    untilMonthCombo = new JComboBox<>(months);
    untilMonthCombo.setSelectedItem(futureDate.getMonthValue());
    untilMonthCombo.setFont(new Font("Arial", Font.PLAIN, 13));

    Integer[] days = new Integer[31];
    for (int i = 0; i < 31; i++) {
      days[i] = i + 1;
    }
    untilDayCombo = new JComboBox<>(days);
    untilDayCombo.setSelectedItem(futureDate.getDayOfMonth());
    untilDayCombo.setFont(new Font("Arial", Font.PLAIN, 13));

    panel.add(untilYearCombo);
    panel.add(new JLabel("-"));
    panel.add(untilMonthCombo);
    panel.add(new JLabel("-"));
    panel.add(untilDayCombo);

    return panel;
  }

  private JPanel createButtons() {
    return DialogComponentFactory.createButtonPanel(
        "Create Event",
        new Color(66, 133, 244),
        this::createEvent,
        this::dispose);
  }

  private void addValidationListeners() {
    subjectField.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent e) {
        validateSubject();
      }

      public void focusGained(java.awt.event.FocusEvent e) {
        subjectErrorLabel.setText(" ");
        subjectField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
      }
    });

    javax.swing.event.ChangeListener timeListener = e -> validateTime();
    startHourSpinner.addChangeListener(timeListener);
    startMinuteSpinner.addChangeListener(timeListener);
    endHourSpinner.addChangeListener(timeListener);
    endMinuteSpinner.addChangeListener(timeListener);
  }

  private void validateSubject() {
    String subject = subjectField.getText().trim();
    if (subject.isEmpty()) {
      subjectErrorLabel.setText("Subject cannot be empty");
      subjectField.setBorder(BorderFactory.createLineBorder(new Color(220, 53, 69), 1));
    } else {
      subjectErrorLabel.setText(" ");
      subjectField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    }
  }

  private void validateTime() {
    int startHour = (Integer) startHourSpinner.getValue();
    int startMin = (Integer) startMinuteSpinner.getValue();
    int endHour = (Integer) endHourSpinner.getValue();
    int endMin = (Integer) endMinuteSpinner.getValue();

    LocalTime start = LocalTime.of(startHour, startMin);
    LocalTime end = LocalTime.of(endHour, endMin);

    if (!end.isAfter(start)) {
      timeErrorLabel.setText("End time must be after start time");
    } else {
      timeErrorLabel.setText(" ");
    }
  }

  private void validateRecurring() {
    if (!isRecurringCheckBox.isSelected()) {
      recurringErrorLabel.setText(" ");
      return;
    }
    if (getSelectedWeekdays().isEmpty()) {
      recurringErrorLabel.setText("Select at least one weekday");
    } else {
      recurringErrorLabel.setText(" ");
    }
  }

  private void toggleRecurringPanel() {
    recurringPanel.setVisible(isRecurringCheckBox.isSelected());
    validateRecurring();
    revalidate();
    repaint();
  }

  private void createEvent() {
    String subject = subjectField.getText().trim();
    if (subject.isEmpty()) {
      DialogComponentFactory.showError(this, "Subject cannot be empty");
      return;
    }

    LocalTime startTime = getStartTime();
    LocalTime endTime = getEndTime();
    LocalDateTime start = LocalDateTime.of(selectedDate, startTime);
    LocalDateTime end = LocalDateTime.of(selectedDate, endTime);

    if (!end.isAfter(start)) {
      DialogComponentFactory.showError(this, "End time must be after start time");
      return;
    }

    String description = descriptionArea.getText().trim();
    String location = locationField.getText().trim();
    boolean isPublic = publicRadio.isSelected();

    EventDetails details = new EventDetails.Builder(subject, start, end)
        .description(description)
        .location(location)
        .isPublic(isPublic)
        .build();

    if (!isRecurringCheckBox.isSelected()) {
      CalendarGuiController.EventCreationRequestWrapper.Builder builder =
          new CalendarGuiController.EventCreationRequestWrapper.Builder(subject, start, end)
              .description(description)
              .location(location)
              .isPublic(isPublic);
      controller.createEvent(builder);
    } else {
      createRecurringEvent(details);
    }
    dispose();
  }

  private static class EventDetails {
    final String subject;
    final LocalDateTime start;
    final LocalDateTime end;
    final String description;
    final String location;
    final boolean isPublic;

    private EventDetails(Builder builder) {
      this.subject = builder.subject;
      this.start = builder.start;
      this.end = builder.end;
      this.description = builder.description;
      this.location = builder.location;
      this.isPublic = builder.isPublic;
    }

    static class Builder {
      private final String subject;
      private final LocalDateTime start;
      private final LocalDateTime end;
      private String description = "";
      private String location = "";
      private boolean isPublic = true;

      Builder(String subject, LocalDateTime start, LocalDateTime end) {
        this.subject = subject;
        this.start = start;
        this.end = end;
      }

      Builder description(String description) {
        this.description = description;
        return this;
      }

      Builder location(String location) {
        this.location = location;
        return this;
      }

      Builder isPublic(boolean isPublic) {
        this.isPublic = isPublic;
        return this;
      }

      EventDetails build() {
        return new EventDetails(this);
      }
    }
  }

  private void createRecurringEvent(EventDetails details) {
    String weekdays = getSelectedWeekdays();
    if (weekdays.isEmpty()) {
      DialogComponentFactory.showError(this, "Please select at least one weekday");
      return;
    }

    CalendarGuiController.RecurringEventRequestWrapper.Builder builder =
        new CalendarGuiController.RecurringEventRequestWrapper.Builder(
            details.subject, details.start, details.end, weekdays)
            .description(details.description)
            .location(details.location)
            .isPublic(details.isPublic);

    if (countRadio.isSelected()) {
      builder.repeatCount((Integer) repeatCountSpinner.getValue());
    } else {
      try {
        builder.repeatUntil(getRepeatUntilDate());
      } catch (Exception e) {
        DialogComponentFactory.showError(this, "Invalid until date: " + e.getMessage());
        return;
      }
    }

    controller.createRecurringEvent(builder);
  }

  private LocalTime getStartTime() {
    return LocalTime.of((Integer) startHourSpinner.getValue(),
        (Integer) startMinuteSpinner.getValue());
  }

  private LocalTime getEndTime() {
    return LocalTime.of((Integer) endHourSpinner.getValue(),
        (Integer) endMinuteSpinner.getValue());
  }

  private LocalDateTime getRepeatUntilDate() {
    int year = (Integer) untilYearCombo.getSelectedItem();
    int month = (Integer) untilMonthCombo.getSelectedItem();
    int day = (Integer) untilDayCombo.getSelectedItem();
    return LocalDate.of(year, month, day).atTime(23, 59);
  }

  private String getSelectedWeekdays() {
    StringBuilder sb = new StringBuilder();
    String[] codes = {"M", "T", "W", "R", "F", "S", "U"};
    for (int i = 0; i < 7; i++) {
      if (weekdayCheckboxes[i].isSelected()) {
        sb.append(codes[i]);
      }
    }
    return sb.toString();
  }

  private TitledBorder createTitledBorder(String title) {
    return BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
        title,
        TitledBorder.LEFT,
        TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 12));
  }

  private JLabel createBoldLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(new Font("Arial", Font.BOLD, 13));
    return label;
  }

  private JLabel createErrorLabel() {
    JLabel label = new JLabel(" ");
    label.setFont(new Font("Arial", Font.PLAIN, 11));
    label.setForeground(new Color(220, 53, 69));
    return label;
  }

  private GridBagConstraints createGbc(int x, int y, double weightx) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 15, 10, 15);
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.weightx = weightx;
    return gbc;
  }
}