#!/bin/bash

set -e

ENV_NAME="myenv"

# Check if python3.10 is installed
if ! command -v python3.10 &> /dev/null
then
    echo "Python 3.10 not found. Installing..."
    if [[ "$OSTYPE" == "linux-gnu"* ]]; then
        sudo apt update
        sudo apt install -y python3.10 python3.10-venv python3.10-dev
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        brew install python@3.10
    else
        echo "Unsupported OS. Please install Python 3.9 manually."
        exit 1
    fi
else
    echo "Python 3.10 found."
fi

# Create virtual environment
python3.10 -m venv "$ENV_NAME"
echo "Virtual environment '$ENV_NAME' created."

# Activate the environment
source "$ENV_NAME/bin/activate"

# Upgrade pip
pip install --upgrade pip

# Install packages
pip install torch numpy

echo "PyTorch and NumPy installed in '$ENV_NAME'."
echo "Environment setup complete!"
echo "Run 'source $ENV_NAME/bin/activate' to activate the environment later."
