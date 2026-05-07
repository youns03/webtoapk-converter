import argparse
import os
import shutil
import xml.etree.ElementTree as ET
import re

def update_android_manifest(project_path, app_name, package_name, icon_path=None, splash_screen_path=None):
    manifest_path = os.path.join(project_path, 'android', 'app', 'src', 'main', 'AndroidManifest.xml')
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
            # Assuming icon_path is a path to a drawable resource, e.g., @mipmap/my_custom_icon
            application_tag.set('{http://schemas.android.com/apk/res/android}icon', icon_path)
            application_tag.set('{http://schemas.android.com/apk/res/android}roundIcon', icon_path)

    tree.write(manifest_path, encoding='utf-8', xml_declaration=True)

def update_strings_xml(project_path, app_name):
    strings_path = os.path.join(project_path, 'android', 'app', 'src', 'main', 'res', 'values', 'strings.xml')
    tree = ET.parse(strings_path)
    root = tree.getroot()

    app_name_tag = root.find("string[@name='app_name']")
    if app_name_tag is not None:
        app_name_tag.text = app_name
    else:
        # Add if not exists
        new_string = ET.SubElement(root, 'string', name='app_name')
        new_string.text = app_name

    tree.write(strings_path, encoding='utf-8', xml_declaration=True)

def update_gradle_kts(project_path, package_name):
    gradle_path = os.path.join(project_path, 'android', 'app', 'build.gradle.kts')
    with open(gradle_path, 'r') as f:
        content = f.read()

    # Update namespace
    content = re.sub(r'namespace = ".*"', f'namespace = "{package_name}"', content)
    # Update applicationId
    content = re.sub(r'applicationId = ".*"', f'applicationId = "{package_name}"', content)

    with open(gradle_path, 'w') as f:
        f.write(content)

def build_apk(project_path):
    # This function would trigger the gradle build process
    # For now, it's a placeholder. Actual implementation will be in CI/CD.
    print(f"Building APK for project at {project_path}...")
    # Example: os.system(f"cd {project_path}/android && ./gradlew assembleRelease")

def main():
    parser = argparse.ArgumentParser(description='Convert web app to Android APK.')
    parser.add_argument('--project_path', required=True, help='Path to the Android project.')
    parser.add_argument('--app_name', required=True, help='Name of the application.')
    parser.add_argument('--package_name', required=True, help='Package name for the Android application (e.g., com.example.myapp).')
    parser.add_argument('--icon_path', help='Path to the icon drawable resource (e.g., @mipmap/my_custom_icon).')
    parser.add_argument('--splash_screen_path', help='Path to the splash screen drawable resource.')
    parser.add_argument('--web_files_path', required=True, help='Path to the web files (HTML, CSS, JS).')

    args = parser.parse_args()

    # Copy web files to assets
    assets_path = os.path.join(args.project_path, 'android', 'app', 'src', 'main', 'assets', 'www')
    if os.path.exists(assets_path):
        shutil.rmtree(assets_path)
    shutil.copytree(args.web_files_path, assets_path)

    update_android_manifest(args.project_path, args.app_name, args.package_name, args.icon_path, args.splash_screen_path)
    update_strings_xml(args.project_path, args.app_name)
    update_gradle_kts(args.project_path, args.package_name)
    build_apk(args.project_path)

    print("Project configuration updated successfully.")

if __name__ == '__main__':
    main()
