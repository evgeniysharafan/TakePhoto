package com.evgeniysharafan.takephoto.util.lib;

/**
 * onBackPressed() for fragments. Usage:
 * <pre><code>
 * Fragment fragment = Fragments.getById(getSupportFragmentManager(), R.id.content);
 * if (Fragments.isFragmentAdded(fragment) && OnBackPressedListener.class.isAssignableFrom(fragment.getClass())) {
 *     if (((OnBackPressedListener) fragment).onBackPressed()) {
 *         return;
 *     }
 * }
 * </code></pre>
 * You can put it to the general activity and implement it in each fragment which should do something
 * when onBackPressed is callsed.
 */
public interface OnBackPressedListener {

    /**
     * @return true if this back press was consumed and you shouldn't call super.onBackPressed().
     */
    boolean onBackPressed();

}
