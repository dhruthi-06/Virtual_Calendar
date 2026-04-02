package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages event storage and retrieval.
 */
public class EventRepository {

  private final List<InterfaceEvent> events;

  /**
   * Constructs an EventRepository.
   */
  public EventRepository() {
    this.events = new ArrayList<>();
  }

  /**
   * Adds an event to the repository.
   *
   * @param event the event to add
   */
  public void addEvent(InterfaceEvent event) {
    events.add(event);
  }

  /**
   * Removes an event from the repository.
   *
   * @param event the event to remove
   * @return true if the event was removed
   */
  public boolean removeEvent(InterfaceEvent event) {
    return events.remove(event);
  }

  /**
   * Gets all events in the repository.
   *
   * @return list of all events
   */
  public List<InterfaceEvent> getAllEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Gets the count of events.
   *
   * @return the number of events
   */
  public int getEventCount() {
    return events.size();
  }

  /**
   * Finds an event by subject and start time.
   *
   * @param subject the event subject
   * @param start the start time
   * @return the found event, or null if not found
   */
  public InterfaceEvent findEvent(String subject, LocalDateTime start) {
    InterfaceEvent found = null;
    for (InterfaceEvent event : events) {
      if (event.getSubject().equals(subject) && event.getStart().equals(start)) {
        if (found != null) {
          throw new IllegalArgumentException(
              "Multiple events found with subject '" + subject + "' starting at " + start
                  + ". Cannot uniquely identify event.");
        }
        found = event;
      }
    }
    return found;
  }

  /**
   * Gets all events on a specific date.
   *
   * @param date the date to search
   * @return list of events on the date
   */
  public List<InterfaceEvent> getEventsOnDate(LocalDate date) {
    List<InterfaceEvent> result = new ArrayList<>();

    for (InterfaceEvent event : events) {
      LocalDate eventStartDate = event.getStart().toLocalDate();
      LocalDate eventEndDate = event.getEnd().toLocalDate();

      if (!date.isBefore(eventStartDate) && !date.isAfter(eventEndDate)) {
        result.add(event);
      }
    }

    return result;
  }

  /**
   * Gets all events in a date/time range.
   *
   * @param start the start of the range
   * @param end the end of the range
   * @return list of events in the range
   */
  public List<InterfaceEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    List<InterfaceEvent> result = new ArrayList<>();

    for (InterfaceEvent event : events) {
      if (event.getStart().isBefore(end) && event.getEnd().isAfter(start)) {
        result.add(event);
      }
    }

    return result;
  }

  /**
   * Checks if there is an event at a specific date/time.
   *
   * @param dateTime the date/time to check
   * @return true if busy at the specified time
   */
  public boolean isBusyAt(LocalDateTime dateTime) {
    for (InterfaceEvent event : events) {
      if (!dateTime.isBefore(event.getStart()) && dateTime.isBefore(event.getEnd())) {
        return true;
      }
    }
    return false;
  }
}