#!/usr/bin/env python3
import argparse
import math
import torch
import torch.nn as nn
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
from matplotlib.patches import Rectangle, Circle

# Physics constants: angle=0 points rightward
g = 0.25
speed_multiplier = 100
pendulum_length = 300
friction_factor = 0.995
movement_cost = 0
score_frequency = 1

# Physics update
def physics(angle, angular_velocity, accel):
    angular_velocity += (g / pendulum_length) * math.cos(angle)
    angular_velocity += (accel / pendulum_length) * math.sin(angle)
    angular_velocity *= friction_factor
    angle += angular_velocity
    return angle, angular_velocity

# Run simulation and compute trajectory & score
def simulate(model, start_angle, iterations):
    x = angle = angular_velocity = old_vel = 0.0
    angle = start_angle
    score = 0.0

    x_list, angle_list = [], []
    model.eval()
    with torch.no_grad():
        for i in range(iterations):
            x_list.append(x)
            angle_list.append(angle)

            inp = torch.tensor([[math.cos(angle), math.sin(angle), angular_velocity, old_vel / speed_multiplier]], dtype=torch.float32)
            net_out = model(inp).item()
            center_vel = speed_multiplier * net_out
            accel = center_vel - old_vel
            old_vel = center_vel

            x += center_vel
            if x > 1200.0:
                x, center_vel, score = 1200.0, 0.0, score - 1000.0
            elif x < -1200.0:
                x, center_vel, score = -1200.0, 0.0, score - 1000.0

            angle, angular_velocity = physics(angle, angular_velocity, accel)

            if i % score_frequency == 0:
                score += max(0.0, -pendulum_length * math.sin(angle))
                score -= 5000.0 * (angular_velocity ** 2)
                score -= movement_cost * abs(center_vel)

    return x_list, angle_list, score

# Create animation and return FuncAnimation object
def create_animation(x_list, angle_list):
    fig, ax = plt.subplots()
    ax.set_xlim(-1300, 1300)
    ax.set_ylim(-350, 350)
    ax.set_aspect('equal')

    cart_w, cart_h = 200, 40
    cart = Rectangle((0, -cart_h), cart_w, cart_h, fc='blue', ec='black')
    bob = Circle((0, 0), 20, fc='red', ec='black')
    line, = ax.plot([], [], lw=2)
    ax.add_patch(cart)
    ax.add_patch(bob)

    def init():
        cart.set_xy((-cart_w/2, -cart_h))
        bob.center = (0, 0)
        line.set_data([], [])
        return cart, bob, line

    def update(i):
        x, ang = x_list[i], angle_list[i]
        cart.set_xy((x - cart_w/2, -cart_h))
        pivot = (x, 0)
        bob_x = x + pendulum_length * math.cos(ang)
        bob_y = -pendulum_length * math.sin(ang)
        bob.center = (bob_x, bob_y)
        line.set_data([pivot[0], bob_x], [pivot[1], bob_y])
        return cart, bob, line

    anim = FuncAnimation(fig, update, frames=len(x_list), init_func=init, blit=True, interval=20)
    return anim

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Visualize cart-pendulum with saved state_dict models.")
    parser.add_argument("model_path", help="Path to .pt state_dict model file.")
    parser.add_argument("--iterations", type=int, default=600, help="Simulation steps.")
    parser.add_argument("--start_angle", type=float, default=0.0, help="Initial angle in radians; 0 = rightward.")
    args = parser.parse_args()

    # Rebuild network architecture and load weights
    model = nn.Sequential(
        nn.Linear(4, 16), nn.Tanh(),
        nn.Linear(16, 1), nn.Tanh()
    )
    model.load_state_dict(torch.load(args.model_path))

    # Run sim
    xs, angs, score = simulate(model, args.start_angle, args.iterations)
    print(f"Score: {score:.2f}")

    # Animate and display
    anim = create_animation(xs, angs)
    plt.show()
