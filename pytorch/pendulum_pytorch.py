from torch.utils.cpp_extension import load
import os
os.environ["MAX_JOBS"] = "10"
score_cpp = load(name="score_cpp", sources=["simulate_pendulum_new.cpp"], verbose=True)
print("Loaded c++ file")

import math
from typing import Protocol, Any
from copy import deepcopy
import argparse

import torch
import torch.nn as nn
import numpy as np

from concurrent.futures import ThreadPoolExecutor
executor = ThreadPoolExecutor(max_workers=10)

class ModelProtocol(Protocol):
    def __call__(self, x: torch.Tensor) -> torch.Tensor:
        ...

def script_model(net: nn.Module) -> torch.jit.ScriptModule:
    return torch.jit.script(net.eval())._c

def mutate(model: nn.Sequential, rate=0.1):
    new_model = deepcopy(model)
    with torch.no_grad():
        for param in new_model.parameters():
            param.add_(torch.randn_like(param) * rate)
    return new_model

def score_network(index):
    return score_cpp.score_nnet(0.0, 600, scripted_networks[index])

if __name__  == "__main__":
    generations = 100
    batch_size = 240
    network_array = []
    scripted_networks = []

    parser = argparse.ArgumentParser()
    parser.add_argument("--load-model", action="store_true")
    args = parser.parse_args()
    if args.load_model:
        network_array.append(nn.Sequential(
                nn.Linear(4, 16),
                nn.Tanh(),
                nn.Linear(16, 1),
                nn.Tanh())
            .eval())
        network_array[0].load_state_dict(torch.load(f"pytorch_models/model_{generations-1}.pt"))
        scripted_networks.append(script_model(network_array[0]))
        for i in range(batch_size-1):
            network_array.append(mutate(network_array[0], 0.01))
            scripted_networks.append(script_model(network_array[i]))

    else:
        print("Creating random networks...")
        for i in range(batch_size):
            network_array.append(nn.Sequential(
                nn.Linear(4, 16),
                nn.Tanh(),
                nn.Linear(16, 1),
                nn.Tanh())
            .eval())

            scripted_networks.append(script_model(network_array[i]))

        print("Random networks created")

    learning_rate = 0.1

    scores = np.empty(batch_size, dtype=float)

    print("Starting training...")

    for generation in range(generations):
        futures = [executor.submit(score_network, i) for i in range(batch_size)]
        scores = [f.result() for f in futures]
        top_indices = np.argsort(scores)[-(int(batch_size*0.8/15)):]# [::-1]# to reverse the scores
        top_networks = [network_array[i] for i in top_indices]
        num_copies_total = int(batch_size * 0.8/15)*15

        for i in range(int(batch_size*0.8/15)):
            network_array[15*i] = deepcopy(top_networks[i])
            for j in range(1,15):
                network_array[15*i+j] = mutate(top_networks[i], learning_rate)

        for i in range(batch_size - num_copies_total):
            network_array[i+num_copies_total] = nn.Sequential(
                nn.Linear(4, 16),
                nn.Tanh(),
                nn.Linear(16,1)
            )

        print(f"Iteration: {generation}")
        print(f"Best score: {max(scores)}")
        print(f"Average score: {sum(scores)/batch_size}")
        torch.save(top_networks[len(top_networks)-1].state_dict(), f'pytorch_models/model_{generation}.pt')

        for i in range(batch_size):
            scripted_networks[i] = script_model(network_array[i])

        learning_rate *= 0.91

