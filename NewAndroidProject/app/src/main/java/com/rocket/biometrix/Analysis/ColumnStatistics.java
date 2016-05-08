package com.rocket.biometrix.Analysis;

/**
 * Created by TJ on 5/2/2016.
 * A holder class to hold the statistical data of a single column.
 */
public class ColumnStatistics {

    private int totalCount;
    private float median;
    private float mean;
    private float min;
    private float max;
    private String columnName;
    private String tableName;


    public ColumnStatistics(int totalCount, float median, float mean, float min, String tableName, String columnName, float max) {
        this.totalCount = totalCount;
        this.median = median;
        this.mean = mean;
        this.min = min;
        this.tableName = tableName;
        this.columnName = columnName;
        this.max = max;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public float getMedian() {
        return median;
    }

    public float getMean() {
        return mean;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getTableName() {
        return tableName;
    }
}
