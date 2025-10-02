# Changelog

All notable changes to H PDF Creator will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2025-10-02

### Added
- **PDF Editing Feature**
  - Rotate individual pages by 90° increments
  - Rearrange pages using up/down controls
  - Live preview of all editing changes
  - Save edited PDF with custom name
  - Support for maintaining original page rotations

- **My Files Activity**
  - Browse all PDFs created with the app
  - Display file information (name, size, date)
  - Quick view button to open PDF viewer
  - Delete functionality with confirmation dialog
  - Empty state with helpful message
  - Files sorted by creation date (newest first)

- **Navigation Enhancements**
  - Added "My Files" menu item (📁)
  - Updated menu icon for Language settings (🌐)
  - Smooth navigation between all screens

- **Documentation Updates**
  - Added Step 5 in Help: Edit PDF feature
  - Added Step 6 in Help: View Files feature
  - New tips for editing and file management
  - Updated About page with 7 features (was 5)
  - New feature descriptions in both languages

- **Localization**
  - 15+ new strings in Arabic
  - 15+ new strings in English
  - Full bilingual support for new features

### Fixed
- **Critical Bug**: Fixed incorrect page selection after reordering
  - Issue: Rotating a page after reordering would affect the wrong page
  - Solution: Changed LazyColumn `key` from index-based to pageNumber-based
  - Impact: Ensures correct page is always edited, regardless of order

### Changed
- Enhanced PDF edit screen UI with Material Design 3
- Improved card layouts for better visual hierarchy
- Updated emoji icons throughout the app
- Better error messages and user feedback

### Technical
- Added `MyPDFsActivity.kt` (401 lines)
- Added `EditPDFActivity.kt` (586 lines)
- Updated `NavigationDrawer.kt` with new callback
- Updated `MainActivity.kt` navigation handling
- Updated `AboutActivity.kt` with new features
- Updated `HelpActivity.kt` with new steps
- Registered activities in `AndroidManifest.xml`

## [1.0.0] - 2025-09-XX

### Added
- Initial release
- Convert multiple images to single PDF
- Custom PDF titles
- Share PDF functionality
- Print PDF support
- Multilingual support (Arabic & English)
- RTL layout support for Arabic
- Material Design 3 UI
- Navigation drawer with menu
- Settings activity for language selection
- About activity with app information
- Help activity with usage guide
- Permission handling for media access
- File provider for secure file sharing
- Beautiful emoji-based UI icons

### Features
1. **Image Selection**
   - Select multiple images from gallery
   - Preview selected images
   - Remove individual images
   - Clear all images

2. **PDF Creation**
   - High-quality PDF generation
   - Custom page titles
   - Automatic image scaling
   - Maintains aspect ratios
   - Progress indicators

3. **Sharing & Printing**
   - Share PDF via any app
   - Print using system print service
   - Open PDF in external viewers

4. **Localization**
   - Complete Arabic translation
   - Complete English translation
   - Dynamic language switching
   - RTL support

5. **Settings**
   - Language selection
   - Persistent preferences
   - Clean Material Design UI

### Technical Details
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **PDF Library**: iTextPDF 7.2.5
- **Design System**: Material Design 3
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

---

## Release Types

- **Major** version (X.0.0): Incompatible API changes
- **Minor** version (0.X.0): New features, backward compatible
- **Patch** version (0.0.X): Bug fixes, backward compatible

## Links

- [GitHub Repository](https://github.com/yourusername/PDFCreator)
- [Issue Tracker](https://github.com/yourusername/PDFCreator/issues)
- [Releases](https://github.com/yourusername/PDFCreator/releases)

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to contribute to this project.

## License

© 2025 H PDF Creator. All rights reserved.

Developer: Ahmed Helal (me@ahmedhelal.dev)

