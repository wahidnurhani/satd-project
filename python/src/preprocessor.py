import sys

import numpy as np
import pandas as pd
import re
from bs4 import BeautifulSoup
from nltk.corpus import stopwords
#  from nltk.stem.snowball import SnowballStemmer
# from autocorrect import Speller

# from nltk.stem import WordNetLemmatizer

stop_words = stopwords.words('english')
# lemma = WordNetLemmatizer()
# stemmer = SnowballStemmer("english")


# spell = Speller(lang='en')


def remove_html_tags(text_data):
    soup = BeautifulSoup(text_data, 'lxml')
    return soup.get_text().strip()


def remove_url(text_data):
    re_https = re.sub(r"http\S+", "", text_data)
    re_www = re.sub(r"www.\S+", "", re_https)
    return re_www.strip()


# def remove_special_character(phrase, remove_number=False):
#     phrase = re.sub("\S*\d\S*", "", phrase).strip()
#     if remove_number:
#         phrase = re.sub('[^A-Za-z]+', ' ', phrase)
#     else:
#         phrase = re.sub('[^A-Za-z0-9]+', ' ', phrase)
#     return phrase.strip()
#
#
# # def spelling_corrector(text_data):
# #     words = text_data.split()
# #     result = ""
# #     for w in words:
# #         if w not in stop_words:
# #             result = result + " " + spell(w)
# #     return result.strip()
#
#
def remove_stopwords(text_data):
    words = text_data.split()
    result = ""
    for w in words:
        if w not in stop_words:
            result = result + " " + w
    return result.strip()


# # def stemming_text(text_data):
# #     words = text_data.split()
# #     result = ""
# #     for w in words:
# #         result = result + " " + stemmer.stem(w)
# #     return result.strip()
#
#
# # def lem_text(text_data):
# #     words = text_data.split()
# #     result = ""
# #     for w in words:
# #         result = result + " " + lemma.lemmatize(word=w, pos='v')
# #     return result.strip()
#
#
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


# def replace_empty_string(text_data):
#     if len(text_data.strip()) < 1:
#         return np.nan
#     return text_data.strip()
#
#
# def remove_unwanted_words(text_data):
#     words = text_data.split()
#     result = ""
#     for w in words:
#         if len(w) > 2:
#             result = result + " " + w
#     return result


def export_to_train_and_test(df1, df2, train_path, test_path):
    df1.to_csv(train_path, index=False, header=False, sep="\t")
    df2.to_csv(test_path, index=False, header=False, sep="\t")


def format_data(data_frame: pd.DataFrame):
    cols_to_check = ['commenttext']
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: x.strip())
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: x.lower())
    data_frame[cols_to_check] = data_frame[cols_to_check].replace({'\t': ' '}, regex=True)
    data_frame[cols_to_check] = data_frame[cols_to_check].replace({'\n': '. '}, regex=True)
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: remove_html_tags(x))
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: remove_url(x))
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: fix_contractions(x))
    data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: remove_stopwords(x))
    # data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: remove_unwanted_words(x))
    # # data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: stemming_text(x))
    # # data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: lem_text(x))
    # data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: remove_special_character(x))
    # data_frame[cols_to_check] = data_frame[cols_to_check].applymap(lambda x: replace_empty_string(x))
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
    clean_df = format_data(df)
    transform_data(int(split_number), clean_df, train_file_path, test_file_path)

    print("python process finished")
    sys.exit(220)
