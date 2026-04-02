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
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Dialog for searching and editing events.
 */
public class SearchEventsDialog extends JDialog {

  private final CalendarGuiController controller;
  private JTextField searchField;
  private JList<String> resultsList;
  private JLabel resultsCountLabel;
  private List<InterfaceEvent> foundEvents;

  private JComboBox<String> propertyComboBox;
  private JPanel valueInputPanel;
  private JTextField textValueField;
  private JTextArea textAreaValue;
  private JComboBox<String> statusComboBox;
  private DateTimeInputPanel dateTimePanel;
  private JLabel hintLabel;
  private JLabel errorLabel;
  private JRadioButton allMatchingRadio;
  private JRadioButton selectedOnesRadio;

  private static final Map<String, PropertyInputConfigurator> CONFIGURATORS =
      createConfiguratorMap();

  private final Map<String, Function<SearchEventsDialog, String>> valueExtractors =
      createValueExtractors();

  /**
   * Constructs the SearchEventsDialog.
   *
   * @param parent the parent JFrame.
   * @param controller the application controller.
   */
  public SearchEventsDialog(JFrame parent, CalendarGuiController controller) {
    super(parent, "Search and Edit Events", true);
    this.controller = controller;
    this.foundEvents = new ArrayList<>();
    initializeUi();
  }

  private void initializeUi() {
    setSize(700, 700);
    setLocationRelativeTo(getParent());
    setLayout(new BorderLayout(15, 15));

    JPanel mainContainer = createMainContainer();
    add(mainContainer);
  }

  private JPanel createMainContainer() {
    final JPanel container = new JPanel(new BorderLayout(10, 10));
    container.setBorder(new EmptyBorder(20, 20, 20, 20));
    container.setBackground(Color.WHITE);

    container.add(createHeader(), BorderLayout.NORTH);
    container.add(createScrollableContent(), BorderLayout.CENTER);
    container.add(createButtons(), BorderLayout.SOUTH);

    return container;
  }

  private JPanel createHeader() {
    return DialogComponentFactory.createHeaderPanel(
        "Search and Edit Events",
        new Color(156, 39, 176));
  }

