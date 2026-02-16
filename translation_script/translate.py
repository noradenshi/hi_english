import sqlite3
import pandas as pd
from deep_translator import GoogleTranslator

# 1. Konfiguracja i Mapowanie POS
csv_file = 'word_list_cefr.csv'
db_file = 'dictionary.db'
translator = GoogleTranslator(source='en', target='pl')

pos_map = {
    'noun': 'rzeczownik',
    'verb': 'czasownik',
    'be-verb': 'czasownik (być)',
    'do-verb': 'czasownik (posiłkowy)',
    'have-verb': 'czasownik (mieć)',
    'modal auxiliary': 'czasownik modalny',
    'adjective': 'przymiotnik',
    'adverb': 'przysłówek',
    'preposition': 'przyimek',
    'pronoun': 'zaimek',
    'determiner': 'określnik',
    'conjunction': 'spójnik',
    'number': 'liczebnik',
    'interjection': 'wykrzyknik',
    'infinitive-to': 'partykuła (to)'
}

# 2. Wczytywanie i przygotowanie danych
try:
    df_full = pd.read_csv(csv_file, sep=';')

    # Filtrowanie tylko poziomu A1
    df_a1 = df_full[df_full['CEFR'] == 'A1'].copy()

    # Mapowanie części mowy na polski
    df_a1['pos_pl'] = df_a1['pos'].map(lambda x: pos_map.get(x, x))

    # Grupowanie duplikatów (headword) i łączenie części mowy
    df_grouped = df_a1.groupby('headword').agg({
        # Usuwa powtórki wewnątrz grupy
        'pos_pl': lambda x: ', '.join(dict.fromkeys(x)),
        'CEFR': 'first'
    }).reset_index()

    total_words = len(df_grouped)
    print(f"Wczytano {len(df_full)} słówek ogółem.")
    print(f"Przetwarzanie {total_words} unikalnych słówek z poziomu A1.")

except Exception as e:
    print(f"Błąd danych: {e}")
    exit()

# 3. Baza danych
conn = sqlite3.connect(db_file)
cursor = conn.cursor()

# Tworzenie czystej tabeli (z opisem jako NULL)
cursor.execute('DROP TABLE IF EXISTS words')
cursor.execute('''
    CREATE TABLE words (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        word TEXT NOT NULL,
        pos TEXT,
        cefr TEXT,
        translation_pl TEXT,
        description TEXT,
        image_res TEXT
    )
''')

# 4. Tłumaczenie i zapis
print("Rozpoczynam tłumaczenie... Proszę czekać.")

for index, row in df_grouped.iterrows():
    word_en = row['headword']
    pos_pl = row['pos_pl']
    cefr = row['CEFR']

    try:
        # Tłumaczenie
        translation = translator.translate(word_en)

        cursor.execute('''
            INSERT INTO words (word, pos, cefr, translation_pl, description, image_res)
            VALUES (?, ?, ?, ?, ?, ?)
        ''', (word_en, pos_pl, cefr, translation, None, None))

        if index % 50 == 0:
            print(f"Postęp: {index}/{total_words} ({word_en})")
            conn.commit()

    except Exception as e:
        print(f"Pominięto '{word_en}' z powodu błędu: {e}")

conn.commit()
conn.close()
print(f"Sukces! Baza {db_file} jest gotowa i zawiera {total_words} rekordów.")
