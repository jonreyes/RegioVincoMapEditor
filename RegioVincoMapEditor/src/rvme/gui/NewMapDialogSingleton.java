/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rvme.gui;

import java.io.File;
import java.net.URL;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import properties_manager.PropertiesManager;
import static rvme.PropertyType.GEO_LABEL;
import static rvme.PropertyType.GEO_TITLE;
import static rvme.PropertyType.JSON_EXT;
import static rvme.PropertyType.JSON_EXT_DESC;
import static rvme.PropertyType.NAME_LABEL;
import static rvme.PropertyType.NEW_ERROR_MESSAGE;
import static rvme.PropertyType.NEW_ERROR_TITLE;
import static rvme.PropertyType.NMDIALOG_TITLE;
import static rvme.PropertyType.OK_LABEL;
import static rvme.PropertyType.PARENT_LABEL;
import static rvme.PropertyType.PARENT_TITLE;
import static rvme.PropertyType.SELECT_LABEL;
import rvme.data.DataManager;
import rvme.file.FileManager;
import saf.AppTemplate;
import saf.components.AppDataComponent;
import static saf.components.AppStyleArbiter.CLASS_BORDERED_PANE;
import static saf.components.AppStyleArbiter.CLASS_GRID_PANE;
import static saf.components.AppStyleArbiter.CLASS_PROMPT_LABEL;
import static saf.components.AppStyleArbiter.CLASS_SUBHEADING_LABEL;
import static saf.settings.AppPropertyType.APP_CSS;
import static saf.settings.AppPropertyType.APP_PATH_CSS;
import static saf.settings.AppPropertyType.LOAD_ERROR_MESSAGE;
import static saf.settings.AppPropertyType.LOAD_ERROR_TITLE;
import static saf.settings.AppStartupConstants.PATH_WORK;
import saf.ui.AppGUI;

/**
 *
 * @author Jon Reyes
 */
public class NewMapDialogSingleton extends Stage{
    static NewMapDialogSingleton singleton = null;
    
    AppTemplate app;
    AppGUI gui;
    
    DataManager data;
    PropertiesManager props;
    
    VBox messagePane;
    Scene messageScene;
    Label messageLabel;
    
    GridPane nmGrid;
    
    Label nameLabel;
    TextField nameTextField;
    
    Label parentLabel;
    HBox parentSelect;
    TextField parentTextField;
    Button parentBtn;
    
    Label geoLabel;
    HBox geoSelect;
    TextField geoTextField;
    Button geoBtn;
    
    Button okBtn;
    
    String name;
    String parent;
    File geometry;
    
    final double SPACE = 10;
    final double SCALEW = 0.36;
    final double SCALEH = 0.3;
    
    /**
     * Note that the constructor is private since it follows
     * the singleton design pattern.
     * 
     * @param primaryStage The owner of this modal dialog.
     */
    private NewMapDialogSingleton(){}
    
    /**
     * A static accessor method for getting the singleton object.
     * 
     * @return The one singleton dialog of this object type.
     */
    public static NewMapDialogSingleton getSingleton() {
	if (singleton == null)
	    singleton = new NewMapDialogSingleton();
	return singleton;
    }
    
    public void init(AppTemplate initApp){
        app = initApp;
        gui = app.getGUI();
        data = (DataManager) app.getDataComponent();
        props = PropertiesManager.getPropertiesManager();
        initModality(Modality.WINDOW_MODAL);
        initOwner(gui.getWindow());
        initGUI();
        initHandlers();
        initStyleSheet();
        initStyle();
    }
    
    private void initGUI(){
        nameLabel = new Label(props.getProperty(NAME_LABEL));
        nameTextField = new TextField();
        
        parentLabel = new Label(props.getProperty(PARENT_LABEL));
        parentTextField = new TextField();
        parentBtn = new Button(props.getProperty(SELECT_LABEL));
        
        parentSelect = new HBox();
        parentSelect.setSpacing(SPACE);
        parentSelect.getChildren().add(parentTextField);
        parentSelect.getChildren().add(parentBtn);
        
        geoLabel = new Label(props.getProperty(GEO_LABEL));
        geoTextField = new TextField();
        geoBtn = new Button(props.getProperty(SELECT_LABEL));
        
        geoSelect = new HBox();
        geoSelect.setSpacing(SPACE);
        geoSelect.getChildren().add(geoTextField);
        geoSelect.getChildren().add(geoBtn);
        
        okBtn = new Button(props.getProperty(OK_LABEL));
        
        nmGrid = new GridPane();
        nmGrid.add(nameLabel, 0, 0);
        nmGrid.add(nameTextField, 1, 0);
        nmGrid.add(parentLabel, 0, 1);
        nmGrid.add(parentSelect, 1, 1);
        nmGrid.add(geoLabel, 0, 2);
        nmGrid.add(geoSelect, 1, 2);
        
        messageLabel = new Label(props.getProperty(NMDIALOG_TITLE));
        messagePane = new VBox();
        messagePane.setAlignment(Pos.CENTER);
        messagePane.getChildren().add(messageLabel);
        messagePane.getChildren().add(nmGrid);
        messagePane.getChildren().add(okBtn);
        messageScene = new Scene(messagePane, SCALEW*gui.getWindow().getWidth(), SCALEH*gui.getWindow().getHeight());
        this.setScene(messageScene);
        this.setTitle(props.getProperty(NMDIALOG_TITLE));
    }
    
