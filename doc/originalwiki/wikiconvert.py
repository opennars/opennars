#!/usr/bin/env python

"""
Usage:
    python googlecode2github/wikiconfig.py PROJID SRCDIR DSTDIR

where "PROJID" is the github project id, e.g. "trentm/python-markdown2",
"SRCDIR" is a Google Code project wiki Subversion working copy dir and
"DSTDIR" is the git clone dir of the git project's wiki.
"""

__version__ = "1.0.0"

import re
import sys
from os.path import *
from glob import glob
from pprint import pprint
import codecs
from hashlib import md5


def log(s):
    sys.stderr.write(s+"\n")

def convert_dir(proj_id, src_dir, dst_dir):
    if isfile(src_dir):
        convert_file(proj_id, src_dir, dst_dir)
    else:
        for f in glob(join(src_dir, "*.wiki")):
            convert_file(proj_id, f, dst_dir)

def convert_file(proj_id, src_path, dst_dir):
    src = codecs.open(src_path, 'r', 'utf-8').read()
    meta_lines = []
    body_lines = []
    lines = src.splitlines(False)
    for i, line in enumerate(lines):
        if line.startswith("#"):
            meta_lines.append(line)
        else:
            assert not line.strip(), "line isn't empty: %r" % line
            body_lines = lines[i+1:]
            break
    meta = {}
    for line in meta_lines:
        k,v = line[1:].split(None, 1)
        meta[k] = v
    text = '\n'.join(body_lines)
    s_from_hash = {}

    # Pull out pre-blocks.
    def sub_pre_block(match):
        pre = match.group(1)
        hash = md5(pre.encode('utf8')).hexdigest()
        s_from_hash[hash] = _indent(pre)
        return hash
    text = re.compile(r'^{{{\n(.*?)^}}}', re.M|re.S).sub(sub_pre_block, text)

    # Headings.
    text = re.compile(r'^===(.*?)===\s*$', re.M).sub(lambda m: "### %s\n"%m.group(1).strip(), text)
    text = re.compile(r'^==(.*?)==\s*$', re.M).sub(lambda m: "## %s\n"%m.group(1).strip(), text)
    text = re.compile(r'^=(.*?)=\s*$', re.M).sub(lambda m: "# %s\n"%m.group(1).strip(), text)

    # Tables
    def sub_table(m):
        rows = []
        for line in m.group(0).splitlines(False):
            if not line.strip():
                continue
            rows.append(list(c.strip() for c in line.split("||")[1:-1]))
        lines = ['<table>']
        for row in rows:
            lines.append('  <tr>%s</tr>' % ''.join('<td>%s</td>' % c for c in row))
        lines.append('</table>')
        return '\n\n' + '\n'.join(lines)
    text = re.compile(r'\n(\n^\|\|(.*?\|\|)+$)+', re.M).sub(sub_table, text)

    # Lists (don't handle nested lists).
    text = re.compile(r'^[ \t]+\*[ \t]+(.*?)[ \t]*$', re.M).sub(r'- \1', text)
    text = re.compile(r'^[ \t]+#[ \t]+(.*?)[ \t]*$', re.M).sub(r'1. \1', text)

    # wiki links.
    def sub_wikilink(m):
        gh_page_name = _gh_page_name_from_gc_page_name(m.group(1)).replace('-', ' ')
        if m.group(2):
            s = "[[%s|%s]]" % (gh_page_name, m.group(2))
            pass
        else:
            s = "[[%s]]" % gh_page_name
        hash = md5(s.encode('utf8')).hexdigest()
        s_from_hash[hash] = s
        return hash
    text = re.compile(r'\[((?:[A-Z][a-z]+)+)(?:\s+(.*?))?\]', re.S).sub(sub_wikilink, text)

    # Links
    def sub_link(m):
        s = "[%s](%s)" % (m.group(2), m.group(1))
        hash = md5(s.encode('utf8')).hexdigest()
        s_from_hash[hash] = s
        return hash
    text = re.compile(r'(?<!\[)\[([^\s]+)\s+(.*?)\](?!\])', re.S).sub(sub_link, text)

    # Italics, bold.
    # in*ter*bold: (?<=\w)(\*\w+?\*)(?=\w)
    text = re.compile(r'(?<![*\w])\*([^*]+?)\*(?![*\w])', re.S).sub(r'**\1**', text)
    text = re.compile(r'(?<![_\w])_([^_]+?)_(?![_\w])', re.S).sub(r'*\1*', text)

    # Auto-linking "issue \d+"
    text = re.compile(r'(?<!\[)(issue (\d+))(?!\])').sub(
        r'[\1](https://github.com/%s/issues#issue/\2)' % proj_id, text)

    # Restore hashed-out blocks.
    for hash, s in s_from_hash.items():
        text = text.replace(hash, s)

    # Add summary.
    if "summary" in meta:
        text = ("# %s\n\n" % meta["summary"]) + text

    base = splitext(basename(src_path))[0]
    gh_page_name = _gh_page_name_from_gc_page_name(base)
    dst_path = join(dst_dir, gh_page_name+".md")
    if not exists(dst_path) or codecs.open(dst_path, 'r', 'utf-8').read() != text:
        codecs.open(dst_path, 'w', 'utf-8').write(text)
        log("wrote '%s'" % dst_path)


#---- internal support stuff

def _indent(text):
    return '    ' + '\n    '.join(text.splitlines(False))

def _gh_page_name_from_gc_page_name(gc):
    """Github (gh) Wiki page name from Google Code (gc) Wiki page name."""
    gh = re.sub(r'([A-Z][a-z]+)', r'-\1', gc)[1:]
    return gh


#---- mainline

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print __doc__
        sys.exit(1)
    convert_dir(sys.argv[1], sys.argv[2], sys.argv[3])
