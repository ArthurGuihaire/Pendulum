#!/usr/bin/env python3
# file: fix_state_dict.py
"""
Convert a state_dict saved from a double‑wrapped nn.Sequential into one that
matches a flat Sequential of Linear‑Tanh‑Linear‑Tanh.

Example
-------
$ python fix_state_dict.py \
      pytorch_models/model_17.pt \
      pytorch_models/model_17_fixed.pt
"""

import argparse
import torch

def strip_outer_prefix(state_dict):
    """
    Remove the first 'chunk.' from every key: '0.0.weight' → '0.weight'.
    Works for any outer index ('3.', '12.', …).
    """
    new_sd = {}
    for k, v in state_dict.items():
        # split once on the first dot only
        head, dot, tail = k.partition(".")
        if dot:                       # key had at least one '.'
            new_sd[tail] = v          # keep everything after the first '.'
        else:
            new_sd[k] = v             # no change needed
    return new_sd

def main():
    parser = argparse.ArgumentParser(
        description="Fix state_dict keys produced by an extra nn.Sequential wrapper"
    )
    parser.add_argument("input",  help="path to the bad .pt file")
    parser.add_argument("output", help="where to save the cleaned .pt file")
    args = parser.parse_args()

    bad_state = torch.load(args.input, map_location="cpu")
    good_state = strip_outer_prefix(bad_state)

    torch.save(good_state, args.output)
    print(f"✓ cleaned state_dict ({len(good_state)} tensors) → {args.output}")

if __name__ == "__main__":
    main()
