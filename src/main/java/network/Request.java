package network;

import java.io.Serializable;

public class Request implements Serializable {

    private String clientName;
    private CommandType commandType;
    private Object data;

    public Request() {
    }

    public Request(CommandType commandType, Object data) {
        this(null, commandType, data);
    }

    public Request(String clientName, CommandType commandType, Object data) {
        this.clientName = clientName;
        this.commandType = commandType;
        this.data = data;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    @Override
    public String toString() {
        return "Request{" +
                "clientName='" + clientName + '\'' +
                ", commandType=" + commandType +
                ", data=" + data +
                '}';
    }
}