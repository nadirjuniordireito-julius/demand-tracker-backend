// Gera linhas INSERT para feriados nacionais (mesma regra de DemandaExecucaoService).
// Uso: node scripts/gen-feriados.js
// Ajuste o intervalo em: for (let ano = 2024; ano <= 2030; ano++)
  const a = ano % 19, b = Math.floor(ano / 100), c = ano % 100;
  const d = Math.floor(b / 4), e = b % 4, f = Math.floor((b + 8) / 25);
  const g = Math.floor((b - f + 1) / 3), h = (19 * a + b - d - g + 15) % 30;
  const i = Math.floor(c / 4), k = c % 4, l = (32 + 2 * e + 2 * i - h - k) % 7;
  const m = Math.floor((a + 11 * h + 22 * l) / 451);
  const mes = Math.floor((h + l - 7 * m + 114) / 31);
  const dia = ((h + l - 7 * m + 114) % 31) + 1;
  return new Date(Date.UTC(ano, mes - 1, dia));
}
function addDays(d, n) {
  const x = new Date(d);
  x.setUTCDate(x.getUTCDate() + n);
  return x;
}
function fmt(d) {
  return d.toISOString().slice(0, 10);
}
const fixos = [
  [1, 1, 'Confraternização Universal'],
  [4, 21, 'Tiradentes'],
  [5, 1, 'Dia do Trabalho'],
  [9, 7, 'Independência do Brasil'],
  [10, 12, 'Nossa Senhora Aparecida'],
  [11, 2, 'Finados'],
  [11, 15, 'Proclamação da República'],
  [12, 25, 'Natal'],
];
const rows = new Map();
for (let ano = 2024; ano <= 2030; ano++) {
  for (const [m, d, desc] of fixos) {
    const dt = `${ano}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    rows.set(dt, `Feriado nacional - ${desc}`);
  }
  const p = pascoa(ano);
  [
    [-48, 'Carnaval (segunda-feira)'],
    [-47, 'Carnaval (terça-feira)'],
    [-2, 'Sexta-feira Santa'],
    [60, 'Corpus Christi'],
  ].forEach(([off, desc]) => rows.set(fmt(addDays(p, off)), `Feriado nacional - ${desc}`));
}
[...rows.entries()]
  .sort((a, b) => a[0].localeCompare(b[0]))
  .forEach(([dt, desc]) => console.log(`    ('${dt}', '${desc}'),`));
