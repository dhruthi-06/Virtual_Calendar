# Design Changes from Assignment 4 to Assignment 5

## Introduction

This document explains what changed in our calendar application between Assignment 4 and Assignment 5, and why we made those changes. Assignment 4 supported a single calendar with basic event management. Assignment 5 required multiple calendars, timezones, copying events between calendars, and exporting to iCalendar format.

## Why We Changed the Overall Structure

### Moving from One Calendar to Many Calendars

In Assignment 4, we had one calendar that held all events. The CalendarModelImpl class managed everything in a single list.

In Assignment 5, we needed multiple calendars. We created two new classes to handle this. The CalendarSystem class manages all the calendars in the application. Each Calendar class manages its own events and timezone.

We made this change because the assignment required users to create and manage multiple calendars with different names and timezones. Trying to handle multiple calendars in the old single-calendar structure would have been messy and confusing.

This approach keeps calendars independent from each other. When you work with one calendar, it does not affect the others. When you need to copy events between calendars, the CalendarSystem coordinates that operation.

The downside is we now have an extra layer of code. But the benefit is that each piece has a clear job and the code is easier to understand and test.

### Switching from Class Inheritance to Interfaces

In Assignment 4, we had AbstractEvent as a parent class. Both SingleEvent and EventSeries inherited from it and shared common code.

In Assignment 5, we removed AbstractEvent and created interfaces instead. We have InterfaceEvent, InterfaceRecurringEvent, and InterfaceCalendar. The actual classes are Event, RecurringEvent, and Calendar.

We made this change because the shared parent class was limiting. When we needed to add timezone conversion and copy operations, the rigid structure made things difficult. Interfaces give us more freedom to change how things work without breaking other parts of the code.

The downside is we have some repeated code in Event and RecurringEvent. But the benefit is we can test things more easily and change implementations without affecting the rest of the system.

### Creating Command Objects

In Assignment 4, we had handler classes like CreateCommandHandler and EditCommandHandler. The CommandParser would call the right handler based on the command.

In Assignment 5, we created command objects. Each command is now its own class like CreateEventCommand or CopyEventsCommand. All commands follow the same interface pattern.

We made this change because Assignment 5 added a concept of the current calendar. Before running most commands, you have to tell the system which calendar you want to use. The old handler system could not easily track which calendar was active.

With command objects, the controller knows which calendar is active and passes that information to each command. The command can then validate and execute using that context.

The downside is more classes to manage. But each command class is simple and focused on one operation. Adding new commands means creating a new class instead of modifying existing code.

### Separating Parsing from Execution

In Assignment 4, the command handlers both parsed the command text and executed the operation. For example, CreateCommandHandler would figure out what the user typed and then create the event.

In Assignment 5, we separated these jobs. We created parser classes like CreateCommandParser and EditCommandParser that only figure out what the user wants. The command objects only execute the operation.

We made this change because Assignment 5 commands became more complex. Commands have flags like target calendar names and timezone specifications. Mixing the parsing logic with the execution logic made the code hard to read and test.

Now when we need to change how a command is parsed, we only touch the parser class. When we need to change what a command does, we only touch the command class.

The downside is more files to look through. But the benefit is each file is smaller and easier to understand.

## New Features We Added

### Timezone Support

We added ZoneId to each Calendar to track its timezone. We added a convertTimezone method that changes times from one timezone to another.

We needed this because Assignment 5 requires each calendar to have its own timezone. When you copy an event from a calendar in New York time to a calendar in Los Angeles time, the times need to convert properly.

We used Java built-in timezone support with ZoneId. When converting, we make sure the actual moment in time stays the same but the displayed time changes. An event at 2pm Eastern time becomes 11am Pacific time because that is the same moment.

### Copying Events

We added three new commands to copy events. You can copy a single event, all events on a specific date, or all events in a date range.

We needed this because Assignment 5 requires copying events between calendars. This is useful when you want to reuse a schedule from one semester to the next or copy work events to a personal calendar.

When copying, we create completely new event objects. If the source and destination calendars have different timezones, we convert the times. If events are part of a recurring series, we give them a new series ID so the copy and original do not interfere with each other.

### iCalendar Export

