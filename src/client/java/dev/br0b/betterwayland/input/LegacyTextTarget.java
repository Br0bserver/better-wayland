//#if MC < 260100
package dev.br0b.betterwayland.input;

public interface LegacyTextTarget {
    String betterwayland$getSuggestion();

    void betterwayland$setPreedit(String preedit, int caret, String fallbackSuggestion);

    int[] betterwayland$getCandidateRectangle();
}
//#endif
