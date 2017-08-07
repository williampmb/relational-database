package relationaldatabase2;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Result Message Class
 * 
 * @author jlexen
 */
public class ResultMessage {
    private Stage stage;
    
    public ResultMessage(final String resultText, final Object theParent) {
        try { 
            ResultController.result = resultText;
            Parent parent = FXMLLoader.load(theParent.getClass().getResource("/relationaldatabase2/Result.fxml"));
            stage = new Stage();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("Result");
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    /**
     * Shows result message
     */
    public void show() {
        stage.show();
        stage.setResizable(true);
    }
}
