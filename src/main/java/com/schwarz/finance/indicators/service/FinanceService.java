package com.schwarz.finance.indicators.service;

import org.springframework.stereotype.Service;

import java.util.*;


/**
 * Provides indicators for financial data
 */
@Service
public class FinanceService implements FinanceServiceInterface {

    private SortedMap<Integer, Double> dataFrame;
    private  HashMap<Integer, Double> EWMi = new HashMap<>();
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
        double alpha = 2.0 / (span + 1);

        System.out.print("dataFrameSize: " + dataFrame.size() + "  ");

        // we take only the last min_periods values
        Integer startTimestampKey = dataFrame.entrySet().stream().skip(dataFrame.size() - min_periods).findFirst().get().getKey();
        System.out.println("startTimestampKey: " + startTimestampKey + "  ");

        Integer secondLastTimestampKey = dataFrame.entrySet().stream().skip(dataFrame.size() - 2).findFirst().get().getKey();
        System.out.println("secondLastTimestampKey: " + secondLastTimestampKey + "  ");

        Integer lastTimestampKey = dataFrame.entrySet().stream().skip(dataFrame.size() - 1).findFirst().get().getKey();
        System.out.println("lastTimestampKey: " + lastTimestampKey + "  ");

        Double lastValue = dataFrame.get(lastTimestampKey);
        System.out.println("lastValue: " + lastValue + "  ");

        SortedMap<Integer, Double> lastPeriodsValues = dataFrame.tailMap(startTimestampKey);
        System.out.println("lastPeriodsValues: " + Arrays.toString(lastPeriodsValues.entrySet().toArray()) + "  ");

        // if EWM for last period already exists, use it for calculation
        if (EWMi.get(secondLastTimestampKey) != null) {
            System.out.println("EWMi.get(secondLastTimestampKey): " + EWMi.get(secondLastTimestampKey) + "  ");
            Double EWM_i_th = EWMi.get(secondLastTimestampKey) + (lastValue - EWMi.get(secondLastTimestampKey)) * alpha; // i-th value
            EWMi.put(lastTimestampKey, EWM_i_th);
            System.out.println("EWMi[" + lastTimestampKey + "]: " + EWMi.get(lastTimestampKey) + "  ");
        }
        else { // we should calculate all the EWMs chain (from -span till 0 (now))
            System.out.println("Calculating all the EWMs chain (from -span till 0 (now))");

            EWMi.put(lastPeriodsValues.firstKey(), lastPeriodsValues.get(lastPeriodsValues.firstKey()));

            Integer previousTimestampKey = lastPeriodsValues.firstKey();

            for (Map.Entry<Integer, Double> entry : lastPeriodsValues.tailMap(lastPeriodsValues.firstKey()).entrySet()) {

                Double value = entry.getValue(); // i-th value
                System.out.print("value: " + value + "  ");
                Double EWM_i_th = EWMi.get(previousTimestampKey) + (value - EWMi.get(previousTimestampKey)) * alpha; // i-th value
                EWMi.put(entry.getKey(), EWM_i_th);
                System.out.println("EWMi[" + entry.getKey() + "]: " + EWMi.get(entry.getKey()) + "  ");

                previousTimestampKey = entry.getKey();
            }
        }

        return EWMi.get(lastTimestampKey);
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

