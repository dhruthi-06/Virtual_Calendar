# USEME.md

## Calendar Application - Assignment 7 Part 2

---

## How to Run the Calendar Application

### Prerequisites
- Java 11 or higher installed
- The JAR file built and located in `build/libs/`

### Building the Application
```bash
./gradlew jar
```
This creates `calendar-1.0.jar` in the `build/libs/` directory.

---

## Running Modes

### GUI Mode (Default)
Launch the graphical interface by double-clicking the JAR file or running:
```bash
java -jar build/libs/calendar-1.0.jar
```

The graphical user interface will open automatically. This is the primary mode for Assignment 6 and 7.

### Interactive Mode
Run the calendar in interactive text mode where you can enter commands one at a time:
```bash
java -jar build/libs/calendar-1.0.jar --mode interactive
```

In interactive mode, you'll see a welcome message and can type commands directly.

### Headless Mode
Run the calendar with a command file for scripting:
```bash
java -jar build/libs/calendar-1.0.jar --mode headless res/commands.txt
```

**Important:** Your command file must end with the `exit` command.

---

## GUI Mode Instructions

### Getting Started

When you first launch the GUI, the application automatically creates a default calendar called **"My Calendar"** in your system's timezone. You can immediately start creating events or create additional calendars.

### Main Interface Components

The GUI consists of three main areas:

**Top Panel:** Calendar management buttons (New Calendar, Switch Calendar, Edit Calendar) and month navigation controls (Previous, Next, Today)

**Center Panel:** Month view calendar grid showing all days of the current month

**Right Panel:** Events list for the selected date and action buttons (Create Event, Edit Event, Search Events, Dashboard)

---

### Calendar Management

#### Creating a New Calendar

1. Click the **"New Calendar"** button in the top-left area
2. Enter a calendar name (e.g., "Work", "Personal", "School")
3. Select a timezone from the dropdown menu
4. Click **"Create"**

**Example:**
```
Name: Work Calendar
Timezone: America/Los_Angeles
```

Each calendar is assigned a unique color for visual distinction throughout the interface.

#### Switching Between Calendars

1. Click the **"Switch Calendar"** button
2. Select a calendar from the list
3. Click **"Switch"**

The current calendar name is displayed at the top in its assigned color. The month view and events panel update to show the selected calendar's data.

#### Editing Calendar Properties

1. Click the **"Edit Calendar"** button
2. Modify the calendar name or timezone
3. Click **"Save Changes"**

**Note:** Changing the timezone affects how all event times are displayed. Event times are converted to the calendar's timezone.

---

### Viewing the Calendar

#### Month Navigation

- Click **"<"** to go to the previous month
- Click **">"** to go to the next month
- Click **"Today"** to return to the current month

The current month and year are displayed in the center of the navigation bar.

#### Understanding the Calendar Grid

- **Today's Date:** Displayed with a lighter colored background
- **Selected Date:** Highlighted with a thick colored border (matches calendar color)
- **Days with Events:** Show a bullet point and count (e.g., "• 3")
- **Hover Effect:** Days brighten slightly when the mouse hovers over them

#### Viewing Events for a Specific Date

1. Click any day in the calendar grid
2. The selected date is highlighted
3. Events for that date appear in the right panel
4. Each event card displays:
    - Event subject/name
    - Time (or "All Day" for all-day events)
    - Location (if specified)
    - Description preview (if specified)
    - Status badges:
        - **Public** (green badge)
        - **Private** (red badge)
        - **Recurring** (purple badge) if part of a series

---

### Creating Events

#### Creating a Single Event

1. Click on the date where you want to create the event
2. Click the **"Create Event"** button in the right panel
3. Fill in the event details:

**Required Fields:**
- **Subject:** Event name or title
- **Start Time:** Hour and minute (use spinner controls)
- **End Time:** Hour and minute (use spinner controls)

**Optional Fields:**
- **Location:** Where the event takes place
- **Description:** Additional details or notes
- **Visibility:** Public (default) or Private

4. Click **"Create Event"**

**Notes:**
- The subject field validates in real-time (shows error if empty)
- End time must be after start time (validated automatically)
- Times use 24-hour format (e.g., 14:00 = 2:00 PM)
- Error messages appear in red below fields

**Example:**
```
Subject: Team Meeting
Start Time: 10:00
End Time: 11:30
Location: Conference Room A
Description: Weekly sync-up meeting
Visibility: Public
```

#### Creating a Recurring Event

1. Click on the date for the first occurrence
2. Click the **"Create Event"** button
3. Fill in the event details (subject, times, location, description)
4. Check the **"Make this a recurring event"** checkbox
5. The recurring options panel expands:

**Weekday Selection:**
- Check boxes for the days the event repeats
- Example: Check Mon, Wed, Fri for an MWF pattern
- At least one weekday must be selected

**Frequency Options:**
- **After N occurrences:** Select this option and use the spinner to set the count (e.g., 10 times)
- **On specific date:** Select this option and choose year, month, and day from dropdowns

6. Click **"Create Event"**

**Notes:**
- All occurrences have the same start and end time
- Recurring events cannot span multiple days (must start and end on same day)
- Each occurrence is created as a separate event with a shared series ID

**Example:**
```
Subject: CS5010 Lecture
Start Time: 14:00
End Time: 16:30
Weekdays: ☑ Monday ☑ Wednesday
Frequency: After 12 occurrences
```

---

### Editing Events

