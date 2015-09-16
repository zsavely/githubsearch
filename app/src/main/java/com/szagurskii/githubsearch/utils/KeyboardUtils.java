package com.szagurskii.githubsearch.utils;

import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * @author Savelii Zagurskii
 */
public class KeyboardUtils {
    public static void hide(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputMethodManager.displayCompletions(v, null);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void show(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
    }

    public static boolean isActive(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        return inputMethodManager.isAcceptingText();
    }

    public static boolean isShown(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        return inputMethodManager.isActive();
    }

    public static void toggle(Context context) {
        InputMethodManager inputManager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void show(Window window) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void hide(Window window) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}