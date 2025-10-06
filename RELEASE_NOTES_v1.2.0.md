# 🎉 H PDF Creator - v1.2.0 Release Notes

## 🚀 What's New

### 🔢 Page Numbering Feature (NEW!)

Add professional page numbers to your PDFs with complete customization:

- **6 Position Options**: Choose from Header or Footer, with Left, Center, or Right alignment
- **3 Numbering Formats**: 
  - Simple numbers (1, 2, 3...)
  - Styled format ("Page 1", "Page 2"...)
  - Complete format ("1 of 10", "2 of 10"...)
- **Customizable Settings**:
  - Font size from 8 to 20 points
  - Custom start number (start from any number)
  - Show/hide on first page option
- **Smart Features**:
  - Numbers automatically update when pages are reordered
  - Always rendered on top of content for visibility
  - Bold font (HELVETICA_BOLD) for clarity

### 💧 Watermark Feature (NEW!)

Protect your documents with customizable watermarks:

- **Custom Text**: Add any text up to 3 lines (e.g., "CONFIDENTIAL", "DRAFT", company name)
- **7 Color Options**: 
  - Black
  - Gray
  - Red
  - Blue
  - Green
  - Orange
  - Purple
- **Full Customization**:
  - Font size: 20 to 100 points
  - Opacity: 10% to 100% (transparency control)
  - Rotation: -90° to +90° (diagonal watermarks)
- **Live Preview**: See your watermark before applying
- **Top Layer**: Always rendered above all content for maximum visibility

### 🎨 macOS-style Dock Bar

Beautiful floating toolbar for quick access to editing features:

- **Modern Design**: Floating dock with elevation shadow and rounded corners
- **Two Quick Tools**:
  - "123" button for Page Numbering
  - "©" button for Watermark
- **Visual Feedback**: 
  - Active state with primary color when feature is applied
  - Smooth animations and hover effects
  - Semi-transparent background (95% opacity)
- **Smart Positioning**: 
  - Always visible at the bottom center
  - Doesn't interfere with page scrolling
  - 120dp bottom padding to prevent overlap

## 📝 Key Improvements

### Enhanced PDF Editing Experience

- **Better File Handling**: 
  - Automatic file overwrite with safe temporary file creation
  - Improved error messages in Arabic
  - File existence checks before operations
- **Accurate Text Rendering**: 
  - Fixed text width calculations (font units to pixels)
  - Proper rotation matrix for angled text
  - Color and opacity control with PdfExtGState
- **Auto-save Removed**: Manual save only for better control
- **Toast Notifications**: Success messages with file names
- **Navigation**: Auto-return to main screen after save

### UI/UX Enhancements

- **Dock Bar Design**: macOS-inspired floating toolbar
- **Color Picker**: Custom circular color selector with labels
- **Live Previews**: See watermarks before applying
- **Settings Screens**: Beautiful, organized settings with sliders
- **Material Design 3**: Consistent design across all new screens

## 🐛 Bug Fixes

### Fixed: FileProvider Authority Mismatch
- **Issue**: Share, Open, and Print buttons failing due to authority mismatch
- **Fix**: Corrected FileProvider authority from `.file provider` to `.fileprovider`
- **Impact**: All sharing and printing features now work correctly

### Fixed: Text Width Calculation
- **Issue**: Page numbers and watermarks positioned incorrectly
- **Fix**: Proper conversion from font units to pixels (`getWidth() / 1000f * fontSize`)
- **Impact**: Text is now accurately centered and positioned

### Fixed: File Deletion Handling
- **Issue**: App crashes when trying to edit deleted PDFs
- **Fix**: Added file existence checks and graceful error handling
- **Impact**: App handles deleted files gracefully with user-friendly messages

### Fixed: Language Change Restart
- **Issue**: App only closed instead of restarting after language change
- **Fix**: Proper restart with `FLAG_ACTIVITY_NEW_TASK` and `FLAG_ACTIVITY_CLEAR_TASK`
- **Impact**: Language changes now properly restart the app

### Fixed: Rotation Double-Application
- **Issue**: Rotations were applied twice (file rotation + UI rotation)
- **Fix**: Calculate and apply only the delta rotation in preview
- **Impact**: Page rotations now work correctly without duplication

## 🎯 Technical Details

### New Activities

1. **PageNumberSettingsActivity.kt** (445 lines)
   - Complete page numbering configuration UI
   - 6 position options with visual radio buttons
   - Font size, start number, and format customization
   - Activity result contract for settings return

