package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.scene.control.ProgressBar;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.Node;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.net.http.HttpRequest;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.StandardCharsets;

import java.util.LinkedList;
import java.lang.reflect.Array;
import java.net.URI;
import java.net.http.HttpResponse.BodyHandlers;
import java.lang.InterruptedException;
import java.lang.Math;
import java.util.Random;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import cs1302.api.Dictionary;
import cs1302.api.Definition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    Stage stage;
    Scene scene;

    VBox root;

    HBox welcomeLayer;
    HBox wordLayer;
    HBox defLayer;

    // HB 1
    Label welcome;

    // HB 2
    Button defButton;
    TextField wordField;

    // HB 3
    TextArea definition;

    private String finalWord = "null";
    private String partSpeech = "null";
    private String define = "null";
    private static final String API_KEY = "afb042e4-8d41-45aa-a86b-ce1fc8eb45f3";

    Timeline timeline;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        root = new VBox();

        this.scene = null;
        this.stage = null;

        this.welcomeLayer = new HBox(20);
        this.welcomeLayer.setMinHeight(30);
        this.wordLayer = new HBox(20);
        this.wordLayer.setMinHeight(30);
        this.defLayer = new HBox(20);

        // 1
        this.welcome = new Label("Part of Speech Definer");

        // 2
        this.defButton = new Button("Get Definition");
        this.wordField = new TextField("supercalifragilisticexpialidocious");

        // 3
        this.definition = new TextArea("The word you search will appear here.");
        this.definition.setWrapText(true);
        this.definition.setEditable(false);

        this.timeline = new Timeline();
    } // ApiApp

    /**
     * initializing method.
     */
    public void init() {
        System.out.println("init");
        // 1
        this.welcomeLayer.getChildren().add(this.welcome);
        welcomeLayer.setAlignment(Pos.CENTER);
        // 2
        this.wordLayer.getChildren().addAll(this.defButton, this.wordField);
        wordLayer.setAlignment(Pos.BASELINE_CENTER);
        // 3
        this.defLayer.getChildren().add(this.definition);

        // root
        this.root.getChildren().addAll(welcomeLayer, wordLayer, defLayer);

        EventHandler<ActionEvent> delay = (ActionEvent e) -> {

            System.out.println("delay");
            /**
               if (!this.defButton.isDisabled()){
               this.defButton.setDisable(true);
               } else {
            */
            this.defButton.setDisable(false);
            // } // if
        };

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(5), delay);
        timeline.setCycleCount(2);
        timeline.getKeyFrames().add(keyFrame);

        EventHandler<ActionEvent> define = (ActionEvent e) -> {

            String word = this.wordField.getText();

//            definition.setText(getDesc(word));

            try {
                displayDef(getDesc(word));
                getDesc2(this.partSpeech);
            } catch (IOException ioe) {
                if (!(ioe.getMessage() == null)) {
                    definition.setText(ioe.getMessage());
                    timeline.play();
                } else {
                    this.defButton.setDisable(true);
                    definition.setText("Invalid word. Try again.");
//                    timeline.stop();
                    timeline.play();
                } // if
            } // try

        }; // defButton

        defButton.setOnAction(define);

    } // init


    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {

        this.stage = stage;

        // setup scene

        scene = new Scene(root);

        // setup stage
        stage.setTitle("Part Of Speech - Word Finder");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();

    } // start

    /**
     * builds the URL based on the word the user typed in.
     * @return the URL from the user's word.
     * @param word to be used in search
     * @param mode indicates which api to construct for
     */
    public String getUrl(String word, boolean mode) {
        String url = "";

        if (mode) {
            url = "https://api.dictionaryapi.dev/api/v2/entries/en/";
            url += word.strip().toLowerCase();
        } else {
            url = ("https://www.dictionaryapi.com/api/v3/references/collegiate/json/" +
            word.strip().toLowerCase() + "?key=" + API_KEY);

        } // if

        return url;
    } // getUrl

    /**
     * Fetches the json formatted link from the given {@url}.
     * @param url the link to be parsed
     * @return json formated string based on {@code url}.
     */
    public static String fetchJson(String url) throws IOException {

        String body = "";
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
            body = response.body();
        } catch (Exception e) {
            throw new IOException();
        } // try

        return body;
    } // fetchJson

    /**
     * builds the dictionary object for the given word.
     * @return the array object for API one (dictionary).
     * @param word used in API search
     */
    public Dictionary[] getDesc(String word) throws IOException {
        String json = "null";

        int string = 0;
        int target = 0;
        String msg = "";


        String url = getUrl(word, true);
        json = fetchJson(url);

        System.out.println("Received JSON: " + json);

        if (!json.substring(0,1).equals("[")) {

            if (!finalWord.equals(word)) {
                string = json.indexOf("ge\":");
                target = string + 5;
                msg = json.substring(target, target + 74);
            } else {
                string = json.indexOf("on\":");
                target = string + 5;
                msg = json.substring(target, target + 70);
                this.defButton.setDisable(true);
            } // if

            System.out.println(msg);

            this.finalWord = word;
            throw new IOException(msg);
        } // if

        Dictionary[] dictionary = GSON.fromJson(json, Dictionary[].class);
        this.finalWord = word;
        return dictionary;
    } // getTune

    /**
     * builds the word object for the given word.
     * @param word used in API search
     */
    public void getDesc2(String word) throws IOException {

        String json = "null";
        String url = getUrl(word, false);
        System.out.println("String 2: " + url);

        json = fetchJson(url);
        System.out.println("JSON: " + json);
        Word[] words = GSON.fromJson(json, Word[].class);

        String speech = (partSpeech.substring(0,1).toUpperCase() + partSpeech.substring(1));
        System.out.println(speech);

        String defText = (this.define + "\n\n\n" +
            speech + ": " + words[0].shortdef[0]);
        System.out.println(words[0].shortdef[0]);
        System.out.println(defText);

        this.definition.setText(defText);
    } // getDesc2

    /**
     * displays the dictionary object's characteristics in the text area.
     * @param dictionary array object containing the dictionary
     */
    public void displayDef(Dictionary[] dictionary) {

        System.out.println(dictionary[0].meanings[0].definitions[0].definition);
        this.partSpeech = dictionary[0].meanings[0].partOfSpeech;

        String def = "";
        def += ("Word: " + dictionary[0].word);
        if (dictionary[0].phonetic != null) {
            def += ("\nPhonetic: " + dictionary[0].phonetic);
        } // if

        def += ("\nDefinition: " + dictionary[0].meanings[0].definitions[0].definition);
        def += ("\nPart of Speech: " + dictionary[0].meanings[0].partOfSpeech);

        this.define = def;
        this.definition.setText(def);

    } // Dictionary

} // ApiApp
