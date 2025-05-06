#include <torch/extension.h>
#include <torch/script.h>  // needed for loading TorchScript models
#include <pybind11/pybind11.h>
#include <pybind11/stl.h>  // for vector binding

#include <iostream>
#include <memory>
#include <cmath>
#include <algorithm>

float g = 0.25;
int speedMultiplier = 100;
int score_frequency = 1;
int pendulum_length = 300;
float friction_factor = 0.995;
int movement_cost = 0;

// declare Module jit script
using Module = torch::jit::Module;

torch::jit::script::Module load_model(const std::string& path) {
    return torch::jit::load(path);
}

// unchanged: physics only updates angle, angular_velocity
void physics(float& angle, float& angular_velocity, float accel){
    angular_velocity += (g / pendulum_length) * cos(angle);
    angular_velocity += (accel / pendulum_length) * sin(angle);
    angular_velocity *= friction_factor;
    angle += angular_velocity;
}

float score_nnet(float startAngle, int iterations, torch::jit::Module module) {
    float score = 0.0f;
    float x = 0.0f;
    float angle = startAngle;
    float angular_velocity = 0.0f;

    // initialize both to zero, just like in Java
    float action = 0.0f;
    float old_vel = 0.0f;

    module.eval();

    torch::Tensor input = torch::empty({1, 4}, torch::kFloat32);
    std::vector<c10::IValue> input_vector(1);
    torch::NoGradGuard no_grad;

    for (int i = 0; i < iterations; ++i) {
        // build the same input as Java’s updateInput(...)
        input[0][0] = std::cos(angle);
        input[0][1] = std::sin(angle);
        input[0][2] = angular_velocity;
        input[0][3] = old_vel / speedMultiplier;  

        input_vector[0] = input[0];
        float net_out = module.forward(input_vector)
                              .toTensor()
                              .item<float>();
        
        float centerVel = speedMultiplier * net_out;
        float accel     = centerVel - old_vel;
        old_vel = centerVel;

        // cart‐position update + clamp & penalty
        x += centerVel;
        if (x > 1200.0f) {
            x = 1200.0f;
            centerVel = 0.0f;
            score   -= 1000.0f;
        } else if (x < -1200.0f) {
            x = -1200.0f;
            centerVel = 0.0f;
            score   -= 1000.0f;
        }

        // pass accel into your existing physics
        physics(angle, angular_velocity, accel);

        // only accumulate every score_frequency steps
        if (i % score_frequency == 0) {
            score += std::max(0.0f, -pendulum_length*sin(angle));
            score -= 5000.0f * (angular_velocity * angular_velocity);
            score -= movement_cost * std::abs(centerVel);
        }
    }

    return score;
}

PYBIND11_MODULE(score_cpp, m) {
    m.def("load_model", &load_model, "Load a TorchScript model");
    m.def(
    "score_nnet",
    &score_nnet,
    "Score using TorchScript model",
    pybind11::call_guard<pybind11::gil_scoped_release>()  // <<< release GIL
    );
  }