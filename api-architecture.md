# Community Care Eligibility: API Architecture

## Overview
The Community Care Eligibility API allows consumers to look at _objective_ information (e.g., VA health coverage eligibility, drive/wait times to health services, etc.) to determine whether or not a Veteran is a candidate to utilize Community Care Providers for a given medical service.

## Architecture
```
                   +
                   |
                   v
+------------------+------------------+
|                REST                 |
+-------------------------------------+
|                                     |
|      Community Care Eligibility     |
|                API                  |
|                                     |
+------------------+------------------+
                   |
                   |
                   |
          +--------+---------+
          |                  |
          |                  |
          v                  v
+---------+-------+ +--------+--------+
|      SOAP       | |      REST       |
+-----------------+ +-----------------+
|                 | |                 |
| Eligibility and | |   Facilities    |
|   Enrollment    | |      API        |
|                 | |                 |
+---------+-------+ +--------+--------+
          |                  |
          |                  |
          v                  v
         ???                ???
```
