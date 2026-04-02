package calendar.model;

import java.time.LocalDateTime;

/**
 * Interface for a calendar event.
 * Represents a single event with all its properties.
 */
public interface InterfaceEvent {

  /**
   * Gets the subject of the event.
   *
   * @return event subject (never null or empty)
   */
  String getSubject();

  /**
   * Sets the subject of the event.
   *
   * @param subject new subject
   * @throws IllegalArgumentException if subject is null or empty
   */
  void setSubject(String subject);

  /**
   * Gets the start date and time.
   *
   * @return start date/time (never null)
   */
  LocalDateTime getStart();

  /**
   * Sets the start date and time.
   *
   * @param start new start date/time
   * @throws IllegalArgumentException if start is null or after current end time
   */
  void setStart(LocalDateTime start);

  /**
   * Gets the end date and time.
   *
   * @return end date/time (never null)
   */
  LocalDateTime getEnd();

  /**
   * Sets the end date and time.
   *
   * @param end new end date/time
   * @throws IllegalArgumentException if end is null or before current start time
   */
  void setEnd(LocalDateTime end);

  /**
   * Gets the description.
   *
   * @return description (empty string if not set)
   */
  String getDescription();

  /**
   * Sets the description.
   *
   * @param description new description
   */
  void setDescription(String description);

  /**
   * Gets the location.
   *
   * @return location (empty string if not set)
   */
  String getLocation();

  /**
   * Sets the location.
   *
   * @param location new location
   */
  void setLocation(String location);

  /**
   * Checks if the event is public.
   *
   * @return true if public, false if private
   */
  boolean isPublic();

  /**
   * Sets the public/private status.
   *
   * @param isPublic true for public, false for private
   */
  void setPublic(boolean isPublic);

  /**
   * Checks if this is an all-day event.
   * An all-day event is defined as 8am to 5pm on the same day.
   *
   * @return true if all-day event
   */
  boolean isAllDay();

  /**
   * Checks if this event is part of a recurring series.
   *
   * @return true if part of a series
   */
  boolean isPartOfSeries();

  /**
   * Gets the series ID if this event is part of a series.
   *
   * @return series ID, or null if not part of a series
   */
  String getSeriesId();

  /**
   * Sets the series ID.
   *
   * @param seriesId the series ID (null to indicate not part of series)
   */
  void setSeriesId(String seriesId);

  // VALIDATION BEHAVIOR METHODS - Added to reduce getter/setter dominance
  // These methods encapsulate validation logic within the Event class
  // This promotes behavior-rich objects over anemic data holders

  /**
   * Validates and updates the subject of this event.
   * Encapsulates validation logic to promote behavior over simple setters.
   *
   * @param newSubject the new subject
   * @throws IllegalArgumentException if subject is invalid
   */
  void updateSubject(String newSubject);

  /**
   * Validates and updates the start time of this event.
   * Ensures start is before end time.
   * Encapsulates validation logic to promote behavior over simple setters.
   *
   * @param newStart the new start time
   * @throws IllegalArgumentException if invalid
   */
  void updateStart(LocalDateTime newStart);

  /**
   * Validates and updates the end time of this event.
   * Ensures end is after start time.
   * Encapsulates validation logic to promote behavior over simple setters.
   *
   * @param newEnd the new end time
   * @throws IllegalArgumentException if invalid
   */
  void updateEnd(LocalDateTime newEnd);

  /**
   * Updates the visibility status of this event.
   * Validates that status is either "public" or "private".
   * Encapsulates validation logic to promote behavior over simple setters.
   *
   * @param statusString "public" or "private"
   * @throws IllegalArgumentException if status is invalid
   */
  void updateStatus(String statusString);

  // OTHER BEHAVIOR METHODS

  /**
   * Checks if this event overlaps with another event.
   * Two events overlap if one starts before the other ends.
   *
   * @param other the other event
   * @return true if they overlap
   */
  boolean overlapsWith(InterfaceEvent other);

  /**
   * Checks if this event spans multiple days.
   *
   * @return true if start date and end date are different
   */
  boolean spansMultipleDays();

  /**
   * Gets the duration of the event in minutes.
   *
   * @return duration in minutes
   */
  long getDurationMinutes();

  /**
   * Creates a copy of this event.
   * The copy has the same properties but is a separate object.
   *
   * @return a new event with the same properties
   */
  InterfaceEvent copy();

  /**
   * Shifts this event forward or backward in time.
   * Both start and end times are adjusted by the same amount.
   *
   * @param minutes number of minutes to shift (positive = future, negative = past)
   */
  void shiftTime(long minutes);

  /**
   * Changes the duration of this event by adjusting the end time.
   * The start time remains unchanged.
   *
   * @param minutes new duration in minutes
   * @throws IllegalArgumentException if duration is not positive
   */
  void setDuration(long minutes);

  /**
   * Checks if this event occurs on a specific date.
   * Returns true if any part of the event falls on the given date.
   *
   * @param date the date to check
   * @return true if the event occurs on this date
   */
  boolean occursOnDate(LocalDateTime date);

  /**
   * Checks if this event is currently happening at a given time.
   * Returns true if the time falls between start (inclusive) and end (exclusive).
   *
   * @param time the time to check
   * @return true if the event is active at this time
   */
  boolean isActiveAt(LocalDateTime time);

  /**
   * Determines if this event matches another event exactly.
   * Two events match if they have the same subject, start time, and end time.
   *
   * @param other the other event
   * @return true if events match exactly
   */
  boolean matches(InterfaceEvent other);
}