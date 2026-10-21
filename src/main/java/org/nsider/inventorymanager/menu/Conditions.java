package org.nsider.inventorymanager.menu;

import org.nsider.inventorymanager.extras.Nonnull;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Requirements, written after placeholders are filled. Each line is one of:
 * <pre>
 * permission some.node        the viewer has the permission
 * 5 &gt;= 3                      ==, !=, &gt;=, &lt;=, &gt;, &lt; (numbers compare as numbers, text ignoring case)
 * true                        true / yes / on / a number other than 0
 * </pre>
 * A leading {@code !} negates the line. Every line of a list must pass.
 */
public final class Conditions {

    private static final Pattern COMPARISON = Pattern.compile("^(.*?)\\s*(==|!=|>=|<=|>|<)\\s*(.*)$");

    private Conditions() {
    }

    public static boolean all(@Nonnull List<String> conditions, @Nonnull Predicate<String> hasPermission) {
        for (String condition : conditions) {
            if (!test(condition, hasPermission)) return false;
        }
        return true;
    }

    public static boolean test(@Nonnull String condition, @Nonnull Predicate<String> hasPermission) {
        String text = condition.trim();
        boolean negate = false;
        while (text.startsWith("!") && !text.startsWith("!=")) {
            negate = !negate;
            text = text.substring(1).trim();
        }
        return negate != evaluate(text, hasPermission);
    }

    private static boolean evaluate(String text, Predicate<String> hasPermission) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.startsWith("permission:") || lower.startsWith("permission ")) {
            return hasPermission.test(text.substring("permission".length() + 1).trim());
        }
        Matcher matcher = COMPARISON.matcher(text);
        if (matcher.matches()) return compare(matcher.group(1).trim(), matcher.group(2), matcher.group(3).trim());
        return truthy(text);
    }

    private static boolean compare(String left, String operator, String right) {
        Double a = number(left);
        Double b = number(right);
        if (a != null && b != null) {
            int result = Double.compare(a, b);
            switch (operator) {
                case "==": return result == 0;
                case "!=": return result != 0;
                case ">=": return result >= 0;
                case "<=": return result <= 0;
                case ">": return result > 0;
                default: return result < 0;
            }
        }
        if (operator.equals("==")) return left.equalsIgnoreCase(right);
        if (operator.equals("!=")) return !left.equalsIgnoreCase(right);
        return false;
    }

    static boolean truthy(@Nonnull String text) {
        String value = text.trim().toLowerCase(Locale.ROOT);
        if (value.equals("true") || value.equals("yes") || value.equals("on")) return true;
        Double number = number(value);
        return number != null && number != 0;
    }

    private static Double number(String text) {
        try {
            return Double.parseDouble(text.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
