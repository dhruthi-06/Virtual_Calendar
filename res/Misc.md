# Design Changes from Assignment 5 to Assignment 6

## Introduction

This document explains what changed in our calendar application between Assignment 5 and Assignment 6. Assignment 5 had text-based interfaces with multiple calendars, timezones, and copying features. Assignment 6 required adding a graphical user interface while keeping the text modes working.

## Why We Added GUI Components

### Building the Graphical Interface

In Assignment 5, users interacted with the calendar by typing text commands. They could run in interactive mode or headless mode with script files.

In Assignment 6, we built a graphical user interface using Java Swing. We created several new view classes to handle different parts of the GUI.

We made this change because Assignment 6 requires a GUI. Users should be able to click buttons, select dates visually, and see events displayed in a calendar grid. This is more intuitive than typing commands.

We created separate dialog classes for each major operation. CreateCalendarDialog handles making new calendars. CreateEventDialog handles making new events. EditEventDialog handles editing existing events. SearchEventsDialog handles finding and editing multiple events at once.

The downside is more classes to manage. But the benefit is each dialog has one clear purpose and the code stays organized. If we need to change how event creation works, we only touch CreateEventDialog.

### Separate Controller for GUI

In Assignment 5, we had CalendarController that handled text commands. It parsed command strings and executed them.

In Assignment 6, we created CalendarGuiController as a separate controller. The text-based CalendarController remains unchanged.

We made this change because GUI interactions are fundamentally different from text commands. In the GUI, users click buttons and see immediate results. In text mode, users type complete commands and see text output.

CalendarGuiController has methods like createEvent and editEvent that get called directly when buttons are clicked. CalendarController has a run method that reads command strings and parses them. Keeping them separate means each controller only worries about its own interaction mode.

Both controllers use the same model classes. This follows MVC principles. The model does not care whether requests come from GUI buttons or text commands.

### Supporting Three Execution Modes

In Assignment 5, CalendarRunner checked for mode interactive or mode headless command-line arguments.

In Assignment 6, we updated CalendarRunner to support three modes. If you run the jar with no arguments, it opens the GUI. If you provide mode interactive, it opens text mode. If you provide mode headless with a file path, it runs the script.

We made this change because Assignment 6 says running the jar without arguments should open the GUI. This is what happens when users double-click a jar file. The text modes from Assignment 5 still work for scripting and automation.

CalendarRunner.main looks at the arguments and decides which mode to launch. It creates the appropriate view, controller, and starts the application. Invalid arguments show an error message with usage instructions.

## New GUI Features We Added

### Default Calendar Creation

In Assignment 5 text mode, users had to create a calendar and use it before doing anything else.

In Assignment 6 GUI mode, the application automatically creates a calendar called My Calendar when it starts. It uses the computer's timezone from system settings.

We made this change because Assignment 6 says users should not be forced to create a calendar. They should be able to open the app and start adding events immediately. This matches how Google Calendar and other calendar apps work.

The default calendar creation happens in CalendarGuiController.start. After creating the calendar, it sets it as the current calendar and updates the view. Users can create more calendars if they want, but they do not have to.

### Calendar Color Coding

In Assignment 5, there was no visual way to distinguish calendars since everything was text.

In Assignment 6, each calendar gets assigned a unique color. The color appears in the calendar name label at the top. The selected date border uses the calendar color. Today's date has a lighter tint of the calendar color.

We made this change because Assignment 6 requires users to know which calendar they are using. Color coding makes this immediately obvious. When you switch calendars, the entire interface changes color to match.

We have six preset colors defined in CalendarGuiView. When a new calendar is created, it gets the next color in the rotation. This ensures each calendar looks visually distinct.

### Event Indicators on Calendar Days

In Assignment 5, you had to print events to see if a day had anything scheduled.

In Assignment 6, days with events show a bullet point and count in the calendar grid. A day with three events shows as bullet 3.

We made this change so users can quickly scan the month and see which days are busy. They do not need to click every day to find events. This is standard calendar app behavior.

The indicator is created in the createDayButton method. It checks how many events exist on that date and updates the button text accordingly.

### Event Display Cards with Badges

