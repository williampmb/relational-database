/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relationaldatabase2.strucutres;

import java.io.IOException;
import java.io.RandomAccessFile;
import static relationaldatabase2.MainController.d;
import relationaldatabase2.Reading;
import relationaldatabase2.Writing;

/**
 *
 * @author williampmb
 */
public class Directory {

    long[] addres;

    public Directory(short numBits) {
        int numDir = (int) Math.pow(2, numBits);
        addres = new long[(int) Math.pow(2, numBits)];
        System.out.println("Directory Size Created : " + addres.length);
        for (int i = 0; i < numDir; i++) {
            this.addres[i] = 0;
        }
    }

    public Directory(long[] address) {

        this.addres = address;
    }

    public long getAddressBucket(int index) {
        return addres[index];
    }

    public void setAddressByIndex(int index, long value) {
        addres[index] = value;
    }

    public long[] getAddres() {
        return addres;
    }

    public void setAddres(long[] addres) {
        this.addres = addres;
    }

  

    public static Directory splitDirectory(RandomAccessFile indFile, Directory d, long directoryAddresTab, short numBitsDirectory) throws IOException {
        int newNumBitsDirectory = numBitsDirectory + 1;

        Directory auxD = new Directory((short) (newNumBitsDirectory));
        for (int n = 0; n < d.getAddres().length; n++) {
            auxD.setAddressByIndex(n, d.getAddres()[n]);
            int a = n + ((int) auxD.getAddres().length / 2);
            auxD.setAddressByIndex(n + ((int) auxD.getAddres().length / 2), d.getAddres()[n]);
        }
        Writing.writeDirectory(indFile, newNumBitsDirectory, directoryAddresTab, auxD);

        return auxD;
    }

    public String toString() {
        String result = "";
        int count = 0;
        for (long s : addres) {
            result = result + "d[" + count + "] = " + s;
            count++;
        }
        return result;
    }

}
