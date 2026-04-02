package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Interface for a recurring event.
 * Represents an event that repeats on specific weekdays.
 */
public interface InterfaceRecurringEvent {


  /**
   * Gets the base event template.
   *
   * @return the base event
   */
  InterfaceEvent getBaseEvent();

  /**
   * Gets the subject of the recurring event.
   *
   * @return event subject
   */
  String getSubject();

  /**
   * Gets the start time (time of day) for all occurrences.
   *
   * @return start time
   */
  LocalDateTime getStartTime();

  /**
   * Gets the end time (time of day) for all occurrences.
   *
   * @return end time
   */
  LocalDateTime getEndTime();


  /**
   * Gets the weekdays on which this event repeats.
   *
   * @return set of weekday codes (M=Monday, T=Tuesday, W=Wednesday,
   *         R=Thursday, F=Friday, S=Saturday, U=Sunday)
   */
  Set<String> getWeekdays();

  /**
   * Gets the repeat count.
   *
   * @return number of occurrences, or null if repeating until date
   */
  Integer getRepeatCount();

  /**
   * Gets the repeat until date.
   *
   * @return end date for recurrence (inclusive), or null if using repeat count
   */
  LocalDateTime getRepeatUntil();

  /**
   * Gets the series ID.
   * All occurrences of this recurring event share this ID.
   *
   * @return unique series identifier
   */
  String getSeriesId();


  /**
   * Generates all event occurrences.
   * Creates individual Event objects for each occurrence.
   * All occurrences will have the same series ID.
   *
   * @return list of all event instances
   */
  List<InterfaceEvent> generateOccurrences();

  /**
   * Generates occurrences within a date range.
   * Only creates events that fall within the specified range.
   *
   * @param start start date (inclusive)
   * @param end end date (inclusive)
   * @return list of event instances in range
   */
  List<InterfaceEvent> generateOccurrencesInRange(LocalDate start, LocalDate end);

  /**
   * Checks if this series has occurrences on a specific date.
   *
   * @param date the date to check
   * @return true if there are occurrences on that date
   */
  boolean hasOccurrenceOn(LocalDate date);

  /**
   * Gets the first occurrence date.
   *
   * @return the date of the first occurrence
   */
  LocalDate getFirstOccurrenceDate();

  /**
   * Gets the last occurrence date.
   *
   * @return the date of the last occurrence
   */
  LocalDate getLastOccurrenceDate();

  /**
   * Gets the total number of occurrences.
   *
   * @return total occurrence count
   */
  int getOccurrenceCount();
}