package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Interface for a calendar that manages events.
 * Represents a single calendar with its own timezone and events.
 */
public interface InterfaceCalendar {

  /**
   * Gets the name of this calendar.
   *
   * @return calendar name
   */
  String getName();

  /**
   * Sets the name of this calendar.
   *
   * @param name new name
   * @throws IllegalArgumentException if name is null or empty
   */
  void setName(String name);

  /**
   * Gets the timezone of this calendar.
   *
   * @return timezone as ZoneId
   */
  ZoneId getTimezone();

  /**
   * Sets the timezone of this calendar.
   *
   * @param timezone new timezone
   * @throws IllegalArgumentException if timezone is null
   */
  void setTimezone(ZoneId timezone);


  /**
   * Adds a single event to the calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event conflicts with existing events
   *         or if duplicate event exists (same subject, start, and end)
   */
  void addEvent(InterfaceEvent event);

  /**
   * Adds a recurring event series to the calendar.
   * Generates all occurrences and adds them to the calendar.
   *
   * @param event the recurring event
   * @throws IllegalArgumentException if any occurrence conflicts
   */
  void addRecurringEvent(InterfaceRecurringEvent event);

  /**
   * Removes an event from the calendar.
   *
   * @param subject event subject
   * @param start event start date/time
   * @return true if event was removed, false if not found
   */
  boolean removeEvent(String subject, LocalDateTime start);

  /**
   * Finds an event by subject and start date/time.
   *
   * @param subject event subject
   * @param start event start date/time
   * @return the event, or null if not found
   */
  InterfaceEvent findEvent(String subject, LocalDateTime start);



  /**
   * Edits a single event instance.
   *
   * @param subject event subject
   * @param start event start date/time
   * @param property property to edit (subject, start, end, description, location, status)
   * @param newValue new value
   * @throws IllegalArgumentException if event not found or property invalid
   */
  void editEvent(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Edits all events in a series starting from a specific date.
   * If the start time is changed, creates a new series for events from that date forward.
   *
   * @param subject event subject
   * @param start start date/time of the event to begin editing from
   * @param property property to edit
   * @param newValue new value
   * @throws IllegalArgumentException if event not found or property invalid
   */
  void editEventsFromDate(String subject, LocalDateTime start, String property, String newValue);

  /**
   * Edits all events in a series.
   * If the start time is changed, all events get a new series ID.
   *
   * @param subject event subject
   * @param start any start date/time in the series
   * @param property property to edit
   * @param newValue new value
   * @throws IllegalArgumentException if event not found or property invalid
   */
  void editEntireSeries(String subject, LocalDateTime start, String property, String newValue);


  /**
   * Gets all events scheduled on a specific date.
   * Includes events that start on that date or span across it.
   *
   * @param date the date
   * @return list of events (empty if none)
   */
  List<InterfaceEvent> getEventsOnDate(LocalDate date);

  /**
   * Gets all events that overlap with a date/time range.
   * An event overlaps if any part of it falls within the range.
   *
   * @param start start of range (inclusive)
   * @param end end of range (inclusive)
   * @return list of events (empty if none)
   */
  List<InterfaceEvent> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Checks if the user is busy at a specific date/time.
   *
   * @param dateTime the date/time to check
   * @return true if there's an event at that time, false otherwise
   */
  boolean isBusyAt(LocalDateTime dateTime);

  /**
   * Gets all events in the calendar.
   *
   * @return list of all events (empty if none)
   */
  List<InterfaceEvent> getAllEvents();

  /**
   * Gets the total number of events in the calendar.
   *
   * @return event count
   */
  int getEventCount();


  /**
   * Checks if adding an event would cause a conflict.
   * Two events conflict if they overlap in time.
   *
   * @param event the event to check
   * @return true if there's a conflict, false otherwise
   */
  boolean hasConflict(InterfaceEvent event);
}