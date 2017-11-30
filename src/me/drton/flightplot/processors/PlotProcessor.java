package me.drton.flightplot.processors;

import me.drton.flightplot.MarkersList;
import me.drton.flightplot.PlotItem;
import me.drton.flightplot.Series;
import me.drton.flightplot.XYPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: ton Date: 12.06.13 Time: 18:25
 */
public abstract class PlotProcessor {
    protected static final String WHITESPACE_RE = "[ \t]+";
    protected Map<String, Object> parameters;
    protected Map<String, String> fieldsList = new HashMap<String, String>(); // 获取fieldsList的键值对
    private double skipOut = 0.0;
    private List<PlotItem> seriesList = new ArrayList<PlotItem>(); // 获取item的标题
    private List<XYPoint> lastPoints = new ArrayList<XYPoint>(); // 获取点的坐标

    protected PlotProcessor() {
        this.parameters = getDefaultParameters();
    }

    public abstract void init();

    public void setSkipOut(double skipOut) {
        this.skipOut = skipOut;
    }

    public void setFieldsList(Map<String, String> fieldsList) {
        this.fieldsList = fieldsList;
    }

    private static Object castValue(Object valueOld, Object valueNewObj) { // 强制格式转换，确保新值数据类型与旧值一致
        String valueNewStr = valueNewObj.toString();
        Object valueNew = valueNewObj;
        if (valueOld instanceof String) { // 如果旧值是String
            valueNew = valueNewStr;
        } else if (valueOld instanceof Double) { // 如果旧值是Double
            valueNew = Double.parseDouble(valueNewStr);
        } else if (valueOld instanceof Float) {
            valueNew = Float.parseFloat(valueNewStr);
        } else if (valueOld instanceof Integer) {
            valueNew = Integer.parseInt(valueNewStr);
        } else if (valueOld instanceof Long) {
            valueNew = Long.parseLong(valueNewStr);
        } else if (valueOld instanceof Boolean) {
            char firstChar = valueNewStr.toLowerCase().charAt(0);
            if (firstChar == 'f' || firstChar == 'n' || "0".equals(valueNewStr))
                valueNew = false;
            else
                valueNew = true;
        }
        return valueNew;
    }

    public abstract Map<String, Object> getDefaultParameters();

    public Map<String, Object> getParameters() {
        return parameters; // 返回键值对
    }

    public void setParameters(Map<String, Object> parametersNew) {
        for (Map.Entry<String, Object> entry : parametersNew.entrySet()) { // entrySet()是键值对key-value的集合
            String key = entry.getKey();
            Object oldValue = parameters.get(key);
            Object newValue = parametersNew.get(key);
            if (oldValue != null) {
                parameters.put(key, castValue(oldValue, newValue));
            }
        }
    }

    protected int addSeries() {
        int idx = seriesList.size();
        seriesList.add(new Series("", skipOut));
        lastPoints.add(null);
        return idx;
    }

    protected int addSeries(String label) {
        int idx = seriesList.size();
        seriesList.add(new Series(label, skipOut));
        lastPoints.add(null);
        return idx;
    }

    protected int addMarkersList() {
        int idx = seriesList.size();
        seriesList.add(new MarkersList(""));
        return idx;
    }

    protected int addMarkersList(String label) {
        int idx = seriesList.size();
        seriesList.add(new MarkersList(label));
        return idx;
    }

    public List<PlotItem> getSeriesList() {
        return seriesList;
    }

    protected void addPoint(int seriesIdx, double time, double value) {
        ((Series) seriesList.get(seriesIdx)).addPoint(time, value);
    }

    protected void addMarker(int seriesIdx, double time, String label) {
        ((MarkersList) seriesList.get(seriesIdx)).addMarker(time, label);
    }

    public abstract void process(double time, Map<String, Object> update);

    public String getProcessorType() {
        return getClass().getSimpleName();
    }
}
