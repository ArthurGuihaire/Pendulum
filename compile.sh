SRC_DIR="."
find "$SRC_DIR" -name "*.class" -delete
javac -d . $(find "$SRC_DIR" -name "*.java")
if [ $? -eq 0 ]; then
    echo "Compilation successful!"
else
    echo "Compilation failed!"
fi