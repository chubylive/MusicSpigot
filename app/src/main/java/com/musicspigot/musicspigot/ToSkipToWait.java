package com.musicspigot.musicspigot;

import java.io.Serializable;
import java.util.Random;

public class ToSkipToWait implements Serializable {
    private int countTillSkipWait;
    private int numToSkipWait;
    private int maxWaitTime;
    private int minWaitTime;
    private int changeStep;


    public enum SkipState {
        RAMP_UP, RAMP_DOWN, STEADY, RANDOM
    }

    public enum ChangeState {CHG_MAX_TIME, CHG_MIN_TIME, CHG_STEP, CHG_NUM_TILL_SKIP}

    ;
    private SkipState skipState;


    private ChangeState changeState;

    public ToSkipToWait(int numToSkipWait, int maxWaitTime, int minWaitTime, int changeStep, SkipState skipState) {
        this.numToSkipWait = numToSkipWait;
        this.maxWaitTime = maxWaitTime;
        this.minWaitTime = minWaitTime;
        this.changeStep = changeStep;
        this.skipState = skipState;
        this.countTillSkipWait = 0;
    }

    //steady state
    public ToSkipToWait(int numToSkipWait, int maxWaitTime, SkipState skipState) {
        this.numToSkipWait = numToSkipWait;
        this.maxWaitTime = maxWaitTime;
        this.skipState = skipState;
        this.countTillSkipWait = 0;
    }

    //random state
    public ToSkipToWait(int numToSkipWait, int maxWaitTime, int minWaitTime, SkipState skipState) {
        this.numToSkipWait = numToSkipWait;
        this.maxWaitTime = maxWaitTime;
        this.minWaitTime = minWaitTime;
        this.skipState = skipState;
        this.countTillSkipWait = 0;
    }

    //ramp up/down
    public ToSkipToWait(int numToSkipWait, int maxWaitTime, SkipState skipState, int changeStep) {
        this.numToSkipWait = numToSkipWait;
        this.maxWaitTime = maxWaitTime;
        this.changeStep = changeStep;
        this.skipState = skipState;
        this.countTillSkipWait = 0;
    }

    public int getCountTillSkipWait() {
        return countTillSkipWait;
    }

    public void setCountTillSkipWait(int countTillSkipWait) {
        this.countTillSkipWait = countTillSkipWait;
    }

    public int getNumToSkipWait() {
        return numToSkipWait;
    }

    public void setNumToSkipWait(int numToSkipWait) {
        this.numToSkipWait = numToSkipWait;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public int getMinWaitTime() {
        return minWaitTime;
    }

    public void setMinWaitTime(int minWaitTime) {
        this.minWaitTime = minWaitTime;
    }

    public int getChangeStep() {
        return changeStep;
    }

    public void setChangeStep(int changeStep) {
        this.changeStep = changeStep;
    }

    public SkipState getSkipState() {
        return skipState;
    }

    public void setSkipState(SkipState skipState) {
        this.skipState = skipState;
    }

    public ChangeState getChangeState() {
        return changeState;
    }

    public void setChangeState(ChangeState changeState) {
        this.changeState = changeState;
    }

    public boolean toWait() {
        boolean wait = false;
        if (countTillSkipWait >= numToSkipWait) {
            wait = true;
            countTillSkipWait = 0;
        }
        countTillSkipWait++;
        return wait;
    }

    public int getWaitTimeInSeconds() {
        int waitTime = maxWaitTime;

        //every request modifies the the class attribuite base on state
        switch (skipState) {
            case RAMP_UP:
                maxWaitTime = maxWaitTime + changeStep;
                break;
            case RAMP_DOWN:
                maxWaitTime = maxWaitTime - changeStep;
                if (maxWaitTime < 0) {
                    maxWaitTime = 0;
                }
                break;
            case STEADY:
                break;
            case RANDOM:
                Random rand = new Random();
                waitTime = rand.nextInt((maxWaitTime - minWaitTime) + 1) + minWaitTime;
                break;
        }
        return waitTime;
    }

    public void decBaseOnChangeState() {
        if (changeState == ChangeState.CHG_NUM_TILL_SKIP) {
            numToSkipWait--;
            if (numToSkipWait <= 0) {
                numToSkipWait = 0;
            }
        } else if (changeState == ChangeState.CHG_MAX_TIME) {
            maxWaitTime--;
            if (maxWaitTime <= 0) {
                maxWaitTime = 0;
            }
        } else if (changeState == ChangeState.CHG_MIN_TIME) {
            minWaitTime--;
            if (minWaitTime <= 0) {
                minWaitTime = 0;
            }
        } else if (changeState == ChangeState.CHG_STEP) {
            changeStep--;
            if (changeStep <= 0) {
                changeStep = 0;
            }
        }
    }

    public void incBaseOnChangeState() {
        if (changeState == ChangeState.CHG_NUM_TILL_SKIP) {
            numToSkipWait++;

        } else if (changeState == ChangeState.CHG_MAX_TIME) {
            maxWaitTime++;

        } else if (changeState == ChangeState.CHG_MIN_TIME) {
            minWaitTime++;

        } else if (changeState == ChangeState.CHG_STEP) {
            changeStep++;

        }
    }

    @Override
    public String toString() {
        return "ToSkipToWait{" +
                "countTillSkipWait=" + countTillSkipWait +
                ", numToSkipWait=" + numToSkipWait +
                ", maxWaitTime=" + maxWaitTime +
                ", minWaitTime=" + minWaitTime +
                ", changeStep=" + changeStep +
                ", skipState=" + skipState +
                '}';
    }
}
