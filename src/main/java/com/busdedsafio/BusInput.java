package com.busdedsafio;

/** Teclas del conductor. Las escribe el cliente cada tick; las lee el bus en el cliente del conductor. */
public class BusInput {
	public static volatile float forward;
	public static volatile float side; // +1 = izquierda (A), -1 = derecha (D)
	public static volatile boolean up;
	public static volatile boolean down;

	public static void reset() {
		forward = 0;
		side = 0;
		up = false;
		down = false;
	}
}
