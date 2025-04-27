from torch.utils.cpp_extension import load
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


class PendulumSimulator:
    """
    Reimplementation of the Java Pendulum simulator in Python.
    Does not include any UI; use `score()` to evaluate a PyTorch model.
    """
    def __init__(
        self,
        iterations: int = 600,
        score_frequency: int = 1,
        pendulum_length: float = 150.0,
        g: float = 0.25,
        friction_factor: float = 0.995,
        speed_multiplier: float = 100.0,
        movement_cost: float = 0.0,
        boundary: float = 1200.0,
    ):
        # Simulation parameters
        self.iterations = iterations
        self.score_frequency = score_frequency
        self.pendulum_length = pendulum_length
        self.g = g
        self.friction_factor = friction_factor
        self.speed_multiplier = speed_multiplier
        self.movement_cost = movement_cost
        self.boundary = boundary

    def physics(self, horizontal_acceleration: float) -> None:
        """
        Update angular velocity and angle based on horizontal acceleration.
        """
        self.angular_velocity += (self.g / self.pendulum_length) * math.cos(self.angle)
        self.angular_velocity += (horizontal_acceleration / self.pendulum_length) * math.sin(self.angle)
        self.angular_velocity *= self.friction_factor
        self.angle += self.angular_velocity

        # Update relative positions
        self.x_rel = self.pendulum_length * math.cos(self.angle)
        self.y_rel = self.pendulum_length * math.sin(self.angle)

    def update_input(self) -> torch.Tensor:
        """
        Prepare the 4-element input tensor:
        [cos(angle), sin(angle), angular_velocity, center_vel_old / speed_multiplier]
        """
        return torch.tensor(
            [
                math.cos(self.angle),
                math.sin(self.angle),
                self.angular_velocity,
                self.center_vel_old / self.speed_multiplier,
            ],
            dtype=torch.float32,
        )

    def score(
        self,
        model: ModelProtocol = default_model,
        start_angle: float = 1.5 * math.pi,
    ) -> int:
        """
        Run the pendulum simulation and return the cumulative score.

        Args:
            model: A PyTorch model or callable that maps a 4-dim tensor -> 1-dim tensor.
            start_angle: Initial pendulum angle in radians.

        Returns:
            Integer score (higher is better).
        """
        # Initialize state
        self.user_score = 0.0
        self.x = 0.0
        self.angle = start_angle
        self.angular_velocity = 0.0
        self.center_vel = 0.0
        self.center_vel_old = 0.0
        self.x_rel = 0.0
        self.y_rel = 0.0

        for i in range(self.iterations):
            # Save previous velocity
            self.center_vel_old = self.center_vel

            # Build input and get model output
            inp = self.update_input()
            with torch.no_grad():
                out = model(inp)
            # Extract scalar output
            torque = float(out.view(-1)[0])
            # Scale and apply
            self.center_vel = self.speed_multiplier * torque

            # Update position and boundary conditions
            self.x += self.center_vel
            if self.x > self.boundary:
                self.x = self.boundary
                self.center_vel = 0.0
                self.user_score -= 1000.0
            elif self.x < -self.boundary:
                self.x = -self.boundary
                self.center_vel = 0.0
                self.user_score -= 1000.0

            # Physics update
            self.physics(self.center_vel - self.center_vel_old)

            # Scoring
            if i % self.score_frequency == 0:
                self.user_score += max(0.0, -self.y_rel)
                self.user_score -= 5000.0 * (self.angular_velocity ** 2)
                self.user_score -= self.movement_cost * abs(self.center_vel)

        return int(self.user_score)

def mutate(model: nn.Sequential, rate=0.1):
    new_model = deepcopy(model)
    with torch.no_grad():
        for param in new_model.parameters():
            param.add_(torch.randn_like(param) * rate)
    return new_model

def score_network(network):
    return sim.score(network)

# Example usage:
if __name__ == "__main__":
    # Define a simple PyTorch network with one output neuron:
    print("Creating random networks...")
    network_array = []
    for i in range(100):
        network_array.append(nn.Sequential(
        nn.Linear(4, 16),
        nn.Tanh(),
        nn.Linear(16, 1)
        ).eval())

    print("Random networks created")

    batch_size = 100
    learning_rate = 0.1

    global sim
    sim = PendulumSimulator()
    scores = np.empty(batch_size)

    print("Starting training...")

    for generation in range(1):
        for i in range(batch_size):
            model = torch.jit.script(network_array[i])
            model.save("model.pt")
            score_cpp.load_model("model.pt")
            input_vector = [0.1, 0.2, 0.3, 0.4]
            score = score_cpp.score_nnet(input_vector)
            print("Score:", score)
        top_indices = np.argsort(scores)[-5:]# [::-1]# to reverse the scores
        top_networks = [network_array[i] for i in top_indices]
        for i in range(5):
            network_array[15*i] = deepcopy(top_networks[i])
            for j in range(1,15):
                network_array[15*i+j] = mutate(top_networks[i], learning_rate)
        for i in range(25):
            network_array[i+75] = nn.Sequential(nn.Sequential(
                nn.Linear(4, 16),
                nn.Tanh(),
                nn.Linear(16,1)
            ))

        print(f"Best score: {max(scores)}")
        torch.save(top_networks[4].state_dict(), f'pytorch_models/model_{generation}.pth')

        learning_rate *= 0.9