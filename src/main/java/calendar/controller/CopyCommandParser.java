package calendar.controller;

import calendar.controller.commands.CopyEventsCommand;
import calendar.controller.commands.CopySingleEventCommand;
import calendar.controller.commands.InterfaceCommand;
import calendar.util.DateTimeParser;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Parses copy commands for events.
 */
public class CopyCommandParser {

  /**
   * Parses a copy command.
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  public InterfaceCommand parse(String[] tokens, String fullCommand) {
    if (tokens.length < 2) {
      throw new IllegalArgumentException("Invalid copy command");
    }

    String subCommand = tokens[1].toLowerCase();

    if (subCommand.equals("event")) {
      return parseCopySingleEvent(fullCommand);
    } else if (subCommand.equals("events")) {
      return parseCopyEvents(fullCommand);
    } else {
      throw new IllegalArgumentException("Invalid copy sub-command: " + subCommand);
    }
  }

  private InterfaceCommand parseCopySingleEvent(String fullCommand) {
    String commandBody = fullCommand.substring("copy event ".length()).trim();

    ParserUtils.SubjectAndRemainder sr = ParserUtils.extractEventNameForCopy(commandBody);
    final String eventName = sr.subject;
    String remainder = sr.remainder;

    if (!remainder.startsWith("on ")) {
      throw new IllegalArgumentException("Missing 'on' in copy command");
    }
    remainder = remainder.substring(3).trim();

    String[] targetSplit = remainder.split(" --target ");
    if (targetSplit.length != 2) {
      throw new IllegalArgumentException("Missing '--target' in copy command");
    }

    LocalDateTime sourceDateTime = DateTimeParser.parseDateTime(targetSplit[0].trim());

    String[] toSplit = targetSplit[1].split(" to ");
    if (toSplit.length != 2) {
      throw new IllegalArgumentException("Missing 'to' in copy command");
    }

    String targetCalendar = toSplit[0].trim();
    LocalDateTime targetDateTime = DateTimeParser.parseDateTime(toSplit[1].trim());

    return new CopySingleEventCommand(eventName, sourceDateTime, targetCalendar, targetDateTime);
  }

  private InterfaceCommand parseCopyEvents(String fullCommand) {
    String commandBody = fullCommand.substring("copy events ".length()).trim();

    if (commandBody.startsWith("on ")) {
      return parseCopyEventsOnDate(commandBody);
    } else if (commandBody.startsWith("between ")) {
      return parseCopyEventsBetween(commandBody);
    } else {
      throw new IllegalArgumentException("Invalid copy events format. "
          + "Usage: copy events on <date> --target <cal> to <date> OR "
          + "copy events between <date> and <date> --target <cal> to <date>");
    }
  }

  private InterfaceCommand parseCopyEventsOnDate(String commandBody) {
    commandBody = commandBody.substring(3).trim();
    String[] targetSplit = commandBody.split(" --target ");
    if (targetSplit.length != 2) {
      throw new IllegalArgumentException("Missing '--target' in copy command");
    }

    LocalDate sourceDate = DateTimeParser.parseDate(targetSplit[0].trim());

    String[] toSplit = targetSplit[1].split(" to ");
    if (toSplit.length != 2) {
      throw new IllegalArgumentException("Missing 'to' in copy command");
    }

    String targetCalendar = toSplit[0].trim();
    LocalDate targetDate = DateTimeParser.parseDate(toSplit[1].trim());

    return new CopyEventsCommand(sourceDate, null, targetCalendar, targetDate);
  }

  private InterfaceCommand parseCopyEventsBetween(String commandBody) {
    commandBody = commandBody.substring(8).trim();
    String[] andSplit = commandBody.split(" and ");
    if (andSplit.length != 2) {
      throw new IllegalArgumentException("Missing 'and' in copy command");
    }

    LocalDate startDate = DateTimeParser.parseDate(andSplit[0].trim());

    String[] targetSplit = andSplit[1].split(" --target ");
    if (targetSplit.length != 2) {
      throw new IllegalArgumentException("Missing '--target' in copy command");
    }

    LocalDate endDate = DateTimeParser.parseDate(targetSplit[0].trim());

    String[] toSplit = targetSplit[1].split(" to ");
    if (toSplit.length != 2) {
      throw new IllegalArgumentException("Missing 'to' in copy command");
    }

    String targetCalendar = toSplit[0].trim();
    LocalDate targetDate = DateTimeParser.parseDate(toSplit[1].trim());

    return new CopyEventsCommand(startDate, endDate, targetCalendar, targetDate);
  }
}