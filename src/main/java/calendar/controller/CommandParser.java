package calendar.controller;

import calendar.controller.commands.ExitCommand;
import calendar.controller.commands.HelpCommand;
import calendar.controller.commands.InterfaceCommand;
import java.util.HashMap;
import java.util.Map;

class CommandParser {

  private final Map<String, CommandFactory> commandFactories;
  private final CreateCommandParser createParser;
  private final EditCommandParser editParser;
  private final PrintCommandParser printParser;
  private final CopyCommandParser copyParser;
  private final SimpleCommandParser simpleParser;

  private interface CommandFactory {
    InterfaceCommand create(String[] tokens, String fullCommand);
  }

  CommandParser() {
    this.createParser = new CreateCommandParser();
    this.editParser = new EditCommandParser();
    this.printParser = new PrintCommandParser();
    this.copyParser = new CopyCommandParser();
    this.simpleParser = new SimpleCommandParser();

    this.commandFactories = initializeCommandFactories();
  }

  private Map<String, CommandFactory> initializeCommandFactories() {
    Map<String, CommandFactory> factories = new HashMap<>();
    factories.put("create", createParser::parse);
    factories.put("edit", editParser::parse);
    factories.put("print", printParser::parse);
    factories.put("copy", copyParser::parse);
    factories.put("export", simpleParser::parseExport);
    factories.put("show", this::parseShowCommand);
    factories.put("use", simpleParser::parseUseCalendar);
    factories.put("help", (tokens, cmd) -> new HelpCommand());
    factories.put("exit", (tokens, cmd) -> new ExitCommand());
    return factories;
  }

  /**
   * Parses show commands - can be "show status" or "show calendar dashboard".
   *
   * @param tokens the command tokens
   * @param fullCommand the full command string
   * @return the parsed command
   */
  private InterfaceCommand parseShowCommand(String[] tokens, String fullCommand) {
    if (tokens.length < 2) {
      throw new IllegalArgumentException("Invalid show command. "
          + "Usage: show status on <dateTime> OR show calendar dashboard from <date> to <date>");
    }

    String subCommand = tokens[1].toLowerCase();
    if (subCommand.equals("status")) {
      return simpleParser.parseShowStatus(tokens, fullCommand);
    } else if (subCommand.equals("calendar")) {
      return simpleParser.parseShowDashboard(tokens, fullCommand);
    } else {
      throw new IllegalArgumentException("Invalid show sub-command: " + subCommand
          + ". Expected 'status' or 'calendar'");
    }
  }

  InterfaceCommand parse(String commandText) {
    if (commandText == null || commandText.trim().isEmpty()) {
      throw new IllegalArgumentException("Empty command");
    }

    String[] tokens = ParserUtils.tokenize(commandText);

    if (tokens.length == 0) {
      throw new IllegalArgumentException("Empty command");
    }

    String mainCommand = tokens[0].toLowerCase();

    CommandFactory factory = commandFactories.get(mainCommand);
    if (factory == null) {
      throw new IllegalArgumentException("Unknown command: " + mainCommand);
    }

    return factory.create(tokens, commandText);
  }
}