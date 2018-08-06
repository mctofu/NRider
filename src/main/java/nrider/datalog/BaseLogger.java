package nrider.datalog;

import nrider.io.PerformanceData;

import java.util.*;

public abstract class BaseLogger {

    private LinkedList<PerformanceData> _data = new LinkedList<>();

    public void logData(PerformanceData data) {
        _data.add(data);
    }

    private float getAverage(List<Float> values) {
        float sum = 0;

        for (Float value : values) {
            sum += value;
        }

        return sum / values.size();
    }

    public Log computeLog() {
        List<LogEntry> logEntries = new ArrayList<>();
        long trackStart = _data.getFirst().getTimeStamp();
        HashMap<PerformanceData.Type, List<Float>> trackValues = new HashMap<>();
        HashMap<PerformanceData.Type, Float> lastValue = new HashMap<>();
        for (PerformanceData pd : _data) {
            if (pd.getTimeStamp() - trackStart > 1000 || pd == _data.getLast()) {
                if (!trackValues.isEmpty()) {
                    LogEntry entry = new LogEntry();
                    entry.setTimeStamp(trackStart);
                    HashMap<PerformanceData.Type, Float> values = new HashMap<>();
                    for (Map.Entry<PerformanceData.Type, List<Float>> dataEntry : trackValues.entrySet()) {
                        float value = getAverage(dataEntry.getValue());
                        values.put(dataEntry.getKey(), value);
                        lastValue.put(dataEntry.getKey(), value);
                    }
                    for (Map.Entry<PerformanceData.Type, Float> lastEntry : lastValue.entrySet()) {
                        if (!values.containsKey(lastEntry.getKey())) {
                            values.put(lastEntry.getKey(), lastEntry.getValue());
                        }
                    }
                    entry.setValues(values);
                    logEntries.add(entry);
                }
                trackValues.clear();
                trackStart = pd.getTimeStamp();
            }
            if (!trackValues.containsKey(pd.getType())) {
                trackValues.put(pd.getType(), new ArrayList<Float>());
            }
            trackValues.get(pd.getType()).add(pd.getValue());
        }

        Log log = new Log();
        log.setEntries(logEntries);
        return log;
    }

    public abstract void close();

    class Log {
        private List<LogEntry> _entries;

        public boolean isEmpty() {
            return _entries.isEmpty();
        }

        public long getStartTime() {
            return _entries.get(0).getTimeStamp();
        }

        public long getEndTime() {
            return _entries.get(_entries.size() - 1).getTimeStamp();
        }

        public long getDuration() {
            return getEndTime() - getStartTime();
        }

        public List<LogEntry> getEntries() {
            return _entries;
        }

        public void setEntries(List<LogEntry> entries) {
            _entries = entries;
        }
    }

    class LogEntry {
        private long _timeStamp;
        private HashMap<PerformanceData.Type, Float> _values = new HashMap<>();

        public long getTimeStamp() {
            return _timeStamp;
        }

        public HashMap<PerformanceData.Type, Float> getValues() {
            return _values;
        }

        public void setTimeStamp(long timeStamp) {
            _timeStamp = timeStamp;
        }

        public void setValues(HashMap<PerformanceData.Type, Float> values) {
            _values = values;
        }
    }
}
