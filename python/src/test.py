import sys


def return_value(args):
    if type(args) is str:
        return args
    return "argument is not string"


result = return_value(sys.argv[1])
print(result)

with open("data/splitted/satd.train", 'w') as target:
    target.write(result)
