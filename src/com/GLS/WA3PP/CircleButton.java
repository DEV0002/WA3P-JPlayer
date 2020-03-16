package com.GLS.WA3PP;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JButton;

public class CircleButton extends JButton {
   private static final long serialVersionUID = -3281796891100400086L;
   private boolean mouseOver = false;
   public boolean mousePressed = false;

   public CircleButton(String text) {
      super(text);
      this.setOpaque(false);
      this.setFocusPainted(false);
      this.setBorderPainted(false);
      MouseAdapter mouseListener = new MouseAdapter() {
         public void mousePressed(MouseEvent me) {
            if (CircleButton.this.contains(me.getX(), me.getY())) {
               CircleButton.this.mousePressed = !CircleButton.this.mousePressed;
               CircleButton.this.repaint();
            }

         }

         public void mouseReleased(MouseEvent me) {
            CircleButton.this.repaint();
         }

         public void mouseExited(MouseEvent me) {
            CircleButton.this.mouseOver = false;
            CircleButton.this.repaint();
         }

         public void mouseMoved(MouseEvent me) {
            CircleButton.this.mouseOver = CircleButton.this.contains(me.getX(), me.getY());
            CircleButton.this.repaint();
         }
      };
      this.addMouseListener(mouseListener);
      this.addMouseMotionListener(mouseListener);
   }

   private int getDiameter() {
      int diameter = Math.min(this.getWidth(), this.getHeight());
      return diameter;
   }

   public Dimension getPreferredSize() {
      FontMetrics metrics = this.getGraphics().getFontMetrics(this.getFont());
      int minDiameter = 10 + Math.max(metrics.stringWidth(this.getText()), metrics.getHeight());
      return new Dimension(minDiameter, minDiameter);
   }

   public boolean contains(int x, int y) {
      int radius = this.getDiameter() / 2;
      return Point2D.distance((double)x, (double)y, (double)(this.getWidth() / 2), (double)(this.getHeight() / 2)) < (double)radius;
   }

   public void paintComponent(Graphics g) {
      int diameter = this.getDiameter();
      int radius = diameter / 2;
      if (this.mousePressed) {
         g.setColor(Color.LIGHT_GRAY);
      } else {
         g.setColor(Color.WHITE);
      }

      g.fillOval(this.getWidth() / 2 - radius, this.getHeight() / 2 - radius, diameter, diameter);
      if (this.mouseOver) {
         g.setColor(Color.BLUE);
      } else {
         g.setColor(Color.BLACK);
      }

      g.drawOval(this.getWidth() / 2 - radius, this.getHeight() / 2 - radius, diameter, diameter);
      g.setColor(Color.BLACK);
      g.setFont(this.getFont());
      FontMetrics metrics = g.getFontMetrics(this.getFont());
      int stringWidth = metrics.stringWidth(this.getText());
      int stringHeight = metrics.getHeight();
      g.drawString(this.getText(), this.getWidth() / 2 - stringWidth / 2, this.getHeight() / 2 + stringHeight / 4);
   }
}
