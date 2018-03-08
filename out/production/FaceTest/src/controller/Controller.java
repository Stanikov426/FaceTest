package controller;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    Image imagePom = null;
    File file;
    public static final String subscriptionKey = "12e27f241694404d98812ab476a8028a";
    public static final String uriBase = "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect";


    @FXML
    private Button sendButton;

    @FXML
    private Button chooseButton;

    @FXML
    private ImageView image;

    @FXML
    private Label gender;

    @FXML
    private Label age;

    @FXML
    private Label smile;

    @FXML
    private Label glasses;

    @FXML
    private Label faceHair;

    @FXML
    private Label emotion2;

    @FXML
    private Label hair;

    @FXML
    private Label emotion1;

    @FXML
    private Label emotion3;

    @FXML
    private Label emotion4;

    @FXML
    private Label emotion5;

    @FXML
    private TextField urlImage;

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    void chooseClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);

        //Show open file dialog
        file = fileChooser.showOpenDialog(null);

        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            URL myUrl = file.toURI().toURL();
            System.out.println(myUrl);
            imagePom = SwingFXUtils.toFXImage(bufferedImage, null);
            image.setImage(imagePom);
        } catch (IOException ex) {
            alert("Something is wrong with photo");
        }
        setLabels();
        choiceBox.getSelectionModel().select(0);
    }

    @FXML
    void sendClick(ActionEvent event) {
        if(choiceBox.getValue()==null){
            alert("Check image url or choose something!!!");
        }else if(choiceBox.getValue().equals("Use URL link")){
            if(urlImage.getText().equals("")){
                alert("Check image url or choose something!!!");
            }else{
                setData(1);
            }
        }else if(choiceBox.getValue().equals("Use local photo")&&imagePom!=null){
            setData(0);
        } else{
            alert("Check image url or choose something!!!");
        }
    }

    private void setLabels(){
        gender.setText("");
        age.setText("");
        smile.setText("");
        glasses.setText("");
        faceHair.setText("");
        emotion1.setText("");
        emotion2.setText("");
        emotion3.setText("");
        emotion4.setText("");
        emotion5.setText("");
        hair.setText("");
        urlImage.setText("");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setLabels();
        choiceBox.getItems().addAll("Use local photo", "Use URL link");
    }
    private void alert(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ups...");
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }

    private void setData(int option){
        HttpClient httpclient = new DefaultHttpClient();
        try
        {
            URIBuilder builder = new URIBuilder(uriBase);

            // Request parameters. All of them are optional.
            builder.setParameter("returnFaceId", "true");
            builder.setParameter("returnFaceLandmarks", "false");
            builder.setParameter("returnFaceAttributes", "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise");

            // Prepare the URI for the REST API call.
            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);

            if(option==0){
                // Request headers.
                request.setHeader("Content-Type", "application/octet-stream");
                request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

                // Request body.
                FileEntity reqEntity = new FileEntity(file, ContentType.APPLICATION_OCTET_STREAM);
                request.setEntity(reqEntity);
            }else if(option==1){
                // Request headers.
                request.setHeader("Content-Type", "application/json");
                request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

                // Request body.
                StringEntity reqEntity = new StringEntity("{\"url\":\"" + urlImage.getText() + "\"}");
                request.setEntity(reqEntity);
                Image urlPom = new Image(urlImage.getText());
                image.setImage(urlPom);
            }

            // Execute the REST API call and get the response entity.
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                // Format and display the JSON response.
                System.out.println("REST Response:\n");

                String jsonString = EntityUtils.toString(entity).trim();

                if (jsonString.charAt(0) == '[') {
                    JSONArray jsonArray = new JSONArray(jsonString);
                    //System.out.println(jsonArray.toString(2));
                    JSONObject main = jsonArray.getJSONObject(0);
                    System.out.println(main.toString());
                    JSONObject face = main.getJSONObject("faceAttributes");
                    gender.setText("Your gender is "+face.getString("gender"));
                    age.setText("Age: "+(int)(face.getDouble("age")));
                    smile.setText("Smile: "+ (int)(face.getDouble("smile")*100)+"%");
                    glasses.setText("Glasses: "+face.getString("glasses"));
                    JSONObject faceHairs =  face.getJSONObject("facialHair");
                    faceHair.setText("Beard: "+(int)(faceHairs.getDouble("beard")*100)+"%, moustache: "+(int)(faceHairs.getDouble("moustache")*100)+"%");
                    JSONObject emotions = face.getJSONObject("emotion");
                    emotion1.setText("Emotions");
                    emotion2.setText("contemp: "+(int)(emotions.getDouble("contempt")*100)+"%, surprise: " +(int)(emotions.getDouble("surprise")*100)+"%");
                    emotion3.setText("happinese: "+(int)(emotions.getDouble("happiness")*100)+"%, neutral: "+(int)(emotions.getDouble("neutral")*100)+"%");
                    emotion4.setText("sadness: "+(int)(emotions.getDouble("sadness")*100)+"%, anger: "+(int)(emotions.getDouble("anger")*100)+"%");
                    emotion5.setText("fear: "+(int)(emotions.getDouble("fear")*100)+"%");
                    JSONObject hairPom = face.getJSONObject("hair");
                    JSONObject pom = hairPom.getJSONArray("hairColor").getJSONObject(0);
                    hair.setText("Hair color: "+pom.getString("color")+" confidence: "+(int)(pom.getDouble("confidence")*100)+"%");
                }
                else if (jsonString.charAt(0) == '{') {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    System.out.println(jsonObject.toString(2));
                } else {
                    System.out.println(jsonString);
                }

            }
        }
        catch (Exception e)
        {
            // Display error message.
            System.out.println(e.getMessage());
            alert("Cant read image :(");
        }
    }
}