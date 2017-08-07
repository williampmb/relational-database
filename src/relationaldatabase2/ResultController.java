package relationaldatabase2;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;



/**
 * Controls Result screen
 *
 * @author williampmb
 */
public class ResultController implements Initializable {
    
    static String result = "result";
    @FXML
    Label lResult;
    @FXML
    Button btnClose;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lResult.setText(result);
    } 
    
    @FXML
    void close(){
        Stage result = (Stage) btnClose.getScene().getWindow();
        result.close();
    }
}
