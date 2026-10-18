package com.kitsunemod.entity;

public enum RelationStatus {
    STRANGER("Desconocida", 0xAAAAAA),
    FRIEND("Amiga", 0x8FD18F),
    DATING("Saliendo", 0xFFB86B),
    GIRLFRIEND("Novia ♥", 0xFF6FA8);

    public final String label;
    public final int color;

    RelationStatus(String label, int color) {
        this.label = label;
        this.color = color;
    }
}
