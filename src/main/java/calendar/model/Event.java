package calendar.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Implementation of InterfaceEvent.
 * Represents a single calendar event with all its properties.
 */
public class Event implements InterfaceEvent {

  private String subject;
  private LocalDateTime start;
  private LocalDateTime end;
  private String description;
  private String location;
  private boolean isPublic;
  private String seriesId;

  /**
   * Constructor with required fields only.
   * Optional fields default to empty strings and public status.
   *
   * @param subject event subject
   * @param start start date/time
   * @param end end date/time
   * @throws IllegalArgumentException if validation fails
   */
  public Event(String subject, LocalDateTime start, LocalDateTime end) {
    validateSubject(subject);
    validateStartTime(start);
    validateEndTime(end);
    validateTimesOrder(start, end);

    this.subject = subject.trim();
    this.start = start;
    this.end = end;
    this.description = "";
    this.location = "";
    this.isPublic = true;
    this.seriesId = null;
  }

  /**
   * Constructor for all-day events.
   * Creates an event from 8am to 5pm on the given date.
   *
   * @param subject event subject
   * @param date the date
   * @throws IllegalArgumentException if validation fails
   */
  public Event(String subject, LocalDateTime date) {
    this(subject,
        date.withHour(8).withMinute(0).withSecond(0).withNano(0),
        date.withHour(17).withMinute(0).withSecond(0).withNano(0));
  }

  /**
   * Builder pattern for creating Events with optional fields.
   */
  public static class Builder {
    private final String subject;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private String description = "";
    private String location = "";
    private boolean isPublic = true;

    /**
     * Creates a builder with required fields.
     *
     * @param subject event subject
     * @param start start date/time
     * @param end end date/time
     */
    public Builder(String subject, LocalDateTime start, LocalDateTime end) {
      this.subject = subject;
      this.start = start;
      this.end = end;
    }

    /**
     * Sets the description.
     *
     * @param description event description
     * @return this builder
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location.
     *
     * @param location event location
     * @return this builder
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the public/private status.
     *
     * @param isPublic true for public, false for private
     * @return this builder
     */
    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Builds the Event.
     *
     * @return a new Event instance
     */
    public Event build() {
      Event event = new Event(subject, start, end);
      event.setDescription(description);
      event.setLocation(location);
      event.setPublic(isPublic);
      return event;
    }
  }

