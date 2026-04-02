# Virtual Calendar System
**Academic Project — Northeastern University, Khoury College of Computer Sciences**  
Course: Programming Design Paradigm | Fall 2025

> This repository is private to maintain academic integrity for future students.

---

## Table of Contents
- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technologies Used](#technologies-used)
- [Development Process](#development-process)
- [Project Structure](#project-structure)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Design Decisions](#design-decisions)

---

## Overview

A comprehensive multi-calendar application built in Java that demonstrates software engineering principles through iterative development. The system supports complex event management, multiple calendar coordination, timezone handling, and provides both graphical and command-line interfaces.

- **Development Timeline:** September 2025 – December 2025
- **Team Size:** 2 developers
- **Total Commits:** 100+
- **Test Coverage:** 95%+

**Learning Objectives Achieved:**
- Model-View-Controller (MVC) architecture implementation
- SOLID principles application throughout codebase
- Design patterns: Factory, Observer, Strategy, Command
- Test-driven development with comprehensive coverage
- Collaborative development with code reviews
- Working with external codebases

---

## Key Features

### Calendar Management
- Create and manage multiple calendars simultaneously
- Full IANA timezone database integration (e.g., `America/New_York`, `Europe/Paris`)
- Seamlessly switch context between calendars
- Edit calendar names and timezones dynamically

### Event Management
- **Single Events:** One-time events with customizable properties
- **Recurring Events:** Repeat on specific weekdays (M/T/W/R/F/S/U), for N occurrences or until a date; supports single/forward/all series modification
- **All-Day Events:** Automatic 8 AM–5 PM scheduling
- **Multi-Day Events:** Events spanning multiple days
- **Event Properties:** Subject, description, location, start/end times, public/private status

### Advanced Operations
- Edit single events or entire series (this only / this and forward / all)
- Copy events across calendars with automatic timezone conversion
- Bulk copy all events within a date range
- Conflict detection to prevent duplicate events

### Analytics Dashboard
- Total event count and events grouped by subject
- Weekday and weekly distribution
- Monthly event trends and average events per day
- Busiest/least busy days
- Online vs. in-person event breakdown

### Data Export
- **CSV:** Google Calendar compatible format
- **iCal:** Standard `.ical` format
- Automatic format detection based on file extension

### User Interfaces
- **GUI:** Java Swing monthly calendar view with interactive date selection and event management
- **CLI – Interactive Mode:** Real-time command execution
- **CLI – Headless Mode:** Script file execution

---

## Architecture

```
┌─────────────────┐
│      View       │  ← Swing GUI / Text Interface
│  (Presentation) │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│   Controller    │  ← Command Processing / Event Handling
│   (Logic Flow)  │
└────────┬────────┘
         │
         ↓
┌─────────────────┐
│     Model       │  ← Calendar / Event / Business Logic
│  (Data & Rules) │
└─────────────────┘
```

### SOLID Principles
- **SRP:** Each class has a single, well-defined responsibility
- **OCP:** Strategy and Factory patterns allow extension without modification
- **LSP:** Proper inheritance hierarchy for event types
- **ISP:** Separate, focused interfaces for views and controllers
- **DIP:** Controller depends on view interfaces, not concrete implementations

### Design Patterns
| Pattern | Usage |
|---|---|
| MVC | Clean separation of concerns across all layers |
| Factory | `EventFactory` encapsulates event creation logic |
| Observer | Model notifies views of state changes |
| Strategy | Pluggable export strategies (CSV, iCal) and view implementations |
| Command | Uniform text command processing |
| Singleton | Calendar manager instance for controlled global state |

---

## Technologies Used

| Category | Technology | Purpose |
|---|---|---|
| Language | Java 17 | Core implementation |
| GUI Framework | Java Swing | Graphical user interface |
| Testing | JUnit 5 | Unit and integration testing |
| Build Tool | Gradle | Build automation |
| Version Control | Git | Source code management |
| Date/Time | `java.time` API | Timezone and datetime handling |

---

## Development Process

### Phase 1 — Core Calendar System (Assignment 3)
Single calendar, event creation (single + recurring), event editing, calendar queries, CSV export, CLI (interactive + headless), MVC architecture.

### Phase 2 — Multi-Calendar & Timezones (Assignment 4)
Multiple calendars, IANA timezone integration, cross-calendar copying with timezone conversion, iCal export, enhanced conflict detection.

### Phase 3 — GUI Implementation (Assignment 5)
Java Swing GUI with monthly calendar view, interactive event management, visual calendar distinction, error handling and validation.

### Phase 4 — Code Review & Analytics (Assignment 6)
External codebase integration, code review and critique, analytics dashboard (8+ metrics), documentation improvements, final testing (95%+).

---

---

## Running the Application

### Prerequisites
- Java 17 or higher
- Gradle 7.0+ (included via wrapper)

### Build
```bash
./gradlew jar
# JAR created in build/libs/
```

### Execution Modes

**GUI Mode (Default)**
```bash
java -jar build/libs/VirtualCalendarSystem.jar
```

**Interactive Text Mode**
```bash
java -jar build/libs/VirtualCalendarSystem.jar --mode interactive
```

**Headless Script Mode**
```bash
java -jar build/libs/VirtualCalendarSystem.jar --mode headless path/to/commands.txt
```

---

## Testing

**Coverage:** 95%+ overall (Model: 98%, Controller: 93%, Utilities: 96%)

```bash
./gradlew test
./gradlew test jacocoTestReport
open build/reports/jacoco/test/html/index.html
```

Test categories include unit tests, integration tests, and edge cases (duplicate detection, invalid timezones, date boundaries, recurring event edge cases, cross-calendar copying).

---

## Design Decisions

**MVC:** Enables multiple view implementations, independent testing, and view changes without touching the model.

**`java.time` API:** Immutable, thread-safe, with built-in timezone support — a clean replacement for legacy `Date`/`Calendar`.

**Strategy Pattern for Export:** Keeps CSV and iCal logic separate, testable in isolation, and easy to extend with new formats.

**In-Memory Storage:** Course requirement focused on design over persistence; data can be preserved via export/import.

---

## Notes

This project was developed as coursework demonstrating mastery of object-oriented design, software architecture, and collaborative development. The repository is kept private per university policy to maintain assignment integrity for future students.

**Developed by:** Dhruthi Rajesh  
**Institution:** Northeastern University, Khoury College of Computer Sciences  
**Course:** Programming Design Paradigm | Fall 2025
