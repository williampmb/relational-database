/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relationaldatabase2.strucutres;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author williampmb
 */
public class Bucket {
    int count;
    short numBits;
    int[] pk;
    long[] address;

    public Bucket(int count, short numBits, int bucketSize) {
        this.count = count;
        this.numBits = numBits;
        pk = new int[bucketSize];
        address = new long[bucketSize];
        for(int i =0; i<bucketSize; i++){
            this.pk[i] =-1;
            this.address[i] = 0;
        }
    }
    
    public Bucket(int count, short numBits, int bucketSize, int[]pk, long[] address) {
        this.count = count;
        this.numBits = numBits;
        this.pk = pk;
        this.address = address;
    }
    
    public int getBucketSizeInBytes(){
        return 4+2+count*4 +count*8;
    }
    
    public void setPkByIndex(int index, int value){
        System.out.println("pk[" + index+"]=" + value);
        pk[index] = value;
    }
    
    public void setAddressByIndex(int index, long value){
        address[index] = value;
    }
    

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public short getNumBits() {
        return numBits;
    }

    public void setNumBits(short numBits) {
        this.numBits = numBits;
    }

    public int[] getPk() {
        return pk;
    }

    public void setPk(int[] pk) {
        this.pk = pk;
    }

    public long[] getAddress() {
        return address;
    }

    public void setAddress(long[] address) {
        this.address = address;
    }
    
    public String toString(){
        String result = "numKeys: " + count + " numBits: " + numBits;
        String keys = "";
        for(int i = 0; i< count; i++){
            keys = keys + "key["+i+"]: " + pk[i] + " address["+i+"]: " +address[i];
        }
        
        result = result + keys;
        return  result;
    }
  
}