#### Selecting an Event to Edit

1. Click on a date that has events
2. Review the events in the right panel
3. Click the **"Edit Event"** button
4. Select the event from the dropdown menu (displays subject and time)

#### Choosing What to Change

**Property Selection:**
- Select the property to edit from the dropdown:
    - **subject** - Event name/title
    - **start** - Start date and time
    - **end** - End date and time
    - **description** - Event details
    - **location** - Event location
    - **status** - Public or private

**New Value:**
- Enter the new value in the input field
- For date/time properties, use the date and time pickers
- For status, select from the dropdown (public or private)

#### Edit Scope (for Recurring Events)

For recurring events, choose the scope of your edit:

1. **This event only:** Changes apply to only this single occurrence
2. **This and all following events:** Changes apply from this date forward
3. **All events in the series:** Changes apply to every occurrence in the series

**Notes:**
- Single (non-recurring) events only have the "This event only" option enabled
- Editing the start or end time of some occurrences may split the series
- Changes apply immediately after clicking "Save Changes"

**Example:**
```
Event: "Team Meeting at 10:00"
Property: location
New Value: Conference Room B
Scope: This event only
```

---

### Searching and Bulk Editing Events

#### Using the Search Feature

1. Click the **"Search Events"** button in the right panel
2. Enter the event name in the search field
3. Click **"Search"**
4. Results display all matching events with their dates and times

The search looks for exact matches of the event subject across the entire current calendar.

### Calendar Dashboard (New Feature - Assignment 7 Part 2)

The Calendar Analytics Dashboard is a powerful new feature that provides comprehensive insights into your calendar usage patterns. This feature helps you analyze and monitor how you use your calendar over any selected date range.

#### Accessing the Dashboard

1. Launch the application in GUI mode (default mode)
2. Ensure you have a calendar selected (the application creates "My Calendar" by default)
3. Click the **"Dashboard"** button in the right panel (below the Search Events button)
4. The Calendar Dashboard dialog window will open

#### Using the Dashboard

1. GUI Mode:

**Step 1: Set Date Range**

The dashboard dialog opens with default date fields pre-filled:
- **Start Date:** Automatically set to one month ago (e.g., if today is 2025-12-15, it shows 2025-11-15)
- **End Date:** Automatically set to today's date

You can modify these dates:
- Click in the **Start Date** field and enter a date in `YYYY-MM-DD` format (e.g., `2025-11-01`)
- Click in the **End Date** field and enter a date in `YYYY-MM-DD` format (e.g., `2025-11-30`)
- Both dates are **inclusive** (events on both the start and end dates are included)

**Step 2: Calculate Analytics**

1. After entering your desired date range, click the **"Calculate Analytics"** button
2. The analytics results will appear in the scrollable results panel below
3. If there are any errors (invalid date format, start date after end date, etc.), a red error message will appear below the date fields

2. Interactive Text Mode

You can also test the dashboard through the text interface.

Example commands:

show calendar dashboard from 2025-11-01 to 2025-11-30

This will print all dashboard metrics in the console.
Dates must be in YYYY-MM-DD format and both dates are inclusive.


#### Dashboard Metrics 

The dashboard provides the following metrics:

1. **Total Number of Events**
   - Shows the total count of all events in the selected date range for the current calendar

2. **Events by Subject**
   - Groups events by their subject/name
   - Shows the count for each unique event subject
   - Sorted by count (most frequent events first)
   - Example: "Team Meeting: 12", "Office Hours: 8"

3. **Events by Weekday**
   - Shows event distribution across days of the week
   - Displays counts for Monday through Sunday
   - Helps identify which days of the week are busiest

4. **Events by Week**
   - Groups events by week number within the date range
   - Week numbers are calculated from the start of the year
   - Sorted chronologically

5. **Events by Month**
   - Groups events by month
   - Shows month names (January, February, etc.) with event counts
   - Sorted chronologically

6. **Average Events per Day**
   - Calculates the average number of events per day in the date range
   - Displayed with 2 decimal places (e.g., "3.45")

7. **Busiest Day**
   - Identifies the single day with the most events
   - Displays the date in YYYY-MM-DD format
   - Shows "(none)" if no events exist in the range

8. **Least Busy Day**
   - Identifies the single day with the fewest events (excluding days with zero events)
   - Displays the date in YYYY-MM-DD format
   - Shows "(none)" if no events exist in the range

9. **Online vs Not Online Events**
   - **Online events:** Percentage of events whose location contains the word "online" (case-insensitive)
   - **Not online events:** Percentage of events that are not online
   - Both percentages are displayed with 2 decimal places
   - Example: "Online events: 45.50%", "Not online events: 54.50%"

#### Screenshots

Screenshots of the Calendar Dashboard feature are available in the `res/` directory:
- `Dashboard1.png` 
- `Dashboard2.png`

### Provider Code Issues

The provider’s code had some issues related to existing features:

* The **Edit Calendar** functionality does not work correctly and throws an error.
* The provider code did not include support for **all-day events**, which is listed as a required feature in earlier assignment.

However, neither of these issues impacted the implementation of the new dashboard feature.
All the necessary functionality for retrieving events, selecting date ranges, and displaying calendars worked correctly.
Therefore, I was able to fully integrate the dashboard into both the text interface and the GUI.

Additionally, although we requested the provider to address these issues, the fixes were not included in the final code 
delivery. As a result, these original limitations still remain in their codebase.
