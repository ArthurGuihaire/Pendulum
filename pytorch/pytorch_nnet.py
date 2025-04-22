import torch
import torch.nn as nn
import random
import copy

class Nnet(nn.Module):
    def __init__(self):
        super().__init__()
        self.net = nn.Sequential(
            nn.Linear(4, 8),
            nn.Tanh(),
            nn.Linear(8, 1)
        )

    def forward(self, x):
        return self.net(x)

def mutate(model, rate=0.1):
    new_model = copy.deepcopy(model)
    with torch.no_grad():
        for param in new_model.parameters():
            param.add_(torch.randn_like(param) * rate)
    return new_model