We added InterfaceCalExporter to export calendars in iCalendar format. We also created InterfaceExporter as a general export interface.

We needed this because Assignment 5 requires supporting both CSV and iCalendar formats. Making an interface means we can add more formats later without changing existing code.

The system looks at the file extension the user provides. If they type calendar.csv it exports as CSV. If they type calendar.ical it exports as iCalendar format.

### Better Event Editing

We created EventEditor to handle updating event properties with proper validation.

We needed this because edit operations have different rules for different properties. Changing a subject just updates text. Changing a start time needs to validate it is before the end time. Having one place for these rules makes the code cleaner.

### Conflict and Duplicate Detection

We created ConflictDetector to check if events overlap in time or are exact duplicates.

We needed this to keep the duplicate checking logic separate from the storage logic. Now the Calendar asks ConflictDetector if there is a problem before adding an event.

## How We Changed the Controller

In Assignment 4, the CalendarController called CommandParser which called handler classes. The handlers directly changed the model.

In Assignment 5, the CalendarController tracks which calendar is currently active. It takes command text, asks the parser to create a command object, then executes that command. Commands receive the current calendar name and do their work.

We made this change because the use calendar command concept requires tracking state. The controller is the right place to track which calendar the user selected. Commands can then work with that calendar.

This makes the controller simple. It only manages the active calendar and runs commands. Commands do the actual work.

## How We Changed the Model

In Assignment 4, we had CalendarModelImpl with a list of events.

In Assignment 5, we have CalendarSystem that contains a map of Calendar objects. Each Calendar has an EventRepository that stores its events.

We made this change because we needed multiple independent calendars. Each calendar needs its own event list and timezone. Separating storage into EventRepository means we could later change how events are stored without affecting the Calendar class.

## How We Changed the View

In Assignment 4, we had separate InteractiveView and HeadlessView classes.

In Assignment 5, we have one CalendarTextView that handles both modes. The CalendarRunner decides whether to use keyboard input or file input and creates the controller accordingly.

We made this change because the two view classes were almost identical. The only difference was where input came from. Combining them eliminates duplicate code and makes the output consistent between modes.

## Helper Classes We Added

We added DateTimeParser to handle all date and time parsing in one place. Before this, date parsing code was scattered everywhere. Now there is one place to handle date formats and show helpful error messages when dates are invalid.

We added WeekdayCode as an enum for the day codes like M for Monday and F for Friday. This is safer than passing strings around and makes the code clearer.

## How We Improved Error Messages

In Assignment 4, errors were basic. If something failed, you got a generic message.

In Assignment 5, we added validation at multiple levels. The BaseCommand class checks if a calendar is active before letting commands run. Individual commands check their specific requirements. The model checks business rules like unique calendar names and valid timezones.

Error messages now tell you exactly what is wrong and how to fix it. If you forget to select a calendar, it tells you to use the use calendar command first. If you provide an invalid timezone, it tells you to use IANA format with examples.

## Still Following MVC Architecture

The Model contains CalendarSystem, Calendar, Event, RecurringEvent, and all the repository and helper classes. These handle business logic and data storage.

The View contains CalendarTextView which formats output for the user. It knows nothing about how the model works.

The Controller contains CalendarController, all the command classes, and all the parser classes. These coordinate between model and view.

We kept these boundaries clear. The model never prints output. The view never changes data. The controller coordinates but does not contain business logic.

## What This Design Makes Easy in the Future

If we wanted to save calendars to a database instead of memory, we would only change EventRepository. Nothing else needs to know where events are stored.

If we wanted to connect to Google Calendar or another service, we could create a RemoteCalendar class that implements InterfaceCalendar. The rest of the system would work with it the same way.

If we wanted to export to JSON or XML format, we would create a new exporter class. The export command would automatically support it based on file extension.

If we wanted to add undo functionality, the command pattern makes this natural. Each command could remember what it changed and reverse the operation.

## What Are the Limitations

We have more classes now which means more files to navigate. A developer new to the code needs to understand the structure before making changes.

Commands need access to both model and view which creates some coupling. But this is necessary because commands need to execute operations and show results.

The interface-based design means some code is duplicated between Event and RecurringEvent. We accepted this tradeoff for the flexibility interfaces provide.

