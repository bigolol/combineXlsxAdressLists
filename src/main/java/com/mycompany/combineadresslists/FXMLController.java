package com.mycompany.combineadresslists;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Workbook;

public class FXMLController implements Initializable {

    @FXML
    private TableView<List<String>> lhsTableView;
    @FXML
    private TableView<List<String>> rhsTableView;
    private Stage primaryStage;
    private Workbook lhsWB;
    private Workbook rhsWB;

    private Adresslist lhsAdressList;
    private Adresslist rhsAdressList;

    public void setPriaryStage(Stage priaryStage) {
        this.primaryStage = priaryStage;
    }

    @FXML
    public void onCombineTables() {
        if (lhsAdressList != null && rhsAdressList != null) {
            try {
                Adresslist combinedAdressLists = ExcelHandler.combineAdressLists(lhsAdressList, rhsAdressList);
                Workbook wb = ExcelHandler.transformAdresslistToWorkbook(combinedAdressLists);
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Choose Save location");
                File file = fileChooser.showSaveDialog(primaryStage);
                String filepath = file.getAbsolutePath();
                filepath = filepath.endsWith(".xlsx") ? filepath : filepath + ".xlsx";
                FileOutputStream out = new FileOutputStream(filepath);
                wb.write(out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onChooseLhsTable() {
        try {
            lhsWB = ExcelHandler.letUserChooseWB(primaryStage);
            lhsAdressList = ExcelHandler.extractAdressList(lhsWB.getSheetAt(lhsWB.getActiveSheetIndex()));
            ExcelHandler.displayInTableView(lhsAdressList, lhsTableView);
        } catch (Exception e) {
        }
    }

    @FXML
    public void onChooseRhsTable() {
        try {
            rhsWB = ExcelHandler.letUserChooseWB(primaryStage);
            rhsAdressList = ExcelHandler.extractAdressList(rhsWB.getSheetAt(rhsWB.getActiveSheetIndex()));
            ExcelHandler.displayInTableView(rhsAdressList, rhsTableView);

        } catch (Exception e) {
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
}
