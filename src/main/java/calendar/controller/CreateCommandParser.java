package calendar.controller;

import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.CreateEventCommand;
import calendar.controller.commands.InterfaceCommand;
import calendar.util.DateTimeParser;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Parses create commands for calendars and events.
 */
public class CreateCommandParser {

  /**
   * Parses a create command.
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  public InterfaceCommand parse(String[] tokens, String fullCommand) {
    if (tokens.length < 2) {
      throw new IllegalArgumentException("Invalid create command. "
          + "Usage: create calendar --name <name> --timezone <tz> OR create event ...");
    }

    String subCommand = tokens[1].toLowerCase();

    if (subCommand.equals("calendar")) {
      return parseCreateCalendar(tokens);
    } else if (subCommand.equals("event")) {
      return parseCreateEvent(fullCommand);
    } else {
      throw new IllegalArgumentException("Invalid create sub-command: " + subCommand);
    }
  }

  /**
   * Parses a create calendar command.
   * Extracts --name and --timezone flags.
   *
   * @param tokens the command tokens
   * @return the create calendar command
   */
  private InterfaceCommand parseCreateCalendar(String[] tokens) {
    String name = null;
    String timezone = null;

    for (int i = 2; i < tokens.length; i++) {
      if (tokens[i].equals("--name") && i + 1 < tokens.length) {
        name = tokens[i + 1];
        i++;
      } else if (tokens[i].equals("--timezone") && i + 1 < tokens.length) {
        timezone = tokens[i + 1];
        i++;
      }
    }

    if (name == null || timezone == null) {
      throw new IllegalArgumentException(
          "Usage: create calendar --name <name> --timezone <timezone>");
    }

    return new CreateCalendarCommand(name, timezone);
  }

  /**
   * Parses a create event command.
   * Delegates to specific parsers based on event type.
   *
   * @param fullCommand the full command string
   * @return the create event command
   */
  private InterfaceCommand parseCreateEvent(String fullCommand) {
    String commandBody = fullCommand.substring("create event ".length()).trim();

    ParserUtils.SubjectAndRemainder sr = ParserUtils.extractSubject(commandBody);
    String subject = sr.subject;
    String remainder = sr.remainder;

    if (remainder.startsWith("on ")) {
      return parseAllDayEvent(subject, remainder);
    } else if (remainder.startsWith("from ")) {
      return parseTimedEvent(subject, remainder);
    } else {
      throw new IllegalArgumentException("Invalid event command format. "
          + "Expected 'on <date>' or 'from <datetime> to <datetime>'");
    }
  }

  /**
   * Parses an all-day event command.
   * Format: create event subject on date repeats weekdays pattern.
   *
   * @param subject the event subject
   * @param remainder the remainder of the command after the subject
   * @return the create event command
   */
  private InterfaceCommand parseAllDayEvent(String subject, String remainder) {
    remainder = remainder.substring(3).trim();

    String[] parts = remainder.split(" repeats ");
    String dateStr = parts[0].trim();
    LocalDate date = DateTimeParser.parseDate(dateStr);

    LocalDateTime start = date.atTime(8, 0);
    LocalDateTime end = date.atTime(17, 0);

    if (parts.length == 1) {
      return new CreateEventCommand.Builder(subject, start, end).build();
    } else {
      String repeatPattern = parts[1].trim();
      return buildRecurringEventCommand(subject, start, end, repeatPattern);
    }
  }

  /**
   * Parses a timed event command.
   * Format: create event subject from datetime to datetime repeats weekdays pattern.
   *
   * @param subject the event subject
   * @param remainder the remainder of the command after the subject
   * @return the create event command
   */
  private InterfaceCommand parseTimedEvent(String subject, String remainder) {
    remainder = remainder.substring(5).trim();

    String[] parts = remainder.split(" to ");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Missing 'to' in event command. "
          + "Expected: from <datetime> to <datetime>");
    }

    LocalDateTime start = DateTimeParser.parseDateTime(parts[0].trim());

    String endAndRemainder = parts[1].trim();
    String[] repeatSplit = endAndRemainder.split(" repeats ");

    LocalDateTime end = DateTimeParser.parseDateTime(repeatSplit[0].trim());

    if (repeatSplit.length == 1) {
      return new CreateEventCommand.Builder(subject, start, end).build();
    } else {
      return buildRecurringEventCommand(subject, start, end, repeatSplit[1].trim());
    }
  }

  /**
   * Builds a recurring event command from a repeat pattern.
   * Handles both for N times and until specified date patterns.
   *
   * @param subject the event subject
   * @param start the start date and time
   * @param end the end date and time
   * @param pattern the recurrence pattern string
   * @return the create event command
   */
  private InterfaceCommand buildRecurringEventCommand(String subject,
                                                      LocalDateTime start,
                                                      LocalDateTime end,
                                                      String pattern) {
    ParserUtils.RecurrenceDetails details = ParserUtils.extractRecurrenceDetails(pattern);

    CreateEventCommand.Builder builder = new CreateEventCommand.Builder(subject, start, end);

    if (details.count != null) {
      builder.repeats(details.weekdays, details.count);
    } else {
      builder.repeatsUntil(details.weekdays, details.until);
    }

    return builder.build();
  }
}