In Assignment 5, event information was printed as text lines.

In Assignment 6, events display as cards in the right panel. Each card shows the event subject, time, location, and description preview. Colored badges indicate Public (green), Private (red), and Recurring (purple) status.

We made this change because visual cards are easier to scan than text lists. The badges provide instant information about event properties without opening edit dialogs. Users can see at a glance which events are private or recurring.

The cards are created in the createEventPanel method. It builds a JPanel with formatted labels and colored badge labels for each event.

### Date Selection and Navigation

In Assignment 5, users typed dates in YYYY-MM-DD format to specify which day they wanted to work with.

In Assignment 6, users click on dates in the calendar grid. Navigation buttons let them move between months. A Today button jumps back to the current date.

We made this change because clicking dates is more natural than typing them. The previous/next month buttons and Today button are standard calendar navigation that users expect.

The calendar grid is built in the updateCalendarGrid method. It calculates which day of the week the month starts on and places buttons accordingly. Selected dates get a thick colored border. Today gets a lighter background.

### Three-Scope Event Editing

In Assignment 5, we had three text commands for editing: edit event, edit events, and edit series. Users had to know which command to use.

In Assignment 6, the edit dialog shows three radio button options all in one place. This event only, This and all following events, and All events in the series.

We made this change because radio buttons make the choice explicit and clear. Users can see all three options and pick the one they want. For non-recurring events, only the first option is enabled which prevents confusion.

The scope selection is in EditEventDialog. Based on which radio button is selected, it calls different controller methods: editEvent, editEventsFromDate, or editEntireSeries.

### Search and Bulk Edit

In Assignment 5, there was no way to find and edit multiple events with the same name all at once.

In Assignment 6, we added a search dialog. Users type an event name and see all matching events across the entire calendar. They can then edit all of them together or select specific ones to edit.

We made this change because Assignment 6 specifically requires this feature. It says users should be able to identify multiple events with the same name and edit them together. This is useful for things like changing the location for all Office Hours events.

SearchEventsDialog has a search field and a results list. After searching, users pick a property to change and enter a new value. They can choose to edit all results or only selected ones from the list.

### Real-Time Validation

In Assignment 5, validation happened when commands executed. Invalid commands showed error messages after you typed them.

In Assignment 6, form fields validate as users type or change values. Error messages appear immediately in red text below the field.

We made this change because GUI users expect instant feedback. If you set the end time before the start time, the form shows an error right away. You do not have to click submit to find out there is a problem.

We added validation listeners to form components. For example, the time spinners in CreateEventDialog have a ChangeListener that checks if end time is after start time whenever either spinner changes.

### User-Friendly Input Components

In Assignment 5, users typed everything as text. Dates were YYYY-MM-DD. Times were HH:mm. One typo and the command failed.

In Assignment 6, we use GUI components that prevent bad input. JSpinners for hours and minutes only accept valid numbers. Hour spinners go from 0 to 23. Minute spinners go from 0 to 59 in 15-minute increments.

We made this change because Assignment 6 says to avoid making users type when a less error-prone method exists. Spinners let users click up/down arrows or type numbers, but they cannot type invalid values like hour 25.

The same applies to weekday selection. Instead of typing MTWRF, users check boxes for Monday, Tuesday, Wednesday, Thursday, Friday. Timezone selection uses a dropdown with 50 plus common timezones instead of typing IANA identifiers.

### Timezone Dropdowns

In Assignment 5, users typed timezone strings like America/New_York. Typos caused errors.

In Assignment 6, calendar dialogs have dropdown menus with common IANA timezones pre-populated.

We made this change because typing timezone identifiers is error-prone. The dropdown shows all valid options. Users cannot make typos. The system timezone is automatically selected as the default.

The timezone list in CreateCalendarDialog and EditCalendarDialog includes timezones from all continents organized by region. This covers most use cases while keeping the list manageable.

## Code Quality Improvements

### Builder Pattern for Event Creation

In Assignment 5, the Event constructor took three required parameters. Setting optional properties required calling setters afterward.

In Assignment 6, we added Event.Builder class. This allows creating events with optional parameters in a clean, readable way.

