# Dormatrix

A Java-based dormitory management system with a command-line interface for managing rooms, facilities, food services, complaints, and more.

## Features

- **Room Management** — View and manage room assignments
- **Facility Booking** — Reserve laundry slots, study rooms, etc.
- **Food System** — Weekly menus, meal tokens, and shopping cart
- **Store & Inventory** — Account balances and dues tracking
- **Lost & Found** — Report and search for lost items
- **Complaints** — Submit and track complaints
- **Routines** — View and manage weekly schedules
- **Announcements** — Read hall announcements

## User Roles

| Role | Description |
|------|-------------|
| Student | Access room info, book facilities, buy meal tokens, file complaints |
| Attendant | Handle complaints, manage routines |
| Maintenance Worker | View and resolve maintenance complaints |
| Store-in-Charge | Manage store inventory and student dues |
| Hall Office | Oversee hall operations and room assignments |
| Cafeteria Manager | Manage food menus and meal schedules |
| Admin | Full system administration |

## Prerequisites

- Java JDK 17 or higher
- [WezTerm](https://wezfurlong.org/wezterm/) (recommended) or any true-color terminal emulator — the UI uses ANSI true-color rendering and dynamic terminal size detection

## Build & Run

Run the following commands in **WezTerm** (or a compatible true-color terminal):

```bash
# Build and run
setup.bat

# Run only (after building)
run.bat
```

## Project Structure

```
src/          — Java source code
data/         — File-based data storage
config/       — Configuration files
lib/          — Dependencies
```

### Source Packages (`src/`)

```
src/
├── cli/                    — Command-line interface layer
│   ├── complaint/          — Complaint CLI handlers
│   ├── components/         — Reusable UI components
│   ├── dashboard/          — Role-based dashboards
│   │   ├── account/
│   │   ├── food/
│   │   └── room/
│   ├── forms/              — Input forms
│   │   ├── account/
│   │   ├── complaint/
│   │   └── food/
│   ├── routine/            — Routine CLI handlers
│   └── views/              — Display views
│       ├── account/
│       ├── complaint/
│       ├── food/
│       ├── room/
│       └── store/
├── controllers/            — Business logic controllers
│   ├── account/
│   ├── authentication/
│   ├── balance/
│   ├── complaint/
│   ├── dashboard/
│   │   ├── account/
│   │   └── room/
│   ├── facilities/
│   ├── food/
│   ├── miscellaneous/
│   ├── room/
│   ├── routine/
│   └── store/
├── exceptions/             — Custom exceptions
│   ├── account/
│   ├── config/
│   └── food/
├── libraries/              — Utility libraries
│   ├── collections/
│   ├── file/
│   ├── hashing/
│   ├── logs/
│   └── slots/
├── models/                 — Data models
│   ├── complaints/
│   ├── enums/
│   ├── facilities/
│   ├── food/
│   ├── miscellaneous/
│   ├── room/
│   ├── routine/
│   ├── store/
│   └── users/
├── module/                 — Feature modules
│   └── complaint/
├── repo/                   — Data repositories
│   └── file/
└── utils/                  — Helper utilities
```

## Default Admin Credentials

- **Username:** admin
- **Password:** admin123

## Team

- [tayma-06](https://github.com/tayma-06)
- [aymannondiny](https://github.com/aymannondiny)
- [SayTas](https://github.com/SayTas)
- [prochetaSilvie](https://github.com/prochetaSilvie)
