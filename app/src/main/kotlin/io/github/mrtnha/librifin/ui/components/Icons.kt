package io.github.mrtnha.librifin.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp
import io.github.mrtnha.librifin.ui.theme.JellyfinBlue
import io.github.mrtnha.librifin.ui.theme.JellyfinPurple

/**
 * The app's icons: Material Symbols Outlined (weight 400) by Google, Apache-2.0, see README.
 * Their vector paths are embedded here, so there's no icon library and no network access.
 * New ones: download the SVG from https://fonts.google.com/icons and copy its path (the `d` attribute).
 * Tint them via `Icon(tint = …)`.
 */
object LibrifinIcons {
    /** "arrow_back". Top bar back button. */
    val ArrowBack: ImageVector by lazy {
        symbol("ArrowBack", "m313-440 224 224-57 56-320-320 320-320 57 56-224 224h487v80H313Z")
    }

    /** "check". Badge on the cover of a finished book, like Jellyfin's mark for played items. */
    val Check: ImageVector by lazy {
        symbol("Check", "M382-240 154-468l57-57 171 171 367-367 57 57-424 424Z")
    }

    /** "import_contacts" (an open book). Cover placeholder. */
    val Book: ImageVector by lazy { symbol("Book", OPEN_BOOK) }

    /**
     * The open book of [Book] in Jellyfin's gradient: purple at the bottom left to blue at the top right,
     * like the Jellyfin logo. The app logo; show it with `Image`, as tinting would hide the gradient.
     */
    val Logo: ImageVector by lazy {
        symbol(
            "Logo",
            OPEN_BOOK,
            fill = Brush.linearGradient(
                listOf(JellyfinPurple, JellyfinBlue),
                // In the path's coordinates, see symbol().
                start = Offset(40f, -160f),
                end = Offset(920f, -800f),
            ),
        )
    }

    private const val OPEN_BOOK =
        "M260-320q47 0 91.5 10.5T440-278v-394q-41-24-87-36t-93-12q-36 0-71.5 7T120-692v396q35-12 69.5-18" +
            "t70.5-6Zm260 42q44-21 88.5-31.5T700-320q36 0 70.5 6t69.5 18v-396q-33-14-68.5-21t-71.5-7q-47 0-93 12" +
            "t-87 36v394Zm-40 118q-48-38-104-59t-116-21q-42 0-82.5 11T100-198q-21 11-40.5-1T40-234v-482" +
            "q0-11 5.5-21T62-752q46-24 96-36t102-12q58 0 113.5 15T480-740q51-30 106.5-45T700-800q52 0 102 12" +
            "t96 36q11 5 16.5 15t5.5 21v482q0 23-19.5 35t-40.5 1q-37-20-77.5-31T700-240q-60 0-116 21t-104 59Z" +
            "M280-494Z"

    /** "info". Help next to the server address field. */
    val Info: ImageVector by lazy {
        symbol(
            "Info",
            "M440-280h80v-240h-80v240Zm68.5-331.5Q520-623 520-640t-11.5-28.5Q497-680 480-680t-28.5 11.5" +
                "Q440-657 440-640t11.5 28.5Q463-600 480-600t28.5-11.5ZM480-80q-83 0-156-31.5T197-197q-54-54-85.5-127" +
                "T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480" +
                "q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm0-80q134 0 227-93t93-227q0-134-93-227t-227-93" +
                "q-134 0-227 93t-93 227q0 134 93 227t227 93Zm0-320Z",
        )
    }

    /** "login". Log in button. */
    val Login: ImageVector by lazy {
        symbol(
            "Login",
            "M480-120v-80h280v-560H480v-80h280q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H480Z" +
                "m-80-160-55-58 102-102H120v-80h327L345-622l55-58 200 200-200 200Z",
        )
    }

    /** "logout". Log out button. */
    val Logout: ImageVector by lazy {
        symbol(
            "Logout",
            "M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h280v80H200v560h280v80H200Z" +
                "m440-160-55-58 102-102H360v-80h327L585-622l55-58 200 200-200 200Z",
        )
    }

    /** "account_circle". Profile button in the library top bar. */
    val Profile: ImageVector by lazy {
        symbol(
            "Profile",
            "M234-276q51-39 114-61.5T480-360q69 0 132 22.5T726-276q35-41 54.5-93T800-480q0-133-93.5-226.5T480-800" +
                "q-133 0-226.5 93.5T160-480q0 59 19.5 111t54.5 93Zm146.5-204.5Q340-521 340-580t40.5-99.5" +
                "Q421-720 480-720t99.5 40.5Q620-639 620-580t-40.5 99.5Q539-440 480-440t-99.5-40.5ZM480-80" +
                "q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880" +
                "q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Zm100-95.5" +
                "q47-15.5 86-44.5-39-29-86-44.5T480-280q-53 0-100 15.5T294-220q39 29 86 44.5T480-160q53 0 100-15.5Z" +
                "M523-537q17-17 17-43t-17-43q-17-17-43-17t-43 17q-17 17-17 43t17 43q17 17 43 17t43-17Zm-43-43Zm0 360Z",
        )
    }

    /** "close". Clears the search text. */
    val Close: ImageVector by lazy {
        symbol(
            "Close",
            "m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z",
        )
    }

    /** "search". Search button in the library top bar. */
    val Search: ImageVector by lazy {
        symbol(
            "Search",
            "M784-120 532-372q-30 24-69 38t-83 14q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840" +
                "q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l252 252-56 56ZM380-400q75 0 127.5-52.5T560-580" +
                "q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z",
        )
    }

    /** "match_case" ("Aa"). Reader app bar: opens the appearance sheet (theme, text size, font). */
    val MatchCase: ImageVector by lazy {
        symbol(
            "MatchCase",
            "m131-252 165-440h79l165 440h-76l-39-112H247l-40 112h-76Zm139-176h131l-64-182h-4l-63 182Zm395 186" +
                "q-51 0-81-27.5T554-342q0-44 34.5-72.5T677-443q23 0 45 4t38 11v-12q0-29-20.5-47T685-505q-23 0-42 9.5" +
                "T610-468l-47-35q24-29 54.5-43t68.5-14q69 0 103 32.5t34 97.5v178h-63v-37h-4q-14 23-38 35t-53 12Z" +
                "m12-54q35 0 59.5-24t24.5-56q-14-8-33.5-12.5T689-393q-32 0-50 14t-18 37q0 20 16 33t40 13Z",
        )
    }

    /** "keyboard_arrow_up". Reader search bar: previous match. */
    val KeyboardArrowUp: ImageVector by lazy {
        symbol("KeyboardArrowUp", "M480-528 296-344l-56-56 240-240 240 240-56 56-184-184Z")
    }

    /** "keyboard_arrow_down". Reader search bar: next match. */
    val KeyboardArrowDown: ImageVector by lazy {
        symbol("KeyboardArrowDown", "M480-344 240-584l56-56 184 184 184-184 56 56-240 240Z")
    }

    /**
     * A 24 dp icon from the path of a Material Symbols SVG. Their viewBox is "0 -960 960 960": 960 units
     * wide and high, from y = -960 at the top to 0 at the bottom, so the path is moved down by 960.
     */
    private fun symbol(name: String, path: String, fill: Brush = SolidColor(Color.Black)): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).addGroup(translationY = 960f).addPath(addPathNodes(path), fill = fill).build()
}
