/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relationaldatabase2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import relationaldatabase2.error.ErrorMessage;
import relationaldatabase2.strucutres.Bucket;
import relationaldatabase2.strucutres.Directory;

/**
 *
 * @author williampmb
 */
public class MainController implements Initializable {

    public static Directory d;

    @FXML
    private TextField tfCName;
    @FXML
    private TextField tfCCol1;
    @FXML
    private TextField tfCBucketSize;

    @FXML
    private TextField tfICol1;
    @FXML
    private TextField tfITabName;

    @FXML
    private TextField tfFPK;
    @FXML
    private TextField tfFTabName;

    @FXML
    private TextField tfImPK;
    @FXML
    private TextField tfImTabName;
    @FXML
    private TextField tfFilePath;

    public static String suffixTable = ".tab";
    public static String suffixIndex = ".ind";
    public static short NUM_BITS_OF_HASH_STARTED = 1;
    public static long DIRECTORY_ADDRESS = 2 + 2 + 8;
    public static long SIZE_ITEM_DIRECTORY = 8;
    public static short BUKECT_SIZE = 1;

    int currentBucketSize = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    @FXML
    public void createTable(ActionEvent event) throws IOException {
        String tableName = tfCName.getText();
        String cols = tfCCol1.getText();

        criarTable(tableName, cols);

    }

    @FXML
    public void insertRow(ActionEvent event) throws FileNotFoundException, IOException {

        String tableName = tfITabName.getText();
        String cols = tfICol1.getText();

        insertRowMethod(cols, tableName);
    }

    private boolean checkErrors(String tableName, File indCheck) {
        if (tableName.equals("")) {
            ErrorMessage msg = new ErrorMessage("202: Invalid Table Name.", this);
            msg.show();
            return true;
        }
        if (!indCheck.exists()) {
            ErrorMessage msg = new ErrorMessage("201: Table not exists.", this);
            msg.show();
            return true;
        }
        if (tableName == null) {
            ErrorMessage msg = new ErrorMessage("203: Table name cannot be null.", this);
            msg.show();
            return true;
        }
        return false;
    }

