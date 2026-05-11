import argparse
import os
import shutil
import xml.etree.ElementTree as ET
import re

def update_android_manifest(project_path, package_name, icon_path=None):
    manifest_path = os.path.join(project_path, 'app', 'src', 'main', 'AndroidManifest.xml')
    tree = ET.parse(manifest_path)
    root = tree.getroot()

    # Update package name
    root.set('package', package_name)

    application_tag = root.find('application')
    if application_tag is not None:
        # Update app name reference
        application_tag.set('{http://schemas.android.com/apk/res/android}label', '@string/app_name')
        
        # Update icon if provided
        if icon_path:
            application_tag.set('{http://schemas.android.com/apk/res/android}icon', icon_path)
            application_tag.set('{http://schemas.android.com/apk/res/android}roundIcon', icon_path)

    tree.write(manifest_path, encoding='utf-8', xml_declaration=True)

def update_strings_xml(project_path, app_name):
    strings_path = os.path.join(project_path, 'app', 'src', 'main', 'res', 'values', 'strings.xml')
    # Ensure directory exists
    os.makedirs(os.path.dirname(strings_path), exist_ok=True)
    
    if os.path.exists(strings_path):
        tree = ET.parse(strings_path)
        root = tree.getroot()
    else:
        root = ET.Element('resources')
        tree = ET.ElementTree(root)

    app_name_tag = root.find("string[@name='app_name']")
    if app_name_tag is not None:
        app_name_tag.text = app_name
    else:
        new_string = ET.SubElement(root, 'string', name='app_name')
        new_string.text = app_name

    tree.write(strings_path, encoding='utf-8', xml_declaration=True)

def update_gradle_kts(project_path, package_name):
    gradle_path = os.path.join(project_path, 'app', 'build.gradle.kts')
    with open(gradle_path, 'r') as f:
        content = f.read()

    # Update namespace
    content = re.sub(r'namespace = ".*"', f'namespace = "{package_name}"', content)
    # Update applicationId
    content = re.sub(r'applicationId = ".*"', f'applicationId = "{package_name}"', content)

    with open(gradle_path, 'w') as f:
        f.write(content)

def update_main_activity(project_path, web_url=None):
    activity_path = os.path.join(project_path, 'app', 'src', 'main', 'kotlin', 'com', 'webtopack', 'MainActivity.kt')
    with open(activity_path, 'r') as f:
        content = f.read()

    if web_url:
        # Replace the default local URL with the provided web URL
        content = content.replace('webView.loadUrl("file:///android_asset/www/index.html")', f'webView.loadUrl("{web_url}")')
    
    with open(activity_path, 'w') as f:
        f.write(content)

def main():
    parser = argparse.ArgumentParser(description='Convert web app to Android APK.')
    parser.add_argument('--project_path', required=True, help='Path to the Android project copy.')
    parser.add_argument('--app_name', required=True, help='Name of the application.')
    parser.add_argument('--package_name', required=True, help='Package name for the Android application.')
    parser.add_argument('--icon_path', help='Android drawable resource path for the icon.')
    parser.add_argument('--splash_screen_path', help='Android drawable resource path for the splash screen.')
    parser.add_argument("--web_files_path", help="Path to the local web files.")
    parser.add_argument("--web_url", help="URL of the web application.")

    args = parser.parse_args()

    # Copy web files to assets if provided
    assets_path = os.path.join(args.project_path, 'app', 'src', 'main', 'assets', 'www')
    if os.path.exists(assets_path):
        shutil.rmtree(assets_path)
    os.makedirs(assets_path, exist_ok=True)

    if args.web_files_path:
        if os.path.exists(args.web_files_path):
            shutil.copytree(args.web_files_path, assets_path, dirs_exist_ok=True)
            print(f"✅ Local web files copied to {assets_path}")
    
    # Update project files
    update_android_manifest(args.project_path, args.package_name, args.icon_path)
    update_strings_xml(args.project_path, args.app_name)
    update_gradle_kts(args.project_path, args.package_name)
    update_main_activity(args.project_path, args.web_url)

    print("🚀 Project configuration updated successfully.")

if __name__ == '__main__':
    main()
