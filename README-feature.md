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
