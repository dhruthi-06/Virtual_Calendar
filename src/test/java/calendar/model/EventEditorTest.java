package calendar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import calendar.controller.CalendarController;
import calendar.view.CalendarTextView;
import calendar.view.InterfaceCalendarView;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for EventEditor.
 * Tests event property editing functionality.
 */
public class EventEditorTest {

  private InterfaceCalendarSystem model;
  private ByteArrayOutputStream outputStream;
  private InterfaceCalendarView view;
  private EventEditor editor;

  /**
   * Sets up test fixtures before each test.
   */
  @Before
  public void setUp() {
    model = new CalendarSystem();
    outputStream = new ByteArrayOutputStream();
    view = new CalendarTextView(new PrintStream(outputStream));
    editor = new EventEditor();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdatePropertyNullEvent() {
    editor.updateProperty(null, "subject", "Test");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdatePropertyNullProperty() {
    InterfaceEvent event = new Event("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, null, "value");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdatePropertyEmptyProperty() {
    InterfaceEvent event = new Event("Test",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, "  ", "value");
  }

  @Test
  public void testUpdatePropertySubject() {
    InterfaceEvent event = new Event("OldName",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, "subject", "NewName");
    assertEquals("NewName", event.getSubject());
  }

  @Test
  public void testUpdatePropertyStart() {
    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, "start", "2025-11-15T09:00");
    assertEquals(LocalDateTime.of(2025, 11, 15, 9, 0), event.getStart());
  }

  @Test
  public void testUpdatePropertyEnd() {
    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, "end", "2025-11-15T12:00");
    assertEquals(LocalDateTime.of(2025, 11, 15, 12, 0), event.getEnd());
  }

  @Test
  public void testUpdatePropertyDescription() {
    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, "description", "Important meeting");
    assertEquals("Important meeting", event.getDescription());

    editor.updateProperty(event, "description", null);
    assertEquals("", event.getDescription());
  }

  @Test
  public void testUpdatePropertyLocation() {
    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, "location", "Room 101");
    assertEquals("Room 101", event.getLocation());

