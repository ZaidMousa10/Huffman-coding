package com.example.algo_huffman;

public class HuffCode {
    private String charDisplay;
    private int frequency;
    private String huffCode;
    private int huffLength;

    // Constructor to initialize the fields
    public HuffCode(byte charCode, int freq, String huffCode, int huffLength) {
        this.charDisplay = String.valueOf((char) charCode); // Convert byte to char and then to String
        this.frequency = freq;                             // Assign frequency directly
        this.huffCode = huffCode;                          // Assign Huffman code directly
        this.huffLength = huffLength;                      // Assign Huffman length directly
    }

    // Getter for charDisplay
    public String getCharDisplay() {
        return charDisplay;
    }

    // Getter for frequency
    public int getFrequency() {
        return frequency;
    }

    // Getter for huffCode
    public String getHuffCode() {
        return huffCode;
    }

    // Getter for huffLength
    public int getHuffLength() {
        return huffLength;
    }

}
