#include <torch/extension.h>
#include <torch/script.h>  // needed for loading TorchScript models
#include <pybind11/pybind11.h>
#include <pybind11/stl.h>  // for vector binding

#include <iostream>
#include <memory>
#include <cmath>
#include <algorithm>

float pendulum_length = 150;
float g = 0.25;
float friction_factor = 0.995;
float speed_multiplier = 100.0;
float movement_cost = 0.0;
float boundary = 1200.0;

// This function sets the model from file
torch::jit::script::Module load_model(const std::string& path) {
  return torch::jit::load(path);
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

float score_nnet(float startAngle, int iterations, int model_number) {
  float score = 0;
  float x = 0;
  float angle = startAngle;
  float angular_velocity = 0;
  float x_rel = 0.0;
  float y_rel = -pendulum_length;
  float center_vel;
  float center_vel_old = 0.0;
  auto module = load_model("pytorch_models/model_" + std::to_string(model_number) + ".pt");
  module.eval();
  torch::Tensor input = torch::empty({1, 4}, torch::kFloat32);
  std::vector<c10::IValue> input_vector(1);
  torch::NoGradGuard no_grad;
  for(int i = 0; i < 600; i++){
    center_vel_old = center_vel;
    input[0][0] = cos(angle);
    input[0][1] = sin(angle);
    input[0][2] = angular_velocity;
    input[0][3] = center_vel_old;

    input_vector[0] = input[0];
    at::Tensor output = module.forward(input_vector).toTensor();
    center_vel = output.item<float>();
    physics(x, angle, angular_velocity, x_rel, y_rel, center_vel);  // assuming scalar output
    score += std::max(static_cast<float>(0.0), -y_rel);
    score -= 5000.0 * (angular_velocity * angular_velocity);
    score -= movement_cost * std::abs(center_vel);
  }
  return score;
}

// Expose the program to python
PYBIND11_MODULE(score_cpp, m) {
  m.def("load_model", &load_model, "Load a TorchScript model");
  m.def(
  "score_nnet",
  &score_nnet,
  "Score using TorchScript model",
  pybind11::call_guard<pybind11::gil_scoped_release>()  // <<< release GIL
  );
}
