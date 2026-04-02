package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Dialog window for editing the name and timezone of the current calendar.
 */
public class EditCalendarDialog extends JDialog {

  private final CalendarGuiController controller;
  private final String currentCalendarName;
  private JTextField nameField;
  private JComboBox<String> timezoneCombo;

  /**
   * Constructs the EditCalendarDialog.
   *
   * @param parent the parent JFrame.
   * @param controller the application controller.
   * @param currentCalendarName the name of the calendar currently being edited.
   */
  public EditCalendarDialog(JFrame parent, CalendarGuiController controller,
                            String currentCalendarName) {
    super(parent, "Edit Calendar", true);
    this.controller = controller;
    this.currentCalendarName = currentCalendarName;
    initializeUi();
  }

  private void initializeUi() {
    setSize(500, 250);
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
    container.add(createFormPanel(), BorderLayout.CENTER);
    container.add(createButtonPanel(), BorderLayout.SOUTH);

    return container;
  }

  private JPanel createHeader() {
    return DialogComponentFactory.createHeaderPanel(
        "Edit Calendar: " + currentCalendarName,
        new Color(251, 188, 5));
  }

  private JPanel createFormPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(createTitledBorder("Calendar Properties"));
    panel.setBackground(Color.WHITE);

    GridBagConstraints gbc = createGbc(0, 0, 0.3);
    panel.add(createBoldLabel("Name:"), gbc);

    gbc = createGbc(1, 0, 0.7);
    nameField = new JTextField(currentCalendarName);
    nameField.setFont(new Font("Arial", Font.PLAIN, 13));
    nameField.setPreferredSize(new Dimension(250, 32));
    panel.add(nameField, gbc);

    gbc = createGbc(0, 1, 0.3);
    panel.add(createBoldLabel("Timezone:"), gbc);

    gbc = createGbc(1, 1, 0.7);
    timezoneCombo = new JComboBox<>(TimezoneConstants.COMMON_TIMEZONES);
    timezoneCombo.setFont(new Font("Arial", Font.PLAIN, 13));
    timezoneCombo.setSelectedItem(controller.getCurrentCalendarTimezone());
    panel.add(timezoneCombo, gbc);

    return panel;
  }

  private JPanel createButtonPanel() {
    return DialogComponentFactory.createButtonPanel(
        "Save Changes",
        new Color(251, 188, 5),
        this::saveChanges,
        this::dispose);
  }

  private void saveChanges() {
    String newName = nameField.getText().trim();
    String newTimezone = (String) timezoneCombo.getSelectedItem();

    if (newName.isEmpty()) {
      DialogComponentFactory.showError(this, "Calendar name cannot be empty");
      return;
    }

    try {
      if (!newName.equals(currentCalendarName)) {
        controller.editCalendarName(currentCalendarName, newName);
      }

      String currentTz = controller.getCurrentCalendarTimezone();
      if (!newTimezone.equals(currentTz)) {
        controller.editCalendarTimezone(newName, newTimezone);
      }

      DialogComponentFactory.showMessage(this, "Calendar updated successfully");
      dispose();
    } catch (Exception e) {
      DialogComponentFactory.showError(this, "Failed to update calendar: " + e.getMessage());
    }
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
}