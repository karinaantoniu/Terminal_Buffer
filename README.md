# Java Terminal Text Buffer

## Overview
This project implements a robust **Terminal Text Buffer** designed to mimic the core behavior of modern terminal emulators. It focuses on solving complex rendering challenges, specifically handling the wide character problem and executing seamless dynamic text reflow upon screen resizing.

## Key Features

* **Dynamic Text Reflow:** A sophisticated resize algorithm that logically reconstructs, slices, and redistributes text. Text dynamically unwraps and re-wraps without losing characters or introducing artificial gaps.
* **Wide Character Support:** Fully supports characters that occupy two logical cells (detected via Unicode code points). Includes boundary safety to prevent word-breaking when a wide character is typed at the absolute edge of the screen.
* **High-Performance Scrollback:** Utilizes a Deque memory structure for the scrollback history, providing O(1) time complexity for both appending new lines and dropping the oldest ones when the memory limit is reached.

## Architectural Decisions & Trade-offs

### 1. From a 2D Matrix to a 1D Array Paradigm
Initially, the screen was modeled as a simple 2D array of cells. While sufficient for a static grid, it made dynamic resizing computationally expensive. The architecture was shifted to encapsulate cell arrays into a custom made class. This allows each row to hold vital metadata, specifically a boolean flag which enables the buffer to reconstruct logical paragraphs during screen resizing.

### 2. Design Patterns Implemented
To ensure a clean, maintainable, and decoupled codebase, two primary design patterns were implemented:
* **The Command Pattern:** Action processing is handled via a Command interface, encapsulating every action into its own discrete class and avoiding massive if else blocks. The main loop acts solely as an invoker.
* **The Observer Pattern:** The TerminalBuffer acts as the Subject, remaining completely oblivious to how the UI is rendered. Whenever a structural change occurs (writing, clearing, resizing), it cleanly triggers the observers.
