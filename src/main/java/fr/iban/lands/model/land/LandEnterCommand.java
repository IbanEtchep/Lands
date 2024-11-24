package fr.iban.lands.model.land;

import java.util.UUID;

public class LandEnterCommand {

    private final UUID uniqueId;
    private String command;
    private boolean asConsole;

    public LandEnterCommand(String command, boolean asConsole) {
        this.uniqueId = UUID.randomUUID();
        this.command = command;
        this.asConsole = asConsole;
    }

    public LandEnterCommand(UUID uniqueId, String command, boolean asConsole) {
        this.uniqueId = uniqueId;
        this.command = command;
        this.asConsole = asConsole;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getCommand() {
        return command;
    }

    public boolean isAsConsole() {
        return asConsole;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setAsConsole(boolean asConsole) {
        this.asConsole = asConsole;
    }
}
