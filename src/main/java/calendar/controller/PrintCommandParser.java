package calendar.controller;

import calendar.controller.commands.InterfaceCommand;
import calendar.controller.commands.PrintEventsCommand;
import calendar.util.DateTimeParser;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Parses print commands for events.
 */
public class PrintCommandParser {

  /**
   * Parses a print command.
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  public InterfaceCommand parse(String[] tokens, String fullCommand) {
    if (tokens.length < 3 || !tokens[1].equals("events")) {
      throw new IllegalArgumentException("Invalid print command. "
          + "Usage: print events on <date> OR print events from <dateTime> to <dateTime>");
    }

    String commandBody = fullCommand.substring("print events ".length()).trim();

    if (commandBody.startsWith("on ")) {
      return parsePrintOnDate(commandBody);
    } else if (commandBody.startsWith("from ")) {
      return parsePrintInRange(commandBody);
    } else {
      throw new IllegalArgumentException("Invalid print command format");
    }
  }

  private InterfaceCommand parsePrintOnDate(String commandBody) {
    String dateStr = commandBody.substring(3).trim();
    LocalDate date = DateTimeParser.parseDate(dateStr);
    return new PrintEventsCommand(date);
  }

  private InterfaceCommand parsePrintInRange(String commandBody) {
    commandBody = commandBody.substring(5).trim();
    String[] parts = commandBody.split(" to ");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Missing 'to' in print command");
    }
    LocalDateTime start = DateTimeParser.parseDateTime(parts[0].trim());
    LocalDateTime end = DateTimeParser.parseDateTime(parts[1].trim());
    return new PrintEventsCommand(start, end);
  }
}