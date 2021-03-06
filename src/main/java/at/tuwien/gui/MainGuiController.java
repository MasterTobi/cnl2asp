package at.tuwien.gui;

import at.tuwien.service.IMainGuiService;
import at.tuwien.service.impl.MainGuiService;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainGuiController implements Initializable{

    @FXML
    public MenuItem miOpenFile;
    @FXML
    public MenuItem miSaveFile;
    @FXML
    public MenuItem miManualTranslations;
    @FXML
    public TabPane tabPane;
    @FXML
    public MenuItem miExportASP;
    @FXML
    public MenuItem miNewTab;
    @FXML
    public MenuItem miTranslate;



    public HashMap<Tab,TranslationTabController> tabTranslationTabControllerHashMap;

    private static int tabCount = 1;

    private IMainGuiService mainGuiService;
    private MainGuiController mainGuiController;
    private Scene scene;


    final KeyCombination keyComb1 = new KeyCodeCombination(KeyCode.S,
            KeyCombination.CONTROL_DOWN);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainGuiController = this;
        tabTranslationTabControllerHashMap = new HashMap<>();
        mainGuiService = new MainGuiService();
        mainGuiService.setTranslationType(TranslationType.MANUAL);

        createNewTab();
    }

    public void openFileClicked(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CNL problem description");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("TXT", "*.txt")
        );

        Stage stage = (Stage) tabPane.getScene().getWindow();

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            openFile(file);
        }
    }

    private void openFile(File file) {

        TranslationTabController translationTabController = createNewTab();

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            translationTabController.caCNL.replaceText("");

            for (String line: lines) {
                translationTabController.caCNL.appendText(line + "\n");
            }

            translationTabController.getTab().setText(file.getName());

            translationTabController.setFile(file);

            if(mainGuiService.getTranslationType() == TranslationType.AUTOMATIC) {
                translationTabController.translate();
            }

        } catch (IOException e) {
            translationTabController.taError.appendText(e.getMessage());
        }
    }

    public void saveFileClicked(ActionEvent actionEvent) {
        saveCnlFile();
    }

    public void saveAsFileClicked(ActionEvent actionEvent) {
        File file = showSaveDialog();
        saveCLN(file);
    }

    private File showSaveDialog(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CNL problem description");

        Stage stage = (Stage) tabPane.getScene().getWindow();

        File file = fileChooser.showSaveDialog(stage);

        return file;
    }

    private void saveCnlFile() {
        if(getControllerOfSelectedTab().getFile() != null)
        {
            saveCLN(getControllerOfSelectedTab().getFile());
        }
        else {
            File file = showSaveDialog();
            saveCLN(file);
        }
    }

    private void saveCLN(File file){

        if (file != null) {

            String path = file.getPath();
            if(!file.getPath().endsWith(".txt"))
            {
                path += ".txt";
            }

            try(  PrintWriter out = new PrintWriter( path) ){
                out.println( getControllerOfSelectedTab().caCNL.getText() );
            } catch (FileNotFoundException e) {
                getControllerOfSelectedTab().taError.appendText(e.getMessage());
            }

            getControllerOfSelectedTab().setInitialCNLContent(getControllerOfSelectedTab().caCNL.getText());
            getSelectedTab().setText(file.getName());
            getControllerOfSelectedTab().highlightTabLabel(false);
        }
    }

    public void addWordClicked(ActionEvent actionEvent) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_word.fxml"));

            Stage stage = new Stage();

            /* block parent window */
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());

            /* set the scene */
            stage.setScene(new Scene(loader.load(), 391, 59));
            stage.setTitle("Add Word to Dictionary");

            stage.show();

            AddWordController addWordController = (AddWordController) loader.getController();
            addWordController.setMainGuiService(mainGuiService);

        } catch (IOException e) {
            e.printStackTrace();
        }

        mainGuiService.updateDirectory();
    }

    public void openDictionaryClicked(ActionEvent actionEvent) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/directory.fxml"));

            Stage stage = new Stage();

            /* block parent window */
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());

            /* set the scene */
            stage.setScene(new Scene(loader.load(), 538, 400));
            stage.setTitle("Dictionary");

            stage.show();

            DictionaryController dictionaryController = (DictionaryController) loader.getController();
            dictionaryController.setMainGuiService(mainGuiService);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mainGuiService.updateDirectory();

    }

    public void rmiAutomaticTranslationSelected(ActionEvent actionEvent) {
        mainGuiService.setTranslationType(TranslationType.AUTOMATIC);
    }

    public void rmiManualTranslationSelected(ActionEvent actionEvent) {
        mainGuiService.setTranslationType(TranslationType.MANUAL);
    }

    public void addTranslationPatternClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/add_translation_pattern.fxml"));

            Stage stage = new Stage();

            /* block parent window */
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());

            /* set the scene */
            stage.setScene(new Scene(loader.load(), 411, 206));
            stage.setTitle("Add Sentence Pattern");

            stage.show();

            AddTranslationPatternController addTranslationPatternController = (AddTranslationPatternController) loader.getController();
            addTranslationPatternController.setMainGuiService(mainGuiService);

        } catch (IOException e) {
            e.printStackTrace();
        }

        mainGuiService.updatedTranslationPatterns();
    }

    public void showTranslationPatternsClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/translation_patterns.fxml"));

            Stage stage = new Stage();

            /* block parent window */
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());

            /* set the scene */
            stage.setScene(new Scene(loader.load(), 900, 400));
            stage.setTitle("Self-Defined Translation Patterns");

            stage.show();

            TranslationPatternsController translationPatternsController = (TranslationPatternsController) loader.getController();
            translationPatternsController.setMainGuiService(mainGuiService);

        } catch (IOException e) {
            e.printStackTrace();
        }

        mainGuiService.updatedTranslationPatterns();
    }

    public void showManualTranslationsClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/manual_translations.fxml"));

            Stage stage = new Stage();

            /* block parent window */
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tabPane.getScene().getWindow());

            /* set the scene */
            stage.setScene(new Scene(loader.load(), 900, 400));
            stage.setTitle("Manual Translations");

            stage.show();

            ManualTranslationsController manualTranslationsController = (ManualTranslationsController) loader.getController();
            manualTranslationsController.setMainGuiService(mainGuiService);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportASPClicked(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export ASP program");

        Stage stage = (Stage) tabPane.getScene().getWindow();

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {

            String path = file.getPath();
            if(!file.getPath().endsWith(".dl"))
            {
                path += ".dl";
            }

            try(  PrintWriter out = new PrintWriter( path) ){
                out.println( getControllerOfSelectedTab().caASP.getText() );
            } catch (FileNotFoundException e) {
                getControllerOfSelectedTab().taError.appendText(e.getMessage());
            }
        }
    }


    private boolean firstStart = true;  // Workaround: addTabClicked is called before initialize => e.g. translationTabControllerList == NULL
    public void addTabClicked(Event event) {

        if(!firstStart) {
            createNewTab();
        }
        firstStart = false;
    }

    public TranslationTabController createNewTab(){

        TranslationTabController translationTabController = null;

        try {
            Tab tab = new Tab(String.format("new Tab (%d)",tabCount++));

            FXMLLoader loader = new FXMLLoader();

            Node n = loader.load(MainGuiController.class.getResourceAsStream("/translation_tab.fxml"));

            translationTabController = (TranslationTabController) loader.getController();
            translationTabController.setMainGuiService(mainGuiService);
            translationTabController.setTab(tab);

            TranslationTabController finalTranslationTabController = translationTabController;
            tab.setOnCloseRequest(new EventHandler<Event>() {
                @Override
                public void handle(Event event) {
                    if (finalTranslationTabController.hasCnlContentChanged()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Save changes");
                        alert.setHeaderText("Do you want to save your changes?");
                        alert.setContentText("Your changes will be lost if you don't save them.");

                        ButtonType buttonTypeYes = new ButtonType("Yes");
                        ButtonType buttonTypeNo = new ButtonType("No");

                        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.get() == buttonTypeYes) {
                            saveCnlFile();
                        }
                    }
                }
            });

            tabTranslationTabControllerHashMap.put(tab,translationTabController);

            tab.setContent(n);
            tabPane.getTabs().add(tabPane.getTabs().size()-1,tab);
            tabPane.getSelectionModel().select(tab);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return translationTabController;
    }

    private TranslationTabController getControllerOfSelectedTab(){
        return tabTranslationTabControllerHashMap.get(tabPane.getSelectionModel().getSelectedItem());
    }

    private Tab getSelectedTab(){
        return tabPane.getSelectionModel().getSelectedItem();
    }

    public void newTabClicked(ActionEvent actionEvent) {
        createNewTab();
    }

    public void translateClicked(ActionEvent actionEvent) {
        getControllerOfSelectedTab().translate();
    }

    public void setScene(Scene scene) {
        this.scene = scene;

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.isShortcutDown() && keyEvent.getCode().equals(KeyCode.N)){
                    createNewTab();
                } else if(keyEvent.isShortcutDown() && keyEvent.getCode().equals(KeyCode.T)){
                    getControllerOfSelectedTab().translate();
                }

            }
        });
    }
}
