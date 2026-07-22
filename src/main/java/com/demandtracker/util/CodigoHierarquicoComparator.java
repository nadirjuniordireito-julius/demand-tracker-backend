package com.demandtracker.util;

import java.util.Comparator;

/**
 * Compara códigos hierárquicos (ex.: 1.1, 1.2, 1.10, 2.1) segmento a segmento.
 * Segmentos numéricos são comparados como inteiros; demais segmentos, como texto (case-insensitive).
 */
public final class CodigoHierarquicoComparator implements Comparator<String> {

    public static final CodigoHierarquicoComparator INSTANCE = new CodigoHierarquicoComparator();

    private CodigoHierarquicoComparator() {
    }

    @Override
    public int compare(String a, String b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }

        String[] partesA = a.trim().split("\\.");
        String[] partesB = b.trim().split("\\.");
        int len = Math.min(partesA.length, partesB.length);
        for (int i = 0; i < len; i++) {
            int cmp = compareSegmento(partesA[i].trim(), partesB[i].trim());
            if (cmp != 0) {
                return cmp;
            }
        }
        return Integer.compare(partesA.length, partesB.length);
    }

    private static int compareSegmento(String sa, String sb) {
        if (sa.isEmpty() && sb.isEmpty()) {
            return 0;
        }
        if (sa.isEmpty()) {
            return -1;
        }
        if (sb.isEmpty()) {
            return 1;
        }
        try {
            long ia = Long.parseLong(sa);
            long ib = Long.parseLong(sb);
            return Long.compare(ia, ib);
        } catch (NumberFormatException e) {
            return sa.compareToIgnoreCase(sb);
        }
    }
}
