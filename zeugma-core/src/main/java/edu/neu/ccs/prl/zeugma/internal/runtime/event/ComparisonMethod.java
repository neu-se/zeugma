package edu.neu.ccs.prl.zeugma.internal.runtime.event;

public enum ComparisonMethod {
    STRING_EQUALS("java/lang/String", "equals", "(Ljava/lang/Object;)Z", false),
    STRING_CONTENT_EQUALS("java/lang/String", "contentEquals", "(Ljava/lang/StringBuffer;)Z", false),
    STRING_CONTENT_EQUALS2("java/lang/String", "contentEquals", "(Ljava/lang/CharSequence;)Z", false),
    STRING_EQUALS_IGNORE_CASE("java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z", false),
    STRING_COMPARE_TO("java/lang/String", "compareTo", "(Ljava/lang/String;)I", false),
    STRING_COMPARE_TO_IGNORE_CASE("java/lang/String", "compareToIgnoreCase", "(Ljava/lang/String;)I", false),
    STRING_REGION_MATCHES("java/lang/String", "regionMatches", "(ILjava/lang/String;II)Z", false),
    STRING_REGION_MATCHES2("java/lang/String", "regionMatches", "(ZILjava/lang/String;II)Z", false),
    STRING_STARTS_WITH("java/lang/String", "startsWith", "(Ljava/lang/String;)Z", false),
    STRING_STARTS_WITH2("java/lang/String", "startsWith", "(Ljava/lang/String;I)Z", false),
    STRING_ENDS_WITH("java/lang/String", "endsWith", "(Ljava/lang/String;)Z", false),
    STRING_INDEX_OF("java/lang/String", "indexOf", "(Ljava/lang/String;)I", false),
    STRING_INDEX_OF2("java/lang/String", "indexOf", "(Ljava/lang/String;I)I", false),
    STRING_LAST_INDEX_OF("java/lang/String", "lastIndexOf", "(Ljava/lang/String;)I", false),
    STRING_LAST_INDEX_OF2("java/lang/String", "lastIndexOf", "(Ljava/lang/String;I)I", false),
    STRING_CONTAINS("java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false),
    STRING_REPLACE("java/lang/String", "replace",
                   "(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;", false),
    STRING_BUILDER_INDEX_OF("java/lang/StringBuilder", "indexOf", "(Ljava/lang/String;)I", false),
    STRING_BUILDER_INDEX_OF2("java/lang/StringBuilder", "indexOf", "(Ljava/lang/String;I)I", false),
    STRING_BUILDER_LAST_INDEX_OF("java/lang/StringBuilder", "lastIndexOf", "(Ljava/lang/String;)I", false),
    STRING_BUILDER_LAST_INDEX_OF2("java/lang/StringBuilder", "lastIndexOf", "(Ljava/lang/String;I)I", false),
    STRING_BUFFER_INDEX_OF("java/lang/StringBuffer", "indexOf", "(Ljava/lang/String;)I", false),
    STRING_BUFFER_INDEX_OF2("java/lang/StringBuffer", "indexOf", "(Ljava/lang/String;I)I", false),
    STRING_BUFFER_LAST_INDEX_OF("java/lang/StringBuffer", "lastIndexOf", "(Ljava/lang/String;)I", false),
    STRING_BUFFER__LAST_INDEX_OF2("java/lang/StringBuffer", "lastIndexOf", "(Ljava/lang/String;I)I", false);

    private final String owner;
    private final String name;
    private final String descriptor;
    private final boolean isStatic;

    ComparisonMethod(String owner, String name, String descriptor, boolean isStatic) {
        if (owner == null || name == null || descriptor == null) {
            throw new NullPointerException();
        }
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
        this.isStatic = isStatic;
    }

    public boolean matches(String owner, String name, String descriptor, boolean isStatic) {
        return this.owner.equals(owner) && this.name.equals(name) && this.descriptor.equals(descriptor)
               && this.isStatic == isStatic;
    }
}
