package com.example.algo_huffman;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Represents the scene for decompression functionality in a JavaFX application. This class handles the decompression
 * process of files using Huffman coding, displaying the results, and providing interactive elements for user interaction.
 */
public class Decompress extends Scene {

    // Class attributes
    int[] freq = new int[256]; // Frequency array for each character
    File file; // File to be decompressed
    String fileName; // Name of the file
    MinHeap heap; // Heap used in the Huffman coding process
    byte numberOfLeafs; // Number of leaf nodes in the Huffman tree

    String Fullheader; // Full header string extracted from the file
    Node[] nodes = new Node[256]; // Array of nodes representing the Huffman tree

    Node rootNode; // Root node of the Huffman tree

    BorderPane bp = new BorderPane(); // Main layout pane

    long lengthBefore; // File size before decompression
    long lengthAfter; // File size after decompression

    byte extLength; // Length of the file extension
    String extString; // File extension
    int headerLength; // Length of the header
    String header; // Header data

    Stage stage; // Current stage
    Scene scene; // Original scene to return to

    /**
     * Constructs a new DecompressScene with a specified Stage, Scene, and File.
     * Initializes the GUI elements and starts the decompression process.
     *
     * @param stage The primary stage of the application
     * @param scene The previous scene to return to
     * @param file  The file to be decompressed
     */
    public Decompress(Stage stage, Scene scene, File file) {
        super(new BorderPane(), 1200, 600);
        this.stage = stage;
        this.scene = scene;

        this.bp = ((BorderPane) this.getRoot());


        Image backgroundImage = new Image("file:///C:/Users/user/IdeaProjects/Algo_huffman/src/main/resources/com/example/algo_huffman/WhatsApp Image 2024-11-11 at 20.58.58_b02e5a96.jpg");
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, false, true)
        );
        bp.setBackground(new Background(bgImage));
        this.file = file;

        this.lengthBefore = this.file.length();

        getHeader();

        addFX();
    }

    /**
     * Extracts the header from the file and initializes the Huffman tree based on the extracted header.
     * Reads and processes the file data to perform decompression.
     * The method first reads the extension length and the extension itself from the file,
     * then reads the header length and constructs the Huffman tree from the serialized header data.
     * Finally, it decompresses the remaining data in the file using the Huffman tree.
     */
    private void getHeader() {

        byte[] bufferIn = new byte[8];
        byte[] extBuffer;

        try (FileInputStream inputStream = new FileInputStream(file)) {

            this.extLength = (byte) inputStream.read();

            extBuffer = new byte[this.extLength];

            byte length = (byte) inputStream.read(extBuffer);

            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++)
                builder.append((char) extBuffer[i]);

            this.extString = builder.toString();

            inputStream.read(bufferIn, 0, 4);

            this.headerLength = bufferIn[3] & 0xFF | (bufferIn[2] & 0xFF) << 8 | (bufferIn[1] & 0xFF) << 16
                    | (bufferIn[0] & 0xFF) << 24;

            int bytesRead;

            int numberOfBytesForHeader_Conter = 0;
            int numberOfBytersForHeader = (this.headerLength % 8 == 0) ? this.headerLength / 8
                    : (this.headerLength / 8) + 1;

            // StringBuilders for header and the serialized data
            StringBuilder header = new StringBuilder();
            StringBuilder serialData = new StringBuilder();

            // Read the rest of the file
            while ((bytesRead = inputStream.read(bufferIn)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (numberOfBytesForHeader_Conter < numberOfBytersForHeader) {
                        header.append(convertByteToBinary(bufferIn[i]));
                        numberOfBytesForHeader_Conter++;
                    } else
                        serialData.append(convertByteToBinary(bufferIn[i]));

                }
            }

            this.header = header.toString();

            inputStream.close();

            // Huffman tree reconstruction
            Stack stack = new Stack(256);
            int counter = 0;
            while (counter < this.headerLength) {
                if (header.charAt(counter) == '1') {
                    counter++;
                    stack.push(new Node((byte) Integer.parseInt(header.substring(counter, counter + 8), 2), 0));
                    counter += 8;
                } else {
                    counter++;
                    Node node = new Node(0);
                    node.setRight(stack.pop());
                    node.setLeft(stack.pop());
                    stack.push(node);
                }
            }

            this.rootNode = stack.peek();

            generateHuffmanCodes(this.rootNode, "", (byte) 0);

            String[] nameInfo = file.getName().split("\\.");

            this.fileName = nameInfo[0] + "." + extString;


            StringBuilder outFileName = new StringBuilder(fileName);
            getUniquName(outFileName);
            File outFile = new File(outFileName.toString());


            // Write the uncompressed data to a file
            FileOutputStream out = new FileOutputStream(outFile);
            int startIndex = serialData.length() - 8;
            int addedBits = Integer.parseInt(serialData.substring(startIndex), 2);
            serialData.delete(startIndex - addedBits, serialData.length());

            // Buffer for writing to the output file
            byte[] bufferOut = new byte[8];
            int counterForBufferSerialData = 0, counterForBufferOut = 0;

            // Process the serialized data to extract the original content
            while (counterForBufferSerialData < serialData.length()) {
                Node curr = rootNode;

                // Traverse the Huffman tree to find the corresponding byte
                while (curr != null && counterForBufferSerialData < serialData.length()) {
                    if (serialData.charAt(counterForBufferSerialData) == '0' && curr.getLeft() != null)
                        curr = curr.getLeft();
                    else if (curr.getRight() != null)
                        curr = curr.getRight();
                    else if (rootNode.getLeft() == null && rootNode.getRight() == null) {
                        counterForBufferSerialData++;
                        break;
                    } else
                        break;

                    counterForBufferSerialData++;
                }

                // Write the byte to the output buffer

                bufferOut[counterForBufferOut++] = curr.getCharCode();
                if (counterForBufferOut == 8) {
                    out.write(bufferOut);

                    counterForBufferOut = 0;
                }
            }

            // Write any remaining bytes in the buffer to the file
            if (counterForBufferOut > 0)
                out.write(bufferOut, 0, counterForBufferOut);

            out.close();

            lengthAfter = outFile.length();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Generates Huffman codes for each node in the tree recursively.
     * Sets Huffman code and length in each leaf node.
     * It traverses the Huffman tree and assigns a binary code to each leaf node based on its position in the tree.
     *
     * @param node  Current node in the Huffman tree
     * @param code  Current Huffman code being built
     * @param length Length of the current Huffman code
     */
    public void generateHuffmanCodes(Node node, String code, byte length) {
        if (node != null) {
            if (node.getLeft() == null && node.getRight() == null) { // Check if it's a leaf node
                // Setting the Huffman code and length for the node (not needed if just
                // printing)
                node.setHuffCode(code);
                node.setHuffLength(length);

                Byte charCode = node.getCharCode();
                if (charCode < 0)
                    nodes[node.getCharCode() + 256] = node;
                else
                    nodes[node.getCharCode()] = node;

            } else {
                // Recursively generate codes for left subtree
                generateHuffmanCodes(node.getLeft(), code + "0", (byte) (length + 1));
                // Recursively generate codes for right subtree
                generateHuffmanCodes(node.getRight(), code + "1", (byte) (length + 1));
            }
        }
    }

    public StringBuilder getHeader(Node root) { // post order
        StringBuilder builder = new StringBuilder();
        getHelper(root, builder);
        return builder;
    }

    private void getHelper(Node node, StringBuilder builder) {
        if (node == null)
            return;
        getHelper(node.getLeft(), builder);
        getHelper(node.getRight(), builder);

        if (node.getLeft() == null && node.getRight() == null) {
            byte charCode = node.getCharCode();
            builder.append("1" + convertByteToBinary(charCode));
        } else {
            builder.append("0");
        }
    }

    /**
     * Converts a byte into its binary string representation.
     * This method computes the binary string for the byte, from the most significant bit to the least significant.
     *
     * @param b Byte to be converted
     * @return A string representing the binary value of the byte
     */
    public static String convertByteToBinary(byte b) {
        StringBuilder binaryString = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            int bit = (b >> i) & 1;
            binaryString.append(bit);
        }
        return binaryString.toString();
    }

    /**
     * Creates and returns a TableView populated with Huffman coding data for each
     * character in the file.
     *
     * @return A fully initialized TableView with Huffman data.
     */
    private TableView<HuffCode> getTable() {
        TableView<HuffCode> table = new TableView<>();
        ObservableList<HuffCode> data = FXCollections.observableArrayList();

        // Populate the observable list with node data for display in the table
        for (Node node : nodes) {
            if (node != null) {
                data.add(new HuffCode(node.getCharCode(), node.getFreq(), node.getHuffCode(), node.getHuffLength()));
            }
        }

        // Set up table columns for character, frequency, Huffman code, code length
        TableColumn<HuffCode, String> charColumn = new TableColumn<>("Character");
        charColumn.setCellValueFactory(new PropertyValueFactory<>("charDisplay"));
        charColumn.setPrefWidth(150);

        TableColumn<HuffCode, String> codeColumn = new TableColumn<>("Huffman Code");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("huffCode"));
        codeColumn.setPrefWidth(180);

        TableColumn<HuffCode, Number> lengthColumn = new TableColumn<>("Code Length");
        lengthColumn.setCellValueFactory(new PropertyValueFactory<>("huffLength"));
        lengthColumn.setPrefWidth(150);



        // Style settings for table columns to enhance readability
        charColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 14px;");
        codeColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 14px;");
        lengthColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 14px;");

        // Apply custom row factory to adjust row styling dynamically
        table.setRowFactory(tv -> new TableRow<HuffCode>() {
            @Override
            protected void updateItem(HuffCode item, boolean empty) {
                super.updateItem(item, empty);
                setStyle(item == null || empty ? "" : "-fx-font-weight: bold; -fx-font-size: 14px;");
            }
        });

        // Add all columns to the table
        table.getColumns().add(charColumn);
        table.getColumns().add(codeColumn);
        table.getColumns().add(lengthColumn);
        table.setItems(data);
        table.setStyle("-fx-border-color: black; -fx-border-radius: 10; -fx-background-radius: 10;");

        return table;
    }
    /**
     * Creates a graphical representation of the decompression percentage.
     * The pane displays a percentage bar indicating the size reduction achieved through decompression.
     *
     * @return A Pane containing visual representation of the decompression percentage
     */
    private Pane getPercentagePane() {
        double percentage = ((double) lengthBefore -  lengthAfter);
        String percentageText = String.format("%.2f%%", ( percentage/lengthBefore) * 100);

        // Cap the percentage if it exceeds 100%
        if (percentage > 1) {
            percentage = 1;
            percentageText = "More than " + String.format("%.2f%%", ( percentage) * 100);
        }

        // Create the percentage display text
        Text percentageDisplay = new Text(percentageText);
        percentageDisplay.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 20));

        // Set colors based on the percentage being positive or negative
        Color textColor = percentage >= 0 ? Color.BLACK : Color.RED;
        Color barColor = percentage >= 0 ? Color.web("#90D26D") : Color.web("#FF6B6B");

        percentageDisplay.setFill(textColor);

        // Set Pane dimensions
        double paneWidth = 200;
        double paneHeight = 50;

        // Create the outer rectangle (background)
        Rectangle outerRectangle = new Rectangle(0, 0, paneWidth, paneHeight);
        outerRectangle.setFill(Color.LIGHTGRAY);
        outerRectangle.setArcWidth(20);
        outerRectangle.setArcHeight(20);
        outerRectangle.setStroke(Color.web("#FFF5E0"));
        outerRectangle.setStrokeWidth(3);

        // Create the filled rectangle
        double adjustedPercentage = Math.abs(percentage); // Ensure the width is always positive
        Rectangle filledRectangle = new Rectangle(0, 0, paneWidth * adjustedPercentage, paneHeight);
        filledRectangle.setFill(barColor);
        filledRectangle.setArcWidth(20);
        filledRectangle.setArcHeight(20);
        filledRectangle.setClip(new Rectangle(0, 0, paneWidth * adjustedPercentage, paneHeight));

        // Calculate and center the text within the pane
        percentageDisplay.setLayoutX((paneWidth - percentageDisplay.getBoundsInLocal().getWidth()) / 2);
        percentageDisplay.setLayoutY((paneHeight + percentageDisplay.getBoundsInLocal().getHeight()) / 2);

        // Set up the pane
        Pane pane = new Pane();
        pane.setPrefSize(paneWidth, paneHeight);
        pane.getChildren().addAll(outerRectangle, filledRectangle, percentageDisplay);

        // Ensure the Pane itself is centered in any container it is added to
        pane.setMinWidth(Region.USE_PREF_SIZE);
        pane.setMaxWidth(Region.USE_PREF_SIZE);

        return pane;
    }

    /**
     * Opens the directory containing the file.
     * This method attempts to open the system's default file manager at the specified directory path.
     *
     * @param dir Directory to open
     */
    private void openDirectory(String dir) {
        try {
            Desktop.getDesktop().open(new File(dir));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Ensures the decompressed file has a unique name by appending numbers if necessary.
     * This method modifies the file name if the target file already exists by adding an incremental number to ensure uniqueness.
     *
     * @param fileName The StringBuilder object containing the base file name
     */
    public void getUniquName(StringBuilder fileName) {
        // Create a File object based on the input file name.
        File file = new File(fileName.toString());
        // Initialize a counter and a flag for the while loop.
        int number = 1, flag = 0;
        // Loop to check if the file exists and modify the file name accordingly.
        while (file.exists()) {
            int lastDotIndex;
            if (flag == 0) {
                // Find the last dot (.) position to locate the extension.
                lastDotIndex = fileName.lastIndexOf(".");
                // Insert a number before the extension for the first time.
                fileName.insert(lastDotIndex, "(" + (number++) + ")");
            } else {
                // For subsequent iterations, remove the old number and add a new one.
                int startIndex = fileName.lastIndexOf("(");
                int endIndex = fileName.lastIndexOf(")") + 1;
                fileName.delete(startIndex, endIndex);
                lastDotIndex = fileName.lastIndexOf(".");
                fileName.insert(lastDotIndex, "(" + (number++) + ")");
            }
            // Update the file object with the new file name.
            file = new File(fileName.toString());
            // Set flag to 1 to indicate that the file name has been modified at least once.
            flag = 1;
        }
    }

    /**
     * Adds interactive and visual elements to the header decompression scene.
     * This method sets up various controls and displays including buttons, labels, and statistics about the decompression table.
     */
    private Scene getHeaderScene() {
        // Create a GridPane
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        // Create and style labels for descriptions (label1)
        Label extensionLengthLabel = new Label("Extension Length");
        extensionLengthLabel.setStyle("-fx-text-fill: #e80af6; " + "-fx-padding: 2; " + "-fx-font-size: 22px;");
        Label fileExtensionLabel = new Label("File Extension");
        fileExtensionLabel.setStyle("-fx-text-fill: #e80af6; " + "-fx-padding: 2; " + "-fx-font-size: 22px;");
        Label headerLengthLabel = new Label("Header Length");
        headerLengthLabel.setStyle("-fx-text-fill: #e80af6; " + "-fx-padding: 2; " + "-fx-font-size: 22px;");
        Label headerLabel = new Label("Header");
        headerLabel.setStyle("-fx-text-fill: #e80af6; " + "-fx-padding: 2; " + "-fx-font-size: 22px;");

        // Create and style labels for input (label2)
        Label extensionLengthValue = new Label(extLength + " Bytes"); // Example value
        extensionLengthValue.setStyle("-fx-text-fill: #2a2d85; " + "-fx-background-color: white; " + "-fx-padding: 10; "
                + "-fx-border-color: #e80af6; " + "-fx-border-radius: 5; " + "-fx-background-radius: 5; "
                + "-fx-font-size: 16px;");

        extensionLengthValue.setMinWidth(200);
        Label fileExtensionValue = new Label("." + extString); // Example value
        fileExtensionValue.setStyle("-fx-text-fill: #2a2d85; " + "-fx-background-color: white; " + "-fx-padding: 10; "
                + "-fx-border-color: #e80af6; " + "-fx-border-radius: 5; " + "-fx-background-radius: 5; "
                + "-fx-font-size: 16px;");
        fileExtensionValue.setMinWidth(200);

        Label headerLengthValue = new Label(this.headerLength + " Bits"); // Example value
        headerLengthValue.setStyle("-fx-text-fill: #2a2d85; " + "-fx-background-color: white; " + "-fx-padding: 10; "
                + "-fx-border-color: #e80af6; " + "-fx-border-radius: 5; " + "-fx-background-radius: 5; "
                + "-fx-font-size: 16px;");
        headerLengthValue.setMinWidth(200);

        // Create text area for header
        TextArea headerTextArea = new TextArea(this.header);
        headerTextArea.setWrapText(true);
        headerTextArea.setEditable(false);
        headerTextArea.setStyle("-fx-text-fill: #2a2d85; " + "-fx-background-color: white; " + "-fx-padding: 10; "
                + "-fx-border-color: #e80af6; " + "-fx-border-radius: 5; " + "-fx-background-radius: 5; "
                + "-fx-font-size: 16px;");

        // Adding all components to grid
        grid.add(extensionLengthLabel, 0, 0);
        grid.add(extensionLengthValue, 1, 0);
        grid.add(fileExtensionLabel, 0, 1);
        grid.add(fileExtensionValue, 1, 1);
        grid.add(headerLengthLabel, 0, 2);
        grid.add(headerLengthValue, 1, 2);
        grid.add(headerLabel, 0, 3);
        grid.add(headerTextArea, 1, 3);

        // Set up the primary stage
        Scene scene = new Scene(grid, 850, 450);

        return scene;

    }

    /**
     * Adds interactive and visual elements to the decompression scene.
     * This method sets up various controls and displays including buttons, labels, and statistics about the decompression process.
     */
    private void addFX() {
        // Huffman Table Label
        Label huffTableLabel = new Label("Huffman Table");
        huffTableLabel.setStyle("-fx-text-fill: #FFF5E0; " + "-fx-padding: 2; " + "-fx-font-size: 22px;");

        // File Name Label
        Label fileName = new Label("File Name : " + this.fileName);
        fileName.setStyle("-fx-text-fill: #FFF5E0; " + "-fx-padding: 2; " + "-fx-font-size: 22px;");

        // Table Box
        VBox tableBox = new VBox(10, huffTableLabel, getTable(), fileName);
        tableBox.setAlignment(Pos.CENTER);
        bp.setPadding(new Insets(15));
        BorderPane.setMargin(tableBox, new Insets(0, 100, 0, 100));
        bp.setCenter(tableBox);

        // Statistics Button
        Button statisticsButton = new Button("Statistics");
        statisticsButton.setStyle("-fx-font-size: 18px; -fx-background-color: #2f3394; -fx-text-fill: white;");
        statisticsButton.setMaxWidth(Double.MAX_VALUE);

        statisticsButton.setOnAction(e -> {
            // Create new Stage for Statistics
            Stage statsStage = new Stage();
            statsStage.setTitle("Compression Statistics");

            // Size Before and After Labels
            Label beforeLabel = new Label("Size Before : " + lengthBefore + " Byte");
            beforeLabel.setStyle("-fx-text-fill: #141E46; " + "-fx-background-color: white; " + "-fx-padding: 10; "
                    + "-fx-border-color: #41B06E; " + "-fx-border-radius: 5; " + "-fx-background-radius: 5; "
                    + "-fx-font-size: 16px;");
            beforeLabel.setAlignment(Pos.CENTER);

            Label afterLabel = new Label("Size After : " + lengthAfter + " Byte");
            afterLabel.setStyle("-fx-text-fill: #141E46; " + "-fx-background-color: white; " + "-fx-padding: 10; "
                    + "-fx-border-color: #41B06E; " + "-fx-border-radius: 5; " + "-fx-background-radius: 5; "
                    + "-fx-font-size: 16px;");
            afterLabel.setAlignment(Pos.CENTER);

            // Percentage Pane
            Pane percentagePane = getPercentagePane();

            // VBox for Statistics
            VBox statsVBox = new VBox(20, percentagePane, beforeLabel, afterLabel);
            statsVBox.setAlignment(Pos.CENTER);
            statsVBox.setPadding(new Insets(20));

            // Scene for Statistics Stage
            Scene statsScene = new Scene(statsVBox, 400, 300);
            statsStage.setScene(statsScene);
            statsStage.show();
        });

        // Open Directory Button
        Button openDirectoryButton = new Button("Open File Directory");
        openDirectoryButton.setStyle("-fx-font-size: 18px; -fx-background-color: #2f3394; -fx-text-fill: white;");
        openDirectoryButton.setOnAction(e -> openDirectory(System.getProperty("user.dir")));
        openDirectoryButton.setMaxWidth(Double.MAX_VALUE);

        // Header Information Button
        Button headerButton = new Button("Header Information");
        headerButton.setStyle("-fx-font-size: 18px; -fx-background-color: #2f3394; -fx-text-fill: white;");
        headerButton.setOnAction(e -> {
            Stage headerStage = new Stage();
            Scene headerScene = getHeaderScene();
            headerStage.setScene(headerScene);
            headerStage.setTitle("Header Information");
            headerStage.show();
        });
        headerButton.setMaxWidth(Double.MAX_VALUE);

        // Back Button
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 18px; -fx-background-color: #2f3394; -fx-text-fill: white;");
        backButton.setOnAction(e -> stage.setScene(scene));
        backButton.setMaxWidth(Double.MAX_VALUE);

        // Right Side VBox
        HBox rightBox = new HBox(20, statisticsButton, openDirectoryButton, headerButton, backButton);
        rightBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(rightBox, new Insets(0, 150, 0, 0));
        bp.setBottom(rightBox);

        // Set Background
        BackgroundImage bgImage = new BackgroundImage(
                new Image("file:///C:/Users/user/IdeaProjects/Algo_huffman/src/main/resources/com/example/algo_huffman/WhatsApp Image 2024-11-11 at 20.58.58_b02e5a96.jpg"),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(100, 100, true, true, false, true)
        );
        bp.setBackground(new Background(bgImage));
    }



}