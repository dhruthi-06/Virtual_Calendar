package calendar.view.gui;

import calendar.controller.CalendarGuiController;
import calendar.model.InterfaceEvent;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface for the graphical calendar view.
 */
public interface InterfaceCalendarGuiView {

  /**
   * Makes the view visible.
   */
  void setVisible(boolean visible);

  /**
   * Sets the controller features for this view.
   * Allows the view to communicate back to the controller.
   *
   * @param controller the calendar GUI controller
   */
  void setFeatures(CalendarGuiController controller);

  /**
   * Displays an error message to the user.
   *
   * @param message the error message
   */
  void showError(String message);

  /**
   * Displays an informational message to the user.
   *
   * @param message the message
   */
  void showMessage(String message);

  /**
   * Refreshes the calendar display for the current month.
   */
  void refreshCalendar();

  /**
   * Updates the events display for a specific date.
   *
   * @param date the date
   * @param events the events on that date
   */
  void updateEventsForDate(LocalDate date, List<InterfaceEvent> events);

  /**
   * Updates the list of available calendars.
   *
   * @param calendarNames list of calendar names
   */
  void updateCalendarList(List<String> calendarNames);

  /**
   * Sets the current calendar being displayed.
   *
   * @param calendarName the calendar name
   */
  void setCurrentCalendar(String calendarName);

  /**
   * Gets the currently displayed month.
   *
   * @return the current month date
   */
  LocalDate getCurrentMonth();

  /**
   * Gets the currently selected date.
   *
   * @return the selected date, or null if none selected
   */
  LocalDate getSelectedDate();
}