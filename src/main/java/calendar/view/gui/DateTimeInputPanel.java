package calendar.view.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * A panel that allows input of a date and time using JSpinners.
 */
public class DateTimeInputPanel extends JPanel {

  private final JSpinner yearSpinner;
  private final JSpinner monthSpinner;
  private final JSpinner daySpinner;
  private final JSpinner hourSpinner;
  private final JSpinner minuteSpinner;

  /**
   * Constructs the DateTimeInputPanel.
   *
   * @param initialDateTime the initial date and time value for the spinners.
   */
  public DateTimeInputPanel(LocalDateTime initialDateTime) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Color.WHITE);


    yearSpinner = createSpinner(initialDateTime.getYear(), 2020, 2100, 1, 4);
    monthSpinner = createSpinner(initialDateTime.getMonthValue(), 1, 12, 1, 2);
    daySpinner = createSpinner(initialDateTime.getDayOfMonth(), 1, 31, 1, 2);
    hourSpinner = createSpinner(initialDateTime.getHour(), 0, 23, 1, 2);

    minuteSpinner = createSpinner(initialDateTime.getMinute(), 0, 59, 1, 2);

    add(createDatePanel());
    add(Box.createVerticalStrut(8));
    add(createTimePanel());
  }

  private JPanel createDatePanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
    panel.setBackground(Color.WHITE);

    JLabel label = new JLabel("Date:");
    label.setFont(new Font("Arial", Font.BOLD, 12));
    panel.add(label);
    panel.add(yearSpinner);
    panel.add(new JLabel("-"));
    panel.add(monthSpinner);
    panel.add(new JLabel("-"));
    panel.add(daySpinner);

    return panel;
  }

  private JPanel createTimePanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
    panel.setBackground(Color.WHITE);

    JLabel label = new JLabel("Time:");
    label.setFont(new Font("Arial", Font.BOLD, 12));
    panel.add(label);
    panel.add(hourSpinner);

    JLabel colon = new JLabel(":");
    colon.setFont(new Font("Arial", Font.BOLD, 16));
    panel.add(colon);
    panel.add(minuteSpinner);

    return panel;
  }

  private JSpinner createSpinner(int value, int min, int max, int step, int columns) {
    JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
    spinner.setFont(new Font("Arial", Font.PLAIN, 14));


    JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
    editor.getTextField().setColumns(columns);

    editor.getFormat().setGroupingUsed(false);

    return spinner;
  }

  /**
   * Gets the current date and time value from the spinners.
   *
   * @return the current LocalDateTime.
   */
  public LocalDateTime getDateTime() {
    int year = (Integer) yearSpinner.getValue();
    int month = (Integer) monthSpinner.getValue();
    int day = (Integer) daySpinner.getValue();
    int hour = (Integer) hourSpinner.getValue();
    int minute = (Integer) minuteSpinner.getValue();
    return LocalDateTime.of(year, month, day, hour, minute);
  }

  /**
   * Adds a change listener to all JSpinners in the panel.
   *
   * @param listener the change listener to add.
   */
  public void addChangeListener(javax.swing.event.ChangeListener listener) {
    yearSpinner.addChangeListener(listener);
    monthSpinner.addChangeListener(listener);
    daySpinner.addChangeListener(listener);
    hourSpinner.addChangeListener(listener);
    minuteSpinner.addChangeListener(listener);
  }
}