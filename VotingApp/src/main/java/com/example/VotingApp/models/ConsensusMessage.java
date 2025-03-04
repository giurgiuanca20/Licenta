package com.example.VotingApp.models;


public class ConsensusMessage {
    private String type;  // "pre-prepare", "prepare", "commit"
    private Block block;
    private int sender;
    private int view;
    private int sequenceNumber;

    public ConsensusMessage() {

    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public Block getBlock() {
        return block;
    }

    public int getSender() {
        return sender;
    }

    public int getView() {
        return view;
    }
    public void setView(int view) {
        this.view = view;
    }
    public int getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
}