    editor.updateProperty(event, "location", null);
    assertEquals("", event.getLocation());
  }

  @Test
  public void testUpdatePropertyStatus() {
    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, "status", "private");
    assertFalse(event.isPublic());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdatePropertyInvalid() {
    InterfaceEvent event = new Event("Meeting",
        LocalDateTime.of(2025, 11, 15, 10, 0),
        LocalDateTime.of(2025, 11, 15, 11, 0));
    editor.updateProperty(event, "invalid", "value");
  }

  @Test
  public void testIsValidProperty() {
    assertFalse(editor.isValidProperty(null));
    assertFalse(editor.isValidProperty(""));
    assertFalse(editor.isValidProperty("   "));

    assertTrue(editor.isValidProperty("subject"));
    assertTrue(editor.isValidProperty("start"));
    assertTrue(editor.isValidProperty("end"));
    assertTrue(editor.isValidProperty("description"));
    assertTrue(editor.isValidProperty("location"));
    assertTrue(editor.isValidProperty("status"));

    assertTrue(editor.isValidProperty("SUBJECT"));
    assertTrue(editor.isValidProperty("Start"));

    assertFalse(editor.isValidProperty("invalid"));
    assertFalse(editor.isValidProperty("duration"));
  }

  @Test
  public void testGetValidProperties() {
    String[] properties = editor.getValidProperties();
    assertNotNull(properties);
    assertEquals(6, properties.length);
    assertTrue(contains(properties, "subject"));
    assertTrue(contains(properties, "start"));
    assertTrue(contains(properties, "end"));
    assertTrue(contains(properties, "description"));
    assertTrue(contains(properties, "location"));
    assertTrue(contains(properties, "status"));
  }

  private boolean contains(String[] array, String value) {
    for (String s : array) {
      if (s.equals(value)) {
        return true;
      }
    }
    return false;
  }

  @Test
  public void testValidatePropertyValueInvalidInputs() {
    String error = editor.validatePropertyValue(null, "value");
    assertNotNull(error);
    assertTrue(error.contains("Property cannot be null or empty"));

    error = editor.validatePropertyValue("", "value");
    assertNotNull(error);
    assertTrue(error.contains("Property cannot be null or empty"));

    error = editor.validatePropertyValue("invalid", "value");
    assertNotNull(error);
    assertTrue(error.contains("Invalid property"));
  }

  @Test
  public void testValidatePropertyValueSubject() {
    String error = editor.validatePropertyValue("subject", "Meeting");
    assertNull(error);

    error = editor.validatePropertyValue("subject", "");
    assertNotNull(error);
    assertTrue(error.contains("Subject cannot be empty"));

    error = editor.validatePropertyValue("subject", null);
    assertNotNull(error);
    assertTrue(error.contains("Subject cannot be empty"));
  }

  @Test
  public void testValidatePropertyValueStartEnd() {
    String error = editor.validatePropertyValue("start", "2025-11-15T10:00");
    assertNull(error);

    error = editor.validatePropertyValue("start", "invalid");
    assertNotNull(error);
    assertTrue(error.contains("Invalid date/time format"));

    error = editor.validatePropertyValue("end", "2025-11-15T11:00");
    assertNull(error);

    error = editor.validatePropertyValue("end", "invalid");
    assertNotNull(error);
    assertTrue(error.contains("Invalid date/time format"));
  }

  @Test
  public void testValidatePropertyValueStatus() {
    String error = editor.validatePropertyValue("status", "public");
    assertNull(error);

    error = editor.validatePropertyValue("status", "private");
    assertNull(error);

    error = editor.validatePropertyValue("status", "invalid");
    assertNotNull(error);
    assertTrue(error.contains("Status must be 'public' or 'private'"));

    error = editor.validatePropertyValue("status", null);
    assertNotNull(error);
    assertTrue(error.contains("Status must be 'public' or 'private'"));
  }

  @Test
  public void testValidatePropertyValueDescriptionLocation() {
    String error = editor.validatePropertyValue("description", "Any text");
    assertNull(error);

    error = editor.validatePropertyValue("description", "");
    assertNull(error);

    error = editor.validatePropertyValue("location", "Room 101");
    assertNull(error);

    error = editor.validatePropertyValue("location", "");
    assertNull(error);
  }

  @Test
  public void testEditSubject() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event OldName from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject OldName from 2025-11-15T10:00 with NewName\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("NewName"));
    assertNotNull(model.getCalendar("Work").findEvent("NewName",
        LocalDateTime.parse("2025-11-15T10:00")));
  }

  @Test
  public void testEditStartTime() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event start Meeting from 2025-11-15T10:00 with 2025-11-15T09:00\n"
        + "print events on 2025-11-15\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    assertNotNull(model.getCalendar("Work").findEvent("Meeting",
        LocalDateTime.parse("2025-11-15T09:00")));
  }

  @Test
  public void testEditEndTime() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event end Meeting from 2025-11-15T10:00 with 2025-11-15T12:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("updated successfully"));
  }

  @Test
  public void testEditDescription() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event description Meeting from 2025-11-15T10:00 "
        + "with \"Quarterly review meeting\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("updated successfully"));
  }

  @Test
  public void testEditLocation() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event location Meeting from 2025-11-15T10:00 with \"Building A Room 101\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("updated successfully"));
  }

  @Test
  public void testEditStatus() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event status Meeting from 2025-11-15T10:00 with private\n"
        + "edit event status Meeting from 2025-11-15T10:00 with public\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("updated successfully"));
  }

  @Test
  public void testEditInvalidProperty() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event invalid Meeting from 2025-11-15T10:00 with value\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Invalid property"));
    assertTrue(output.contains("Valid properties"));
  }

  @Test
  public void testEditMultipleProperties() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject Meeting from 2025-11-15T10:00 with UpdatedMeeting\n"
        + "edit event location UpdatedMeeting from 2025-11-15T10:00 with RoomA\n"
        + "edit event description UpdatedMeeting from 2025-11-15T10:00 with Important\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("updated successfully"));
  }

  @Test
  public void testEditEmptySubject() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event subject Meeting from 2025-11-15T10:00 with \"\"\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Subject cannot be empty"));
  }

  @Test
  public void testEditStartAfterEnd() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event start Meeting from 2025-11-15T10:00 with 2025-11-15T12:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Start time cannot be after end time"));
  }

  @Test
  public void testEditStartEqualsEnd() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event start Meeting from 2025-11-15T10:00 with 2025-11-15T11:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Start time cannot equal end time"));
  }

  @Test
  public void testEditEndBeforeStart() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event end Meeting from 2025-11-15T10:00 with 2025-11-15T09:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("End time cannot be before start time"));
  }

  @Test
  public void testEditEndEqualsStart() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event end Meeting from 2025-11-15T10:00 with 2025-11-15T10:00\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("End time cannot equal start time"));
  }

  @Test
  public void testEditInvalidStatus() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event status Meeting from 2025-11-15T10:00 with invalid\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Status must be 'public' or 'private'"));
  }

  @Test
  public void testEditInvalidDateFormat() {
    String commands = "create calendar --name Work --timezone UTC\n"
        + "use calendar --name Work\n"
        + "create event Meeting from 2025-11-15T10:00 to 2025-11-15T11:00\n"
        + "edit event end Meeting from 2025-11-15T10:00 with InvalidDateTime\n"
        + "edit event start Meeting from 2025-11-15T10:00 with InvalidDateTime\n"
        + "exit\n";
    new CalendarController(model, view, new StringReader(commands)).run();

    String output = outputStream.toString();
    assertTrue(output.contains("ERROR"));
    assertTrue(output.contains("Invalid date/time format"));
  }
}