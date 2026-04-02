package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of InterfaceCalendar.
 * Represents a single calendar with events and timezone support.
 * Delegates to specialized classes for storage.
 */
public class Calendar implements InterfaceCalendar {

  private String name;
  private ZoneId timezone;
  private final EventRepository repository;

  /**
   * Constructs a Calendar.
   *
   * @param name the calendar name
   * @param timezone the timezone
   */
  public Calendar(String name, ZoneId timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }

    this.name = name;
    this.timezone = timezone;
    this.repository = new EventRepository();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    this.name = name;
  }

  @Override
  public ZoneId getTimezone() {
    return timezone;
  }

  @Override
  public void setTimezone(ZoneId newTimezone) {
    if (newTimezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }

    ZoneId oldTimezone = this.timezone;
    this.timezone = newTimezone;

    if (!oldTimezone.equals(newTimezone)) {
      for (InterfaceEvent event : repository.getAllEvents()) {
        LocalDateTime newStart = TimezoneUtils.convertBetweenTimezones(
            event.getStart(), oldTimezone, newTimezone);
        LocalDateTime newEnd = TimezoneUtils.convertBetweenTimezones(
            event.getEnd(), oldTimezone, newTimezone);
        event.setStart(newStart);
        event.setEnd(newEnd);
      }
    }
  }

  @Override
  public void addEvent(InterfaceEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (isDuplicate(event, repository.getAllEvents())) {
      throw new IllegalArgumentException(
          "Event with same subject, start, and end time already exists");
    }

    repository.addEvent(event);
  }

  @Override
  public void addRecurringEvent(InterfaceRecurringEvent recurringEvent) {
    if (recurringEvent == null) {
      throw new IllegalArgumentException("Recurring event cannot be null");
    }

    List<InterfaceEvent> occurrences = recurringEvent.generateOccurrences();

    for (InterfaceEvent occurrence : occurrences) {
      if (isDuplicate(occurrence, repository.getAllEvents())) {
        throw new IllegalArgumentException(
            "Recurring event creates duplicate event on "
                + occurrence.getStart().toLocalDate());
      }
    }

    for (InterfaceEvent occurrence : occurrences) {
      repository.addEvent(occurrence);
    }
  }

  @Override
  public boolean removeEvent(String subject, LocalDateTime start) {
    InterfaceEvent event = repository.findEvent(subject, start);
    if (event == null) {
      return false;
    }
    return repository.removeEvent(event);
  }

  @Override
  public InterfaceEvent findEvent(String subject, LocalDateTime start) {
    return repository.findEvent(subject, start);
  }

  @Override
  public void editEvent(String subject, LocalDateTime start, String property, String newValue) {
    InterfaceEvent event = repository.findEvent(subject, start);
    if (event == null) {
      throw new IllegalArgumentException("Event not found: " + subject + " at " + start);
    }

    updateEventProperty(event, property, newValue);
  }

  @Override
  public void editEventsFromDate(String subject, LocalDateTime start, String property,
                                 String newValue) {
    InterfaceEvent event = repository.findEvent(subject, start);
    if (event == null) {
      throw new IllegalArgumentException("Event not found: " + subject + " at " + start);
    }

    if (!event.isPartOfSeries()) {
      editEvent(subject, start, property, newValue);
      return;
    }

    String seriesId = event.getSeriesId();

    if (property.equalsIgnoreCase("start")) {
      splitSeriesFromDate(seriesId, start, property, newValue);
    } else {
      for (InterfaceEvent e : repository.getAllEvents()) {
        if (e.isPartOfSeries()
            && e.getSeriesId().equals(seriesId)
            && !e.getStart().isBefore(start)) {
          updateEventProperty(e, property, newValue);
        }
      }
    }
  }

  @Override
  public void editEntireSeries(String subject, LocalDateTime start, String property,
                               String newValue) {
    InterfaceEvent event = repository.findEvent(subject, start);
    if (event == null) {
      throw new IllegalArgumentException("Event not found: " + subject + " at " + start);
    }

    if (!event.isPartOfSeries()) {
      editEvent(subject, start, property, newValue);
      return;
    }

    String seriesId = event.getSeriesId();

    if (property.equalsIgnoreCase("start")) {
      splitEntireSeries(seriesId, property, newValue);
    } else {
      for (InterfaceEvent e : repository.getAllEvents()) {
        if (e.isPartOfSeries() && e.getSeriesId().equals(seriesId)) {
          updateEventProperty(e, property, newValue);
        }
      }
    }
  }

  private void splitSeriesFromDate(String seriesId, LocalDateTime splitDate,
                                   String property, String newValue) {
    String newSeriesId = UUID.randomUUID().toString();

    for (InterfaceEvent e : repository.getAllEvents()) {
      if (e.isPartOfSeries()
          && e.getSeriesId().equals(seriesId)
          && !e.getStart().isBefore(splitDate)) {
        e.setSeriesId(newSeriesId);
        updateEventProperty(e, property, newValue);
      }
    }
  }

  private void splitEntireSeries(String seriesId, String property, String newValue) {
    String newSeriesId = UUID.randomUUID().toString();

    for (InterfaceEvent e : repository.getAllEvents()) {
      if (e.isPartOfSeries() && e.getSeriesId().equals(seriesId)) {
        e.setSeriesId(newSeriesId);
        updateEventProperty(e, property, newValue);
      }
    }
  }

  @Override
  public List<InterfaceEvent> getEventsOnDate(LocalDate date) {
    return repository.getEventsOnDate(date);
  }

  @Override
  public List<InterfaceEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    return repository.getEventsInRange(start, end);
  }

  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    return repository.isBusyAt(dateTime);
  }

  @Override
  public List<InterfaceEvent> getAllEvents() {
    return repository.getAllEvents();
  }

  @Override
  public int getEventCount() {
    return repository.getEventCount();
  }

  @Override
  public boolean hasConflict(InterfaceEvent newEvent) {
    return hasConflictWithExisting(newEvent, repository.getAllEvents());
  }


  private boolean hasConflictWithExisting(InterfaceEvent newEvent,
                                          List<InterfaceEvent> existingEvents) {
    if (newEvent == null || existingEvents == null) {
      return false;
    }

    for (InterfaceEvent existing : existingEvents) {
      if (newEvent.overlapsWith(existing)) {
        return true;
      }
    }
    return false;
  }

  private boolean isDuplicate(InterfaceEvent newEvent, List<InterfaceEvent> existingEvents) {
    if (newEvent == null || existingEvents == null) {
      return false;
    }

    for (InterfaceEvent existing : existingEvents) {
      if (existing.getSubject().equals(newEvent.getSubject())
          && existing.getStart().equals(newEvent.getStart())
          && existing.getEnd().equals(newEvent.getEnd())) {
        return true;
      }
    }
    return false;
  }



  private void updateEventProperty(InterfaceEvent event, String property, String newValue) {
    String normalizedProperty = property.toLowerCase();

    switch (normalizedProperty) {
      case "subject":
        event.updateSubject(newValue);
        break;
      case "start":
        updateStartTime(event, newValue);
        break;
      case "end":
        updateEndTime(event, newValue);
        break;
      case "description":
        event.setDescription(newValue != null ? newValue : "");
        break;
      case "location":
        event.setLocation(newValue != null ? newValue : "");
        break;
      case "status":
        event.updateStatus(newValue);
        break;
      default:
        throw new IllegalArgumentException("Invalid property: " + property
            + ". Valid properties: subject, start, end, description, location, status");
    }
  }

  private void updateStartTime(InterfaceEvent event, String newValue) {
    try {
      LocalDateTime newStart = LocalDateTime.parse(newValue);
      event.updateStart(newStart);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + newValue
          + ". Expected: YYYY-MM-DDTHH:mm");
    }
  }

  private void updateEndTime(InterfaceEvent event, String newValue) {
    try {
      LocalDateTime newEnd = LocalDateTime.parse(newValue);
      event.updateEnd(newEnd);
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid date/time format: " + newValue
          + ". Expected: YYYY-MM-DDTHH:mm");
    }
  }


  /**
   * Utility class for timezone conversions.
   * Centralizes timezone logic to prevent divergent change.
   */
  private static class TimezoneUtils {
    /**
     * Converts a LocalDateTime from one timezone to another,
     * preserving the physical instant in time.
     */
    static LocalDateTime convertBetweenTimezones(
        LocalDateTime time, ZoneId fromZone, ZoneId toZone) {
      ZonedDateTime timeInOldZone = time.atZone(fromZone);
      ZonedDateTime timeInNewZone = timeInOldZone.withZoneSameInstant(toZone);
      return timeInNewZone.toLocalDateTime();
    }
  }
}