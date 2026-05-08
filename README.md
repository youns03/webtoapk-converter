# WebToAPK Converter

Turn any website or web app (HTML/ZIP/URL) into a ready-to-install Android APK.

![Build](https://img.shields.io/github/actions/workflow/status/youns03/webtoapk-converter/build-apk.yml?branch=main)
![License](https://img.shields.io/github/license/youns03/webtoapk-converter)
![Stars](https://img.shields.io/github/stars/youns03/webtoapk-converter)

A powerful automation tool that converts web applications into native Android APKs using WebView, GitHub Actions, and a customizable Android template. This project simplifies Android app creation by converting web applications into native APKs without manual Android development.

## 🚀 Vision

Turn any web app into a native Android APK in minutes using automation.

## 💡 Use Cases

*   Convert landing pages to Android apps.
*   Turn web stores into native APKs.
*   Package HTML/CSS/JS projects into installable Android applications.

## ⚙️ How It Works

```mermaid
graph TD
    A[Input: Web App (URL / ZIP / HTML)] --> B(GitHub Actions)
    B --> C(Python CLI: webtoapk_cli.py)
    C --> D(Android WebView Wrapper)
    D --> E[Output: Generated APK]
```

## ⚡ Quick Start

1.  **Fork the repository:** Click the "Fork" button at the top right of this page.
2.  **Open GitHub Actions:** Navigate to the "Actions" tab in your forked repository.
3.  **Run workflow:** Select the `Build Android APK` workflow and click "Run workflow".
4.  **Enter your details:** Provide your website URL (or upload a ZIP), desired app name, and package name.
5.  **Download generated APK:** Once the workflow completes, download the `app-debug.apk` from the workflow artifacts.

## 🛠️ CLI Documentation (`cli/webtoapk_cli.py`)

This Python command-line interface (CLI) tool automates the process of configuring the Android project based on your web application and desired settings.

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
└── README.md                   # This documentation file
```

## 🚀 Features

*   **Convert any website to APK:** Easily package any web content into an Android application.
*   **Support for HTML / CSS / JS projects:** Works with static web files or dynamic web applications.
*   **GitHub Actions automated build:** Streamlined CI/CD for generating APKs.
*   **Custom Android WebView wrapper:** Optimized WebView for performance and offline capabilities.
*   **Offline caching support:** Enhanced user experience with content available offline.
*   **Customizable App Name, Icon, Package Name:** Full control over app branding.
*   **Splash Screen Support:** Add a professional touch with a custom splash screen.

## 📦 Build Output

*   Android APK (Debug / Release)
*   Ready to install on Android devices

## 💻 Tech Stack

*   **Kotlin:** For the native Android WebView wrapper.
*   **Python:** For the CLI automation and project configuration.
*   **GitHub Actions:** For continuous integration and deployment.
*   **Gradle:** The build automation system for Android.

---

**Manus AI**

**Last Updated:** May 08, 2026
