# POS y fidelización de clientes (Java + PostgreSQL)

Sistema de punto de venta que atiende rápido (filas con Cola y Min-Heap), corrige errores fácilmente (Pila) y premia a los clientes y sus familias (Union-Find con puntos compartidos).

## Ejecutar
1. Instale Java 17+ y PostgreSQL. Cree una base vacía llamada `pos` (pgAdmin: Databases > Create > Database).
2. Contraseña distinta de `postgres`: defina `POS_DB_PASS`. Otras variables opcionales: `POS_DB_URL` (por defecto `jdbc:postgresql://localhost:5432/pos`), `POS_DB_USER`, `POS_PUERTO` (8080), `POS_LOG_NIVEL` (INFO, o FINE para ver todo).
3. IntelliJ: abra la carpeta con `pom.xml` como proyecto, recargue Maven y ejecute `pos.Main`. Con Maven: `mvn compile exec:java`.
4. Abra http://localhost:8080. Las tablas y los datos de ejemplo se crean solos (`schema.sql`).

## Pruebas, logs y rendimiento
- **Pruebas unitarias (JUnit 5, 63 pruebas):** `mvn test`, o en IntelliJ clic derecho sobre `src/test/java` > Run All Tests. No necesitan base de datos: `Tienda` usa la interfaz `Repositorio` y las pruebas la reemplazan por `RepositorioMemoria`.
- **Logs:** consola, archivo `logs/pos-0.log` (rotativo) y la pestaña "Rendimiento y bitácora" de la web. Registran llegadas, llamados, escaneos, deshacer, ventas, familias, errores (WARNING/SEVERE) y el tiempo de cada petición HTTP.
- **Mediciones de rendimiento:** (a) en la web, pestaña "Rendimiento", el tiempo de cada operación (estructuras en memoria vs. PostgreSQL); (b) `pos.Analisis` (clic derecho > Run) compara cada estructura propia con la alternativa ingenua.

Ejemplo de `pos.Analisis` (1.000 consultas, tiempos en ms; su equipo dará otros valores):

| Clientes | Union-Find | Recorrer tabla O(N) | | Clientes | MinHeap | Lista O(n²) | | Clientes | Cola | ArrayList.remove(0) |
|---|---|---|---|---|---|---|---|---|---|---|
| 10.000 | 0 | 15 | | 10.000 | 1 | 89 | | 50.000 | 3 | 108 |
| 1.000.000 | 0 | 448 | | 50.000 | 9 | 1.219 | | 100.000 | 5 | 403 |

## Arquitectura (por capas)
| Paquete | Responsabilidad |
|---|---|
| `adt/` | Contratos: `ICarrito`, `ICliente`, `IFila`, `IFamilias` (el qué, sin el cómo) |
| `ds/` | Estructuras **propias**: `Bag`, `Pila`, `Cola`, `ColaPrioridad` (Min-Heap), `ListaDoble`, `UnionFind` |
| `modelo/` | Records (`Producto`, `Cliente`, `Turno`, `Recibo`) y `Reglas` de negocio |
| `servicio/` | `Tienda` (lógica del negocio), `Carrito`, `Metricas` |
| `repo/` | `Repositorio` (interfaz) y `RepositorioJdbc` (PostgreSQL, consultas parametrizadas, venta en una transacción) |
| `web/` | `Api` (rutas HTTP) y `Json`; `resources/web/index.html` es el frontend |

## Cumplimiento de las reglas de la entrega
| Requisito | Dónde se cumple |
|---|---|
| Backend completamente en Java | Todo el código de `src/main/java` |
| Frontend libre / motor de BD libre | HTML + CSS + JavaScript; PostgreSQL por JDBC |
| Mínimo 3 estructuras reales | 6 estructuras, cada una con una necesidad concreta (tabla siguiente) |
| Al menos una implementación propia | Las 6 están implementadas a mano en `ds/` |
| Organización del código | Capas `adt` / `ds` / `modelo` / `servicio` / `repo` / `web`; la lógica no depende de PostgreSQL |
| Pruebas unitarias | `src/test/java` (63 pruebas: estructuras, reglas, carrito, métricas, JSON y flujo completo de la tienda) |
| Logs | `ConfigLog` + `java.util.logging` en `Tienda`, `Api`, `RepositorioJdbc` y `Main` |
| Mediciones de rendimiento | `Metricas` (en vivo) y `Analisis` |
| Buenas prácticas | Interfaces, inyección del repositorio, consultas parametrizadas, transacción en el cobro, validaciones, reglas centralizadas, mensajes de error claros |

