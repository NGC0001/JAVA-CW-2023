package edu.uob;

// This class represents a condition by which table entities are chosen.
public interface Condition {
    public boolean evaluate(ValueMapper valueMapper) throws DBException;

    @FunctionalInterface
    public static interface ValueMapper {
        public String getValueByKey(String key) throws DBException;
    }

    public static Condition negate(Condition cond) {
        return new Condition() {
            public boolean evaluate(ValueMapper valueMapper) throws DBException {
                if (cond == null) {
                    throw new DBException.NullObjectException("negating null condition");
                }
                return !cond.evaluate(valueMapper);
            }
        };
    }
}
