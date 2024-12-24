package application;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.BasicStroke;

import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class CustomMapMarker extends MapMarkerDot {

    private Color innerColor;
    private int size;
    private int borderWidth; // Độ dày của viền đen

    public CustomMapMarker(double lat, double lon, Color innerColor, int size, int borderWidth) {
        super(lat, lon);
        this.innerColor = innerColor;
        this.size = size;
        this.borderWidth = borderWidth; // Khởi tạo độ dày viền
    }

    @Override
    public void paint(Graphics g, Point position, int radius) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Tính toán kích thước vòng tròn trắng (dựa vào size)
        int whiteCircleRadius = (int) (size * 0.4);

        // Tính toán kích thước vòng tròn đen (dựa vào vòng tròn trắng + độ dày viền)
        int borderSize = whiteCircleRadius * 2 + borderWidth * 2; // Đường kính + 2 lần độ dày viền

        // Vẽ vòng tròn ngoài (viền đen)
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(borderWidth)); // Sử dụng borderWidth trực tiếp
        g2d.drawOval(position.x - borderSize / 2, position.y - borderSize / 2, borderSize, borderSize);

        // Vẽ vòng tròn trắng ở giữa
        g2d.setColor(Color.WHITE);
        g2d.fillOval(position.x - whiteCircleRadius, position.y - whiteCircleRadius, whiteCircleRadius * 2, whiteCircleRadius * 2);

        // Vẽ vòng tròn màu bên trong cùng
        int innerCircleRadius = (int) (whiteCircleRadius * 0.75); // Dựa vào vòng tròn trắng
        g2d.setColor(innerColor);
        g2d.fillOval(position.x - innerCircleRadius, position.y - innerCircleRadius, innerCircleRadius * 2, innerCircleRadius * 2);

        g2d.dispose();
    }
}