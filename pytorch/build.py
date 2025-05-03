from torch.utils.cpp_extension import load
import os
os.environ["MAX_JOBS"] = "6"
score_cpp = load(name="score_cpp", sources=["simulate_pendulum.cpp"], verbose=True)