2. **WatermarkSettingsActivity.kt** (360 lines)
   - Watermark text input with 3-line support
   - Custom color picker with 7 colors
   - Live preview with rotation effect
   - Opacity and font size sliders

### Updated Files

- **EditPDFActivity.kt** (+200 lines)
  - Added macOS-style Dock Bar component
  - Implemented `addPageNumbersToPDF()` function
  - Implemented `addWatermarkToPDF()` function
  - Enhanced error handling and file management
  - State management for page numbers and watermarks

- **AndroidManifest.xml**
  - Registered PageNumberSettingsActivity
  - Registered WatermarkSettingsActivity
  - Fixed FileProvider authority
  - Added READ_MEDIA_VISUAL_USER_SELECTED permission

- **strings.xml** (+40 strings)
  - Page numbering strings (13 new)
  - Watermark strings (10 new)
  - Color name strings (7 new)
  - Format and position strings

- **MainActivity.kt**
  - Enhanced BroadcastReceiver for deleted files
  - Fixed receiver flags for API 33+
  - Improved file deletion handling

- **PDFViewScreen.kt**
  - Added thumbnail preview (first page)
  - Reload on resume lifecycle event
  - Button reordering: Open, Share, Print, Edit, Create New
  - Deleted file detection

- **MyPDFsActivity.kt**
  - PDF thumbnail previews instead of generic icon
  - Broadcast when PDF is deleted

- **SettingsActivity.kt**
  - Fixed app restart after language change

### Dependencies

```kotlin
// Core
implementation("com.itextpdf:itext7-core:7.2.5")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

// Compose
implementation(platform("androidx.compose:compose-bom:2023.10.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")

// Other
implementation("androidx.activity:activity-compose:1.8.2")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
```

### New Functions

#### EditPDFActivity.kt
```kotlin
// Page Numbering
private suspend fun addPageNumbersToPDF(
    context: Context,
    pdfPath: String,
    settings: PageNumberSettings,
    totalPages: Int
)

private fun calculatePageNumberPosition(
    position: PageNumberPosition,
    pageSize: Rectangle,
    text: String,
    font: PdfFont,
    fontSize: Float
): Pair<Float, Float>

// Watermark
private suspend fun addWatermarkToPDF(
    context: Context,
    pdfPath: String,
    settings: WatermarkSettings
)

// UI Components
@Composable
private fun MacOSDockBar(...)

@Composable
private fun DockBarButton(...)
```

## 📊 Statistics

### Code Metrics

| Metric | v1.1.0 | v1.2.0 | Change |
|--------|--------|--------|--------|
| **Total Lines** | ~3,500 | ~5,000 | +1,500 (43% ↑) |
| **Kotlin Files** | 15 | 17 | +2 |
| **Activities** | 6 | 8 | +2 |
| **Composable Functions** | 20+ | 30+ | +10 |
| **String Resources** | 160+ | 200+ | +40 |
| **Data Classes** | 5 | 7 | +2 |

### Feature Comparison

| Feature | v1.1.0 | v1.2.0 |
|---------|--------|--------|
| Images to PDF | ✅ | ✅ |
| Custom Titles | ✅ | ✅ |
| Share & Print | ✅ | ✅ |
| PDF Editing | ✅ | ✅ |
| File Manager | ✅ | ✅ |
| Page Numbering | ❌ | ✅ NEW |
| Watermark | ❌ | ✅ NEW |
| Dock Bar UI | ❌ | ✅ NEW |
| Multilingual | ✅ | ✅ |

## 🌍 Localization

All new features fully localized in:

- **Arabic (العربية)**: 
  - Complete RTL support
  - All new strings translated
  - Cultural considerations (right-to-left layouts)
  
- **English**: 
  - Professional translations
  - Clear, concise language
  - Consistent terminology

## 📋 Complete Feature List

- ✅ **Images to PDF** - Convert multiple images to a single PDF
- ✅ **Custom Titles** - Add personalized titles to your PDFs
- ✅ **Share & Print** - Easy sharing and printing options
- ✅ **Edit PDF** - Rotate and rearrange pages
- ✅ **Page Numbering** - Add professional page numbers (NEW)
- ✅ **Watermark** - Protect documents with watermarks (NEW)
- ✅ **Dock Bar** - Quick access toolbar (NEW)
- ✅ **File Management** - Browse and manage your PDFs
- ✅ **Multilingual** - Full Arabic and English support
- ✅ **Beautiful UI** - Modern Material Design 3 interface

