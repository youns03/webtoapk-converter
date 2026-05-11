# WebToAPK Converter

![Build](https://img.shields.io/github/actions/workflow/status/youns03/webtoapk-converter/build-apk.yml?branch=main)
![License](https://img.shields.io/github/license/youns03/webtoapk-converter)
![Stars](https://img.shields.io/github/stars/youns03/webtoapk-converter)

**WebToAPK Converter** is a powerful automation tool that transforms any website or web application (HTML/ZIP/URL) into a native Android APK. It leverages GitHub Actions and a customizable Android WebView template to simplify the process of creating Android apps from web content.

## 🚀 Quick Start: Get Your APK in 5 Steps!

1.  **Fork this repository:** Click the "Fork" button at the top right of this page.
2.  **Go to Actions:** Navigate to the "Actions" tab in your forked repository.
3.  **Run Workflow:** Select the `Build Android APK` workflow and click "Run workflow".
4.  **Provide Details:** Enter your website URL (or upload a ZIP file), desired app name, and package name.
5.  **Download APK:** Once the workflow completes, download the `app-debug.apk` from the workflow artifacts.

## ⚙️ How It Works: Web to Android in a Nutshell

This project converts your web content into an Android application using a Python CLI tool that configures a WebView-based Android template. The entire process is automated via GitHub Actions.

```mermaid
graph TD
    A[Web App (URL / ZIP)] --> B(GitHub Actions)
    B --> C(Python CLI: cli/webtoapk_cli.py)
    C --> D(Android WebView Template)
    D --> E[Generated Android APK]
```

## 💡 Real-World Example

**Input:** A simple HTML page (e.g., `index.html` with basic content).

**Output:** A fully functional Android APK that displays the HTML page within a native WebView, ready for installation on any Android device.

## 🛠️ CLI Documentation (`cli/webtoapk_cli.py`)

This Python command-line interface (CLI) tool automates the configuration of the Android project based on your web application and desired settings.

### Usage

```bash
python cli/webtoapk_cli.py \
  --project_path <path_to_android_template_copy> \
  --app_name "Your App Name" \
  --package_name "com.yourcompany.yourapp" \
  --web_files_path <path_to_your_web_files> \
  --web_url <your_website_url> \
  --icon_path "@mipmap/my_custom_icon" \
  --splash_screen_path "@drawable/my_splash_screen"
```

### Arguments

*   `--project_path` (required): Path to the copied Android project template (e.g., `android-app`).
*   `--app_name` (required): The desired name for your Android application.
*   `--package_name` (required): The unique package name for your Android application (e.g., `com.example.myapp`).
*   `--web_files_path` (optional): Path to the directory containing your web application's HTML, CSS, and JS files. (Mutually exclusive with `--web_url`)
*   `--web_url` (optional): The URL of your web application. The content will be loaded directly in the WebView. (Mutually exclusive with `--web_files_path`)
*   `--icon_path` (optional): Android drawable resource path for the app icon (e.g., `@mipmap/ic_launcher`).
*   `--splash_screen_path` (optional): Android drawable resource path for the splash screen.

## 📂 Project Structure

```
/
├── android-template/           # The base Android project template
├── cli/                        # Python CLI tool for project configuration
│   └── webtoapk_cli.py
├── .github/workflows/          # GitHub Actions workflows
│   └── build-apk.yml
├── assets/                     # Placeholder for additional assets (e.g., custom icons, splash screens)
├── docs/                       # Project documentation (future use)
├── LICENSE                     # MIT License file
├── TODO.md                     # Project roadmap and future tasks
└── README.md                   # This documentation file
```

## ✨ Features at a Glance

*   **Web to APK Conversion:** Convert any web content (URL or ZIP) into an Android APK.
*   **Automated Builds:** Seamless CI/CD with GitHub Actions for generating APKs.
*   **Customizable Branding:** Easily change app name, icon, and package name.
*   **Enhanced WebView:** Optimized for performance, offline caching, and splash screen support.

## 💻 Tech Stack

*   **Kotlin:** For the native Android WebView wrapper.
*   **Python:** For the CLI automation and project configuration.
*   **GitHub Actions:** For continuous integration and deployment.
*   **Gradle:** The build automation system for Android.

---

**Manus AI**

**Last Updated:** May 08, 2026
