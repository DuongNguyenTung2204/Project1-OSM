package application;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

public class CustomMapViewer extends JMapViewer {
	private List<Coordinate> routePoints;

    // Hàm thiết lập danh sách điểm
    public void setRoutePoints(List<Coordinate> routePoints) {
        this.routePoints = routePoints;
        repaint(); // Vẽ lại bản đồ
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Nếu chưa có danh sách điểm thì không vẽ
        if (routePoints == null || routePoints.size() < 2) {
            return;
        }

        // Vẽ từng đoạn thẳng nối các điểm
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE); // Màu của đường
        g2d.setStroke(new BasicStroke(4)); // Độ dày đường

        for (int i = 1; i < routePoints.size(); i++) {
            // Lấy tọa độ điểm i-1 và i
            Coordinate start = routePoints.get(i - 1);
            Coordinate end = routePoints.get(i);

            // Chuyển đổi tọa độ địa lý sang tọa độ pixel trên màn hình
            Point startPoint = getMapPosition(start.getLat(), start.getLon(), false);
            Point endPoint = getMapPosition(end.getLat(), end.getLon(), false);

            // Vẽ đoạn thẳng nếu cả hai điểm hợp lệ
            if (startPoint != null && endPoint != null) {
                g2d.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
            }
        }
    }
}
