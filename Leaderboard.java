import java.io.*;
import java.util.*;

public class Leaderboard {
    public static class Entry {
        public final String name;
        public final int score;
        public Entry(String name, int score) { this.name = name; this.score = score; }
    }

    private final File store;

    public Leaderboard(String path) {
        this.store = new File(path);
    }

    public synchronized List<Entry> load() {
        List<Entry> list = new ArrayList<>();
        if (!store.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(store))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    try {
                        int s = Integer.parseInt(parts[1]);
                        list.add(new Entry(parts[0], s));
                    } catch (NumberFormatException ignore) {}
                }
            }
        } catch (IOException ignore) {}
        list.sort((a,b) -> Integer.compare(b.score, a.score));
        return list;
    }

    public synchronized void add(String name, int score) {
        try {
            if (!store.exists()) {
                File parent = store.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();
                store.createNewFile();
            }

            List<Entry> existing = load();
            Map<String, Integer> byName = new HashMap<>();
            for (Entry e : existing) {
                Integer prev = byName.get(e.name);
                if (prev == null || e.score > prev) {
                    byName.put(e.name, e.score);
                }
            }

            String cleanName = name.replace(",", " ").trim();
            Integer prevScore = byName.get(cleanName);
            if (prevScore == null || score > prevScore) {
                byName.put(cleanName, score);
            }

            List<Entry> toWrite = new ArrayList<>();
            for (Map.Entry<String, Integer> me : byName.entrySet()) {
                toWrite.add(new Entry(me.getKey(), me.getValue()));
            }
            toWrite.sort((a, b) -> Integer.compare(b.score, a.score));

            try (FileWriter fw = new FileWriter(store, false)) {
                for (Entry e : toWrite) {
                    fw.write(e.name + "," + e.score + System.lineSeparator());
                }
            }
        } catch (IOException ignore) {}
    }

    public List<Entry> top(int n) {
        List<Entry> list = load();
        return list.size() > n ? list.subList(0, n) : list;
    }
}
