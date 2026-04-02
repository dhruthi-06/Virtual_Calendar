package calendar.controller.commands;

import calendar.model.InterfaceCalendar;
import calendar.model.InterfaceCalendarSystem;
import calendar.util.CsvExporter;
import calendar.util.InterfaceCalExporter;
import calendar.util.InterfaceExporter;
import calendar.view.InterfaceCalendarView;
import java.io.File;

/**
 * Command to export calendar to a file.
 * Supports CSV and iCal formats.
 * Format is auto-detected based on file extension.
 */
public class ExportCalendarCommand extends BaseCommand {

  private final String fileName;

  /**
   * Constructor for ExportCalendarCommand.
   *
   * @param fileName the name of the file to export to
   */
  public ExportCalendarCommand(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      throw new IllegalArgumentException("File name cannot be empty");
    }
    this.fileName = fileName;
  }

  @Override
  public void execute(InterfaceCalendarSystem model,
                      InterfaceCalendarView view, String currentCalendar) {
    validateCalendarInUse(currentCalendar);

    try {
      InterfaceCalendar calendar = model.getCalendar(currentCalendar);

      InterfaceExporter exporter;
      String lowerFileName = fileName.toLowerCase();

      if (lowerFileName.endsWith(".csv")) {
        exporter = new CsvExporter();
      } else if (lowerFileName.endsWith(".ical") || lowerFileName.endsWith(".ics")) {
        exporter = new InterfaceCalExporter();
      } else {
        throw new IllegalArgumentException(
            "Unsupported file format. Use .csv or .ical/.ics extension");
      }

      String absolutePath = exporter.export(calendar, fileName);

      File exportedFile = new File(absolutePath);
      if (exportedFile.exists()) {
        view.displayMessage("Calendar exported successfully!");
        view.displayMessage("File location: " + absolutePath);
        view.displayMessage("File size: " + exportedFile.length() + " bytes");
      } else {
        view.displayError("Export completed but file not found at: " + absolutePath);
      }

    } catch (IllegalArgumentException e) {
      view.displayError("Invalid export request: " + e.getMessage());
    } catch (Exception e) {
      view.displayError("Failed to export calendar: " + e.getMessage());
    }
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Gets the export format based on file extension.
   *
   * @return the format name
   */
  public String getFormat() {
    String lower = fileName.toLowerCase();
    if (lower.endsWith(".csv")) {
      return "CSV";
    } else if (lower.endsWith(".ical") || lower.endsWith(".ics")) {
      return "iCal";
    } else {
      return "Unknown";
    }
  }
}