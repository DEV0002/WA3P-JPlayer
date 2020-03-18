/* WA3P Player Version Log
 * 
 * V0.1 WA3P Format Created
 * 		GUI Created
 * 
 * V0.2 Unlimited Files
 * 		Progress Bar
 * 		File Limited to ~3246 Hours of audio (Probably will break around 100 hour mark)
 * 
 * V0.2.1
 * 		Able To Play Properly most of the time*
 * 		*With Random Fuck-Ups
 * 
 * V0.2.2
 * 		Source replaced with decompiled jar after accident, weird optimizations
 * 		Automatically directs to path of file
 * 
 * Future Updates:
 * 		MP3 Support
 * 		AAC Support
 * 		MORE COMPRESSION ON WA3P FILE
 * 		OGG VORBIS Support
 * 		Better Pause System
 * 		Skip to different time
 */

package com.GLS.WA3PP;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

public class Main {
	
   static String song = "Loading";
   
   private CircleButton pp = new CircleButton("Play/Pause");
   private JButton skip = new JButton("Skip");
   private JButton prev = new JButton("Prev");
   private JProgressBar pb = new JProgressBar();
   private JFileChooser fc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
   private static JPanel newPanel;
   private JFrame frame;
   private boolean bPrev = false;
   private boolean bSkip = false;

