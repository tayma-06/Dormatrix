<div align="center">

![Dormatrix Banner](assets/banner.png)

# DORMATRIX

**IUT Female Dormitory · Islamic University of Technology**

[![Java](https://img.shields.io/badge/Java-JDK%2017+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)
[![Terminal](https://img.shields.io/badge/Terminal-WezTerm-4B9CD3?style=flat-square)](https://wezfurlong.org/wezterm/)
[![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?style=flat-square&logo=windows)](https://www.microsoft.com/windows)
[![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square)](#)

A feature-rich, terminal-based dormitory management system with vivid ANSI true-color UI — built for students, staff, and administrators of the IUT Female Dormitory.

[📋 Overview](#-overview) · [🎬 Walkthrough](#-walkthrough) · [🎭 Roles](#-user-roles) · [✨ Features](#-features) · [⚙️ Installation](#️-installation) · [🏗️ Architecture](#️-architecture) · [💾 Data Storage](#-data-storage) · [🤝 Contributing](#-contributing)

</div>

---

## 📋 Overview

**Dormatrix** is a fully-featured dormitory management system with a beautifully styled ANSI CLI. It unifies all dormitory operations — room management, cafeteria services, complaint handling, facility booking, store inventory, and more — under a single role-aware platform.

Each of the 7 user roles gets its own **color-themed dashboard**, designed to surface exactly the tools that role needs. The system is built on a clean **MVC + Repository** architecture with file-based persistence — no database setup required.

> 💡 The name is a blend of *Dormitory* + *Matrix*, reflecting a structured, interconnected system for dorm life. On first login, each role triggers a **matrix rain animation** before the dashboard appears.

---

## 🎬 Walkthrough

This section walks through the application exactly as a user experiences it — from launch to every role's dashboard.

---

### Step 1 — Launch & Role Selection

![Role Selection](assets/role-selection.png)

When Dormatrix starts, the screen is filled with the **DORMATRIX** splash header in a purple-to-cyan gradient rendered with ANSI true-color escape codes, underneath which reads *IUT Female Dormitory · Islamic University of Technology*.

Below the header is the **role selection menu** — the entry point for every session. The currently highlighted option is shown in a **lilac highlight bar**, and navigation works both by typing a number and pressing Enter, as well as arrow keys.

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

### Step 2 — Login

![Login Screen](assets/login.png)

After selecting a role, the screen transitions to the **Login Credentials** panel with two fields:

- **User ID** — your unique identifier registered in the system
- **Password** — entered with masked input (each character echoed as `*`) using JLine, so nothing is visible on screen

Credentials are validated against the stored records for the selected role. Incorrect input shows an error and re-prompts. Successful login triggers a **matrix rain animation** (on first login per session), then loads the role-specific dashboard.

> 🔐 Passwords are stored as hashes computed by a custom **DJB2 + XOR combined hash function** (`libraries/hashing/HashFunction.java`) — plain-text passwords are never written to disk.

---

### Step 3 — Student Dashboard

![Student Dashboard](assets/student-dashboard.png)

The **Student Dashboard** is themed in deep **navy blue**. The welcome banner shows the logged-in student's name (e.g. *Welcome, Khadiza Sultana*). Students have access to **11 features** covering every aspect of dormitory life:

#### `[1]` View Room Info
Displays the student's current room assignment — room number, block, and allocation status. New students are initially marked `UNASSIGNED` until the Hall Office allocates a room.

#### `[2]` Facility Booking
Reserve three types of shared facilities:
- **Laundry** — 6 machines displayed as a grid. Each student can hold only one booking at a time. After booking, a timer starts and the machine auto-releases when the wash cycle completes. Bookings persist across sessions via `data/facility/laundrySlots.txt`.
- **Study Room** — seat-based booking for the communal study space.
- **Fridge** — personal fridge slot allocation using a **First-Fit algorithm** (`libraries/slots/FirstFitAllocator.java`) to automatically assign the next available slot.

#### `[3]` Meal Token Purchase
Purchase meal tokens for the cafeteria. The system is time-aware and enforces purchase windows — you cannot buy a token for a meal after its service window has already closed. Tokens carry a status: `ACTIVE`, `USED`, or `EXPIRED`. The system automatically switches to **Ramadan mode** meal times when enabled by the Cafeteria Manager.

| Mode | Meal | Window |
|------|------|--------|
| Normal | Breakfast | 07:00 – 09:30 (10:00 on weekends) |
| Normal | Lunch | 12:00 – 14:00 |
| Normal | Dinner | 19:00 – 21:00 |
| Ramadan | Suhoor | 03:00 – 04:30 |
| Ramadan | Iftar | 18:00 – 19:15 |
| Ramadan | Dinner | 19:30 – 21:30 |

#### `[4]` Store Account & Dues
View the full store account — current balance, itemized transaction history, and any outstanding dues. Every purchase made through the system (meal tokens, store items) is recorded here.

#### `[5]` Lost & Found
Report a lost item with description, category, and last known location — or browse the found items list to claim something. Items are logged with timestamps and a Claimed / Unclaimed status.

#### `[6]` Complaint Menu
Submit a new complaint under one of four categories: **Electricity, Plumbing, Internet, or Cleaning**. The **ComplaintPolicy engine** automatically evaluates the description for emergency keywords (fire, smoke, flood, burst pipe, electric shock, etc.) and assigns an appropriate priority level. Complaints flow through a four-stage lifecycle:

```
SUBMITTED → ASSIGNED → IN_PROGRESS → RESOLVED
```

Previously submitted complaints can be tracked by ID and followed up on. Emergency-flagged complaints are elevated to `EMERGENCY` priority automatically.

#### `[7]` Weekly Routine
View and manage your personal weekly schedule — class timings, activities, or any custom entries. Attendants also have read access to student routines.

#### `[8]` View Announcements
Read official notices posted by the Hall Office or Attendants — event announcements, policy updates, rule changes, and general hall notices, ordered by most recent.

#### `[9]` Store Shopping Cart
Browse available items in the dormitory store, add them to a cart, review the order, and confirm the purchase. Confirmed orders are billed to the student's store account and sent to the Store-in-Charge for processing.

#### `[10]` Emergency Contacts
View the dormitory's official emergency contacts — on-duty warden, campus medical, security desk — and manage your own personal emergency contact information.

#### `[11]` Edit Profile
Update contact details (phone number) or change the login password. Password change requires the current password to be verified first.

---

### Step 4 — Attendant Dashboard

![Attendant Dashboard](assets/attendant-dashboard.png)

The **Attendant Dashboard** is themed in deep **teal/green**. Attendants are the operational backbone of the dormitory and have 7 management options:

#### `[1]` Handle Student Complaints
View all complaints submitted by students. The attendant can update a complaint's status, add resolution notes, and assign it to the appropriate maintenance worker based on the complaint category. The system maps categories to worker specializations: Electricity → Electrician, Plumbing → Plumber, Internet → Internet Tech, Cleaning → Cleaning.

#### `[2]` Handle Worker Schedule
Assign and manage the weekly shifts of maintenance workers and cleaning staff — view current assignments and make adjustments.

#### `[3]` Add Found Items
Log newly found items into the Lost & Found system — description, location found, date, and status — so students can search and claim them.

#### `[4]` View Student Routine
Browse the weekly routines of all students — useful for planning hall inspections or scheduling activities without disrupting class timetables.

#### `[5]` Announcements
Create and publish new announcements to the student-facing notice board, edit or retract existing notices, and archive outdated ones.

#### `[6]` Manage Emergency Contacts
Add, update, or remove the official emergency contacts that appear on every student's Emergency Contacts screen — wardens, medical staff, campus security.

#### `[7]` Edit Profile
Update personal information or change the login password.

---

### Step 5 — Store-in-Charge Dashboard

![Store Dashboard](assets/store-dashboard.png)

The **Store-in-Charge Dashboard** is themed in **warm brown / amber**. The store manager has 4 focused options:

#### `[1]` Inventory Management
View the full catalog of store items with current stock levels. Add new products, restock existing items, update prices, or flag items as out of stock. Items are stored with an `itemId`, `name`, `quantity`, and `price`.

#### `[2]` Process Purchase
Review and fulfill pending student purchase orders. Confirm orders, deduct quantities from inventory, and bill the student's store account. Orders can be rejected with a reason if stock is unavailable.

#### `[3]` Sales Summary & Reports
View aggregated store activity — daily and weekly totals, most purchased items, revenue summaries, and low-stock alerts for restocking decisions.

#### `[4]` Edit Profile
Update personal details or change the login password.

---

### Step 6 — Maintenance Worker Dashboard

The **Maintenance Worker Dashboard** is themed in **steel blue/gray**, reflecting the technical nature of the role. Workers have 4 options:

#### `[1]` View Work Field
Displays the worker's assigned specialization and a description of their responsibilities:
- **Electrician** — Electrical faults, wiring, and power issues
- **Plumber** — Plumbing, water leaks, and pipe issues
- **Internet Tech** — Connectivity and network issues
- **Cleaning** — Cleaning and sanitation of dorm facilities

#### `[2]` View Task
View all complaints that have been assigned to this worker, with their priority level, category, student name, room number, and current status. Workers can update complaint status to `IN_PROGRESS` or `RESOLVED`.

#### `[3]` View Routine
View the worker's assigned visit schedule for the dormitory.

#### `[4]` Edit Profile
Update personal details or change the login password.

---

### Step 7 — Hall Office Dashboard

The **Hall Office Dashboard** is themed in **hot pink / magenta**. The Hall Officer oversees room management and overall dormitory administration:

#### `[1]` Update Student Hall Room Info
A sub-menu with three options:
- **Live Preview Available Rooms** — real-time view of all rooms with occupancy status (`AVAILABLE` / `FULL`)
- **Allocate / Change Student Room** — look up a student by ID, see their current room, and assign a new one
- **Review Room Change Applications** — view, approve, or reject formal room change requests submitted by students, with status transitions: `PENDING → COMPLETED / REJECTED`

#### `[2]` View Student Complaints
View and filter all open complaints submitted by students across the hall.

#### `[3]` View Worker Schedule
View current maintenance worker assignments and schedules.

#### `[4]` Handle Attendant Task
Manage attendant-level operational tasks from the officer view.

#### `[5]` Edit Profile
Update personal details or change the login password.

---

### Step 8 — Admin Dashboard

The **Admin Dashboard** is themed in **deep red**, signaling elevated access. The Admin has 5 options for full system control:

#### `[1]` Create Account
Create new accounts for any role — Student, Attendant, Maintenance Worker, Store-in-Charge, Hall Officer, or Cafeteria Manager. Input is validated with custom exceptions:
- `InvalidEmailException` — malformed email address
- `InvalidPhoneException` — invalid phone number format
- `InvalidDepartmentException` — unrecognized department
- `InvalidPasswordException` — password does not meet requirements
- `UserAlreadyExistsException` — ID already registered

#### `[2]` Delete Account
Remove a user account from the system by ID, with confirmation.

#### `[3]` View & Search Accounts
Browse all registered users across roles, or search by name or ID.

#### `[4]` Manage Rooms
Add, edit, or remove room records — set capacity and track occupancy across all rooms in the hall.

#### `[5]` Edit Profile
Update admin personal details or change the login password.

---

### Step 9 — Cafeteria Manager Dashboard

The **Cafeteria Manager Dashboard** is themed in **golden yellow** and includes a live, animated **meal slot progress bar** that updates in real time to show the current meal window and remaining time. A header line shows the current simulated date, time, active meal slot, and Ramadan mode status.

#### `[1]` Update Weekly Menu
Set or update the weekly meal menu for each day and meal type. Menus are stored per day, per meal type, and respect the Ramadan/normal mode distinction.

#### `[2]` Schedule Special Event
Plan and announce special cafeteria events or custom meal days outside the standard weekly menu.

#### `[3]` Verify Student Token
Manually verify a meal token by token ID — confirms whether it is `ACTIVE`, `USED`, or `EXPIRED`.

#### `[4]` Toggle Ramadan Mode
Switch the entire food system between **normal mode** and **Ramadan mode** with one toggle. This change is persisted to `data/foods/config.txt` and affects meal times, token purchase windows, and the menu system for all users immediately.

#### `[5]` Edit Profile
Update personal details or change the login password.

---

## 🎭 User Roles

```
┌──────────────────────────────────────────────────────────┐
│           WELCOME TO IUT FEMALE DORMITORY                │
│               Select your role to continue               │
├──────────────────────────────────────────────────────────┤
│  [1] Student              [5] Hall Office                │
│  [2] Attendant            [6] Admin                      │
│  [3] Maintenance Worker   [7] Cafeteria Manager          │
│  [4] Store In Charge      [0] Exit                       │
└──────────────────────────────────────────────────────────┘
```

| Role | Theme | Options | Primary Focus |
|------|-------|:-------:|---------------|
| Student | Navy Blue | 11 | Room, meals, booking, complaints, store |
| Attendant | Deep Teal | 7 | Complaints, scheduling, announcements |
| Maintenance Worker | Steel Blue / Gray | 4 | Repair and maintenance task resolution |
| Store-in-Charge | Warm Brown / Amber | 4 | Inventory, orders, sales reports |
| Hall Office | Hot Pink / Magenta | 5 | Room allocation, hall administration |
| Admin | Deep Red | 5 | Full system control, account management |
| Cafeteria Manager | Golden Yellow | 5 | Weekly menus, tokens, Ramadan mode |

---

## ✨ Features

### Core Modules

| Module | Description |
|--------|-------------|
| 🔐 **Authentication** | Role-based login with JLine masked input and hashed passwords |
| 🏠 **Room Management** | Allocation, room-change applications, live availability view |
| 📅 **Facility Booking** | Laundry (timed slots), Study Room, Fridge (first-fit) |
| 🍽️ **Food System** | Weekly menus, time-locked tokens, Ramadan mode |
| 🏪 **Store System** | Inventory, shopping cart, billing, dues tracking |
| 🔍 **Lost & Found** | Report, search, and claim items |
| 📢 **Complaints** | Smart complaint routing with emergency detection |
| 📋 **Routines** | View and manage weekly schedules |
| 📣 **Announcements** | Post and read hall-wide notices |
| ☎️ **Emergency Contacts** | Dorm-wide and personal emergency info |

### Custom-Built Libraries

A point of pride in this project — **no `java.util` data structures** were used. All collection and utility needs are served by custom-built libraries:

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

### Smart Complaint Engine

The `ComplaintPolicy` class evaluates every complaint description against a list of emergency keywords before assigning priority:

```
Fire · Smoke · Burning · Sparks · Electric Shock
Flood · Burst Pipe · Overflow · Danger · Panic
```

If any keyword matches, the complaint is automatically elevated to `EMERGENCY` priority, regardless of category. Otherwise, priority defaults to `NORMAL` for all categories.

Worker field routing is automatic:

```
ELECTRICITY  →  ELECTRICIAN
PLUMBING     →  PLUMBER
INTERNET     →  INTERNET_TECH
CLEANING     →  CLEANING
```

### Time & Demo Mode

The `TimeManager` class supports three modes:

- **Demo Mode** (default on startup) — 20 real-world minutes simulate a full 15-hour day (07:00–22:00), so meal slot transitions happen quickly during presentations
- **Real Mode** — uses actual system time
- **Ramadan Mode** — replaces normal meal times with Suhoor, Iftar, and Dinner windows; persisted to disk across restarts

### UI Highlights

- 🎨 **8 distinct ANSI true-color themes** — one per role, applied down to the background fill
- 🌧️ **Matrix rain animation** on first login to each role's dashboard
- 📊 **Live animated progress bar** in the Cafeteria Manager dashboard showing current meal slot
- ⌨️ **Arrow key + number navigation** throughout all menus
- 🔆 **Lilac highlight bar** on the currently selected menu item
- 🖥️ **Dynamic terminal sizing** — layout adapts to your window dimensions

---

## ⚙️ Installation

### Prerequisites

| Requirement | Version | Link |
|-------------|---------|------|
| Java JDK | 17 or higher | [Download](https://www.oracle.com/java/technologies/downloads/) |
| WezTerm | Latest stable | [Download](https://wezfurlong.org/wezterm/) |

> ⚠️ Standard **Windows Command Prompt** and **PowerShell** are not supported. The UI depends on ANSI true-color escape codes and dynamic terminal size detection. Use **WezTerm** or Windows Terminal with VT processing enabled.

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/tayma-06/Dormatrix.git
cd Dormatrix

# 2. First-time build and run
setup.bat

# 3. Subsequent runs
run.bat
```

### Default Admin Credentials

```
User ID  :  admin
Password :  admin123
```

> ⚠️ Change the admin password after first login in any real deployment. Credentials are loaded from `config/admin.config` and hashed on first write to disk.

---

## 📁 Project Structure

```
dormatrix/
├── src/
│   ├── Dormatrix.java                  Entry point — initializes TimeManager, launches MainDashboard
│   ├── cli/                            View layer (all CLI screens)
│   │   ├── announcement/               Announcement board screens
│   │   ├── complaint/                  Complaint screens per role
│   │   ├── contacts/                   Emergency contacts screens
│   │   ├── dashboard/                  Role-specific dashboard menus
│   │   │   ├── food/                   Cafeteria service screens
│   │   │   └── room/                   Room management screens
│   │   ├── forms/                      Data entry forms (accounts, complaints, food)
│   │   ├── profile/                    Edit profile screen
│   │   ├── routine/                    Routine screens
│   │   ├── schedule/                   Worker schedule screens
│   │   └── views/                      Read-only display screens (store, food, room, etc.)
│   ├── controllers/                    Business logic (MVC controllers)
│   │   ├── account/                    CRUD for user accounts
│   │   ├── announcement/
│   │   ├── authentication/             AuthController, AccountManager, ConfigLoader
│   │   ├── balance/                    Balance and dues logic
│   │   ├── complaint/                  Per-role complaint handling
│   │   ├── contacts/                   Emergency contact management
│   │   ├── dashboard/                  One controller per dashboard role
│   │   ├── facilities/                 Laundry, StudyRoom, Fridge controllers
│   │   ├── food/                       Cafeteria, menu, token controllers
│   │   ├── miscellaneous/              Lost & Found
│   │   ├── profile/                    Profile update logic
│   │   ├── room/                       Room allocation, room change logic
│   │   ├── routine/                    Routine management
│   │   ├── schedule/                   Worker visit scheduling
│   │   └── store/                      Inventory, purchase, sales, dues
│   ├── exceptions/                     Custom exception classes
│   │   ├── account/                    InvalidEmail/Phone/Dept/Password, UserAlreadyExists
│   │   ├── config/                     ConfigurationLoadException
│   │   ├── food/                       InvalidTokenException
│   │   ├── InsufficientInventoryException
│   │   ├── InvalidChoiceException
│   │   └── SlotUnavailableException
│   ├── libraries/                      Custom data structures and utilities
│   │   ├── collections/                MyArrayList, MyString, MyOptional
│   │   ├── file/                       FilePaths, TextFile
│   │   ├── hashing/                    HashFunction (DJB2 + XOR)
│   │   ├── logs/                       Logger
│   │   └── slots/                      SlotAllocator, FirstFitAllocator
│   ├── models/                         Domain entities
│   │   ├── announcements/              Announcement
│   │   ├── complaints/                 Complaint, ComplaintPolicy, ComplaintIdGenerator
│   │   ├── contacts/                   EmergencyContactEntry
│   │   ├── enums/                      ComplaintCategory, ComplaintStatus, PriorityLevel,
│   │   │                               RoomChangeApplicationStatus, WorkerField
│   │   ├── facilities/                 LaundrySlot, StudyRoomSeat, FridgeSlot
│   │   ├── food/                       DailyMenu, MealToken, MealType, TokenStatus
│   │   ├── miscellaneous/              LostItem, FoundItem
│   │   ├── room/                       Room, RoomChangeApplication
│   │   ├── routine/                    RoutineEntry, StudentRoutineEntry
│   │   ├── schedule/                   WorkerVisitEntry
│   │   ├── store/                      Item, CartItem, ShoppingCart, DueRecord,
│   │   │                               SaleRecord, StudentBalance
│   │   └── users/                      User (abstract), Student, HallAttendant,
│   │                                   MaintenanceWorker, StoreInCharge, HallOfficer,
│   │                                   CafeteriaManager, SystemAdmin, StudentPublicInfo
│   ├── module/complaint/               ComplaintModule, ComplaintService
│   ├── repo/                           Repository interfaces
│   │   └── file/                       File-based implementations
│   ├── tests/                          TerminalUITest, UnitTests
│   ├── themes.json                     Theme color definitions
│   └── utils/                          Terminal helpers
│       ├── BackgroundFiller.java       8 role-specific color themes
│       ├── CafeteriaAsciiUI.java       Animated meal slot progress bar
│       ├── ConsoleColors.java          ANSI color constants
│       ├── ConsoleUtil.java            Screen clearing, pause
│       ├── FastInput.java              Fast terminal input reading
│       ├── FeaturePaths.java           Data file path constants
│       ├── InputHelper.java            Input validation helpers
│       ├── RoleMapper.java             Role string normalization
│       ├── TerminalUI.java             Core UI drawing (boxes, dashboards, menus)
│       ├── TerminalUIExtras.java       Matrix rain animation, extras
│       └── TimeManager.java           Real/demo/Ramadan mode time management
├── data/                               Persistent flat-file data storage
│   ├── users/                          One .txt file per role
│   ├── complaints/
│   ├── routines/
│   ├── schedules/
│   ├── announcements/
│   ├── contacts/
│   ├── facility/
│   └── foods/
├── config/                             System configuration (admin credentials)
├── lib/                                External JAR dependencies (JLine)
├── assets/                             Screenshots and media for README
├── setup.bat                           Build + run (first time)
└── run.bat                             Run only
```

---

## 🏗️ Architecture

Dormatrix uses a clean **MVC + Repository** layered architecture:

```
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

**Key design decisions:**
- **No `java.util`** — custom `MyArrayList`, `MyString`, and `MyOptional` used throughout
- **File-based storage** — all data lives in structured `|`-delimited flat files under `data/`
- **Role-aware routing** — login determines which controller stack and dashboard loads
- **Hashed credentials** — DJB2 + XOR hash, never stored in plain text
- **First-Fit slot allocation** — `FirstFitAllocator` handles fridge slot assignment
- **Policy-driven complaints** — `ComplaintPolicy` applies emergency detection and auto-routing without any manual intervention
- **Demo mode time simulation** — 20 real minutes = 15-hour day for presentation demos

---

## 💾 Data Storage

All data is stored as structured plain-text flat files. No external database is required.

| File | Contents |
|------|----------|
| `data/users/students.txt` | `ID\|Name\|STUDENT\|Dept\|Hash\|Phone\|Email\|Room` |
| `data/users/admin.txt` | Admin credentials |
| `data/users/hall_attendants.txt` | Attendant records |
| `data/users/maintenance_workers.txt` | Worker records with field specialization |
| `data/users/store_in_charges.txt` | Store-in-Charge records |
| `data/users/hall_officers.txt` | Hall Officer records |
| `data/users/cafeteria_managers.txt` | Cafeteria Manager records |
| `data/complaints/complaints.txt` | All complaints with status, priority, tags |
| `data/routines/student_routines.txt` | Weekly routine entries per student |
| `data/schedules/worker_visits.txt` | Maintenance worker visit schedule |
| `data/announcements/announcements.txt` | Hall announcement records |
| `data/contacts/emergency_contacts.txt` | Emergency contact entries |
| `data/facility/laundrySlots.txt` | Current laundry booking state (`slotIndex,studentId`) |
| `data/foods/config.txt` | Ramadan mode toggle (`RAMADAN=true/false`) |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17+ |
| UI Rendering | Custom ANSI True-Color escape code renderer |
| Password Input | JLine3 (masked input with `*` echo) |
| Data Persistence | Structured plain-text flat files |
| Data Structures | Custom (`MyArrayList`, `MyString`, `MyOptional`) |
| Architecture | MVC + Repository Pattern |
| Terminal | WezTerm (recommended) |
| Build | Windows Batch scripts (`setup.bat`, `run.bat`) |

---

## 🤝 Contributing

Contributions, bug reports, and suggestions are welcome.

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -m "Add: description of change"`
4. Push to your branch: `git push origin feature/my-feature`
5. Open a Pull Request

### Bug Reports

Please include:
- Steps to reproduce
- Expected vs. actual behavior
- Java version (`java -version`) and terminal emulator used
- Screenshot or terminal output if applicable

---

## 📄 License

This project is licensed under the **MIT License** — see [LICENSE](LICENSE) for details.

---

## 👥 Team

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

Made with ❤️ at IUT · CSE Department

⭐ Star this repo if Dormatrix helped you!

**[GitHub Repository](https://github.com/tayma-06/Dormatrix)**

</div>