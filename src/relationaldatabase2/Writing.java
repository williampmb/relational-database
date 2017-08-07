/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relationaldatabase2;

import java.io.IOException;
import java.io.RandomAccessFile;
import relationaldatabase2.strucutres.Bucket;
import relationaldatabase2.strucutres.Directory;

/**
 *
 * @author williampmb
 */
public class Writing {

    public static void writeBucket(RandomAccessFile w, Bucket b, long bucketAddress) throws IOException {
        w.seek(bucketAddress);
        w.writeInt(b.getCount());

        w.writeShort(b.getNumBits());

        for (int i = 0; i < b.getPk().length; i++) {

            w.writeInt(b.getPk()[i]);

            w.writeLong(b.getAddress()[i]);

        }

    }

    public static void writeDirectory(RandomAccessFile w, Directory d) throws IOException {

        for (int i = 0; i < d.getAddres().length; i++) {
            w.writeLong(d.getAddres()[i]);
        }

    }

    public static void writeHeaderInd(RandomAccessFile w, short bucketSize, short numBits, long directoryAddress) throws IOException {
        w.seek(0);
        w.writeShort(bucketSize);
        w.writeShort(numBits);
        w.writeLong(directoryAddress);

    }

    public static void writeDirectory(RandomAccessFile f, int numBits, long posIni, Directory d) throws IOException {
        long cont = posIni;
        int i = 0;
        int a = 1;
        int numItensDirectory = 0;
        int c = ((int) Math.pow(2, numBits));
        while (cont != 0) {
            f.seek(cont);

            while (numItensDirectory != ((int) Math.pow(2, a))) {
                System.out.println("d[" + i + "]" + " - " + d.getAddressBucket(i) + " normal adding");
                f.writeLong(d.getAddressBucket(i));
                numItensDirectory++;
                i++;
            }
            a++;
            long ultLong = f.getFilePointer();
            cont = f.readLong();
            if (numItensDirectory != ((int) Math.pow(2, numBits)) && cont == 0) {

                long eof = f.length();
                long ultimolong2 = f.getFilePointer() - 8;
                f.seek(ultimolong2);
                f.writeLong(eof);
                f.seek(eof);
                while (numItensDirectory != ((int) Math.pow(2, numBits))) {
                    System.out.println("d[" + i + "]" + " - " + d.getAddressBucket(i) + " normal adding");

                    f.writeLong(d.getAddressBucket(i));
                    i++;
                    numItensDirectory++;
                }
                f.writeLong(0);
                cont = 0;
            }
            System.out.println("");
        }
        long[] head = Reading.readHeaderInd(f);
        short numBitsDirectory = (short) numBits;
        System.out.println("write header: num bits: " + numBits);
        short bucketSize = (short) head[0];
        Writing.writeHeaderInd(f, bucketSize, numBitsDirectory, head[2]);
    }

}
