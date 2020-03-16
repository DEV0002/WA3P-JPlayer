package com.GLS.WA3PP;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;

public class WA3 {
   int tFS;
   char[] audioName;
   byte audioFormat;
   int SAMPLE_RATE;
   AudioFormat af;
   byte[] PWM;

   public WA3(byte[] file, int tFS) throws UnsupportedEncodingException {
      this.tFS = tFS;
      int aHS = ByteBuffer.wrap(file, 0, 2).getShort();
      String text = "";

      text = new String(Arrays.copyOfRange(file, 2, aHS), "US-ASCII");

      this.audioName = text.toCharArray();
      int offset = 2 + aHS;
      byte audioFormat = file[offset];
      offset++;
      if ((audioFormat & 0xFF) == 1)
         PWM = this.readWav(Arrays.copyOfRange(file, offset, file.length));
      else
         throw new UnsupportedEncodingException("Unsupported Version");
      SAMPLE_RATE = (int)af.getSampleRate();
   }

   public byte[] readWav(byte[] file) {
      try {
         InputStream is = new ByteArrayInputStream(file);
         is.skip(20);
         byte[] format = is.readNBytes(16);
         this.af = new AudioFormat((float)ByteBuffer.wrap(format, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt(), (format[15] & 0xFF) << 8 | format[14] & 0xFF, (format[3] & 0xFF) << 8 | format[2] & 0xFF, true, false);
         byte[] PWM = is.readAllBytes();
         return PWM;
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }
}