## 🎯 Use Cases

Perfect for:

- 📄 Converting documents and receipts
- 📸 Creating photo albums in PDF format
- 📝 Organizing scanned documents
- ✏️ Fixing page orientation issues
- 🔄 Reordering PDF pages
- 🔢 Adding page numbers to reports and documents
- 💧 Protecting confidential documents with watermarks
- 🏢 Creating branded documents with company watermarks
- 📁 Managing PDF collections

## 📱 Requirements

- **Android**: 8.0 (API 26) or higher
- **Permissions**: 
  - Storage (for saving PDFs)
  - Media Images (for accessing photos)
  - Media Visual User Selected (Android 14+)
- **Storage**: ~10 MB app size
- **Memory**: ~50-80 MB average usage

## 🚀 Installation

### From GitHub Releases
1. Go to [Releases](https://github.com/helalrules7/PDFCreator/releases/tag/v1.2.0)
2. Download `H-PDF-Creator-v1.2.0.apk`
3. Install on your Android device
4. Grant required permissions

### From Source
```bash
git clone https://github.com/helalrules7/PDFCreator.git
cd PDFCreator
git checkout v1.2.0
./gradlew assembleDebug
```

## 📖 Usage Guide

### How to Add Page Numbers

1. Open any PDF in Edit mode
2. Tap the **"123"** button in the Dock Bar
3. Configure your preferences:
   - Select position (Header/Footer + Left/Center/Right)
   - Choose numbering format
   - Set font size (8-20)
   - Set start number
   - Toggle "Show on first page"
4. Tap **"Apply"**
5. Save your PDF with **Save** button

### How to Add Watermark

1. Open any PDF in Edit mode
2. Tap the **"©"** button in the Dock Bar
3. Configure your watermark:
   - Enter your text (e.g., "CONFIDENTIAL")
   - Select color from 7 options
   - Adjust font size (20-100)
   - Set opacity (10%-100%)
   - Adjust rotation angle (-90° to +90°)
   - Preview in real-time
4. Tap **"Apply"**
5. Save your PDF with **Save** button

## 🔒 Privacy & Security

- ✅ **Completely Offline**: No internet permission required
- ✅ **No Data Collection**: Your files never leave your device
- ✅ **No Analytics**: Zero tracking or telemetry
- ✅ **Local Storage Only**: All PDFs saved locally
- ✅ **Secure Sharing**: Uses FileProvider for safe file sharing

## 🙏 Credits

- **Developer**: Ahmed Helal
- **Email**: me@ahmedhelal.dev
- **GitHub**: [@helalrules7](https://github.com/helalrules7)
- **Package**: com.tsavvy.pdfcreator
- **License**: All rights reserved © 2025

### Special Thanks

- [iTextPDF](https://itextpdf.com/) - PDF manipulation library
- [Material Design 3](https://m3.material.io/) - Design system
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Kotlin](https://kotlinlang.org/) - Programming language

## 🐛 Known Issues

None currently! 🎉

Report any issues on our [GitHub Issues](https://github.com/helalrules7/PDFCreator/issues) page.

## 🗺️ What's Next?

### Planned for v1.3.0
- [ ] Merge multiple PDFs into one
- [ ] Split PDF into multiple files
- [ ] PDF compression
- [ ] Import external PDFs for editing

### Future Releases
- [ ] Text annotations
- [ ] Password protection
- [ ] Cloud backup integration
- [ ] Dark theme
- [ ] Batch processing

## 📞 Support

- 📧 **Email**: me@ahmedhelal.dev
- 🐛 **Bug Reports**: [GitHub Issues](https://github.com/helalrules7/PDFCreator/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/helalrules7/PDFCreator/discussions)
- ⭐ **Star on GitHub**: [PDFCreator Repository](https://github.com/helalrules7/PDFCreator)

---

## 📦 Download

**Latest Release**: [v1.2.0](https://github.com/helalrules7/PDFCreator/releases/tag/v1.2.0)

**Full Changelog**: [v1.1.0...v1.2.0](https://github.com/helalrules7/PDFCreator/compare/v1.1.0...v1.2.0)

---

<div align="center">

**Made with ❤️ by Ahmed Helal**

⭐ **Like this release? Give us a star on GitHub!**

[Download v1.2.0](https://github.com/helalrules7/PDFCreator/releases/tag/v1.2.0) • [View Source](https://github.com/helalrules7/PDFCreator) • [Report Issue](https://github.com/helalrules7/PDFCreator/issues)

</div>