    private void insertRowMethod(String cols, String tableName) throws NumberFormatException, IOException, FileNotFoundException {
        String[] colsToken = cols.split(",");
        int pk = Integer.valueOf(colsToken[0]);

        File indCheck = new File(tableName + suffixIndex);

        if (checkErrors(tableName, indCheck)) {
            return;
        }
        String found = findPk(tableName, pk);
        if (found != null) {
            ErrorMessage msg = new ErrorMessage("101: Primary Key already exits.", this);
            msg.show();
            return;
        }
        d = loadTable(tableName);

        RandomAccessFile indexFile = new RandomAccessFile(tableName + suffixIndex, "rw");
        RandomAccessFile tableFile = new RandomAccessFile(tableName + suffixTable, "rw");

        long[] headIndex = Reading.readHeaderInd(indexFile);
        short bucketSize = (short) headIndex[0];
        short numBitsTab = (short) headIndex[1];
        long directoryAddresTab = headIndex[2];

        int hashPk = pk % ((int) Math.pow(2, numBitsTab));
        long lookBucket = d.getAddressBucket(hashPk);
        Bucket readBucket = Reading.readBucket(indexFile, bucketSize, lookBucket);

        if (readBucket.getCount() == bucketSize) {
            if (readBucket.getNumBits() == numBitsTab) {
                int hashNewKey;
                int hashFirstKey;
                int bitsOld = readBucket.getNumBits();
                hashNewKey = pk % ((int) Math.pow(2, numBitsTab));
                hashFirstKey = readBucket.getPk()[0] % ((int) Math.pow(2, numBitsTab));
                while (hashNewKey == hashFirstKey && readBucket.getCount() == bucketSize) {
                    if (hashFirstKey == -1) {
                        break;
                    }
                    int posIni = hashFirstKey;
                    int posNewBucket = posIni + ((int) Math.pow(2, bitsOld));
//                  
                    long addressFirstHashBucket = d.getAddressBucket(hashFirstKey);
                    for (int i = 0; i < d.getAddres().length; i++) {
                        long firstAddressOfHash = d.getAddres()[i];
                        if (firstAddressOfHash == addressFirstHashBucket) {
                            posIni = i;
                            break;
                        }
                    }
                    d = Directory.splitDirectory(indexFile, d, directoryAddresTab, numBitsTab);
                    long[] headIndUpd = Reading.readHeaderInd(indexFile);
                    bucketSize = (short) headIndUpd[0];
                    numBitsTab = (short) headIndUpd[1];
                    directoryAddresTab = headIndUpd[2];
                    long eof = indexFile.length();
//
                    Bucket createBucket = new Bucket(0, (short) (readBucket.getNumBits() + 1), bucketSize);

                    Writing.writeBucket(indexFile, createBucket, eof);
                    d.setAddressByIndex(posNewBucket, eof);
                    for (int i = posNewBucket; i < d.getAddres().length; i = (int) (i + Math.pow(2, readBucket.getNumBits() + 1))) {
                        d.setAddressByIndex(i, eof);
                    }

                    int[] tempKeys = new int[bucketSize];
                    long[] tempAddress = new long[bucketSize];

                    for (int j = 0; j < bucketSize; j++) {
                        if (readBucket.getCount() != 0) {
                            tempKeys[j] = readBucket.getPk()[j];
                            tempAddress[j] = readBucket.getAddress()[j];
                            readBucket.setPkByIndex(j, -1);
                            readBucket.setAddressByIndex(j, 0);
                            int count = readBucket.getCount() - 1;
                            readBucket.setCount(count);
                        }

                    }
                    readBucket.setNumBits((short) (readBucket.getNumBits() + 1));
                    Writing.writeBucket(indexFile, readBucket, lookBucket);

                    for (int j = 0; j < bucketSize; j++) {
                        Bucket updateBucket;
                        int tempHash = tempKeys[j] % ((int) Math.pow(2, numBitsTab));
                        long pkAddress = tempAddress[j];

                        long addressBucket = d.getAddressBucket(tempHash);
                        updateBucket = Reading.readBucket(indexFile, bucketSize, addressBucket);
                        updateBucket.setPkByIndex(updateBucket.getCount(), tempKeys[j]);
                        updateBucket.setAddressByIndex(updateBucket.getCount(), pkAddress);
                        updateBucket.setCount(updateBucket.getCount() + 1);
                        Writing.writeBucket(indexFile, updateBucket, addressBucket);

                    }
                    bitsOld++;
                    readBucket = Reading.readBucket(indexFile, bucketSize, lookBucket);
                    hashNewKey = pk % ((int) Math.pow(2, numBitsTab));
                    hashFirstKey = readBucket.getPk()[0] % ((int) Math.pow(2, numBitsTab));
                }
                long[] headIndUpd = Reading.readHeaderInd(indexFile);
                bucketSize = (short) headIndUpd[0];
                numBitsTab = (short) headIndUpd[1];
                directoryAddresTab = headIndUpd[2];
                Writing.writeDirectory(indexFile, numBitsTab, directoryAddresTab, d);
                insertRowMethod(cols, tableName);

            } else {
                int hashNewKey;
                int hashFirstKey;
                int bitsOld = readBucket.getNumBits();
                int bitsNew = numBitsTab;
                hashNewKey = pk % ((int) Math.pow(2, numBitsTab));
                hashFirstKey = readBucket.getPk()[0] % ((int) Math.pow(2, numBitsTab));
                while (readBucket.getCount() == bucketSize && readBucket.getNumBits() < numBitsTab) {

                    hashNewKey = pk % ((int) Math.pow(2, numBitsTab));
                    hashFirstKey = readBucket.getPk()[0] % ((int) Math.pow(2, numBitsTab));
                    if (hashFirstKey == -1) {
                        break;
                    }
                    int posIni = hashFirstKey;
                    long addressFirstHashBucket = d.getAddressBucket(hashFirstKey);
                    for (int i = 0; i < d.getAddres().length; i++) {
                        long firstAddressOfHash = d.getAddres()[i];
                        if (firstAddressOfHash == addressFirstHashBucket) {
                            posIni = i;
                            break;
                        }
                    }
                    int posNewBucket = posIni + ((int) Math.pow(2, bitsOld));

                    long eof = indexFile.length();
                    Bucket createBucket = new Bucket(0, (short) (readBucket.getNumBits() + 1), bucketSize);
                    Writing.writeBucket(indexFile, createBucket, eof);
                    d.setAddressByIndex(posNewBucket, eof);

                    for (int i = posNewBucket; i < d.getAddres().length; i = (int) (i + Math.pow(2, readBucket.getNumBits() + 1))) {
                        d.setAddressByIndex(i, eof);
                    }

                    int[] tempKeys = new int[bucketSize];
                    long[] tempAddress = new long[bucketSize];

                    for (int j = 0; j < bucketSize; j++) {
                        if (readBucket.getCount() != 0) {
                            tempKeys[j] = readBucket.getPk()[j];
                            tempAddress[j] = readBucket.getAddress()[j];
                            readBucket.setPkByIndex(j, -1);
                            readBucket.setAddressByIndex(j, 0);
                            readBucket.setCount(readBucket.getCount() - 1);
                        }

                    }

                    readBucket.setNumBits((short) (readBucket.getNumBits() + 1));
                    Writing.writeBucket(indexFile, readBucket, lookBucket);

                    for (int j = 0; j < bucketSize; j++) {
                        Bucket updateBucket;
                        int tempHash = tempKeys[j] % ((int) Math.pow(2, numBitsTab));
                        long pkAddress = tempAddress[j];

                        long addressBucket = d.getAddressBucket(tempHash);
                        updateBucket = Reading.readBucket(indexFile, bucketSize, addressBucket);
                        updateBucket.setPkByIndex(updateBucket.getCount(), tempKeys[j]);
                        updateBucket.setAddressByIndex(updateBucket.getCount(), pkAddress);
                        updateBucket.setCount(updateBucket.getCount() + 1);
                        Writing.writeBucket(indexFile, updateBucket, addressBucket);

                    }
                    bitsOld++;

                    hashPk = pk % ((int) Math.pow(2, numBitsTab));
                    lookBucket = d.getAddressBucket(hashPk);
                    readBucket = Reading.readBucket(indexFile, bucketSize, lookBucket);
                   

                }

                hashPk = pk % ((int) Math.pow(2, numBitsTab));
                lookBucket = d.getAddressBucket(hashPk);
                readBucket = Reading.readBucket(indexFile, bucketSize, lookBucket);
                Writing.writeDirectory(indexFile, numBitsTab, directoryAddresTab, d);
                if (readBucket.getCount() == bucketSize) {
                    insertRowMethod(cols, tableName);

                } else {
                    insertNewKey(pk, tableFile, indexFile, bucketSize, numBitsTab, cols);
                    tableFile.close();

                }

                indexFile.close();

            }
        } else {
            insertNewKey(pk, tableFile, indexFile, bucketSize, numBitsTab, cols);

            tableFile.close();
            indexFile.close();
        }
    }

