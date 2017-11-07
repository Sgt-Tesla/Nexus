package com.meti;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author SirMathhman
 * @version 0.0.0
 * @since 11/7/2017
 */
public class ServerAdvancedCreator {
    private ServerBuilder serverBuilder;
    private Action<Integer> onMaxQueueSizeChanged;
    private Action<InetAddress> onLocalAddress;

    @FXML
    private TextField localAddressField;

    @FXML
    private TextField maxQueueSizeField;

    @FXML
    public void localAddressChanged() {
        try {
            InetAddress localAddress = InetAddress.getByName(localAddressField.getText());
            onLocalAddress.act(localAddress);
        } catch (UnknownHostException e) {
            try {
                Dialog dialog = Utility.openNewDialog();
                dialog.setMessageException(e);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    @FXML
    public void maxQueueSizeChanged() {
        try {
            Integer portInteger = Integer.valueOf(maxQueueSizeField.getText());

            if (portInteger > 0) {
                this.onMaxQueueSizeChanged.act(portInteger);
                this.serverBuilder.host();
            } else {
                Dialog dialog = Utility.openNewDialog();
                dialog.setMessageText("Max queue size entered is not greater or equal to 0");
            }
        } catch (NumberFormatException e) {
            try {
                Dialog dialog = Utility.openNewDialog();
                dialog.setMessageText("Max queue size entered is not a number");
            } catch (IOException e1) {
                //this is a funny exception (an exception caused by an exception, LOL)
                serverBuilder.getConsole().log(e1);
            }
        } catch (IOException e) {
            serverBuilder.getConsole().log(e);
        }
    }

    public void setServerBuilder(ServerBuilder serverBuilder) {
        this.serverBuilder = serverBuilder;
    }

    public void setOnMaxQueueSizeChanged(Action<Integer> onMaxQueueSizeChanged) {
        this.onMaxQueueSizeChanged = onMaxQueueSizeChanged;
    }

    public void setOnLocalAddress(Action<InetAddress> onLocalAddress) {
        this.onLocalAddress = onLocalAddress;
    }
}
