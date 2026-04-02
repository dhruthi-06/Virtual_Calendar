package calendar.view;

import calendar.model.InterfaceEvent;
import java.util.List;

/**
 * Interface for the calendar view.
 * Handles displaying information to the user.
 * Supports both interactive and headless modes.
 */
public interface InterfaceCalendarView {



  /**
   * Displays a message to the user.
   *
   * @param message the message to display
   */
  void displayMessage(String message);

  /**
   * Displays an error message to the user.
   *
   * @param error the error message to display
   */
  void displayError(String error);

  /**
   * Displays a list of events.
   *
   * @param events the events to display
   */
  void displayEvents(List<InterfaceEvent> events);

  /**
   * Displays events for a specific date.
   * Shows events in a bulleted list with time and location.
   *
   * @param events the events to display
   * @param date the date as a string
   */
  void displayEventsForDate(List<InterfaceEvent> events, String date);

  /**
   * Displays events in a date range.
   * Shows each event on a single line with full details.
   *
   * @param events the events to display
   * @param startDate the start date as a string
   * @param endDate the end date as a string
   */
  void displayEventsInRange(List<InterfaceEvent> events, String startDate, String endDate);



  /**
   * Displays the busy status at a specific date/time.
   *
   * @param isBusy true if busy, false if available
   * @param dateTime the date/time being checked
   */
  void displayStatus(boolean isBusy, String dateTime);



  /**
   * Displays a welcome message.
   * Typically shown at the start of interactive mode.
   */
  void displayWelcome();

  /**
   * Displays help information about available commands.
   */
  void displayHelp();



  /**
   * Displays a list of calendar names.
   *
   * @param calendarNames list of calendar names
   */
  void displayCalendarList(List<String> calendarNames);

  /**
   * Displays information about a specific calendar.
   *
   * @param name calendar name
   * @param timezone calendar timezone
   * @param eventCount number of events in calendar
   */
  void displayCalendarInfo(String name, String timezone, int eventCount);

  /**
   * Displays the calendar analytics dashboard for a date range.
   *
   * @param analytics the analytics result containing all metrics
   * @param startDate the start date of the range
   * @param endDate the end date of the range
   */
  void displayDashboard(calendar.util.CalendarAnalytics.AnalyticsResult analytics,
                       java.time.LocalDate startDate, java.time.LocalDate endDate);
}