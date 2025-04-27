#include <torch/extension.h>
#include <torch/script.h>  // needed for loading TorchScript models
#include <pybind11/pybind11.h>
#include <pybind11/stl.h>  // for vector binding

#include <iostream>
#include <memory>
#include <cmath>
#include <algorithm>

int iterations = 600;
float pendulum_length = 150;
float g = 0.25;
float friction_factor = 0.995;
float speed_multiplier = 100.0;
float movement_cost = 0.0;
float boundary = 1200.0

torch::jit::script::Module module;

// This function sets the model from file
void load_model(const std::string& path) {
    module = torch::jit::load(path);
    module.eval();
}

void physics(float& x, float& angle, float& angular_velocity, float& x_rel, float& y_rel, float& input){
    angular_velocity += (g / pendulum_length) * cos(angle);
    angular_velocity += (input / pendulum_length) * sin(angle);
    angular_velocity *= friction_factor;
    angle += angular_velocity;

    // Update relative positions
    x_rel = pendulum_length * cos(angle);
    y_rel = pendulum_length * sin(angle);
}

float score_nnet(float startAngle) {
    float score = 0;
    float x = 0;
    float angle = startAngle;
    float angular_velocity = 0;
    float x_rel = 0.0;
    float y_rel = -pendulum_length;
    float input;
    for(int i = 0; i < 600; i++){
        torch::Tensor input_tensor = torch::tensor(input_vec).unsqueeze(0); // add batch dim
        torch::Tensor output = module.forward({input_tensor}).toTensor();
        input = output.item<float>();
        physics(x, angle, angular_velocity, x_rel, y_rel, input);  // assuming scalar output
        score += std::max(static_cast<float>(0.0), -y_rel);
        score -= 5000.0 * (angular_velocity * angular_velocity);
        score -= movement_cost * std::abs(input);
    }
    return score;
}

PYBIND11_MODULE(score_cpp, m) {
    m.def("load_model", &load_model, "Load a TorchScript model");
    m.def("score_nnet", &score_nnet, "Score using TorchScript model");
}           