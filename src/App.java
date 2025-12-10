import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Stack;

public class App extends Application {

    private final Stack<String> stack = new Stack<>();
    private final VBox stackPanel = new VBox(5);
    private final Label infoLabel = new Label();
    private final Label sizeLabel = new Label();

    // HP system
    private int hp = 3;
    private Label hpLabel;
    private ImageView heartView;

    // UI controls we need to disable during animations
    private Button pushBtn, popBtn, peekBtn, searchBtn, emptyBtn, sizeBtn;
    private TextField inputField;

    @Override
    public void start(Stage stage) {

        // ----- START SCREEN -----
        VBox startScreen = new VBox(20);
        startScreen.setAlignment(Pos.CENTER);
        startScreen.setStyle("-fx-background-color: black;");

        Label title = new Label("RETRO STACK GAME");
        title.setTextFill(Color.CYAN);
        title.setFont(Font.font("Consolas", 36));

        Label creator = new Label("Created by Ed James Ursal");
        creator.setTextFill(Color.LIME);
        creator.setFont(Font.font("Consolas", 24));

        Label pressAnyKey = new Label("Press ANY KEY to start");
        pressAnyKey.setTextFill(Color.MAGENTA);
        pressAnyKey.setFont(Font.font("Consolas", 20));

        startScreen.getChildren().addAll(title, creator, pressAnyKey);
        Scene startScene = new Scene(startScreen, 900, 550);

        stage.setScene(startScene);
        stage.setTitle("Retro Stack Game");
        stage.show();

        // ----- MAIN GAME UI -----

        // Load icons from your Windows absolute path (use your exact path)
        // Make sure these files exist at these locations:
        // C:\Users\ED\Desktop\ED Project Java\StackV2\Images\Profile.PNG
        // C:\Users\ED\Desktop\ED Project Java\StackV2\Images\HP.png
        Image heartImg = new Image("file:/C:/Users/ED/Desktop/ED Project Java/StackV2/Images/HP.png", false);
        heartView = new ImageView(heartImg);
        heartView.setFitWidth(45);
        heartView.setFitHeight(45);

        Image profileImg = new Image("file:/C:/Users/ED/Desktop/ED Project Java/StackV2/Images/Profile.PNG", false);
        ImageView profileView = new ImageView(profileImg);
        profileView.setFitWidth(70);
        profileView.setFitHeight(70);

        hpLabel = new Label("HP: " + hp);
        hpLabel.setTextFill(Color.RED);
        hpLabel.setFont(Font.font("Consolas", 22));

        HBox playerBar = new HBox(20, profileView, heartView, hpLabel);
        playerBar.setAlignment(Pos.CENTER_LEFT);

        // Stack visual panel
        stackPanel.setStyle("-fx-background-color: #111; -fx-padding: 10; -fx-border-color: #0FF; -fx-border-width: 3px;");
        stackPanel.setAlignment(Pos.TOP_CENTER);
        stackPanel.setPrefHeight(250);

        // Input & buttons
        inputField = new TextField();
        inputField.setPromptText("Enter item");

        pushBtn = createRetroButton("Push");
        popBtn = createRetroButton("Pop");
        peekBtn = createRetroButton("Peek");
        searchBtn = createRetroButton("Search");
        emptyBtn = createRetroButton("Empty");
        sizeBtn = createRetroButton("Size");

        // Hook actions
        pushBtn.setOnAction(e -> pushItem(inputField.getText()));
        popBtn.setOnAction(e -> popItem());
        peekBtn.setOnAction(e -> peekItem());
        searchBtn.setOnAction(e -> searchItem(inputField.getText()));
        emptyBtn.setOnAction(e -> checkEmpty());
        sizeBtn.setOnAction(e -> checkSize());

        HBox controls = new HBox(10, inputField, pushBtn, popBtn, peekBtn, searchBtn, emptyBtn, sizeBtn);
        controls.setAlignment(Pos.CENTER);

        infoLabel.setTextFill(Color.LIME);
        infoLabel.setFont(Font.font("Consolas", 16));
        sizeLabel.setTextFill(Color.CYAN);
        sizeLabel.setFont(Font.font("Consolas", 16));
        infoLabel.setText("Stack is empty");
        sizeLabel.setText("Size: 0");

        VBox mainGame = new VBox(20, playerBar, stackPanel, controls, infoLabel, sizeLabel);
        mainGame.setStyle("-fx-background-color: black; -fx-padding: 20;");

        Scene mainScene = new Scene(mainGame, 900, 550);

        // Start when any key is pressed
        startScene.addEventFilter(KeyEvent.KEY_PRESSED, e -> stage.setScene(mainScene));
    }

