package calendar.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of InterfaceCalendarSystem.
 * Manages multiple calendars with different timezones.
 * Provides operations for creating, editing, and managing calendars and their events.
 */
public class CalendarSystem implements InterfaceCalendarSystem {

  private final Map<String, InterfaceCalendar> calendars;
  private final EventCopier eventCopier;

  /**
   * Creates a new calendar system with no calendars.
   */
  public CalendarSystem() {
    this.calendars = new HashMap<>();
    this.eventCopier = new EventCopier();
  }

  @Override
  public void createCalendar(String name, String timezone) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar with name '" + name + "' already exists");
    }

    try {
      ZoneId zoneId = ZoneId.of(timezone);
      InterfaceCalendar calendar = new Calendar(name, zoneId);
      calendars.put(name, calendar);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezone + ". "
          + "Use IANA timezone format (e.g., America/New_York)");
    }
  }

  @Override
  public InterfaceCalendar getCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar not found: " + name);
    }
    return calendars.get(name);
  }

  @Override
  public boolean calendarExists(String name) {
    return calendars.containsKey(name);
  }

  @Override
  public void editCalendarName(String oldName, String newName) {
    if (oldName == null || oldName.trim().isEmpty()) {
      throw new IllegalArgumentException("Old calendar name cannot be empty");
    }

    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("New calendar name cannot be empty");
    }

    if (!calendars.containsKey(oldName)) {
      throw new IllegalArgumentException("Calendar not found: " + oldName);
    }

    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("Calendar with name '" + newName + "' already exists");
    }

    InterfaceCalendar calendar = calendars.remove(oldName);
    calendar.setName(newName);
    calendars.put(newName, calendar);
  }

  @Override
  public void editCalendarTimezone(String calendarName, String newTimezone) {
    InterfaceCalendar calendar = getCalendar(calendarName);

    try {
      ZoneId zoneId = ZoneId.of(newTimezone);
      calendar.setTimezone(zoneId);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid timezone: " + newTimezone + ". "
          + "Use IANA timezone format (e.g., America/New_York)");
    }
  }

  @Override
  public List<String> getAllCalendarNames() {
    return new ArrayList<>(calendars.keySet());
  }

  @Override
  public void deleteCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar not found: " + name);
    }
    calendars.remove(name);
  }

  @Override
  public void createEvent(String calendarName, String subject, LocalDateTime start,
                          LocalDateTime end) {
    InterfaceCalendar calendar = getCalendar(calendarName);
    InterfaceEvent event = new Event(subject, start, end);
    calendar.addEvent(event);
  }

  @Override
  public void createEvent(String calendarName, EventCreationRequest request) {
    InterfaceCalendar calendar = getCalendar(calendarName);

    InterfaceEvent event = new Event.Builder(
        request.getSubject(),
        request.getStart(),
        request.getEnd())
        .description(request.getDescription())
        .location(request.getLocation())
        .isPublic(request.isPublic())
        .build();

    calendar.addEvent(event);
  }

  @Override
  public void createRecurringEvent(String calendarName, RecurringEventCreationRequest request) {
    InterfaceCalendar calendar = getCalendar(calendarName);

    RecurringEvent.Builder builder = new RecurringEvent.Builder(
        request.getSubject(),
        request.getStart(),
        request.getEnd(),
        request.getWeekdays());

    if (request.hasRepeatCount()) {
      builder.repeatCount(request.getRepeatCount());
    } else if (request.hasRepeatUntil()) {
      builder.repeatUntil(request.getRepeatUntil());
    }

    InterfaceRecurringEvent recurringEvent = builder.build();
    calendar.addRecurringEvent(recurringEvent);
  }

  @Override
  public void copyEvent(String sourceCalendar, EventCopyRequest request) {
    InterfaceCalendar source = getCalendar(sourceCalendar);
    InterfaceCalendar target = getCalendar(request.getTargetCalendar());

    eventCopier.copySingleEvent(source, request, target);
  }

  @Override
  public int copyEventsInRange(String sourceCalendar, String targetCalendar,
                               DateRangeCopyRequest request) {
    InterfaceCalendar source = getCalendar(sourceCalendar);
    InterfaceCalendar target = getCalendar(targetCalendar);

    if (request.isSingleDate()) {
      return eventCopier.copyEventsOnDate(source, target, request);
    } else {
      return eventCopier.copyEventsBetween(source, target, request);
    }
  }



  /**
   * Handles copying events between calendars.
   */
  private static class EventCopier {

    public void copySingleEvent(InterfaceCalendar source, EventCopyRequest request,
                                InterfaceCalendar target) {
      InterfaceEvent sourceEvent = source.findEvent(
          request.getEventName(),
          request.getSourceDateTime());

      if (sourceEvent == null) {
        throw new IllegalArgumentException("Event not found: " + request.getEventName()
            + " at " + request.getSourceDateTime());
      }

      InterfaceEvent copiedEvent = sourceEvent.copy();

      long minutesDiff = Duration.between(
          request.getSourceDateTime(),
          request.getTargetDateTime()).toMinutes();

      LocalDateTime newStart = copiedEvent.getStart().plusMinutes(minutesDiff);
      LocalDateTime newEnd = copiedEvent.getEnd().plusMinutes(minutesDiff);

      if (!source.getTimezone().equals(target.getTimezone())) {
        newStart = TimezoneConverter.convertBetweenTimezones(
            newStart, source.getTimezone(), target.getTimezone());
        newEnd = TimezoneConverter.convertBetweenTimezones(
            newEnd, source.getTimezone(), target.getTimezone());
      }

      copiedEvent.setEnd(newEnd);
      copiedEvent.setStart(newStart);

      if (copiedEvent.isPartOfSeries()) {
        copiedEvent.setSeriesId(UUID.randomUUID().toString());
      }

      target.addEvent(copiedEvent);
    }

    public int copyEventsOnDate(InterfaceCalendar source, InterfaceCalendar target,
                                DateRangeCopyRequest copyRequest) {
      List<InterfaceEvent> events = source.getEventsOnDate(copyRequest.getSourceStartDate());
      EventCopyContext context = new EventCopyContext(source, target, copyRequest.getDaysDiff());
      return copyEventsWithContext(events, context);
    }

    public int copyEventsBetween(InterfaceCalendar source, InterfaceCalendar target,
                                 DateRangeCopyRequest copyRequest) {
      LocalDateTime rangeStart = copyRequest.getSourceStartDate().atStartOfDay();
      LocalDateTime rangeEnd = copyRequest.getSourceEndDate().atTime(23, 59, 59);

      List<InterfaceEvent> events = source.getEventsInRange(rangeStart, rangeEnd);
      EventCopyContext context = new EventCopyContext(source, target, copyRequest.getDaysDiff());
      return copyEventsWithContext(events, context);
    }

    private int copyEventsWithContext(List<InterfaceEvent> events, EventCopyContext context) {
      int count = 0;

      for (InterfaceEvent event : events) {
        InterfaceEvent copiedEvent = createCopiedEvent(event, context);

        if (tryAddEventToTarget(context.target, copiedEvent, event)) {
          count++;
        }
      }

      return count;
    }

    private InterfaceEvent createCopiedEvent(InterfaceEvent event, EventCopyContext context) {
      InterfaceEvent copiedEvent = event.copy();

      LocalDateTime newStart = copiedEvent.getStart().plusDays(context.daysDiff);
      LocalDateTime newEnd = copiedEvent.getEnd().plusDays(context.daysDiff);

      if (context.needsTimezoneConversion()) {
        newStart = TimezoneConverter.convertBetweenTimezones(
            newStart, context.source.getTimezone(), context.target.getTimezone());
        newEnd = TimezoneConverter.convertBetweenTimezones(
            newEnd, context.source.getTimezone(), context.target.getTimezone());
      }

      copiedEvent.setEnd(newEnd);
      copiedEvent.setStart(newStart);

      updateSeriesIdIfNeeded(copiedEvent, context.seriesIdMap);

      return copiedEvent;
    }

    private void updateSeriesIdIfNeeded(InterfaceEvent copiedEvent,
                                        Map<String, String> seriesIdMap) {
      if (copiedEvent.isPartOfSeries()) {
        String oldSeriesId = copiedEvent.getSeriesId();
        if (!seriesIdMap.containsKey(oldSeriesId)) {
          seriesIdMap.put(oldSeriesId, UUID.randomUUID().toString());
        }
        copiedEvent.setSeriesId(seriesIdMap.get(oldSeriesId));
      }
    }

    private boolean tryAddEventToTarget(InterfaceCalendar target, InterfaceEvent copiedEvent,
                                        InterfaceEvent originalEvent) {
      try {
        target.addEvent(copiedEvent);
        return true;
      } catch (IllegalArgumentException e) {
        System.err.println("Skipped event due to conflict: " + originalEvent.getSubject());
        return false;
      }
    }
  }

  /**
   * Helper class to encapsulate event copying context.
   * Reduces parameter count in copy methods.
   */
  private static class EventCopyContext {
    final InterfaceCalendar source;
    final InterfaceCalendar target;
    final long daysDiff;
    final Map<String, String> seriesIdMap;

    EventCopyContext(InterfaceCalendar source, InterfaceCalendar target, long daysDiff) {
      this.source = source;
      this.target = target;
      this.daysDiff = daysDiff;
      this.seriesIdMap = new HashMap<>();
    }

    boolean needsTimezoneConversion() {
      return !source.getTimezone().equals(target.getTimezone());
    }
  }
}