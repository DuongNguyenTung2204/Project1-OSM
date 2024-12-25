package application;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class RequestMapMarker extends MapMarkerDot{
	private Color outerColor; // Màu của hình tròn lớn (đỏ)
    private int size; // Kích thước tổng thể của marker

    public RequestMapMarker(double lat, double lon, Color outerColor, int size) {
        super(lat, lon);
        this.outerColor = outerColor;
        this.size = size;
    }

    @Override
    public void paint(Graphics g, Point position, int radius) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Tính kích thước các phần
        int circleRadius = size / 2; // Bán kính hình tròn lớn (đỏ)
        int innerCircleRadius = (int) (circleRadius * 0.4); // Bán kính hình tròn nhỏ (trắng)
        int stemWidth = size / 10; // Độ rộng của thân (đường thẳng)
        int stemHeight = size; // Chiều cao của thân

        // Tính lại vị trí
        int adjustedY = position.y - (circleRadius + stemHeight); // Điều chỉnh Y để đáy thân trùng với vị trí tọa độ

        // Vẽ thân (đường thẳng màu đỏ)
        g2d.setColor(outerColor);
        int stemX = position.x - stemWidth / 2;
        int stemY = adjustedY + circleRadius;
        g2d.fillRect(stemX, stemY, stemWidth, stemHeight);

        // Vẽ hình tròn lớn (đỏ)
        g2d.setColor(outerColor);
        g2d.fillOval(position.x - circleRadius, adjustedY, circleRadius * 2, circleRadius * 2);

        // Vẽ hình tròn nhỏ (trắng) ở giữa
        g2d.setColor(Color.WHITE);
        g2d.fillOval(position.x - innerCircleRadius, adjustedY + circleRadius - innerCircleRadius, innerCircleRadius * 2, innerCircleRadius * 2);

        g2d.dispose();
    }

}
