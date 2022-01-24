package wooga.gradle

/**
 * For use in the declaration of enumerations meant to be used to match command line options.
 * @param <T> An Enum type
 */
trait OptionSpec {
    abstract String getFlag()

    abstract OptionBuilder getBuilder()

    String compose(Object value) {
        return builder.generateOutput(this.flag, value)
    }
}

/**
 * To be used in a class/trait that maps an enumerated option
 * to its properties
 * @param <T> An Enum type
 */
interface OptionMapper<T extends Enum> {
    abstract String getOption(T option)
}

/**
 * For use in classes that need to fetch all mapped options
 */
trait OptionAggregator {

    static <T extends Enum> List<String> getMappedOptions(Object target, Class enumType) {
        getMappedOptions(target, (T[])enumType.enumConstants)
    }

    static <T extends Enum> List<String> getMappedOptions(Object target, T[] options) {
        List<String> result = new ArrayList<String>()

        def mapper = (OptionMapper<T>) target

        for (o in options) {
            def opt = mapper.getOption(o)
            if (opt != null) {
                result.addAll(opt)
            }
        }

        result
    }
}

class OptionInput {
    final Class type

    OptionInput(Class type) {
        this.type = type
    }
}


abstract class OptionBuilder<T> {

    Class getOptionType() { T.class }

    abstract List<OptionInput> getInputs()

    /**
     * Will attempt to generate valid output from the given value
     */
    String generateOutput(String flag, Object value) {
        T constrainedValue
        // Try casting the value to the specified type
        try {
            constrainedValue = value as T
        }
        // If the type could not be casted directly,
        // call the optional convert function
        catch (ex) {
            constrainedValue = convert(value)
            if (constrainedValue == null) {
                return null
            }
        }
        generateOutputConstrained(flag, constrainedValue)
    }

    protected abstract String generateOutputConstrained(String flag, T value)

    protected T convert(Object value) {
        null
    }
}