The CalendarSystem acting as a middleman adds an extra step when working with calendars. But this extra layer is what enables features like copying between calendars and managing multiple calendars cleanly.

## Detailed Changes By Component

### Controller Layer Changes

In Assignment 4, we had five main controller classes: CalendarController, CommandParser, CreateCommandHandler, EditCommandHandler, PrintCommandHandler, ExportCommandHandler, and ParsingCommands utility class.

In Assignment 5, we restructured this completely. We still have CalendarController and CommandParser, but now we have dedicated parser classes for each command type: CreateCommandParser, EditCommandParser, CopyCommandParser, PrintCommandParser, and SimpleCommandParser. We also have ParserUtils for shared parsing logic.

The command handlers became command classes. Instead of CreateCommandHandler, we now have CreateCalendarCommand and CreateEventCommand. Instead of EditCommandHandler, we have EditCalendarCommand and EditEventCommand. We added new commands like UseCalendarCommand, CopyEventsCommand, CopySingleEventCommand, and ExportCalendarCommand.

We made this change because separating parsing from execution makes the code cleaner. Each parser only understands command syntax. Each command only performs the operation. This separation makes testing easier because we can test parsing without executing commands and test commands without parsing text.

The BaseCommand abstract class provides common functionality like checking if a calendar is in use. This eliminates repeated validation code across command classes.

### Model Layer Changes

In Assignment 4, we had AbstractEvent as a base class with SingleEvent and EventSeries as subclasses. We had EventInterface as a simple interface. CalendarModelImpl managed all events in one list.

In Assignment 5, we removed AbstractEvent entirely. We created focused interfaces: InterfaceEvent for individual events, InterfaceRecurringEvent for recurring events, InterfaceCalendar for calendar operations, and InterfaceCalendarSystem for system-wide operations. The concrete implementations are Event, RecurringEvent, Calendar, and CalendarSystem.

We also extracted specialized classes from the monolithic model. EventRepository handles storing and retrieving events. EventEditor handles property updates with validation. ConflictDetector checks for overlaps and duplicates.

We made these changes because the Assignment 4 structure could not support multiple calendars. The AbstractEvent inheritance was too rigid for the new requirements. Breaking the model into smaller focused classes follows the Single Responsibility Principle. Each class now has one clear job.

The EventRepository extraction is particularly important. In Assignment 4, CalendarModelImpl directly managed the events list. Now EventRepository encapsulates storage. If we wanted to store events in a database, we would only change EventRepository. The Calendar class would not need any changes.

### Event Series Handling Changes

In Assignment 4, EventSeries was a class that expanded itself into SingleEvent instances. All instances shared a series ID stored as a string.

In Assignment 5, RecurringEvent generates Event instances. The series relationship is maintained through the series ID, but RecurringEvent itself is not stored in the calendar. Only the generated Event instances are stored.

We made this change to simplify series management. The RecurringEvent class is now just a factory for creating events. It does not need to track what happens to those events after creation. The Calendar and EventRepository handle the created events like any other events.

When editing events in a series, the system uses the series ID to find related events. If you change the start time of events in a series, those events get a new series ID because they are no longer part of the original pattern. This matches the behavior described in the Assignment 4 specification.

### Calendar Context Management

Assignment 4 had no concept of calendar context because there was only one calendar.

Assignment 5 requires users to first select a calendar with the use calendar command before they can work with events. The CalendarController tracks which calendar is currently in use. This context is passed to every command that needs it.

We implemented this by having the controller maintain a currentCalendarName field. When UseCalendarCommand executes, it returns the new calendar name through the getNewCalendarContext method. The controller updates its current calendar accordingly.

Commands that need a calendar check if one is in use by calling validateCalendarInUse in BaseCommand. If no calendar is active, the command fails with a clear error message telling the user to run use calendar first.

This design keeps the context management in the controller where it belongs. Commands do not need to track state. They receive the current calendar name and validate it.

### Export System Changes

In Assignment 4, we had ExportCommandHandler that called a method on CalendarModelImpl to export to CSV format.

