import torch
import torch.nn as nn

net = nn.Sequential(
    nn.Linear(4, 8),
    nn.Tanh(),
    nn.Linear(8, 2)
)

net.load_state_dict(torch.load('net.pth'))
net.eval()