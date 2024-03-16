import os
import csv

def parse_java_data(file_path):
    data = []
    with open(file_path, 'r') as file:
        lines = file.readlines()
        for line in lines:
            if line.startswith('Time:'):
                time_str = line.strip().split(' ')[1]
                time = float(time_str)
                data.append(time)
    return data

def process_java_files(directory):
    time_data = {}

    for filename in os.listdir(directory):
        if filename.endswith('.txt'):
            size = int(filename.split('x')[0])
            file_path = os.path.join(directory, filename)
            data = parse_java_data(file_path)
            time_data[size] = data

    # Sort the dictionary keys (sizes) in ascending order
    time_data = dict(sorted(time_data.items()))

    return time_data

def write_time_csv(time_data, output_file):
    with open(output_file, 'w', newline='') as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["size"] + list(range(1, len(next(iter(time_data.values()))) + 1)))
        for size, times in time_data.items():
            writer.writerow([size] + times)

if __name__ == "__main__":
    directory = '/home/daniel/Documents/uni/cpd/g14/assign1/data/part1/ex2/java'
    output_file = 'time_java.csv'

    java_time_data = process_java_files(directory)
    write_time_csv(java_time_data, output_file)
