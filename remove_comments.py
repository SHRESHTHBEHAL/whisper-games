import os
import re

def remove_comments_from_kotlin(content):
    lines = content.split('\n')
    result_lines = []
    in_block_comment = False

    for line in lines:
        if in_block_comment:
            end_pos = line.find('*/')
            if end_pos != -1:
                in_block_comment = False
                line = line[end_pos + 2:]
            else:
                continue

        start_pos = line.find('/*')
        if start_pos != -1:
            end_pos = line.find('*/', start_pos)
            if end_pos != -1:
                line = line[:start_pos] + line[end_pos + 2:]
            else:
                in_block_comment = True
                line = line[:start_pos]

        comment_pos = line.find('//')
        if comment_pos != -1:
            in_string = False
            quote_char = None
            escaped = False

            for i, char in enumerate(line[:comment_pos]):
                if escaped:
                    escaped = False
                    continue
                if char == '\\':
                    escaped = True
                    continue
                if char in ['"', "'"]:
                    if not in_string:
                        in_string = True
                        quote_char = char
                    elif char == quote_char:
                        in_string = False
                        quote_char = None

            if not in_string:
                line = line[:comment_pos]

        if line.strip() or not result_lines or result_lines[-1].strip():
            result_lines.append(line.rstrip())

    while result_lines and not result_lines[-1].strip():
        result_lines.pop()

    return '\n'.join(result_lines) + '\n' if result_lines else ''

def process_kotlin_files(root_dir):
    count = 0
    for dirpath, dirnames, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.endswith('.kt'):
                filepath = os.path.join(dirpath, filename)
                print(f"Processing: {filepath}")

                try:
                    with open(filepath, 'r', encoding='utf-8') as f:
                        content = f.read()

                    cleaned_content = remove_comments_from_kotlin(content)

                    with open(filepath, 'w', encoding='utf-8') as f:
                        f.write(cleaned_content)

                    count += 1
                    print(f"  ✓ Cleaned")
                except Exception as e:
                    print(f"  ✗ Error: {e}")
    return count

if __name__ == "__main__":
    app_dir = "app/src"
    print(f"Removing comments from Kotlin files in {app_dir}...")
    count = process_kotlin_files(app_dir)
    print(f"Done! Processed {count} files.")

