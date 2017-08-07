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
public class Reading {

    public static long[] readHeaderInd(RandomAccessFile f) throws IOException {
        f.seek(0);
        short bucketSize = f.readShort();
        short numBits = f.readShort();
        long directoryAddress = f.readLong();

        long[] header = new long[]{bucketSize, numBits, directoryAddress};

        return header;
    }

    public static String readHeadTab(RandomAccessFile f) throws IOException {
        f.seek(0);
        String result = f.readUTF();
        return result;
    }

    public static Bucket readBucket(RandomAccessFile f, int bucketSize, long bucketAddress) throws IOException {
        f.seek(bucketAddress);
        int[] pk = new int[bucketSize];
        long[] address = new long[bucketSize];
        int count = f.readInt();
        short numBitsBucket = f.readShort();

        for (int i = 0; i < bucketSize; i++) {
            pk[i] = f.readInt();
            address[i] = f.readLong();
        }
        return new Bucket(count, numBitsBucket, bucketSize, pk, address);
    }

    public static String readTabRow(RandomAccessFile tabFile, long rowAddress) throws IOException {
        String result = "";
        tabFile.seek(0);
        String[] cols = tabFile.readUTF().split(",");
        tabFile.seek(rowAddress);
        for (int i = 0; i < cols.length; i++) {
            switch (cols[i]) {
                case "primaryKey":
                    int pk = tabFile.readInt();
                    result = result + "primary key: " + pk;
                    break;
                case "int":
                    int colInt = tabFile.readInt();
                    result = result + " ColInt[" + i + "]:" + colInt;

                    break;
                case "str":
                    String colStr = tabFile.readUTF();
                    result = result + " ColStr[" + i + "]:" + colStr;

                    break;
                default:
                    System.out.println("default");
                    //throw an erro invalid type broke file
                    return null;
            }
        }

        return result;
    }
    
    public static Directory getDirectoryFromFile(RandomAccessFile f, int numBits, long posIni) throws IOException {
        long[] directory = new long[((int) Math.pow(2, numBits))];
        long cont = posIni;
        int i = 0;
        int a = 1;
        int numItensDirectory = 0;

        while (cont != 0) {
            f.seek(cont);

            while (numItensDirectory != ((int) Math.pow(2, a))) {
                directory[i] = f.readLong();
                numItensDirectory++;
                i++;
            }
            a++;
            cont = f.readLong();
        }

        return new Directory(directory);
    }

}
