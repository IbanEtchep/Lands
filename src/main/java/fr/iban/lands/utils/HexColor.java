package fr.iban.lands.utils;

public enum HexColor {
    OLIVE("#708d23"),
    MARRON("#B95608"),
    MARRON_CLAIR("#fbeba7"),
    ROUGE_ORANGE("#ee2400"),
    FLAT_BLUE("#0984e3"),
    FLAT_BLUE_GREEN("#00cec9"),
    FLAT_PURPLE("#6c5ce7"),
    FLAT_LIGHT_RED("#ff7675"),
    FLAT_RED("#d63031"),
    FLAT_PINK("#e84393"),
    FLAT_GREEN("#00b894"),
    FLAT_LIGHT_GREEN("#55efc4");

    private final String hex;

    HexColor(String hex) {
        this.hex = hex;
    }

    public String getHex() {
        return hex;
    }
}
