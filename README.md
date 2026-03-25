<div align="center">

<br>

<pre>
██████╗▒▒██████╗▒██████╗▒███╗▒▒▒███╗▒█████╗▒████████╗██████╗▒██╗██╗▒▒██╗
██╔══██╗██╔═══██╗██╔══██╗████╗▒████║██╔══██╗╚══██╔══╝██╔══██╗██║╚██╗██╔╝
██║▒▒██║██║▒▒▒██║██████╔╝██╔████╔██║███████║▒▒▒██║▒▒▒██████╔╝██║▒╚███╔╝▒
██║▒▒██║██║▒▒▒██║██╔══██╗██║╚██╔╝██║██╔══██║▒▒▒██║▒▒▒██╔══██╗██║▒██╔██╗▒
██████╔╝╚██████╔╝██║▒▒██║██║▒╚═╝▒██║██║▒▒██║▒▒▒██║▒▒▒██║▒▒██║██║██╔╝▒██╗
╚═════╝▒▒╚═════╝▒╚═╝▒▒╚═╝╚═╝▒▒▒▒▒╚═╝╚═╝▒▒╚═╝▒▒▒╚═╝▒▒▒╚═╝▒▒╚═╝╚═╝╚═╝▒▒╚═╝
</pre>

# DORMATRIX

<b>IUT Female Dormitory · Islamic University of Technology</b>

<br>