  private void validateSubject(String subject) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Event subject cannot be null or empty");
    }
  }

  private void validateStartTime(LocalDateTime start) {
    if (start == null) {
      throw new IllegalArgumentException("Event start time cannot be null");
    }
  }

  private void validateEndTime(LocalDateTime end) {
    if (end == null) {
      throw new IllegalArgumentException("Event end time cannot be null");
    }
  }

  private void validateTimesOrder(LocalDateTime start, LocalDateTime end) {
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("Event end time cannot be before start time");
    }
    if (end.equals(start)) {
      throw new IllegalArgumentException("Event end time cannot equal start time");
    }
  }

  /**
   * Validates and updates the subject of this event.
   * Encapsulates validation logic within the Event class.
   *
   * @param newSubject the new subject
   * @throws IllegalArgumentException if subject is invalid
   */
  @Override
  public void updateSubject(String newSubject) {
    if (newSubject == null || newSubject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be empty");
    }
    this.subject = newSubject.trim();
  }

  /**
   * Validates and updates the start time of this event.
   * Ensures start is before end time.
   *
   * @param newStart the new start time
   * @throws IllegalArgumentException if invalid
   */
  @Override
  public void updateStart(LocalDateTime newStart) {
    if (newStart == null) {
      throw new IllegalArgumentException("Start time cannot be null");
    }
    if (newStart.isAfter(this.end)) {
      throw new IllegalArgumentException("Start time cannot be after end time");
    }
    if (newStart.equals(this.end)) {
      throw new IllegalArgumentException("Start time cannot equal end time");
    }
    this.start = newStart;
  }

  /**
   * Validates and updates the end time of this event.
   * Ensures end is after start time.
   *
   * @param newEnd the new end time
   * @throws IllegalArgumentException if invalid
   */
  @Override
  public void updateEnd(LocalDateTime newEnd) {
    if (newEnd == null) {
      throw new IllegalArgumentException("End time cannot be null");
    }
    if (newEnd.isBefore(this.start)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
    if (newEnd.equals(this.start)) {
      throw new IllegalArgumentException("End time cannot equal start time");
    }
    this.end = newEnd;
  }

  /**
   * Updates the visibility status of this event.
   * Validates that status is either "public" or "private".
   *
   * @param statusString "public" or "private"
   * @throws IllegalArgumentException if status is invalid
   */
  @Override
  public void updateStatus(String statusString) {
    if (!statusString.equalsIgnoreCase("public")
        && !statusString.equalsIgnoreCase("private")) {
      throw new IllegalArgumentException("Status must be 'public' or 'private'");
    }
    this.isPublic = statusString.equalsIgnoreCase("public");
  }

  /**
   * Checks if this event conflicts with another event in time.
   *
   * @param other the other event to check against
   * @return true if the events overlap in time
   */
  @Override
  public boolean overlapsWith(InterfaceEvent other) {
    if (other == null) {
      return false;
    }
    return this.start.isBefore(other.getEnd()) && this.end.isAfter(other.getStart());
  }

  /**
   * Checks if this event spans multiple calendar days.
   *
   * @return true if the event crosses midnight
   */
  @Override
  public boolean spansMultipleDays() {
    return !start.toLocalDate().equals(end.toLocalDate());
  }

  /**
   * Calculates the duration of this event in minutes.
   *
   * @return the event duration in minutes
   */
  @Override
  public long getDurationMinutes() {
    return Duration.between(start, end).toMinutes();
  }

  /**
   * Checks if this event is an all-day event.
   * An all-day event is defined as starting at 8:00 AM and ending at 5:00 PM on the same day.
   *
   * @return true if this is an all-day event
   */
  @Override
  public boolean isAllDay() {
    LocalTime startTime = start.toLocalTime();
    LocalTime endTime = end.toLocalTime();
    return start.toLocalDate().equals(end.toLocalDate())
        && startTime.equals(LocalTime.of(8, 0))
        && endTime.equals(LocalTime.of(17, 0));
  }

  /**
   * Checks if this event is part of a recurring series.
   *
   * @return true if this event has a series ID
   */
  @Override
  public boolean isPartOfSeries() {
    return seriesId != null;
  }

  /**
   * Creates a deep copy of this event.
   * The copy has identical properties but is a separate object.
   *
   * @return a new Event object with the same properties
   */
  @Override
  public InterfaceEvent copy() {
    Event copy = new Event(this.subject, this.start, this.end);
    copy.setDescription(this.description);
    copy.setLocation(this.location);
    copy.setPublic(this.isPublic);
    copy.setSeriesId(this.seriesId);
    return copy;
  }

  /**
   * Shifts this event forward or backward in time.
   *
   * @param minutes number of minutes to shift (positive = future, negative = past)
   */
  @Override
  public void shiftTime(long minutes) {
    this.start = this.start.plusMinutes(minutes);
    this.end = this.end.plusMinutes(minutes);
  }

  /**
   * Changes the duration of this event by adjusting the end time.
   *
   * @param minutes new duration in minutes
   * @throws IllegalArgumentException if duration is not positive
   */
  @Override
  public void setDuration(long minutes) {
    if (minutes <= 0) {
      throw new IllegalArgumentException("Duration must be positive");
    }
    this.end = this.start.plusMinutes(minutes);
  }

  /**
   * Checks if this event occurs on a specific date.
   *
   * @param date the date to check
   * @return true if the event occurs on this date
   */
  @Override
  public boolean occursOnDate(LocalDateTime date) {
    LocalDateTime dateStart = date.toLocalDate().atStartOfDay();
    LocalDateTime dateEnd = date.toLocalDate().atTime(23, 59, 59);
    return this.start.isBefore(dateEnd) && this.end.isAfter(dateStart);
  }

  /**
   * Checks if this event is currently happening at a given time.
   *
   * @param time the time to check
   * @return true if the event is active at this time
   */
  @Override
  public boolean isActiveAt(LocalDateTime time) {
    return !time.isBefore(this.start) && time.isBefore(this.end);
  }

  /**
   * Determines if this event matches another event exactly.
   * Two events match if they have the same subject, start time, and end time.
   *
   * @param other the other event
   * @return true if events match exactly
   */
  @Override
  public boolean matches(InterfaceEvent other) {
    if (other == null) {
      return false;
    }
    return this.subject.equals(other.getSubject())
        && this.start.equals(other.getStart())
        && this.end.equals(other.getEnd());
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public void setSubject(String subject) {
    validateSubject(subject);
    this.subject = subject.trim();
  }

  @Override
  public LocalDateTime getStart() {
    return start;
  }

  @Override
  public void setStart(LocalDateTime start) {
    validateStartTime(start);
    if (this.end != null && start.isAfter(this.end)) {
      throw new IllegalArgumentException("Start time cannot be after end time");
    }
    if (this.end != null && start.equals(this.end)) {
      throw new IllegalArgumentException("Start time cannot equal end time");
    }
    this.start = start;
  }

  @Override
  public LocalDateTime getEnd() {
    return end;
  }

  @Override
  public void setEnd(LocalDateTime end) {
    validateEndTime(end);
    if (this.start != null && end.isBefore(this.start)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
    if (this.start != null && end.equals(this.start)) {
      throw new IllegalArgumentException("End time cannot equal start time");
    }
    this.end = end;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String description) {
    this.description = description != null ? description : "";
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public void setLocation(String location) {
    this.location = location != null ? location : "";
  }

  @Override
  public boolean isPublic() {
    return isPublic;
  }

  @Override
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  @Override
  public String getSeriesId() {
    return seriesId;
  }

  @Override
  public void setSeriesId(String seriesId) {
    this.seriesId = seriesId;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(subject);
    sb.append(" from ").append(start);
    sb.append(" to ").append(end);

    if (!location.isEmpty()) {
      sb.append(" at ").append(location);
    }

    if (isPartOfSeries()) {
      sb.append(" [Series]");
    }

    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Event)) {
      return false;
    }

    Event other = (Event) obj;
    return this.subject.equals(other.subject)
        && this.start.equals(other.start)
        && this.end.equals(other.end);
  }

  @Override
  public int hashCode() {
    int result = subject.hashCode();
    result = 31 * result + start.hashCode();
    result = 31 * result + end.hashCode();
    return result;
  }
}