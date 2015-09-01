package com.evgeniysharafan.takephoto.util.lib;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

public final class Fragments {

    public static final int TRANSIT_DEFAULT = -42;
    public static final int ANIM_DEFAULT = 0;

    private Fragments() {
    }

    /**
     * @param tag can be null or empty string, in this case will be used
     *            fragment.getClass().getSimpleName() as tag.
     */
    public static void replace(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        replace(fragmentManager, containerId, fragment, tag, false);
    }

    /**
     * @param tag can be null or empty string, in this case will be used
     *            fragment.getClass().getSimpleName() as tag.
     */
    public static void replace(FragmentManager fragmentManager, int containerId, Fragment fragment,
                               String tag, boolean addToBackStack) {
        replace(fragmentManager, containerId, fragment, tag, TRANSIT_DEFAULT, addToBackStack);
    }

    /**
     * @param tag          can be null or empty string, in this case will be used
     *                     fragment.getClass().getSimpleName() as tag.
     * @param transitionId TRANSIT_DEFAULT or transitionId
     */
    public static void replace(FragmentManager fragmentManager, int containerId, Fragment fragment,
                               String tag, int transitionId, boolean addToBackStack) {
        replace(fragmentManager, containerId, fragment, tag, transitionId, ANIM_DEFAULT, ANIM_DEFAULT,
                ANIM_DEFAULT, ANIM_DEFAULT, addToBackStack);
    }

    /**
     * @param tag                  can be null or empty string, in this case will be used
     *                             fragment.getClass().getSimpleName() as tag.
     * @param customAnimationEnter ANIM_DEFAULT or animation
     * @param customAnimationExit  ANIM_DEFAULT or animation
     */
    public static void replace(FragmentManager fragmentManager, int containerId, Fragment fragment,
                               String tag, int customAnimationEnter, int customAnimationExit, boolean addToBackStack) {
        replace(fragmentManager, containerId, fragment, tag, customAnimationEnter, customAnimationExit,
                ANIM_DEFAULT, ANIM_DEFAULT, addToBackStack);
    }

    /**
     * @param tag                     can be null or empty string, in this case will be used
     *                                fragment.getClass().getSimpleName() as tag.
     * @param customAnimationEnter    ANIM_DEFAULT or animation
     * @param customAnimationExit     ANIM_DEFAULT or animation
     * @param customAnimationPopEnter ANIM_DEFAULT or animation
     * @param customAnimationPopExit  ANIM_DEFAULT or animation
     */
    public static void replace(FragmentManager fragmentManager, int containerId, Fragment fragment,
                               String tag, int customAnimationEnter, int customAnimationExit, int customAnimationPopEnter,
                               int customAnimationPopExit, boolean addToBackStack) {
        replace(fragmentManager, containerId, fragment, tag, TRANSIT_DEFAULT, customAnimationEnter,
                customAnimationExit, customAnimationPopEnter, customAnimationPopExit, addToBackStack);
    }

    /**
     * @param tag                     can be null or empty string, in this case will be used
     *                                fragment.getClass().getSimpleName() as tag.
     * @param transitionId            TRANSIT_DEFAULT or transitionId
     * @param customAnimationEnter    ANIM_DEFAULT or animation
     * @param customAnimationExit     ANIM_DEFAULT or animation
     * @param customAnimationPopEnter ANIM_DEFAULT or animation
     * @param customAnimationPopExit  ANIM_DEFAULT or animation
     */
    public static void replace(FragmentManager fragmentManager, int containerId, Fragment fragment,
                               String tag, int transitionId, int customAnimationEnter, int customAnimationExit,
                               int customAnimationPopEnter, int customAnimationPopExit, boolean addToBackStack) {
        replace(fragmentManager, containerId, fragment, tag, transitionId, customAnimationEnter,
                customAnimationExit, customAnimationPopEnter, customAnimationPopExit, addToBackStack, null);
    }

    /**
     * @param tag                     can be null or empty string, in this case will be used
     *                                fragment.getClass().getSimpleName() as tag.
     * @param transitionId            TRANSIT_DEFAULT or transitionId
     * @param customAnimationEnter    ANIM_DEFAULT or animation
     * @param customAnimationExit     ANIM_DEFAULT or animation
     * @param customAnimationPopEnter ANIM_DEFAULT or animation
     * @param customAnimationPopExit  ANIM_DEFAULT or animation
     * @param stackName               Name of the back stack.
     */
    public static void replace(FragmentManager fragmentManager, int containerId, Fragment fragment,
                               String tag, int transitionId, int customAnimationEnter, int customAnimationExit,
                               int customAnimationPopEnter, int customAnimationPopExit, boolean addToBackStack,
                               String stackName) {
        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (transitionId != TRANSIT_DEFAULT) {
            transaction.setTransition(transitionId);
        }

        if (customAnimationPopEnter != ANIM_DEFAULT || customAnimationPopExit != ANIM_DEFAULT) {
            transaction.setCustomAnimations(customAnimationEnter, customAnimationExit,
                    customAnimationPopEnter, customAnimationPopExit);
        } else if (customAnimationEnter != ANIM_DEFAULT || customAnimationExit != ANIM_DEFAULT) {
            transaction.setCustomAnimations(customAnimationEnter, customAnimationExit);
        }

        transaction.replace(containerId, fragment, TextUtils.isEmpty(tag) ? fragment.getClass().getSimpleName() : tag);

        if (addToBackStack) {
            transaction.addToBackStack(stackName);
        }

        transaction.commit();
    }

