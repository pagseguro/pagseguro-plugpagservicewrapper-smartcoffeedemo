package br.com.uol.pagseguro.smartcoffee.otherFeatures.softwarecapability;

public class SoftwareCapability {
    private final int index;
    private final int mode;
    private final String name;
    private boolean has = false;
    private boolean hasParameter = false;

    public SoftwareCapability(int index, String name) {
        this.index = index;
        this.name = name;
        this.mode = -1;
    }

    public SoftwareCapability(int index, int mode, String name) {
        this.index = index;
        this.mode = mode;
        this.name = name;
        this.hasParameter = true;
    }

    public int getIndex() {
        return index;
    }

    public int getMode() {
        return mode;
    }

    public String getName() {
        return name;
    }

    public boolean getHas() {
        return has;
    }

    public void setHas(boolean has) {
        this.has = has;
    }

    public boolean hasParameter() {
        return hasParameter;
    }

    public String getMessage() {
        String message;

        message = getName();

        if (this.hasParameter()) {
            message += ("[" + this.mode + "]");
        }

        message += (" : " + (this.getHas() ? "OK" : "NO") + "\n");

        return message;
    }
}
