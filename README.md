# FileForge

A JavaFX desktop toolkit for everyday **document**, **image**, and **text** tasks — convert, edit, merge, split, compress, translate, and more, all in one app with a light/dark theme and a built-in activity log.

> Built with Java 21, JavaFX, Maven, and SQLite.

---

## Download

A portable Windows build (no Java installation required) is available on the
[Releases page](https://github.com/Tot4llyUnknowN/FileForge/releases).

1. Download the `.zip` from the latest release
2. Extract it anywhere
3. Run `FileForge.exe`

---

## Features

### Document Tools
| Tool | Description |
|---|---|
| Document Info | View PDF metadata (title, author, pages, size, encryption status) |
| PDF Merger | Merge multiple PDFs with drag-to-reorder thumbnails |
| Split PDF | Split by clicking pages or typing ranges (e.g. `1-3, 4-6`) |
| Delete Pages | Click pages to mark them, save without them |
| Reorder Pages | Drag page thumbnails into a new order |
| Image to PDF | Combine images into a single PDF |
| PDF to Image | Export every page as PNG |
| DOCX to PDF | Convert and merge Word documents |
| PPTX to PDF | Convert and merge PowerPoint presentations |
| Excel to CSV | Export any sheet of an `.xlsx` file to CSV |
| Lock PDF | Password-protect a PDF with custom permissions |
| Unlock PDF | Remove password protection |

### Image Tools
| Tool | Description |
|---|---|
| JPG ↔ PNG, WEBP → JPG | Format converters (transparency flattened for JPG) |
| Resize | By exact pixels or percentage |
| Compress | Bulk compression to a target size |
| Crop | Draw a selection on the image and save |
| Rotate & Flip | 90° / 180° rotation, horizontal / vertical flip |
| Metadata Viewer | Inspect EXIF and header metadata |

### Text Tools
- Word count, character count
- Title Case, UPPER, lower
- Copy result to clipboard
- **English → Bangla translation** (live HTTP call to the MyMemory API, JSON parsed with Jackson)

### App-level
- **Activity log** on the Home screen backed by SQLite, with full CRUD: entries are created automatically, read on load, notes are editable inline, and rows can be deleted
- **Settings**: theme (Classic Light / Studio Dark), font family, default export path, thread-pool size
- **Responsive layout**: sidebar, card grids, and dashboard columns scale with window size

---

## Tech Stack

| Area | Library / Tool |
|---|---|
| Language / UI | Java 21, JavaFX 21.0.1 (FXML + CSS) |
| Build | Maven (Shade plugin for fat jar) |
| PDF | Apache PDFBox 3.0.3 |
| Office files | Apache POI 5.3.0, documents4j 1.1.12 |
| Images | Java ImageIO, TwelveMonkeys (WEBP), metadata-extractor |
| Database | SQLite via sqlite-jdbc |
| JSON / HTTP | Jackson, `java.net.http.HttpClient` |

---

## OOP & Design Highlights

- **Interface + abstract class + inheritance**: `ImageConverter` → `AbstractImageConverter` → `JpgToPngConverter`, `PngToJpgConverter`, `WebpToJpgConverter`
- **Generics**: `ThumbnailCell<T>` is reused for both `File` and `Integer` (page number) lists
- **Concurrency**: shared, reconfigurable thread pool in `AppExecutor`; long tasks (compression, DOCX/PPTX conversion, translation) run as `javafx.concurrent.Task`
- **Persistence**: `DatabaseManager` (SQLite schema, foreign key `operational_history.user_fk → users.user_id`) and `SettingsEngine` (JSON settings)
- **Responsiveness**: property bindings to scene/container width (`prefWidthProperty().bind(...)`)

---

## Project Structure

```
src/main/java/com/fileforge/
├── Launcher.java, Main.java
├── controller/          # Home, Document, Image, Text, Settings, navigation
│   ├── document/        # PDF / Office tool controllers
│   └── image/           # Image tool controllers
├── database/            # DatabaseManager, SettingsEngine
├── model/               # AppSettings, ActivityLogEntry
└── util/                # Converters, AppExecutor, ActivityLogger, thumbnails
src/main/resources/
├── view/                # FXML screens
└── css/app.css          # Light + dark theme
```

---

## Run From Source

**Requirements:** JDK 21, Maven

```bash
git clone https://github.com/Tot4llyUnknowN/FileForge.git
cd FileForge
mvn javafx:run
```

## Build a Standalone Executable

```bash
mvn clean package
```

Then package with `jpackage` (JDK 21) using the JavaFX jmods, pointing `--input` at a folder that contains **only** `FileForge-1.0-SNAPSHOT.jar` (not the `original-…` jar) and `--main-class com.fileforge.Launcher`. Include `java.sql` and the `javafx.*` modules via `--add-modules`.

---

## Notes & Limitations

- **DOCX to PDF** uses documents4j, which requires **Microsoft Word installed** on the machine (Windows).
- **Translation** uses the free MyMemory API — no key needed, but it is rate-limited and works best with full sentences rather than single words.
- `settings.json` and `fileforge.db` are created relative to where the app runs.

---

## Author

**Md. Imran Faraj** — [@Tot4llyUnknowN](https://github.com/Tot4llyUnknowN)

© 2026 Md. Imran Faraj