    /**
     * @param tag can be null or empty string, in this case will be used
     *            fragment.getClass().getSimpleName() as tag.
     */
    public static void add(FragmentManager fragmentManager, Fragment fragment, String tag) {
        add(fragmentManager, 0, fragment, tag, false);
    }

    /**
     * @param tag can be null or empty string, in this case will be used
     *            fragment.getClass().getSimpleName() as tag.
     */
    public static void add(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        add(fragmentManager, containerId, fragment, tag, false);
    }

    /**
     * @param tag can be null or empty string, in this case will be used
     *            fragment.getClass().getSimpleName() as tag.
     */
    public static void add(FragmentManager fragmentManager, int containerId, Fragment fragment,
                           String tag, boolean addToBackStack) {
        add(fragmentManager, containerId, fragment, tag, TRANSIT_DEFAULT, addToBackStack);
    }

    /**
     * @param tag          can be null or empty string, in this case will be used
     *                     fragment.getClass().getSimpleName() as tag.
     * @param transitionId TRANSIT_DEFAULT or transitionId
     */
    public static void add(FragmentManager fragmentManager, int containerId, Fragment fragment,
                           String tag, int transitionId, boolean addToBackStack) {
        add(fragmentManager, containerId, fragment, tag, transitionId, ANIM_DEFAULT, ANIM_DEFAULT,
                ANIM_DEFAULT, ANIM_DEFAULT, addToBackStack);
    }

    /**
     * @param tag                  can be null or empty string, in this case will be used
     *                             fragment.getClass().getSimpleName() as tag.
     * @param customAnimationEnter ANIM_DEFAULT or animation
     * @param customAnimationExit  ANIM_DEFAULT or animation
     */
    public static void add(FragmentManager fragmentManager, int containerId, Fragment fragment,
                           String tag, int customAnimationEnter, int customAnimationExit, boolean addToBackStack) {
        add(fragmentManager, containerId, fragment, tag, customAnimationEnter, customAnimationExit,
                ANIM_DEFAULT, ANIM_DEFAULT, addToBackStack);
    }

    /**
     * @param tag                     can be null or empty string, in this case will be used
     *                                fragment.getClass().getSimpleName() as tag.
     * @param customAnimationEnter    ANIM_DEFAULT or animation
     * @param customAnimationExit     ANIM_DEFAULT or animation
     * @param customAnimationPopEnter ANIM_DEFAULT or animation
     * @param customAnimationPopExit  ANIM_DEFAULT or animation
     */
    public static void add(FragmentManager fragmentManager, int containerId, Fragment fragment,
                           String tag, int customAnimationEnter, int customAnimationExit, int customAnimationPopEnter,
                           int customAnimationPopExit, boolean addToBackStack) {
        add(fragmentManager, containerId, fragment, tag, TRANSIT_DEFAULT, customAnimationEnter,
                customAnimationExit, customAnimationPopEnter, customAnimationPopExit, addToBackStack);
    }

    /**
     * @param tag                     can be null or empty string, in this case will be used
     *                                fragment.getClass().getSimpleName() as tag.
     * @param transitionId            TRANSIT_DEFAULT or transitionId
     * @param customAnimationEnter    ANIM_DEFAULT or animation
     * @param customAnimationExit     ANIM_DEFAULT or animation
     * @param customAnimationPopEnter ANIM_DEFAULT or animation
     * @param customAnimationPopExit  ANIM_DEFAULT or animation
     */
    private static void add(FragmentManager fragmentManager, int containerId, Fragment fragment,
                            String tag, int transitionId, int customAnimationEnter, int customAnimationExit,
                            int customAnimationPopEnter, int customAnimationPopExit, boolean addToBackStack) {
        final FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (transitionId != TRANSIT_DEFAULT) {
            transaction.setTransition(transitionId);
        }

        if (customAnimationPopEnter != ANIM_DEFAULT || customAnimationPopExit != ANIM_DEFAULT) {
            transaction.setCustomAnimations(customAnimationEnter, customAnimationExit,
                    customAnimationPopEnter, customAnimationPopExit);
        } else if (customAnimationEnter != ANIM_DEFAULT || customAnimationExit != ANIM_DEFAULT) {
            transaction.setCustomAnimations(customAnimationEnter, customAnimationExit);
        }

        transaction.add(containerId, fragment, TextUtils.isEmpty(tag) ? fragment.getClass().getSimpleName() : tag);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    public static void remove(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction().remove(fragment).commit();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Fragment> T getById(FragmentManager fragmentManager, @IdRes int id) {
        return (T) fragmentManager.findFragmentById(id);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Fragment> T getByTag(FragmentManager fragmentManager, Class<T> fragment) {
        return (T) fragmentManager.findFragmentByTag(fragment.getSimpleName());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Fragment> T getByTag(FragmentManager fragmentManager, String tag) {
        return (T) fragmentManager.findFragmentByTag(tag);
    }

    public static String getTag(Class<?> fragment) {
        return fragment.getSimpleName();
    }

    public static void popEntireBackStack(FragmentManager fragmentManager) {
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static void popEntireBackStackImmediate(FragmentManager fragmentManager) {
        fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static void popBackStackImmediate(String name, FragmentManager fragmentManager) {
        fragmentManager.popBackStackImmediate(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static void popBackStack(FragmentManager fragmentManager) {
        fragmentManager.popBackStack();
    }

    public static boolean isFragmentAdded(Fragment fragment) {
        return fragment != null && fragment.isAdded();
    }

}
