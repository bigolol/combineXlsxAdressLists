/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.combineadresslists;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author holgerklein
 */
public class ExcelHandlerTest {

    public ExcelHandlerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of extractAdressList method, of class ExcelHandler.
     */
    @Test
    public void testExtractAdressList() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(new FileInputStream(new File("testres/adresslist1.xlsx")));
        Adresslist extractAdressList = ExcelHandler.extractAdressList(workbook.getSheetAt(0));
    }

    @Test
    public void testCombineTwoAdressLists() throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(new FileInputStream(new File("testres/adresslist1.xlsx")));
        Adresslist extractAdressList = ExcelHandler.extractAdressList(workbook.getSheetAt(0));
        Workbook workbook2 = new XSSFWorkbook(new FileInputStream(new File("testres/adresslist2.xlsx")));
        Adresslist extractAdressList2 = ExcelHandler.extractAdressList(workbook2.getSheetAt(0));
        Adresslist combinedAdressLists = ExcelHandler.combineAdressLists(extractAdressList, extractAdressList2);
    }

}
