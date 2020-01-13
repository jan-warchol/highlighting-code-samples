import re

def hello(name):
    # Example comment
    return f"hello, {name}\nhow are you?"

class Demo(object):
    def xyz_to_luv(self, _hx_tuple):
        x = float(_hx_tuple[0])
        y = float(_hx_tuple[1])
        z = float(_hx_tuple[2])
        divider = x + 15 * y + 3 * z
        var_u = 4 * x
        var_v = 9 * y
        if divider != 0:
            var_u = var_u / divider
            var_v = var_v / divider
        else:
            var_u = float("nan")
            var_v = float("nan")
        l = _y_to_l(y)
        if l == 0:
            return [0, 0, 0]
        u = 13 * l * (var_u - refU)
        v = 13 * l * (var_v - refV)
        return [l, u, v]
