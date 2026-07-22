package com.demandtracker.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CodigoHierarquicoComparatorTest {

  private final CodigoHierarquicoComparator comparator = CodigoHierarquicoComparator.INSTANCE;

  @Test
  void ordenaSegmentosNumericos() {
    List<String> codigos = new ArrayList<>(List.of("2.1", "1.10", "1.2", "1.1"));
    codigos.sort(comparator);
    assertThat(codigos).containsExactly("1.1", "1.2", "1.10", "2.1");
  }

  @Test
  void nullsPorUltimo() {
    List<String> codigos = new ArrayList<>();
    codigos.add("2.1");
    codigos.add(null);
    codigos.add("1.1");
    codigos.sort(Comparator.nullsLast(comparator));
    assertThat(codigos).containsExactly("1.1", "2.1", null);
  }

  @Test
  void segmentosNaoNumericos_usaTexto() {
    assertThat(comparator.compare("A.2", "A.10")).isNegative();
    assertThat(comparator.compare("A.2", "B.1")).isNegative();
  }

  @Test
  void codigoComMaisSegmentosDepois() {
    assertThat(comparator.compare("1.1", "1.1.1")).isNegative();
  }
}
