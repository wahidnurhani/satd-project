import sys

import numpy as np
import pandas as pd


def export_to_train_and_test(df1, df2):
    df1.to_csv('data/splited/trainFile.train', index=False, header=False, sep=" ")
    df2.to_csv('data/splited/testFile.test', index=False, header=False, sep=" ")


def data_preprocessing(df: pd.DataFrame):
    D = df.drop(['projectname'], axis=1)
    return D


def split_data(data_number: int, df):
    split = np.array_split(df, 10)
    test = split[data_number-1]
    frames = []
    numCross = 1
    for data in split:
        if numCross != data_number:
            frames.append(data)
        numCross += 1
    return pd.concat(frames), test


def transform_data(data_number, df):
    train, test = split_data(data_number, df)
    export_to_train_and_test(train, test)


split_number = sys.argv[1]
csv_source = sys.argv[2]
print("cross validation number: " + split_number)  # 1

df = pd.read_csv(csv_source)

clean_df = data_preprocessing(df)
transform_data(int(split_number), clean_df)

print("python process finished")
sys.exit(220)
