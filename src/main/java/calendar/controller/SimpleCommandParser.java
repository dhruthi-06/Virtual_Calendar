package calendar.controller;

import calendar.controller.commands.ExportCalendarCommand;
import calendar.controller.commands.InterfaceCommand;
import calendar.controller.commands.ShowCalendarDashboardCommand;
import calendar.controller.commands.ShowStatusCommand;
import calendar.controller.commands.UseCalendarCommand;
import calendar.util.DateTimeParser;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Parses simple commands that don't require complex parsing logic.
 */
public class SimpleCommandParser {

  /**
   * Parses an export command.
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  public InterfaceCommand parseExport(String[] tokens, String fullCommand) {
    if (tokens.length < 3 || !tokens[1].equals("cal")) {
      throw new IllegalArgumentException("Invalid export command. "
          + "Usage: export cal <filename>");
    }

    String fileName = tokens[2];
    return new ExportCalendarCommand(fileName);
  }

  /**
   * Parses a show status command.
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  public InterfaceCommand parseShowStatus(String[] tokens, String fullCommand) {
    if (tokens.length < 4 || !tokens[1].equals("status") || !tokens[2].equals("on")) {
      throw new IllegalArgumentException("Invalid show status command. "
          + "Usage: show status on <YYYY-MM-DDTHH:mm>");
    }

    String dateTimeStr = tokens[3];
    LocalDateTime dateTime = DateTimeParser.parseDateTime(dateTimeStr);
    return new ShowStatusCommand(dateTime);
  }

  /**
   * Parses a use calendar command.
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  public InterfaceCommand parseUseCalendar(String[] tokens, String fullCommand) {
    if (tokens.length < 4 || !tokens[1].equals("calendar") || !tokens[2].equals("--name")) {
      throw new IllegalArgumentException("Usage: use calendar --name <name>");
    }
    return new UseCalendarCommand(tokens[3]);
  }

  /**
   * Parses a show calendar dashboard command.
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  public InterfaceCommand parseShowDashboard(String[] tokens, String fullCommand) {
    if (tokens.length < 6 || !tokens[1].equals("calendar") || !tokens[2].equals("dashboard")
        || !tokens[3].equals("from") || !tokens[5].equals("to")) {
      throw new IllegalArgumentException(
          "Invalid show calendar dashboard command. "
              + "Usage: show calendar dashboard from <YYYY-MM-DD> to <YYYY-MM-DD>");
    }

    String startDateStr = tokens[4];
    String endDateStr = tokens.length > 6 ? tokens[6] : null;

    if (endDateStr == null) {
      throw new IllegalArgumentException(
          "Invalid show calendar dashboard command. "
              + "Usage: show calendar dashboard from <YYYY-MM-DD> to <YYYY-MM-DD>");
    }

    LocalDate startDate = DateTimeParser.parseDate(startDateStr);
    LocalDate endDate = DateTimeParser.parseDate(endDateStr);

    return new ShowCalendarDashboardCommand(startDate, endDate);
  }
}