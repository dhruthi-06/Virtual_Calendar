package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles conflict detection between events.
 * simple conflict checking.
 */
public class ConflictDetector {

  /**
   * Checks if an event conflicts with any existing events.
   * Two events conflict if they overlap in time.
   */
  public boolean hasConflict(InterfaceEvent newEvent, List<InterfaceEvent> existingEvents) {
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

  /**
   * Checks if an event is a duplicate (same subject, start, and end).
   */
  public boolean isDuplicate(InterfaceEvent newEvent, List<InterfaceEvent> existingEvents) {
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

  /**
   * Finds all events that conflict with the given event.
   * Useful for displaying which events are conflicting.
   *
   * @param event the event to check
   * @param existingEvents list of existing events
   * @return list of conflicting events (empty if no conflicts)
   */
  public List<InterfaceEvent> findConflictingEvents(InterfaceEvent event,
                                                    List<InterfaceEvent> existingEvents) {
    List<InterfaceEvent> conflicts = new ArrayList<>();

    if (event == null || existingEvents == null) {
      return conflicts;
    }

    for (InterfaceEvent existing : existingEvents) {
      if (event.overlapsWith(existing)) {
        conflicts.add(existing);
      }
    }

    return conflicts;
  }

  /**
   * Checks if an event can be scheduled without any conflicts or duplicates.
   * Combines both conflict and duplicate checks.
   *
   * @param event the event to check
   * @param existingEvents list of existing events
   * @return true if the event can be safely scheduled
   */
  public boolean canSchedule(InterfaceEvent event, List<InterfaceEvent> existingEvents) {
    if (event == null || existingEvents == null) {
      return true;
    }

    return !hasConflict(event, existingEvents) && !isDuplicate(event, existingEvents);
  }

  /**
   * Finds available time slots on a specific date.
   * Returns time slots where no events are scheduled.
   *
   * @param date the date to check
   * @param durationMinutes desired duration in minutes
   * @param existingEvents list of existing events
   * @return list of available start times (empty if no slots available)
   */
  public List<LocalDateTime> findAvailableSlots(LocalDate date,
                                                long durationMinutes,
                                                List<InterfaceEvent> existingEvents) {
    List<LocalDateTime> availableSlots = new ArrayList<>();

    if (date == null || durationMinutes <= 0) {
      return availableSlots;
    }

    LocalDateTime currentSlot = date.atTime(8, 0);
    LocalDateTime endOfDay = date.atTime(17, 0);

    while (currentSlot.plusMinutes(durationMinutes).isBefore(endOfDay)
        || currentSlot.plusMinutes(durationMinutes).equals(endOfDay)) {

      LocalDateTime slotEnd = currentSlot.plusMinutes(durationMinutes);


      InterfaceEvent tempEvent = new Event("temp", currentSlot, slotEnd);


      if (!hasConflict(tempEvent, existingEvents)) {
        availableSlots.add(currentSlot);
      }


      currentSlot = currentSlot.plusMinutes(30);
    }

    return availableSlots;
  }

  /**
   * Counts the number of conflicts an event would have.
   * Useful for determining severity of scheduling conflicts.
   *
   * @param event the event to check
   * @param existingEvents list of existing events
   * @return number of conflicting events
   */
  public int countConflicts(InterfaceEvent event, List<InterfaceEvent> existingEvents) {
    if (event == null || existingEvents == null) {
      return 0;
    }

    int count = 0;
    for (InterfaceEvent existing : existingEvents) {
      if (event.overlapsWith(existing)) {
        count++;
      }
    }
    return count;
  }

  /**
   * Checks if a time range is completely free (no events).
   *
   * @param start start of the time range
   * @param end end of the time range
   * @param existingEvents list of existing events
   * @return true if the time range has no events
   */
  public boolean isTimeRangeFree(LocalDateTime start, LocalDateTime end,
                                 List<InterfaceEvent> existingEvents) {
    if (start == null || end == null || existingEvents == null) {
      return true;
    }


    InterfaceEvent tempEvent = new Event("temp", start, end);
    return !hasConflict(tempEvent, existingEvents);
  }
}