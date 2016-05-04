package com.rocket.biometrix.Analysis;

/**
 * Created by TJ on 5/2/2016. A container type class. It is used to hold the column statistics for
 * each column, and other needed correlation information
 */
public class ColumnCorrelation
{
    int daysApart;
    double correlationValue;
    int correlatedEntries;


    public ColumnCorrelation(int daysApart, double correlationValue, int correlatedEntries) {
        this.daysApart = daysApart;
        this.correlationValue = correlationValue;
        this.correlatedEntries = correlatedEntries;
    }

    public double getCorrelationValue() {
        return correlationValue;
    }

    public int getDaysApart() {
        return daysApart;
    }

    public int getCorrelatedEntries() {
        return correlatedEntries;
    }
}
