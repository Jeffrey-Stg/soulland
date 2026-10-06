package com.zelf115.soulland.client.screen;

/** The rectangle of the cultivation screen a tab draws in. */
public record TabArea(int left, int top, int width, int height) {

    public int right() {
        return left + width;
    }

    public int bottom() {
        return top + height;
    }

    public int centerX() {
        return left + width / 2;
    }

    public boolean contains(final double x, final double y) {
        return x >= left && x < right() && y >= top && y < bottom();
    }
}