   private void createFrame() {
      this.frame = new JFrame("WA3P Player V0.2.2");
      this.pb.setMinimum(0);
      this.pb.setStringPainted(false);
      this.frame.setSize(300, 200);
      this.frame.setResizable(false);
      newPanel = new JPanel(new BorderLayout());
      this.skip.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            bSkip = !bSkip;
         }
      });
      this.prev.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            bPrev = !bPrev;
         }
      });
      newPanel.add(this.pp, BorderLayout.CENTER);
      newPanel.add(this.skip, BorderLayout.EAST);
      newPanel.add(this.prev, BorderLayout.WEST);
      newPanel.add(this.pb, BorderLayout.SOUTH);
      newPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Current Song: " + song));
      this.frame.add(newPanel);
      this.frame.setDefaultCloseOperation(3);
      this.frame.setLocationRelativeTo((Component)null);
      this.frame.setVisible(true);
   }

   public Main() {
      File[] args = null;
      fc.setDialogTitle("Choose Audio Files or a WA3P File: ");
      fc.setMultiSelectionEnabled(true);
      fc.setFileSelectionMode(0);
      fc.setAcceptAllFileFilterUsed(false);
      fc.setCurrentDirectory(new File("."));
      FileNameExtensionFilter f1 = new FileNameExtensionFilter("WAV, AAC, MP3 Files", new String[]{"wav", "aac", "mp3"});
      fc.addChoosableFileFilter(f1);
      fc.setFileFilter(new FileNameExtensionFilter("WA3P File", new String[]{"wa3p"}));
      int rv = fc.showOpenDialog(null);
      if (rv == 0)
         args = fc.getSelectedFiles();
      try {
         if (args == null)
            throw new FileNotFoundException("Please Select Files!");
         this.createFrame();
         String playlist = args[0].getAbsolutePath();
         if (!playlist.endsWith(".wa3p"))
            playlist = writeWA3P(args);
         long[] files = readWA3P(playlist);
         int[] order = new int[files.length];
         for(int i = 0; i < order.length; order[i] = i++);
         shuffle(order);
         boolean play = true;
         boolean shuffle = true;
         boolean loop = true;
         int i = 0;
         while(play) {
            WA3 playing = readWA3(playlist, files[order[i]]);
            song = new String(playing.audioName);
            newPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Current Song: " + song));
            AudioFormat af = playing.af;
            int SAMPLE_RATE = playing.SAMPLE_RATE;
            int FS = SAMPLE_RATE * af.getChannels() * (af.getSampleSizeInBits() / 8);
            byte[] audioData;
            for(audioData = playing.PWM; audioData.length % af.getFrameSize() != 0; audioData = Arrays.copyOf(audioData, audioData.length+1));
            pb.setMaximum(audioData.length);
            i++;
            SourceDataLine line = AudioSystem.getSourceDataLine(af);
            line.open(af, FS);
            line.start();
            boolean playin = true;
            int offset = 0;
            while(playin) {
               if (playin && !pp.mousePressed) {
                  line.write(audioData, offset, SAMPLE_RATE);
                  offset += SAMPLE_RATE;
                  pb.setValue(offset);
               }
               if (this.bSkip) {
                  this.bSkip = false;
                  break;
               }
               if (this.bPrev) {
                  this.bPrev = false;
                  i -= 2;
                  break;
               }
               if (offset + SAMPLE_RATE >= audioData.length)
                  break;
            }
            line.stop();
            this.pp.mousePressed = false;
            playin = false;
            if (i >= files.length) {
               i = 0;
               if (shuffle)
                  shuffle(order);
               if (!loop)
                  play = false;
            }
            if (i == -1)
               i = order.length - 1;
            line.drain();
            line.close();
         }
      } catch (Exception e) {
         e.printStackTrace();
         JOptionPane.showMessageDialog(this.frame, "Exception!\n" + e.getMessage());
         System.exit(-1);
      }

   }

   public static byte[] readRaw16BPWM(String file) {
      try {
         InputStream is = new FileInputStream(file);
         byte[] PWM = is.readAllBytes();
         is.close();
         return PWM;
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static WA3 readWA3(String file, long offset) {
      try {
         InputStream is = new FileInputStream(file);
         is.skip(offset);
         int cFS = ByteBuffer.wrap(is.readNBytes(4), 0, 4).getInt();
         byte[] data = CompressionUtils.decompress(is.readNBytes(cFS));
         WA3 wa3 = new WA3(data, data.length);
         is.close();
         return wa3;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public static long[] readWA3P(String file) {
      try {
         InputStream is = new FileInputStream(file);
         long offset = 0L;
         int nOF = ByteBuffer.wrap(is.readNBytes(2), 0, 2).getShort();
         float tFS = ByteBuffer.wrap(is.readNBytes(4), 0, 4).getFloat();
         offset += 6L;
         float fs = 0.0F;
         int cF = 0;
         
         long[] aF;
         int cFS;
         for(aF = new long[nOF]; fs < tFS; offset += (long)(cFS + 4)) {
            cFS = ByteBuffer.wrap(is.readNBytes(4), 0, 4).getInt();
            fs += (float)cFS / 1024.0F;
            is.readNBytes(cFS);
            aF[cF] = offset;
            cF++;
         }
         is.close();
         return aF;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public static String writeWA3P(File... files) {
      try {
         ByteArrayOutputStream bs = new ByteArrayOutputStream();
         float tfs = 0.0F;
         File[] var6 = files;
         int var5 = files.length;

         File file;
         for(int var4 = 0; var4 < var5; ++var4) {
            file = var6[var4];
            InputStream is = new FileInputStream(file);
            byte[] data = is.readAllBytes();
            ByteArrayOutputStream bs1 = new ByteArrayOutputStream();
            if (file.getAbsolutePath().endsWith(".wav")) {
               String[] split = file.getAbsolutePath().split("\\\\");
               byte[] header = StandardCharsets.US_ASCII.encode(CharBuffer.wrap(split[split.length - 1].replaceAll(".wav", "  ").toCharArray())).array();
               song = "Writing " + new String(header, "US-ASCII");
               newPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), "Current Song: " + song));
               bs1.write(ByteBuffer.allocate(2).putShort((short)header.length).array());
               bs1.write(header);
               bs1.write(1);
            }
            bs1.write(data);
            byte[] compData = CompressionUtils.compress(bs1.toByteArray());
            bs.write(ByteBuffer.allocate(4).putInt(compData.length).array());
            tfs += (float)compData.length / 1024.0F;
            bs.write(compData);
            is.close();
         }
         file = new File("playlist.wa3p");
         file.createNewFile();
         FileOutputStream fr = new FileOutputStream(file);
         byte[] tFL = ByteBuffer.allocate(2).putShort((short)files.length).array();
         fr.write(tFL);
         byte[] tFSB = ByteBuffer.allocate(4).putFloat(tfs).array();
         fr.write(tFSB);
         bs.writeTo(fr);
         fr.flush();
         fr.close();
         return file.getAbsolutePath();
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   public static void main(String[] args) {
      new Main();
   }

   public static void shuffle(int[] a) {
      int n = a.length;

      for(int i = 0; i < n; ++i) {
         int r = i + (int)(Math.random() * (double)(n - i));
         int tmp = a[i];
         a[i] = a[r];
         a[r] = tmp;
      }

   }
}
