package com.zelf115.soulland.spirit;

/** The body plan a spirit beast is built on: it sizes the hitbox here and picks the model on the client. */
public enum SpiritBeastShape {
    BIG_CAT(1.2F, 1.3F),
    LION(1.2F, 1.4F),
    BIRD(0.8F, 1.6F),
    SERPENT(1.2F, 0.6F),
    CANINE(0.9F, 1.3F),
    BEAR(1.4F, 1.4F),
    HEAVY(1.4F, 1.3F),
    PORCUPINE(0.9F, 0.8F),
    APE(1.3F, 2.1F),
    HORNED_APE(1.3F, 2.3F),
    DRAGON(1.6F, 1.8F),
    SPIDER(1.4F, 0.9F),
    SCORPION(1.4F, 1.0F),
    SHARK(1.2F, 1.1F),
    ORCA(1.2F, 1.1F),
    OCTOPUS(1.2F, 1.6F),
    FLOATING_EYE(1.0F, 1.9F);

    private final float width;
    private final float height;

    SpiritBeastShape(final float width, final float height) {
        this.width = width;
        this.height = height;
    }

    public float width() {
        return width;
    }

    public float height() {
        return height;
    }
}
