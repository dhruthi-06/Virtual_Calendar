package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import calendar.model.InterfaceEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for editing existing events.
 */
public class EditEventDialog extends JDialog {

  private final CalendarGuiController controller;
  private final LocalDate selectedDate;
  private final List<InterfaceEvent> events;

  private JComboBox<String> eventComboBox;
  private JComboBox<String> propertyComboBox;
  private JPanel valueInputPanel;
  private JTextField textValueField;
  private JTextArea textAreaValue;
  private JComboBox<String> statusComboBox;
  private DateTimeInputPanel dateTimePanel;
  private JLabel hintLabel;
  private JLabel errorLabel;
  private JRadioButton singleRadio;
  private JRadioButton fromDateRadio;
  private JRadioButton seriesRadio;
  private InterfaceEvent selectedEvent;

  private static final Map<String, PropertyInputHandler> PROPERTY_HANDLERS = createHandlerMap();

  private final Map<String, Function<EditEventDialog, String>> valueExtractors =
      createValueExtractors();

  /**
   * Constructs the EditEventDialog.
   *
   * @param parent the parent JFrame.
   * @param controller the application controller.
   * @param selectedDate the date whose events are being edited.
   * @param events the list of events on the selected date.
   */
  public EditEventDialog(JFrame parent, CalendarGuiController controller,
                         LocalDate selectedDate, List<InterfaceEvent> events) {
    super(parent, "Edit Event", true);
    this.controller = controller;
    this.selectedDate = selectedDate;
    this.events = events;
    initializeUi();
  }

