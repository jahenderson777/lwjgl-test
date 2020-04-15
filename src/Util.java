
public class Util {
    static float hue2rgb(float p, float  q, float  t) {
        if (t < 0) t += 1.0f;
        if (t > 1) t -= 1.0f;
        if (t < 0.166666666f) return p + (q - p) * 6.0f * t;
        if (t < 0.5f) return q;
        if (t < 0.666666666f) return p + (q - p) * (0.666666666f - t) * 6.0f;
        return p;
    }

    public static byte[] hslToRgb(float h, float  s, float  l) {
        float r, g, b;

        if (s == 0) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5f ? l * (1.0f + s) : l + s - l * s;
            var p = 2.0f * l - q;

            r = Util.hue2rgb(p, q, h + 0.33333333333f);
            g = hue2rgb(p, q, h);
            b = hue2rgb(p, q, h - 0.33333333333f);
        }

        return new byte[]{ (byte)(r * 255.0f), (byte)(g * 255.0f), (byte)(b * 255.0f)} ;
    }

    public static float[] rgbToHsl(byte _r, byte _g, byte _b) {
        float r = (float)(_r & 0xFF)/255.0f;
        float g = (float)(_g & 0xFF)/255.0f;
        float b = (float)(_b & 0xFF)/255.0f;
      
        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);
        float h = 0.0f, s, l = (max + min) / 2.0f;
      
        if (max == min) {
          h = s = 0.0f; // achromatic
        } else {
          float d = max - min;
          s = l > 0.5f ? d / (2.0f - max - min) : d / (max + min);

          if (max == r) {
            h = (g - b) / d + (g < b ? 6.0f : 0.0f);
          } else 
          if (max == g) {
            h = (b - r) / d + 2.0f;
          } else 
          if (max == b) {
            h = (r - g) / d + 4.0f;
          }
      
          h = h/6.0f;
        }
      
        return new float[] { h, s, l };
      }

    public static String foo() {
        return "bar";
    }
}