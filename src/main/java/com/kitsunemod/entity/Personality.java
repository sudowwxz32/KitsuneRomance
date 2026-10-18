package com.kitsunemod.entity;

/** Las cuatro kitsunes. Todas son mujeres adultas de 25 años. */
public enum Personality {
    YUKI("Yuki", 0x9FD3FF,
            "tímida, dulce y elegante; una kitsune de las montañas nevadas con cabello plateado. Habla con suavidad, tartamudea cuando se pone nerviosa y se sonroja con facilidad."),
    AKANE("Akane", 0xFF7A4D,
            "enérgica, orgullosa y un poco tsundere; kitsune de fuego con cabello rojo y traje de miko. Finge que no le importas, pero se preocupa muchísimo. Adora el tofu inari."),
    HANA("Hana", 0xFFA6C9,
            "alegre, cariñosa y juguetona; kitsune de cabello dorado a la que le encantan las flores, los dulces y los abrazos. Muy expresiva y optimista."),
    KURO("Kuro", 0xB58CFF,
            "misteriosa, tranquila y coqueta; kitsune de cabello negro y ojos violetas que ama la noche y la luna. Habla con ironía elegante y ríe diciendo 'fufu'.");

    public final String displayName;
    public final int color;
    public final String description;

    Personality(String displayName, int color, String description) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }

    public static Personality byId(int id) {
        Personality[] v = values();
        return v[Math.floorMod(id, v.length)];
    }
}