    @FXML
    public void findRow(ActionEvent event) throws IOException {
        int pk = Integer.valueOf(tfFPK.getText());
        String tabName = tfFTabName.getText();
        String found = findPk(tabName, pk);

        if (found == null) {
            ErrorMessage msg = new ErrorMessage("102: Primary Key not found.", this);
            msg.show();
        } else {
            ResultMessage msg = new ResultMessage(found, this);
            msg.show();
        }

    }

    @FXML
    public void browseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showOpenDialog(null);
        try {

            if (file != null) {
                tfFilePath.setText(file.toURI().toURL().getPath());

            }

        } catch (IOException ex) {

        }
    }

    @FXML
    public void importFile(ActionEvent event) throws NumberFormatException, IOException {
        String tableName = tfImTabName.getText();
        File file = new File(tfFilePath.getText());
        importData(tableName, file);
    }

    private String findPk(String tabName, int pk) throws IOException, FileNotFoundException {

        RandomAccessFile indFile = new RandomAccessFile(tabName + suffixIndex, "rw");
        long[] head = Reading.readHeaderInd(indFile);
        short bucketSize = (short) head[0];
        short numBits = (short) head[1];
        long directoryAddress = head[2];

        d = Reading.getDirectoryFromFile(indFile, numBits, directoryAddress);

        int hashPk = pk % ((int) Math.pow(2, numBits));
        long lookBucket = d.getAddressBucket(hashPk);
        RandomAccessFile tabFile = new RandomAccessFile(tabName + suffixTable, "rw");
        Bucket readBucket = Reading.readBucket(indFile, bucketSize, lookBucket);

        System.out.println(readBucket.toString());
        for (int i = 0; i < bucketSize; i++) {
            if (readBucket.getPk()[i] == pk) {
                String row = Reading.readTabRow(tabFile, readBucket.getAddress()[i]);
                indFile.close();
                tabFile.close();
                return row;

            }

        }

        indFile.close();
        tabFile.close();

        return null;
    }

    private void criarTable(String tableName, String cols) throws FileNotFoundException, IOException {
        File checkFile = new File(tableName + suffixTable);
        if (checkFile.exists()) {
            return;
        }
        int bSize = Integer.valueOf(tfCBucketSize.getText());

        cols = "primaryKey," + cols;
        long currentPos;
        RandomAccessFile tabFile = null;
        RandomAccessFile indFile = null;
        tabFile = new RandomAccessFile(tableName + suffixTable, "rw");
        indFile = new RandomAccessFile(tableName + suffixIndex, "rw");

        d = new Directory(NUM_BITS_OF_HASH_STARTED);
        Bucket b1 = new Bucket(0, NUM_BITS_OF_HASH_STARTED, bSize);
        Bucket b2 = new Bucket(0, NUM_BITS_OF_HASH_STARTED, bSize);

        tabFile.writeUTF(cols);

        Writing.writeHeaderInd(indFile, (short) bSize, NUM_BITS_OF_HASH_STARTED, DIRECTORY_ADDRESS);
        Writing.writeDirectory(indFile, d);

        indFile.writeLong(0);

        currentPos = indFile.getFilePointer();

        Writing.writeBucket(indFile, b1, currentPos);
        indFile.seek(DIRECTORY_ADDRESS);
        indFile.writeLong(currentPos);
        d.setAddressByIndex(0, currentPos);

        currentPos = indFile.length();

        Writing.writeBucket(indFile, b2, currentPos);
        indFile.seek(DIRECTORY_ADDRESS + SIZE_ITEM_DIRECTORY);
        indFile.writeLong(currentPos);
        d.setAddressByIndex(1, currentPos);

        tabFile.close();
        indFile.close();

    }

    private Directory loadTable(String tableName) throws FileNotFoundException, IOException {
        RandomAccessFile tableFile = new RandomAccessFile(tableName + suffixTable, "rw");
        RandomAccessFile indexFile = new RandomAccessFile(tableName + suffixIndex, "rw");
        long[] head = Reading.readHeaderInd(indexFile);
        short bucketSize = (short) head[0];
        short numBitsTab = (short) head[1];
        long directoryAddresTab = head[2];
        currentBucketSize = bucketSize;

        Directory result = Reading.getDirectoryFromFile(indexFile, numBitsTab, directoryAddresTab);

        return result;
    }

    private long insertOnTable(String cols, RandomAccessFile tableFile) throws FileNotFoundException, IOException {
        System.out.println("inserir na tabela");
        long eof = tableFile.length();

        String[] values = cols.split(",");
        String colsTypes[] = tableFile.readUTF().split(",");

        tableFile.seek(eof);
        int count = 0;
        if (values.length != colsTypes.length) {

            ErrorMessage msg = new ErrorMessage("301: Invalid Parameters", this);
            msg.show();
        } else {
            for (String types : colsTypes) {
                System.out.println(types);
                switch (types) {
                    case "primaryKey":
                        tableFile.writeInt(Integer.valueOf(values[count]));
                        count++;
                        break;
                    case "int":
                        tableFile.writeInt(Integer.valueOf(values[count]));
                        count++;
                        break;
                    case "str":
                        tableFile.writeUTF(values[count]);
                        count++;
                        break;
                    default:
                        System.out.println("default");
                        ErrorMessage msg = new ErrorMessage("401: File has been Broken. Wrote Wrongly.", this);
                        msg.show();
                        return 0;
                }
            }
        }
        return eof;
    }

    private void insertNewKey(int pk, RandomAccessFile tableFile, RandomAccessFile indexFile, int bucketSize, int numBitsTab, String cols) throws IOException {
        long addressRow = insertOnTable(cols, tableFile);

        Bucket insertInBucket;
        int tempHash = pk % ((int) Math.pow(2, numBitsTab));

        long addressBucket = d.getAddressBucket(tempHash);
        insertInBucket = Reading.readBucket(indexFile, bucketSize, addressBucket);
        insertInBucket.setPkByIndex(insertInBucket.getCount(), pk);
        insertInBucket.setAddressByIndex(insertInBucket.getCount(), addressRow);
        insertInBucket.setCount(insertInBucket.getCount() + 1);
        Writing.writeBucket(indexFile, insertInBucket, addressBucket);

    }

    private void importData(String tableName, File f) throws FileNotFoundException, NumberFormatException, IOException {
        System.out.println(f.toString());
        Scanner sc = new Scanner(f);
        while (sc.hasNext()) {
            String line = sc.nextLine();
            System.out.println("line : " + line);
            insertRowMethod(line, tableName);
        }
        sc.close();

    }
}
