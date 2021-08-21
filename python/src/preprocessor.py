import sys

import numpy as np
import pandas as pd


def export_to_train_and_test(df1, df2, train_path, test_path):
    df1.to_csv(train_path, index=False, header=False, sep="\t")
    df2.to_csv(test_path, index=False, header=False, sep="\t")


def format_data(data_frame: pd.DataFrame):
    cols_to_check = ['commenttext']
    data_frame[cols_to_check] = data_frame[cols_to_check].replace({'\t': ' '}, regex=True)
    # data_frame[cols_to_check] = data_frame[cols_to_check].replace({'\n': ' '}, regex=True)
    return data_frame


def split_data(data_number: int, data_frame):
    s_array = data_frame["projectname"].to_numpy()
    unique = np.unique(s_array)

    train = data_frame[data_frame['projectname'] != unique[data_number - 1]].drop(["projectname"], axis=1)
    test = data_frame[data_frame['projectname'] == unique[data_number - 1]].drop(["projectname"], axis=1)
    return train, test


def transform_data(data_number, df, train_path, test_path):
    train, test = split_data(data_number, df)
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