  private void initializeUi() {
    setSize(650, 650);
    setLocationRelativeTo(getParent());
    setLayout(new BorderLayout(15, 15));

    JPanel mainContainer = createMainContainer();
    add(mainContainer);
    updateSelectedEvent();
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
        "Edit Event - " + dayName + ", " + dateStr,
        new Color(52, 168, 83));
  }

  private JScrollPane createScrollableContent() {
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(Color.WHITE);

    contentPanel.add(createEventSelectionPanel());
    contentPanel.add(Box.createVerticalStrut(15));
    contentPanel.add(createPropertyPanel());
    contentPanel.add(Box.createVerticalStrut(15));
    contentPanel.add(createScopePanel());

    JScrollPane scrollPane = new JScrollPane(contentPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  private JPanel createEventSelectionPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(DialogComponentFactory.createStandardTitledBorder("Select Event to Edit"));
    panel.setBackground(Color.WHITE);

    GridBagConstraints gbc = DialogComponentFactory.createStandardGbc(0, 0, 0.3);
    panel.add(DialogComponentFactory.createBoldLabel("Event:"), gbc);

    gbc = DialogComponentFactory.createStandardGbc(1, 0, 0.7);
    eventComboBox = createEventComboBox();
    panel.add(eventComboBox, gbc);

    return panel;
  }

  private JComboBox<String> createEventComboBox() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    for (InterfaceEvent event : events) {
      String displayText = formatEventDisplay(event);
      model.addElement(displayText);
    }
    JComboBox<String> combo = new JComboBox<>(model);
    combo.setFont(new Font("Arial", Font.PLAIN, 13));
    combo.addActionListener(e -> updateSelectedEvent());
    return combo;
  }

  private String formatEventDisplay(InterfaceEvent event) {
    String displayText = event.getSubject() + " at " + event.getStart().toLocalTime();
    if (event.isPartOfSeries()) {
      displayText += " (Recurring)";
    }
    return displayText;
  }

  private JPanel createPropertyPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(DialogComponentFactory.createStandardTitledBorder("What to Change"));
    panel.setBackground(Color.WHITE);

    panel.add(createPropertySelectionPanel());
    panel.add(createValueInputSection());

    return panel;
  }

  private JPanel createPropertySelectionPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Color.WHITE);

    GridBagConstraints gbc = DialogComponentFactory.createCustomGbc(
        0, 0, 0.3, new java.awt.Insets(10, 15, 5, 15));
    panel.add(DialogComponentFactory.createBoldLabel("Property:"), gbc);

    gbc = DialogComponentFactory.createCustomGbc(
        1, 0, 0.7, new java.awt.Insets(10, 15, 5, 15));
    String[] properties = {"subject", "start", "end", "description", "location", "status"};
    propertyComboBox = new JComboBox<>(properties);
    propertyComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
    propertyComboBox.addActionListener(e -> updateInputField());
    panel.add(propertyComboBox, gbc);

    return panel;
  }

  private JPanel createValueInputSection() {
    JPanel panel = new JPanel(new BorderLayout(10, 5));
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(5, 15, 10, 15));

    panel.add(DialogComponentFactory.createBoldLabel("New Value:"), BorderLayout.NORTH);

    valueInputPanel = new JPanel(new BorderLayout());
    valueInputPanel.setBackground(Color.WHITE);

    textValueField = new JTextField();
    textValueField.setFont(new Font("Arial", Font.PLAIN, 13));
    textValueField.setPreferredSize(new Dimension(450, 32));
    valueInputPanel.add(textValueField, BorderLayout.CENTER);

    panel.add(valueInputPanel, BorderLayout.CENTER);
    panel.add(createHintErrorPanel(), BorderLayout.SOUTH);

    return panel;
  }

  private JPanel createHintErrorPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(5, 0, 0, 0));

    hintLabel = new JLabel("Enter new subject");
    hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
    hintLabel.setForeground(new Color(120, 120, 120));
    hintLabel.setAlignmentX(LEFT_ALIGNMENT);
    panel.add(hintLabel);

    errorLabel = DialogComponentFactory.createErrorLabel();
    errorLabel.setAlignmentX(LEFT_ALIGNMENT);
    panel.add(errorLabel);

    return panel;
  }

  private JPanel createScopePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(DialogComponentFactory.createStandardTitledBorder("Edit Scope"));
    panel.setBackground(Color.WHITE);

    final ButtonGroup group = new ButtonGroup();

    singleRadio = createScopeRadio("This event only", true);
    fromDateRadio = createScopeRadio("This and all following events in the series", false);
    seriesRadio = createScopeRadio("All events in the series", false);

    group.add(singleRadio);
    group.add(fromDateRadio);
    group.add(seriesRadio);

    panel.add(singleRadio);
    panel.add(fromDateRadio);
    panel.add(seriesRadio);

    return panel;
  }

  private JRadioButton createScopeRadio(String text, boolean selected) {
    JRadioButton radio = new JRadioButton(text, selected);
    radio.setFont(new Font("Arial", Font.PLAIN, 13));
    radio.setBackground(Color.WHITE);
    radio.setBorder(new EmptyBorder(10, 15, 8, 15));
    return radio;
  }

  private JPanel createButtons() {
    return DialogComponentFactory.createButtonPanel(
        "Save Changes",
        new Color(52, 168, 83),
        this::saveChanges,
        this::dispose);
  }

  private void updateSelectedEvent() {
    int index = eventComboBox.getSelectedIndex();
    if (index >= 0 && index < events.size()) {
      selectedEvent = events.get(index);
      updateScopeRadioButtons();
      updateInputField();
    }
  }

  private void updateScopeRadioButtons() {
    boolean isRecurring = selectedEvent.isPartOfSeries();
    fromDateRadio.setEnabled(isRecurring);
    seriesRadio.setEnabled(isRecurring);
    if (!isRecurring) {
      singleRadio.setSelected(true);
    }
  }

  /**
   * Uses Map lookup instead of switch statement to update input fields.
   */
  private void updateInputField() {
    if (selectedEvent == null) {
      return;
    }

    String property = (String) propertyComboBox.getSelectedItem();
    valueInputPanel.removeAll();

    PropertyInputHandler handler = PROPERTY_HANDLERS.getOrDefault(
        property,
        new DefaultInputHandler());
    handler.setupInput(this, selectedEvent);

    valueInputPanel.revalidate();
    valueInputPanel.repaint();
  }

  private void saveChanges() {
    if (selectedEvent == null) {
      DialogComponentFactory.showError(this, "No event selected");
      return;
    }

    String property = (String) propertyComboBox.getSelectedItem();
    String newValue = extractNewValue(property);

    if (newValue == null) {
      return;
    }

    if (newValue.isEmpty() && !property.equals("description") && !property.equals("location")) {
      DialogComponentFactory.showError(this, "New value cannot be empty");
      return;
    }

    executeEdit(property, newValue);
  }

  /**
   * Uses Map lookup instead of switch statement to extract new value.
   *
   * @param property the property name
   * @return the new value, or null if extraction failed
   */
  private String extractNewValue(String property) {
    Function<EditEventDialog, String> extractor = valueExtractors.get(property);

    if (extractor != null) {
      return extractor.apply(this);
    }

    return textValueField.getText().trim();
  }

  private void executeEdit(String property, String newValue) {
    String subject = selectedEvent.getSubject();
    LocalDateTime startDateTime = selectedEvent.getStart();

    try {
      if (singleRadio.isSelected()) {
        controller.editEvent(subject, startDateTime, property, newValue);
      } else if (fromDateRadio.isSelected()) {
        controller.editEventsFromDate(subject, startDateTime, property, newValue);
      } else if (seriesRadio.isSelected()) {
        controller.editEntireSeries(subject, startDateTime, property, newValue);
      }
      dispose();
    } catch (Exception e) {
      DialogComponentFactory.showError(this, "Failed to save changes: " + e.getMessage());
    }
  }

  /**
   * Creates Map of property handlers.
   *
   * @return map of property name to handler
   */
  private static Map<String, PropertyInputHandler> createHandlerMap() {
    Map<String, PropertyInputHandler> handlers = new HashMap<>();
    handlers.put("subject", new SubjectInputHandler());
    handlers.put("start", new DateTimeInputHandler("start"));
    handlers.put("end", new DateTimeInputHandler("end"));
    handlers.put("description", new DescriptionInputHandler());
    handlers.put("location", new LocationInputHandler());
    handlers.put("status", new StatusInputHandler());
    return handlers;
  }

  /**
   * Creates Map of value extractors.
   *
   * @return map of property name to extractor function
   */
  private Map<String, Function<EditEventDialog, String>> createValueExtractors() {
    Map<String, Function<EditEventDialog, String>> extractors = new HashMap<>();

    extractors.put("start", dialog -> {
      try {
        return dialog.dateTimePanel.getDateTime().toString();
      } catch (Exception e) {
        DialogComponentFactory.showError(dialog, "Invalid date/time: " + e.getMessage());
        return null;
      }
    });

    extractors.put("end", dialog -> {
      try {
        return dialog.dateTimePanel.getDateTime().toString();
      } catch (Exception e) {
        DialogComponentFactory.showError(dialog, "Invalid date/time: " + e.getMessage());
        return null;
      }
    });

    extractors.put("description", dialog -> dialog.textAreaValue.getText().trim());
    extractors.put("status", dialog -> (String) dialog.statusComboBox.getSelectedItem());

    return extractors;
  }

  private interface PropertyInputHandler {
    void setupInput(EditEventDialog dialog, InterfaceEvent event);
  }

  private static class SubjectInputHandler implements PropertyInputHandler {
    public void setupInput(EditEventDialog dialog, InterfaceEvent event) {
      dialog.textValueField.setText(event.getSubject());
      dialog.valueInputPanel.add(dialog.textValueField, BorderLayout.CENTER);
      dialog.hintLabel.setText("Enter the new event name");
      dialog.errorLabel.setText(" ");

      dialog.textValueField.addFocusListener(new java.awt.event.FocusAdapter() {
        public void focusLost(java.awt.event.FocusEvent e) {
          if (dialog.textValueField.getText().trim().isEmpty()) {
            dialog.errorLabel.setText("Subject cannot be empty");
          }
        }

        public void focusGained(java.awt.event.FocusEvent e) {
          dialog.errorLabel.setText(" ");
        }
      });
    }
  }

  private static class DateTimeInputHandler implements PropertyInputHandler {
    private final String property;

    DateTimeInputHandler(String property) {
      this.property = property;
    }

    public void setupInput(EditEventDialog dialog, InterfaceEvent event) {
      LocalDateTime currentTime = property.equals("start") ? event.getStart() : event.getEnd();
      dialog.dateTimePanel = new DateTimeInputPanel(currentTime);
      dialog.valueInputPanel.add(dialog.dateTimePanel, BorderLayout.CENTER);
      dialog.hintLabel.setText("Set the new " + property + " date and time");
      dialog.errorLabel.setText(" ");

      dialog.dateTimePanel.addChangeListener(evt -> {
        try {
          LocalDateTime newTime = dialog.dateTimePanel.getDateTime();
          validateDateTime(dialog, newTime, property, event);
        } catch (Exception ex) {
          dialog.errorLabel.setText("Invalid date/time");
        }
      });
    }

    private void validateDateTime(EditEventDialog dialog, LocalDateTime newTime,
                                  String prop, InterfaceEvent event) {
      if (prop.equals("start") && newTime.isAfter(event.getEnd())) {
        dialog.errorLabel.setText("Start time cannot be after end time");
      } else if (prop.equals("end") && newTime.isBefore(event.getStart())) {
        dialog.errorLabel.setText("End time cannot be before start time");
      } else if (prop.equals("start") && newTime.equals(event.getEnd())) {
        dialog.errorLabel.setText("Start time cannot equal end time");
      } else if (prop.equals("end") && newTime.equals(event.getStart())) {
        dialog.errorLabel.setText("End time cannot equal start time");
      } else {
        dialog.errorLabel.setText(" ");
      }
    }
  }

  private static class DescriptionInputHandler implements PropertyInputHandler {
    public void setupInput(EditEventDialog dialog, InterfaceEvent event) {
      dialog.textAreaValue = new JTextArea(3, 30);
      dialog.textAreaValue.setText(event.getDescription());
      dialog.textAreaValue.setFont(new Font("Arial", Font.PLAIN, 13));
      dialog.textAreaValue.setLineWrap(true);
      dialog.textAreaValue.setWrapStyleWord(true);
      dialog.textAreaValue.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(Color.LIGHT_GRAY),
          BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      JScrollPane scrollPane = new JScrollPane(dialog.textAreaValue);
      scrollPane.setPreferredSize(new Dimension(450, 90));
      dialog.valueInputPanel.add(scrollPane, BorderLayout.CENTER);
      dialog.hintLabel.setText("Enter the event description");
      dialog.errorLabel.setText(" ");
    }
  }

  private static class LocationInputHandler implements PropertyInputHandler {
    public void setupInput(EditEventDialog dialog, InterfaceEvent event) {
      dialog.textValueField.setText(event.getLocation());
      dialog.valueInputPanel.add(dialog.textValueField, BorderLayout.CENTER);
      dialog.hintLabel.setText("Enter the event location (e.g., Room 101, Zoom link)");
      dialog.errorLabel.setText(" ");
    }
  }

  private static class StatusInputHandler implements PropertyInputHandler {
    public void setupInput(EditEventDialog dialog, InterfaceEvent event) {
      dialog.statusComboBox = new JComboBox<>(new String[]{"public", "private"});
      dialog.statusComboBox.setSelectedItem(event.isPublic() ? "public" : "private");
      dialog.statusComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
      dialog.statusComboBox.setPreferredSize(new Dimension(150, 30));

      JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      statusPanel.setBackground(Color.WHITE);
      statusPanel.add(dialog.statusComboBox);
      dialog.valueInputPanel.add(statusPanel, BorderLayout.WEST);
      dialog.hintLabel.setText("Select whether this event is public or private");
      dialog.errorLabel.setText(" ");
    }
  }

  private static class DefaultInputHandler implements PropertyInputHandler {
    public void setupInput(EditEventDialog dialog, InterfaceEvent event) {
      dialog.valueInputPanel.add(dialog.textValueField, BorderLayout.CENTER);
      dialog.hintLabel.setText("");
      dialog.errorLabel.setText(" ");
    }
  }
}