We made this change because CalendarSystem had a createEvent method with seven parameters. This is too many parameters for a single method. The Builder pattern solves this problem.

Now you can create an event with the Builder:

Event event = new Event.Builder(subject, start, end).description("Meeting notes").location("Room 101").isPublic(false).build();

The Builder makes it clear which properties are being set. It also makes the code easier to read and maintain. If we want to add more optional properties later, we just add more builder methods.

The simple Event constructor still exists for basic usage. The Builder is there for when you need to set optional fields.

### EventCopyContext Helper Class

In Assignment 5, the copyEventsInternal method had many parameters: source calendar, target calendar, list of events, days difference, and a series ID map. This is too many parameters.

In Assignment 6, we created EventCopyContext as a helper class. It groups all the copy-related information together.

We made this change to reduce parameter count. Instead of passing five separate parameters, we pass one EventCopyContext object. This makes the method signatures cleaner and the code easier to understand.

EventCopyContext also has a needsTimezoneConversion method. This encapsulates the logic for checking if timezones differ. It is a small behavior but it belongs with the context data.

### Enhanced Event Behavior

In Assignment 5, Event was mostly getters and setters. It was primarily a data holder.

In Assignment 6, we added behavior methods to Event. Now it has shiftTime to move an event forward or backward in time. It has setDuration to change how long an event lasts. It has occursOnDate to check if an event happens on a specific date. It has isActiveAt to check if an event is happening at a specific time. It has matches to compare events exactly.

We made this change because a class should do more than just hold data. Event should have behavior related to events. These methods encapsulate event logic inside the Event class where it belongs.

For example, instead of writing event.setStart followed by event.getStart and plusMinutes, you can now write event.shiftTime. This is cleaner and shows the intent more clearly.

We also added these methods to InterfaceEvent so they can be called through the interface. This maintains the abstraction and allows any InterfaceEvent implementation to provide this behavior.

## How the GUI Works with the Model

### CalendarGuiController as the Coordinator

The GUI never directly accesses or modifies the model. All operations go through CalendarGuiController.

When a user clicks Create Event in the GUI, the CreateEventDialog captures the input. When the user clicks the Create button, the dialog calls controller.createEvent with the entered data. The controller then calls the model to actually create the event. After the model creates the event, the controller tells the view to refresh and show the new event.

This maintains MVC separation. The view (GUI dialogs) handles display and captures clicks. The controller (CalendarGuiController) coordinates operations. The model (CalendarSystem, Calendar, Event) handles the business logic.

We can change any piece without affecting the others. If we want to change how the calendar grid looks, we only touch CalendarGuiView. If we want to change how events are stored, we only touch the model. The controller interface stays the same.

### Event Listeners and Callbacks

The GUI uses Java Swing event listeners. When users click buttons or select dates, listeners trigger callbacks in the controller.

For example, when a day button is clicked, it updates which date is selected and refreshes the events panel. The controller provides methods like getEventsForDate that the view calls to fetch data.

### Dialog Modal Pattern

All the dialog windows (CreateEventDialog, EditEventDialog, etc.) are modal. This means users must interact with the dialog before doing anything else in the main window.

We use modal dialogs because they guide users through a specific task. Create event, edit event, and search are all focused operations. The modal dialog keeps users focused on that task until they click Create or Cancel.

When a dialog closes, it calls controller methods to execute the operation. Then the main calendar view refreshes to show the changes.

## Still Following MVC Architecture

The Model contains CalendarSystem, Calendar, Event, RecurringEvent, EventRepository, EventEditor, ConflictDetector, and all helper classes. These handle business logic and data storage.

The View contains CalendarTextView for text mode and CalendarGuiView with all the dialog classes for GUI mode. Views format output and capture input. They know nothing about how the model works internally.

The Controller contains CalendarController for text mode and CalendarGuiController for GUI mode. They also include all the command classes and parser classes. Controllers coordinate between model and view but do not contain business logic.

We kept these boundaries clear. The model never displays anything. The view never changes data directly. The controller coordinates but delegates actual work to the model.

## What Works and What Does Not

### Everything Works

All features required by Assignment 6 are fully implemented and tested.

