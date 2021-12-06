package fr.benco11.urne.utils;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UrneUtils {
    public static class DateUtils {
        public static final SimpleDateFormat POLL_END_FORMAT = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss");

        public static final SimpleDateFormat POLL_END_GOOD_FORMAT = new SimpleDateFormat("dd/MM/yyyy à hh:mm:ss");
    }

    public static final Random RANDOM = new Random();
    private static final List<Color> VOTES_EMBED_COLOR = Arrays.asList(Color.BLUE, Color.CYAN, Color.MAGENTA, Color.LIGHT_GRAY, Color.ORANGE, Color.PINK, Color.WHITE, Color.DARK_GRAY,
            new Color(124, 95, 140), new Color(26, 88, 75), new Color(143, 161, 51), new Color(218, 182, 104));

    public static final boolean isLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public static final Color getRandomVoteColor() {
        return VOTES_EMBED_COLOR.get(RANDOM.nextInt(VOTES_EMBED_COLOR.size()));
    }

    public static Map<String, String> proposalsToEmojisMap(List<String> proposals) {
        return IntStream.range(0, proposals.size()).mapToObj(a -> new AbstractMap.SimpleEntry<>(new StringBuilder().appendCodePoint(Integer.parseInt(Integer.toHexString(127462 + a), 16)).toString(), proposals.get(a)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }
}
