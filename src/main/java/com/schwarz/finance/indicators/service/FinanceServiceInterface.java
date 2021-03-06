package com.schwarz.finance.indicators.service;

import java.util.SortedMap;
import java.util.TreeMap;

public interface FinanceServiceInterface {

    /**
     * Provides exponentially weighted moving average (EWMA)
     * To be compatible to naming in Pythons pandas.DataFrame library
     * we call this method ewm()
     *
     * @return Double exponentially weighted moving average
     */
    public abstract FinanceService getInstance(SortedMap<Integer, Double> dataFrame);
    public abstract TreeMap<Integer, Double> getTestData();
    public abstract Double ewm(Double span, boolean adjust, int min_periods);
    public abstract Double MACD(int fastPeriod, int slowPeriod);
    public abstract Double signal(int signalPeriod);
    public abstract Double histogram();
}

