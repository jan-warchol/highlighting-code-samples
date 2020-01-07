from colormath.color_objects import LabColor, sRGBColor
from colormath.color_conversions import convert_color


class Palette(object):
    """Build a palette from bg, fg, lightness data and optional accent colors.

    base_shades: colors derived from background and foreground (in CIE Lab).
    accent_colors: "special" colors like "blue", with variants (in CIE Lab).
    rgb_values(): returns a dict mapping color names to hex sRGB coords."""

    def __init__(self, background, foreground, name=None):
        """Initialize with base shades. Accent colors may remain empty."""
        self.name = name
        self.bg = LabColor(*background, illuminant='d50')
        self.fg = LabColor(*foreground, illuminant='d50')
        self.contrast = self.fg.lab_l - self.bg.lab_l

        self.base_shades = {}
        self.accent_colors = {}

    @classmethod
    def load_from_module(cls, module_name):
        """Init from module containing specs, with accent colors if present."""
        import importlib
        config = importlib.import_module(module_name)

        print("Creating palette {}...".format(config.name))
        result = cls(config.background,
                     config.foreground,
                     config.name)

        try:  # accent colors are optional
            result.build_variants(config.colors, config.color_variants)
            print("Generated {} colors with {} variant(s) each.".format(
                len(config.colors),
                len(config.color_variants) + 1))
        except AttributeError:
            print("No information about accent colors, skipping.")

        return result

    def build_variants(self, colors, variants):
        """Add accent variants with different lightness/saturation."""
        for name, coords in colors.items():
            self.accent_colors[name] = coords
            l, a, b = coords
            for suffix, (brightness, saturation) in variants.items():
                a = a * saturation
                b = b * saturation
                # Accents may have different lightness than foreground.
                # Adjust desaturated variants for smooth transition.
                l = l * saturation + self.fg.lab_l * (1 - saturation)
                l = min(l * brightness, 100)
                self.accent_colors[name + "_" + suffix] = (l, a, b)

    def lab_colors(self):
        return {
            name: LabColor(*coords, illuminant='d50')
            for name, coords in self.all_colors().items()
        }


if __name__ == "__main__":
    import sys

    if len(sys.argv) < 2:
        print("Missing argument!"); sys.exit()

    print(Palette.load_from_path(sys.argv[1]).rgb_values())
