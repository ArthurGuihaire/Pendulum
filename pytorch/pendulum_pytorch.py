from torch.utils.cpp_extension import load
import os
os.environ["MAX_JOBS"] = "6"
score_cpp = load(name="score_cpp", sources=["simulate_pendulum.cpp"], verbose=True)
print("Loaded c++ file")

import math
from typing import Protocol, Any
from copy import deepcopy

import torch
import torch.nn as nn
import numpy as np

import multiprocessing as mp

def default_model(input_tensor: torch.Tensor) -> torch.Tensor:
    """
    A placeholder PyTorch model that outputs zero torque.
    Replace or subclass with your trained network.
    """
    return torch.zeros(1)


class ModelProtocol(Protocol):
    def __call__(self, x: torch.Tensor) -> torch.Tensor:
        ...

def mutate(model: nn.Sequential, rate=0.1):
    new_model = deepcopy(model)
    with torch.no_grad():
        for param in new_model.parameters():
            param.add_(torch.randn_like(param) * rate)
    return new_model

def score_network(args):
    i, network = args
    model = torch.jit.script(network)
    model.save(f"pytorch_models/model_{i}.pt")
    return score_cpp.score_nnet(0.0, 600, i)

# Example usage:
if __name__ == "__main__":
    # Define a simple PyTorch network with one output neuron:
    print("Creating random networks...")
    network_array = []
    batch_size = 150
    for i in range(batch_size):
        network_array.append(nn.Sequential(
            nn.Linear(4, 16),
            nn.Tanh(),
            nn.Linear(16, 1))
        .eval())

    print("Random networks created")

    learning_rate = 0.1

    scores = np.empty(batch_size, dtype=float)
    args_array = [None] * batch_size

    print("Starting training...")

    for generation in range(500):
        for i in range(batch_size):
            args_array[i] = (i, network_array[i])
        with mp.Pool(processes=6) as pool:
            scores = pool.map(score_network, args_array)
        top_indices = np.argsort(scores)[-(int(batch_size*0.8/15)):]# [::-1]# to reverse the scores
        top_networks = [network_array[i] for i in top_indices]
        num_copies_total = int(batch_size * 0.8/15)*15
        for i in range(int(batch_size*0.8/15)):
            network_array[15*i] = deepcopy(top_networks[i])
            for j in range(1,15):
                network_array[15*i+j] = mutate(top_networks[i], learning_rate)
        for i in range(batch_size - num_copies_total):
            network_array[i+num_copies_total] = nn.Sequential(nn.Sequential(
                nn.Linear(4, 16),
                nn.Tanh(),
                nn.Linear(16,1)
            ))

        print(f"Best score: {max(scores)}")
        torch.save(top_networks[4].state_dict(), f'pytorch_models/model_{generation}.pth')

        learning_rate *= 0.75
