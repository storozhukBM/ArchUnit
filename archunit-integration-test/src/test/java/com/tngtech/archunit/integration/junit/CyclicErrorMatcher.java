package com.tngtech.archunit.integration.junit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.tngtech.archunit.junit.ExpectedViolation.ExpectedAccess;
import com.tngtech.archunit.junit.MessageAssertionChain;

import static com.google.common.base.Functions.toStringFunction;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.getLast;

class CyclicErrorMatcher implements MessageAssertionChain.Link {
    private final List<String> cycleDescriptions = new ArrayList<>();
    private final Multimap<String, ExpectedAccess> details = LinkedHashMultimap.create();

    static CyclicErrorMatcher cycle() {
        return new CyclicErrorMatcher();
    }

    private String cycleText() {
        return "Cycle detected: " +
                Joiner.on(" -> ").join(FluentIterable.from(cycleDescriptions).append(cycleDescriptions.get(0)));
    }

    private String detailText() {
        return System.lineSeparator() + Joiner.on(System.lineSeparator()).join(detailLines());
    }

    private List<String> detailLines() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Collection<ExpectedAccess>> detail : details.asMap().entrySet()) {
            result.add("Dependencies of " + detail.getKey());
            result.addAll(transform(detail.getValue(), toStringFunction()));
        }
        return result;
    }

    public CyclicErrorMatcher from(String sliceName) {
        cycleDescriptions.add(sliceName);
        return this;
    }

    public CyclicErrorMatcher byAccess(ExpectedAccess access) {
        details.put(getLast(cycleDescriptions), access);
        return this;
    }

    @Override
    public MessageAssertionChain.Link.Result filterMatching(List<String> lines) {
        return new Result.Builder()
                .containsLine(cycleText())
                .containsConsecutiveLines(detailLines())
                .build(lines);
    }

    @Override
    public String getDescription() {
        return String.format("Message contains cycle description '%s' and details '%s'",
                cycleText(), detailText());
    }
}
