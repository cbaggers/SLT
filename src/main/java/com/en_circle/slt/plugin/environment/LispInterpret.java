package com.en_circle.slt.plugin.environment;

public enum LispInterpret {

    ABCL(":abcl"),
    SBCL(":sbcl"),
    CCL(":ccl"),
    ALLEGRO(":allegro")

    ;

    public final String symbolName;

    LispInterpret(String symbolName) {
        this.symbolName = symbolName;
    }

}
