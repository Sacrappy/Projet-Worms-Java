package Frontend;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
 
 // Simple preview panel used to display a BufferedImage or a placeholder when empty.
    public class ImagePreviewPanel extends JPanel {
        private BufferedImage bgImage, mapImage;
        private final int prefW;
        private final int prefH;

        public ImagePreviewPanel(int width, int height) {
            this.prefW = width;
            this.prefH = height;
            setPreferredSize(new Dimension(width, height));
            setBackground(new Color(50, 50, 50));
        }

        public void setImage(BufferedImage bg, BufferedImage map) {
            this.bgImage =bg;
            this.mapImage =map;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage == null && mapImage == null) {
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial", Font.ITALIC, 20));
                FontMetrics fm = g.getFontMetrics();
                String text = "Preview Area";
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() / 2) + fm.getAscent() / 4;
                g.drawString(text, x, y);
                return;
            } 
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
            }
            if (mapImage != null) {
                g.drawImage(mapImage, 0, 0, getWidth(), getHeight(), null);
            }   
        }
    }