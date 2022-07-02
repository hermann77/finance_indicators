package com.schwarz.finance.indicators.service;

import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Provides indicators for financial data
 */
@Service
public class FinanceService implements FinanceServiceInterface {

    private SortedMap<Integer, Double> dataFrame;
    private Stack<Double> macd = new Stack<>();
    private Stack<Double> signal = new Stack<>();
    private Stack<Double> histogram = new Stack<>();

    @Override
    public FinanceService getInstance(SortedMap<Integer, Double> dataFrame) {
        this.dataFrame = dataFrame;
        return this;
    }


    @Override
    public TreeMap<Integer, Double> getTestData() {
        TreeMap<Integer, Double> data = new TreeMap<>();
        // fill data
        /*
        for (int i = 0; i <= 99; i++) {
            Integer timestamp = Math.toIntExact((System.currentTimeMillis() / 1000) + i);
            data.put(timestamp, Math.random() * 5);
        }
        */
        // 25, 20, 14, 16, 27, 20, 12, 15, 14, 19, 22, 24, 26, 25
        Double[] values = {25.0, 20.0, 14.0, 16.0, 27.0, 20.0, 12.0, 15.0, 14.0, 19.0, 22.0, 24.0, 26.0, 25.0};
        for (int i = 0; i < values.length; i++) {
            data.put(i, values[i]);
        }

        return data;
    }

    /**
     * Provides exponentially weighted moving average (EWMA)
     * To be compatible to naming in Pythons pandas.DataFrame library
     * we call this method ewm()
     *
     * @return Double exponentially weighted moving average
     */
    @Override
    public Double ewm(Double span, boolean adjust, int min_periods) {

        if (dataFrame == null || dataFrame.size() < min_periods) {
            return null;
        }

        System.out.print("dataFrameSize: " + dataFrame.size() + "  ");

        // we take only the last min_periods values
        Integer startTimestampKey = dataFrame.entrySet().stream().skip(dataFrame.size() - min_periods).findFirst().get().getKey();

        System.out.print("startTimestampKey: " + startTimestampKey + "  ");

        SortedMap<Integer, Double> lastPeriodsValues = dataFrame.tailMap(startTimestampKey);

        System.out.print("lastPeriodsValues: " + Arrays.toString(lastPeriodsValues.entrySet().toArray()) + "  ");

        Double[] EWMi = new Double[lastPeriodsValues.size()];
        double alpha = 2.0 / (span + 1);

        EWMi[0] = lastPeriodsValues.get(lastPeriodsValues.firstKey());

        for (int i = 1; i < EWMi.length; i++) {
            Double value = lastPeriodsValues.entrySet().stream().skip(i-1).findFirst().get().getValue(); // i-th value
            EWMi[i] = EWMi[i - 1] + (value - EWMi[i - 1]) * alpha;
        }

        return EWMi[EWMi.length - 1];
    }

    /**
     * Provides moving average convergence divergence (MACD)
     *
     * @return Double MACD
     */
    @Override
    public Double MACD(int fastPeriod, int slowPeriod) {

        Double fastEMA = ewm((double)fastPeriod, true, fastPeriod);
        Double slowEMA = ewm((double)slowPeriod, true, slowPeriod);
        if (fastEMA == null || slowEMA == null) {
            return null;
        }
        macd.push(fastEMA - slowEMA);

        // cleanup the macd stack if we over the 'slowPeriod' (i.e. 26 periods)
        // TODO: create a separate thread to clean up the stack
        if (this.macd.size() > slowPeriod) {
            this.macd.remove(0);
        }

        return macd.peek();
    }

    @Override
    public Double signal(int signalPeriod) {

        if (this.macd.size() < signalPeriod) {
            return null;
        }

        List<Double> macdLastValues = this.macd.subList(this.macd.size() - signalPeriod, this.macd.size());

        Double[] signalArray = new Double[macdLastValues.size()];
        signalArray[0] = macdLastValues.stream().mapToDouble(val -> val).average().orElse(0.0);
        // define the smoothing factor alpha
        double alpha = 2.0 / (signalPeriod + 1);

        for (int i = 1; i < signalArray.length; i++) {
            signalArray[i] = alpha * (macdLastValues.get(i) - signalArray[i - 1]) + signalArray[i - 1];
        }

        this.signal.push(signalArray[signalArray.length - 1]);

        // cleanup the signalArray stack if we over the 'slowPeriod' (i.e. 26 periods)
        // TODO: create a separate thread to clean up the stack
        if (this.signal.size() > 26) {
            this.signal.remove(0);
        }

        return signalArray[signalArray.length - 1];
    }

    @Override
    public Double histogram() {

        if (this.macd.empty() || this.signal.empty()) {
            return null;
        }

        // MACD - Signal
        Double currentHistValue = this.macd.peek() - this.signal.peek();
        this.histogram.push(currentHistValue);
        return currentHistValue;
    }

}

