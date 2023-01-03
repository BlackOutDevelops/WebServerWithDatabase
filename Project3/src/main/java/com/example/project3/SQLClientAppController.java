package com.example.project3;

import com.mysql.cj.jdbc.MysqlDataSource;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.ResourceBundle;

public class SQLClientAppController implements Initializable {
    private Connection Connection;
    private Connection UpdateConnection;
    private Properties UpdateOperationsRootPropertyFile = new Properties();
    private Boolean isConnected = false;
    @FXML
    private Label ConnectionDetailsLabel;
    @FXML
    private Label PropertiesFileLabel;
    @FXML
    private Label SQLTextAreaLabel;
    @FXML
    private ComboBox<String> PropertiesFileComboBox;
    @FXML
    private GridPane OverallGridPane;
    @FXML
    private Label UsernameLabel;
    @FXML
    private Label PasswordLabel;
    @FXML
    private Button ClearButton;
    @FXML
    private Button ExecuteButton;
    @FXML
    private Button ConnectButton;
    @FXML
    private TextField ConnectStatusTextField;
    @FXML
    private Label SQLExecutionLabel;
    @FXML
    private Button ClearExecutionButton;
    @FXML
    private TextArea SQLCommandTextArea;
    @FXML
    private TextField UsernameTextField;
    @FXML
    private TextField PasswordTextField;
    @FXML
    private TableView<String[]> SQLExecutionResultTable;

