#!/usr/bin/env sage
# -*- coding: utf-8 -*-
"""
Compute order and generator G for E: y^2 = x^3 + ax + b (mod p)
Output: JSON one-line for easy parsing in Java
Usage: sage compute_order_complete.py <p_hex> <a_hex> <b_hex>
"""

import sys, json
from sage.all import *

def parse_hex(value_str):
    """Accept hex or signed decimal (e.g., '-3')."""
    s = str(value_str).strip()
    if s.startswith('-') and s[1:].isdigit():
        return int(s, 10)
    if s.startswith(('0x','0X')):
        return int(s, 16)
    try:
        return int(s, 16)
    except ValueError:
        return int(s, 10)

def compute_curve_complete(p, a, b):
    # Reduce a,b mod p
    a_mod = a % p
    b_mod = b % p

    # (optional) sanity: discriminant != 0
    disc = (4 * a_mod^3 + 27 * b_mod^2) % p
    if disc == 0:
        return {
            "error": "Singular curve (discriminant = 0)",
            "n": None, "h": None, "Gx": None, "Gy": None,
            "Ncurve": None, "n_is_prime": False, "h_is_one": False
        }

    F = GF(p)
    E = EllipticCurve(F, [a_mod, b_mod])

    Ncurve = E.order()

    # Strict: require h = 1 (i.e., Ncurve prime)
    if not is_prime(Ncurve):
        return {
            "error": "Ncurve is not prime (h != 1)",
            "n": None, "h": None, "Gx": None, "Gy": None,
            "Ncurve": hex(Ncurve)[2:],
            "n_is_prime": False, "h_is_one": False
        }

    n = Ncurve
    h = 1
    n_is_prime = True
    h_is_one = True

    # Find generator G (group order prime ⇒ any non-zero point has order n)
    G = None
    attempts = 0
    max_attempts = 200
    while G is None and attempts < max_attempts:
        P = E.random_point()
        if not P.is_zero():
            G = P
            break
        attempts += 1

    if G is None:
        return {
            "error": "Failed to find generator after {} attempts".format(max_attempts),
            "n": None, "h": None, "Gx": None, "Gy": None,
            "Ncurve": hex(Ncurve)[2:], "n_is_prime": True, "h_is_one": True
        }

    Gx = int(G[0])
    Gy = int(G[1])

    return {
        "n": hex(n)[2:],   # no 0x prefix
        "h": h,            # number, not string
        "Gx": hex(Gx)[2:],
        "Gy": hex(Gy)[2:],
        "Ncurve": hex(Ncurve)[2:],
        "n_is_prime": n_is_prime,
        "h_is_one": h_is_one
        # no error field on success
    }

if __name__ == "__main__":
    if len(sys.argv) < 4:
        print("Usage: sage compute_order_complete.py <p_hex> <a_hex> <b_hex>")
        sys.exit(1)
    try:
        p = parse_hex(sys.argv[1])
        a = parse_hex(sys.argv[2])
        b = parse_hex(sys.argv[3])

        result = compute_curve_complete(p, a, b)
        # one-line JSON for robust parsing
        print(json.dumps(result))
    except Exception as e:
        error_result = {
            "error": str(e),
            "n": None, "h": None, "Gx": None, "Gy": None,
            "Ncurve": None, "n_is_prime": False, "h_is_one": False
        }
        print(json.dumps(error_result))
        sys.exit(1)




