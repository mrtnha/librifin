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

    /** "download" (an arrow into a tray). Badge on the cover of a book that's on the phone. */
    val Download: ImageVector by lazy {
        symbol(
            "Download",
            "M480-320 280-520l56-58 104 104v-326h80v326l104-104 56 58-200 200ZM240-160q-33 0-56.5-23.5" +
                "T160-240v-120h80v120h480v-120h80v120q0 33-23.5 56.5T720-160H240Z",
        )
    }

    /** "cloud". Library top bar: all books are shown, the ones on the server too. */
    val Cloud: ImageVector by lazy {
        symbol(
            "Cloud",
            "M260-160q-91 0-155.5-63T40-377q0-78 47-139t123-78q25-92 100-149t170-57q117 0 198.5 81.5T760-520" +
                "q69 8 114.5 59.5T920-340q0 75-52.5 127.5T740-160H260Zm0-80h480q42 0 71-29t29-71q0-42-29-71" +
                "t-71-29h-60v-80q0-83-58.5-141.5T480-720q-83 0-141.5 58.5T280-520h-20q-58 0-99 41t-41 99q0 58 41 99" +
                "t99 41Zm220-240Z",
        )
    }

    /** "cloud_off" (a crossed-out cloud). Library top bar: only the downloaded books are shown. */
    val CloudOff: ImageVector by lazy {
        symbol(
            "CloudOff",
            "M792-56 686-160H260q-92 0-156-64T40-380q0-77 47.5-137T210-594q3-8 6-15.5t6-16.5L56-792l56-56 736 736" +
                "-56 56ZM260-240h346L284-562q-2 11-3 21t-1 21h-20q-58 0-99 41t-41 99q0 58 41 99t99 41Zm185-161Z" +
                "m419 191-58-56q17-14 25.5-32.5T840-340q0-42-29-71t-71-29h-60v-80q0-83-58.5-141.5T480-720" +
                "q-27 0-52 6.5T380-693l-58-58q35-24 74.5-36.5T480-800q117 0 198.5 81.5T760-520q69 8 114.5 59.5" +
                "T920-340q0 39-15 72.5T864-210ZM593-479Z",
        )
    }

    /** "import_contacts" (an open book). Cover placeholder. */
    val Book: ImageVector by lazy { symbol("Book", OPEN_BOOK) }

    /**
     * The open book of [Book] in Jellyfin's gradient: purple at the top left to blue at the bottom right,
     * along the same line as in the launcher icon (ic_launcher_foreground.xml). The app logo; show it with
     * `Image`, as tinting would hide the gradient.
     */
    val Logo: ImageVector by lazy {
        symbol(
            "Logo",
            OPEN_BOOK,
            fill = Brush.linearGradient(
                listOf(JellyfinPurple, JellyfinBlue),
                // In the path's coordinates, see symbol().
                start = Offset(172f, -570f),
                end = Offset(986f, -100f),
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

    /** "info". Help next to the server address field, and the settings row with the app's version. */
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

    /** "settings". Settings button in the library top bar. */
    val Settings: ImageVector by lazy {
        symbol(
            "Settings",
            "m370-80-16-128q-13-5-24.5-12T307-235l-119 50L78-375l103-78q-1-7-1-13.5v-27q0-6.5 1-13.5L78-585" +
                "l110-190 119 50q11-8 23-15t24-12l16-128h220l16 128q13 5 24.5 12t22.5 15l119-50 110 190-103 78" +
                "q1 7 1 13.5v27q0 6.5-2 13.5l103 78-110 190-118-50q-11 8-23 15t-24 12L590-80H370Zm70-80h79l14-106" +
                "q31-8 57.5-23.5T639-327l99 41 39-68-86-65q5-14 7-29.5t2-31.5q0-16-2-31.5t-7-29.5l86-65-39-68-99 42" +
                "q-22-23-48.5-38.5T533-694l-13-106h-79l-14 106q-31 8-57.5 23.5T321-633l-99-41-39 68 86 64q-5 15-7 30" +
                "t-2 32q0 16 2 31t7 30l-86 65 39 68 99-42q22 23 48.5 38.5T427-266l13 106Zm42-180q58 0 99-41t41-99" +
                "q0-58-41-99t-99-41q-59 0-99.5 41T342-480q0 58 40.5 99t99.5 41Zm-2-140Z",
        )
    }

    /** "delete" (a trash can). Settings row that removes the downloaded books. */
    val Delete: ImageVector by lazy {
        symbol(
            "Delete",
            "M280-120q-33 0-56.5-23.5T200-200v-520h-40v-80h200v-40h240v40h200v80h-40v520q0 33-23.5 56.5" +
                "T680-120H280Zm400-600H280v520h400v-520ZM360-280h80v-360h-80v360Zm160 0h80v-360h-80v360Z" +
                "M280-720v520-520Z",
        )
    }

    /** "chevron_right". End of a settings row that opens another screen. */
    val ChevronRight: ImageVector by lazy {
        symbol("ChevronRight", "M504-480 320-664l56-56 240 240-240 240-56-56 184-184Z")
    }

    /** "description" (a page of text). Open source licenses row in the settings. */
    val Licenses: ImageVector by lazy {
        symbol(
            "Licenses",
            "M320-240h320v-80H320v80Zm0-160h320v-80H320v80ZM240-80q-33 0-56.5-23.5T160-160v-640q0-33 23.5-56.5" +
                "T240-880h320l240 240v480q0 33-23.5 56.5T720-80H240Zm280-520v-200H240v640h480v-440H520Z" +
                "M240-800v200-200 640-640Z",
        )
    }

    /** "account_circle". Settings row with the name of the logged-in user. */
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

    /** "dns" (a server). Settings row with the address of the server the user is logged in to. */
    val Server: ImageVector by lazy {
        symbol(
            "Server",
            "M300-720q-25 0-42.5 17.5T240-660q0 25 17.5 42.5T300-600q25 0 42.5-17.5T360-660q0-25-17.5-42.5" +
                "T300-720Zm0 400q-25 0-42.5 17.5T240-260q0 25 17.5 42.5T300-200q25 0 42.5-17.5T360-260" +
                "q0-25-17.5-42.5T300-320ZM160-840h640q17 0 28.5 11.5T840-800v280q0 17-11.5 28.5T800-480H160" +
                "q-17 0-28.5-11.5T120-520v-280q0-17 11.5-28.5T160-840Zm40 80v200h560v-200H200Zm-40 320h640" +
                "q17 0 28.5 11.5T840-400v280q0 17-11.5 28.5T800-80H160q-17 0-28.5-11.5T120-120v-280" +
                "q0-17 11.5-28.5T160-440Zm40 80v200h560v-200H200Zm0-400v200-200Zm0 400v200-200Z",
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

    /** "sort". Sort button in the library top bar: opens the sheet to choose the order of the books. */
    val Sort: ImageVector by lazy {
        symbol("Sort", "M120-240v-80h240v80H120Zm0-200v-80h480v80H120Zm0-200v-80h720v80H120Z")
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
