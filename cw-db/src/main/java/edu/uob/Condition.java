package edu.uob;

// This class represents a condition by which table entities are chosen.
public interface Condition {
    // Conditions are combinations of the form "attributeName op targetValue".
    // To evaluate a given condition,
    // the attribute name needs to be mapped into an actual value.
    // This is what a ValueMapper does.
    // Obviously, a value mapper is associated with a table entity.
    public boolean evaluate(ValueMapper valueMapper) throws DBException;

    @FunctionalInterface
    public static interface ValueMapper {
        public String getValueByKey(String key) throws DBException;
    }

    // Turns a condition cond into `!cond`.
    // Be careful with negation, e.g., should `NULL < 0` == !`NULL >= 0` or not
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
