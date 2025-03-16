import subprocess
from glob import glob

java_files = glob("nnet/*.java")
subprocess.run(["javac"] + java_files)
java_process = subprocess.Popen(["java", "-cp", "nnet", "Main"])

java_process.wait()