[![Java](https://img.shields.io/badge/Java-JDK%2017+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)
[![Terminal](https://img.shields.io/badge/Terminal-WezTerm-4B9CD3?style=flat-square)](https://wezfurlong.org/wezterm/)
[![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?style=flat-square&logo=windows)](https://www.microsoft.com/windows)
[![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square)](#)
<br><br>

<i>A feature-rich, terminal-based dormitory management system with vivid ANSI true-color UI — built for students, staff, and administrators of the IUT Female Dormitory.</i>

<br><br>

<a href="#overview">Overview</a> ·
<a href="#demo">Demo</a> ·
<a href="#walkthrough">Walkthrough</a> ·
<a href="#features">Features</a> ·
<a href="#installation">Installation</a> ·
<a href="#architecture">Architecture</a> ·
<a href="#data-storage">Data Storage</a> ·

</div>

---

<div align="center">

<table>
<tr>
<td align="center"><b>Role-Aware Dashboards</b></td>
<td align="center"><b>ANSI True-Color UI</b></td>
<td align="center"><b>MVC + Repository</b></td>
</tr>
<tr>
<td align="center">7 unique user experiences</td>
<td align="center">Rich terminal visuals and themes</td>
<td align="center">Clean, maintainable project structure</td>
</tr>
</table>

</div>

---

<a name="overview"></a>
## Overview

<table>
<tr>
<td width="64%" valign="top">

**Dormatrix** is a fully-featured dormitory management system. It unifies all dormitory operations — room management, cafeteria services, complaint handling, facility booking, store inventory, lost & found, routines, announcements, and emergency contacts — under a single role-based platform.

Each of the **7 user roles** gets its own **color-themed dashboard**, designed to surface exactly the tools that role needs. The system is built on a clean **MVC + Repository** architecture with file-based persistence — no database setup required.

<br>

[![Dorm Life](https://img.shields.io/badge/Dorm%20Life-Integrated-FF69B4?style=flat-square)](#)
[![Architecture](https://img.shields.io/badge/MVC%20%2B%20Repository-Clean-C71585?style=flat-square)](#)
[![Persistence](https://img.shields.io/badge/Persistence-Flat%20File-DB7093?style=flat-square)](#)

</td>
<td width="36%" valign="top" align="center">

<pre>
        *       *            *      *                  *         
  *   *          ▒▒▒▒▒    *                 ██████               
             ▒▒▒▒▒▒▒▒▒▒▒         *        ██████████             
     *       ▒▒▒▒▒▒▒▒▒▒▒▒▒                ████  ████      *      
             ▒▒▒▒▒▒▒▒▒▒▒▒▒   *            ████    ██             
         *                          *     ████  ████  *      *   
  *                           ▒▒▒▒    *   ██████████     *       
            *      *        ▒▒▒▒▒▒▒▒        ██████               
   *     *       *    *     ▒▒▒▒▒▒▒▒     *        *        *     
           ▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄             
      *    █████████████████████████████████████████     *       
           █████████████████████████████████████████             
           █████████████████████████████████████████             
           ████▒▒▄▄▄▒▒██████▒▒▄▄▄▒▒██████▒▒▒▒▒▒▒████             
           ████▒█████▒██████▒████▒▒██████▒▒▓▓▓▒▒████             
           ████▒█▒█▒█▒██████▒▒▒█▒▒▒██████▒▓▓▓▓▓▒████             
           ████▒█▒▒▒█▒██████▒▒▀▀▀▒▒██████▒▒▓▓▓▒▒████             
           █████████████████████████████████████████             
           ████▒▒▒▒▒▒▒██████▒▒▀▀▀▒▒██████▒▒▒▒▒▒▒████             
           ████▒▒▓▓▒▒▒██████▒▒▒█▒▒▒██████▒▒▒▓▓▒▒████             
           ████▒▒▒▒▒▒▒██████▒▒▄▄▄▒▒██████▒▒▒▒▒▒▒████             
   ▀▀▀▀▀▀▀▀█████████████████████████████████████████▀▀▀▀▀▀▀▀▀▀   
   ███████████████████████████████████████████████████████████   
</pre>

</td>
</tr>
</table>

---

<a name="demo"></a>
## Demo

<div align="center">

<a href="#">
  <img src="assets/video-demo-cover.jpeg" alt="Dormatrix Video Demonstration" width="85%" />
</a>

<br><br>

<b>Video Demonstration</b><br>
A guided walkthrough of Dormatrix — from role selection and login to dashboard navigation and key workflows.

</div>

---

<a name="walkthrough"></a>
## Walkthrough

### Launch & Role Selection

<div align="center">
  <img src="assets/role-selection.jpeg" alt="Role Selection" width="90%" />
  <br><sub><b>Role Selection Screen</b></sub>
</div>

<br>

When Dormatrix starts, the screen fills with the **DORMATRIX** splash header rendered in ANSI true-color, followed by a matrix rain animation. Below the header is the **role selection menu**. Navigation works by typing a number and pressing Enter, or using arrow keys.

| Input | Role | Dashboard Theme |
|-------|------|----------------|
| `1` | Student | Navy Blue |
| `2` | Attendant | Deep Teal |
| `3` | Maintenance Worker | Steel Blue / Gray |
| `4` | Store In Charge | Warm Brown / Amber |
| `5` | Hall Office | Hot Pink / Magenta |
| `6` | Admin | Deep Red |
| `7` | Cafeteria Manager | Golden Yellow |
| `0` | Exit | — |

---

### Login

<div align="center">
  <img src="assets/login.jpeg" alt="Login Screen" width="90%" />
  <br><sub><b>Login Screen</b></sub>
</div>

<br>

After selecting a role, the **Login Credentials** panel prompts for a User ID and password. Password input is masked (each character echoed as `•`) using JLine. Incorrect credentials show an error and re-prompt; successful login loads the role-specific dashboard.

> Passwords are stored as hashes — plain-text passwords are never written to disk.

---

### Dashboards

> All dashboards include an **Edit Profile** option for updating contact details or changing the login password.

<details>
<summary><b>Student Dashboard</b> — 11 features</summary>

<br>

<div align="center">
  <img src="assets/student-dashboard.jpeg" alt="Student Dashboard" width="90%" />
  <br><sub><b>Student Dashboard</b></sub>
</div>

<br>

#### `[1]` View Room Info
Displays the student's current room assignment and allocation status. New students are initially marked `UNASSIGNED` until the Hall Office allocates a room. Students can also submit an application for a room change.

#### `[2]` Facility Booking
Reserve three types of shared facilities:
- **Laundry** — 6 machines. Each student can hold only **one active booking at a time**. The machine auto-releases when the wash cycle completes.
- **Study Room** — a **6-slot × 10-seat** grid. Each seat within a slot can only be held by one student; a student cannot hold more than one seat per slot.
- **Fridge** — **10 personal slots** allocated using the **First-Fit algorithm**. If all 10 are occupied, the booking is rejected.

#### `[3]` Meal Token Purchase
Purchase meal tokens for the cafeteria. The system enforces purchase windows — tokens cannot be bought after a meal's service window closes. Tokens carry a status: `ACTIVE`, `USED`, or `EXPIRED`. Meal times automatically switch when Ramadan mode is enabled by the Cafeteria Manager.

| Mode | Meal | Window |
|------|------|--------|
| Normal | Breakfast | 07:00 – 09:30 (10:00 on weekends) |
| Normal | Lunch | 12:00 – 14:00 |
| Normal | Dinner | 19:00 – 21:00 |
| Ramadan | Suhoor | 03:00 – 04:30 |
| Ramadan | Iftar | 18:00 – 19:15 |
| Ramadan | Dinner | 19:30 – 21:30 |

#### `[4]` Store Account & Dues
View the full store account — current balance, itemized transaction history, and any outstanding dues.

#### `[5]` Lost & Found
Report a lost item with description, category, and last known location — or browse the found items list to claim something. Items are logged with timestamps and a Claimed / Unclaimed status.

#### `[6]` Complaint Menu
Submit a complaint under one of four categories: **Electricity, Plumbing, Internet, or Cleaning**. The system automatically detects emergency keywords, assigns priority, and routes to the appropriate worker. Previously submitted complaints can be tracked by ID.

#### `[7]` Weekly Routine
View and manage your personal weekly schedule — class timings, activities, or any custom entries.

#### `[8]` View Announcements
Read official notices posted by the Hall Office or Attendants, ordered by most recent.

#### `[9]` Store Shopping Cart
Browse available store items, add them to a cart, and confirm the purchase. Orders are billed to the student's store account.

#### `[10]` Emergency Contacts
View dormitory emergency contacts — on-duty warden, campus medical, security desk — and manage your own personal emergency contact.

<br>

</details>

<details>
<summary><b>Attendant Dashboard</b> — 7 features</summary>

<br>

<div align="center">
  <img src="assets/attendant-dashboard.jpeg" alt="Attendant Dashboard" width="90%" />
  <br><sub><b>Attendant Dashboard</b></sub>
</div>

<br>

#### `[1]` Handle Student Complaints
View all submitted complaints. Update status, add resolution notes, and assign to the appropriate maintenance worker.

#### `[2]` Handle Worker Schedule
Assign and manage the weekly shifts of maintenance workers and cleaning staff.

#### `[3]` Add Found Items
Log newly found items into the Lost & Found system — description, location, date, and status.

#### `[4]` View Student Routine
Browse the weekly routines of all students — useful for planning inspections without disrupting class timetables.

#### `[5]` Announcements
Create, edit, retract, and archive hall-wide announcements.

#### `[6]` Manage Emergency Contacts
Add, update, or remove the official emergency contacts shown on every student's dashboard.

<br>

</details>

<details>
<summary><b>Maintenance Worker Dashboard</b> — 4 features</summary>

<br>

<div align="center">
  <img src="assets/maintenance-dashboard.jpeg" alt="Maintenance Worker Dashboard" width="90%" />
  <br><sub><b>Maintenance Worker Dashboard</b></sub>
</div>

<br>

#### `[1]` View Work Field
Displays the worker's assigned specialization:

| Specialization | Responsibilities |
|---|---|
| Electrician | Electrical faults, wiring, and power issues |
| Plumber | Plumbing, water leaks, and pipe issues |
| Internet Tech | Connectivity and network issues |
| Cleaning | Cleaning and sanitation of dorm facilities |

#### `[2]` View Task
View all complaints assigned to this worker — with priority level, category, student name, room number, and current status. Workers can update status to `IN_PROGRESS` or `RESOLVED`.

#### `[3]` View Routine
View the worker's assigned visit schedule for the dormitory.

<br>

</details>

<details>
<summary><b>Store-in-Charge Dashboard</b> — 4 features</summary>

<br>

<div align="center">
  <img src="assets/store-dashboard.jpeg" alt="Store-in-Charge Dashboard" width="90%" />
  <br><sub><b>Store-in-Charge Dashboard</b></sub>
</div>

<br>

#### `[1]` Inventory Management
View the full item catalog with stock levels. Add products, restock, update prices, or flag items as out of stock.

#### `[2]` Process Purchase
Review and fulfill pending student orders. Confirm orders, deduct inventory, and bill the student's account. Orders can be rejected with a reason if stock is unavailable.

#### `[3]` Sales Summary & Reports
View daily and weekly totals, most purchased items, revenue summaries, and low-stock alerts.

<br>

</details>

<details>
<summary><b>Hall Office Dashboard</b> — 6 features</summary>

<br>

<div align="center">
  <img src="assets/hall-office-dashboard.jpeg" alt="Hall Office Dashboard" width="90%" />
  <br><sub><b>Hall Office Dashboard</b></sub>
</div>

<br>

#### `[1]` Add New Room
Register a new room — set the room number and capacity.

#### `[2]` View Available Rooms
Real-time list of all rooms with open slots, showing occupancy status (`AVAILABLE` / `FULL`).

#### `[3]` Browse All Rooms
Full catalog of every room including occupied ones, with current occupancy counts and capacity details.

#### `[4]` Assign Room To Unassigned Student
Look up a student by ID or name and assign them a room. Only works for students currently marked `UNASSIGNED`.

#### `[5]` Review Room Change Applications
View, approve, or reject room change requests. Applications follow the lifecycle: `PENDING → COMPLETED / REJECTED`.

<br>

</details>

<details>
<summary><b>Admin Dashboard</b> — 4 features</summary>

<br>

<div align="center">
  <img src="assets/admin-dashboard.jpeg" alt="Admin Dashboard" width="90%" />
  <br><sub><b>Admin Dashboard</b></sub>
</div>

<br>

#### `[1]` Create Account
Create accounts for any role. Input is validated with custom exceptions:

- `InvalidEmailException` — malformed email address
- `InvalidPhoneException` — invalid phone number format
- `InvalidDepartmentException` — unrecognized department
- `InvalidPasswordException` — password does not meet requirements
- `UserAlreadyExistsException` — ID already registered

#### `[2]` Delete Account
Remove a user account by ID, with confirmation.

#### `[3]` View & Search Accounts
Browse all registered users across roles, or search by name or ID.

<br>

</details>

<details>
<summary><b>Cafeteria Manager Dashboard</b> — 5 features</summary>

<br>

<div align="center">
  <img src="assets/cafeteria-dashboard.jpeg" alt="Cafeteria Manager Dashboard" width="90%" />
  <br><sub><b>Cafeteria Manager Dashboard</b></sub>
</div>

<br>

The dashboard includes a live, animated **meal slot progress bar** showing the current meal window and remaining time.

#### `[1]` Update Weekly Menu
Set or update the weekly meal menu for each day and meal type, for both normal and Ramadan modes.

#### `[2]` Schedule Special Event
Plan special cafeteria events or custom meal days outside the standard weekly menu.

#### `[3]` Verify Student Token
Manually verify a meal token by ID — confirms whether it is `ACTIVE`, `USED`, or `EXPIRED`.

#### `[4]` Toggle Ramadan Mode
Switch the entire food system between normal and Ramadan meal times with one toggle. The change persists immediately for all users.

<br>

</details>

---

<a name="features"></a>
## Features

### Smart Complaint Engine

Every complaint description is evaluated against a list of emergency keywords before priority is assigned:

```text
Fire · Smoke · Burning · Sparks · Electric Shock
Flood · Burst Pipe · Overflow · Danger · Panic
```

A keyword match automatically elevates the complaint to `EMERGENCY` priority. Otherwise it defaults to `NORMAL` and is routed to the correct worker field:

```text
ELECTRICITY  →  ELECTRICIAN
PLUMBING     →  PLUMBER
INTERNET     →  INTERNET_TECH
CLEANING     →  CLEANING
```

Complaints follow a four-stage lifecycle: `SUBMITTED → ASSIGNED → IN_PROGRESS → RESOLVED`

### UI Highlights

- **7 distinct ANSI true-color themes** — one per role, applied down to the background fill
- **Matrix rain animation** on first login to each role's dashboard
- **Live animated progress bar** in the Cafeteria Manager dashboard showing the current meal slot
- **Arrow key + number navigation** throughout all menus
- **Lilac highlight bar** on the currently selected menu item
- **Dynamic terminal sizing** — layout adapts to your window dimensions

<details>
<summary><b>Custom-Built Libraries</b></summary>

<br>

All collection and utility needs are served by custom-built libraries — no Java collections used:

| Library | Class | What It Does |
|---------|-------|--------------|
| `libraries/collections` | `MyArrayList<T>` | Full ArrayList re-implementation with dynamic resizing |
| `libraries/collections` | `MyString` | String wrapper with `split`, `toLowerCase`, `containsAny`, `replace` |
| `libraries/collections` | `MyOptional<T>` | Optional wrapper for safe null handling |
| `libraries/hashing` | `HashFunction` | DJB2 + XOR combined hash for password storage |
| `libraries/slots` | `FirstFitAllocator` | First-fit slot allocation for fridge booking |
| `libraries/slots` | `SlotAllocator` | Abstract base for slot allocation strategies |
| `libraries/file` | `TextFile` | File read/write utility |
| `libraries/logs` | `Logger` | System event logging |

<br>

</details>

---

<a name="installation"></a>
## Installation

### Prerequisites

| Requirement | Version | Link |
|-------------|---------|------|
| Java JDK | 17 or higher | [Download](https://www.oracle.com/java/technologies/downloads/) |
| WezTerm | Latest stable | [Download](https://wezfurlong.org/wezterm/) |

> Standard **Windows Command Prompt** and **PowerShell** are not supported. Use **WezTerm** or Windows Terminal with VT processing enabled.

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/tayma-06/Dormatrix.git
cd Dormatrix/code

# 2. First-time build and run
setup.bat

# 3. Subsequent runs
run.bat
```

### Default Admin Credentials

```text
User ID  :  admin
Password :  admin123
```

> Change the admin password after first login in any real deployment.

<details>
<summary><b>Troubleshooting</b></summary>

<br>

| Problem | Fix |
|---------|-----|
| Colors not rendering | Ensure WezTerm or Windows Terminal with VT processing is enabled |
| `jline` not found on startup | Confirm `lib/jline3.jar` is present and re-run `setup.bat` |
| `java: command not found` | Check that `JAVA_HOME` is set and JDK 17+ is on your PATH |
| Blank or broken screen on launch | Terminal window too small — resize to at least 120×40 characters |

<br>

</details>

---

## Project Structure

<details>
<summary><b>View full project structure</b></summary>

<br>

```text
Dormatrix/code/
├── src/
│   ├── Dormatrix.java
│   ├── cli/
│   │   ├── announcement/
│   │   ├── complaint/
│   │   ├── contacts/
│   │   ├── dashboard/
│   │   │   ├── food/
│   │   │   └── room/
│   │   ├── forms/
│   │   ├── profile/
│   │   ├── routine/
│   │   ├── schedule/
│   │   └── views/
│   ├── controllers/
│   │   ├── account/
│   │   ├── announcement/
│   │   ├── authentication/
│   │   ├── balance/
│   │   ├── complaint/
│   │   ├── contacts/
│   │   ├── dashboard/
│   │   ├── facilities/
│   │   ├── food/
│   │   ├── miscellaneous/
│   │   ├── profile/
│   │   ├── room/
│   │   ├── routine/
│   │   ├── schedule/
│   │   └── store/
│   ├── exceptions/
│   │   ├── account/
│   │   ├── config/
│   │   ├── food/
│   │   ├── InsufficientInventoryException.java
│   │   ├── InvalidChoiceException.java
│   │   └── SlotUnavailableException.java
│   ├── libraries/
│   │   ├── collections/
│   │   │   ├── MyArrayList.java
│   │   │   ├── MyOptional.java
│   │   │   └── MyString.java
│   │   ├── file/
│   │   │   ├── FilePaths.java
│   │   │   └── TextFile.java
│   │   ├── hashing/
│   │   │   └── HashFunction.java
│   │   ├── logs/
│   │   │   └── Logger.java
│   │   └── slots/
│   │       ├── FirstFitAllocator.java
│   │       └── SlotAllocator.java
│   ├── models/
│   │   ├── announcements/
│   │   ├── complaints/
│   │   ├── contacts/
│   │   ├── enums/
│   │   ├── facilities/
│   │   ├── food/
│   │   ├── miscellaneous/
│   │   ├── room/
│   │   ├── routine/
│   │   ├── schedule/
│   │   ├── store/
│   │   └── users/
│   ├── module/
│   │   └── complaint/
│   ├── repo/
│   │   └── file/
│   ├── tests/
│   │   ├── TerminalUITest.java
│   │   └── UnitTests.java
│   ├── themes.json
│   └── utils/
│       ├── BackgroundFiller.java
│       ├── CafeteriaAsciiUI.java
│       ├── ConsoleColors.java
│       ├── ConsoleUtil.java
│       ├── FastInput.java
│       ├── FeaturePaths.java
│       ├── InputHelper.java
│       ├── RoleMapper.java
│       ├── TerminalUI.java
│       ├── TerminalUIExtras.java
│       └── TimeManager.java
├── data/
│   ├── users/
│   ├── complaints/
│   ├── routines/
│   ├── schedules/
│   ├── announcements/
│   ├── contacts/
│   ├── facility/
│   └── foods/
├── config/
├── lib/                       
├── assets/
├── setup.bat                  
└── run.bat                     
```

</details>

---

<a name="architecture"></a>
## Architecture

Dormatrix uses a clean **MVC + Repository** layered architecture:

```text
┌──────────────────────────────────────────────────────────┐
│                     CLI Layer (View)                     │
│   Dashboards · Forms · Views · Complaint/Routine Screens │
└─────────────────────────┬────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────┐
│                Controllers (Business Logic)              │
│  Auth · Room · Food · Store · Complaint · Facilities     │
└─────────────────────────┬────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────┐
│               Repositories (Data Access)                 │
│         File-based flat-file persistence layer           │
└─────────────────────────┬────────────────────────────────┘
                          │
┌─────────────────────────▼────────────────────────────────┐
│                    Models (Entities)                     │
│  Users · Rooms · Food · Complaints · Store · Facilities  │
└──────────────────────────────────────────────────────────┘
```

---

<a name="data-storage"></a>
## Data Storage

<details>
<summary><b>View all data files</b></summary>

<br>

| File | Contents |
|------|----------|
| `data/users/students.txt` | `ID\|Name\|STUDENT\|Dept\|Hash\|Phone\|Email\|Room` |
| `data/users/admin.txt` | Admin credentials |
| `data/users/hall_attendants.txt` | Attendant records |
| `data/users/maintenance_workers.txt` | Worker records with field specialization |
| `data/users/store_in_charges.txt` | Store-in-Charge records |
| `data/users/hall_officers.txt` | Hall Officer records |
| `data/users/cafeteria_managers.txt` | Cafeteria Manager records |
| `data/rooms/rooms.txt` | Room records with capacity and occupancy |
| `data/complaints/complaints.txt` | All complaints with status, priority, tags |
| `data/room_change_applications/` | Room change request records (`RCA-` prefixed IDs) |
| `data/routines/student_routines.txt` | Weekly routine entries per student |
| `data/schedules/worker_visits.txt` | Maintenance worker visit schedule |
| `data/announcements/announcements.txt` | Hall announcement records |
| `data/contacts/emergency_contacts.txt` | Emergency contact entries |
| `data/facility/laundrySlots.txt` | Current laundry booking state (`slotIndex,studentId`) |
| `data/foods/config.txt` | Ramadan mode toggle (`RAMADAN=true/false`) |
| `data/store/dues.txt` | Student due records (`studentId,amount`) |
| `data/store/sales.txt` | Sales log (`studentId,itemId,qty,total,date`) |
| `data/inventories/inventory.txt` | Store item catalog (`itemId,name,qty,price`) |
| `data/lostItems.txt` | Lost item reports with description and category |
| `data/foundItems.txt` | Found items with claimed status and claimer ID |
| `config/admin.config` | Admin credentials (hashed on first write) |

<br>

</details>

---

## Testing

Dormatrix includes a comprehensive **JUnit 4** test suite (300+ tests) in `src/tests/`. Tests use file snapshotting to isolate each test from real data — every test saves the original file state and restores it after completion.

<details>
<summary><b>View full test coverage breakdown</b></summary>

<br>

**Custom Libraries**
- `HashFunction` — determinism, uniqueness, hex output, empty-input edge case
- `MyArrayList` — add, get, set, remove, contains, indexOf, clear, dynamic resizing, forEach, out-of-bounds throws
- `MyString` — split, concat, trim, case conversion, substring, contains, containsAny, join, replace, intToHex, null constructor, edge-case substrings
- `MyOptional` — present/empty states, `get`, `orElse`, `ofNullable`, throws on null `of` and empty `get`
- `FirstFitAllocator` — empty slots, partial occupancy, all-full throws `SlotUnavailableException`

**Authentication & Accounts**
- `CreateAccountController` — valid creation for all roles, invalid email/phone/password/department, duplicate ID
- `DeleteAccountController` — invalid role, wrong admin password, successful deletion, user-not-found
- `SearchUserController` — blank input, search by ID across all role files, trim-before-search, not-found returns null
- `AccountManager` — filename mapping, `userExists`, `registerUser`, `deleteUser`, `findUserDetails` across files

**Models & Serialization**
- `Student`, `Room`, `Item`, `DueRecord`, `StudentBalance`, `CartItem`, `ShoppingCart`, `MealToken`, `DailyMenu`, `RoutineEntry`, `MaintenanceWorker`, `HallOfficer`, `CafeteriaManager`, `StoreInCharge` — construction, getters, `toFileString` / `fromString` round-trips

**Facility Slot Booking**
- **Laundry** — slot conflict prevention, duplicate booking detection, out-of-range slot index rejection
- **Study Room** — seat conflict, student-already-booked detection, invalid seat number rejection
- **Fridge** — all-slots-full throws `SlotUnavailableException`, partial occupancy returns correct first-fit index
- **Room** — full-room allocation blocked, occupancy caps at capacity, decrement floors at zero

**Store System**
- `DueController` — missing student returns 0, `addDue` accumulates, `payDue` removes entry correctly
- `InventoryController` — add, duplicate, update, delete, restock, case-insensitive search, price range filter, low-stock threshold
- `PurchaseController` — insufficient stock returns false, credit purchase deducts inventory and records due and sale
- `PurchaseHistoryController` — filters by student, recent-days filter, totals correct
- `SalesSummaryController` — daily summary, custom date range, revenue and average correct

**Room Management**
- `RoomController` — add room persists, allocate increments occupancy, free decrements, full room returns false
- `RoomService` — resolve student by ID or name, change room (null/blank/same/full/missing all return false), successful move updates student file and occupancy counts
- Room change application lifecycle — submit, block second pending, approve, reject

**Complaint Engine**
- `ComplaintPolicy` — normal priority for benign descriptions, `EMERGENCY` for fire/smoke/electric-shock/flood/burst-pipe keywords, correct worker routing for all 4 categories
- `Complaint` — `createNew`, `assignTo` / `clearAssignment` lifecycle, `appendTagNote`, full 4-stage status transition

**Food & Tokens**
- `MealToken` — creation, status lifecycle, past-date auto-expires, serialization round-trip
- Duplicate active token detection, used token cannot be re-verified, expired token blocked

**Routines & Schedules**
- `RoutineController` — slot index bounds validation, `putSlotByStudentId`, `clearSlotByStudentId`, `writeComplaintVisit`
- `WorkerScheduleController` — `isDefaultDutyDay` for all worker fields and days, `manualPlanComplaint` rejects out-of-bounds, `autoPlanComplaint`

**Profile Management**
- `changePassword` — empty/mismatched/too-short/no-digit password rejected
- `updatePhoneNumber` — empty/invalid format rejected

<br>

</details>

---

## License

This project is licensed under the **MIT License** — see [LICENSE](LICENSE) for details.

---

## Team

**Dormatrix** — SWE4304 SPL-1 Project  
CSE Department, Islamic University of Technology, Gazipur, Bangladesh

| Name | GitHub |
|------|--------|
| Procheta Silvie | [@prochetaSilvie](https://github.com/prochetaSilvie) |
| Khadiza Sultana | [@tayma-06](https://github.com/tayma-06) |
| Sayma Tasnim | [@SayTas](https://github.com/SayTas) |
| Ayman Binte Altaf Nondiny | [@aymannondiny](https://github.com/aymannondiny) |

---

<div align="center">

<b><a href="https://github.com/tayma-06/Dormatrix">GitHub Repository</a></b>

</div>
