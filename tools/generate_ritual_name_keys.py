# -*- coding: utf-8 -*-
"""Append ritual name and upstream message keys for every ritual in rituals.json.

English values reuse the ritual `display_name` verbatim (JEI classifies spirit jobs from that English string,
so it must stay English). Chinese values reuse the matching `item.occultism.ritual_dummy.<path>.name` entry with
its `仪式：` prefix stripped, because the chat sentences already supply their own prefix. Six bridge prototype
rituals have no ritual dummy item and are translated manually in MANUAL_ZH below.

Idempotent: keys that already exist in a lang file are skipped.
"""
from __future__ import unicode_literals

import io
import json
import os

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(REPO, 'src', 'main', 'resources', 'assets', 'occultism')
RITUALS = os.path.join(ASSETS, 'rituals', 'rituals.json')
LANG_DIR = os.path.join(ASSETS, 'lang')
ZH_PREFIX = '仪式：'
EN_PREFIX = 'Ritual: '
HEADER = '# Ritual display names, resolved client side so chat sentences stay in one language.'
MESSAGE_HEADER = '# Upstream-style per-ritual message protocol.'

# Rituals without a ritual_dummy item; these are the recently restored bridge prototypes.
MANUAL_ZH = {
    'possess_djinni_prototype': '附身巨灵（原型）',
    'possess_unbound_afrit_prototype': '附身未束缚火灵（原型）',
    'possess_afrit_prototype': '附身火灵（原型）',
    'craft_afrit_prototype': '火灵灌注（原型）',
    'contact_wild_spirit_prototype': '接触荒野魔灵（原型）',
    'contact_eldritch_spirit_prototype': '接触诡奇魔灵（原型）',
}


def read_lang(path):
    entries = {}
    order = []
    with io.open(path, encoding='utf-8') as handle:
        for line in handle:
            line = line.rstrip('\r\n')
            if not line or line.startswith('#') or '=' not in line:
                continue
            key, value = line.split('=', 1)
            if key not in entries:
                order.append(key)
            entries[key] = value
    return entries, order


def dummy_key(ritual):
    path = ritual['ritual_dummy']['id'].split(':', 1)[1].replace('/', '.')
    return 'item.occultism.%s.name' % path


def strip_prefix(value, prefix):
    return value[len(prefix):] if value.startswith(prefix) else value


def main():
    with io.open(RITUALS, encoding='utf-8') as handle:
        rituals = json.load(handle)

    en_path = os.path.join(LANG_DIR, 'en_us.lang')
    zh_path = os.path.join(LANG_DIR, 'zh_cn.lang')
    en, _ = read_lang(en_path)
    zh, _ = read_lang(zh_path)

    additions = {'en_us.lang': [], 'zh_cn.lang': []}
    message_values = {
        'started': ('Starting the ritual: %s.', '正在开始仪式：%s。'),
        'interrupted': ('Interruption in the ritual: %s.', '仪式中断：%s。'),
        'finished': ('Ritual completed successfully: %s.', '仪式成功完成：%s。'),
        'conditions': ('Not all requirements for this ritual are met: %s.', '该仪式的要求尚未全部满足：%s。'),
    }
    for ritual in rituals:
        ritual_id = ritual['id']
        key = 'ritual.occultism.%s.name' % ritual_id
        english = ritual['display_name']
        if ritual_id in MANUAL_ZH:
            chinese = MANUAL_ZH[ritual_id]
        else:
            source = dummy_key(ritual)
            if source not in zh:
                raise SystemExit('No Chinese ritual dummy name for %s (%s)' % (ritual_id, source))
            chinese = strip_prefix(zh[source], ZH_PREFIX)
        if chinese.startswith(ZH_PREFIX) or not chinese.strip():
            raise SystemExit('Bad Chinese ritual name for %s: %r' % (ritual_id, chinese))

        if key not in en:
            additions['en_us.lang'].append((key, english))
        if key not in zh:
            additions['zh_cn.lang'].append((key, chinese))
        for suffix, values in message_values.items():
            message_key = 'ritual.occultism.%s.%s' % (ritual_id, suffix)
            if message_key not in en:
                additions['en_us.lang'].append((message_key, values[0] % english))
            if message_key not in zh:
                additions['zh_cn.lang'].append((message_key, values[1] % chinese))

    for name, path in (('en_us.lang', en_path), ('zh_cn.lang', zh_path)):
        rows = additions[name]
        if not rows:
            print('%s: nothing to add' % name)
            continue
        name_rows = [row for row in rows if row[0].endswith('.name')]
        message_rows = [row for row in rows if not row[0].endswith('.name')]
        with io.open(path, 'a', encoding='utf-8', newline='\n') as handle:
            if name_rows:
                handle.write('\n%s\n' % HEADER)
                for key, value in name_rows:
                    handle.write('%s=%s\n' % (key, value))
            if message_rows:
                handle.write('\n%s\n' % MESSAGE_HEADER)
                for key, value in message_rows:
                    handle.write('%s=%s\n' % (key, value))
        print('%s: appended %d keys' % (name, len(rows)))


if __name__ == '__main__':
    main()
