package calendar.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Utility class for timezone conversions.
 */
public class TimezoneConverter {

  private TimezoneConverter() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Converts a LocalDateTime from one timezone to another,
   * preserving the physical instant in time.
   *
   * @param time the time to convert
   * @param fromZone the source timezone
   * @param toZone the target timezone
   * @return the converted time in the target timezone
   */
  public static LocalDateTime convertBetweenTimezones(
      LocalDateTime time, ZoneId fromZone, ZoneId toZone) {
    ZonedDateTime timeInOldZone = time.atZone(fromZone);
    ZonedDateTime timeInNewZone = timeInOldZone.withZoneSameInstant(toZone);
    return timeInNewZone.toLocalDateTime();
  }

  /**
   * Converts all events in a list from one timezone to another.
   * Updates the start and end times of each event in place.
   *
   * @param events the list of events to convert
   * @param fromZone the source timezone
   * @param toZone the target timezone
   */
  public static void convertAllEvents(List<InterfaceEvent> events,
                                      ZoneId fromZone, ZoneId toZone) {
    if (fromZone.equals(toZone)) {
      return;
    }

    for (InterfaceEvent event : events) {
      LocalDateTime newStart = convertBetweenTimezones(
          event.getStart(), fromZone, toZone);
      LocalDateTime newEnd = convertBetweenTimezones(
          event.getEnd(), fromZone, toZone);
      event.setStart(newStart);
      event.setEnd(newEnd);
    }
  }
}