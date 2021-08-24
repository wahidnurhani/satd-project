import re
import sys

import numpy as np
import pandas as pd
from bs4 import BeautifulSoup
from nltk.corpus import stopwords
from textblob import TextBlob, Word

stop_words = stopwords.words('english')


def fix_contractions(phrase):
    # specific
    phrase = re.sub(r"won\'t", "will not", phrase)
    phrase = re.sub(r"can\'t", "can not", phrase)

    # general
    phrase = re.sub(r"n\'t", " not", phrase)
    phrase = re.sub(r"\'re", " are", phrase)
    phrase = re.sub(r"\'s", " is", phrase)
    phrase = re.sub(r"\'d", " would", phrase)
    phrase = re.sub(r"\'ll", " will", phrase)
    phrase = re.sub(r"\'t", " not", phrase)
    phrase = re.sub(r"\'ve", " have", phrase)
    phrase = re.sub(r"\'m", " am", phrase)
    return phrase


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


def remove_stopwords(text_data):
    words = text_data.split()
    result = ""
    for w in words:
        if w not in stop_words:
            if len(w) > 2:
                word = Word(w)
                result = result + " " + word.lemmatize()
    return result.strip()


def initial_cleaning_and_fix_contractions(text_data):
    # Stanford classifier form
    text_data = text_data.replace('\t', ' ')
    text_data = text_data.replace('\n', '. ')

    # remove html tags and url
    soup = BeautifulSoup(text_data, 'lxml')
    re_https = re.sub(r"http\S+", "", soup.get_text())
    clean_text = re.sub(r"www.\S+", "", re_https)

    # fix contractions
    contractions_free = fix_contractions(clean_text)
    return contractions_free


def second_cleaning_and_lemmatization(phrase, remove_number=True):
    # remove whitespaces and wordnumber
    phrase = re.sub("\S*\d\S*", "", phrase).strip()
    # remove unwanted character
    if remove_number:
        phrase = re.sub('[^.,A-Za-z]+', ' ', phrase)
    else:
        phrase = re.sub('[^.,A-Za-z0-9]+', ' ', phrase)
    if len(phrase) <= 2:
        phrase = ""
    else:
        # remove stopwords
        phrase = remove_stopwords(phrase)
        # lemmatization
        phrase = lemmatization(phrase)

    # replace empty string with np.nan
    phrase = replace_empty_string(phrase)
    return phrase


def export_to_train_and_test(df1, df2, train_path, test_path):
    df1.to_csv(train_path, index=False, header=False, sep="\t")
    df2.to_csv(test_path, index=False, header=False, sep="\t")


def clean_and_normalize_data(data_frame: pd.DataFrame):
    cols_to_check = ['commenttext']
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: x.lower())
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: initial_cleaning_and_fix_contractions(x))
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: second_cleaning_and_lemmatization(x))
    data_frame_clean = df.drop_duplicates(subset=['classification', 'commenttext'], keep='last')
    return data_frame_clean.dropna()


def split_data(data_number: int, data_frame):
    s_array = data_frame["projectname"].to_numpy()
    unique = np.unique(s_array)

    train = data_frame[data_frame['projectname'] != unique[data_number - 1]].drop(["projectname"], axis=1)
    test = data_frame[data_frame['projectname'] == unique[data_number - 1]].drop(["projectname"], axis=1)
    return train, test


def transform_data(data_number, _df, train_path, test_path):
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
    clean_df = clean_and_normalize_data(df)
    transform_data(int(split_number), clean_df, train_file_path, test_file_path)

    print("python process finished")
    sys.exit(220)
