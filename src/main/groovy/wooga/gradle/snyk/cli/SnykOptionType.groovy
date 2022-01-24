package wooga.gradle.snyk.cli

import org.gradle.api.file.Directory
import wooga.gradle.OptionBuilder
import wooga.gradle.OptionInput

class SnykStringOption extends OptionBuilder<String> {

    @Override
    String generateOutputConstrained(String flag, String value) {
        "${flag}=${value}"
    }

    @Override
    List<OptionInput> getInputs() {
        [new OptionInput(String.class)]
    }
}

class SnykBooleanOption extends OptionBuilder<Boolean> {

    @Override
    String generateOutputConstrained(String flag, Boolean value) {
        if (!value){
            return null
        }
        "${flag}"
    }

    @Override
    List<OptionInput> getInputs() {
        [new OptionInput(Boolean.class)]
    }

    @Override
    String generateOutput(String flag, Object value) {
        generateOutputConstrained(flag, (Boolean) value)
    }
}

class SnykIntegerOption extends OptionBuilder<Integer> {

    @Override
    String generateOutputConstrained(String flag, Integer value) {
        "${flag}=${value}"
    }

    @Override
    List<OptionInput> getInputs() {
        [new OptionInput(Integer.class)]
    }
}

class SnykEnumOption<T extends Enum<T>> extends OptionBuilder<T> {

    final List<T> values
    final List<String> valueStrings
    final Class enumType
    final String enumTypeName

    @Override
    Class getOptionType() {
        enumType
    }

    SnykEnumOption(Class<T> enumType) {
        this.enumType = enumType
        this.enumTypeName = enumType.name
        this.values = enumType.getEnumConstants() as T[]
        this.valueStrings = this.values.collect({ it -> it.toString() })
    }

    @Override
    String generateOutputConstrained(String flag, T value) {
        "${flag}=${value}"
        //"${flag}=${enumTypeName}.${value}"
    }

    @Override
    List<OptionInput> getInputs() {
        [new OptionInput(optionType)]
    }
}

class SnykEnumListOption<T extends Enum<T>> extends OptionBuilder<Iterable<T>> {

    final List<T> values
    final List<String> valueStrings
    final Class enumType
    final String enumTypeName

    @Override
    Class getOptionType() {
        enumType
    }

    SnykEnumListOption(Class<T> enumType) {
        this.enumType = enumType
        this.enumTypeName = enumType.name
        this.values = enumType.getEnumConstants() as T[]
        this.valueStrings = this.values.collect({ it -> it.toString() })
    }

    @Override
    String generateOutputConstrained(String flag, Iterable<T> values) {
        "${flag}=${values.collect({ it.toString()}).join(",")}"
    }

    @Override
    List<OptionInput> getInputs() {
        [new OptionInput(optionType)]
    }
}

class SnykMapOption extends OptionBuilder<Map> {

    @Override
    List<OptionInput> getInputs() {
        [new OptionInput(Map.class)]
    }

    @Override
    protected String generateOutputConstrained(String flag, Map value) {
        "${flag}=${value.collect( {"${it.key}=${it.value}"}).join(",")}"
    }
}

class SnykFileOption extends OptionBuilder<File> {

    @Override
    String generateOutputConstrained(String flag, File value) {
        "${flag}=${value.path}"
    }

    @Override
    List<OptionInput> getInputs() {
        [new OptionInput(File.class)]
    }
}

class SnykListOption extends OptionBuilder<Iterable> {

    Closure<String> toString
    Class elementType

    SnykListOption(Class elementType, Closure<String> toString = null){
        this.elementType = elementType
        this.toString = toString
    }

    @Override
    List<OptionInput> getInputs() {
        [new OptionInput(elementType)]
    }

    @Override
    protected String generateOutputConstrained(String flag, Iterable value) {
        "${flag}=${value.collect({ stringify(it)}).join(",")}"
    }

    private String stringify(Object value) {
        if (toString != null){
            return toString(value)
        }
        value.toString()
    }
}