    private Button createRetroButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: black; -fx-text-fill: #0FF; -fx-font-family: 'Consolas'; -fx-font-size: 14px; -fx-border-color: #0FF; -fx-border-width: 2px;");
        return btn;
    }

    // Damage (HP) animation
    private void damage() {
        if (hp > 0) hp--;
        hpLabel.setText("HP: " + hp);

        FadeTransition ft = new FadeTransition(Duration.millis(200), heartView);
        ft.setFromValue(1.0);
        ft.setToValue(0.2);
        ft.setAutoReverse(true);
        ft.setCycleCount(2);
        ft.play();
    }

    // ---------- CORE STACK FUNCTIONS ----------

    // Push: add trimmed item to stack (top)
    private void pushItem(String rawItem) {
        String item = rawItem == null ? "" : rawItem.trim();
        if (item.isEmpty()) {
            infoLabel.setText("Cannot push empty value!");
            damage(); // optional: keep existing damage behavior on bad input
            return;
        }

        // push to logical stack
        stack.push(item);
        infoLabel.setText("Pushed: " + item);
        sizeLabel.setText("Size: " + stack.size());

        // Rebuild visual to guarantee consistency, then animate top element in
        refreshDisplayWithTopAnimation();
    }

    // Pop: remove top element and animate removal
    private void popItem() {
        if (stack.isEmpty()) {
            infoLabel.setText("Stack empty! Nothing to pop!");
            damage();
            return;
        }

        // Pop logical stack
        String popped = stack.pop();
        infoLabel.setText("Popped: " + popped);
        sizeLabel.setText("Size: " + stack.size());

        // Animate removal of current top visual (if exists) and then rebuild display
        if (!stackPanel.getChildren().isEmpty()) {
            disableControlsFor(Duration.millis(300));
            // remove the visual top (index 0)
            Label top = (Label) stackPanel.getChildren().get(0);
            TranslateTransition tt = new TranslateTransition(Duration.millis(300), top);
            tt.setToY(-50);
            tt.setOnFinished(e -> refreshDisplay()); // rebuild to ensure exact mapping
            tt.play();
        } else {
            // fallback: just refresh
            refreshDisplay();
        }
    }

    // Peek: show top element without removing it
    private void peekItem() {
        if (stack.isEmpty()) {
            infoLabel.setText("Stack is empty!");
            damage();
            return;
        }

        String top = stack.peek();
        infoLabel.setText("Top: " + top);

        // highlight the top visual (if present)
        if (!stackPanel.getChildren().isEmpty()) {
            Label lbl = (Label) stackPanel.getChildren().get(0);
            highlightLabel(lbl, Color.YELLOW, Duration.millis(500));
        } else {
            // If visuals are out of sync, rebuild and then highlight
            refreshDisplay();
            if (!stackPanel.getChildren().isEmpty()) {
                Label lbl = (Label) stackPanel.getChildren().get(0);
                highlightLabel(lbl, Color.YELLOW, Duration.millis(500));
            }
        }
    }

    // Search: uses Stack.search (1-based from top) and highlights that element
    private void searchItem(String rawItem) {
        String item = rawItem == null ? "" : rawItem.trim();
        if (item.isEmpty()) {
            infoLabel.setText("Enter a value to search!");
            damage();
            return;
        }

        // search returns 1-based position from top, -1 if not found
        int pos = stack.search(item);
        if (pos == -1) {
            infoLabel.setText("Item not found!");
            damage();
            return;
        }

        infoLabel.setText(item + " found at pos (from top): " + pos);

        // Map to visual index: top element is stackPanel child 0
        int index = pos - 1;

        // Safety: if visuals are out of sync, rebuild first
        if (index < 0 || index >= stackPanel.getChildren().size()) {
            refreshDisplay(); // rebuild now visuals
        }

        if (index >= 0 && index < stackPanel.getChildren().size()) {
            Label lbl = (Label) stackPanel.getChildren().get(index);
            highlightLabel(lbl, Color.ORANGE, Duration.millis(600));
        } else {
            // If still out of bounds (shouldn't happen), show simple message
            infoLabel.setText(item + " found at pos (from top): " + pos + " (visual sync failed)");
        }
    }

    // empty(): true if no elements, false otherwise (mirrors Stack.empty())
    private void checkEmpty() {
        boolean isEmpty = stack.isEmpty();
        infoLabel.setText("Stack empty(): " + isEmpty);
        sizeLabel.setText("Size: " + stack.size());
    }

    // size(): show current size
   private void checkSize() {
    infoLabel.setText("Stack size: " + stack.size());
    infoLabel.setTextFill(Color.YELLOW); // <-- change this to your desired color
    sizeLabel.setText("Size: " + stack.size());
   }

    // ---------- UI HELPER METHODS ----------+

    // Rebuild the visual stack panel from the logical stack (top -> index 0)
    private void refreshDisplay() {
        Platform.runLater(() -> {
            stackPanel.getChildren().clear();
            // iterate from top (stack.size()-1) down to bottom (0)
            for (int i = stack.size() - 1; i >= 0; i--) {
                String val = stack.get(i);
                Label lbl = createStackLabel(val, Color.RED);
                stackPanel.getChildren().add(lbl); // top will end up at index 0
            }
        });
    }

    // Rebuild display and animate the newly-added top item (if any)
    private void refreshDisplayWithTopAnimation() {
        Platform.runLater(() -> {
            stackPanel.getChildren().clear();
            for (int i = stack.size() - 1; i >= 0; i--) {
                String val = stack.get(i);
                Label lbl = createStackLabel(val, Color.LIME);
                stackPanel.getChildren().add(lbl);
            }

            // animate the top if exists
            if (!stackPanel.getChildren().isEmpty()) {
                Label top = (Label) stackPanel.getChildren().get(0);
                top.setTranslateY(-30);
                disableControlsFor(Duration.millis(300));
                TranslateTransition tt = new TranslateTransition(Duration.millis(300), top);
                tt.setToY(0);
                tt.play();
            }
        });
    }

    // highlight with a temporary color and fade animation
    private void highlightLabel(Label lbl, Color highlightColor, Duration duration) {
        // animate a color change -> fade -> restore
        Color original = (Color) lbl.getTextFill();
        lbl.setTextFill(highlightColor);

        FadeTransition ft = new FadeTransition(duration, lbl);
        ft.setFromValue(1.0);
        ft.setToValue(0.3);
        ft.setAutoReverse(true);
        ft.setCycleCount(2);
        ft.setOnFinished(e -> lbl.setTextFill(original));
        disableControlsFor(duration);
        ft.play();
    }

    // disable primary controls briefly to avoid overlapping actions during animation
    private void disableControlsFor(Duration d) {
        pushBtn.setDisable(true);
        popBtn.setDisable(true);
        peekBtn.setDisable(true);
        searchBtn.setDisable(true);
        emptyBtn.setDisable(true);
        sizeBtn.setDisable(true);
        inputField.setDisable(true);

        // schedule re-enable after duration
        new Thread(() -> {
            try {
                Thread.sleep((long) d.toMillis() + 20);
            } catch (InterruptedException ignored) { }
            Platform.runLater(() -> {
                pushBtn.setDisable(false);
                popBtn.setDisable(false);
                peekBtn.setDisable(false);
                searchBtn.setDisable(false);
                emptyBtn.setDisable(false);
                sizeBtn.setDisable(false);
                inputField.setDisable(false);
            });
        }).start();
    }

    // Create styled label for items
    private Label createStackLabel(String text, Color color) {
        Label lbl = new Label(text);
        lbl.setTextFill(color);
        lbl.setFont(Font.font("Consolas", 18));
        lbl.setStyle("-fx-background-color: #222; -fx-padding: 8; -fx-border-color: #0FF; -fx-border-width: 2;");
        lbl.setAlignment(Pos.CENTER);
        lbl.setMaxWidth(Double.MAX_VALUE);
        return lbl;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
