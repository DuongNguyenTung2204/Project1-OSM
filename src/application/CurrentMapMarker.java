package application;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;

import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class CurrentMapMarker extends MapMarkerDot{
	private Color outerColor; // Màu của ghim định vị (nền)
    private int size; // Kích thước marker

    public CurrentMapMarker(double lat, double lon, Color outerColor, int size) {
        super(lat, lon);
        this.outerColor = outerColor; // Màu nền
        this.size = size;
    }

    @Override
    public void paint(Graphics g, Point position, int radius) {
    	Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Tính kích thước hình tròn bên ngoài (nền đỏ) và bên trong (màu trắng)
        int outerRadius = size / 2; // Bán kính hình tròn lớn (nền)
        int innerRadius = (int) (size * 0.3); // Bán kính hình tròn nhỏ (trắng)

        // Tính chiều cao của tam giác (đuôi ghim)
        int triangleHeight = size;

        // Điều chỉnh vị trí để đầu dưới tam giác trỏ đúng vào marker
        int adjustedY = position.y - (outerRadius + triangleHeight);

        // Vẽ tam giác (đuôi ghim màu đỏ)
        Polygon triangle = new Polygon();
        triangle.addPoint(position.x, position.y); // Đỉnh dưới của tam giác (trỏ vào vị trí marker)
        triangle.addPoint(position.x - outerRadius, adjustedY + outerRadius); // Góc trái
        triangle.addPoint(position.x + outerRadius, adjustedY + outerRadius); // Góc phải
        g2d.setColor(outerColor);
        g2d.fillPolygon(triangle);

        // Vẽ hình tròn lớn (nền đỏ)
        g2d.fillOval(position.x - outerRadius, adjustedY, outerRadius * 2, outerRadius * 2);

        // Vẽ hình tròn nhỏ màu trắng (ở giữa)
        g2d.setColor(Color.WHITE);
        g2d.fillOval(position.x - innerRadius, adjustedY + outerRadius - innerRadius, innerRadius * 2, innerRadius * 2);

        g2d.dispose();
    }
}
