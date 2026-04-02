package calendar.util;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports a calendar to iCal format (.ics file).
 */
public class InterfaceCalExporter implements InterfaceExporter {

  private static final DateTimeFormatter ICAL_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
  private static final DateTimeFormatter ICAL_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  @Override
  public String export(InterfaceCalendar calendar, String fileName) throws IOException {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("File name cannot be null or empty");
    }

    File file = new File(fileName);

    if (file.getParentFile() != null) {
      file.getParentFile().mkdirs();
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      writer.write("BEGIN:VCALENDAR");
      writer.newLine();
      writer.write("VERSION:2.0");
      writer.newLine();
      writer.write("PRODID:-//Calendar Application//EN");
      writer.newLine();
      writer.write("CALSCALE:GREGORIAN");
      writer.newLine();
      writer.write("METHOD:PUBLISH");
      writer.newLine();
      writer.write("X-WR-CALNAME:" + escapeIcalText(calendar.getName()));
      writer.newLine();
      writer.write("X-WR-TIMEZONE:" + calendar.getTimezone().getId());
      writer.newLine();

      writeTimezone(writer, calendar);

      List<InterfaceEvent> events = calendar.getAllEvents();
      for (InterfaceEvent event : events) {
        writeEvent(writer, event, calendar);
      }

      writer.write("END:VCALENDAR");
      writer.newLine();
    }

    return file.getAbsolutePath();
  }

  /**
   * Writes timezone information to the iCal file.
   *
   * @param writer the writer
   * @param calendar the calendar
   * @throws IOException if writing fails
   */
  private void writeTimezone(BufferedWriter writer, InterfaceCalendar calendar)
      throws IOException {
    writer.write("BEGIN:VTIMEZONE");
    writer.newLine();
    writer.write("TZID:" + calendar.getTimezone().getId());
    writer.newLine();
    writer.write("END:VTIMEZONE");
    writer.newLine();
  }

  /**
   * Writes a single event to the iCal file.
   *
   * @param writer the writer
   * @param event the event
   * @param calendar the calendar
   * @throws IOException if writing fails
   */
  private void writeEvent(BufferedWriter writer, InterfaceEvent event, InterfaceCalendar calendar)
      throws IOException {
    writer.write("BEGIN:VEVENT");
    writer.newLine();

    String uid = generateUid(event);
    writer.write("UID:" + uid);
    writer.newLine();

    writer.write("DTSTAMP:" + ZonedDateTime.now().format(ICAL_FORMATTER) + "Z");
    writer.newLine();

    ZonedDateTime startZoned = event.getStart().atZone(calendar.getTimezone());
    if (event.isAllDay()) {
      writer.write("DTSTART;VALUE=DATE:"
          + event.getStart().format(ICAL_DATE_FORMATTER));
    } else {
      writer.write("DTSTART;TZID=" + calendar.getTimezone().getId() + ":"
          + event.getStart().format(ICAL_FORMATTER));
    }
    writer.newLine();

    ZonedDateTime endZoned = event.getEnd().atZone(calendar.getTimezone());
    if (event.isAllDay()) {
      writer.write("DTEND;VALUE=DATE:"
          + event.getEnd().plusDays(1).format(ICAL_DATE_FORMATTER));
    } else {
      writer.write("DTEND;TZID=" + calendar.getTimezone().getId() + ":"
          + event.getEnd().format(ICAL_FORMATTER));
    }
    writer.newLine();

    writer.write("SUMMARY:" + escapeIcalText(event.getSubject()));
    writer.newLine();

    if (!event.getDescription().isEmpty()) {
      writer.write("DESCRIPTION:" + escapeIcalText(event.getDescription()));
      writer.newLine();
    }

    if (!event.getLocation().isEmpty()) {
      writer.write("LOCATION:" + escapeIcalText(event.getLocation()));
      writer.newLine();
    }

    writer.write("CLASS:" + (event.isPublic() ? "PUBLIC" : "PRIVATE"));
    writer.newLine();

    writer.write("STATUS:CONFIRMED");
    writer.newLine();

    writer.write("TRANSP:OPAQUE");
    writer.newLine();

    writer.write("END:VEVENT");
    writer.newLine();
  }

  /**
   * Generates a unique ID for an event.
   *
   * @param event the event
   * @return unique ID
   */
  private String generateUid(InterfaceEvent event) {
    String base = event.getSubject() + event.getStart().toString() + event.getEnd().toString();
    int hashCode = base.hashCode();

    if (hashCode < 0) {
      hashCode = -hashCode;
    }
    return hashCode + "@calendar-app.com";
  }

  /**
   * Escapes special characters according to RFC 5545.
   *
   * @param text the text to escape
   * @return escaped text
   */
  private String escapeIcalText(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }

    return text.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\n", "\\n")
        .replace("\r", "");
  }
}