package io.github.mrtnha.librifin.ui.reader

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import org.readium.r2.navigator.epub.EpubNavigatorFragment

/**
 * The activity's [FragmentFactory]. Readium's [EpubNavigatorFragment] needs the open book in its
 * constructor, and Android recreates fragments on its own (e.g. on rotation), so the activity must
 * be able to build it at any time: from the book that is currently open.
 *
 * Set it on the activity's fragment manager before `super.onCreate()`.
 */
object ReaderFragmentFactory : FragmentFactory() {
    /** Readium's factory for the book that is currently open, or null if no book is open. */
    internal var epub: FragmentFactory? = null

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment =
        if (className == EpubNavigatorFragment::class.java.name) {
            // No open book: Android is restoring a reader after the app was killed. Use a placeholder,
            // removed right away by removeOrphans().
            (epub ?: EpubNavigatorFragment.createDummyFactory()).instantiate(classLoader, className)
        } else {
            super.instantiate(classLoader, className)
        }

    /**
     * Removes readers that Android restored after the app was killed: their book is gone, and the app
     * starts again at the library. Call right after `super.onCreate()`.
     */
    fun removeOrphans(fragmentManager: FragmentManager) {
        if (epub != null) return
        val orphans = fragmentManager.fragments.filterIsInstance<EpubNavigatorFragment>()
        if (orphans.isEmpty()) return
        val transaction = fragmentManager.beginTransaction()
        orphans.forEach { transaction.remove(it) }
        transaction.commitNowAllowingStateLoss()
    }
}
