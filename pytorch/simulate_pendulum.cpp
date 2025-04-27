#include <torch/extension.h>
#include <torch/script.h>  // needed for loading TorchScript models
#include <pybind11/pybind11.h>
#include <pybind11/stl.h>  // for vector binding

#include <iostream>
#include <memory>

torch::jit::script::Module module;

// This function sets the model from file
void load_model(const std::string& path) {
    module = torch::jit::load(path);
    module.eval();
}

float score_nnet(float startAngle) {
    float score = 0;
    float x = 0;
    float angle = startAngle;
    float angularVel = 0;
    torch::Tensor input_tensor = torch::tensor(input_vec).unsqueeze(0); // add batch dim
    torch::Tensor output = module.forward({input_tensor}).toTensor();
    return output.item<float>();  // assuming scalar output
}

PYBIND11_MODULE(score_cpp, m) {
    m.def("load_model", &load_model, "Load a TorchScript model");
    m.def("score_nnet", &score_nnet, "Score using TorchScript model");
}

