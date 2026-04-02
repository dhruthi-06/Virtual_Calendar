package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.ZoneId;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog window for creating a new calendar.
 */
public class CreateCalendarDialog extends JDialog {

  private final CalendarGuiController controller;
  private JTextField nameField;
  private JComboBox<String> timezoneCombo;

  /**
   * Constructs the CreateCalendarDialog.
   *
   * @param parent the parent JFrame.
   * @param controller the application controller.
   */
  public CreateCalendarDialog(JFrame parent, CalendarGuiController controller) {
    super(parent, "Create New Calendar", true);
    this.controller = controller;
    initializeUi();
  }

  private void initializeUi() {
    setSize(400, 200);
    setLocationRelativeTo(getParent());
    setLayout(new BorderLayout(10, 10));

    add(createFormPanel(), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);
  }

  private JPanel createFormPanel() {
    JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
    panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

    panel.add(new JLabel("Calendar Name:"));
    nameField = new JTextField();
    panel.add(nameField);

    panel.add(new JLabel("Timezone:"));
    timezoneCombo = new JComboBox<>(TimezoneConstants.COMMON_TIMEZONES);
    timezoneCombo.setSelectedItem(ZoneId.systemDefault().getId());
    panel.add(timezoneCombo);

    return panel;
  }

  private JPanel createButtonPanel() {
    return DialogComponentFactory.createButtonPanel(
        "Create",
        new Color(66, 133, 244),
        this::createCalendar,
        this::dispose);
  }

  private void createCalendar() {
    String name = nameField.getText().trim();
    String timezone = (String) timezoneCombo.getSelectedItem();

    if (name.isEmpty()) {
      DialogComponentFactory.showError(this, "Calendar name cannot be empty");
      return;
    }

    controller.createCalendar(name, timezone);
    dispose();
  }
}