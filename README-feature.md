# AuthService - Rama Feature

Esta rama está destinada a la implementación de nuevas funcionalidades o experimentos. Cada funcionalidad importante debe desarrollarse en su propia rama `feature/nombre` basada en `developer`.

## Reglas para ramas feature
- Basar siempre la rama en `developer`.
- Hacer commits claros y atómicos.
- Probar localmente antes de solicitar merge.
- Eliminar la rama feature tras el merge.

## Ejemplo de flujo
1. Crea tu rama: `git checkout -b feature/nueva-funcionalidad developer`
2. Desarrolla y prueba tu código.
3. Haz push y solicita Pull Request a `developer`.

---

> Las ramas feature pueden ser eliminadas tras su integración. No usar en producción.

## 6.2.3 Diagrama De Flujo: Ciclo De Ingesta Con Factory Method Y Circuit Breaker

```mermaid
flowchart TD
	A(( )) --> B[Scheduler activa ciclo de sincronizacion]
	B --> C[ConnectorFactory.create(system.getType())]
	C --> D{Circuit Breaker abierto?}
	D -- Si --> E[Retornar snapshot del ultimo lote]
	D -- No --> F[Conector extrae datos del sistema fuente]
	F --> G{Sistema responde?}
	G -- No --> H[Incrementar contador - CB puede abrirse]
	H --> F
	G -- Si --> I[Transformar al esquema unificado]
	I --> J[Validar esquema con Bean Validation]
	J --> K{Datos validos?}
	K -- No --> L[Registrar error en events_db]
	K -- Si --> M[Persistir en events_db Spring Data JPA]
	M --> N[Notificar KPI Engine POST /recalculate]
	N --> O(( ))
```

Figura 3: Flujo de ingesta - Factory Method + Circuit Breaker.
