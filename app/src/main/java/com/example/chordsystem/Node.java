package com.example.chordsystem;

public class Node {

    private int id = -1;
    private int fakeId = -1;
    private Node succNode = null;
    private Node predNode = null;

    private boolean isRealNode = false;

    private String data = "example data";

    public Node(boolean real) {
        isRealNode = real;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setFakeId(int fakeId) {
        this.fakeId = fakeId;
    }

    public int getFakeId() {
        return fakeId;
    }

    public void setSuccNode(Node node) {
        succNode = node;
    }

    public Node getSuccNode() {
        return succNode;
    }

    public void setPredNode(Node node) {
        predNode = node;
    }

    public Node getPredNode() {
        return predNode;
    }

    public boolean isRealNode() {
        return isRealNode;
    }

    public String getData() {
        return data;
    }

}
