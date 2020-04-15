import org.lwjgl.BufferUtils;
import java.util.*;

public class HitTester {
    public java.nio.ByteBuffer pixels;
    int radius;
    int bg;
    public double lastAngle;

    public class HitTableEntry {
        public int dx, dy; 
        public double l, angle, cx, cy;

        public HitTableEntry(int _dx, int _dy, double _l, double _angle, double _cx, double _cy) {
            dx = _dx;
            dy = _dy;
            l = _l;
            angle = _angle;
            cx = _cx;
            cy = _cy;
        }
    }

    List<HitTableEntry> hitTable = new ArrayList<HitTableEntry>();

    public void createHitTable() {
        hitTable = new ArrayList<HitTableEntry>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                double l, angle = 0, cx, cy;
                boolean skip = false;
                l = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                if (dx == 0) {
                    if (dy != 0) {
                        if (dy > 0) {
                            angle = 90.0;
                        } else {
                            angle = -90.0;
                        } 
                    } else {
                        skip = true;
                    }
                } else {
                    angle = 180.0 * (Math.atan2(dy, dx) / Math.PI);
                }
                cx = CosSineTable.cos((float)angle);
                cy = CosSineTable.sin((float)angle);
                if (!skip && l <= radius) {
                    hitTable.add(new HitTableEntry(dx, dy, l, angle, cx, cy));
                }
            }
        }
    }

    public HitTester(int _radius) {
        radius = _radius;
        bg = 0;
        pixels = BufferUtils.createByteBuffer(4 * (radius*2 + 1) * (radius*2 + 1));
        createHitTable();
    }

    public void setBackground() {
        bg = getPixel((int)radius/2, (int)radius/2);
    }

    public byte getPixel(int x, int y) {
        return pixels.get(4*(x + radius*2*y));
    }

    public double angleDiff(double a, double b) {
        double a2 = a < 0 ? 360.0 + a : a;
        double b2 = b < 0 ? 360.0 + b : b;
        double phi = Math.abs(b2-a2) % 360.0;
        if (phi > 180.0)
            return 360.0 - phi;
        else
            return phi;
    }

    public Double findHit(double _dir) {
        //System.out.println("finding hit " +Double.toString(_dir));
        int cnt = 0;
        double sumCx = 0, sumCy = 0;
        for (HitTableEntry hte : hitTable) {
            byte testPixel = getPixel(radius + hte.dx, radius + hte.dy);
            if (testPixel != bg) {
                //System.out.println("hte found " + Double.toString(hte.cx) + " " + Double.toString(hte.cy));
                sumCx += hte.cx;
                sumCy += hte.cy;
                cnt++;
            }
        }
        if (cnt > 4) {
            double angle = 180 * (Math.atan2(sumCy, sumCx) / Math.PI);
            lastAngle = angle;
            double newDir = (2.0*angle + 180.0 - _dir) % 360.0;
            double diff = angleDiff(angle, newDir);
            if (diff > 90.0) {
                //System.out.println("new dir " + Double.toString(_dir) + " " + Double.toString(newDir));
                return newDir;
            } else {
                //System.out.println("diff, dir, new dir " + Double.toString(diff) + " " + Double.toString(_dir) + " " + Double.toString(newDir));
            }
        }
        return null;
    }
}