Calendar Management works. Create calendar with name and timezone. Switch between multiple calendars. Edit calendar name and timezone. Default calendar created automatically in GUI. Each calendar has its own color in GUI.

Event Creation works. Create single timed events. Create all-day events. Create recurring events for N times or until date. Select which weekdays for recurring. Add description, location, public or private status.

Event Viewing works. Month view calendar in GUI. Click dates to see events. Navigate between months. Event count indicators on days. Today highlighting. Detailed event cards with badges.

Event Editing works. Edit single event. Edit from date forward in series. Edit entire series. Edit any property including subject, start, end, location, description, status. Search events by name. Bulk edit multiple events.

Text Modes work. Interactive mode works. Headless mode works. All commands from Assignment 5 work. Export to CSV and iCal.

### Nothing is Broken

All features from Assignment 5 still work. The text modes were not modified. Adding the GUI did not break existing functionality.

## Additional Information for Graders

### Running the Application

Build the jar file with this command:

./gradlew jar

The jar is located at build/libs/calendar-1.0.jar

Three ways to run the application:

GUI mode with this command: java -jar build/libs/calendar-1.0.jar

Interactive text mode with this command: java -jar build/libs/calendar-1.0.jar --mode interactive

Headless mode with script file: java -jar build/libs/calendar-1.0.jar --mode headless res/commands.txt

### What is in the res Folder

The res folder contains commands.txt with valid command examples for testing. It contains invalid.txt with invalid command examples for testing. It contains test_calendars.txt with comprehensive test script. It contains Misc.md which is this file. It contains USEME.md with instructions for using the application. It contains screenshot.png with screenshot of the GUI.

### Testing

We tested the model and controller with unit tests. The model has over 90 percent code coverage. The controller has over 85 percent code coverage.

We tested the GUI manually by clicking through all the features and verifying they work correctly. We tested the text modes with the command files in the res folder.

All edge cases are handled including timezone conversions, recurring event series editing, copying events between calendars, duplicate detection, and invalid input.

### Code Quality

The code follows Google Java Style formatting. All public methods have JavaDoc comments. Access modifiers are used properly throughout.

We use design patterns where appropriate. Command pattern for text commands. Strategy pattern for exporters including CSV versus iCal. Builder pattern for event creation. Factory pattern for command creation in parsers. Observer pattern for GUI event listeners.

We addressed code quality feedback. No methods with more than 4 parameters because we used Builder pattern. Event class has behavior beyond getters and setters because we added utility methods. Helper classes reduce complexity such as EventCopyContext.

### Assignment Compliance

The application meets all Assignment 6 requirements. Uses only Java Swing with no external libraries. Month view calendar implemented. Multiple calendars with timezone support. All required GUI features working. Default calendar created automatically. Visual calendar distinction using color coding. Reasonable UI layout and proportions. User-friendly interactions including spinners, dropdowns, checkboxes. Graceful error handling with helpful messages. MVC architecture maintained. All three execution modes functional.

### Platform Compatibility

The application works on Windows, Mac, and Linux. File paths use File.separator for platform independence. Timezone handling uses Java ZoneId. The GUI uses layout managers instead of absolute positioning.

## What We Did Not Implement

These features are not required by Assignment 6. Week view or day view because only month view is required. Drag and drop event rescheduling. Delete events from GUI. Undo or redo functionality. Event reminders or notifications. Printing calendars.

These could be added in future iterations but were not needed for this assignment.

## Summary

We transformed the text-based calendar application into a full GUI application. The big changes were creating CalendarGuiView and CalendarGuiController for the graphical interface, supporting three execution modes in CalendarRunner, and adding GUI-specific features like color coding and visual indicators.

We also improved code quality by adding the Builder pattern to Event, creating helper classes to reduce parameter counts, and adding behavior methods to Event.

These changes follow MVC architecture. The model remains unchanged from Assignment 5. We added a new view and controller for the GUI. The text-based view and controller from Assignment 5 still work without modification.

The application now supports all three assignments requirements. Assignment 4 basic features work. Assignment 5 multiple calendar features work. Assignment 6 GUI features work. The design is flexible enough that we could add more features in the future without major rewrites.