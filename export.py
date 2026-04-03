import os

# Extensions to skip
skip_exts = [
    '.class', '.jar', '.war',
    '.log', '.lock', '.tsbuildinfo',
    '.js.map', '.css.map', '.xml', '.json',
    '.png', '.jpg', '.jpeg', '.webp',
    '.gif', '.ico', '.mp3', '.mp4',
    '.ttf', '.otf', '.eot', '.svg',
    '.so', '.bin', '.dex', '.apk', '.aar',
    '.iml', '.db', '.bat', '.sh',
    '.pro', '.gradle', '.keystore'
]

# Directories to skip
skip_dirs = [
    'build', 'bin', 'libs', 'outputs', 'intermediates',
    '.idea', '.git', '.gradle',
    '__pycache__', '.svn', 'captures', '.settings'
]

# Set root directory to Android project root (usually containing 'app/src/main/java')
# Change this path to point to your actual Android project root if needed
root = os.path.abspath(".")

# Output file path
output_file_path = os.path.join(root, "android_code.txt")

# Open the output file once for writing all contents
with open(output_file_path, 'w', encoding='utf-8') as output_file:
    # Walk through the directory tree
    for dirpath, dirnames, filenames in os.walk(root):
        # Skip unwanted directories
        dirnames[:] = [d for d in dirnames if d not in skip_dirs]

        for f in filenames:
            # Process only Java files
            if not f.lower().endswith(".java"):
                continue

            # Skip if it has unwanted extensions (edge cases)
            if any(f.lower().endswith(ext) for ext in skip_exts):
                continue

            filepath = os.path.join(dirpath, f)
            relpath = os.path.relpath(filepath, root)

            try:
                # Read Java file content
                with open(filepath, 'r', encoding='utf-8') as source_file:
                    content = source_file.read()

                    # Write header and content
                    output_file.write(f"\n--- {relpath} ---\n")
                    output_file.write(content)
                    output_file.write("\n")

            except Exception as e:
                print(f"Could not read {relpath}: {e}")

print("✅ All Java source files saved into 'android_code.txt'.")