  private JScrollPane createScrollableContent() {
    final JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBackground(Color.WHITE);

    contentPanel.add(createSearchPanel());
    contentPanel.add(Box.createVerticalStrut(15));
    contentPanel.add(createResultsPanel());
    contentPanel.add(Box.createVerticalStrut(15));
    contentPanel.add(createEditPanel());

    JScrollPane scrollPane = new JScrollPane(contentPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  private JPanel createSearchPanel() {
    final JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(createTitledBorder("Search Events"));
    panel.setBackground(Color.WHITE);

    GridBagConstraints gbc = createGbc(0, 0, 0.3);
    panel.add(createBoldLabel("Event Name:"), gbc);

    gbc = createGbc(1, 0, 0.5);
    searchField = new JTextField();
    searchField.setFont(new Font("Arial", Font.PLAIN, 13));
    searchField.setPreferredSize(new Dimension(250, 32));
    panel.add(searchField, gbc);

    gbc = createGbc(2, 0, 0.2);
    JButton searchButton = createSearchButton();
    panel.add(searchButton, gbc);

    return panel;
  }

  private JButton createSearchButton() {
    JButton button = new JButton("Search");
    button.setFont(new Font("Arial", Font.BOLD, 12));
    button.setBackground(new Color(156, 39, 176));
    button.setForeground(Color.WHITE);
    button.setOpaque(true);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.addActionListener(e -> performSearch());
    return button;
  }

  private JPanel createResultsPanel() {
    final JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBorder(createTitledBorder("Search Results"));
    panel.setBackground(Color.WHITE);
    panel.setPreferredSize(new Dimension(650, 150));

    resultsCountLabel = new JLabel("No search performed yet");
    resultsCountLabel.setFont(new Font("Arial", Font.ITALIC, 11));
    resultsCountLabel.setForeground(new Color(120, 120, 120));
    resultsCountLabel.setBorder(new EmptyBorder(5, 15, 5, 15));
    panel.add(resultsCountLabel, BorderLayout.NORTH);

    resultsList = new JList<>();
    resultsList.setFont(new Font("Arial", Font.PLAIN, 12));
    resultsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane scrollPane = new JScrollPane(resultsList);
    scrollPane.setBorder(new EmptyBorder(0, 15, 10, 15));
    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  private JPanel createEditPanel() {
    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(createTitledBorder("Edit Matching Events"));
    panel.setBackground(Color.WHITE);

    panel.add(createScopePanel());
    panel.add(createPropertySelectionPanel());
    panel.add(createValueInputSection());

    return panel;
  }

  private JPanel createScopePanel() {
    final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(5, 10, 5, 10));

    final ButtonGroup group = new ButtonGroup();

    allMatchingRadio = new JRadioButton("Edit all matching events", true);
    allMatchingRadio.setFont(new Font("Arial", Font.PLAIN, 13));
    allMatchingRadio.setBackground(Color.WHITE);

    selectedOnesRadio = new JRadioButton("Edit only selected events");
    selectedOnesRadio.setFont(new Font("Arial", Font.PLAIN, 13));
    selectedOnesRadio.setBackground(Color.WHITE);

    group.add(allMatchingRadio);
    group.add(selectedOnesRadio);

    panel.add(allMatchingRadio);
    panel.add(selectedOnesRadio);

    return panel;
  }

  private JPanel createPropertySelectionPanel() {
    final JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Color.WHITE);

    GridBagConstraints gbc = createGbc(0, 0, 0.3);
    gbc.insets = new Insets(5, 15, 5, 15);
    panel.add(createBoldLabel("Property:"), gbc);

    gbc = createGbc(1, 0, 0.7);
    gbc.insets = new Insets(5, 15, 5, 15);

    String[] properties = {"subject", "start", "end", "description", "location", "status"};
    propertyComboBox = new JComboBox<>(properties);
    propertyComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
    propertyComboBox.addActionListener(e -> updateInputField());
    panel.add(propertyComboBox, gbc);

    return panel;
  }

  private JPanel createValueInputSection() {
    final JPanel panel = new JPanel(new BorderLayout(10, 5));
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(5, 15, 10, 15));

    panel.add(createBoldLabel("New Value:"), BorderLayout.NORTH);

    valueInputPanel = new JPanel(new BorderLayout());
    valueInputPanel.setBackground(Color.WHITE);

    textValueField = new JTextField();
    textValueField.setFont(new Font("Arial", Font.PLAIN, 13));
    textValueField.setPreferredSize(new Dimension(500, 32));
    valueInputPanel.add(textValueField, BorderLayout.CENTER);

    panel.add(valueInputPanel, BorderLayout.CENTER);
    panel.add(createHintErrorPanel(), BorderLayout.SOUTH);

    return panel;
  }

  private JPanel createHintErrorPanel() {
    final JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(5, 0, 0, 0));

    hintLabel = new JLabel("Enter new subject for all matching events");
    hintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
    hintLabel.setForeground(new Color(120, 120, 120));
    hintLabel.setAlignmentX(LEFT_ALIGNMENT);
    panel.add(hintLabel);

    errorLabel = new JLabel(" ");
    errorLabel.setFont(new Font("Arial", Font.PLAIN, 11));
    errorLabel.setForeground(new Color(220, 53, 69));
    errorLabel.setAlignmentX(LEFT_ALIGNMENT);
    panel.add(errorLabel);

