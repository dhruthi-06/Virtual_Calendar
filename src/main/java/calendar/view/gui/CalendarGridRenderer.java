package calendar.view.gui;

import calendar.model.InterfaceEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Handles rendering of the calendar grid (monthly view).
 */
public class CalendarGridRenderer {
  private final JPanel gridPanel;
  private final CalendarColorManager colorManager;
  private final Map<LocalDate, JButton> dayButtons;

  /**
   * Constructs a CalendarGridRenderer.
   *
   * @param gridPanel the panel to render the grid into
   * @param colorManager the color manager for styling
   */
  public CalendarGridRenderer(JPanel gridPanel, CalendarColorManager colorManager) {
    this.gridPanel = gridPanel;
    this.colorManager = colorManager;
    this.dayButtons = new HashMap<>();
  }

  /**
   * Renders a month's calendar grid using context object.
   *
   * @param context the rendering context containing all necessary parameters
   */
  public void renderMonth(MonthRenderContext context) {
    gridPanel.removeAll();
    dayButtons.clear();

    LocalDate firstOfMonth = context.getMonth().atDay(1);
    int daysInMonth = context.getMonth().lengthOfMonth();
    int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

    Color calendarColor = colorManager.getCalendarColor(context.getCalendarName());

    addEmptyDays(dayOfWeek);
    addDayButtons(context, daysInMonth, calendarColor);

    gridPanel.revalidate();
    gridPanel.repaint();
  }

  /**
   * Adds empty labels for days before the first of the month.
   *
   * @param count number of empty days
   */
  private void addEmptyDays(int count) {
    for (int i = 0; i < count; i++) {
      gridPanel.add(new JLabel(""));
    }
  }

  /**
   * Adds day buttons for each day in the month.
   *
   * @param context the rendering context
   * @param daysInMonth number of days in the month
   * @param calendarColor the calendar color
   */
  private void addDayButtons(MonthRenderContext context, int daysInMonth, Color calendarColor) {
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = context.getMonth().atDay(day);
      JButton dayButton = createDayButton(date, context, calendarColor);
      dayButtons.put(date, dayButton);
      gridPanel.add(dayButton);
    }
  }

  /**
   * Creates a single day button.
   *
   * @param date the date for this button
   * @param context the rendering context
   * @param calendarColor the calendar color
   * @return configured JButton
   */
  private JButton createDayButton(LocalDate date, MonthRenderContext context,
                                  Color calendarColor) {
    JButton button = new JButton(String.valueOf(date.getDayOfMonth()));
    button.setPreferredSize(new Dimension(80, 80));
    button.setFont(new Font("Arial", Font.PLAIN, 12));

    configureDayButtonAppearance(button, date, context.getSelectedDate(), calendarColor);
    addDayButtonMouseListener(button, date, context.getSelectedDate(), calendarColor);
    addEventIndicator(button, date, context.getEventProvider());
    button.addActionListener(e -> context.getClickListener().onDayClicked(date));

    return button;
  }

  /**
   * Configures the appearance of a day button.
   *
   * @param button the button to configure
   * @param date the date
   * @param selectedDate the currently selected date
   * @param calendarColor the calendar color
   */
  private void configureDayButtonAppearance(JButton button, LocalDate date,
                                            LocalDate selectedDate, Color calendarColor) {
    boolean isSunday = date.getDayOfWeek().getValue() == 7;
    boolean isToday = date.equals(LocalDate.now());
    boolean isSelected = date.equals(selectedDate);

    Color baseColor = colorManager.calculateDayButtonColor(calendarColor, isToday, isSunday);
    button.setBackground(baseColor);

    if (isSelected) {
      Color darkerBorder = colorManager.calculateBorderColor(calendarColor);
      button.setBorder(BorderFactory.createLineBorder(darkerBorder, 3));
    } else {
      button.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    }

    button.setForeground(Color.BLACK);
    button.setOpaque(true);
    button.setBorderPainted(true);
    button.setFocusPainted(false);
  }

  /**
   * Adds mouse hover effects to a day button.
   *
   * @param button the button
   * @param date the date
   * @param selectedDate the selected date
   * @param calendarColor the calendar color
   */
  private void addDayButtonMouseListener(JButton button, LocalDate date,
                                         LocalDate selectedDate, Color calendarColor) {
    boolean isToday = date.equals(LocalDate.now());
    boolean isSelected = date.equals(selectedDate);
    boolean isSunday = date.getDayOfWeek().getValue() == 7;
    Color baseColor = colorManager.calculateDayButtonColor(calendarColor, isToday, isSunday);

    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        if (!isSelected && !isToday) {
          Color hoverColor = colorManager.calculateHoverColor(calendarColor);
          button.setBackground(hoverColor);
        }
      }

      public void mouseExited(java.awt.event.MouseEvent evt) {
        if (!isSelected && !isToday) {
          button.setBackground(baseColor);
        }
      }
    });
  }

  /**
   * Adds an event count indicator to a day button.
   *
   * @param button the button
   * @param date the date
   * @param eventProvider the event provider
   */
  private void addEventIndicator(JButton button, LocalDate date,
                                 MonthRenderContext.EventProvider eventProvider) {
    List<InterfaceEvent> events = eventProvider.getEventsForDate(date);
    if (!events.isEmpty()) {
      button.setText("<html><center>" + date.getDayOfMonth() + " "
          + events.size() + "</center></html>");
      button.setFont(new Font("Arial", Font.PLAIN, 11));
    }
  }
}