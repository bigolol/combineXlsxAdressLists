/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.combineadresslists;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.impl.common.Levenshtein;

/**
 *
 * @author holgerklein
 */
public class ExcelHandler {

    static Workbook letUserChooseWB(Stage stage) throws IOException, InvalidFormatException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("please choose adress list");
        File f = fileChooser.showOpenDialog(stage);
        return new XSSFWorkbook(f);
    }

    static Workbook transformAdresslistToWorkbook(Adresslist adresslist) {
        Workbook workbook = new XSSFWorkbook();
        Sheet createdSheet = workbook.createSheet();
        Row keyRow = createdSheet.createRow(0);
        int i = 0;
        for (String k : adresslist.getKeys()) {
            keyRow.createCell(i).setCellValue(k);
            ++i;
        }

        int pos = 1;
        for (List<String> list : adresslist.getEntries()) {
            Row currentRow = createdSheet.createRow(pos);
            int cellPos = 0;
            for (String toEnter : list) {
                currentRow.createCell(cellPos).setCellValue(toEnter);
                ++cellPos;
            }
            ++pos;
        }
        return workbook;
    }

    static void displayInTableView(Adresslist adresslist, TableView<List<String>> tv) {
        tv.getColumns().clear();
        for (String k : adresslist.getKeys()) {
            TableColumn<List<String>, String> column = new TableColumn(k);
            column.setCellValueFactory((param) -> {
                List<String> list = (List<String>) param.getValue();
                int pos = adresslist.getKeys().indexOf(k);
                return new ReadOnlyObjectWrapper<>(list.get(pos));
            });
            tv.getColumns().add(column);
        }
        tv.getItems().addAll(adresslist.getEntries());
    }

    static Adresslist combineAdressLists(Adresslist a1, Adresslist a2) {
        List<String> keys1 = a1.getKeys();
        List<String> keys2 = a2.getKeys();
        List<String> combinedKeys = new ArrayList<>(keys2);
        Map<Integer, Integer> newIndicesOfFirstKeyset = new HashMap<>();
        for (String k1 : keys1) {
            String closestWord = getClosestWord(k1, keys2);
            float sim = calcWordSimilarity(k1, closestWord);
            if (sim < .95) {
                newIndicesOfFirstKeyset.put(keys1.indexOf(k1), combinedKeys.size());
                combinedKeys.add(k1);
            } else {
                newIndicesOfFirstKeyset.put(keys1.indexOf(k1), keys2.indexOf(closestWord));
            }
        }

        Adresslist created = new Adresslist(combinedKeys);

        for (List<String> list : a1.getEntries()) {
            List<String> currentEntry = new ArrayList<>(combinedKeys.size());
            for (int i = 0; i < combinedKeys.size(); ++i) {
                currentEntry.add("");
            }
            for (int i = 0; i < list.size(); ++i) {
                currentEntry.set(newIndicesOfFirstKeyset.get(i), list.get(i));
            }
            created.addEntry(currentEntry);
        }

        for (List<String> list : a2.getEntries()) {
            List<String> currentEntry = new ArrayList<>(combinedKeys.size());
            for (String entry : list) {
                currentEntry.add(entry);
            }
            created.addEntry(currentEntry);
        }

        return created;
    }

    static String getClosestWord(String word, List<String> closeValues) {
        String closest = null;
        float highestCompVal = 0;
        for (String s : closeValues) {
            float sim = calcWordSimilarity(word, s);
            if (sim > highestCompVal) {
                highestCompVal = sim;
                closest = s;
            }
        }

        return closest;
    }

    static Adresslist extractAdressList(Sheet sheet) {
        List<String> keys = new ArrayList<>();
        List<Integer> keyXIndeces = new ArrayList<>();
        Row keyRow = getKeyRow(sheet);

        for (Cell c : keyRow) {
            keys.add(c.getStringCellValue().trim());
            keyXIndeces.add(c.getColumnIndex());
        }

        int startX = keyRow.getFirstCellNum();
        int endX = keyRow.getLastCellNum();

        Adresslist adresslist = new Adresslist(keys);

        for (Row r : sheet) {
            if (r.equals(keyRow)) {
                continue;
            }
            List<String> created = new ArrayList<>();
            for (int col : keyXIndeces) {
                try {
                    String strValueFromCell = getStrValueFromCell(r.getCell(col));
                    created.add(strValueFromCell);
                } catch (Exception e) {
                    created.add("");
                }
            }
            adresslist.addEntry(created);
        }

        return adresslist;
    }

    private static String getStrValueFromCell(Cell c) {
        try {
            String stringCellValue = c.getStringCellValue();
            return stringCellValue;
        } catch (Exception e) {
        }
        try {
            double numericCellValue = c.getNumericCellValue();
            return String.valueOf(numericCellValue);
        } catch (Exception e) {
        }
        try {
            Date dateCellValue = c.getDateCellValue();
            return dateCellValue.toString();
        } catch (Exception e) {
        }
        return "";
    }

    private static Row getKeyRow(Sheet sheet) {
        int maxIndex = 0;
        float maxProb = 0;
        Row keyRow = null;
        for (Row r : sheet) {
            Float keyProb = calcKeyProb(r);
            if (keyProb > maxProb) {
                keyRow = r;
                maxProb = keyProb;
            }
        }
        return keyRow;
    }

    private static Float calcKeyProb(Row r) {
        float prob = 0;
        for (Cell c : r) {
            try {
                String stringCellValue = c.getStringCellValue();
                float highest = getClosestVal(stringCellValue);
                prob += highest;
            } catch (Exception e) {
            }
        }
        return prob;
    }

    private static float getClosestVal(String w) {
        String[] possKeys = {"name", "fist name", "adress", "street", "mail", "e-mail", "email", "phone", "tel"};

        float max = 0;
        for (String k : possKeys) {
            float wordSimilarity = calcWordSimilarity(k, w.toLowerCase());
            if (wordSimilarity > max) {
                max = wordSimilarity;
            }
        }
        return max;
    }

    private static float calcWordSimilarity(String w1, String w2) {
        return 1.0F / Levenshtein.distance(w1, w2);
    }

}