In Assignment 5, we created an export system with InterfaceExporter as the base interface. CsvExporter and InterfaceCalExporter implement this interface. ExportCalendarCommand automatically detects the format based on file extension.

We made this change because Assignment 5 requires supporting multiple export formats. Creating an interface for exporters means adding new formats does not require changing existing code. If we wanted to add JSON export, we would create JsonExporter implementing InterfaceExporter. The ExportCalendarCommand would automatically support it.

The exporters receive a Calendar object and return the absolute file path. They handle all format-specific details internally. The CSV format changed slightly to be more compatible with Google Calendar imports. The iCalendar format follows RFC 5545 specification.

### Parsing Improvements

In Assignment 4, we had ParsingCommands utility class with methods like extractSubject and parseDays. The command handlers contained additional parsing logic mixed with execution logic.

In Assignment 5, we have ParserUtils with improved parsing methods and dedicated parser classes for each command type. The parsers handle all the complexity of extracting information from command text.

For example, EditCommandParser handles the different formats for edit event versus edit events. It validates that required keywords like from and with are present. It extracts quoted strings properly. It handles the timezone specification in calendar commands.

We improved the parsing in several ways. We added better error messages that tell users exactly what is wrong with their command. We handle quoted strings consistently across all commands. We validate command structure before trying to parse values. We extract datetime values and immediately validate them using DateTimeParser.

The ParserUtils.tokenize method properly handles quoted strings when splitting command text. The extractSubject method works for both quoted multi-word subjects and single-word subjects. The removeQuotes method safely removes quotes when present but leaves unquoted text unchanged.

### View Simplification

In Assignment 4, we had CalendarView interface with InteractiveView and HeadlessView implementations. Each view created its own CalendarController.

In Assignment 5, we have InterfaceCalendarView interface with CalendarTextView as the single implementation. The CalendarRunner creates the controller with the appropriate input source.

We made this change because InteractiveView and HeadlessView were nearly identical. The only difference was whether input came from System.in or a file. Moving this decision to CalendarRunner eliminated the duplicate code.

CalendarTextView now has more methods for different display scenarios. It has displayEventsForDate for showing events on a specific date. It has displayEventsInRange for showing events in a time range. It has displayStatus for showing busy or available status. It has displayHelp for showing command information.

The view formats output consistently but does not decide what to display. Commands tell the view what to display. This keeps display logic in the view and business logic in commands.

### Error Handling Strategy

In Assignment 4, exceptions were thrown with basic messages. The controller caught them and displayed error messages.

In Assignment 5, we have layered validation with specific error messages at each level. Commands validate that a calendar is in use. Parsers validate command syntax and provide helpful error messages about what is wrong. The model validates business rules like unique calendar names and valid timezones.

Error messages are now instructive. Instead of "Invalid command" we say "Usage: create calendar --name <name> --timezone <timezone>". Instead of "Event not found" we say "No event found with subject 'Meeting' starting at 2024-03-13T10:00".

The controller catches all exceptions and displays them through the view. This keeps error handling consistent. The view always shows errors in the same format whether they come from parsing, validation, or execution.

### Testing Considerations

The Assignment 5 design makes testing much easier than Assignment 4. The interface-based design allows using mock objects. We can test commands with mock models and views. We can test parsers without executing commands. We can test the model without worrying about controllers or views.

Each component has clear inputs and outputs. Parsers take strings and return command objects. Commands take model, view, and context and execute operations. The model provides methods that operate on data and return results.

The separation of concerns means we can test each piece independently. We can test that EditCommandParser correctly extracts information from command text. We can test that EditEventCommand correctly updates events. We can test that EventEditor correctly validates property changes. Each test focuses on one responsibility.

## Summary

We transformed a single-calendar application into a multi-calendar system. The big changes were adding CalendarSystem to manage multiple calendars, using interfaces instead of inheritance for flexibility, creating command objects to encapsulate operations, and separating parsing from execution.

These changes increased the number of files but made the code much better organized. Each class has one clear purpose. The boundaries between model, view, and controller are clear. The system can grow with new features without major rewrites.

We made these changes because Assignment 5 requirements could not fit into the Assignment 4 structure. Along the way we improved the design to make future changes easier. The code is now easier to test, easier to understand, and easier to extend.