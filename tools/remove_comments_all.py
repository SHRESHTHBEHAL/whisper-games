#!/usr/bin/env python3
import os
import sys

def strip_comments_from_source(src: str) -> str:
    out = []
    i = 0
    n = len(src)
    in_s = False
    s_char = ''
    escaped = False
    in_block = False
    while i < n:
        ch = src[i]
        nxt = src[i+1] if i+1 < n else ''
        if in_block:
            if ch == '*' and nxt == '/':
                in_block = False
                i += 2
                continue
            else:
                i += 1
                continue
        if in_s:
            out.append(ch)
            if escaped:
                escaped = False
            elif ch == '\\':
                escaped = True
            elif ch == s_char:
                in_s = False
            i += 1
            continue
        # not in string or block
        if ch == '/' and nxt == '/':
            # line comment: skip until end of line
            i += 2
            while i < n and src[i] != '\n':
                i += 1
            # keep the newline if present
            if i < n and src[i] == '\n':
                out.append('\n')
                i += 1
            continue
        if ch == '/' and nxt == '*':
            in_block = True
            i += 2
            continue
        if ch == '"' or ch == "'":
            in_s = True
            s_char = ch
            out.append(ch)
            i += 1
            continue
        out.append(ch)
        i += 1
    return ''.join(out)


def process(root):
    exts = ('.kt', '.kts', '.java')
    total = 0
    for dirpath, dirnames, filenames in os.walk(root):
        for fname in filenames:
            if fname.endswith(exts):
                path = os.path.join(dirpath, fname)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        src = f.read()
                except Exception as e:
                    print(f"skip {path}: read error {e}")
                    continue
                new = strip_comments_from_source(src)
                if new != src:
                    try:
                        with open(path, 'w', encoding='utf-8') as f:
                            f.write(new)
                        print(f"cleaned: {path}")
                        total += 1
                    except Exception as e:
                        print(f"skip {path}: write error {e}")
    print(f"done. cleaned {total} files")

if __name__ == '__main__':
    root = sys.argv[1] if len(sys.argv) > 1 else 'app/src'
    if not os.path.exists(root):
        print('root not found:', root)
        sys.exit(1)
    process(root)