## Qué estructura resuelve cada necesidad
| Necesidad | Estructura | Costo | Alternativa descartada |
|---|---|---|---|
| Carrito (orden irrelevante, repetidos) | Bag | agregar O(1) | ArrayList: orden innecesario y redimensionado |
| "Borrar último" | Pila | push/pop O(1) | Lista con `remove(i)`: O(n) |
| Fila normal | Cola (FIFO) | ingresar/atender O(1) | `ArrayList.remove(0)`: O(n) |
| Caja preferencial | Min-Heap | ingresar/extraer O(log n) | Lista: buscar el mínimo O(n) por cada cliente |
| Historial del día | Lista doble | agregar al final O(1) | Arreglo: redimensionar y copiar |
| Familias y puntos compartidos | Union-Find | find/unir O(α(n)) | Recorrer la tabla: O(N) |

Prioridad en el Min-Heap: 0 = embarazo, 1 = adulto mayor (65 años o más), 2 = fila normal. Solo las prioridades 0 y 1 entran al heap (caja preferencial); a igual prioridad se atiende por orden de llegada. Las reglas están en `modelo/Reglas.java` y `Cliente.prioridadEnFila()`.

## Análisis de Union-Find
- Con unión por tamaño, la altura de un árbol es ≤ log₂ n (un nodo solo baja un nivel cuando su árbol al menos se duplica), así que `find` es O(log n) en el peor caso.
- Con compresión de camino (path halving), m operaciones sobre n elementos cuestan O((m+n)·α(n)) (Tarjan, 1975). α es la inversa de Ackermann y vale ≤ 4 para cualquier n realista: en la práctica, O(1) amortizado.
- Los puntos del grupo se acumulan solo en la raíz, por lo que consultarlos cuesta lo mismo que un `find`.
- Comparación honesta: una consulta SQL con índice sobre la familia también sería O(log N), pero con un viaje a la base por cada pregunta. Union-Find responde en memoria, en caja; PostgreSQL guarda las uniones (`union_familiar`) y se reconstruyen al arrancar.

## Reglas de negocio
1 punto por cada $1.000 pagados. Si el grupo familiar suma 20 puntos o más, cada compra de cualquiera de sus miembros recibe 10 % de descuento. Los datos de ejemplo permiten probarlo: Ana (12) y Carlos (9) suman 21 al unirlos. Todas las reglas están en `modelo/Reglas.java`.

## Interfaz (simple y en colores pastel)
- **Caja:** tres columnas que siguen el orden de la venta. Paso 1: se escribe el nombre del cliente y el sistema muestra su familia, sus puntos y si tiene descuento (si el nombre no existe, se registra con su edad). Paso 2: filas de pago y «Llamar siguiente». Pasos 3 y 4: se escanean los productos y se muestra el total a pagar con el descuento, si lo tiene. Una franja azul arriba dice en qué paso va y resalta lo que toca hacer.
- **Familias e historial:** unir clientes en familias, comprobar si dos son de la misma familia, agregar clientes nuevos y ver las ventas de hoy.
- **Por dentro:** cada estructura de datos en vivo (para estudiar; no hace falta para vender).
- **Tiempos y registros:** rendimiento de cada operación y bitácora (para estudiar).
- «¿Cómo se usa?» abre una guía corta (también aparece sola la primera vez) y «Ver demostración» atiende a tres clientes explicando cada paso (registra una venta de ejemplo).
- Si se pulsa algo que todavía no corresponde (por ejemplo cobrar sin cliente), la página explica qué falta. Opcionales: teclas 1–9 escanear, Ctrl+Z borrar último, Ctrl+Enter cobrar; arrastrar un cliente sobre otro crea una familia.

## Límites de esta versión
Una sola conexión y sin autenticación (apto para demostración). Para producción: pool de conexiones (HikariCP), usuarios y roles, y JPA/Hibernate si se desea.
