import os
import shutil
import re

old_pkg = "com.webscare.urdufonts"
new_pkg = "com.urdufonts.app"
root_dir = r"d:\UrduFonts\app"

print(f"Starting package rename from {old_pkg} to {new_pkg}...")

# 1. Update text content in all relevant files
extensions = (".kt", ".xml", ".kts", ".pro")
for dirpath, _, filenames in os.walk(root_dir):
    for f in filenames:
        if f.endswith(extensions):
            filepath = os.path.join(dirpath, f)
            try:
                with open(filepath, "r", encoding="utf-8") as file:
                    content = file.read()
                if old_pkg in content:
                    new_content = content.replace(old_pkg, new_pkg)
                    with open(filepath, "w", encoding="utf-8") as file:
                        file.write(new_content)
                    print(f"Updated content in: {f}")
            except Exception as e:
                print(f"Error processing {filepath}: {e}")

# 2. Move source directories
moves = [
    (r"d:\UrduFonts\app\src\main\java\com\webscare\urdufonts", r"d:\UrduFonts\app\src\main\java\com\urdufonts\app"),
    (r"d:\UrduFonts\app\src\test\java\com\webscare\urdufonts", r"d:\UrduFonts\app\src\test\java\com\urdufonts\app"),
    (r"d:\UrduFonts\app\src\androidTest\java\com\webscare\urdufonts", r"d:\UrduFonts\app\src\androidTest\java\com\urdufonts\app"),
]

for old_dir, new_dir in moves:
    if os.path.exists(old_dir):
        os.makedirs(os.path.dirname(new_dir), exist_ok=True)
        shutil.move(old_dir, new_dir)
        print(f"Moved directory: {old_dir} -> {new_dir}")
        parent_webscare = os.path.dirname(old_dir)
        if os.path.exists(parent_webscare) and not os.listdir(parent_webscare):
            os.rmdir(parent_webscare)
            print(f"Cleaned up empty directory: {parent_webscare}")

print("Package rename completed 100% successfully!")
