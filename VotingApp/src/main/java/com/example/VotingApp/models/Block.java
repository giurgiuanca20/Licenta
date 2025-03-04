package com.example.VotingApp.models;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class Block {
    private int index;
    private long timestamp;
    private List<String> votes;
    private String previousHash;
    private String hash;

    public Block() {

    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setVotes(List<String> votes) {
        this.votes = votes;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getIndex() {
        return index;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<String> getVotes() {
        return votes;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public String getHash() {
        return hash;
    }

    // Metodă pentru calculul hash-ului (folosind, de exemplu, SHA-256)
    public static String calculateHash(Block block) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String text = block.index + Long.toString(block.timestamp) + block.votes.toString() + block.previousHash;
            byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}