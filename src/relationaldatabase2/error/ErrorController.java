package relationaldatabase2.error;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;



/**
 * Controls Error screen
 *
 * @author williampmb
 */
public class ErrorController implements Initializable {
    
    static String error = "error";
    @FXML
    Label lError;
    @FXML
    Button btnClose;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lError.setText(error);
    } 
    
    @FXML
    void close(){
        Stage erro = (Stage) btnClose.getScene().getWindow();
        erro.close();
    }
}
