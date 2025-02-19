package com.example.algo_huffman;

public class Node {
    private Byte charCode;
    private int freq;
    private Node left, right;

    private String huffCode;
    private byte huffLength;


    public Node(int freq) {
        this.freq = freq;
    }

    public Node(byte charCode, int freq) {
        this.charCode = charCode;
        this.freq = freq;
    }

    public Byte getCharCode() {
        return charCode;
    }

    public void setCharCode(Byte charCode) {
        this.charCode = charCode;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public void setHuffCode(String huufCode) {
        this.huffCode = huufCode;
    }

    public String getHuffCode() {
        return huffCode;
    }

    public byte getHuffLength() {
        return huffLength;
    }

    public void setHuffLength(byte huffLength) {
        this.huffLength = huffLength;
    }

}