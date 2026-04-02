package calendar.util;

import calendar.model.InterfaceCalendar;
import java.io.IOException;

/**
 * Interface for calendar exporters.
 */
public interface InterfaceExporter {

  /**
   * Exports a calendar to a file.
   *
   * @param calendar the calendar to export
   * @param fileName the output file name (with extension)
   * @return the absolute path of the exported file
   * @throws IOException if export fails
   * @throws IllegalArgumentException if calendar or fileName is null
   */
  String export(InterfaceCalendar calendar, String fileName) throws IOException;
}