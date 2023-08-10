package com.ogerardin.xplane.inspection;

@FunctionalInterface
public interface Inspectable {

    InspectionResult inspect();

    default Inspectable and(Inspectable other) {
        return () -> {
            var result = Inspectable.this.inspect();
            return result.isNotAbort() ? result.append(other.inspect()) : result;
        };
    }

}
