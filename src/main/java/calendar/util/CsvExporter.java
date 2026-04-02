package calendar.util;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exports a calendar to CSV format compatible with Google Calendar.
 */
public class CsvExporter implements InterfaceExporter {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofPattern("HH:mm");

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
      writer.write("Subject,Start Date,Start Time,End Date,End Time,"
          + "All Day Event,Description,Location,Private");
      writer.newLine();

      List<InterfaceEvent> events = calendar.getAllEvents();
      for (InterfaceEvent event : events) {
        writer.write(formatEventAsCsv(event));
        writer.newLine();
      }
    }

    return file.getAbsolutePath();
  }

  /**
   * Formats an event as a CSV row.
   *
   * @param event the event to format
   * @return CSV formatted string
   */
  private String formatEventAsCsv(InterfaceEvent event) {
    StringBuilder sb = new StringBuilder();

    sb.append(escapeCsv(event.getSubject())).append(",");

    sb.append(event.getStart().format(DATE_FORMATTER)).append(",");

    if (event.isAllDay()) {
      sb.append(",");
    } else {
      sb.append(event.getStart().format(TIME_FORMATTER)).append(",");
    }

    sb.append(event.getEnd().format(DATE_FORMATTER)).append(",");

    if (event.isAllDay()) {
      sb.append(",");
    } else {
      sb.append(event.getEnd().format(TIME_FORMATTER)).append(",");
    }

    sb.append(event.isAllDay() ? "True" : "False").append(",");

    sb.append(escapeCsv(event.getDescription())).append(",");

    sb.append(escapeCsv(event.getLocation())).append(",");

    sb.append(event.isPublic() ? "False" : "True");

    return sb.toString();
  }

  /**
   * Escapes special characters in CSV values.
   *
   * @param value the value to escape
   * @return escaped value
   */
  private String escapeCsv(String value) {
    if (value == null || value.isEmpty()) {
      return "";
    }

    if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    return value;
  }
}