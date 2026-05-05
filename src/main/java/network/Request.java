package network;

import java.io.Serializable;

public class Request implements Serializable {

    private CommandType commandType;
    private Object data;

    public Request() {
    }

    public Request(CommandType commandType, Object data) {
        this.commandType = commandType;
        this.data = data;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public void setCommandType(CommandType commandType) {
        this.commandType = commandType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}