    return panel;
  }

  private JPanel createButtons() {
    return DialogComponentFactory.createButtonPanel(
        "Apply Changes",
        new Color(156, 39, 176),
        this::applyChanges,
        this::dispose);
  }

  private void performSearch() {
    String searchTerm = searchField.getText().trim();

    if (searchTerm.isEmpty()) {
      DialogComponentFactory.showError(this, "Please enter an event name to search");
      return;
    }

    List<InterfaceEvent> allEvents = controller.getAllEventsFromCalendar();
    foundEvents.clear();

    for (InterfaceEvent event : allEvents) {
      if (event.getSubject().equalsIgnoreCase(searchTerm)) {
        foundEvents.add(event);
      }
    }

    updateResultsDisplay(searchTerm);
  }

  private void updateResultsDisplay(String searchTerm) {
    if (foundEvents.isEmpty()) {
      resultsCountLabel.setText("No events found with name: " + searchTerm);
      resultsList.setListData(new String[0]);
    } else {
      resultsCountLabel.setText("Found " + foundEvents.size() + " event(s) with name: "
          + searchTerm);
      String[] displayData = formatEventsForDisplay();
      resultsList.setListData(displayData);
    }
  }

  private String[] formatEventsForDisplay() {
    String[] displayData = new String[foundEvents.size()];
    for (int i = 0; i < foundEvents.size(); i++) {
      displayData[i] = formatEventDisplay(foundEvents.get(i));
    }
    return displayData;
  }

  private String formatEventDisplay(InterfaceEvent event) {
    String dateStr = event.getStart().toLocalDate().getMonth()
        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        + " " + event.getStart().toLocalDate().getDayOfMonth();
    String timeStr = event.getStart().toLocalTime().toString();
    String recurring = event.isPartOfSeries() ? " (Recurring)" : "";
    return dateStr + " at " + timeStr + recurring;
  }

  /**
   * Uses Map lookup instead of switch statement to update input fields.
   */
  private void updateInputField() {
    String property = (String) propertyComboBox.getSelectedItem();
    valueInputPanel.removeAll();

    PropertyInputConfigurator configurator = CONFIGURATORS.getOrDefault(
        property,
        new DefaultConfigurator());
    configurator.configure(this);

    valueInputPanel.revalidate();
    valueInputPanel.repaint();
  }

  private void applyChanges() {
    if (foundEvents.isEmpty()) {
      DialogComponentFactory.showError(this, "No events found. Please search first.");
      return;
    }

    String property = (String) propertyComboBox.getSelectedItem();
    String newValue = extractNewValue(property);


    if (newValue == null) {
      return;
    }

    if (newValue.isEmpty() && property.equals("subject")) {
      DialogComponentFactory.showError(this, "Subject cannot be empty");
      return;
    }

    List<InterfaceEvent> eventsToEdit = selectEventsToEdit();
    if (eventsToEdit.isEmpty()) {
      DialogComponentFactory.showError(this, "Please select events to edit");
      return;
    }

    int successCount = editEvents(eventsToEdit, property, newValue);

    if (successCount > 0) {
      DialogComponentFactory.showMessage(this, successCount + " event(s) updated successfully");
      dispose();
    } else {
      DialogComponentFactory.showError(this, "Failed to update events");
    }
  }

  /**
   * Uses Map lookup instead of switch statement to extract new value.
   */
  private String extractNewValue(String property) {
    Function<SearchEventsDialog, String> extractor = valueExtractors.get(property);

    if (extractor != null) {
      return extractor.apply(this);
    }


    return textValueField.getText().trim();
  }

  private List<InterfaceEvent> selectEventsToEdit() {
    if (allMatchingRadio.isSelected()) {
      return foundEvents;
    }

    int[] selectedIndices = resultsList.getSelectedIndices();
    List<InterfaceEvent> selected = new ArrayList<>();
    for (int index : selectedIndices) {
      selected.add(foundEvents.get(index));
    }
    return selected;
  }

  private int editEvents(List<InterfaceEvent> events, String property, String newValue) {
    int successCount = 0;
    for (InterfaceEvent event : events) {
      try {
        controller.editEvent(event.getSubject(), event.getStart(), property, newValue);
        successCount++;
      } catch (Exception e) {
        //expected

      }
    }
    return successCount;
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

  private GridBagConstraints createGbc(int x, int y, double weightx) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(10, 15, 10, 15);
    gbc.gridx = x;
    gbc.gridy = y;
    gbc.weightx = weightx;
    return gbc;
  }



  /**
   * Creates Map of property configurators.
   */
  private static Map<String, PropertyInputConfigurator> createConfiguratorMap() {
    Map<String, PropertyInputConfigurator> map = new HashMap<>();
    map.put("subject", new SubjectConfigurator());
    map.put("start", new DateTimeConfigurator("start"));
    map.put("end", new DateTimeConfigurator("end"));
    map.put("description", new DescriptionConfigurator());
    map.put("location", new LocationConfigurator());
    map.put("status", new StatusConfigurator());
    return map;
  }

  /**
   * Creates Map of value extractors.
   */
  private Map<String, Function<SearchEventsDialog, String>> createValueExtractors() {
    Map<String, Function<SearchEventsDialog, String>> extractors = new HashMap<>();


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



  private interface PropertyInputConfigurator {
    void configure(SearchEventsDialog dialog);
  }

  private static class SubjectConfigurator implements PropertyInputConfigurator {
    public void configure(SearchEventsDialog dialog) {
      dialog.textValueField.setText("");
      dialog.valueInputPanel.add(dialog.textValueField, BorderLayout.CENTER);
      dialog.hintLabel.setText("Enter new subject for all matching events");
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


  private static class DateTimeConfigurator implements PropertyInputConfigurator {
    private final String property;

    DateTimeConfigurator(String property) {
      this.property = property;
    }

    public void configure(SearchEventsDialog dialog) {

      LocalDateTime initialTime = LocalDateTime.now();

      if (!dialog.foundEvents.isEmpty()) {
        InterfaceEvent firstEvent = dialog.foundEvents.get(0);
        if (property.equals("start")) {
          initialTime = firstEvent.getStart();
        } else if (property.equals("end")) {
          initialTime = firstEvent.getEnd();
        }
      }

      dialog.dateTimePanel = new DateTimeInputPanel(initialTime);
      dialog.valueInputPanel.add(dialog.dateTimePanel, BorderLayout.CENTER);
      dialog.hintLabel.setText("Set the new " + property
          + " date and time for all matching events");
      dialog.errorLabel.setText(" ");
    }
  }

  private static class DescriptionConfigurator implements PropertyInputConfigurator {
    public void configure(SearchEventsDialog dialog) {
      dialog.textAreaValue = new JTextArea(3, 30);
      dialog.textAreaValue.setFont(new Font("Arial", Font.PLAIN, 13));
      dialog.textAreaValue.setLineWrap(true);
      dialog.textAreaValue.setWrapStyleWord(true);
      dialog.textAreaValue.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(Color.LIGHT_GRAY),
          BorderFactory.createEmptyBorder(5, 5, 5, 5)));

      JScrollPane scrollPane = new JScrollPane(dialog.textAreaValue);
      scrollPane.setPreferredSize(new Dimension(500, 80));
      dialog.valueInputPanel.add(scrollPane, BorderLayout.CENTER);
      dialog.hintLabel.setText("Enter description for all matching events");
      dialog.errorLabel.setText(" ");
    }
  }

  private static class LocationConfigurator implements PropertyInputConfigurator {
    public void configure(SearchEventsDialog dialog) {
      dialog.textValueField.setText("");
      dialog.valueInputPanel.add(dialog.textValueField, BorderLayout.CENTER);
      dialog.hintLabel.setText("Enter location for all matching events");
      dialog.errorLabel.setText(" ");
    }
  }

  private static class StatusConfigurator implements PropertyInputConfigurator {
    public void configure(SearchEventsDialog dialog) {
      dialog.statusComboBox = new JComboBox<>(new String[]{"public", "private"});
      dialog.statusComboBox.setFont(new Font("Arial", Font.PLAIN, 13));
      dialog.statusComboBox.setPreferredSize(new Dimension(150, 30));

      JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      statusPanel.setBackground(Color.WHITE);
      statusPanel.add(dialog.statusComboBox);
      dialog.valueInputPanel.add(statusPanel, BorderLayout.WEST);
      dialog.hintLabel.setText("Select status for all matching events");
      dialog.errorLabel.setText(" ");
    }
  }

  private static class DefaultConfigurator implements PropertyInputConfigurator {
    public void configure(SearchEventsDialog dialog) {
      dialog.valueInputPanel.add(dialog.textValueField, BorderLayout.CENTER);
    }
  }
}