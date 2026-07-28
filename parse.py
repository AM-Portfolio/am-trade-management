import json
with open('comments.json', encoding='utf-16') as f:
    d=json.load(f)
with open('comments.txt', 'w', encoding='utf-8') as f:
    for c in d:
        f.write(f'FILE: {c.get("path")}\nLINE: {c.get("line")}\nCOMMENT: {c.get("body")}\n\n')
