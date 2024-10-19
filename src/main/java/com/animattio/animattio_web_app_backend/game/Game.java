package com.animattio.animattio_web_app_backend.game;

import java.util.Date;
import java.util.List;

public class Game {
    private int comissionErrors;
    private int hitRate;
    private String id;
    private int omissionErrors;
    private List<Integer> intervals;
    private List<Integer> reactionTimes;
    private List<String> shownImages;
    private List<Boolean> result;
    private String mode;
    private String stimuli;
    private String theme;
    private Date timestamp;

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
