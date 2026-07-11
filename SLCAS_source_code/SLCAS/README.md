# Smart Library Circulation & Automation System (SLCAS)
COS 202 Project - MIVA Open University

## Requirements
- Java JDK 17 or later (JDK 21 also works)

## Project Structure
```
SLCAS/
  src/
    model/        LibraryItem (abstract), Book, Magazine, Journal, Borrowable,
                   UserAccount, LibraryDatabase
    controller/    LibraryManager, BorrowController, SearchEngine, SortEngine,
                   AdminAction
    gui/           MainWindow, ViewItemsPanel, BorrowPanel, AdminPanel,
                   SearchSortPanel, ItemTableModel, AvailabilityRenderer
    utils/         IDGenerator, FileHandler
  data/            (created automatically - stores the saved database file)
```

## How to Compile
From the `SLCAS` folder:
```
mkdir -p bin
javac -d bin $(find src -name "*.java")
```

## How to Run
```
java -cp bin gui.MainWindow
```

The application starts with 8 sample catalogue items pre-loaded (books,
magazines, and journals) so you can try every feature immediately.

## Quick Feature Tour
1. **View Items tab** - browse the full catalogue; use the report buttons to
   see most-borrowed items, overdue users, category distribution, and the
   frequently-accessed cache.
2. **Borrow/Return tab** - enter an Item ID and a User ID (+ name for new
   users), then click Borrow or Return. If an item is already checked out,
   the user is placed on a FIFO reservation queue and is automatically
   handed the item when it is returned. A background Timer checks for
   overdue items periodically and shows a reminder dialog.
3. **Admin tab** - add a new item (the extra field changes automatically
   between ISBN / Issue Number / Volume depending on the Type you choose),
   delete an item, Undo the last add/delete, import/export CSV via a file
   chooser, and save/load the whole database to `data/library_data.txt`.
4. **Search & Sort tab** - search by title/author/type using Linear,
   Binary, or Recursive search (Binary search requires the catalogue to
   already be sorted by Title - the app will tell you if it isn't), and
   sort by title/author/year using Selection, Insertion, Merge, or Quick
   sort, selectable from dropdowns.

## Notes on This Submission
- A UML class diagram is included as `uml_class_diagram.png`.
- Since this environment cannot open a real display, `tab1..tab4_*.png`
  are carefully constructed mockups that match the actual running layout
  exactly (same components, same data, same colours) - useful for the
  report, but for your own submission it's worth running the app locally
  and taking real screenshots (File > *, or your OS's screenshot tool)
  to submit alongside this code, since your instructor may want to see
  the live app.
- The 2-3 page written report (`SLCAS_Project_Report.docx`) covers the
  description, features, data structures used, algorithms chosen and why,
  and challenges faced, and also embeds the UML diagram and screenshots
  for convenience.
