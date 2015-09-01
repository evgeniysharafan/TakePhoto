package com.evgeniysharafan.takephoto.util.lib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class RevealAnimation {

    private static final String ARG_START_REVEAL_X = "arg_start_reveal_x";
    private static final String ARG_START_REVEAL_Y = "arg_start_reveal_y";
    private static final String ARG_START_REVEAL_RADIUS = "arg_start_reveal_radius";
    private static final String ARG_REVEAL_DURATION = "arg_reveal_duration";
    private static final String ARG_REVEAL_SCREEN_WIDTH = "arg_reveal_screen_width";
    private static final String ARG_REVEAL_SCREEN_HEIGHT = "arg_reveal_screen_height";

    private static AnimatorSet revealSet;
    private static AnimatorSet concealSet;

    private RevealAnimation() {
    }

    /**
     * Get a bundle with all needed params for reveal animation from fab
     *
     * @param fab      floating action button or similar view
     * @param duration reveal animation duration
     */
    public static Bundle getBundle(View fab, long duration) {
        if (!Utils.hasLollipop()) {
            return null;
        }

        Rect rect = new Rect();
        fab.getGlobalVisibleRect(rect);

        return getBundle(rect.left + rect.width() / 2, rect.top + rect.height() / 2, rect.width() / 2,
                duration);
    }

    /**
     * Get a bundle with all needed params for reveal animation from params
     */
    public static Bundle getBundle(int startRevealX, int startRevealY, int startRevealRadius,
                                   long duration) {
        if (!Utils.hasLollipop()) {
            return null;
        }

        Bundle b = new Bundle();
        b.putInt(ARG_START_REVEAL_X, startRevealX);
        b.putInt(ARG_START_REVEAL_Y, startRevealY);
        b.putInt(ARG_START_REVEAL_RADIUS, startRevealRadius);
        b.putLong(ARG_REVEAL_DURATION, duration);

        int screenWidth = Res.getDisplayMetrics().widthPixels;
        b.putInt(ARG_REVEAL_SCREEN_WIDTH, screenWidth);

        int screenHeight = Res.getDisplayMetrics().heightPixels;
        b.putInt(ARG_REVEAL_SCREEN_HEIGHT, screenHeight);

        return b;
    }

    /**
     * Play reveal animation with a bundle from getBundle()
     *
     * @param view      root content view
     * @param reveal    reveal view with the same color as fab, can be gone
     * @param arguments bundle from getBundle()
     */
    public static void playRevealAnimation(ActionBar actionBar, View view, View reveal, Bundle arguments) {
        if (!Utils.hasLollipop()) {
            return;
        }

        playRevealAnimation(actionBar, view, reveal, getStartRevealX(arguments),
                getStartRevealY(arguments), getStartRevealRadius(arguments), getDuration(arguments),
                getRevealScreenWidth(arguments), getRevealScreenHeight(arguments));
    }

    /**
     * Play reveal animation with
     *
     * @param view     root content view
     * @param reveal   reveal view with the same color as fab, can be gone
     * @param duration reveal animation duration
     */
    public static void playRevealAnimation(final ActionBar actionBar, final View view, final View reveal,
                                           final int startRevealX, final int startRevealY,
                                           final int startRevealRadius, final long duration,
                                           final int screenWidth, final int screenHeight) {
        if (!Utils.hasLollipop()) {
            return;
        }

        Utils.doOnPreDraw(view, true, new Runnable() {
            @Override
            public void run() {
                revealSet = getAnimation(true, screenHeight, actionBar, startRevealY, screenWidth,
                        startRevealX, view, startRevealRadius, reveal, duration, new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                revealSet = null;
                                reveal.setVisibility(View.GONE);
                            }
                        });
                revealSet.start();
            }
        });
    }

    /**
     * Play conceal animation with a bundle from getBundle()
     *
     * @param view           root content view
     * @param conceal        conceal view with the same color as fab which was reveal
     *                       for reveal animation, can be gone
     * @param arguments      bundle from getBundle()
     * @param onAnimationEnd runnable which will be executed on onAnimationEnd()
     */
    public static void playConcealAnimation(ActionBar actionBar, View view, View conceal,
                                            Bundle arguments, Runnable onAnimationEnd) {
        if (!Utils.hasLollipop()) {
            return;
        }

        playConcealAnimation(actionBar, view, conceal, getStartRevealX(arguments),
                getStartRevealY(arguments), getStartRevealRadius(arguments), getDuration(arguments),
                getRevealScreenWidth(arguments), getRevealScreenHeight(arguments), onAnimationEnd);
    }

    /**
     * Play conceal animation with a bundle from getBundle()
     *
     * @param view           root content view
     * @param conceal        conceal view with the same color as fab which was reveal
     *                       for reveal animation, can be gone
     * @param duration       conceal animation duration
     * @param onAnimationEnd runnable which will be executed on onAnimationEnd()
     */
    public static void playConcealAnimation(ActionBar actionBar, View view, final View conceal,
                                            int startRevealX, int startRevealY,
                                            int endRevealRadius, long duration,
                                            int revealScreenWidth, int revealScreenHeight,
                                            final Runnable onAnimationEnd) {
        if (!Utils.hasLollipop()) {
            return;
        }

        if (concealSet != null && concealSet.isRunning()) {
            concealSet.end();
            return;
        }

        if (revealSet != null && revealSet.isRunning()) {
            revealSet.end();
        }

        int screenWidth = Res.getDisplayMetrics().widthPixels;
        int screenHeight = Res.getDisplayMetrics().heightPixels;

        if (screenWidth != revealScreenWidth) {
            if (!Utils.isRtl()) {
                startRevealX = screenWidth - (revealScreenWidth - startRevealX);
            } else {
                startRevealX = screenWidth - (screenWidth - startRevealX);
            }

            startRevealY = screenHeight - (revealScreenHeight - startRevealY);
        }

        concealSet = getAnimation(false, screenHeight, actionBar, startRevealY, screenWidth,
                startRevealX, view, endRevealRadius, conceal, duration, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        concealSet = null;
                        conceal.setVisibility(View.GONE);
                        onAnimationEnd.run();
                    }
                });
        concealSet.start();
    }

    /**
     * Should be called on onPause()
     */
    public static void endAnimations() {
        if (!Utils.hasLollipop()) {
            return;
        }

        if (revealSet != null && revealSet.isRunning())
            revealSet.end();

        if (concealSet != null && concealSet.isRunning())
            concealSet.end();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static AnimatorSet getAnimation(boolean isReveal, int screenHeight, ActionBar actionBar,
                                            int startRevealY, int screenWidth, int startRevealX, View view,
                                            int startRevealRadius, final View reveal, long duration,
                                            Animator.AnimatorListener listener) {
        int layoutHeight = screenHeight - getActionBarAndStatusBarHeight(actionBar);
        int startLayoutRevealY = startRevealY - getActionBarAndStatusBarHeight(actionBar);

        int endRadius;
        if (!Utils.isRtl()) {
            endRadius = (int) (Math.hypot(screenWidth, layoutHeight)
                    - Math.hypot(screenWidth - startRevealX, layoutHeight - startLayoutRevealY));
        } else {
            endRadius = (int) (Math.hypot(screenWidth, layoutHeight)
                    - Math.hypot(screenWidth - (screenWidth - startRevealX),
                    layoutHeight - startLayoutRevealY));
        }

        Animator revealAnim = ViewAnimationUtils.createCircularReveal(view,
                startRevealX, startLayoutRevealY, isReveal ? startRevealRadius : endRadius,
                isReveal ? endRadius : startRevealRadius);

        reveal.setVisibility(View.VISIBLE);
        Animator alphaAnim = ObjectAnimator.ofFloat(reveal, View.ALPHA, isReveal ? 1 : 0, isReveal ? 0 : 1);

        AnimatorSet set = new AnimatorSet();
        set.setDuration(isReveal ? duration : (long) (duration * 0.7f));
        set.setInterpolator(isReveal ? new DecelerateInterpolator() : new AccelerateInterpolator());
        set.addListener(listener);
        set.playTogether(revealAnim, alphaAnim);

        return set;
    }

    private static int getActionBarAndStatusBarHeight(ActionBar actionBar) {
        return Res.getStatusBarHeight() + actionBar.getHeight();
    }

    private static int getStartRevealX(Bundle arguments) {
        return arguments.getInt(ARG_START_REVEAL_X);
    }

    private static int getStartRevealY(Bundle arguments) {
        return arguments.getInt(ARG_START_REVEAL_Y);
    }

    private static int getStartRevealRadius(Bundle arguments) {
        return arguments.getInt(ARG_START_REVEAL_RADIUS);
    }

    private static long getDuration(Bundle arguments) {
        return arguments.getLong(ARG_REVEAL_DURATION);
    }

    private static int getRevealScreenWidth(Bundle arguments) {
        return arguments.getInt(ARG_REVEAL_SCREEN_WIDTH);
    }

    private static int getRevealScreenHeight(Bundle arguments) {
        return arguments.getInt(ARG_REVEAL_SCREEN_HEIGHT);
    }

}
