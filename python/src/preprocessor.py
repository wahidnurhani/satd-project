import re
import sys

import numpy as np
import pandas as pd
import contractions
from sklearn.utils import shuffle
from bs4 import BeautifulSoup
# from nltk.corpus import stopwords
from textblob import TextBlob, Word


# stop_words = stopwords.words('english')


def fix_contractions(text_data):
    words = text_data.split()
    result = ""
    for w in words:
        result = result + " " + contractions.fix(w)
    return result.strip()


def replace_empty_string(text_data):
    if len(text_data.strip()) < 1:
        return np.nan
    return text_data.strip()


def lemmatization(text_data):
    sentence = TextBlob(text_data)
    tags_dict = {"J": 'a',
                 "N": 'n',
                 "V": 'v',
                 "R": 'r'}
    words_and_tags = [(w, tags_dict.get(pos[0], 'n')) for w, pos in sentence.tags]
    lemmatized_list = [wd.lemmatize(tag) for wd, tag in words_and_tags]
    return " ".join(lemmatized_list)


#
#
# def remove_stopwords(text_data):
#     words = text_data.split()
#     result = ""
#     for w in words:
#         if w not in stop_words:
#             if len(w) > 2:
#                 word = Word(w)
#                 result = result + " " + word.lemmatize()
#     return result.strip()


def initial_cleaning_and_fix_contractions(text_data):
    # remove html tags and url
    soup = BeautifulSoup(text_data, 'lxml')
    re_https = re.sub(r"http\S+", "", soup.get_text())
    clean_text = re.sub(r"www.\S+", "", re_https)

    # fix contractions
    contractions_free = fix_contractions(clean_text)
    return contractions_free


def stopwords_and_lemmatization(phrase):
    # remove stopwords
    # phrase = remove_stopwords(phrase)
    # lemmatization
    phrase = lemmatization(phrase)

    # replace empty string with np.nan
    return phrase


def export_to_train_and_test(df1, df2, train_path, test_path):
    df1.to_csv(train_path, index=False, header=False, sep="\t")
    df2.to_csv(test_path, index=False, header=False, sep="\t")


def clean_and_normalize_data(data_frame: pd.DataFrame):
    cols_to_check = ['commenttext']
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: x.lower())
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: initial_cleaning_and_fix_contractions(x))
    # data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: second_cleaning_and_lemmatization(x))
    data_frame_clean = df.drop_duplicates(subset=['classification', 'commenttext'], keep='last')
    return data_frame_clean.dropna()


def clean_data(text_data):
    # remove new line char and tab char (stanford classifier requirements)
    text_data = text_data.replace('\t', ' ')
    text_data = text_data.replace('\n', '. ')
    # remove java comment syntax
    text_data = text_data.replace('//', '')
    text_data = text_data.replace('/*', '')
    text_data = text_data.replace('*/', '')
    # tokenization remove punctuation
    text_data = text_data.replace('...', ' ')
    words = text_data.split()
    words = [re.sub('[,:;#]+', '', word) for word in words]
    # rejoin token and remove excess whitespace
    phrase = " ".join(words)
    phrase.strip()
    phrase = re.sub(' +', ' ', phrase)
    # replace empty string with np.na
    phrase = replace_empty_string(phrase)
    return phrase


def clean_data2(text_data):
    # remove new line char and tab char (stanford classifier requirements)
    text_data = text_data.replace('\t', ' ')
    text_data = text_data.replace('\n', '. ')
    # remove java comment syntax
    text_data = text_data.replace('//', '')
    text_data = text_data.replace('/*', '')
    text_data = text_data.replace('*/', '')
    # tokenization remove punctuation
    words = text_data.split()
    words = [re.sub('^[!?]+', ' ', word) for word in words]
    # rejoin token and remove excess whitespace
    phrase = " ".join(words)
    phrase.strip()
    phrase = re.sub(' +', ' ', phrase)
    # replace empty string with np.na
    phrase = replace_empty_string(phrase)
    return phrase


def prepare_data(data_frame: pd.DataFrame):
    cols_to_check = ['commenttext']
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: x.lower())
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: initial_cleaning_and_fix_contractions(x))
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: clean_data(x))
    data_frame_clean = df.drop_duplicates(subset=['classification', 'commenttext'], keep='last')
    return data_frame_clean.dropna()


def split_data(data_number: int, data_frame):
    s_array = data_frame["projectname"].to_numpy()
    unique = np.unique(s_array)
    train = shuffle(data_frame[data_frame['projectname'] != unique[data_number - 1]].drop(["projectname"], axis=1))
    test = data_frame[data_frame['projectname'] == unique[data_number - 1]].drop(["projectname"], axis=1)
    return train, test


def split_data2(data_number: int, data_frame: pd.DataFrame):
    data_frame = data_frame.drop(["projectname"], axis=1)
    data_frame = data_frame.sort_values(by=["classification"])

    df_list = []
    for i in range(10):
        c = pd.DataFrame()
        df_list.insert(i, c)

    a = 0
    for i in range(data_frame.shape[0]):
        df_list[a] = df_list[a].append(data_frame.iloc[i])
        a = a+1
        if a == 10:
            a = 0

    test = df_list[data_number - 1]

    df_list.pop(data_number - 1)
    train = pd.concat(df_list, ignore_index=True)
    return train, test


def segregation_data(data_number, _df, train_path, test_path):
    train, test = split_data(data_number, _df)
    export_to_train_and_test(train, test, train_path, test_path)


if __name__ == '__main__':
    split_number = sys.argv[1]
    csv_source = sys.argv[2]
    train_file_path = sys.argv[3]
    test_file_path = sys.argv[4]
    prop_file_path = sys.argv[5]

    cols_to_use = ['classification', 'commenttext']
    df = pd.read_csv(csv_source)

    # to_remove_index = ['DOCUMENTATION', 'DEFECT', 'TEST']
    #
    # for i in to_remove_index:
    #     indexName = df[df['classification'] == i].index
    #     df.drop(indexName, inplace=True)

    clean_df = prepare_data(df)
    segregation_data(int(split_number), clean_df, train_file_path, test_file_path)

    print("python process finished")
    sys.exit(220)