    @FXML
    private void MouseEntered(MouseEvent event) {
        Button triggeredButton = (Button)event.getSource();

        Paint tempBG = triggeredButton.getBackground().getFills().get(0).getFill();
        Color tempColor = (Color)tempBG;
        double red = tempColor.getRed();
        double green = tempColor.getGreen();
        double blue = tempColor.getBlue();

        triggeredButton.setBackground(new Background(new BackgroundFill(Color.color(red * .9, green * .9, blue * .9), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    @FXML
    private void MouseExited(MouseEvent event) {
        Button triggeredButton = (Button)event.getSource();

        Paint tempBG = triggeredButton.getBackground().getFills().get(0).getFill();
        Color tempColor = (Color)tempBG;
        double red = tempColor.getRed();
        double green = tempColor.getGreen();
        double blue = tempColor.getBlue();

        triggeredButton.setBackground(new Background(new BackgroundFill(Color.color(red / .9, green / .9, blue / .9), CornerRadii.EMPTY, Insets.EMPTY)));
    }

    @FXML
    private void MousePressed(MouseEvent event) {
        Button triggeredButton = (Button)event.getSource();

        triggeredButton.setEffect(new InnerShadow(BlurType.GAUSSIAN, Color.DARKGRAY, 5, 0, 0, 0));
    }

    @FXML
    private void MouseReleased(MouseEvent event) {
        Button triggeredButton = (Button)event.getSource();

        triggeredButton.setEffect(null);
    }

    @FXML
    private void Execute_ButtonClicked(ActionEvent ignoredEvent) throws SQLException {
        if (Connection != null)
        {
            String sqlCommand = SQLCommandTextArea.getText();
            String[] splitSqlCommand = sqlCommand.split(" ", 2);
            Statement updateStatement = UpdateConnection.createStatement();
            ResultSet updateResultSet;
            int num_queries, num_updates;

            try
            {
                Statement statement = Connection.createStatement();

                if (splitSqlCommand[0].trim().toLowerCase().equals("select"))
                {
                    ResultSet resultSet = statement.executeQuery(sqlCommand);
                    PopulateExecutionResults(resultSet);
                    updateResultSet = updateStatement.executeQuery("select num_queries from operationscount");
                    updateResultSet.next();
                    num_queries = Integer.parseInt(updateResultSet.getString(1));
                    num_queries++;
                    updateStatement.executeQuery("SET SQL_SAFE_UPDATES = 0;");
                    updateStatement.executeUpdate("UPDATE operationscount SET num_queries = " + num_queries + ";");
                    updateStatement.executeQuery("SET SQL_SAFE_UPDATES = 1;");
                }
                else
                {
                    statement.executeUpdate(sqlCommand);
                    updateResultSet = updateStatement.executeQuery("select num_updates from operationscount");
                    updateResultSet.next();
                    num_updates = Integer.parseInt(updateResultSet.getString(1));
                    num_updates++;
                    updateStatement.executeQuery("SET SQL_SAFE_UPDATES = 0;");
                    updateStatement.executeUpdate("UPDATE operationscount SET num_updates = " + num_updates + ";");
                    updateStatement.executeQuery("SET SQL_SAFE_UPDATES = 1;");
                }
            }
            catch (SQLException e)
            {
                Label errorMessage = new Label(e.getMessage());
                errorMessage.setWrapText(true);
                Alert sqlAlert = new Alert(Alert.AlertType.ERROR);
                sqlAlert.getDialogPane().setContent(errorMessage);
                sqlAlert.show();
                throw e;
            }
        }
        else
        {
            Alert noConnectionError = new Alert(Alert.AlertType.ERROR, "Please connect to a database first!");
            noConnectionError.show();
        }
    }

    @FXML
    private void Clear_ButtonClicked(ActionEvent ignoredEvent) {
        SQLCommandTextArea.clear();
    }

    @FXML
    private void ClearExecuteResults_ButtonClicked(ActionEvent ignoredEvent) {
        SQLExecutionResultTable.getColumns().clear();
        SQLExecutionResultTable.getItems().clear();
    }

    @FXML
    private void Connect_ButtonClicked(ActionEvent event) throws SQLException {
        Node node = (Node) event.getSource();
        Stage stage = (Stage) node.getScene().getWindow();
        StringBuilder missingInputs = new StringBuilder("Please Enter the Following Missing Fields: \n");
        boolean isMissingInputs = false;

        for (int i = 0; i < 3; i++) {
            if (i == 0 && PropertiesFileComboBox.getValue() == null)
            {
                isMissingInputs = true;
                missingInputs.append("Nothing Selected In Property File Box\n");
            }
            else if (i == 1 && UsernameTextField.getText().isEmpty())
            {
                isMissingInputs = true;
                missingInputs.append("Username Field Left Blank\n");
            }
            else if (i == 2 && PasswordTextField.getText().isEmpty())
            {
                isMissingInputs = true;
                missingInputs.append("Password Field Left Blank\n");
            }
        }

        if (isMissingInputs)
        {
            Alert errorMissingInputs = new Alert(Alert.AlertType.ERROR, missingInputs.toString());
            errorMissingInputs.show();
            return;
        }

        ConnectToDatabase(stage);
    }

    private void PopulateExecutionResults(ResultSet resultSet) throws SQLException {
        boolean isInitialized = false;
        int i;
        int size;
        ObservableList<String[]> data = FXCollections.observableArrayList();
        ResultSetMetaData metaData = resultSet.getMetaData();
        size = metaData.getColumnCount();
        while (resultSet.next()) {
            if (!isInitialized)
                for (i = 1; i <= size; i++)
                {
                    final int indexOfData = i;
                    isInitialized = true;
                    TableColumn<String[], String> column = new TableColumn<>(metaData.getColumnName(i).substring(0, 1).toUpperCase() + metaData.getColumnName(i).substring(1));
                    column.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue()[indexOfData - 1]));
                    SQLExecutionResultTable.getColumns().add(column);
                }

            String[] eachData = new String[size];
            for (i = 1; i <= size; i++)
            {
                eachData[i - 1] = resultSet.getString(i);
            }
            data.add(eachData);
        }
        SQLExecutionResultTable.getItems().addAll(data);
        SQLExecutionResultTable.refresh();
    }

    private void ConnectToDatabase(Stage stage) throws SQLException {
        Properties selectedPropertyFile = new Properties();

        try {
            UpdateOperationsRootPropertyFile.load(new FileReader("src/main/resources/root.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (PropertiesFileComboBox.getValue() != null) {
            try {
                selectedPropertyFile.load(new FileReader("src/main/resources/" + PropertiesFileComboBox.getValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Alert missingPropertySelection = new Alert(Alert.AlertType.ERROR, "Please Select a Property File!");
            missingPropertySelection.show();
            return;
        }

        MysqlDataSource dataSource = new MysqlDataSource();

        if (UsernameTextField.getText().trim().contentEquals(selectedPropertyFile.getProperty("MYSQL_DB_USERNAME")))
            dataSource.setUser(UsernameTextField.getText().trim());
        else
        {
            Alert errorUsername = new Alert(Alert.AlertType.ERROR, "Please Enter a Valid Username!");
            errorUsername.show();
            return;
        }

        if (PasswordTextField.getText().contentEquals(selectedPropertyFile.getProperty("MYSQL_DB_PASSWORD")))
            dataSource.setPassword(PasswordTextField.getText());
        else
        {
            Alert errorPassword = new Alert(Alert.AlertType.ERROR, "Please Enter a Valid Password!");
            errorPassword.show();
            return;
        }

        if (isConnected)
        {
            UpdateConnection.close();
            Connection.close();
            System.out.println("Connections Closed...\n");
        }

        dataSource.setURL(selectedPropertyFile.getProperty("MYSQL_DB_URL"));
        Connection = dataSource.getConnection();

        dataSource.setUser(UpdateOperationsRootPropertyFile.getProperty("MYSQL_DB_USERNAME"));
        dataSource.setPassword(UpdateOperationsRootPropertyFile.getProperty("MYSQL_DB_PASSWORD"));
        dataSource.setURL(UpdateOperationsRootPropertyFile.getProperty("MYSQL_DB_OPERATIONSLOG_URL"));
        UpdateConnection = dataSource.getConnection();

        isConnected = true;
        ConnectStatusTextField.setText("Connected to " + selectedPropertyFile.getProperty("MYSQL_DB_USERNAME") + " with " + selectedPropertyFile.getProperty("MYSQL_DB_URL"));

        DatabaseMetaData dbMetaData = Connection.getMetaData();
        System.out.println("JDBC Driver name " + dbMetaData.getDriverName());
        System.out.println("JDBC Driver version " + dbMetaData.getDriverVersion());
        System.out.println("Driver Major version " +dbMetaData.getDriverMajorVersion());
        System.out.println("Driver Minor version " +dbMetaData.getDriverMinorVersion());
        System.out.println();

        stage.setOnCloseRequest(e->{
            try {
                Connection.close();
                System.out.println("Connection Closed");
            } catch (Exception exc) {
                System.err.println("Could Not Close Connection");
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add property filenames to for combobox here. Make sure they are within resources before running program
        ObservableList<String> options =
                FXCollections.observableArrayList(
                    "root.properties", "client.properties"
                );
//        System.out.println("Working directory = " + System.getProperty("user.dir"));

        initializeHeaders(ConnectionDetailsLabel);
        initializeHeaders(SQLTextAreaLabel);

        initializeUserInputLabels(PropertiesFileLabel);
        initializeUserInputLabels(UsernameLabel);
        initializeUserInputLabels(PasswordLabel);

        PropertiesFileComboBox.getItems().addAll(options);
        PropertiesFileComboBox.setPrefWidth(600);

        SQLCommandTextArea.setMinHeight(150);
        initializeButtons(ClearButton, Color.WHITE, Color.RED);
        initializeButtons(ExecuteButton, Color.color(0, 1, 0), Color.BLACK);
        initializeButtons(ConnectButton, Color.BLUE, Color.color(1,1, 0));
        ConnectButton.setMinWidth(150);

        ConnectStatusTextField.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
        ConnectStatusTextField.setFont(Font.font(null, FontWeight.EXTRA_BOLD, Font.getDefault().getSize()));
        ConnectStatusTextField.setPrefWidth(2000);
        ConnectStatusTextField.setText("No Connection Now");

        initializeHeaders(SQLExecutionLabel);

        SQLExecutionResultTable.setPrefHeight(2000);
        SQLExecutionResultTable.setPrefWidth(2000);
        SQLExecutionResultTable.setPlaceholder(new Label(""));
        SQLExecutionResultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        initializeButtons(ClearExecutionButton, Color.YELLOW, Color.BLACK);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(50);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        OverallGridPane.getColumnConstraints().addAll(column1, column2);
    }

    private void initializeButtons(Button temp, Color backgroundColor, Color textColor) {
        temp.setBorder(Border.EMPTY);
        temp.setBackground(new Background(new BackgroundFill(backgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
        temp.setFont(Font.font(null, FontWeight.EXTRA_BOLD, Font.getDefault().getSize()));
        temp.setTextFill(textColor);
    }

    private void initializeHeaders(Label temp) {
        temp.setFont(Font.font(null, FontWeight.EXTRA_BOLD, FontPosture.REGULAR, 14));
        temp.setTextFill(Color.BLUE);
    }

    private void initializeUserInputLabels(Label temp) {
        temp.setFont(Font.font(null, FontWeight.NORMAL, FontPosture.REGULAR, 14));
        temp.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        temp.setPadding(new Insets(3, 0, 3, 2));
        temp.setMinWidth(120);
    }
}