    private void initHandlers(){
        parentBtn.setOnAction(e->{
            selectParent();
        });
        geoBtn.setOnAction(e->{
            selectGeometry();
        });
        okBtn.setOnAction(e->{
            submitNewMap();
        });
    }
    
    private void submitNewMap(){
        name = nameTextField.getText();
        parent = parentTextField.getText();
        try{
            if(name.isEmpty() || parent.isEmpty()){throw new Exception();}
            data.reset();
            data.setName(name);
            data.setParent(parent);
            setGeometry();
            Workspace workspace = (Workspace) app.getWorkspaceComponent();
            workspace.fileController.newMap();
            this.hide();
            this.reset();
        }
        catch(Exception e){
            e.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText(props.getProperty(NEW_ERROR_TITLE));
            alert.setContentText(props.getProperty(NEW_ERROR_MESSAGE));
            // RESIZE FOR MESSAGE
            alert.getDialogPane().getChildren().stream().filter(node -> node instanceof Label).forEach(node -> ((Label)node).setMinHeight(Region.USE_PREF_SIZE));
            alert.showAndWait();
        }
    }
    
    private void selectParent(){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setInitialDirectory(new File(PATH_WORK));
        dc.setTitle(props.getProperty(PARENT_TITLE));
        parent = dc.showDialog(app.getGUI().getWindow()).getPath();
        if(!parent.isEmpty()) parentTextField.setText(parent);
    }
    
    private void selectGeometry(){
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(PATH_WORK));
        fc.setTitle(props.getProperty(GEO_TITLE));
        fc.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter(props.getProperty(JSON_EXT_DESC), props.getProperty(JSON_EXT)));
        geometry = fc.showOpenDialog(app.getGUI().getWindow());
        if(geometry != null) geoTextField.setText(geometry.getPath());
    }
    
    private void setGeometry(){
        Workspace workspace = (Workspace) app.getWorkspaceComponent();
            try {
                AppDataComponent dataManager = app.getDataComponent();
                FileManager fileManager = (FileManager) app.getFileComponent();
                fileManager.loadGeometry(dataManager, geometry.getAbsolutePath());
                
                workspace.reloadWorkspace();
                    
                workspace.activateWorkspace(app.getGUI().getAppPane());
                
                workspace.updateFileControls(false,false);
            }catch (Exception e){
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeaderText(props.getProperty(LOAD_ERROR_TITLE));
                alert.setContentText(props.getProperty(LOAD_ERROR_MESSAGE));
                alert.showAndWait();
            }
    }
    
    public void reset(){
        nameTextField.clear();
        parentTextField.clear();
        geoTextField.clear();
    }
    
    private void initStyleSheet(){
         // LOADING CSS
	String stylesheet = props.getProperty(APP_PATH_CSS);
	stylesheet += props.getProperty(APP_CSS);
	URL stylesheetURL = app.getClass().getResource(stylesheet);
	String stylesheetPath = stylesheetURL.toExternalForm();
        messageScene.getStylesheets().add(stylesheetPath);
    }
    
    private void initStyle(){
        messagePane.getStyleClass().add(CLASS_BORDERED_PANE);
        nmGrid.getStyleClass().add(CLASS_GRID_PANE);
        messageLabel.getStyleClass().add(CLASS_SUBHEADING_LABEL);
        nameLabel.getStyleClass().add(CLASS_PROMPT_LABEL);
        parentLabel.getStyleClass().add(CLASS_PROMPT_LABEL);
        geoLabel.getStyleClass().add(CLASS_PROMPT_LABEL);
    }
}
