import os
import csv


def parse_file(file_path):
    data = []
    with open(file_path, "r") as file:
        lines = file.readlines()
        for line in lines:
            if line.strip():  # Check if line is not empty
                data.append(float(line.strip()))
    return data


def process_files(directory):
    time_data = {}
    l1_cache_misses_data = {}
    l2_cache_misses_data = {}
    dp_ops_data = {}

    for filename in os.listdir(directory):
        print(filename)
        if filename.endswith(".txt"):
            size = int(filename.split("x")[0])
            file_path = os.path.join(directory, filename)
            data = parse_file(file_path)
            time_data[size] = data[::4]
            l1_cache_misses_data[size] = data[1::4]
            l2_cache_misses_data[size] = data[2::4]
            dp_ops_data[size] = [
                dp_ops / time for dp_ops, time in zip(data[3::4], data[::4])
            ]

    time_data = dict(sorted(time_data.items()))
    l1_cache_misses_data = dict(sorted(l1_cache_misses_data.items()))
    l2_cache_misses_data = dict(sorted(l2_cache_misses_data.items()))
    dp_ops_data = dict(sorted(dp_ops_data.items()))

    return time_data, l1_cache_misses_data, l2_cache_misses_data, dp_ops_data


def write_csv(data, file_name):
    with open(file_name, "w", newline="") as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["size"] + list(range(1, len(next(iter(data.values()))) + 1)))
        for size, values in data.items():
            writer.writerow([size] + values)


if __name__ == "__main__":
    directory = "/home/daniel/Documents/uni/cpd/g14/assign1/data/part1/ex2/c++"
    time_data, l1_cache_misses_data, l2_cache_misses_data, dp_ops_data = process_files(
        directory
    )
    if time_data and l1_cache_misses_data and l2_cache_misses_data and dp_ops_data:
        write_csv(time_data, "time.csv")
        write_csv(l1_cache_misses_data, "l1dcm.csv")
        write_csv(l2_cache_misses_data, "l2dcm.csv")
        write_csv(dp_ops_data, "flops.csv")
        print("CSV files have been successfully created.")
    else:
        print("Some data is missing in the files.")
