package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for managing multiple calendars.
 * Represents the entire calendar system that can contain multiple calendars.
 * Each calendar has its own timezone and set of events.
 */
public interface InterfaceCalendarSystem {

  /**
   * Creates a new calendar with the given name and timezone.
   *
   * @param name unique name for the calendar
   * @param timezone IANA timezone format (e.g., "America/New_York")
   * @throws IllegalArgumentException if name is not unique or timezone is invalid
   */
  void createCalendar(String name, String timezone);

  /**
   * Gets a calendar by name.
   *
   * @param name the calendar name
   * @return the calendar
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  InterfaceCalendar getCalendar(String name);

  /**
   * Checks if a calendar exists.
   *
   * @param name the calendar name
   * @return true if calendar exists
   */
  boolean calendarExists(String name);

  /**
   * Edits the name of a calendar.
   *
   * @param oldName current calendar name
   * @param newName new calendar name
   * @throws IllegalArgumentException if old name doesn't exist or new name already exists
   */
  void editCalendarName(String oldName, String newName);

  /**
   * Edits the timezone of a calendar.
   *
   * @param calendarName the calendar name
   * @param newTimezone new timezone in IANA format
   * @throws IllegalArgumentException if calendar doesn't exist or timezone is invalid
   */
  void editCalendarTimezone(String calendarName, String newTimezone);

  /**
   * Gets all calendar names in the system.
   *
   * @return list of calendar names
   */
  List<String> getAllCalendarNames();

  /**
   * Deletes a calendar from the system.
   *
   * @param name the calendar name to delete
   * @throws IllegalArgumentException if calendar doesn't exist
   */
  void deleteCalendar(String name);

  /**
   * Creates a single event in the specified calendar.
   *
   * @param calendarName the calendar to add the event to
   * @param subject event subject
   * @param start start date and time
   * @param end end date and time
   * @throws IllegalArgumentException if calendar doesn't exist or event conflicts
   */
  void createEvent(String calendarName, String subject, LocalDateTime start, LocalDateTime end);

  /**
   * Creates a single event with all properties.
   *
   * @param calendarName the calendar to add the event to
   * @param request event creation request with all properties
   * @throws IllegalArgumentException if calendar doesn't exist or event conflicts
   */
  void createEvent(String calendarName, EventCreationRequest request);

  /**
   * Creates a recurring event.
   *  Uses RecurringEventCreationRequest parameter object.
   *
   * @param calendarName the calendar to add the event to
   * @param request recurring event creation request
   * @throws IllegalArgumentException if calendar doesn't exist or any occurrence conflicts
   */
  void createRecurringEvent(String calendarName, RecurringEventCreationRequest request);

  /**
   * Copies a single event from source calendar to target calendar.
   * Uses EventCopyRequest parameter object.
   *
   * @param sourceCalendar source calendar name
   * @param request event copy request
   * @throws IllegalArgumentException if calendars don't exist or event not found
   */
  void copyEvent(String sourceCalendar, EventCopyRequest request);

  /**
   * Copies events in a date range from source to target calendar.
   *
   * @param sourceCalendar source calendar name
   * @param targetCalendar target calendar name
   * @param request date range copy request
   * @return number of events copied
   * @throws IllegalArgumentException if calendars don't exist
   */
  int copyEventsInRange(String sourceCalendar, String targetCalendar,
                        DateRangeCopyRequest request);
}