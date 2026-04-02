package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Dialog window for switching the current active calendar.
 */
public class SwitchCalendarDialog extends JDialog {

  private final CalendarGuiController controller;
  private JList<String> calendarList;

  /**
   * Constructs the SwitchCalendarDialog.
   *
   * @param parent the parent JFrame.
   * @param controller the application controller.
   */
  public SwitchCalendarDialog(JFrame parent, CalendarGuiController controller) {
    super(parent, "Switch Calendar", true);
    this.controller = controller;
    initializeUi();
  }

  private void initializeUi() {
    setSize(300, 300);
    setLocationRelativeTo(getParent());
    setLayout(new BorderLayout(10, 10));

    add(createListPanel(), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);
  }

  private JScrollPane createListPanel() {
    DefaultListModel<String> listModel = new DefaultListModel<>();
    List<String> calendarNames = controller.getAllCalendarNames();
    for (String name : calendarNames) {
      listModel.addElement(name);
    }

    calendarList = new JList<>(listModel);
    calendarList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    String currentCalendar = controller.getCurrentCalendarName();
    if (currentCalendar != null) {
      calendarList.setSelectedValue(currentCalendar, true);
    }

    JScrollPane scrollPane = new JScrollPane(calendarList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
    return scrollPane;
  }

  private JPanel createButtonPanel() {
    return DialogComponentFactory.createButtonPanel(
        "Switch",
        new Color(66, 133, 244),
        this::switchCalendar,
        this::dispose);
  }

  private void switchCalendar() {
    String selectedCalendar = calendarList.getSelectedValue();

    if (selectedCalendar == null) {
      DialogComponentFactory.showError(this, "Please select a calendar");
      return;
    }

    controller.switchCalendar(selectedCalendar);
    dispose();
  }
}