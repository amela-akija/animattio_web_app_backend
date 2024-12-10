package com.animattio.animattio_web_app_backend.game;

import java.util.Date;
import java.util.List;

/**
 * Represents a game object in the system.
 * This class stores information about a game session, including error counts,
 * reaction times, shown images, and game-specific metadata.
 */
public class Game {

    /**
     * The number of commission errors during the game.
     */
    private int comissionErrors;

    /**
     * The hit rate (percentage of correct responses) in the game.
     */
    private int hitRate;

    /**
     * The unique identifier for the game.
     */
    private String id;

    /**
     * The number of omission errors during the game.
     */
    private int omissionErrors;

    /**
     * List of intervals used in the game.
     */
    private List<Integer> intervals;

    /**
     * List of reaction times recorded during the game.
     */
    private List<Integer> reactionTimes;

    /**
     * List of images shown during the game.
     */
    private List<String> shownImages;

    /**
     * List of boolean results indicating correctness for each attempt.
     * True represents a correct response, and false represents an incorrect one.
     */
    private List<Boolean> result;

    /**
     * The mode of the game.
     */
    private String mode;

    /**
     * The target stimulus used in the game.
     */
    private String stimuli;

    /**
     * The theme of the game.
     */
    private String theme;

    /**
     * The timestamp indicating when the game session occurred.
     */
    private Date timestamp;

    // Getters and Setters
    public int getComissionErrors() {
        return comissionErrors;
    }

    public void setComissionErrors(int comissionErrors) {
        this.comissionErrors = comissionErrors;
    }

    public int getHitRate() {
        return hitRate;
    }

    public void setHitRate(int hitRate) {
        this.hitRate = hitRate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOmissionErrors() {
        return omissionErrors;
    }

    public void setOmissionErrors(int omissionErrors) {
        this.omissionErrors = omissionErrors;
    }

    public List<Integer> getIntervals() {
        return intervals;
    }

    public void setIntervals(List<Integer> intervals) {
        this.intervals = intervals;
    }

    public List<Integer> getReactionTimes() {
        return reactionTimes;
    }

    public void setReactionTimes(List<Integer> reactionTimes) {
        this.reactionTimes = reactionTimes;
    }

    public List<String> getShownImages() {
        return shownImages;
    }

    public void setShownImages(List<String> shownImages) {
        this.shownImages = shownImages;
    }

    public List<Boolean> getResult() {
        return result;
    }

    public void setResult(List<Boolean> result) {
        this.result = result;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getStimuli() {
        return stimuli;
    }

    public void setStimuli(String stimuli) {
        this.stimuli = stimuli;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
