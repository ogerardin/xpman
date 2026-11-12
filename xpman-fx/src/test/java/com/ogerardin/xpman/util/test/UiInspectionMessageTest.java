package com.ogerardin.xpman.util.test;

import com.ogerardin.xplane.inspection.InspectionMessage;
import com.ogerardin.xpman.diag.UiInspectionMessage;
import com.ogerardin.xpman.util.SpelUtil;
import com.ogerardin.xpman.util.jfx.menu.IntrospectionHelper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UiInspectionMessageTest {

    @Test
    void introspectionExposesExactlyThreeActions() {
        List<String> methods = IntrospectionHelper.computeRelevantMethods(UiInspectionMessage.class).stream()
                .map(java.lang.reflect.Method::getName).toList();

        assertThat(methods, is(List.of("searchWeb", "copyDetails", "applyFix")));
    }

    @Test
    void searchUrlUsesDetailsFirstLineOrMessage() {
        UiInspectionMessage withDetails = new UiInspectionMessage(message("original", "first line\nsecond line"), null);
        UiInspectionMessage withoutDetails = new UiInspectionMessage(message("search this", null), null);

        assertThat(withDetails.getSearchUrl(), is("https://www.google.com/search?q=first+line"));
        assertThat(withoutDetails.getSearchUrl(), is("https://www.google.com/search?q=search+this"));
    }

    @Test
    void detailsFallbackReturnsMessageWhenDetailsAbsent() {
        UiInspectionMessage withDetails = new UiInspectionMessage(message("m", "details"), null);
        UiInspectionMessage withoutDetails = new UiInspectionMessage(message("m", null), null);

        assertThat(withDetails.getDetailsText(), is("details"));
        assertThat(withoutDetails.getDetailsText(), is("m"));
    }

    @Test
    void tableMessagePropertyIsTheWrappedTextNotTheMessageObject() throws Exception {
        UiInspectionMessage row = new UiInspectionMessage(message("plain text", null), null);

        Object value = UiInspectionMessage.class.getMethod("getMessage").invoke(row);

        assertThat(value, is((Object) "plain text"));
    }

    @Test
    void spelCanEvaluateEnabledIfProperty() {
        UiInspectionMessage withDetails = new UiInspectionMessage(message("m", "details"), null);
        UiInspectionMessage withoutDetails = new UiInspectionMessage(message("m", null), null);

        assertThat(SpelUtil.eval("detailsPresent", withDetails), is(true));
        assertThat(SpelUtil.eval("detailsPresent", withoutDetails), is(false));
    }

    @Test
    void hasFixReturnsTrueWhenFixesExist() {
        UiInspectionMessage withFix = new UiInspectionMessage(message("m", null, List.of(new TestFix())), null);
        UiInspectionMessage withoutFix = new UiInspectionMessage(message("m", null), null);

        assertThat(withFix.isHasFix(), is(true));
        assertThat(withoutFix.isHasFix(), is(false));
    }

    private static class TestFix implements com.ogerardin.xplane.inspection.Fix {
        @Override
        public String label() {
            return "Test Fix";
        }

        @Override
        public void apply() {
            // no-op
        }
    }

    private static InspectionMessage message(String message, String details) {
        return InspectionMessage.builder().message(message).details(details).build();
    }

    private static InspectionMessage message(String message, String details, java.util.List<com.ogerardin.xplane.inspection.Fix> fixes) {
        return InspectionMessage.builder().message(message).details(details).fixes(fixes).build();
    }
}
