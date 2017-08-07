package relationaldatabase2.error;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Error Message Class
 * 
 * @author jlexen
 */
public class ErrorMessage {
    private Stage stage;
    
    public ErrorMessage(final String errorText, final Object theParent) {
        try { 
            ErrorController.error = errorText;
            Parent parent = FXMLLoader.load(theParent.getClass().getResource("/relationaldatabase2/error/Error.fxml"));
            stage = new Stage();
            Scene scene = new Scene(parent);
            stage.setScene(scene);
            stage.setTitle("Error");
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
    
    /**
     * Shows error message
     */
    public void show() {
        stage.show();
        stage.setResizable(false);
    }
}
